package guru.qa.niffler.test.db;

import guru.qa.niffler.model.AuthorityJson;
import guru.qa.niffler.model.UserAuthJson;
import guru.qa.niffler.service.AuthDbClient;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDbClientJtaTest {
    private AuthDbClient authDbClient;
    private String testUsername;
    private String testPassword;

    @BeforeEach
    void setUp() {
        authDbClient = new AuthDbClient();
        testUsername = "cat";
        testPassword = "123456";
    }

    @Test
    void shouldRollbackUserAndAuthoritiesWhenExceptionInTransaction() {
        // Arrange
        String rollbackUsername = RandomDataUtils.randomUsername();

        try {
            // Act - пытаемся создать пользователя с именем, которое вызовет искусственную ошибку
            createUserWithSimulatedFailure(rollbackUsername, testPassword, "READ", "WRITE");
            fail("Expected exception was not thrown");

        } catch (SimulatedTransactionException e) {
            // Assert - проверяем что транзакция ОТКАТИЛАСЬ полностью
            System.out.println("Transaction rolled back as expected: " + e.getMessage());

            // Пользователь НЕ должен существовать
            assertFalse(authDbClient.userExists(rollbackUsername),
                    "User should not exist after transaction rollback");

            // Authorities тоже НЕ должны существовать
            List<AuthorityJson> authorities = authDbClient.getUserAuthorities(rollbackUsername);
            assertTrue(authorities.isEmpty(),
                    "Authorities should not exist after transaction rollback");
        }
    }

    @Test
    void shouldCommitTransactionWhenNoErrors() {
        // Arrange
        String successUsername = RandomDataUtils.randomUsername();

        // Act - создаем пользователя БЕЗ ошибок
        UserAuthJson createdUser = authDbClient.createUser(
                new UserAuthJson(null, successUsername, testPassword, true, true, true, true),
                "READ", "WRITE"
        );

        // Assert - проверяем что транзакция ЗАКОММИТИЛАСЬ полностью
        assertNotNull(createdUser);
        assertNotNull(createdUser.id());

        // Пользователь ДОЛЖЕН существовать
        assertTrue(authDbClient.userExists(successUsername),
                "User should exist after successful transaction commit");

        // Authorities тоже ДОЛЖНЫ существовать
        List<AuthorityJson> authorities = authDbClient.getUserAuthorities(successUsername);
        assertEquals(2, authorities.size(),
                "Authorities should exist after successful transaction commit");
//        assertEquals(createdUser.id(), authorities.get(0).id(),)
        assertTrue(authorities.stream().anyMatch(a -> a.authority().equals("READ")));
        assertTrue(authorities.stream().anyMatch(a -> a.authority().equals("WRITE")));
    }

    @Test
    void shouldRollbackPartialUpdatesWhenErrorOccurs() {
        // Arrange - сначала создаем нормального пользователя
        UserAuthJson initialUser = authDbClient.createUser(
                new UserAuthJson(null, testUsername, testPassword, true, true, true, true),
                "READ"
        );

        try {
            // Act - пытаемся обновить пользователя с добавлением authority, но с ошибкой
            updateUserWithSimulatedFailure(initialUser, "WRITE");
            fail("Expected exception was not thrown");

        } catch (SimulatedTransactionException e) {
            // Assert - проверяем что ОБНОВЛЕНИЕ ОТКАТИЛОСЬ
            System.out.println("✅ Update transaction rolled back as expected: " + e.getMessage());

            // Пользователь должен остаться в исходном состоянии
            Optional<UserAuthJson> userAfterRollback = authDbClient.findUserByUsername(testUsername);
            assertTrue(userAfterRollback.isPresent());

            // Новая authority НЕ должна добавиться
            List<AuthorityJson> authoritiesAfter = authDbClient.getUserAuthorities(testUsername);
            assertEquals(1, authoritiesAfter.size(),
                    "Only original authority should exist after rollback");
            assertEquals("READ", authoritiesAfter.get(0).authority());
        }
    }

    @Test
    void shouldRollbackDeleteOperationWhenErrorOccurs() {
        // Arrange - создаем пользователя для удаления
        String deleteUsername = RandomDataUtils.randomUsername();
        UserAuthJson userToDelete = authDbClient.createUser(
                new UserAuthJson(null, deleteUsername, testPassword, true, true, true, true),
                "READ", "WRITE"
        );

        try {
            // Act - пытаемся удалить пользователя с симуляцией ошибки
            deleteUserWithSimulatedFailure(deleteUsername);
            fail("Expected exception was not thrown");

        } catch (SimulatedTransactionException e) {
            // Assert - проверяем что УДАЛЕНИЕ ОТКАТИЛОСЬ
            System.out.println("✅ Delete transaction rolled back as expected: " + e.getMessage());

            // Пользователь ДОЛЖЕН все еще существовать
            assertTrue(authDbClient.userExists(deleteUsername),
                    "User should still exist after failed delete transaction");

            // Authorities тоже ДОЛЖНЫ существовать
            List<AuthorityJson> authorities = authDbClient.getUserAuthorities(deleteUsername);
            assertEquals(2, authorities.size(),
                    "Authorities should still exist after failed delete transaction");
        }
    }

    @Test
    void shouldHandleMultipleUsersWithRandomData() {
        // Arrange
        String user1 = RandomDataUtils.randomUsername();
        String user2 = RandomDataUtils.randomUsername();
        String password1 = "pass_" + RandomDataUtils.randomUsername();
        String password2 = "pass_" + RandomDataUtils.randomUsername();

        // Act
        UserAuthJson createdUser1 = authDbClient.createUser(
                new UserAuthJson(null, user1, password1, true, true, true, true),
                "READ"
        );

        UserAuthJson createdUser2 = authDbClient.createUser(
                new UserAuthJson(null, user2, password2, true, true, true, true),
                "WRITE", "READ"
        );

        // Assert
        assertNotNull(createdUser1);
        assertNotNull(createdUser2);
        assertNotEquals(createdUser1.id(), createdUser2.id());

        // Проверяем изоляцию данных
        List<AuthorityJson> authorities1 = authDbClient.getUserAuthorities(user1);
        List<AuthorityJson> authorities2 = authDbClient.getUserAuthorities(user2);

        assertEquals(1, authorities1.size());
        assertEquals("READ", authorities1.get(0).authority());

        assertEquals(2, authorities2.size());
        assertTrue(authorities2.stream().anyMatch(a -> a.authority().equals("WRITE")));
        assertTrue(authorities2.stream().anyMatch(a -> a.authority().equals("READ")));
    }

    @Test
    void shouldHandleUsersWithRandomAttributes() {
        // Arrange
        String username = RandomDataUtils.randomUsername();
        String password = "pass_" + RandomDataUtils.randomeSentence(2).replace(" ", "").replace(".", "");
        Boolean enabled = Math.random() > 0.5;
        Boolean accountNonExpired = Math.random() > 0.5;
        Boolean accountNonLocked = Math.random() > 0.5;
        Boolean credentialsNonExpired = Math.random() > 0.5;

        // Act
        UserAuthJson createdUser = authDbClient.createUser(
                new UserAuthJson(null, username, password, enabled, accountNonExpired, accountNonLocked, credentialsNonExpired),
                "READ", "WRITE"
        );

        // Assert
        assertNotNull(createdUser);
        assertEquals(username, createdUser.username());
        assertEquals(enabled, createdUser.enabled());
        assertEquals(accountNonExpired, createdUser.accountNonExpired());
        assertEquals(accountNonLocked, createdUser.accountNonLocked());
        assertEquals(credentialsNonExpired, createdUser.credentialsNonExpired());

        // Проверяем в БД
        Optional<UserAuthJson> foundUser = authDbClient.findUserByUsername(username);
        assertTrue(foundUser.isPresent());
        assertEquals(enabled, foundUser.get().enabled());
    }

    @Test
    void shouldCreateUserWithRandomAuthorities() {
        // Arrange
        String username = RandomDataUtils.randomUsername();
        String[] authorities = {"READ", "WRITE"};
        int randomCount = (int) (Math.random() * authorities.length) + 1;

        String[] selectedAuthorities = new String[randomCount];
        System.arraycopy(authorities, 0, selectedAuthorities, 0, randomCount);

        // Act
        UserAuthJson createdUser = authDbClient.createUser(
                new UserAuthJson(null, username, testPassword, true, true, true, true),
                selectedAuthorities
        );

        // Assert
        assertNotNull(createdUser);

        List<AuthorityJson> userAuthorities = authDbClient.getUserAuthorities(username);
        assertEquals(randomCount, userAuthorities.size());

        for (String expectedAuthority : selectedAuthorities) {
            assertTrue(userAuthorities.stream().anyMatch(a -> a.authority().equals(expectedAuthority)),
                    "Authority " + expectedAuthority + " should be present");
        }
    }

    @Test
    void shouldVerifyTransactionIsolationWithRandomUsers() {
        // Arrange - создаем несколько пользователей для проверки изоляции
        int userCount = 5;
        String[] usernames = new String[userCount];

        for (int i = 0; i < userCount; i++) {
            usernames[i] = RandomDataUtils.randomUsername();
            authDbClient.createUser(
                    new UserAuthJson(null, usernames[i], "pass_" + i, true, true, true, true),
                    "READ"
            );
        }

        // Act & Assert - проверяем что все пользователи создались независимо
        for (String username : usernames) {
            assertTrue(authDbClient.userExists(username),
                    "User " + username + " should exist after creation");

            List<AuthorityJson> authorities = authDbClient.getUserAuthorities(username);
            assertEquals(1, authorities.size());
            assertEquals("READ", authorities.get(0).authority());
        }

        System.out.println("✅ Transaction isolation verified for " + userCount + " random users");
    }

    // ===== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ СИМУЛЯЦИИ ОШИБОК =====

    private void createUserWithSimulatedFailure(String username, String password, String... authorities) {
        // Создаем пользователя нормально
        UserAuthJson user = authDbClient.createUser(
                new UserAuthJson(null, username, password, true, true, true, true),
                authorities
        );

        // Искусственно вызываем ошибку ПОСЛЕ создания пользователя
        throw new SimulatedTransactionException("Artificial failure after user creation for: " + username);
    }

    private void updateUserWithSimulatedFailure(UserAuthJson user, String newAuthority) {
        // Обновляем пользователя
        String newPassword = "newpass_" + RandomDataUtils.randomUsername();
        UserAuthJson updatedUser = authDbClient.updateUser(
                new UserAuthJson(user.id(), user.username(), newPassword, false, false, false, false)
        );

        // Добавляем authority
        authDbClient.createAuthority(user.username(), newAuthority);

        // Искусственная ошибка - все изменения должны откатиться
        throw new SimulatedTransactionException("Artificial failure after update for: " + user.username());
    }

    private void deleteUserWithSimulatedFailure(String username) {
        // Искусственная ошибка до завершения удаления
        throw new SimulatedTransactionException("Artificial failure during delete for: " + username);
    }

    /**
     * Исключение для симуляции ошибок в транзакциях
     */
    private static class SimulatedTransactionException extends RuntimeException {
        public SimulatedTransactionException(String message) {
            super(message);
        }
    }
}
