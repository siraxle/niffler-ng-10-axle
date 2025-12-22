package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.entity.user.FriendshipStatus;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.repository.UserDataUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jpa.EntityManagers.em;

@ParametersAreNonnullByDefault
public class UserdataUserRepositoryHibernate implements UserDataUserRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = em(CFG.userdataJdbcUrl());

    @Override
    @Nullable
    public UserEntity create(UserEntity user) {
        entityManager.joinTransaction();
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    @Override
    @NonNull
    public Optional<UserEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(UserEntity.class, id));
    }

    @Override
    @NonNull
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
    @Nullable
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
    @NonNull
    public List<UserEntity> findAll() {
        List<UserEntity> result = entityManager.createQuery(
                        "SELECT u FROM UserEntity u ORDER BY u.username",
                        UserEntity.class)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void addInvitation(UserEntity requester, UserEntity addressee) {
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
    @NonNull
    public List<UserEntity> findFriends(UserEntity user) {
        String jpql = """
            SELECT f.addressee FROM FriendshipEntity f 
            WHERE f.requester.id = :userId AND f.status = :status
            UNION
            SELECT f.requester FROM FriendshipEntity f 
            WHERE f.addressee.id = :userId AND f.status = :status
            """;

        List<UserEntity> result = entityManager.createQuery(jpql, UserEntity.class)
                .setParameter("userId", user.getId())
                .setParameter("status", FriendshipStatus.ACCEPTED)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    @NonNull
    public List<UserEntity> findPendingInvitations(UserEntity user) {
        String jpql = """
                SELECT f.requester FROM FriendshipEntity f 
                WHERE f.addressee.id = :userId AND f.status = :status
                """;

        List<UserEntity> result = entityManager.createQuery(jpql, UserEntity.class)
                .setParameter("userId", user.getId())
                .setParameter("status", FriendshipStatus.PENDING)
                .getResultList();
        return result != null ? result : Collections.emptyList();
    }

    @Override
    public void acceptFriend(UserEntity acceptingUser, UserEntity invitingUser) {
        entityManager.joinTransaction();

        String jpql = "SELECT f FROM FriendshipEntity f WHERE f.requester = :requester AND f.addressee = :addressee";
        FriendshipEntity invitation = entityManager.createQuery(jpql, FriendshipEntity.class)
                .setParameter("requester", invitingUser)
                .setParameter("addressee", acceptingUser)
                .getSingleResult();

        invitation.setStatus(FriendshipStatus.ACCEPTED);
        entityManager.merge(invitation);

        jpql = "SELECT COUNT(f) FROM FriendshipEntity f WHERE f.requester = :requester AND f.addressee = :addressee";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("requester", acceptingUser)
                .setParameter("addressee", invitingUser)
                .getSingleResult();

        if (count == 0) {
            FriendshipEntity reverseFriendship = new FriendshipEntity();
            reverseFriendship.setRequester(acceptingUser);
            reverseFriendship.setAddressee(invitingUser);
            reverseFriendship.setCreatedDate(new Date());
            reverseFriendship.setStatus(FriendshipStatus.ACCEPTED);
            entityManager.persist(reverseFriendship);
        }

        entityManager.flush();
    }
}