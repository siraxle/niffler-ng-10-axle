package guru.qa.niffler.test.web;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UserDao;
import guru.qa.niffler.data.dao.impl.*;
import guru.qa.niffler.model.*;
import guru.qa.niffler.service.SpendDbClient;
import guru.qa.niffler.service.UsersDbClient;
import guru.qa.niffler.service.UsersDbClientCTM;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcTest {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();
    private final UserDao userDao = new UdUserDaoSpringJdbc();


    @Test
    void springJdbcTest() {
        UsersDbClient usersDbClient = new UsersDbClient();
        UserJson userJson = usersDbClient.createUser(
                new UserJson(
                        null,
                        "valent-01",
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
                                "test-cat-name-9",
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

    @Test
    void testDirectConnection() {
        try {
            // Пробуем прямое подключение без Spring
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/niffler-spend";
            Connection conn = DriverManager.getConnection(url, "postgres", "secret");
            System.out.println("✓ Direct connection successful");
            conn.close();
        } catch (Exception e) {
            System.out.println("✗ Direct connection failed: " + e.getMessage());
            e.printStackTrace();
        }
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
