package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jpa.EntityManagers.em;

public class CategoryRepositoryHibernate implements CategoryRepository {

    private static final Config CFG = Config.getInstance();
    private final EntityManager entityManager = em(CFG.spendJdbcUrl());

    @Override
    public CategoryEntity create(CategoryEntity category) {
        entityManager.joinTransaction();
        entityManager.persist(category);
        return category;
    }

    @Override
    public Optional<CategoryEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(CategoryEntity.class, id));
    }

    @Override
    public Optional<CategoryEntity> findByUsernameAndName(String username, String name) {
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
    public List<CategoryEntity> findByUsername(String username) {
        return entityManager.createQuery(
                        "SELECT c FROM CategoryEntity c WHERE c.username = :username ORDER BY c.name",
                        CategoryEntity.class)
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    public CategoryEntity update(CategoryEntity category) {
        entityManager.joinTransaction();
        return entityManager.merge(category);
    }

    @Override
    public void remove(CategoryEntity category) {
        entityManager.joinTransaction();
        CategoryEntity managedCategory = entityManager.find(CategoryEntity.class, category.getId());
        if (managedCategory != null) {
            entityManager.remove(managedCategory);
        }
    }

    @Override
    public List<CategoryEntity> findAll() {
        return entityManager.createQuery("SELECT c FROM CategoryEntity c ORDER BY c.username, c.name",
                        CategoryEntity.class)
                .getResultList();
    }
}