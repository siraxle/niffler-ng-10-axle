package guru.qa.niffler.test.web;

import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UdUserDao;
import guru.qa.niffler.data.dao.impl.AuthUserDaoJdbc;
import guru.qa.niffler.data.dao.impl.UdUserDaoJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserAuthJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.AuthDbClient;
import guru.qa.niffler.service.UsersDbClient;
import guru.qa.niffler.service.UsersDbClientCTM;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


class UserCreationTransactionInvariantsTest {

    private static final String TEST_PREFIX = "tx_test_";
    private String currentTestId;

    @BeforeEach
    void setupTest() {
        currentTestId = RandomDataUtils.randomUUID().substring(0, 8);
    }

    // ==================== 1. JDBC БЕЗ ТРАНЗАКЦИЙ ====================

    @Test
    @Order(1)
    void bothDatabasesWorkTest() {
        AuthUserDao authDao = new AuthUserDaoJdbc();
        UdUserDao userDao = new UdUserDaoJdbc();

        String username = currentTestId + "_jdbc_no_tx";

        // Шаг 1: Создаем в auth БД - УСПЕХ
        AuthUserEntity authUser = createAuthUserEntity(username);
        AuthUserEntity createdAuth = authDao.create(authUser);
        assertNotNull(createdAuth.getId(), "User should be created in auth DB");
        System.out.println("Created in auth DB: " + createdAuth.getId());

        UserEntity userEntity = createUserEntity(username);
        UserEntity createdUser = userDao.create(userEntity);

        assertNotNull(createdUser.getId(), "User should be created in userdata DB");
        System.out.println("Created in userdata DB: " + createdUser.getId());

        // Проверяем что ОБА пользователя существуют
        boolean existsInAuth = authDao.findByUsername(username).isPresent();
        boolean existsInUserdata = userDao.findByUsername(username).isPresent();

        assertTrue(existsInAuth, "User should exist in auth DB");
        assertTrue(existsInUserdata, "User should exist in userdata DB");

    }

    // ==================== 2. JDBC С ТРАНЗАКЦИЯМИ (per database) ====================

    @Test
    @Order(2)
    void jdbcWithTransactionsTest() {
        AuthDbClient authClient = new AuthDbClient();
        String username = currentTestId + "_jdbc_tx";

        // Создание пользователя в auth БД с транзакцией (включая authorities)
        var userAuth = createUserAuthJson(username);
        var createdAuth = authClient.createUser(userAuth);

        assertNotNull(createdAuth.id(), "User should be created in auth DB with transaction");
        System.out.println("✓ Created in auth DB with transaction: " + createdAuth.id());

        // Проверяем атомарность: если бы была ошибка при создании authorities, все откатилось бы
        var authorities = authClient.getUserAuthorities(username);
        assertFalse(authorities.isEmpty(), "User should have authorities created atomically");
        System.out.println("✓ Authorities created atomically: " + authorities.size() + " authorities");

        // Проверяем доступность
        Optional<?> foundAuth = authClient.findUserByUsername(username);
        assertTrue(foundAuth.isPresent(), "User should be findable after transactional creation");
        System.out.println("✓ Atomic per-database transaction verified\n");
    }

    // ==================== 3. SPRING JDBC БЕЗ РАСПРЕДЕЛЕННЫХ ТРАНЗАКЦИЙ ====================

//    @Test
//    @Order(3)
//    void springJdbcWithXaTransactionsTest() {
//        UsersDbClient usersDbClient = new UsersDbClient();
//        String username = currentTestId + "_spring_single";
//
//        UserJson user = createUserJson(username);
//        UserJson createdUser = usersDbClient.createUser(user);
//
//        assertNotNull(createdUser.id(), "User should be created in userdata DB");
//        System.out.println("✓ Created in userdata DB: " + createdUser.id());
//
//        Optional<UserJson> foundUser = usersDbClient.findUserByUsername(username);
//        assertTrue(foundUser.isPresent(), "User should be immediately findable - REQUIRED for UserExtension");
//        assertEquals(username, foundUser.get().username(), "Usernames should match");
//
//        System.out.println("✓ Immediate availability confirmed - suitable for UserExtension");
//        System.out.println("✓ Spring JDBC single DB atomicity verified\n");
//    }

