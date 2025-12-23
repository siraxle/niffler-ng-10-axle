package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.user.FriendShipId;
import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.repository.FriendshipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.data.jpa.EntityManagers.em;

@ParametersAreNonnullByDefault
public class FriendshipRepositoryHibernate implements FriendshipRepository {

    private static final Config CFG = Config.getInstance();
    private final EntityManager entityManager = em(CFG.userdataJdbcUrl());

    @Override
    @Nonnull
    public FriendshipEntity create(FriendshipEntity friendship) {
        entityManager.joinTransaction();
        UserEntity managedRequester = entityManager.find(UserEntity.class, friendship.getRequester().getId());
        UserEntity managedAddressee = entityManager.find(UserEntity.class, friendship.getAddressee().getId());

        if (managedRequester == null) {
            throw new IllegalArgumentException("Requester not found with id: " + friendship.getRequester().getId());
        }
        if (managedAddressee == null) {
            throw new IllegalArgumentException("Addressee not found with id: " + friendship.getAddressee().getId());
        }

        friendship.setRequester(managedRequester);
        friendship.setAddressee(managedAddressee);

        entityManager.persist(friendship);
        entityManager.flush();
        return friendship;
    }

    @Override
    @Nonnull
    public Optional<FriendshipEntity> findById(FriendShipId id) {
        String jpql = "SELECT f FROM FriendshipEntity f WHERE f.requester.id = :requesterId AND f.addressee.id = :addresseeId";
        TypedQuery<FriendshipEntity> query = entityManager.createQuery(jpql, FriendshipEntity.class);
        query.setParameter("requesterId", id.getRequester());
        query.setParameter("addresseeId", id.getAddressee());

        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Nonnull
    public List<FriendshipEntity> findByRequester(String username) {
        List<FriendshipEntity> result = entityManager.createQuery(
                        "SELECT f FROM FriendshipEntity f WHERE f.requester.username = :username ORDER BY f.createdDate DESC",
                        FriendshipEntity.class)
                .setParameter("username", username)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    @Nonnull
    public List<FriendshipEntity> findByAddressee(String username) {
        List<FriendshipEntity> result = entityManager.createQuery(
                        "SELECT f FROM FriendshipEntity f WHERE f.addressee.username = :username ORDER BY f.createdDate DESC",
                        FriendshipEntity.class)
                .setParameter("username", username)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    @Nonnull
    public FriendshipEntity update(FriendshipEntity friendship) {
        entityManager.joinTransaction();
        return entityManager.merge(friendship);
    }

    @Override
    public void remove(FriendshipEntity friendship) {
        entityManager.joinTransaction();
        FriendShipId id = new FriendShipId();
        id.setRequester(friendship.getRequester().getId());
        id.setAddressee(friendship.getAddressee().getId());

        Optional<FriendshipEntity> managedFriendship = findById(id);
        managedFriendship.ifPresent(entityManager::remove);
    }
}