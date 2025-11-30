package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UdUserDao;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.UdUserDaoSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.JdbcTransactionTemplate;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.AuthorityJson;
import guru.qa.niffler.model.UserAuthJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthDbClient {
    private static final Config CFG = Config.getInstance();

    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();
    private final UdUserDao userDao = new UdUserDaoSpringJdbc();

    private final TransactionTemplate transactionTemplate = new TransactionTemplate(
            new JdbcTransactionManager(
                    DataSources.dataSource(CFG.authJdbcUrl())
            )
    );

    private final JdbcTransactionTemplate jdbcTxTemplate = new JdbcTransactionTemplate(
            CFG.authJdbcUrl()
    );

    public UserAuthJson createUser(UserAuthJson user) {
        return jdbcTxTemplate.execute(() -> {
            AuthUserEntity createdUser = authUserDao.create(toAuthUserEntity(user));

            // Создаем authorities и устанавливаем связь с пользователем
            AuthorityEntity readAuthority = new AuthorityEntity();
            readAuthority.setUser(createdUser);
            readAuthority.setAuthority(Authority.read);

            AuthorityEntity writeAuthority = new AuthorityEntity();
            writeAuthority.setUser(createdUser);
            writeAuthority.setAuthority(Authority.write);

            authAuthorityDao.create(readAuthority, writeAuthority);

            return UserAuthJson.fromEntity(createdUser);
        });
    }

    public Optional<UserAuthJson> findUserByUsername(String username) {
        return jdbcTxTemplate.execute(() -> {
            Optional<AuthUserEntity> user = authUserDao.findByUsername(username);
            return user.map(UserAuthJson::fromEntity);
        });
    }

    public Optional<UserAuthJson> findUserById(UUID id) {
        return jdbcTxTemplate.execute(() -> {
            Optional<AuthUserEntity> user = authUserDao.findById(id);
            return user.map(UserAuthJson::fromEntity);
        });
    }

    public List<AuthorityJson> getUserAuthorities(String username) {
        return jdbcTxTemplate.execute(() -> {
            Optional<AuthUserEntity> user = authUserDao.findByUsername(username);

            if (user.isPresent()) {
                List<AuthorityEntity> authorities = authAuthorityDao.findAuthoritiesByUserId(user.get().getId());
                return authorities.stream()
                        .map(AuthorityJson::fromEntity)
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        });
    }

    public List<AuthorityJson> getUserAuthoritiesById(UUID userId) {
        return jdbcTxTemplate.execute(() -> {
            List<AuthorityEntity> authorities = authAuthorityDao.findAuthoritiesByUserId(userId);
            return authorities.stream()
                    .map(AuthorityJson::fromEntity)
                    .collect(Collectors.toList());
        });
    }

    public UserAuthJson updateUser(UserAuthJson user) {
        return jdbcTxTemplate.execute(() -> {
            AuthUserEntity userEntity = toAuthUserEntity(user);
            userEntity.setId(user.id());

            AuthUserEntity updatedUser = authUserDao.update(userEntity);
            return UserAuthJson.fromEntity(updatedUser);
        });
    }

    public void deleteUser(String username) {
        // Удаляем из auth БД
        jdbcTxTemplate.execute(() -> {
            Optional<AuthUserEntity> user = authUserDao.findByUsername(username);

            if (user.isPresent()) {
                List<AuthorityEntity> authorities = authAuthorityDao.findAuthoritiesByUserId(user.get().getId());
                for (AuthorityEntity authority : authorities) {
                    authAuthorityDao.deleteAuthority(authority);
                }
                authUserDao.delete(user.get());
            }
            return null;
        });

        // Удаляем из userdata БД
        JdbcTransactionTemplate userdataTxTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());
        userdataTxTemplate.execute(() -> {
            Optional<UserEntity> user = userDao.findByUsername(username);
            user.ifPresent(userDao::delete);
            return null;
        });
    }

    public boolean userExists(String username) {
        return findUserByUsername(username).isPresent();
    }

    private AuthUserEntity toAuthUserEntity(UserAuthJson userJson) {
        AuthUserEntity entity = new AuthUserEntity();
        entity.setUsername(userJson.username());
        entity.setPassword(userJson.password());
        entity.setEnabled(userJson.enabled());
        entity.setAccountNonExpired(userJson.accountNonExpired());
        entity.setAccountNonLocked(userJson.accountNonLocked());
        entity.setCredentialsNonExpired(userJson.credentialsNonExpired());
        return entity;
    }

}