package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.user.FriendshipStatus;
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

import java.util.*;

import static java.util.Objects.requireNonNull;


public class UsersDbClient implements UsersClient {
    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final AuthUserRepository authUserRepository = new AuthUserRepositoryHibernate();
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

    //    @Override
//    public UserJson createUser(String username, String password) {
//        return xaTxTemplate.execute(() -> {
//                    AuthUserEntity authUser = authUserEntity(username, password);
//                    authUserRepository.create(authUser);
//                    return UserJson.fromEntity(
//                            udUserRepository.create(userEntity(username)),
//                            null
//                    );
//                }
//        );
//    }
    @Override
    public UserJson createUser(String username, String password) {
        return requireNonNull(xaTxTemplate.execute(() -> {
                    AuthUserEntity authUser = authUserEntity(username, password);
                    authUserRepository.create(authUser);
                    return UserJson.fromEntity(
                            udUserRepository.create(userEntity(username)),
                            null
                    );
                }
        ));
    }

    @Override
    public List<UserJson> createFriends(UserJson targetUser, int count) {
        System.out.println("DEBUG: Creating friends for user id=" + targetUser.id());
        return xaTxTemplate.execute(() -> {
            List<UserJson> friends = new ArrayList<>();
            UserEntity targetEntity = udUserRepository.findById(targetUser.id())
                    .orElseThrow(() -> new IllegalArgumentException("Target user not found with id: " + targetUser.id()));
            System.out.println("DEBUG: Target entity loaded: " + targetEntity.getUsername());

            for (int i = 0; i < count; i++) {
                UserEntity friendEntity = createRandomUser("friend_" + (i + 1) + "_" + targetUser.username());
                System.out.println("DEBUG: Creating friend: " + friendEntity.getUsername());
                UserEntity savedFriend = udUserRepository.create(friendEntity);
                udUserRepository.addFriend(targetEntity, savedFriend);
                udUserRepository.addFriend(savedFriend, targetEntity);
                friends.add(UserJson.fromEntity(savedFriend, FriendshipStatus.FRIEND));
            }
            return friends;
        });
    }

    @Override
    public Optional<UserJson> findUserByUsername(String username) {
        return xaTxTemplate.execute(() -> {
            Optional<UserEntity> user = udUserRepository.findByUsername(username);
            return user.map(u -> UserJson.fromEntity(u, null));
        });
    }

    @Override
    public Optional<UserJson> findUserById(UUID id) {
        return xaTxTemplate.execute(() -> {
            Optional<UserEntity> user = udUserRepository.findById(id);
            return user.map(u -> UserJson.fromEntity(u, null));
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
    public List<UserJson> addIncomeInvitation(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = udUserRepository.findById(targetUser.id())
                    .orElseThrow();

            List<UserJson> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String username = RandomDataUtils.randomUsername();

                xaTxTemplate.execute(() -> {
                    AuthUserEntity authUser = authUserEntity(username, "12345");
                    authUserRepository.create(authUser);
                    UserEntity adressee = udUserRepository.create(userEntity(username));
                    udUserRepository.addIncomeInvitation(targetEntity, adressee);
                    return null;
                });

                result.add(UserJson.fromEntity(
                        udUserRepository.findByUsername(username).orElseThrow(),
                        FriendshipStatus.INVITE_RECEIVED
                ));
            }
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    public List<UserJson> addOutcomeInvitation(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = udUserRepository.findById(targetUser.id())
                    .orElseThrow();

            List<UserJson> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String username = RandomDataUtils.randomUsername();

                xaTxTemplate.execute(() -> {
                    AuthUserEntity authUser = authUserEntity(username, "12345");
                    authUserRepository.create(authUser);
                    UserEntity adressee = udUserRepository.create(userEntity(username));
                    udUserRepository.addOutcomeInvitation(targetEntity, adressee);
                    return null;
                });

                result.add(UserJson.fromEntity(
                        udUserRepository.findByUsername(username).orElseThrow(),
                        FriendshipStatus.INVITE_SENT
                ));
            }
            return result;
        }
        return Collections.emptyList();
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