package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.model.Authority;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AuthAuthorityDaoSpringJdbc implements AuthAuthorityDao {

    private final DataSource dataSource;

    public AuthAuthorityDaoSpringJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public AuthorityEntity[] create(AuthorityEntity... authority) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.batchUpdate("insert into authority(user_id, authority) values(?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, authority[i].getUserId());
                        ps.setString(2, authority[i].getAuthority().name());
                    }

                    @Override
                    public int getBatchSize() {
                        return authority.length;
                    }
                });
        return null;
    }

    @Override
    public List<AuthorityEntity> findAuthoritiesByUserId(UUID userId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT * FROM authority WHERE user_id = ? ORDER BY authority";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToAuthorityEntity(rs), userId);
    }

    @Override
    public void deleteAuthority(AuthorityEntity authority) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("DELETE FROM authority WHERE id = ?", authority.getId());
    }

    @Override
    public List<AuthorityEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT * FROM authority ORDER BY user_id, authority";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToAuthorityEntity(rs));
    }

    private AuthorityEntity mapResultSetToAuthorityEntity(ResultSet rs) throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        AuthorityEntity authority = new AuthorityEntity();
        authority.setId(rs.getObject("id", UUID.class));
        authority.setUserId(rs.getObject("user_id", UUID.class));

        String authorityStr = rs.getString("authority");
        if (authorityStr != null) {
            authority.setAuthority(Authority.valueOf(authorityStr));
        }

        return authority;
    }
}
