package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UserDao;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.UdUserDaoSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.UserJson;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.entity.user.UserEntity.fromJson;
import static guru.qa.niffler.data.entity.user.UserEntity.toUserEntity;

public class UsersDbClientCTM {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();
    private final UserDao userDao = new UdUserDaoSpringJdbc();

    // TransactionManager для отдельных БД
    private final PlatformTransactionManager authTransactionManager =
            new DataSourceTransactionManager(DataSources.dataSource(CFG.authJdbcUrl()));

    private final PlatformTransactionManager userdataTransactionManager =
            new DataSourceTransactionManager(DataSources.dataSource(CFG.userdataJdbcUrl()));

    // ChainedTransactionManager для распределенных транзакций
    private final PlatformTransactionManager chainedTransactionManager =
            new org.springframework.data.transaction.ChainedTransactionManager(
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
            authUser.setPassword(pe.encode("12345"));
            authUser.setEnabled(true);
            authUser.setAccountNonExpired(true);
            authUser.setAccountNonLocked(true);
            authUser.setCredentialsNonExpired(true);

            AuthUserEntity createdAuthUser = authUserDao.create(authUser);

            // Создание authorities
            AuthorityEntity[] authorityEntities = Arrays.stream(Authority.values())
                    .map(e -> {
                        AuthorityEntity authAuthority = new AuthorityEntity();
                        authAuthority.setUserId(createdAuthUser.getId());
                        authAuthority.setAuthority(e);
                        return authAuthority;
                    })
                    .toArray(AuthorityEntity[]::new);

            authAuthorityDao.create(authorityEntities);

            // Создание в userdata БД
            UserEntity createdUser = userDao.create(fromJson(user));

            return UserJson.fromEntity(createdUser);
        });
    }

    // Обычные транзакции для работы только с userdata БД
    public UserJson createUser(UserJson user) {
        return userdataTxTemplate.execute(status -> {
            UserEntity createdUser = userDao.create(toUserEntity(user));
            return UserJson.fromEntity(createdUser);
        });
    }

    public Optional<UserJson> findUserByUsername(String username) {
        return userdataTxTemplate.execute(status -> {
            Optional<UserEntity> user = userDao.findByUsername(username);
            return user.map(UserJson::fromEntity);
        });
    }

    public Optional<UserJson> findUserById(UUID id) {
        return userdataTxTemplate.execute(status -> {
            Optional<UserEntity> user = userDao.findById(id);
            return user.map(UserJson::fromEntity);
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
}