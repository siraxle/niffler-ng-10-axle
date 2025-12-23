package guru.qa.niffler.test.db;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.AuthUserRepositorySpringJdbc;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.AfterEach;
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

@DisplayName("Тестирование всех реализаций AuthUserRepository")
public class AllAuthUserRepositoriesTest {

    private static final Config CFG = Config.getInstance();

    static Stream<Arguments> repositories() {
        return Stream.of(
                arguments("AuthUserRepositoryJdbc", new AuthUserRepositoryJdbc()),
                arguments("AuthUserRepositorySpringJdbc", new AuthUserRepositorySpringJdbc()),
                arguments("AuthUserRepositoryHibernate", new AuthUserRepositoryHibernate())
        );
    }

    private XaTransactionTemplate xaTxTemplate;
    private String testUsername;

    @BeforeEach
    void setUp() {
        xaTxTemplate = new XaTransactionTemplate(CFG.authJdbcUrl());
        testUsername = RandomDataUtils.randomUsername();
    }

    @AfterEach
    void tearDown() {
        // Очистка данных для всех репозиториев
        repositories().forEach(arg -> {
            AuthUserRepository repo = (AuthUserRepository) arg.get()[1];
            xaTxTemplate.execute(() -> {
                List<AuthUserEntity> allUsers = repo.findAll();
                for (AuthUserEntity user : allUsers) {
                    if (testUsername.equals(user.getUsername())) {
                        repo.remove(user);
                    }
                }
                return null;
            });
        });
    }

    @DisplayName("Создание пользователя с authorities")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createUserWithAuthoritiesTest(String repoName, AuthUserRepository repository) {
        xaTxTemplate.execute(() -> {
            AuthUserEntity user = createTestUser(testUsername);
            AuthUserEntity created = repository.create(user);

            assertNonnull(created.getId());
            assertEquals(testUsername, created.getUsername());
            return null;
        });
    }

    @DisplayName("Поиск пользователя по ID")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByIdTest(String repoName, AuthUserRepository repository) {
        xaTxTemplate.execute(() -> {
            AuthUserEntity user = createTestUser(testUsername);
            AuthUserEntity created = repository.create(user);

            Optional<AuthUserEntity> found = repository.findById(created.getId());
            assertTrue(found.isPresent());
            assertEquals(created.getId(), found.get().getId());
            assertEquals(testUsername, found.get().getUsername());

            return null;
        });
    }

    @DisplayName("Поиск пользователя по username")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByUsernameTest(String repoName, AuthUserRepository repository) {
        xaTxTemplate.execute(() -> {
            AuthUserEntity user = createTestUser(testUsername);
            repository.create(user);

            Optional<AuthUserEntity> found = repository.findByUsername(testUsername);
            assertTrue(found.isPresent());
            assertEquals(testUsername, found.get().getUsername());

            return null;
        });
    }

    @DisplayName("Обновление пользователя")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void updateUserTest(String repoName, AuthUserRepository repository) {
        xaTxTemplate.execute(() -> {
            AuthUserEntity user = createTestUser(testUsername);
            AuthUserEntity created = repository.create(user);

            created.setPassword("newpassword");
            AuthUserEntity updated = repository.update(created);
            assertEquals("newpassword", updated.getPassword());

            return null;
        });
    }

    @DisplayName("Удаление пользователя")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void deleteUserTest(String repoName, AuthUserRepository repository) {
        xaTxTemplate.execute(() -> {
            AuthUserEntity user = createTestUser(testUsername);
            AuthUserEntity created = repository.create(user);

            repository.remove(created);

            Optional<AuthUserEntity> found = repository.findByUsername(testUsername);
            assertFalse(found.isPresent());

            return null;
        });
    }

