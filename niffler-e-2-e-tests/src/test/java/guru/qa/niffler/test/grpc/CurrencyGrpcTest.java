package guru.qa.niffler.test.grpc;

import com.google.protobuf.Empty;
import guru.qa.niffler.grpc.CalculateRequest;
import guru.qa.niffler.grpc.CalculateResponse;
import guru.qa.niffler.grpc.Currency;
import guru.qa.niffler.grpc.CurrencyResponse;
import guru.qa.niffler.grpc.CurrencyValues; // gRPC enum
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyGrpcTest extends BaseGgrpcTest {

    @Test
    public void allCurrenciesShouldReturned() {
        CurrencyResponse response = blockingStub.getAllCurrencies(Empty.getDefaultInstance());
        final List<Currency> allCurrenciesList = response.getAllCurrenciesList();
        assertEquals(4, allCurrenciesList.size());
    }

    @Test
    public void shouldReturnAllCurrencies() {
        CurrencyResponse response = blockingStub.getAllCurrencies(Empty.getDefaultInstance());
        List<Currency> allCurrenciesList = response.getAllCurrenciesList();

        assertEquals(4, allCurrenciesList.size());

        assertTrue(allCurrenciesList.stream()
                .anyMatch(c -> c.getCurrency() == CurrencyValues.RUB));
        assertTrue(allCurrenciesList.stream()
                .anyMatch(c -> c.getCurrency() == CurrencyValues.USD));
        assertTrue(allCurrenciesList.stream()
                .anyMatch(c -> c.getCurrency() == CurrencyValues.EUR));
        assertTrue(allCurrenciesList.stream()
                .anyMatch(c -> c.getCurrency() == CurrencyValues.KZT));

        // Проверяем, что курсы валют > 0
        allCurrenciesList.forEach(currency ->
                assertTrue(currency.getCurrencyRate() > 0));
    }

    @Test
    public void shouldReturnEmptyWhenNoCurrencies() {
        CurrencyResponse response = blockingStub.getAllCurrencies(Empty.getDefaultInstance());
        assertNotNull(response);
    }

    @ParameterizedTest
    @MethodSource("conversionTestCases")
    public void shouldCalculateRateCorrectly(
            double amount,
            CurrencyValues spendCurrency,
            CurrencyValues desiredCurrency,
            double expectedMin,
            double expectedMax) {

        CalculateRequest request = CalculateRequest.newBuilder()
                .setAmount(amount)
                .setSpendCurrency(spendCurrency)
                .setDesiredCurrency(desiredCurrency)
                .build();

        CalculateResponse response = blockingStub.calculateRate(request);
        double result = response.getCalculatedAmount();

        assertTrue(result >= expectedMin && result <= expectedMax,
                String.format("Result %.2f not in range [%.2f, %.2f]",
                        result, expectedMin, expectedMax));
    }

    private static Stream<Arguments> conversionTestCases() {
        return Stream.of(
                Arguments.of(100.0, CurrencyValues.RUB, CurrencyValues.USD, 1.0, 1.5),
                Arguments.of(1.0, CurrencyValues.USD, CurrencyValues.RUB, 60.0, 100.0),
                Arguments.of(1.0, CurrencyValues.EUR, CurrencyValues.RUB, 60.0, 110.0),
                Arguments.of(1000.0, CurrencyValues.KZT, CurrencyValues.RUB, 130.0, 200.0),
                Arguments.of(100.0, CurrencyValues.USD, CurrencyValues.EUR, 90.0, 100.0),
                Arguments.of(100.0, CurrencyValues.EUR, CurrencyValues.USD, 100.0, 120.0),
                Arguments.of(150.0, CurrencyValues.RUB, CurrencyValues.RUB, 150.0, 150.0),
                Arguments.of(200.0, CurrencyValues.USD, CurrencyValues.USD, 200.0, 200.0)
        );
    }

    @Test
    public void shouldConvertZeroAmount() {
        CalculateRequest request = CalculateRequest.newBuilder()
                .setAmount(0.0)
                .setSpendCurrency(CurrencyValues.USD)
                .setDesiredCurrency(CurrencyValues.RUB)
                .build();

        CalculateResponse response = blockingStub.calculateRate(request);
        assertEquals(0.0, response.getCalculatedAmount(), 0.001);
    }

    @Test
    public void shouldConvertLargeAmounts() {
        CalculateRequest request = CalculateRequest.newBuilder()
                .setAmount(1_000_000.0)
                .setSpendCurrency(CurrencyValues.USD)
                .setDesiredCurrency(CurrencyValues.RUB)
                .build();

        CalculateResponse response = blockingStub.calculateRate(request);
        assertTrue(response.getCalculatedAmount() > 0);
    }

    @Test
    public void shouldThrowExceptionForInvalidCurrency() {
        CalculateRequest request = CalculateRequest.newBuilder()
                .setAmount(100.0)
                .setSpendCurrency(CurrencyValues.CURRENCY_UNSPECIFIED)
                .setDesiredCurrency(CurrencyValues.USD)
                .build();

        assertThrows(StatusRuntimeException.class, () -> {
            blockingStub.calculateRate(request);
        });
    }

    @Test
    public void calculateRateShouldBeCommutative() {
        double amount = 100.0;

        CalculateRequest requestRubToUsd = CalculateRequest.newBuilder()
                .setAmount(amount)
                .setSpendCurrency(CurrencyValues.RUB)
                .setDesiredCurrency(CurrencyValues.USD)
                .build();

        CalculateResponse responseRubToUsd = blockingStub.calculateRate(requestRubToUsd);
        double usdAmount = responseRubToUsd.getCalculatedAmount();

        CalculateRequest requestUsdToRub = CalculateRequest.newBuilder()
                .setAmount(usdAmount)
                .setSpendCurrency(CurrencyValues.USD)
                .setDesiredCurrency(CurrencyValues.RUB)
                .build();

        CalculateResponse responseUsdToRub = blockingStub.calculateRate(requestUsdToRub);

        assertEquals(amount, responseUsdToRub.getCalculatedAmount(), 0.1);
    }

    @Test
    public void multipleCurrenciesShouldHaveConsistentRates() {
        double amount = 1000.0;
        CalculateRequest directRequest = CalculateRequest.newBuilder()
                .setAmount(amount)
                .setSpendCurrency(CurrencyValues.RUB)
                .setDesiredCurrency(CurrencyValues.USD)
                .build();
        double directResult = blockingStub.calculateRate(directRequest).getCalculatedAmount();

        CalculateRequest rubToEurRequest = CalculateRequest.newBuilder()
                .setAmount(amount)
                .setSpendCurrency(CurrencyValues.RUB)
                .setDesiredCurrency(CurrencyValues.EUR)
                .build();
        double eurAmount = blockingStub.calculateRate(rubToEurRequest).getCalculatedAmount();

        CalculateRequest eurToUsdRequest = CalculateRequest.newBuilder()
                .setAmount(eurAmount)
                .setSpendCurrency(CurrencyValues.EUR)
                .setDesiredCurrency(CurrencyValues.USD)
                .build();
        double crossResult = blockingStub.calculateRate(eurToUsdRequest).getCalculatedAmount();

        assertEquals(directResult, crossResult, 0.5);
    }

}