package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.mapper.SpendEntityRowMapper;
import guru.qa.niffler.data.repository.SpendRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.DataSources.dataSource;

public class SpendRepositorySpringJdbc implements SpendRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public SpendRepositorySpringJdbc() {
        DataSource dataSource = dataSource(CFG.spendJdbcUrl());
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public SpendEntity create(SpendEntity spend) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO spend (username, spend_date, currency, amount, description, category_id) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, spend.getUsername());
            ps.setTimestamp(2, new Timestamp(spend.getSpendDate().getTime()));
            ps.setString(3, spend.getCurrency().name());
            ps.setDouble(4, spend.getAmount());
            ps.setString(5, spend.getDescription());
            ps.setObject(6, spend.getCategory().getId());
            return ps;
        }, keyHolder);

        UUID generatedId = (UUID) keyHolder.getKeys().get("id");
        spend.setId(generatedId);
        return spend;
    }

    @Override
    public Optional<SpendEntity> findById(UUID id) {
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id " +
                "WHERE s.id = ?";

        try {
            SpendEntity spend = jdbcTemplate.queryForObject(sql, SpendEntityRowMapper.instance, id);
            return Optional.ofNullable(spend);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SpendEntity> findByUsername(String username) {
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id " +
                "WHERE s.username = ? ORDER BY s.spend_date DESC";

        return jdbcTemplate.query(sql, SpendEntityRowMapper.instance, username);
    }

    @Override
    public List<SpendEntity> findByUsernameAndSpendDescription(String username, String description) {
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id " +
                "WHERE s.username = ? AND s.description ILIKE ? ORDER BY s.spend_date DESC";

        return jdbcTemplate.query(sql, SpendEntityRowMapper.instance, username, "%" + description + "%");
    }

    @Override
    public List<SpendEntity> findByCategory(String categoryName, String username) {
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id " +
                "WHERE c.name = ? AND s.username = ? ORDER BY s.spend_date DESC";

        return jdbcTemplate.query(sql, SpendEntityRowMapper.instance, categoryName, username);
    }

    @Override
    public SpendEntity update(SpendEntity spend) {
        jdbcTemplate.update(
                "UPDATE spend SET username = ?, spend_date = ?, currency = ?, amount = ?, description = ?, category_id = ? WHERE id = ?",
                spend.getUsername(),
                new Timestamp(spend.getSpendDate().getTime()),
                spend.getCurrency().name(),
                spend.getAmount(),
                spend.getDescription(),
                spend.getCategory().getId(),
                spend.getId()
        );
        return spend;
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
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
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        String sql = "SELECT * FROM category WHERE id = ?";
        try {
            CategoryEntity category = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                CategoryEntity cat = new CategoryEntity();
                cat.setId(rs.getObject("id", UUID.class));
                cat.setName(rs.getString("name"));
                cat.setUsername(rs.getString("username"));
                cat.setArchived(rs.getBoolean("archived"));
                return cat;
            }, id);
            return Optional.ofNullable(category);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CategoryEntity> findCategoryByUsernameAndCategoryName(String username, String name) {
        String sql = "SELECT * FROM category WHERE username = ? AND name = ?";
        try {
            CategoryEntity category = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                CategoryEntity cat = new CategoryEntity();
                cat.setId(rs.getObject("id", UUID.class));
                cat.setName(rs.getString("name"));
                cat.setUsername(rs.getString("username"));
                cat.setArchived(rs.getBoolean("archived"));
                return cat;
            }, username, name);
            return Optional.ofNullable(category);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void remove(SpendEntity spend) {
        jdbcTemplate.update("DELETE FROM spend WHERE id = ?", spend.getId());
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        jdbcTemplate.update("DELETE FROM category WHERE id = ?", category.getId());
    }

    @Override
    public List<SpendEntity> findAll() {
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id " +
                "ORDER BY s.spend_date DESC";

        return jdbcTemplate.query(sql, SpendEntityRowMapper.instance);
    }
}