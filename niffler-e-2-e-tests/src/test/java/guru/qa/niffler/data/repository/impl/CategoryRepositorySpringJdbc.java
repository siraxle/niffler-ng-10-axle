package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.mapper.CategoryEntityRowMapper;
import guru.qa.niffler.data.repository.CategoryRepository;
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

public class CategoryRepositorySpringJdbc implements CategoryRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public CategoryRepositorySpringJdbc() {
        DataSource dataSource = dataSource(CFG.spendJdbcUrl());
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public CategoryEntity create(CategoryEntity category) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO category (name, username, archived) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, category.getName());
            ps.setString(2, category.getUsername());
            ps.setBoolean(3, category.isArchived());
            return ps;
        }, keyHolder);

        UUID generatedId = (UUID) keyHolder.getKeys().get("id");
        category.setId(generatedId);
        return category;
    }

    @Override
    public Optional<CategoryEntity> findById(UUID id) {
        String sql = "SELECT * FROM category WHERE id = ?";
        try {
            CategoryEntity category = jdbcTemplate.queryForObject(sql, CategoryEntityRowMapper.instance, id);
            return Optional.ofNullable(category);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CategoryEntity> findByUsernameAndName(String username, String name) {
        String sql = "SELECT * FROM category WHERE username = ? AND name = ?";
        try {
            CategoryEntity category = jdbcTemplate.queryForObject(sql, CategoryEntityRowMapper.instance, username, name);
            return Optional.ofNullable(category);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CategoryEntity> findByUsername(String username) {
        String sql = "SELECT * FROM category WHERE username = ? ORDER BY name";
        return jdbcTemplate.query(sql, CategoryEntityRowMapper.instance, username);
    }

    @Override
    public CategoryEntity update(CategoryEntity category) {
        jdbcTemplate.update(
                "UPDATE category SET name = ?, username = ?, archived = ? WHERE id = ?",
                category.getName(),
                category.getUsername(),
                category.isArchived(),
                category.getId()
        );
        return category;
    }

    @Override
    public void remove(CategoryEntity category) {
        jdbcTemplate.update("DELETE FROM category WHERE id = ?", category.getId());
    }

    @Override
    public List<CategoryEntity> findAll() {
        String sql = "SELECT * FROM category ORDER BY username, name";
        return jdbcTemplate.query(sql, CategoryEntityRowMapper.instance);
    }
}