package guru.qa.niffler.service;

import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface SpendClient {

    SpendJson createSpend(SpendJson spend);

    CategoryJson createCategory(CategoryJson category);

    Optional<CategoryJson> findCategoryByNameAndUsername(String categoryName, String username);

    CategoryJson updateCategory(CategoryJson category);

    List<CategoryJson> getAllCategories(String username);

    List<SpendJson> allSpends(String username);
}
