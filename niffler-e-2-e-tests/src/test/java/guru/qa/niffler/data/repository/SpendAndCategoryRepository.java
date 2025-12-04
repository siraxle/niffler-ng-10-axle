package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpendAndCategoryRepository {

    SpendEntity createSpend(SpendEntity spend);

    SpendEntity updateSpend(SpendEntity spend);

    Optional<SpendEntity> findSpendById(UUID id);

    List<SpendEntity> findSpendsByUsername(String username);

    List<SpendEntity> findSpendsByUsernameAndDescription(String username, String description);

    List<SpendEntity> findSpendsByCategory(String categoryName, String username);

    void removeSpend(SpendEntity spend);

    List<SpendEntity> findAllSpends();

    CategoryEntity createCategory(CategoryEntity category);

    CategoryEntity updateCategory(CategoryEntity category);

    Optional<CategoryEntity> findCategoryById(UUID id);

    Optional<CategoryEntity> findCategoryByUsernameAndName(String username, String name);

    List<CategoryEntity> findCategoriesByUsername(String username);

    void removeCategory(CategoryEntity category);

    List<CategoryEntity> findAllCategories();
}