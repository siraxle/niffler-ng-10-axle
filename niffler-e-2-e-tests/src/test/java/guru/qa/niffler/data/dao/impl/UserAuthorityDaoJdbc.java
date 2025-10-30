package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.data.dao.UserAuthorityDao;
import guru.qa.niffler.data.entity.auth.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.CurrencyValues;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserAuthorityDaoJdbc implements UserAuthorityDao {
    private final Connection connection;

    public UserAuthorityDaoJdbc(Connection connection) {
        this.connection = connection;
    }
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    public UserEntity createUser(UserEntity user) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO \"user\" (username, currency, firstname, surname, full_name, photo, photo_small) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getCurrency().name());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getSurname());
            ps.setString(5, user.getFullname());
            ps.setBytes(6, user.getPhoto());
            ps.setBytes(7, user.getPhotoSmall());
            ps.executeUpdate();

            final UUID generatedKey;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedKey = rs.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            user.setId(generatedKey);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM \"user\" WHERE id = ?"
        )) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUserEntity(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM \"user\" WHERE username = ?"
        )) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUserEntity(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UserEntity user) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM \"user\" WHERE id = ?"
        )) {
            ps.setObject(1, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private UserEntity mapResultSetToUserEntity(ResultSet rs) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getObject("id", UUID.class));
        user.setUsername(rs.getString("username"));

        String currencyStr = rs.getString("currency");
        if (currencyStr != null) {
            user.setCurrency(CurrencyValues.valueOf(currencyStr));
        }

        user.setFirstname(rs.getString("firstname"));
        user.setSurname(rs.getString("surname"));
        user.setFullname(rs.getString("full_name"));
        user.setPhoto(rs.getBytes("photo"));
        user.setPhotoSmall(rs.getBytes("photo_small"));
        return user;
    }


    @Override
    public AuthAuthorityEntity[] createAuthority(AuthAuthorityEntity... authorities) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO authority (user_id, authority) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            for (AuthAuthorityEntity authority : authorities) {
                ps.setObject(1, authority.getUserId());
                ps.setString(2, authority.getAuthority().name());
                ps.addBatch();
            }
            ps.executeBatch();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                int i = 0;
                while (rs.next()) {
                    authorities[i].setId(rs.getObject(1, UUID.class));
                    i++;
                }
            }
            return authorities;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AuthAuthorityEntity> findAuthoritiesByUserId(UUID userId) {
        List<AuthAuthorityEntity> authorities = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM authority WHERE user_id = ?"
        )) {
            ps.setObject(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuthAuthorityEntity authority = mapResultSetToAuthAuthorityEntity(rs);
                    authorities.add(authority);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return authorities;
    }

    @Override
    public void deleteAuthority(AuthAuthorityEntity authority) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM authority WHERE id = ?"
        )) {
            ps.setObject(1, authority.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthAuthorityEntity mapResultSetToAuthAuthorityEntity(ResultSet rs) throws SQLException {
        AuthAuthorityEntity authority = new AuthAuthorityEntity();
        authority.setId(rs.getObject("id", UUID.class));
        authority.setUserId(rs.getObject("user_id", UUID.class));
        String authorityStr = rs.getString("authority");
        if (authority != null) {
            authority.setAuthority(Authority.valueOf(authorityStr));
        }
        return authority;
    }

    @Override
    public AuthUserEntity createUser(AuthUserEntity user) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO \"user\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            String encodedPassword = pe.encode(user.getPassword());

            ps.setString(1, user.getUsername());
            ps.setString(2, encodedPassword);
            ps.setBoolean(3, user.getEnabled());
            ps.setBoolean(4, user.getAccountNonExpired());
            ps.setBoolean(5, user.getAccountNonLocked());
            ps.setBoolean(6, user.getCredentialsNonExpired());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getObject("id", UUID.class));
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findUserById(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM \"user\" WHERE id = ?"
        )) {
            ps.setObject(1, id);
            ps.executeQuery();
            try (ResultSet rs = ps.getResultSet()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAuthUser(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findUserByUsername(String username) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM \"user\" WHERE username = ?"
        )) {
            ps.setString(1, username);
            ps.executeQuery();

            try (ResultSet rs = ps.getResultSet()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAuthUser(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthUserEntity updateUser(AuthUserEntity user) {
        try (PreparedStatement ps = connection.prepareStatement(
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
    public void deleteUser(AuthUserEntity user) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM \"user\" WHERE id = ?"
        )) {
            ps.setObject(1, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private AuthUserEntity mapResultSetToAuthUser(ResultSet rs) throws SQLException {
        AuthUserEntity user = new AuthUserEntity();
        user.setId(rs.getObject("id", UUID.class));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password")); // уже закодирован
        user.setEnabled(rs.getBoolean("enabled"));
        user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
        user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
        user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
        return user;
    }
}
