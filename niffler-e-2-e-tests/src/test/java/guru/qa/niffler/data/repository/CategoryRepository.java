package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.spend.CategoryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    CategoryEntity create(CategoryEntity category);
    Optional<CategoryEntity> findById(UUID id);
    Optional<CategoryEntity> findByUsernameAndName(String username, String name);
    List<CategoryEntity> findByUsername(String username);
    CategoryEntity update(CategoryEntity category);
    void delete(CategoryEntity category);
    List<CategoryEntity> findAll();
}