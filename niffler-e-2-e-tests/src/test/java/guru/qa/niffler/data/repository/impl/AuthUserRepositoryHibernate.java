package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jpa.EntityManagers.em;

@ParametersAreNonnullByDefault
public class AuthUserRepositoryHibernate implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = em(CFG.authJdbcUrl());

    @Override
    public @NonNull AuthUserEntity create(AuthUserEntity user) {
        entityManager.joinTransaction();
        entityManager.persist(user);
        return user;
    }

    @Override
    public @NonNull Optional<AuthUserEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(AuthUserEntity.class, id));
    }

    @Override
    public @NonNull Optional<AuthUserEntity> findByUsername(String username) {
        try {
            return Optional.of(entityManager.createQuery("SELECT u FROM AuthUserEntity u where u.username =: username", AuthUserEntity.class)
                    .setParameter("username", username)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public @NonNull AuthUserEntity update(AuthUserEntity user) {
        entityManager.joinTransaction();
        return entityManager.merge(user);
    }

    @Override
    public void remove(AuthUserEntity user) {
        entityManager.joinTransaction();
        AuthUserEntity managedUser = entityManager.find(AuthUserEntity.class, user.getId());
        if (managedUser != null) {
            entityManager.remove(managedUser);
        }
    }

    @Override
    public @NonNull List<AuthUserEntity> findAll() {
        List<AuthUserEntity> authUserEntities = entityManager.createQuery("SELECT u FROM AuthUserEntity u", AuthUserEntity.class)
                .getResultList();
        return authUserEntities != null ? authUserEntities : Collections.emptyList();
    }
}
