package guru.qa.niffler.test.db;

import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.repository.FriendshipRepository;
import guru.qa.niffler.data.repository.UserDataRepository;
import guru.qa.niffler.data.repository.impl.FriendshipRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.UserDataRepositoryJdbc;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDataRepositoryTest {

    private final UserDataRepository userDataRepository = new UserDataRepositoryJdbc();
    private String testUsername;
    private String testUsername2;
    private FriendshipRepository friendshipRepository;

    @BeforeEach
    void setUp() {
        testUsername = RandomDataUtils.randomUsername();
        testUsername2 = RandomDataUtils.randomUsername();
        friendshipRepository = new FriendshipRepositoryJdbc();
    }

    @Test
    void createUser() {
        UserEntity user = createTestUser(testUsername);

        UserEntity created = userDataRepository.create(user);

        assertNotNull(created.getId());
        assertEquals(testUsername, created.getUsername());
        assertEquals(CurrencyValues.USD, created.getCurrency());
        assertEquals(user.getFirstname(), created.getFirstname());
        assertEquals(user.getSurname(), created.getSurname());
        assertEquals(user.getFirstname() + " " + user.getSurname(), created.getFullname());
    }

    @Test
    void findById() {
        UserEntity user = createTestUser(testUsername);
        UserEntity created = userDataRepository.create(user);

        Optional<UserEntity> found = userDataRepository.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(testUsername, found.get().getUsername());
    }

    @Test
    void findByUsername() {
        UserEntity user = createTestUser(testUsername);
        userDataRepository.create(user);

        Optional<UserEntity> found = userDataRepository.findByUsername(testUsername);

        assertTrue(found.isPresent());
        assertEquals(testUsername, found.get().getUsername());
    }

    @Test
    void updateUser() {
        UserEntity user = createTestUser(testUsername);
        UserEntity created = userDataRepository.create(user);

        String updatedFirstName = "Updated_" + RandomDataUtils.randomUUID().substring(0, 8);
        created.setFirstname(updatedFirstName);
        UserEntity updated = userDataRepository.update(created);

        assertEquals(updatedFirstName, updated.getFirstname());
        assertEquals(testUsername, updated.getUsername());
    }

    @Test
    void deleteUser() {
        UserEntity user = createTestUser(testUsername);
        UserEntity created = userDataRepository.create(user);

        userDataRepository.delete(created);

        Optional<UserEntity> found = userDataRepository.findByUsername(testUsername);
        assertFalse(found.isPresent());
    }

    @Test
    void findAll() {
        UserEntity user1 = createTestUser(testUsername);
        UserEntity user2 = createTestUser(testUsername2);

        userDataRepository.create(user1);
        userDataRepository.create(user2);

        List<UserEntity> allUsers = userDataRepository.findAll();

        assertFalse(allUsers.isEmpty());
        assertTrue(allUsers.size() >= 2);
    }

    @Test
    void addAndRemoveFriend() {
        UserEntity user1 = createTestUser(testUsername);
        UserEntity user2 = createTestUser(testUsername2);

        UserEntity createdUser1 = userDataRepository.create(user1);
        UserEntity createdUser2 = userDataRepository.create(user2);

        // Создаем ПРИНЯТУЮ дружбу
        userDataRepository.addFriend(createdUser1, createdUser2);

        // Проверяем друзей (ACCEPTED), а не pending invitations
        List<UserEntity> friends = userDataRepository.findFriends(createdUser1);
        assertFalse(friends.isEmpty());
        assertEquals(createdUser2.getUsername(), friends.get(0).getUsername());

        // Удаляем дружбу
        userDataRepository.removeFriend(createdUser1, createdUser2);

        // Проверяем что друзья удалены
        List<UserEntity> friendsAfterRemoval = userDataRepository.findFriends(createdUser1);
        assertTrue(friendsAfterRemoval.isEmpty());
    }

    @Test
    void addAcceptAndRemoveFriend() {
        UserEntity user1 = createTestUser(testUsername);
        UserEntity user2 = createTestUser(testUsername2);

        UserEntity createdUser1 = userDataRepository.create(user1);
        UserEntity createdUser2 = userDataRepository.create(user2);

        userDataRepository.addFriend(createdUser1, createdUser2);
        userDataRepository.acceptFriend(createdUser2, createdUser1);

        List<UserEntity> friendsUser1 = userDataRepository.findFriends(createdUser1);
        List<UserEntity> friendsUser2 = userDataRepository.findFriends(createdUser2);

        assertFalse(friendsUser1.isEmpty());
        assertFalse(friendsUser2.isEmpty());
        assertEquals(createdUser2.getUsername(), friendsUser1.get(0).getUsername());
        assertEquals(createdUser1.getUsername(), friendsUser2.get(0).getUsername());

        userDataRepository.removeFriend(createdUser1, createdUser2);

        List<UserEntity> friendsAfterRemoval = userDataRepository.findFriends(createdUser1);
        assertTrue(friendsAfterRemoval.isEmpty());
    }

    @Test
    void findPendingInvitations() {
        UserEntity user1 = createTestUser(testUsername);
        UserEntity user2 = createTestUser(testUsername2);

        UserEntity createdUser1 = userDataRepository.create(user1);
        UserEntity createdUser2 = userDataRepository.create(user2);

        userDataRepository.addIncomeInvitation(createdUser1, createdUser2);

        List<UserEntity> pending = userDataRepository.findPendingInvitations(createdUser2);
        assertFalse(pending.isEmpty());
        assertEquals(createdUser1.getUsername(), pending.get(0).getUsername());
    }

    @Test
    void fullFriendshipCycle() {
        UserEntity user1 = createTestUser(testUsername);
        UserEntity user2 = createTestUser(testUsername2);

        UserEntity createdUser1 = userDataRepository.create(user1);
        UserEntity createdUser2 = userDataRepository.create(user2);

        // 1. Создаем приглашение (PENDING)
        userDataRepository.addIncomeInvitation(createdUser1, createdUser2);

        // 2. Проверяем pending invitations
        List<UserEntity> pending = userDataRepository.findPendingInvitations(createdUser2);
        assertFalse(pending.isEmpty());
        assertEquals(createdUser1.getUsername(), pending.get(0).getUsername());

        // 3. Принимаем приглашение
        userDataRepository.acceptFriend(createdUser2, createdUser1);

        // 4. Проверяем что стали друзьями
        List<UserEntity> friends = userDataRepository.findFriends(createdUser1);
        assertFalse(friends.isEmpty());
        assertEquals(createdUser2.getUsername(), friends.get(0).getUsername());
    }

    @Test
    void findFriends() {
        UserEntity user1 = createTestUser(testUsername);
        UserEntity user2 = createTestUser(testUsername2);

        UserEntity createdUser1 = userDataRepository.create(user1);
        UserEntity createdUser2 = userDataRepository.create(user2);

        userDataRepository.addFriend(createdUser1, createdUser2);
        userDataRepository.acceptFriend(createdUser2, createdUser1);

        List<UserEntity> friends = userDataRepository.findFriends(createdUser1);
        assertFalse(friends.isEmpty());
        assertEquals(createdUser2.getUsername(), friends.get(0).getUsername());
    }

    private UserEntity createTestUser(String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setCurrency(CurrencyValues.USD);
        user.setFirstname(RandomDataUtils.randomeName());
        user.setSurname(RandomDataUtils.randomeSurname());
        user.setFullname(user.getFirstname() + " " + user.getSurname());
        return user;
    }
}