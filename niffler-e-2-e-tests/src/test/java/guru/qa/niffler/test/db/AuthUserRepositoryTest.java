package guru.qa.niffler.test.db;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthUserRepositoryTest {

    private final AuthUserRepository authUserRepository = new AuthUserRepositoryJdbc();

    @Test
    void createUserWithAuthorities() {
        String testUsername = RandomDataUtils.randomUsername();
        AuthUserEntity user = createTestUser(testUsername);
        AuthUserEntity created = authUserRepository.create(user);

        assertNotNull(created.getId());
        assertEquals(testUsername, created.getUsername());
        assertFalse(created.getAuthorities().isEmpty());
        assertEquals(2, created.getAuthorities().size());
    }

    @Test
    void findById() {
        String testUsername = RandomDataUtils.randomUsername();
        AuthUserEntity user = createTestUser(testUsername);
        AuthUserEntity created = authUserRepository.create(user);

        Optional<AuthUserEntity> found = authUserRepository.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(testUsername, found.get().getUsername());
    }

    @Test
    void findByUsername() {
        String testUsername = RandomDataUtils.randomUsername();
        AuthUserEntity user = createTestUser(testUsername);
        authUserRepository.create(user);

        Optional<AuthUserEntity> found = authUserRepository.findByUsername(testUsername);

        assertTrue(found.isPresent());
        assertEquals(testUsername, found.get().getUsername());
    }

    @Test
    void updateUser() {
        String testUsername = RandomDataUtils.randomUsername();
        ;
        AuthUserEntity user = createTestUser(testUsername);
        AuthUserEntity created = authUserRepository.create(user);

        created.setPassword("newpassword");
        AuthUserEntity updated = authUserRepository.update(created);

        assertEquals("newpassword", updated.getPassword());
    }

    @Test
    void deleteUser() {
        String testUsername = RandomDataUtils.randomUsername();
        ;
        AuthUserEntity user = createTestUser(testUsername);
        AuthUserEntity created = authUserRepository.create(user);

        authUserRepository.delete(created);

        Optional<AuthUserEntity> found = authUserRepository.findByUsername(testUsername);
        assertFalse(found.isPresent());
    }

    @Test
    void findAll() {
        AuthUserEntity user1 = createTestUser(RandomDataUtils.randomUsername());
        AuthUserEntity user2 = createTestUser(RandomDataUtils.randomUsername());

        authUserRepository.create(user1);
        authUserRepository.create(user2);

        List<AuthUserEntity> allUsers = authUserRepository.findAll();

        assertFalse(allUsers.isEmpty());
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
        authority1.setAuthority(Authority.READ);
        user.addAuthorities(authority1);

        AuthorityEntity authority2 = new AuthorityEntity();
        authority2.setAuthority(Authority.WRITE);
        user.addAuthorities(authority2);

        authority1.setUser(user);
        authority2.setUser(user);

        return user;
    }



}