package guru.qa.niffler.test.web;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.Databases.XaFunction;
import guru.qa.niffler.data.dao.impl.UserAuthorityDaoJdbc;
import guru.qa.niffler.data.entity.auth.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.SpendDbClient;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.Databases.transaction;
import static guru.qa.niffler.data.Databases.xaTransaction;
import static org.junit.jupiter.api.Assertions.*;

public class JdbcTest {

    private static final Config CFG = Config.getInstance();


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

    @Test
    void createAuthUser() {
        AuthUserEntity createdUser = transaction(con -> {
            AuthUserEntity user = new AuthUserEntity();
            user.setUsername(RandomDataUtils.randomUsername());
            user.setPassword(RandomDataUtils.randomeSentence(3));
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            return new UserAuthorityDaoJdbc(con).createUser(user);
        }, CFG.authJdbcUrl());

        assertNotNull(createdUser.getId());

        Optional<AuthUserEntity> foundUser = transaction((Connection con) ->
                        new UserAuthorityDaoJdbc(con).findUserById(createdUser.getId()),
                CFG.authJdbcUrl()
        );

        assertTrue(foundUser.isPresent());
    }

    @Test
    void createAuthorities() {
        String testUsername = RandomDataUtils.randomUsername();

        UUID userId = transaction(con -> {
            AuthUserEntity user = new AuthUserEntity();
            user.setUsername(testUsername);
            user.setPassword(RandomDataUtils.randomeSentence(3));
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            return new UserAuthorityDaoJdbc(con).createUser(user).getId();
        }, CFG.authJdbcUrl(), Connection.TRANSACTION_READ_COMMITTED);

        transaction((Connection con) -> {
            AuthAuthorityEntity authAuthorityReadEntity = new AuthAuthorityEntity();
            authAuthorityReadEntity.setUserId(userId);
            authAuthorityReadEntity.setAuthority(Authority.READ);

            AuthAuthorityEntity authAuthorityWriteEntity = new AuthAuthorityEntity();
            authAuthorityWriteEntity.setUserId(userId);
            authAuthorityWriteEntity.setAuthority(Authority.WRITE);

            new UserAuthorityDaoJdbc(con).createAuthority(authAuthorityReadEntity);
            new UserAuthorityDaoJdbc(con).createAuthority(authAuthorityWriteEntity);
        }, CFG.authJdbcUrl(), Connection.TRANSACTION_READ_COMMITTED);

        Optional<AuthUserEntity> userWithAuthorities = transaction((Connection con) ->
                        new UserAuthorityDaoJdbc(con).findUserById(userId),
                CFG.authJdbcUrl(),
                Connection.TRANSACTION_READ_COMMITTED
        );

        assertTrue(userWithAuthorities.isPresent());
        assertEquals(testUsername, userWithAuthorities.get().getUsername());
    }

    @Test
    void xaTransactionRollback() {
        String testUsername = RandomDataUtils.randomUsername();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                xaTransaction(
                        Connection.TRANSACTION_READ_COMMITTED,
                        new XaFunction<>(
                                con -> {
                                    AuthUserEntity authUser = new AuthUserEntity();
                                    authUser.setUsername(testUsername);
                                    authUser.setPassword(RandomDataUtils.randomeSentence(3));
                                    authUser.setEnabled(true);
                                    authUser.setAccountNonExpired(true);
                                    authUser.setAccountNonLocked(true);
                                    authUser.setCredentialsNonExpired(true);
                                    return new UserAuthorityDaoJdbc(con).createUser(authUser);
                                },
                                CFG.authJdbcUrl()
                        ),
                        new XaFunction<>(
                                con -> {
                                    throw new RuntimeException();
                                },
                                CFG.userdataJdbcUrl()
                        )
                )
        );

        assertNotNull(exception);
    }

    @Test
    void transactionIsolationLevel() {
        transaction((Connection con) -> {
            try {
                assertEquals(Connection.TRANSACTION_REPEATABLE_READ, con.getTransactionIsolation());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CFG.authJdbcUrl(), Connection.TRANSACTION_REPEATABLE_READ);
    }

    @Test
    void xaTransactionIsolationLevel() {
        xaTransaction(
                Connection.TRANSACTION_SERIALIZABLE,
                new XaFunction<>(
                        con -> {
                            try {
                                assertEquals(Connection.TRANSACTION_SERIALIZABLE, con.getTransactionIsolation());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            return null;
                        },
                        CFG.authJdbcUrl()
                ),
                new XaFunction<>(
                        con -> {
                            try {
                                assertEquals(Connection.TRANSACTION_SERIALIZABLE, con.getTransactionIsolation());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            return null;
                        },
                        CFG.userdataJdbcUrl()
                )
        );
    }

}
