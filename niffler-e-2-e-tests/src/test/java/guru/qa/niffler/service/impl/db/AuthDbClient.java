package guru.qa.niffler.service.impl.db;

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
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.AuthorityJson;
import guru.qa.niffler.model.UserAuthJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
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

    private final XaTransactionTemplate xaTxTemplate = new XaTransactionTemplate(
            CFG.authJdbcUrl()
    );

    @Nullable
    public UserAuthJson createUser(UserAuthJson user) {
        return xaTxTemplate.execute(() -> {
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

    @Nonnull
    public Optional<UserAuthJson> findUserByUsername(String username) {
        return Objects.requireNonNull(xaTxTemplate.execute(() -> {
            Optional<AuthUserEntity> user = authUserDao.findByUsername(username);
            return user.map(UserAuthJson::fromEntity);
        }));
    }

    @Nonnull
    public Optional<UserAuthJson> findUserById(UUID id) {
        return Objects.requireNonNull(xaTxTemplate.execute(() -> {
            Optional<AuthUserEntity> user = authUserDao.findById(id);
            return user.map(UserAuthJson::fromEntity);
        }));
    }

    @Nonnull
    public List<AuthorityJson> getUserAuthorities(String username) {
        return Objects.requireNonNull(xaTxTemplate.execute(() -> {
            Optional<AuthUserEntity> user = authUserDao.findByUsername(username);

            if (user.isPresent()) {
                List<AuthorityEntity> authorities = authAuthorityDao.findAuthoritiesByUserId(user.get().getId());
                if (authorities != null) {
                    return authorities.stream()
                            .map(AuthorityJson::fromEntity)
                            .collect(Collectors.toList());
                }
            }
            return Collections.emptyList();
        }));
    }

    @Nonnull
    public List<AuthorityJson> getUserAuthoritiesById(UUID userId) {
        return Objects.requireNonNull(xaTxTemplate.execute(() -> {
            List<AuthorityEntity> authorities = authAuthorityDao.findAuthoritiesByUserId(userId);
            if (authorities != null) {
                return authorities.stream()
                        .map(AuthorityJson::fromEntity)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }));
    }

    @Nullable
    public UserAuthJson updateUser(UserAuthJson user) {
        return xaTxTemplate.execute(() -> {
            AuthUserEntity userEntity = toAuthUserEntity(user);
            userEntity.setId(user.id());

            AuthUserEntity updatedUser = authUserDao.update(userEntity);
            return UserAuthJson.fromEntity(updatedUser);
        });
    }

    public void deleteUser(String username) {
        // Удаляем из auth БД
        xaTxTemplate.execute(() -> {
            Optional<AuthUserEntity> user = authUserDao.findByUsername(username);

            if (user.isPresent()) {
                List<AuthorityEntity> authorities = authAuthorityDao.findAuthoritiesByUserId(user.get().getId());
                if (authorities != null) {
                    for (AuthorityEntity authority : authorities) {
                        authAuthorityDao.deleteAuthority(authority);
                    }
                }
                authUserDao.delete(user.get());
            }
            return null;
        });

        // Удаляем из userdata БД
        JdbcTransactionTemplate userdataTxTemplate = new JdbcTransactionTemplate(CFG.userdataJdbcUrl());
        xaTxTemplate.execute(() -> {
            Optional<UserEntity> user = userDao.findByUsername(username);
            user.ifPresent(userDao::delete);
            return null;
        });
    }

    public boolean userExists(String username) {
        return findUserByUsername(username).isPresent();
    }

    @Nonnull
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