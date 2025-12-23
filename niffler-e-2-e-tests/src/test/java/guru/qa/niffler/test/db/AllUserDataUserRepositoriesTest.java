package guru.qa.niffler.test.db;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.repository.UserDataUserRepository;
import guru.qa.niffler.data.repository.impl.UserdataRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.UserdataRepositorySpringJdbc;
import guru.qa.niffler.data.repository.impl.UserdataUserRepositoryHibernate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Тестирование методов UserDataUserRepository")
public class AllUserDataUserRepositoriesTest {

    private static final Config CFG = Config.getInstance();

    static Stream<Arguments> repositories() {
        return Stream.of(
                arguments("UserdataRepositoryJdbc", new UserdataRepositoryJdbc()),
                arguments("UserdataRepositorySpringJdbc", new UserdataRepositorySpringJdbc()),
                arguments("UserdataUserRepositoryHibernate", new UserdataUserRepositoryHibernate())
        );
    }

    private XaTransactionTemplate xaTxTemplate;
    private String testUsername;

    @BeforeEach
    void setUp() {
        xaTxTemplate = new XaTransactionTemplate(CFG.userdataJdbcUrl());
        testUsername = RandomDataUtils.randomUsername();
    }

    @DisplayName("create")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user = createTestUser(testUsername);
            UserEntity created = repository.create(user);

            assertNotNull(created.getId());
            assertEquals(testUsername, created.getUsername());
            return null;
        });
    }

    @DisplayName("findById")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByIdTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user = createTestUser(testUsername);
            UserEntity created = repository.create(user);

            Optional<UserEntity> found = repository.findById(created.getId());
            assertTrue(found.isPresent());
            assertEquals(created.getId(), found.get().getId());
            return null;
        });
    }

    @DisplayName("findByUsername")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByUsernameTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user = createTestUser(testUsername);
            repository.create(user);

            Optional<UserEntity> found = repository.findByUsername(testUsername);
            assertTrue(found.isPresent());
            assertEquals(testUsername, found.get().getUsername());
            return null;
        });
    }

    @DisplayName("update")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void updateTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user = createTestUser(testUsername);
            UserEntity created = repository.create(user);

            created.setFirstname("Updated");
            UserEntity updated = repository.update(created);

            assertEquals("Updated", updated.getFirstname());
            return null;
        });
    }

    @DisplayName("remove")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void removeTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user = createTestUser(testUsername);
            UserEntity created = repository.create(user);

            repository.remove(created);

            Optional<UserEntity> found = repository.findByUsername(testUsername);
            assertFalse(found.isPresent());
            return null;
        });
    }

    @DisplayName("findAll")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findAllTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user = createTestUser(testUsername);
            repository.create(user);

            List<UserEntity> allUsers = repository.findAll();
            assertFalse(allUsers.isEmpty());
            return null;
        });
    }

    @DisplayName("addIncomeInvitation")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void addIncomeInvitationTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user1 = createTestUser(testUsername);
            String user2Name = testUsername + "_inviter";
            UserEntity user2 = createTestUser(user2Name);

            repository.create(user1);
            repository.create(user2);

            repository.addInvitation(user2, user1);

            List<UserEntity> invitations = repository.findPendingInvitations(user1);
            boolean found = invitations.stream()
                    .anyMatch(u -> user2Name.equals(u.getUsername()));
            assertTrue(found);
            return null;
        });
    }

    @DisplayName("addFriend")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void addFriendTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user1 = createTestUser(testUsername);
            String friendName = testUsername + "_friend";
            UserEntity user2 = createTestUser(friendName);

            repository.create(user1);
            repository.create(user2);

            repository.addFriend(user1, user2);

            List<UserEntity> friends = repository.findFriends(user1);
            boolean found = friends.stream()
                    .anyMatch(u -> friendName.equals(u.getUsername()));
            assertTrue(found);
            return null;
        });
    }

    @DisplayName("addOutcomeInvitation")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void addOutcomeInvitationTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user1 = createTestUser(testUsername);
            String user2Name = testUsername + "_target";
            UserEntity user2 = createTestUser(user2Name);

            repository.create(user1);
            repository.create(user2);

            repository.addInvitation(user1, user2);

            List<UserEntity> invitations = repository.findPendingInvitations(user2);
            boolean found = invitations.stream()
                    .anyMatch(u -> testUsername.equals(u.getUsername()));
            assertTrue(found);
            return null;
        });
    }

    @DisplayName("removeFriend")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void removeFriendTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user1 = createTestUser(testUsername);
            String friendName = testUsername + "_friend";
            UserEntity user2 = createTestUser(friendName);

            repository.create(user1);
            repository.create(user2);

            repository.addFriend(user1, user2);

            // Проверяем что друг добавлен
            List<UserEntity> friendsBefore = repository.findFriends(user1);
            boolean friendAdded = friendsBefore.stream()
                    .anyMatch(u -> friendName.equals(u.getUsername()));
            assertTrue(friendAdded);

            repository.removeFriend(user1, user2);

            List<UserEntity> friendsAfter = repository.findFriends(user1);
            boolean friendRemoved = friendsAfter.stream()
                    .anyMatch(u -> friendName.equals(u.getUsername()));
            assertFalse(friendRemoved);
            return null;
        });
    }

    @DisplayName("findFriends")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findFriendsTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user1 = createTestUser(testUsername);
            String friendName = testUsername + "_friend";
            UserEntity user2 = createTestUser(friendName);

            repository.create(user1);
            repository.create(user2);

            repository.addFriend(user1, user2);

            List<UserEntity> friends = repository.findFriends(user1);
            assertFalse(friends.isEmpty());
            return null;
        });
    }

    @DisplayName("findPendingInvitations")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findPendingInvitationsTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user1 = createTestUser(testUsername);
            String inviterName = testUsername + "_inviter";
            UserEntity user2 = createTestUser(inviterName);

            repository.create(user1);
            repository.create(user2);

            repository.addInvitation(user2, user1);

            List<UserEntity> invitations = repository.findPendingInvitations(user1);

            assertFalse(invitations.isEmpty(), "Invitations should not be empty for " + repoName);
            boolean found = invitations.stream()
                    .anyMatch(u -> inviterName.equals(u.getUsername()));
            assertTrue(found, "Should find invitation from " + inviterName + " for " + repoName);

            return null;
        });
    }

    @DisplayName("acceptFriend")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void acceptFriendTest(String repoName, UserDataUserRepository repository) {
        xaTxTemplate.execute(() -> {
            UserEntity user1 = createTestUser(testUsername);
            String friendName = testUsername + "_friend";
            UserEntity user2 = createTestUser(friendName);

            repository.create(user1);
            repository.create(user2);

            repository.addInvitation(user2, user1);
            repository.acceptFriend(user1, user2);

            List<UserEntity> friends = repository.findFriends(user1);
            boolean isFriend = friends.stream()
                    .anyMatch(u -> friendName.equals(u.getUsername()));
            assertTrue(isFriend, "Should be friends after accepting invitation for " + repoName);

            return null;
        });
    }

    private UserEntity createTestUser(String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setCurrency(CurrencyValues.RUB);
        user.setFirstname("Test");
        user.setSurname("User");
        user.setFullname("Test User");
        return user;
    }
}