package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.model.Authority;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.DataSources.dataSource;

public class AuthUserRepositorySpringJdbc implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public AuthUserRepositorySpringJdbc() {
        DataSource dataSource = dataSource(CFG.authJdbcUrl());
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO \"user\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, user.getEnabled());
            ps.setBoolean(4, user.getAccountNonExpired());
            ps.setBoolean(5, user.getAccountNonLocked());
            ps.setBoolean(6, user.getCredentialsNonExpired());
            return ps;
        }, keyHolder);

        UUID generatedId = (UUID) keyHolder.getKeys().get("id");
        user.setId(generatedId);

        // Создаем authorities
        if (user.getAuthorities() != null) {
            for (AuthorityEntity authority : user.getAuthorities()) {
                jdbcTemplate.update(
                        "INSERT INTO authority (user_id, authority) VALUES (?, ?)",
                        generatedId,
                        authority.getAuthority().name()
                );
            }
        }

        return user;
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        try {
            AuthUserEntity user = jdbcTemplate.queryForObject(sql, AuthUserEntityRowMapper.instance, id);
            if (user != null) {
                loadAuthorities(user);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        String sql = "SELECT * FROM \"user\" WHERE username = ?";
        try {
            AuthUserEntity user = jdbcTemplate.queryForObject(sql, AuthUserEntityRowMapper.instance, username);
            if (user != null) {
                loadAuthorities(user);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public AuthUserEntity update(AuthUserEntity user) {
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
    public void remove(AuthUserEntity user) {
        // Сначала удаляем authorities
        jdbcTemplate.update("DELETE FROM authority WHERE user_id = ?", user.getId());
        // Затем удаляем пользователя
        jdbcTemplate.update("DELETE FROM \"user\" WHERE id = ?", user.getId());
    }

    @Override
    public List<AuthUserEntity> findAll() {
        String sql = "SELECT * FROM \"user\" ORDER BY username";
        List<AuthUserEntity> users = jdbcTemplate.query(sql, AuthUserEntityRowMapper.instance);

        for (AuthUserEntity user : users) {
            loadAuthorities(user);
        }

        return users;
    }

    private void loadAuthorities(AuthUserEntity user) {
        String sql = "SELECT * FROM authority WHERE user_id = ?";
        List<AuthorityEntity> authorities = jdbcTemplate.query(sql, (rs, rowNum) -> {
            AuthorityEntity authority = new AuthorityEntity();
            authority.setId(rs.getObject("id", UUID.class));
            authority.setAuthority(Authority.valueOf(rs.getString("authority")));
            authority.setUser(user);
            return authority;
        }, user.getId());

        user.setAuthorities(authorities);
    }
}