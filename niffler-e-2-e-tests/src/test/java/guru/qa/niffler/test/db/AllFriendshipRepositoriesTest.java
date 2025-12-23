package guru.qa.niffler.test.db;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.user.FriendShipId;
import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.entity.user.FriendshipStatus;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.repository.FriendshipRepository;
import guru.qa.niffler.data.repository.UserDataUserRepository;
import guru.qa.niffler.data.repository.impl.*;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Тестирование методов FriendshipRepository")
public class AllFriendshipRepositoriesTest {

    private static final Config CFG = Config.getInstance();

    static Stream<Arguments> repositories() {
        return Stream.of(
                arguments("JDBC",
                        new FriendshipRepositoryJdbc(),
                        new UserdataRepositoryJdbc()),
                arguments("Spring JDBC",
                        new FriendshipRepositorySpringJdbc(),
                        new UserdataRepositorySpringJdbc()),
                arguments("Hibernate",
                        new FriendshipRepositoryHibernate(),
                        new UserdataUserRepositoryHibernate())
        );
    }

    private XaTransactionTemplate xaTxTemplate;

    @BeforeEach
    void setUp() {
        xaTxTemplate = new XaTransactionTemplate(CFG.userdataJdbcUrl());
    }

    @DisplayName("createFriendship")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createFriendshipTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            String username1 = RandomDataUtils.randomUsername();
            String username2 = RandomDataUtils.randomUsername();

            UserEntity user1 = createTestUser(username1);
            UserEntity user2 = createTestUser(username2);

            UserEntity savedUser1 = userRepository.create(user1);
            UserEntity savedUser2 = userRepository.create(user2);

