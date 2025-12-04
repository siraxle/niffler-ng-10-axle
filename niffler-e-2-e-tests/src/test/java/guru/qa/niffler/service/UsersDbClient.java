package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.repository.UserDataUserRepository;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.UserdataUserRepositoryHibernate;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.JdbcTransactionTemplate;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.utils.RandomDataUtils;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;


public class UsersDbClient implements UsersClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final AuthUserRepository authUserRepository = new AuthUserRepositoryHibernate();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();
    private final UserDataUserRepository udUserRepository = new UserdataUserRepositoryHibernate();

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

    @Override
    public UserJson createUser(String username, String password) {
        return xaTxTemplate.execute(() -> {
                    AuthUserEntity authUser = authUserEntity(username, password);
                    authUserRepository.create(authUser);
                    return UserJson.fromEntity(
                            udUserRepository.create(userEntity(username))
                            // тут должен быть null или параметр friendsState 36:28 урок 6.2
                    );
                }
        );
    }

    @Override
    public void createFriends(UserJson targetUser, int count) {
        xaTxTemplate.execute(() -> {
            UserEntity targetEntity = udUserRepository.findById(targetUser.id())
                    .orElseThrow(() -> new IllegalArgumentException("Target user not found with id: " + targetUser.id()));
            for (int i = 0; i < count; i++) {
                UserEntity friendEntity = createRandomUser("friend_" + (i + 1) + "_" + targetUser.username());
                UserEntity savedFriend = udUserRepository.create(friendEntity);
                udUserRepository.addFriend(targetEntity, savedFriend);
                udUserRepository.addFriend(savedFriend, targetEntity);
//                udUserRepository.acceptFriend(savedFriend, targetEntity);
//                udUserRepository.acceptFriend(targetEntity, savedFriend);
            }
            return null;
        });
    }

    @Override
    public Optional<UserJson> findUserByUsername(String username) {
        return xaTxTemplate.execute(() -> {
            Optional<UserEntity> user = udUserRepository.findByUsername(username);
            return user.map(UserJson::fromEntity);
        });
    }

    @Override
    public Optional<UserJson> findUserById(UUID id) {
        return xaTxTemplate.execute(() -> {
            Optional<UserEntity> user = udUserRepository.findById(id);
            return user.map(UserJson::fromEntity);
        });
    }

    @Override
    public void deleteUser(String username) {
        xaTxTemplate.execute(() -> {
            Optional<UserEntity> user = udUserRepository.findByUsername(username);
            user.ifPresent(udUserRepository::remove);
            return null;
        });
    }

    @Override
    public boolean userExists(String username) {
        return findUserByUsername(username).isPresent();
    }

    @Override
    public void addIncomeInvitation(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = udUserRepository.findById(
                    targetUser.id()
            ).orElseThrow();
            for (int i = 0; i < count; i++) {
                xaTxTemplate.execute(() -> {
                            String username = RandomDataUtils.randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepository.create(authUser);
                            UserEntity adressee = udUserRepository.create(userEntity(username));
                            udUserRepository.addIncomeInvitation(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    @Override
    public void addOutcomeInvitation(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = udUserRepository.findById(
                    targetUser.id()
            ).orElseThrow();
            for (int i = 0; i < count; i++) {
                xaTxTemplate.execute(() -> {
                            String username = RandomDataUtils.randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepository.create(authUser);
                            UserEntity adressee = udUserRepository.create(userEntity(username));
                            udUserRepository.addOutcomeInvitation(targetEntity, adressee);
                            return null;
                        }
                );

            }
        }
    }

    private static UserEntity userEntity(String username) {
        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setCurrency(CurrencyValues.RUB);
        return entity;
    }

    private static AuthUserEntity authUserEntity(String username, String password) {
        AuthUserEntity authUser = new AuthUserEntity();
        authUser.setUsername(username);
        authUser.setPassword(pe.encode(password));
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
        return authUser;
    }

    private UserEntity createRandomUser(String prefix) {
        UserEntity user = new UserEntity();
        user.setUsername(prefix + "_" + UUID.randomUUID().toString().substring(0, 8));
        user.setCurrency(CurrencyValues.USD);
        user.setFirstname("Test");
        user.setSurname("User");
        user.setFullname("Test User");
        return user;
    }

}