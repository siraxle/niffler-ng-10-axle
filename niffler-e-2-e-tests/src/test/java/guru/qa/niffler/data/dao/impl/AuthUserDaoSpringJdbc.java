package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.extractor.AuthUserWithAuthoritiesExtractor;
import guru.qa.niffler.data.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthUserDaoSpringJdbc implements AuthUserDao {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public AuthUserDaoSpringJdbc() {
        this.jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
    }

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into \"user\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                            "values (?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, user.getEnabled());
            ps.setBoolean(4, user.getAccountNonExpired());
            ps.setBoolean(5, user.getAccountNonLocked());
            ps.setBoolean(6, user.getCredentialsNonExpired());
            return ps;
        }, keyHolder);

        final UUID generatedKey = (UUID) keyHolder.getKeys().get("id");
        user.setId(generatedKey);
        return user;
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        try {
            AuthUserEntity user = jdbcTemplate.queryForObject(
                    "SELECT * FROM \"user\" where id = ?",
                    AuthUserEntityRowMapper.instance,
                    id
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        try {
            AuthUserEntity user = jdbcTemplate.queryForObject(
                    "SELECT * FROM \"user\" WHERE username = ?",
                    AuthUserEntityRowMapper.instance,
                    username
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public AuthUserEntity update(AuthUserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        jdbcTemplate.update(
                "UPDATE \"user\" SET username = ?, password = ?, enabled = ?, account_non_expired = ?, account_non_locked = ?, credentials_non_expired = ? WHERE id = ?",
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                user.getAccountNonExpired(),
                user.getAccountNonLocked(),
                user.getCredentialsNonExpired(),
                user.getId()
        );
        return user;
    }

    @Override
    public void delete(AuthUserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        jdbcTemplate.update("DELETE FROM \"user\" WHERE id = ?", user.getId());
    }

    @Override
    public List<AuthUserEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        return jdbcTemplate.query(
                "SELECT * FROM \"user\" ORDER BY username",
                AuthUserEntityRowMapper.instance
        );
    }

    /**
     * Находит пользователя по ID со всеми его authorities (JOIN запрос)
     */
    public Optional<AuthUserEntity> findByIdWithAuthorities(UUID id) {
        String sql = """
            SELECT a.id as authority_id,
                   authority,
                   u.id,
                   u.username,
                   u.password,
                   u.enabled,
                   u.account_non_expired,
                   u.account_non_locked,
                   u.credentials_non_expired
            FROM "user"
            JOIN authority a ON u.id = a.user_id
            WHERE u.id = ?""";

        AuthUserEntity user = jdbcTemplate.query(sql, AuthUserWithAuthoritiesExtractor.instance, id);
        return Optional.ofNullable(user);
    }

    /**
     * Находит пользователя по username со всеми его authorities (JOIN запрос)
     */
    public Optional<AuthUserEntity> findByUsernameWithAuthorities(String username) {
        String sql = """
            SELECT a.id as authority_id,
                   authority,
                   u.id,
                   u.username,
                   u.password,
                   u.enabled,
                   u.account_non_expired,
                   u.account_non_locked,
                   u.credentials_non_expired
            FROM "user" u\s
            JOIN authority a ON u.id = a.user_id\s
            WHERE u.username = ?
           \s""";

        AuthUserEntity user = jdbcTemplate.query(sql, AuthUserWithAuthoritiesExtractor.instance, username);
        return Optional.ofNullable(user);
    }

}
