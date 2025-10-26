package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.entity.auth.AuthAuthorityEntity;
import guru.qa.niffler.model.Authority;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthAuthorityDaoJdbc implements AuthAuthorityDao {
    private final Connection connection;

    public AuthAuthorityDaoJdbc(Connection connection) {
        this.connection = connection;
    }

    @Override
    public AuthAuthorityEntity createAuthority(AuthAuthorityEntity authority) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO authority (user_id, authority) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setObject(1, authority.getUserId());
            ps.setString(2, authority.getAuthority().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    authority.setId(rs.getObject("id", UUID.class));
                }
            }
            return authority;
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
                    AuthAuthorityEntity authority = new AuthAuthorityEntity();
                    authority.setId(rs.getObject("id", UUID.class));
                    authority.setUserId(rs.getObject("user_id", UUID.class));
                    authority.setAuthority(Authority.valueOf(rs.getString("authority")));
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


}
