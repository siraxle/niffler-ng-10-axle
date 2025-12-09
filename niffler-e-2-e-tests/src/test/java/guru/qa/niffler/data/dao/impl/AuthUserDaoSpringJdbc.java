package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.extractor.AuthUserEntityExtractor;
import guru.qa.niffler.data.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthUserDaoSpringJdbc implements AuthUserDao {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final JdbcTemplate jdbcTemplate;

    public AuthUserDaoSpringJdbc() {
        this.jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
    }

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into \"user\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                            "values (?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, pe.encode(user.getPassword())); // Важно: кодируем пароль!
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
        jdbcTemplate.update(
                "UPDATE \"user\" SET username = ?, password = ?, enabled = ?, account_non_expired = ?, account_non_locked = ?, credentials_non_expired = ? WHERE id = ?",
                user.getUsername(),
                pe.encode(user.getPassword()),
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
        jdbcTemplate.update("DELETE FROM \"user\" WHERE id = ?", user.getId());
    }

    @Override
    public List<AuthUserEntity> findAll() {
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
            FROM "user" u
            LEFT JOIN authority a ON u.id = a.user_id
            ORDER BY u.username""";

        return jdbcTemplate.query(sql, AuthUserEntityExtractor.instance);
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
            FROM "user" u
            LEFT JOIN authority a ON u.id = a.user_id
            WHERE u.id = ?""";

        List<AuthUserEntity> users = jdbcTemplate.query(sql, AuthUserEntityExtractor.instance, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
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
            FROM "user" u
            LEFT JOIN authority a ON u.id = a.user_id
            WHERE u.username = ?""";

        List<AuthUserEntity> users = jdbcTemplate.query(sql, AuthUserEntityExtractor.instance, username);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /**
     * Находит всех пользователей с определенной ролью
     */
    public List<AuthUserEntity> findUsersByAuthority(String authority) {
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
            FROM "user" u
            JOIN authority a ON u.id = a.user_id
            WHERE a.authority = ?
            ORDER BY u.username""";

        return jdbcTemplate.query(sql, AuthUserEntityExtractor.instance, authority);
    }
}