package guru.qa.niffler.service.impl.api;

import guru.qa.niffler.api.SpendApi;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.RestClient;
import guru.qa.niffler.service.SpendClient;
import io.qameta.allure.Step;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ParametersAreNonnullByDefault
public final class SpendApiClient extends RestClient implements SpendClient {

    private final SpendApi spendApi;

    public SpendApiClient() {
        super(CFG.spendUrl());
        this.spendApi = create(SpendApi.class);
    }

    @Step("Создать трату: {spend.description()}")
    @Override
    public @Nullable SpendJson createSpend(SpendJson spend) {
        final Response<SpendJson> response;
        try {
            response = spendApi.createSpend(spend)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(201, response.code());
        return response.body();
    }

    @Step("Редактировать трату: {spend.description()}")
    public @Nullable SpendJson editSpend(SpendJson spend) {
        final Response<SpendJson> response;
        try {
            response = spendApi.editSpend(spend)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return response.body();
    }

    @Step("Получить трату по ID: {id}, пользователь: {username}")
    public @Nullable SpendJson getSpend(Integer id, String username) {
        final Response<SpendJson> response;
        try {
            response = spendApi.getSpend(id, username)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return response.body();
    }

    @Step("Получить все траты пользователя: {username}")
    public @Nonnull List<SpendJson> allSpends(String username) {
        final Response<List<SpendJson>> response;
        try {
            response = spendApi.getAllSpends(username)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return response.body() != null ? response.body() : Collections.emptyList();
    }

    @Step("Удалить траты пользователя: {username}, IDs: {ids}")
    public Response<Void> deleteSpend(String username, List<String> ids) {
        final Response<Void> response;
        try {
            response = spendApi.removeSpend(username, ids)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(202, response.code());
        return response;
    }

    @Step("Создать категорию: {category.name()}")
    @Override
    public @Nullable CategoryJson createCategory(CategoryJson category) {
        final Response<CategoryJson> response;
        try {
            response = spendApi.createCategory(category)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return response.body();
    }

    @Step("Обновить категорию: {category.name()}")
    @Override
    public @Nullable CategoryJson updateCategory(CategoryJson category) {
        final Response<CategoryJson> response;
        try {
            response = spendApi.updateCategory(category)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return response.body();
    }

    @Step("Получить все категории пользователя: {username}")
    public @Nonnull List<CategoryJson> getAllCategories(String username) {
        final Response<List<CategoryJson>> response;
        try {
            response = spendApi.getAllCategories(username)
                    .execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return response.body() != null ? response.body() : Collections.emptyList();
    }

    @Step("Найти категорию по имени: {categoryName}, пользователь: {username}")
    @Override
    public @Nonnull Optional<CategoryJson> findCategoryByNameAndUsername(String categoryName, String username) {
        final Response<List<CategoryJson>> response;
        try {
            response = spendApi.getAllCategories(username).execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return response.body().stream()
                .filter(c -> c.name().equals(categoryName))
                .findFirst();
    }
}