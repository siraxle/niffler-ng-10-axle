package guru.qa.niffler.data.mapper;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.model.Authority;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AuthorityEntityRowMapper implements RowMapper<AuthorityEntity> {

    public static final AuthorityEntityRowMapper instance = new AuthorityEntityRowMapper();

    private AuthorityEntityRowMapper() {
    }

    @Override
    public AuthorityEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuthorityEntity authority = new AuthorityEntity();
        authority.setId(rs.getObject("id", UUID.class));
        AuthUserEntity user = new AuthUserEntity();
        user.setId(rs.getObject("user_id", UUID.class));
        authority.setUser(user);

        String authorityStr = rs.getString("authority");
        if (authorityStr != null) {
            authority.setAuthority(Authority.valueOf(authorityStr));
        }

        return authority;
    }
}