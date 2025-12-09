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
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.UserJson;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class UsersDbClientCTM {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();
    private final UdUserDao userDao = new UdUserDaoSpringJdbc();

    // TransactionManager для отдельных БД
    private final PlatformTransactionManager authTransactionManager =
            new DataSourceTransactionManager(DataSources.dataSource(CFG.authJdbcUrl()));

    private final PlatformTransactionManager userdataTransactionManager =
            new DataSourceTransactionManager(DataSources.dataSource(CFG.userdataJdbcUrl()));

    // ChainedTransactionManager для распределенных транзакций
    private final PlatformTransactionManager chainedTransactionManager =
            new ChainedTransactionManager(
                    authTransactionManager,
                    userdataTransactionManager
            );

    // TransactionTemplate для распределенных транзакций
    private final TransactionTemplate distributedTxTemplate =
            new TransactionTemplate(chainedTransactionManager);

    // TransactionTemplate для отдельных БД
    private final TransactionTemplate authTxTemplate =
            new TransactionTemplate(authTransactionManager);

    private final TransactionTemplate userdataTxTemplate =
            new TransactionTemplate(userdataTransactionManager);

    // Распределенная транзакция - создание пользователя в обеих БД атомарно
    public UserJson createUserSpringJdbc(UserJson user) {
        return distributedTxTemplate.execute(status -> {
            // Создание в auth БД
            AuthUserEntity authUser = new AuthUserEntity();
            authUser.setUsername(user.username());
            authUser.setPassword(pe.encode("12345")); // Пароль по умолчанию или из user
            authUser.setEnabled(true);
            authUser.setAccountNonExpired(true);
            authUser.setAccountNonLocked(true);
            authUser.setCredentialsNonExpired(true);

            AuthUserEntity createdAuthUser = authUserDao.create(authUser);

            // Создание authorities
            AuthorityEntity[] authorityEntities = Arrays.stream(Authority.values())
                    .map(e -> {
                        AuthorityEntity authAuthority = new AuthorityEntity();
                        authAuthority.setUser(createdAuthUser);
                        authAuthority.setAuthority(e);
                        return authAuthority;
                    })
                    .toArray(AuthorityEntity[]::new);

            authAuthorityDao.create(authorityEntities);

            // Создание в userdata БД
            UserEntity createdUser = userDao.create(toUserEntity(user));

            return UserJson.fromEntity(createdUser, null);
        });
    }

    // Обычные транзакции для работы только с userdata БД
    public UserJson createUser(UserJson user) {
        return userdataTxTemplate.execute(status -> {
            UserEntity createdUser = userDao.create(toUserEntity(user));
            return UserJson.fromEntity(createdUser, null);
        });
    }

    public Optional<UserJson> findUserByUsername(String username) {
        return userdataTxTemplate.execute(status -> {
            Optional<UserEntity> user = userDao.findByUsername(username);
            return user.map(u -> UserJson.fromEntity(u, null));
        });
    }

    public Optional<UserJson> findUserById(UUID id) {
        return userdataTxTemplate.execute(status -> {
            Optional<UserEntity> user = userDao.findById(id);
            return user.map(u -> UserJson.fromEntity(u, null));
        });
    }

    // Распределенная транзакция - удаление из обеих БД
    public void deleteUserFromBothDatabases(String username) {
        distributedTxTemplate.executeWithoutResult(status -> {
            // Удаление из auth БД
            Optional<AuthUserEntity> authUser = authUserDao.findByUsername(username);
            if (authUser.isPresent()) {
                // Удаляем authorities
                authAuthorityDao.findAuthoritiesByUserId(authUser.get().getId())
                        .forEach(authAuthorityDao::deleteAuthority);
                // Удаляем пользователя
                authUserDao.delete(authUser.get());
            }

            // Удаление из userdata БД
            Optional<UserEntity> user = userDao.findByUsername(username);
            user.ifPresent(userDao::delete);
        });
    }

    // Удаление только из userdata БД
    public void deleteUser(String username) {
        userdataTxTemplate.executeWithoutResult(status -> {
            Optional<UserEntity> user = userDao.findByUsername(username);
            user.ifPresent(userDao::delete);
        });
    }

    public boolean userExists(String username) {
        return findUserByUsername(username).isPresent();
    }

    private UserEntity toUserEntity(UserJson userJson) {
        UserEntity entity = new UserEntity();
        entity.setUsername(userJson.username());
        entity.setCurrency(userJson.currency());
        entity.setFirstname(userJson.firstname());
        entity.setSurname(userJson.surname());
        entity.setFullname(userJson.fullname());
        entity.setPhoto(userJson.photo());
        entity.setPhotoSmall(userJson.photoSmall());
        return entity;
    }
}