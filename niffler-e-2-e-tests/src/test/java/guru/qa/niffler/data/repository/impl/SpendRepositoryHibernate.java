package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jpa.EntityManagers.em;

public class SpendRepositoryHibernate implements SpendRepository {

    private static final Config CFG = Config.getInstance();
    private final EntityManager entityManager = em(CFG.spendJdbcUrl());

    @Override
    public SpendEntity create(SpendEntity spend) {
        entityManager.joinTransaction();
        if (spend.getCategory() != null && spend.getCategory().getId() != null) {
            CategoryEntity managedCategory = entityManager.find(CategoryEntity.class, spend.getCategory().getId());
            spend.setCategory(managedCategory);
        }

        entityManager.persist(spend);
        return spend;
    }

    @Override
    public Optional<SpendEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(SpendEntity.class, id));
    }

    @Override
    public List<SpendEntity> findByUsername(String username) {
        return entityManager.createQuery(
                        "SELECT s FROM SpendEntity s WHERE s.username = :username ORDER BY s.spendDate DESC",
                        SpendEntity.class)
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    public List<SpendEntity> findByUsernameAndSpendDescription(String username, String description) {
        return entityManager.createQuery(
                        "SELECT s FROM SpendEntity s WHERE s.username = :username AND s.description LIKE :description ORDER BY s.spendDate DESC",
                        SpendEntity.class)
                .setParameter("username", username)
                .setParameter("description", "%" + description + "%")
                .getResultList();
    }

    @Override
    public List<SpendEntity> findByCategory(String categoryName, String username) {
        return entityManager.createQuery(
                        "SELECT s FROM SpendEntity s WHERE s.category.name = :categoryName AND s.username = :username ORDER BY s.spendDate DESC",
                        SpendEntity.class)
                .setParameter("categoryName", categoryName)
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    public SpendEntity update(SpendEntity spend) {
        entityManager.joinTransaction();
        return entityManager.merge(spend);
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        entityManager.persist(category);
        return category;
    }

    @Override
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        return Optional.ofNullable(entityManager.find(CategoryEntity.class, id));
    }

    @Override
    public Optional<CategoryEntity> findCategoryByUsernameAndCategoryName(String username, String name) {
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
    public void remove(SpendEntity spend) {
        entityManager.joinTransaction();
        SpendEntity managedSpend = entityManager.find(SpendEntity.class, spend.getId());
        if (managedSpend != null) {
            entityManager.remove(managedSpend);
        }
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        CategoryEntity managedCategory = entityManager.find(CategoryEntity.class, category.getId());
        if (managedCategory != null) {
            entityManager.remove(managedCategory);
        }
    }

    @Override
    public List<SpendEntity> findAll() {
        return entityManager.createQuery("SELECT s FROM SpendEntity s ORDER BY s.spendDate DESC",
                        SpendEntity.class)
                .getResultList();
    }
}