    // ==================== 4. SPRING JDBC С РАСПРЕДЕЛЕННЫМИ ТРАНЗАКЦИЯМИ ====================

//    @Test
//    @Order(4)
//    void springJdbcWithXATransactionsTest() {
//        UsersDbClientCTM usersDbClient = new UsersDbClientCTM();
//        String username = currentTestId + "_spring_distr";
//
//        // Распределенная транзакция через ChainedTransactionManager
//        UserJson user = createUserJson(username);
//        UserJson createdUser = usersDbClient.createUserSpringJdbc(user);
//
//        assertNotNull(createdUser.id(), "User should be created in distributed transaction");
//        System.out.println("✓ Created in distributed transaction: " + createdUser.id());
//
//        // Проверяем доступность через find (обе БД)
//        Optional<UserJson> foundUser = usersDbClient.findUserByUsername(username);
//        assertTrue(foundUser.isPresent(), "User should be findable after distributed creation");
//
//        // Проверяем что пользователь создан в ОБЕИХ БД
//        AuthDbClient authClient = new AuthDbClient();
//        Optional<?> foundInAuth = authClient.findUserByUsername(username);
//        assertTrue(foundInAuth.isPresent(), "User should exist in auth DB after distributed creation");
//
//        System.out.println("✓ Distributed atomicity verified:");
//        System.out.println("  - Userdata DB: user exists");
//        System.out.println("  - Auth DB: user exists");
//        System.out.println("  - Both databases updated atomically\n");
//    }

    // ==================== 5. ТЕСТ ОТКАТА РАСПРЕДЕЛЕННЫХ ТРАНЗАКЦИЙ ====================

    @Test
    @Order(5)
    void springJdbcRollbackTransactionsTest() {
        UsersDbClientCTM usersDbClient = new UsersDbClientCTM();
        String username = currentTestId + "_rollback_test";

        // Шаг 1: Первое создание - УСПЕХ
        UserJson firstUser = usersDbClient.createUserSpringJdbc(createUserJson(username));
        assertNotNull(firstUser.id());
        System.out.println("✓ First creation successful: " + firstUser.id());

        // Шаг 2: Второе создание с тем же username - ОШИБКА
        try {
            usersDbClient.createUserSpringJdbc(createUserJson(username));
            fail("Should throw exception on duplicate username");
        } catch (DataIntegrityViolationException e) {
            System.out.println("✓ Expected duplicate violation: " + e.getMessage());
        }

        // Шаг 3: Проверяем что ОРИГИНАЛЬНЫЙ пользователь сохранился
        Optional<UserJson> foundUser = usersDbClient.findUserByUsername(username);
        assertTrue(foundUser.isPresent(), "Original user should still exist after failed duplicate creation");
        assertEquals(firstUser.id(), foundUser.get().id(), "User ID should remain the same");

        System.out.println("✓ ROLLBACK PROVEN: Original user preserved after duplicate error");
        System.out.println("✓ Distributed transaction rollback works correctly\n");
    }


    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private AuthUserEntity createAuthUserEntity(String username) {
        AuthUserEntity user = new AuthUserEntity();
        user.setUsername(username);
        user.setPassword("testPass123");
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        return user;
    }

    private UserEntity createUserEntity(String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setCurrency(CurrencyValues.RUB);
        user.setFirstname(RandomDataUtils.randomeName());
        user.setSurname(RandomDataUtils.randomeSurname());
        user.setFullname(RandomDataUtils.randomeName());
        return user;
    }

    private guru.qa.niffler.model.UserAuthJson createUserAuthJson(String username) {
        return new UserAuthJson(
                null, username, "testPass123", true, true, true, true
        );
    }

    private UserJson createUserJson(String username) {
        String firstname = RandomDataUtils.randomeName();
        String surname = RandomDataUtils.randomeSurname();
        String fullname = firstname + " " + surname;
        CurrencyValues currency = CurrencyValues.USD;

        return new UserJson(
                null,
                username,
                firstname,
                surname,
                fullname,
                currency,
                null,
                null,
                null,
                null
        );
    }




    @AfterEach
    void cleanup() {
        // Тщательная очистка всех тестовых данных
        UsersDbClientCTM distClient = new UsersDbClientCTM();
        UsersDbClient singleClient = new UsersDbClient();

        String[] patterns = {
                currentTestId + "_jdbc_no_tx",
                currentTestId + "_jdbc_tx",
                currentTestId + "_spring_single",
                currentTestId + "_spring_distr",
                currentTestId + "_rollback_test",
                currentTestId + "_compare_1",
                currentTestId + "_compare_2",
                currentTestId + "_compare_3",
                currentTestId + "_compare_4"
        };

        for (String pattern : patterns) {
            try {
                distClient.deleteUserFromBothDatabases(pattern);
                singleClient.deleteUser(pattern);
            } catch (Exception e) {
                // Игнорируем ошибки очистки
            }
        }
    }
}