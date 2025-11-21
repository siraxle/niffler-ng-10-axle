package guru.qa.niffler.data.extractor;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.model.Authority;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthUserWithAuthoritiesExtractor implements ResultSetExtractor<AuthUserEntity> {

    @Override
    public AuthUserEntity extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, AuthUserEntity> userMap = new ConcurrentHashMap<>();

        while (rs.next()) {
            UUID userId = rs.getObject("id", UUID.class);

            AuthUserEntity user = userMap.computeIfAbsent(userId, id -> {
                try {
                    AuthUserEntity newUser = new AuthUserEntity();
                    newUser.setId(id);
                    newUser.setUsername(rs.getString("username"));
                    newUser.setPassword(rs.getString("password"));
                    newUser.setEnabled(rs.getBoolean("enabled"));
                    newUser.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    newUser.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    newUser.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                    newUser.setAuthorities(new ArrayList<>());
                    return newUser;
                } catch (SQLException e) {
                    throw new RuntimeException("Error mapping user data", e);
                }
            });

            // Добавляем authority к пользователю
            UUID authorityId = rs.getObject("authority_id", UUID.class);
            if (authorityId != null) {
                AuthorityEntity authority = new AuthorityEntity();
                authority.setId(authorityId);
                authority.setAuthority(Authority.valueOf(rs.getString("authority")));
                authority.setUser(user); // Устанавливаем двустороннюю связь

                user.getAuthorities().add(authority);
            }
        }

        // Возвращаем первого пользователя из мапы (должен быть только один)
        return userMap.isEmpty() ? null : userMap.values().iterator().next();
    }
}