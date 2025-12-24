package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.AuthUserRepositorySpringJdbc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository {

    static AuthUserRepository getInstance() {
        return switch (System.getProperty("repository", "jpa")) {
            case "jpa" -> new AuthUserRepositoryHibernate();
            case "jdbc" -> new AuthUserRepositoryJdbc();
            case "sjdbc" -> new  AuthUserRepositorySpringJdbc();
            default -> throw  new IllegalStateException("Unrecognized repository: " + System.getProperty("repository"));
        };
    }

    AuthUserEntity create(AuthUserEntity user);

    AuthUserEntity update(AuthUserEntity user);

    Optional<AuthUserEntity> findById(UUID id);

    Optional<AuthUserEntity> findByUsername(String username);

    void remove(AuthUserEntity user);

    List<AuthUserEntity> findAll();
}
