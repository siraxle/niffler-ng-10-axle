package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.model.CurrencyValues;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpendDaoSpringJdbc implements SpendDao {

    private final JdbcTemplate jdbcTemplate;

    public SpendDaoSpringJdbc(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public SpendEntity create(SpendEntity spend) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO spend (username, currency, spend_date, amount, description, category_id) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, spend.getUsername());
            ps.setString(2, spend.getCurrency().name());
            ps.setDate(3, spend.getSpendDate());
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
        String sql = """
            SELECT s.*, c.name as name, c.archived as archived 
            FROM spend s 
            JOIN category c ON s.category_id = c.id 
            WHERE s.id = ?
            """;

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return Optional.of(mapResultSetToSpendEntity(rs));
            } else {
                return Optional.empty();
            }
        }, id);
    }

    @Override
    public List<SpendEntity> findAllByUsername(String username) {
        String sql = """
            SELECT s.*, c.name as name, c.archived as archived 
            FROM spend s 
            JOIN category c ON s.category_id = c.id 
            WHERE s.username = ? 
            ORDER BY s.spend_date DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToSpendEntity(rs), username);
    }

    @Override
    public void delete(SpendEntity spend) {
        jdbcTemplate.update("DELETE FROM spend WHERE id = ?", spend.getId());
    }

    private SpendEntity mapResultSetToSpendEntity(ResultSet rs) throws SQLException {
        SpendEntity spend = new SpendEntity();
        spend.setId(rs.getObject("id", UUID.class));
        spend.setUsername(rs.getString("username"));
        spend.setSpendDate(rs.getDate("spend_date"));
        spend.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
        spend.setAmount(rs.getDouble("amount"));
        spend.setDescription(rs.getString("description"));

        // Создаем CategoryEntity
        CategoryEntity category = new CategoryEntity();
        category.setId(rs.getObject("category_id", UUID.class));
        category.setName(rs.getString("name"));
        category.setUsername(rs.getString("username")); // тот же username что и у spend
        category.setArchived(rs.getBoolean("archived"));

        spend.setCategory(category);
        return spend;
    }

    @Override
    public List<SpendEntity> findAll() {
        String sql = """
        SELECT s.*, c.name as category_name, c.archived as category_archived 
        FROM spend s 
        JOIN category c ON s.category_id = c.id 
        ORDER BY s.spend_date DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapResultSetToSpendEntity(rs));
    }



}
