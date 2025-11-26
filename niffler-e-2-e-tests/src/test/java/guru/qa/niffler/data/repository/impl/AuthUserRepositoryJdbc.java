package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.model.Authority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class AuthUserRepositoryJdbc implements AuthUserRepository {

    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final Config CFG = Config.getInstance();


    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        try (PreparedStatement userPs = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "INSERT INTO \"user\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
             PreparedStatement authorityPs = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                     "INSERT INTO authority (user_id, authority) VALUES (?, ?)")) {
            String encodedPassword = pe.encode(user.getPassword());

            userPs.setString(1, user.getUsername());
            userPs.setString(2, encodedPassword);
            userPs.setBoolean(3, user.getEnabled());
            userPs.setBoolean(4, user.getAccountNonExpired());
            userPs.setBoolean(5, user.getAccountNonLocked());
            userPs.setBoolean(6, user.getCredentialsNonExpired());
            userPs.executeUpdate();

            final UUID generatedKey;
            try (ResultSet rs = userPs.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedKey = rs.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            user.setId(generatedKey);

            for (AuthorityEntity authority : user.getAuthorities()) {
                authorityPs.setObject(1, generatedKey);
                authorityPs.setString(2, authority.getAuthority().name());
                authorityPs.addBatch();
                authority.setUser(user);
            }
            authorityPs.executeBatch();

            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "SELECT u.*, a.id as authority_id, a.authority " +
                        "FROM \"user\" u JOIN authority a ON u.id = a.user_id " +
                        "WHERE u.id = ?"
        )) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                AuthUserEntity user = null;
                List<AuthorityEntity> authorities = new ArrayList<>();

                while (rs.next()) {
                    if (user == null) {
                        user = AuthUserEntityRowMapper.instance.mapRow(rs, rs.getRow());
                    }

                    AuthorityEntity authority = new AuthorityEntity();
                    authority.setId(rs.getObject("authority_id", UUID.class));
                    authority.setAuthority(Authority.valueOf(rs.getString("authority")));
                    authority.setUser(user);
                    authorities.add(authority);
                }

                if (user != null) {
                    user.setAuthorities(authorities);
                    return Optional.of(user);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "SELECT u.*, a.id as authority_id, a.authority " +
                        "FROM \"user\" u LEFT JOIN authority a ON u.id = a.user_id " +
                        "WHERE u.username = ?"
        )) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                AuthUserEntity user = null;
                List<AuthorityEntity> authorities = new ArrayList<>();

                while (rs.next()) {
                    if (user == null) {
                        user = AuthUserEntityRowMapper.instance.mapRow(rs, rs.getRow());
                    }

                    String authorityStr = rs.getString("authority");
                    if (authorityStr != null) {
                        AuthorityEntity authority = new AuthorityEntity();
                        authority.setId(rs.getObject("authority_id", UUID.class));
                        authority.setAuthority(Authority.valueOf(authorityStr));
                        authority.setUser(user);
                        authorities.add(authority);
                    }
                }

                if (user != null) {
                    user.setAuthorities(authorities);
                    return Optional.of(user);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthUserEntity update(AuthUserEntity user) {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "UPDATE \"user\" SET username = ?, password = ?, enabled = ?, account_non_expired = ?, account_non_locked = ?, credentials_non_expired = ? WHERE id = ?"
        )) {
            ps.setString(1, user.getUsername());
            ps.setString(2, pe.encode(user.getPassword()));
            ps.setBoolean(3, user.getEnabled());
            ps.setBoolean(4, user.getAccountNonExpired());
            ps.setBoolean(5, user.getAccountNonLocked());
            ps.setBoolean(6, user.getCredentialsNonExpired());
            ps.setObject(7, user.getId());

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("User not found with id: " + user.getId());
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(AuthUserEntity user) {
        try (PreparedStatement deleteAuthoritiesPs = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "DELETE FROM authority WHERE user_id = ?"
        );
             PreparedStatement deleteUserPs = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                     "DELETE FROM \"user\" WHERE id = ?"
             )) {

            deleteAuthoritiesPs.setObject(1, user.getId());
            deleteAuthoritiesPs.executeUpdate();

            deleteUserPs.setObject(1, user.getId());
            deleteUserPs.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AuthUserEntity> findAll() {
        List<AuthUserEntity> users = new ArrayList<>();

        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "SELECT u.*, a.id as authority_id, a.authority " +
                        "FROM \"user\" u LEFT JOIN authority a ON u.id = a.user_id " +
                        "ORDER BY u.username"
        );
             ResultSet rs = ps.executeQuery()) {

            AuthUserEntity currentUser = null;
            UUID currentUserId = null;

            while (rs.next()) {
                UUID userId = rs.getObject("id", UUID.class);

                // Если это новый пользователь
                if (currentUser == null || !userId.equals(currentUserId)) {
                    if (currentUser != null) {
                        users.add(currentUser);
                    }

                    currentUser = AuthUserEntityRowMapper.instance.mapRow(rs, rs.getRow());
                    currentUser.setAuthorities(new ArrayList<>());
                    currentUserId = userId;
                }

                // Добавляем authority если она есть
                String authorityStr = rs.getString("authority");
                if (authorityStr != null) {
                    AuthorityEntity authority = new AuthorityEntity();
                    authority.setId(rs.getObject("authority_id", UUID.class));
                    authority.setAuthority(Authority.valueOf(authorityStr.toUpperCase()));
                    authority.setUser(currentUser);
                    currentUser.getAuthorities().add(authority);
                }
            }

            // Добавляем последнего пользователя
            if (currentUser != null) {
                users.add(currentUser);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

}
