package guru.qa.niffler.test.gql;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.rx2.Rx2Apollo;
import guru.qa.StatQuery;
import guru.qa.niffler.jupiter.annotation.*;
import guru.qa.niffler.model.CurrencyValues;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatGraphQlControllerTest extends BaseGraphQlTest {

    @Test
    @User(
            categories = {
                    @Category(name = "Active category 1"),
                    @Category(name = "Active category 2"),
                    @Category(name = "Archive category 1", archived = true),
                    @Category(name = "Archive category 2", archived = true)
            },
            spendings = {
                    @Spending(category = "Active category 1", description = "Spending with active cat 1", amount = 100, currency = CurrencyValues.RUB),
                    @Spending(category = "Active category 2", description = "Spending with active cat 2", amount = 200, currency = CurrencyValues.EUR),
                    @Spending(category = "Archive category 1", description = "Spending with archive cat 1", amount = 300, currency = CurrencyValues.USD),
                    @Spending(category = "Archive category 2", description = "Spending with archive cat 2", amount = 400, currency = CurrencyValues.KZT)
            }
    )
    @ApiLogin
    public void archivedCategoriesShouldBeGroupedAsArchived(@Token String bearerToken) {
        ApolloCall<StatQuery.Data> call = apolloClient.query(StatQuery.builder().build())
                .addHttpHeader("authorization", bearerToken);

        ApolloResponse<StatQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        List<StatQuery.StatByCategory> statByCategories = response.dataOrThrow().stat.statByCategories;

        step("Архивные категории должны группироваться под именем 'Archived'", () -> {
            // Должно быть 3 записи: 2 активные + 1 объединенная архивная
            assertEquals(3, statByCategories.size());

            assertTrue(statByCategories.stream()
                    .anyMatch(c -> c.categoryName.equals("Active category 1")));
            assertTrue(statByCategories.stream()
                    .anyMatch(c -> c.categoryName.equals("Active category 2")));

            var archivedCategory = statByCategories.stream()
                    .filter(c -> c.categoryName.equals("Archived"))
                    .findFirst()
                    .orElseThrow();

            assertEquals("Archived", archivedCategory.categoryName);
            assertEquals(CurrencyValues.RUB.name(), archivedCategory.currency.rawValue);
            assertTrue(archivedCategory.sum > 0);
        });
    }

    @Test
    @User(
            categories = {
                    @Category(name = "Multi currency category"),
                    @Category(name = "Single currency category")
            },
            spendings = {
                    @Spending(category = "Multi currency category", description = "Spend RUB", amount = 1000, currency = CurrencyValues.RUB),
                    @Spending(category = "Multi currency category", description = "Spend USD", amount = 50, currency = CurrencyValues.USD),
                    @Spending(category = "Multi currency category", description = "Spend EUR", amount = 40, currency = CurrencyValues.EUR),
                    @Spending(category = "Single currency category", description = "Spend KZT", amount = 5000, currency = CurrencyValues.KZT)
            }
    )
    @ApiLogin
    public void statShouldAggregateDifferentCurrencies(@Token String bearerToken) {
        ApolloCall<StatQuery.Data> call = apolloClient.query(StatQuery.builder().build())
                .addHttpHeader("authorization", bearerToken);

        ApolloResponse<StatQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        var stat = response.dataOrThrow().stat;

        step("Статистика должна агрегировать траты в разных валютах", () -> {
            // Проверяем, что общая сумма рассчитана (конвертация валют работает)
            assertEquals(CurrencyValues.RUB.name(), stat.currency.rawValue);
            // Сумма должна быть > 0 после конвертации
            assertEquals(true, stat.total > 0);
        });
    }

    @Test
    @User(
            categories = @Category(name = "Filter test category"),
            spendings = {
                    @Spending(category = "Filter test category", description = "RUB трата", amount = 1000, currency = CurrencyValues.RUB),
                    @Spending(category = "Filter test category", description = "USD трата", amount = 100, currency = CurrencyValues.USD),
                    @Spending(category = "Filter test category", description = "EUR трата", amount = 50, currency = CurrencyValues.EUR)
            }
    )
    @ApiLogin
    public void statWithCurrencyFilterShouldReturnFilteredResult(@Token String bearerToken) {
        ApolloCall<StatQuery.Data> call = apolloClient.query(
                        StatQuery.builder()
                                .filterCurrency(guru.qa.type.CurrencyValues.USD)
                                .build())
                .addHttpHeader("authorization", bearerToken);

        ApolloResponse<StatQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        StatQuery.Stat stat = response.dataOrThrow().stat;

        step("Фильтр по валюте должен оставлять только траты в указанной валюте", () -> {
            // Валюта результата - RUB (дефолтная)
            assertEquals(CurrencyValues.RUB.name(), stat.currency.rawValue);
            // Сумма соответствует конвертированным USD тратам (100 USD → 6666.67 RUB)
            assertEquals(6666.67, stat.total, 0.01);
            // Только одна категория (не должно быть RUB и EUR трат)
            assertEquals(1, stat.statByCategories.size());
            assertEquals("Filter test category", stat.statByCategories.get(0).categoryName);
        });
    }

    @Test
    @User(
            categories = @Category(name = "Currency combo category"),
            spendings = {
                    @Spending(category = "Currency combo category", description = "RUB трата", amount = 1000, currency = CurrencyValues.RUB),
                    @Spending(category = "Currency combo category", description = "USD трата", amount = 100, currency = CurrencyValues.USD),
                    @Spending(category = "Currency combo category", description = "EUR трата", amount = 50, currency = CurrencyValues.EUR)
            }
    )
    @ApiLogin
    public void statWithBothFilterAndStatCurrency(@Token String bearerToken) {
        // Фильтруем USD траты, результат в EUR
        ApolloCall<StatQuery.Data> call = apolloClient.query(
                        StatQuery.builder()
                                .filterCurrency(guru.qa.type.CurrencyValues.USD)
                                .statCurrency(guru.qa.type.CurrencyValues.EUR)
                                .build())
                .addHttpHeader("authorization", bearerToken);

        ApolloResponse<StatQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        StatQuery.Stat stat = response.dataOrThrow().stat;

        step("filterCurrency фильтрует траты, statCurrency задает валюту результата", () -> {
            // Результат в EUR
            assertEquals(CurrencyValues.EUR.name(), stat.currency.rawValue);
            // 100 USD конвертированные в EUR
            // Нужно узнать курс USD→EUR для точной проверки
            assertTrue(stat.total > 0);
            assertEquals(1, stat.statByCategories.size());
        });
    }

    @Test
    @User(
            categories = @Category(name = "Currency conversion category"),
            spendings = {
                    @Spending(category = "Currency conversion category", description = "EUR трата", amount = 100, currency = CurrencyValues.EUR),
                    @Spending(category = "Currency conversion category", description = "USD трата", amount = 50, currency = CurrencyValues.USD)
            }
    )
    @ApiLogin
    public void statCurrencyParameterShouldConvertToTargetCurrency(@Token String bearerToken) {
        // Запрашиваем статистику в KZT
        ApolloCall<StatQuery.Data> call = apolloClient.query(
                        StatQuery.builder()
                                .statCurrency(guru.qa.type.CurrencyValues.KZT)
                                .build())
                .addHttpHeader("authorization", bearerToken);

        ApolloResponse<StatQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        StatQuery.Stat stat = response.dataOrThrow().stat;

        step("Параметр statCurrency должен конвертировать все траты в целевую валюту", () -> {
            assertEquals(CurrencyValues.KZT.name(), stat.currency.rawValue);
            // Сумма в KZT должна быть > 0 (после конвертации)
            assertEquals(true, stat.total > 0);
        });
    }

    @Test
    @User(
            categories = @Category(name = "Empty stat category"),
            spendings = {} // Нет трат
    )
    @ApiLogin
    public void statWithNoSpendsShouldReturnZero(@Token String bearerToken) {
        ApolloCall<StatQuery.Data> call = apolloClient.query(StatQuery.builder().build())
                .addHttpHeader("authorization", bearerToken);

        ApolloResponse<StatQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        StatQuery.Stat stat = response.dataOrThrow().stat;

        step("Статистика без трат должна возвращать 0", () -> {
            assertEquals(0.0, stat.total, 0.01);
            assertEquals(0, stat.statByCategories.size());
        });
    }
}