            // Для Hibernate: небольшая задержка для синхронизации
            if (userRepository instanceof UserdataUserRepositoryHibernate) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // Игнорируем
                }
            }

            FriendshipEntity friendship = createTestFriendship(savedUser1, savedUser2);
            FriendshipEntity created = friendshipRepository.create(friendship);

            assertNonnull(created);
            assertEquals(savedUser1.getId(), created.getRequester().getId());
            assertEquals(savedUser2.getId(), created.getAddressee().getId());
            assertEquals(FriendshipStatus.PENDING, created.getStatus());
            return null;
        });
    }

    @DisplayName("findById")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByIdTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            String username1 = RandomDataUtils.randomUsername();
            String username2 = RandomDataUtils.randomUsername();

            UserEntity user1 = createTestUser(username1);
            UserEntity user2 = createTestUser(username2);

            UserEntity savedUser1 = userRepository.create(user1);
            UserEntity savedUser2 = userRepository.create(user2);

            FriendshipEntity friendship = createTestFriendship(savedUser1, savedUser2);
            friendshipRepository.create(friendship);

            FriendShipId id = new FriendShipId();
            id.setRequester(savedUser1.getId());
            id.setAddressee(savedUser2.getId());

            Optional<FriendshipEntity> found = friendshipRepository.findById(id);

            assertTrue(found.isPresent(), "Friendship should be found for " + repoName);
            assertEquals(FriendshipStatus.PENDING, found.get().getStatus());
            return null;
        });
    }

    @DisplayName("findByRequester")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByRequesterTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            String username1 = RandomDataUtils.randomUsername();
            String username2 = RandomDataUtils.randomUsername();

            UserEntity user1 = createTestUser(username1);
            UserEntity user2 = createTestUser(username2);

            UserEntity savedUser1 = userRepository.create(user1);
            UserEntity savedUser2 = userRepository.create(user2);

            FriendshipEntity friendship = createTestFriendship(savedUser1, savedUser2);
            friendshipRepository.create(friendship);

            List<FriendshipEntity> friendships = friendshipRepository.findByRequester(savedUser1.getUsername());

            assertFalse(friendships.isEmpty(), "Should find friendships for requester in " + repoName);
            assertEquals(savedUser1.getUsername(), friendships.get(0).getRequester().getUsername());
            return null;
        });
    }

    @DisplayName("findByAddressee")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByAddresseeTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            String username1 = RandomDataUtils.randomUsername();
            String username2 = RandomDataUtils.randomUsername();

            UserEntity user1 = createTestUser(username1);
            UserEntity user2 = createTestUser(username2);

            UserEntity savedUser1 = userRepository.create(user1);
            UserEntity savedUser2 = userRepository.create(user2);

            FriendshipEntity friendship = createTestFriendship(savedUser1, savedUser2);
            friendshipRepository.create(friendship);

            List<FriendshipEntity> friendships = friendshipRepository.findByAddressee(savedUser2.getUsername());

            assertFalse(friendships.isEmpty(), "Should find friendships for addressee in " + repoName);
            assertEquals(savedUser2.getUsername(), friendships.get(0).getAddressee().getUsername());
            return null;
        });
    }

    @DisplayName("updateFriendship")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void updateFriendshipTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            String username1 = RandomDataUtils.randomUsername();
            String username2 = RandomDataUtils.randomUsername();

            UserEntity user1 = createTestUser(username1);
            UserEntity user2 = createTestUser(username2);

            UserEntity savedUser1 = userRepository.create(user1);
            UserEntity savedUser2 = userRepository.create(user2);

            FriendshipEntity friendship = createTestFriendship(savedUser1, savedUser2);
            friendshipRepository.create(friendship);

            // Находим дружбу через ID
            FriendShipId id = new FriendShipId();
            id.setRequester(savedUser1.getId());
            id.setAddressee(savedUser2.getId());

            Optional<FriendshipEntity> foundOpt = friendshipRepository.findById(id);
            assertTrue(foundOpt.isPresent(), "Friendship should exist for update in " + repoName);

            FriendshipEntity found = foundOpt.get();
            found.setStatus(FriendshipStatus.ACCEPTED);
            FriendshipEntity updated = friendshipRepository.update(found);

            assertEquals(FriendshipStatus.ACCEPTED, updated.getStatus(),
                    "Status should be ACCEPTED after update in " + repoName);
            return null;
        });
    }

    @DisplayName("deleteFriendship")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void deleteFriendshipTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            String username1 = RandomDataUtils.randomUsername();
            String username2 = RandomDataUtils.randomUsername();

            UserEntity user1 = createTestUser(username1);
            UserEntity user2 = createTestUser(username2);

            UserEntity savedUser1 = userRepository.create(user1);
            UserEntity savedUser2 = userRepository.create(user2);

            FriendshipEntity friendship = createTestFriendship(savedUser1, savedUser2);
            friendshipRepository.create(friendship);

            // Находим дружбу через ID перед удалением
            FriendShipId id = new FriendShipId();
            id.setRequester(savedUser1.getId());
            id.setAddressee(savedUser2.getId());

            Optional<FriendshipEntity> foundOpt = friendshipRepository.findById(id);
            assertTrue(foundOpt.isPresent(), "Friendship should exist before delete in " + repoName);

            friendshipRepository.remove(foundOpt.get());

            Optional<FriendshipEntity> foundAfterDelete = friendshipRepository.findById(id);
            assertFalse(foundAfterDelete.isPresent(), "Friendship should be deleted in " + repoName);
            return null;
        });
    }

    @DisplayName("bidirectionalFriendship")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void bidirectionalFriendshipTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            String username1 = RandomDataUtils.randomUsername();
            String username2 = RandomDataUtils.randomUsername();

            UserEntity user1 = createTestUser(username1);
            UserEntity user2 = createTestUser(username2);

            UserEntity savedUser1 = userRepository.create(user1);
            UserEntity savedUser2 = userRepository.create(user2);

            // Создаем двунаправленную дружбу
            FriendshipEntity friendship1 = createTestFriendship(savedUser1, savedUser2);
            friendship1.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.create(friendship1);

            FriendshipEntity friendship2 = new FriendshipEntity();
            friendship2.setRequester(savedUser2);
            friendship2.setAddressee(savedUser1);
            friendship2.setCreatedDate(new Date());
            friendship2.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.create(friendship2);

            // Проверяем что оба пользователя видят друг друга в друзьях
            List<FriendshipEntity> user1Friendships = friendshipRepository.findByRequester(savedUser1.getUsername());
            List<FriendshipEntity> user2Friendships = friendshipRepository.findByRequester(savedUser2.getUsername());

            assertEquals(1, user1Friendships.size(),
                    "User1 should have 1 friendship in " + repoName);
            assertEquals(1, user2Friendships.size(),
                    "User2 should have 1 friendship in " + repoName);
            return null;
        });
    }

    @DisplayName("friendshipStatusWorkflow")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void friendshipStatusWorkflowTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            String username1 = RandomDataUtils.randomUsername();
            String username2 = RandomDataUtils.randomUsername();

            UserEntity user1 = createTestUser(username1);
            UserEntity user2 = createTestUser(username2);

            UserEntity savedUser1 = userRepository.create(user1);
            UserEntity savedUser2 = userRepository.create(user2);

            FriendshipEntity friendship = createTestFriendship(savedUser1, savedUser2);
            friendshipRepository.create(friendship);

            // Проверяем начальный статус
            FriendShipId id = new FriendShipId();
            id.setRequester(savedUser1.getId());
            id.setAddressee(savedUser2.getId());

            Optional<FriendshipEntity> found = friendshipRepository.findById(id);
            assertTrue(found.isPresent(), "Friendship should be found in " + repoName);
            assertEquals(FriendshipStatus.PENDING, found.get().getStatus());

            // Принимаем дружбу
            FriendshipEntity toUpdate = found.get();
            toUpdate.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.update(toUpdate);

            Optional<FriendshipEntity> updated = friendshipRepository.findById(id);
            assertTrue(updated.isPresent(), "Friendship should still exist after update in " + repoName);
            assertEquals(FriendshipStatus.ACCEPTED, updated.get().getStatus(),
                    "Status should be ACCEPTED after update in " + repoName);
            return null;
        });
    }

    @DisplayName("findMultipleFriendshipsByRequester")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findMultipleFriendshipsByRequesterTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            String username1 = RandomDataUtils.randomUsername();
            String username2 = RandomDataUtils.randomUsername();
            String username3 = RandomDataUtils.randomUsername();

            UserEntity user1 = createTestUser(username1);
            UserEntity user2 = createTestUser(username2);
            UserEntity user3 = createTestUser(username3);

            UserEntity savedUser1 = userRepository.create(user1);
            UserEntity savedUser2 = userRepository.create(user2);
            UserEntity savedUser3 = userRepository.create(user3);

            // Создаем несколько дружб для пользователя
            FriendshipEntity friendship1 = createTestFriendship(savedUser1, savedUser2);
            friendshipRepository.create(friendship1);

            FriendshipEntity friendship2 = new FriendshipEntity();
            friendship2.setRequester(savedUser1);
            friendship2.setAddressee(savedUser3);
            friendship2.setCreatedDate(new Date());
            friendship2.setStatus(FriendshipStatus.PENDING);
            friendshipRepository.create(friendship2);

            // Должны найти обе дружбы где user1 является requester
            List<FriendshipEntity> friendships = friendshipRepository.findByRequester(savedUser1.getUsername());

            assertEquals(2, friendships.size(),
                    "Should find 2 friendships for requester in " + repoName);
            return null;
        });
    }

    @DisplayName("findById with non-existent id")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByIdNonExistentTest(String repoName, FriendshipRepository friendshipRepository, UserDataUserRepository userRepository) {
        xaTxTemplate.execute(() -> {
            FriendShipId nonExistentId = new FriendShipId();
            nonExistentId.setRequester(UUID.randomUUID());
            nonExistentId.setAddressee(UUID.randomUUID());

            Optional<FriendshipEntity> found = friendshipRepository.findById(nonExistentId);
            assertFalse(found.isPresent(), "Should not find non-existent friendship in " + repoName);
            return null;
        });
    }

    private FriendshipEntity createTestFriendship(UserEntity requester, UserEntity addressee) {
        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setCreatedDate(new Date());
        friendship.setStatus(FriendshipStatus.PENDING);
        return friendship;
    }

    private UserEntity createTestUser(String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setCurrency(CurrencyValues.USD);
        user.setFirstname("Test");
        user.setSurname("User");
        user.setFullname("Test User");
        return user;
    }
}