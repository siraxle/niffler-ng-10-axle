package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.mapper.SpendEntityRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
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
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        """
                        SELECT s.*, c.name as category_name, c.archived as category_archived 
                        FROM spend s 
                        JOIN category c ON s.category_id = c.id 
                        WHERE s.id = ?
                        """,
                        SpendEntityRowMapper.instance,
                        id
                )
        );
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

        return jdbcTemplate.query(sql, SpendEntityRowMapper.instance, username);
    }

    @Override
    public void delete(SpendEntity spend) {
        jdbcTemplate.update("DELETE FROM spend WHERE id = ?", spend.getId());
    }

    @Override
    public List<SpendEntity> findAll() {
        String sql = """
        SELECT s.*, c.name as category_name, c.archived as category_archived 
        FROM spend s 
        JOIN category c ON s.category_id = c.id 
        ORDER BY s.spend_date DESC
        """;

        return jdbcTemplate.query(sql, SpendEntityRowMapper.instance);
    }



}
