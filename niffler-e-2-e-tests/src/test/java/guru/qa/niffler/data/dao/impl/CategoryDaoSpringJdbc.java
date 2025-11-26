package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.mapper.CategoryEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryDaoSpringJdbc implements CategoryDao {

    private static final Config CFG = Config.getInstance();

    @Override
    public CategoryEntity create(CategoryEntity category) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
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
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        "SELECT * FROM category WHERE id = ?",
                        CategoryEntityRowMapper.instance,
                        id
                )
        );
    }

    @Override
    public Optional<CategoryEntity> findByUsernameAndCategoryName(String username, String categoryName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        "SELECT * FROM category WHERE username = ? AND name = ?",
                        CategoryEntityRowMapper.instance,
                        username, categoryName
                )
        );
    }

    @Override
    public List<CategoryEntity> findAllByUserName(String username) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        String sql = "SELECT * FROM category WHERE username = ? ORDER BY name";

        return jdbcTemplate.query(sql, CategoryEntityRowMapper.instance, username);
    }

    @Override
    public void delete(CategoryEntity category) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        jdbcTemplate.update("DELETE FROM category WHERE id = ?", category.getId());
    }

    @Override
    public CategoryEntity update(CategoryEntity category) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
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
    public List<CategoryEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        String sql = "SELECT * FROM category ORDER BY username, name";

        return jdbcTemplate.query(sql, CategoryEntityRowMapper.instance);
    }

}
