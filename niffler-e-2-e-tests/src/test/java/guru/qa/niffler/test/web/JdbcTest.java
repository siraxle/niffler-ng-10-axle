package guru.qa.niffler.test.web;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UserDao;
import guru.qa.niffler.data.dao.impl.*;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.model.*;
import guru.qa.niffler.service.SpendDbClient;
import guru.qa.niffler.service.UsersDbClientCTM;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcTest {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();
    private final UserDao userDao = new UdUserDaoSpringJdbc();

    @Test
    void proveChainedTransactionManagerBasicOperation() {
        UsersDbClientCTM usersDbClient = new UsersDbClientCTM();
        String username = "chained-test-" + System.currentTimeMillis();

        try {
            // Пытаемся создать пользователя через распределенную транзакцию
            UserJson createdUser = usersDbClient.createUserSpringJdbc(
                    new UserJson(null, username, CurrencyValues.RUB, null, null, null, null, null)
            );

            // Если дошли сюда без исключения - транзакция прошла
            assertNotNull(createdUser, "User should be created successfully");
            assertNotNull(createdUser.id(), "User should have ID");
            assertEquals(username, createdUser.username(), "Username should match");

            System.out.println("ChainedTransactionManager: User created successfully");

        } catch (Exception e) {
            // Если исключение - анализируем причину
            System.out.println("ChainedTransactionManager failed: " + e.getMessage());
            e.printStackTrace();
            fail("ChainedTransactionManager should work for basic operations");
        }
    }

    @Test
    void proveChainedTransactionManagerRollbackOnDuplicate() {
        UsersDbClientCTM usersDbClient = new UsersDbClientCTM();
        String username = "duplicate-test-" + System.currentTimeMillis();

        // Первое создание - должно работать
        UserJson firstUser = usersDbClient.createUserSpringJdbc(
                new UserJson(null, username, CurrencyValues.RUB, null, null, null, null, null)
        );
        assertNotNull(firstUser, "First user should be created");

        // Второе создание с тем же username - должно вызвать ошибку
        try {
            usersDbClient.createUserSpringJdbc(
                    new UserJson(null, username, CurrencyValues.RUB, null, null, null, null, null)
            );
            fail("Should throw exception on duplicate username");
        } catch (Exception e) {
            // Ожидаем исключение при дубликате
            System.out.println("Expected exception on duplicate: " + e.getMessage());
            assertTrue(e.getMessage().contains("duplicate") || e.getCause().getMessage().contains("duplicate"),
                    "Should be duplicate key violation");
        }
    }

@Test
void springJdbcTest() {
    UsersDbClientCTM usersDbClient = new UsersDbClientCTM();
    UserJson userJson = usersDbClient.createUserSpringJdbc(
            new UserJson(
                    null,
                    "valentin-16",
                    CurrencyValues.RUB,
                    null,
                    null,
                    null,
                    null,
                    null
            )
    );
    System.out.println(userJson);
}



    @Test
    void daoTest() {
        SpendDbClient spendDbClient = new SpendDbClient();
        SpendJson spendJson = spendDbClient.createSpend(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-cat-name-6",
                                "cat",
                                false
                        ),
                        CurrencyValues.RUB,
                        100.0,
                        "test description",
                        "cat"
                )
        );
        System.out.println(spendJson);
    }
