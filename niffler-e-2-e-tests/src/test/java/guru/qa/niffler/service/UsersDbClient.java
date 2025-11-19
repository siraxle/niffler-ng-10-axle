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
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.JdbcTransactionTemplate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.UserJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;


public class UsersDbClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthUserRepository authUserRepository = new AuthUserRepositoryJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();
    private final UserDao userDao = new UdUserDaoSpringJdbc();

    private final TransactionTemplate transactionTemplate = new TransactionTemplate(
            new JdbcTransactionManager(
                    DataSources.dataSource(CFG.userdataJdbcUrl())
            )
    );

    private final JdbcTransactionTemplate jdbcTxTemplate = new JdbcTransactionTemplate(
            CFG.authJdbcUrl()
    );

    private final JdbcTransactionTemplate userdataTxTemplate = new JdbcTransactionTemplate(
            CFG.userdataJdbcUrl()
    );

    private final XaTransactionTemplate xaTxTemplate = new XaTransactionTemplate(
            CFG.authJdbcUrl(),
            CFG.userdataJdbcUrl()
    );

    public UserJson createUser(UserJson user) {
        return xaTxTemplate.execute(() -> {
                    AuthUserEntity authUser = new AuthUserEntity();
                    authUser.setUsername(user.username());
                    authUser.setPassword(pe.encode("12345"));
                    authUser.setEnabled(true);
                    authUser.setAccountNonExpired(true);
                    authUser.setAccountNonLocked(true);
                    authUser.setCredentialsNonExpired(true);
                    authUser.setAuthorities(
                            Arrays.stream(Authority.values()).map(
                                    e -> {
                                        AuthorityEntity authorityEntity = new AuthorityEntity();
                                        authorityEntity.setUser(authUser);
                                        authorityEntity.setAuthority(e);
                                        return authorityEntity;
                                    }
                            ).toList()

                    );
                   authUserRepository.create(authUser);
                    return UserJson.fromEntity(
                            userDao.create(UserEntity.fromJson(user))
                    );
                }
        );
    }

//    public UserJson createUser(UserJson user) {
//        return xaTxTemplate.execute(() -> {
//            UserEntity createdUser = userDao.create(toUserEntity(user));
//            return UserJson.fromEntity(createdUser);
//        });
//    }

    public Optional<UserJson> findUserByUsername(String username) {
        return xaTxTemplate.execute(() -> {
            Optional<UserEntity> user = userDao.findByUsername(username);
            return user.map(UserJson::fromEntity);
        });
    }

    public Optional<UserJson> findUserById(UUID id) {
        return xaTxTemplate.execute(() -> {
            Optional<UserEntity> user = userDao.findById(id);
            return user.map(UserJson::fromEntity);
        });
    }

    public void deleteUser(String username) {
        xaTxTemplate.execute(() -> {
            Optional<UserEntity> user = userDao.findByUsername(username);
            user.ifPresent(userDao::delete);
            return null;
        });
    }

    public boolean userExists(String username) {
        return findUserByUsername(username).isPresent();
    }

}