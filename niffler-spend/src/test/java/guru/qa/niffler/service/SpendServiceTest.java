package guru.qa.niffler.service;

import guru.qa.niffler.data.CategoryEntity;
import guru.qa.niffler.data.SpendEntity;
import guru.qa.niffler.data.repository.SpendRepository;
import guru.qa.niffler.ex.SpendNotFoundException;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class SpendServiceTest {

    @Test
    void getSpendForUserShouldThrowExceptionInCaseThatIdIsIncorrectionFormat(
            @Mock SpendRepository spendRepository, @Mock CategoryService categoryService
            ) {
        final String incorrectId = "incorrect_id";
        SpendService spendService = new SpendService(spendRepository, categoryService);
        SpendNotFoundException ex = Assertions.assertThrows(
                SpendNotFoundException.class,
                () -> spendService.getSpendForUser(incorrectId, "dina"));
        Assertions.assertEquals(
                "Can`t find spend by given id: " + incorrectId,
                ex.getMessage()
        );
    }

    @Test
    void getSpendForUserShouldThrowExceptionInCaseThatSpendNotFoundInDb(
            @Mock SpendRepository spendRepository, @Mock CategoryService categoryService
    ) {
        final String correctId = UUID.randomUUID().toString();
        final String correctUsername = "dina";

        Mockito.when(spendRepository.findByIdAndUsername(eq(UUID.fromString(correctId)), eq(correctUsername)))
                .thenReturn(Optional.empty());

        SpendService spendService = new SpendService(spendRepository, categoryService);
        SpendNotFoundException ex = Assertions.assertThrows(
                SpendNotFoundException.class,
                () -> spendService.getSpendForUser(correctId, "dina"));
        Assertions.assertEquals(
                "Can`t find spend by given id: " + correctId,
                ex.getMessage()
        );
    }

    @Test
    void getSpendForUserShouldReturnCorrectJsonObject(
            @Mock SpendRepository spendRepository, @Mock CategoryService categoryService
    ) {
        final String correctId = UUID.randomUUID().toString();
        final String correctUsername = "dina";
        final SpendEntity spend = new  SpendEntity();
        final CategoryEntity category = new  CategoryEntity();
        spend.setId(UUID.fromString(correctId));
        spend.setUsername(correctUsername);
        spend.setCurrency(CurrencyValues.RUB);
        spend.setAmount(150.16);
        spend.setDescription("unit-test spend description");
        spend.setSpendDate(new Date(0));

        category.setUsername(correctUsername);
        category.setName("unit-test category");
        category.setArchived(false);
        category.setId(UUID.randomUUID());
        spend.setCategory(category);

        Mockito.when(spendRepository.findByIdAndUsername(eq(UUID.fromString(correctId)), eq(correctUsername)))
                .thenReturn(Optional.of(
                        spend
                ));

        SpendService spendService = new SpendService(spendRepository, categoryService);
        SpendJson result = spendService.getSpendForUser(correctId, correctUsername);
        Mockito.verify(spendRepository, Mockito.times(1))
                .findByIdAndUsername(eq(UUID.fromString(correctId)), eq(correctUsername));

        Assertions.assertEquals(
                "unit-test spend description",
                result.description()
        );
    }

}