//    void createAuthUser() {
//        AuthUserEntity createdUser = transaction(con -> {
//            AuthUserEntity user = new AuthUserEntity();
//            user.setUsername(RandomDataUtils.randomUsername());
//            user.setPassword(RandomDataUtils.randomeSentence(3));
//            user.setEnabled(true);
//            user.setAccountNonExpired(true);
//            user.setAccountNonLocked(true);
//            user.setCredentialsNonExpired(true);
//            return new AuthUserDaoJdbc().create(user);
//        }, CFG.authJdbcUrl());
//
//        assertNotNull(createdUser.getId());
//
//        Optional<AuthUserEntity> foundUser = transaction((Connection con) ->
//                        new AuthUserDaoJdbc().findById(createdUser.getId()),
//                CFG.authJdbcUrl()
//        );
//
//        assertTrue(foundUser.isPresent());
//    }
//
//    @Test
//    void createAuthorities() {
//        String testUsername = RandomDataUtils.randomUsername();
//
//        UUID userId = transaction(con -> {
//            AuthUserEntity user = new AuthUserEntity();
//            user.setUsername(testUsername);
//            user.setPassword(RandomDataUtils.randomeSentence(3));
//            user.setEnabled(true);
//            user.setAccountNonExpired(true);
//            user.setAccountNonLocked(true);
//            user.setCredentialsNonExpired(true);
//            return new AuthUserDaoJdbc().create(user).getId();
//        }, CFG.authJdbcUrl(), Connection.TRANSACTION_READ_COMMITTED);
//
//        transaction((Connection con) -> {
//            AuthorityEntity authAuthorityReadEntity = new AuthorityEntity();
//            authAuthorityReadEntity.setUserId(userId);
//            authAuthorityReadEntity.setAuthority(Authority.READ);
//
//            AuthorityEntity authAuthorityWriteEntity = new AuthorityEntity();
//            authAuthorityWriteEntity.setUserId(userId);
//            authAuthorityWriteEntity.setAuthority(Authority.WRITE);
//
//            new AuthAuthorityDaoJdbc().create(authAuthorityReadEntity);
//            new AuthAuthorityDaoJdbc().create(authAuthorityWriteEntity);
//        }, CFG.authJdbcUrl(), Connection.TRANSACTION_READ_COMMITTED);
//
//        Optional<AuthUserEntity> userWithAuthorities = transaction((Connection con) ->
//                        new AuthUserDaoJdbc().findById(userId),
//                CFG.authJdbcUrl(),
//                Connection.TRANSACTION_READ_COMMITTED
//        );
//
//        assertTrue(userWithAuthorities.isPresent());
//        assertEquals(testUsername, userWithAuthorities.get().getUsername());
//    }
//
//    @Test
//    void xaTransactionRollback() {
//        String testUsername = RandomDataUtils.randomUsername();
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () ->
//                xaTransaction(
//                        Connection.TRANSACTION_READ_COMMITTED,
//                        new XaFunction<>(
//                                con -> {
//                                    AuthUserEntity authUser = new AuthUserEntity();
//                                    authUser.setUsername(testUsername);
//                                    authUser.setPassword(RandomDataUtils.randomeSentence(3));
//                                    authUser.setEnabled(true);
//                                    authUser.setAccountNonExpired(true);
//                                    authUser.setAccountNonLocked(true);
//                                    authUser.setCredentialsNonExpired(true);
//                                    return new AuthUserDaoJdbc().create(authUser);
//                                },
//                                CFG.authJdbcUrl()
//                        ),
//                        new XaFunction<>(
//                                con -> {
//                                    throw new RuntimeException();
//                                },
//                                CFG.userdataJdbcUrl()
//                        )
//                )
//        );
//
//        assertNotNull(exception);
//    }
//
//    @Test
//    void transactionIsolationLevel() {
//        transaction((Connection con) -> {
//            try {
//                assertEquals(Connection.TRANSACTION_REPEATABLE_READ, con.getTransactionIsolation());
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }, CFG.authJdbcUrl(), Connection.TRANSACTION_REPEATABLE_READ);
//    }
//
//    @Test
//    void xaTransactionIsolationLevel() {
//        xaTransaction(
//                Connection.TRANSACTION_SERIALIZABLE,
//                new XaFunction<>(
//                        con -> {
//                            try {
//                                assertEquals(Connection.TRANSACTION_SERIALIZABLE, con.getTransactionIsolation());
//                            } catch (Exception e) {
//                                throw new RuntimeException(e);
//                            }
//                            return null;
//                        },
//                        CFG.authJdbcUrl()
//                ),
//                new XaFunction<>(
//                        con -> {
//                            try {
//                                assertEquals(Connection.TRANSACTION_SERIALIZABLE, con.getTransactionIsolation());
//                            } catch (Exception e) {
//                                throw new RuntimeException(e);
//                            }
//                            return null;
//                        },
//                        CFG.userdataJdbcUrl()
//                )
//        );
//    }

}
