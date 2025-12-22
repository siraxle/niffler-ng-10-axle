package guru.qa.niffler.service.impl.db;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.JdbcTransactionTemplate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.AuthorityJson;
import lombok.NonNull;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

import static guru.qa.niffler.model.AuthorityJson.toAuthorityJsonArray;

@ParametersAreNonnullByDefault
public class AuthorityDbClient {
    private static final Config CFG = Config.getInstance();

    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();

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
    public AuthorityJson createAuthority(String username, String authority) {
        return xaTxTemplate.execute(() -> {
            UUID userId = getUserIdByUsername(username);

            AuthorityEntity authAuthority = new AuthorityEntity();
            AuthUserEntity user = new AuthUserEntity();
            user.setId(userId);
            authAuthority.setUser(user);
            authAuthority.setAuthority(Authority.valueOf(authority));

            AuthorityEntity[] createdAuthorities = authAuthorityDao.create(authAuthority);
            return createdAuthorities != null && createdAuthorities.length > 0
                    ? AuthorityJson.fromEntity(createdAuthorities[0])
                    : null;
        });
    }

    @Nullable
    public AuthorityJson[] createAuthorities(String username, String... authorities) {
        return xaTxTemplate.execute(() -> {
            UUID userId = getUserIdByUsername(username);

            AuthorityEntity[] authEntities = new AuthorityEntity[authorities.length];
            for (int i = 0; i < authorities.length; i++) {
                AuthorityEntity authAuthority = new AuthorityEntity();
//                authAuthority.getUser().getId();
                AuthUserEntity user = new AuthUserEntity();
                user.setId(userId);
                authAuthority.setUser(user);
                authAuthority.setAuthority(Authority.valueOf(authorities[i]));
                authEntities[i] = authAuthority;
            }

            AuthorityEntity[] createdAuthorities = authAuthorityDao.create(authEntities);
            return createdAuthorities != null ? toAuthorityJsonArray(createdAuthorities) : null;
        });
    }

    @NonNull
    public List<AuthorityJson> getAuthoritiesByUsername(String username) {
        return Objects.requireNonNull(xaTxTemplate.execute(() -> {
            UUID userId = getUserIdByUsername(username);
            List<AuthorityEntity> authorities = authAuthorityDao.findAuthoritiesByUserId(userId);
            if (authorities != null) {
                return authorities.stream()
                        .map(AuthorityJson::fromEntity)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }));
    }

    @NonNull
    public List<AuthorityJson> getAuthoritiesByUserId(UUID userId) {
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

    public void deleteAuthority(String username, String authority) {
        xaTxTemplate.execute(() -> {
            UUID userId = getUserIdByUsername(username);
            List<AuthorityEntity> userAuthorities = authAuthorityDao.findAuthoritiesByUserId(userId);

            if (userAuthorities != null) {
                userAuthorities.stream()
                        .filter(auth -> auth.getAuthority().name().equals(authority))
                        .findFirst()
                        .ifPresent(authAuthorityDao::deleteAuthority);
            }

            return null;
        });
    }

    public void deleteAllAuthorities(String username) {
        xaTxTemplate.execute(() -> {
            UUID userId = getUserIdByUsername(username);
            List<AuthorityEntity> authorities = authAuthorityDao.findAuthoritiesByUserId(userId);

            if (authorities != null) {
                for (AuthorityEntity authority : authorities) {
                    authAuthorityDao.deleteAuthority(authority);
                }
            }

            return null;
        });
    }

    @NonNull
    private UUID getUserIdByUsername(String username) {
        return Objects.requireNonNull(xaTxTemplate.execute(() -> {
            Optional<AuthUserEntity> user = authUserDao.findByUsername(username);
            return user.map(AuthUserEntity::getId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
        }));
    }
}