package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendAndCategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jpa.EntityManagers.em;

@ParametersAreNonnullByDefault
public class SpendAndCategoryRepositoryHibernate implements SpendAndCategoryRepository {

    private static final Config CFG = Config.getInstance();
    private final EntityManager entityManager = em(CFG.spendJdbcUrl());

    @Override
    @Nonnull
    public SpendEntity createSpend(SpendEntity spend) {
        entityManager.joinTransaction();
        if (spend.getCategory() != null && spend.getCategory().getId() != null) {
            CategoryEntity managedCategory = entityManager.find(CategoryEntity.class, spend.getCategory().getId());
            spend.setCategory(managedCategory);
        }

        entityManager.persist(spend);
        return spend;
    }

    @Override
    @Nonnull
    public SpendEntity updateSpend(SpendEntity spend) {
        entityManager.joinTransaction();
        return entityManager.merge(spend);
    }

    @Override
    @Nonnull
    public Optional<SpendEntity> findSpendById(UUID id) {
        return Optional.ofNullable(entityManager.find(SpendEntity.class, id));
    }

    @Override
    @Nonnull
    public List<SpendEntity> findSpendsByUsername(String username) {
        List<SpendEntity> result = entityManager.createQuery(
                        "SELECT s FROM SpendEntity s WHERE s.username = :username ORDER BY s.spendDate DESC",
                        SpendEntity.class)
                .setParameter("username", username)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    @Nonnull
    public List<SpendEntity> findSpendsByUsernameAndDescription(String username, String description) {
        List<SpendEntity> result = entityManager.createQuery(
                        "SELECT s FROM SpendEntity s WHERE s.username = :username AND s.description LIKE :description ORDER BY s.spendDate DESC",
                        SpendEntity.class)
                .setParameter("username", username)
                .setParameter("description", "%" + description + "%")
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    @Nonnull
    public List<SpendEntity> findSpendsByCategory(String categoryName, String username) {
        List<SpendEntity> result = entityManager.createQuery(
                        "SELECT s FROM SpendEntity s WHERE s.category.name = :categoryName AND s.username = :username ORDER BY s.spendDate DESC",
                        SpendEntity.class)
                .setParameter("categoryName", categoryName)
                .setParameter("username", username)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void removeSpend(SpendEntity spend) {
        entityManager.joinTransaction();
        SpendEntity managedSpend = entityManager.find(SpendEntity.class, spend.getId());
        if (managedSpend != null) {
            entityManager.remove(entityManager.contains(managedSpend) ? managedSpend : entityManager.merge(managedSpend));
        }
    }

    @Override
    @Nonnull
    public List<SpendEntity> findAllSpends() {
        List<SpendEntity> result = entityManager.createQuery(
                        "SELECT s FROM SpendEntity s ORDER BY s.spendDate DESC",
                        SpendEntity.class)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    @Nonnull
    public CategoryEntity createCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        entityManager.persist(category);
        return category;
    }

    @Override
    @Nonnull
    public CategoryEntity updateCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        return entityManager.merge(category);
    }

    @Override
    @Nonnull
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        return Optional.ofNullable(entityManager.find(CategoryEntity.class, id));
    }

    @Override
    @Nonnull
    public Optional<CategoryEntity> findCategoryByUsernameAndName(String username, String name) {
        try {
            return Optional.of(entityManager.createQuery(
                            "SELECT c FROM CategoryEntity c WHERE c.username = :username AND c.name = :name",
                            CategoryEntity.class)
                    .setParameter("username", username)
                    .setParameter("name", name)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Nonnull
    public List<CategoryEntity> findCategoriesByUsername(String username) {
        List<CategoryEntity> result = entityManager.createQuery(
                        "SELECT c FROM CategoryEntity c WHERE c.username = :username ORDER BY c.name",
                        CategoryEntity.class)
                .setParameter("username", username)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        CategoryEntity managedCategory = entityManager.find(CategoryEntity.class, category.getId());
        if (managedCategory != null) {
            entityManager.remove(entityManager.contains(managedCategory) ? managedCategory : entityManager.merge(managedCategory));
        }
    }

    @Override
    @Nonnull
    public List<CategoryEntity> findAllCategories() {
        List<CategoryEntity> result = entityManager.createQuery(
                        "SELECT c FROM CategoryEntity c ORDER BY c.username, c.name",
                        CategoryEntity.class)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }
}