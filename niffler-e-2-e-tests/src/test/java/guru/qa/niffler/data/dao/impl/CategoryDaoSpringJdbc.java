package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.mapper.CategoryEntityRowMapper;
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

    private final JdbcTemplate jdbcTemplate;

    public CategoryDaoSpringJdbc(DataSource dataSource) {
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
        String sql = "SELECT * FROM category WHERE username = ? ORDER BY name";

        return jdbcTemplate.query(sql, CategoryEntityRowMapper.instance, username);
    }

    @Override
    public void delete(CategoryEntity category) {
        jdbcTemplate.update("DELETE FROM category WHERE id = ?", category.getId());
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
    public List<CategoryEntity> findAll() {
        String sql = "SELECT * FROM category ORDER BY username, name";

        return jdbcTemplate.query(sql, CategoryEntityRowMapper.instance);
    }

}