    @DisplayName("Получение всех пользователей")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findAllTest(String repoName, AuthUserRepository repository) {
        xaTxTemplate.execute(() -> {
            AuthUserEntity user1 = createTestUser(testUsername);
            String otherUsername = RandomDataUtils.randomUsername();
            AuthUserEntity user2 = createTestUser(otherUsername);

            repository.create(user1);
            repository.create(user2);

            List<AuthUserEntity> allUsers = repository.findAll();
            assertFalse(allUsers.isEmpty());
            assertTrue(allUsers.size() >= 2);

            return null;
        });
    }

    @DisplayName("Полный CRUD сценарий в одной транзакции")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void fullCrudScenarioInSingleTransactionTest(String repoName, AuthUserRepository repository) {
        String testUsername = RandomDataUtils.randomUsername();
        xaTxTemplate.execute(() -> {
            // 1. CREATE
            AuthUserEntity user = createTestUser(testUsername);
            AuthUserEntity created = repository.create(user);
            assertNonnull(created.getId());
            assertEquals(testUsername, created.getUsername());

            // 2. READ (поиск по ID)
            Optional<AuthUserEntity> foundById = repository.findById(created.getId());
            assertTrue(foundById.isPresent());
            assertEquals(created.getId(), foundById.get().getId());

            // 3. READ (поиск по username)
            Optional<AuthUserEntity> foundByUsername = repository.findByUsername(testUsername);
            assertTrue(foundByUsername.isPresent());
            assertEquals(testUsername, foundByUsername.get().getUsername());

            // 4. UPDATE - меняем только enabled, не трогаем пароль
            AuthUserEntity toUpdate = foundByUsername.get();
            toUpdate.setEnabled(false);
            AuthUserEntity updated = repository.update(toUpdate);
            assertFalse(updated.getEnabled());

            // 5. Проверка UPDATE
            Optional<AuthUserEntity> afterUpdate = repository.findByUsername(testUsername);
            assertTrue(afterUpdate.isPresent());
            assertFalse(afterUpdate.get().getEnabled());

            // 6. DELETE
            repository.remove(afterUpdate.get());

            // 7. Проверка DELETE
            Optional<AuthUserEntity> afterDelete = repository.findByUsername(testUsername);
            assertFalse(afterDelete.isPresent());

            return null;
        });
    }

    @DisplayName("Создание нескольких пользователей в одной транзакции")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createMultipleUsersInSingleTransactionTest(String repoName, AuthUserRepository repository) {
        xaTxTemplate.execute(() -> {
            String user1Name = testUsername + "_1";
            String user2Name = testUsername + "_2";

            AuthUserEntity user1 = createTestUser(user1Name);
            AuthUserEntity user2 = createTestUser(user2Name);

            AuthUserEntity created1 = repository.create(user1);
            AuthUserEntity created2 = repository.create(user2);

            assertNonnull(created1.getId());
            assertNonnull(created2.getId());

            List<AuthUserEntity> allUsers = repository.findAll();
            assertTrue(allUsers.size() >= 2);

            // Удаляем созданных пользователей в той же транзакции
            repository.remove(created1);
            repository.remove(created2);

            return null;
        });
    }

    @DisplayName("Проверка целостности authorities")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void authoritiesIntegrityTest(String repoName, AuthUserRepository repository) {
        xaTxTemplate.execute(() -> {
            AuthUserEntity user = createTestUser(testUsername);
            AuthUserEntity created = repository.create(user);

            // Для Hibernate проверяем загрузку authorities
            if (repository instanceof AuthUserRepositoryHibernate) {
                assertNonnull(created.getAuthorities());
                assertFalse(created.getAuthorities().isEmpty());
            }

            return null;
        });
    }

    private AuthUserEntity createTestUser(String username) {
        AuthUserEntity user = new AuthUserEntity();
        user.setUsername(username);
        user.setPassword("password123");
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        AuthorityEntity authority1 = new AuthorityEntity();
        authority1.setAuthority(Authority.read);
        user.addAuthorities(authority1);

        AuthorityEntity authority2 = new AuthorityEntity();
        authority2.setAuthority(Authority.write);
        user.addAuthorities(authority2);

        authority1.setUser(user);
        authority2.setUser(user);

        return user;
    }
}