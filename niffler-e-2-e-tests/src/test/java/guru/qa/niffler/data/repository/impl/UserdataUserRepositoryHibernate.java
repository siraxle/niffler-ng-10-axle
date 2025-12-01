package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.entity.user.FriendshipStatus;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.repository.UserDataUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jpa.EntityManagers.em;

public class UserdataUserRepositoryHibernate implements UserDataUserRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = em(CFG.userdataJdbcUrl());

    @Override
    public UserEntity create(UserEntity user) {
        entityManager.joinTransaction();
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(UserEntity.class, id));
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        try {
            return Optional.of(entityManager.createQuery(
                            "SELECT u FROM UserEntity u where u.username =: username",
                            UserEntity.class)
                    .setParameter("username", username)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public UserEntity update(UserEntity user) {
        entityManager.joinTransaction();
        UserEntity merged = entityManager.merge(user);
        entityManager.flush();
        return merged;
    }

    @Override
    public void remove(UserEntity user) {
        entityManager.joinTransaction();
        UserEntity managedUser = entityManager.find(UserEntity.class, user.getId());
        if (managedUser != null) {
            entityManager.remove(managedUser);
            entityManager.flush();
        }
    }

    @Override
    public List<UserEntity> findAll() {
        return entityManager.createQuery("SELECT u FROM UserEntity u ORDER BY u.username", UserEntity.class)
                .getResultList();
    }

    @Override
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        entityManager.joinTransaction();
        requester.addFriends(FriendshipStatus.PENDING, addressee);
    }

    @Override
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        entityManager.joinTransaction();
        requester.addFriends(FriendshipStatus.PENDING, addressee);
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        entityManager.joinTransaction();
        requester.addFriends(FriendshipStatus.ACCEPTED, addressee);
        addressee.addFriends(FriendshipStatus.ACCEPTED, requester);
    }

    @Override
    public void removeFriend(UserEntity user, UserEntity friend) {
        entityManager.joinTransaction();
        UserEntity managedUser = entityManager.find(UserEntity.class, user.getId());
        UserEntity managedFriend = entityManager.find(UserEntity.class, friend.getId());

        if (managedUser != null && managedFriend != null) {
            managedUser.removeFriends(managedFriend);
            managedFriend.removeFriends(managedUser);
            entityManager.merge(managedUser);
            entityManager.merge(managedFriend);
        }
    }

    @Override
    public List<UserEntity> findFriends(UserEntity user) {
        // Упрощенный запрос без CASE
        String jpql = """
            SELECT f.addressee FROM FriendshipEntity f 
            WHERE f.requester.id = :userId AND f.status = :status
            UNION
            SELECT f.requester FROM FriendshipEntity f 
            WHERE f.addressee.id = :userId AND f.status = :status
            """;

        return entityManager.createQuery(jpql, UserEntity.class)
                .setParameter("userId", user.getId())
                .setParameter("status", FriendshipStatus.ACCEPTED)
                .getResultList();
    }

    @Override
    public List<UserEntity> findPendingInvitations(UserEntity user) {
        String jpql = """
                SELECT f.requester FROM FriendshipEntity f 
                WHERE f.addressee.id = :userId AND f.status = :status
                """;

        return entityManager.createQuery(jpql, UserEntity.class)
                .setParameter("userId", user.getId())
                .setParameter("status", FriendshipStatus.PENDING)
                .getResultList();
    }

    @Override
    public void acceptFriend(UserEntity user, UserEntity friend) {
        entityManager.joinTransaction();

        // Находим pending invitation
        String jpql = """
                SELECT f FROM FriendshipEntity f 
                WHERE f.requester.id = :friendId AND f.addressee.id = :userId AND f.status = :status
                """;

        try {
            FriendshipEntity pendingFriendship = entityManager.createQuery(jpql, FriendshipEntity.class)
                    .setParameter("friendId", friend.getId())
                    .setParameter("userId", user.getId())
                    .setParameter("status", FriendshipStatus.PENDING)
                    .getSingleResult();

            // Обновляем статус приглашения на ACCEPTED
            pendingFriendship.setStatus(FriendshipStatus.ACCEPTED);

            // Создаем обратную запись о дружбе
            UserEntity managedUser = entityManager.find(UserEntity.class, user.getId());
            UserEntity managedFriend = entityManager.find(UserEntity.class, friend.getId());

            managedUser.addFriends(FriendshipStatus.ACCEPTED, managedFriend);

            entityManager.merge(pendingFriendship);
            entityManager.merge(managedUser);

        } catch (NoResultException e) {
            throw new RuntimeException("Pending friendship invitation not found from user: " + friend.getUsername() + " to user: " + user.getUsername());
        }
    }
}