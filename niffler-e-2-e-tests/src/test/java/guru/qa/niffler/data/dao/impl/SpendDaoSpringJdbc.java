package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.extractor.SpendEntityRowExtractor;
import guru.qa.niffler.data.mapper.SpendEntityRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.DataSources.dataSource;
import static java.util.Objects.requireNonNull;

public class SpendDaoSpringJdbc implements SpendDao {

    private static final Config CFG = Config.getInstance();

    @Override
    public SpendEntity create(SpendEntity spend) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource(CFG.spendJdbcUrl()));
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO spend (username, currency, spend_date, amount, description, category_id) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, spend.getUsername());
            ps.setString(2, spend.getCurrency().name());
            ps.setDate(3, new java.sql.Date(spend.getSpendDate().getTime()));
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
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource(CFG.spendJdbcUrl()));
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

    @Nonnull
    @Override
    public List<SpendEntity> findAllByUsername(String username) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource(CFG.spendJdbcUrl()));
        try {
            return requireNonNull(jdbcTemplate.query(
                    "SELECT * FROM \"spend\" WHERE username = ?",
                    SpendEntityRowExtractor.instance,
                    username
            ));
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void delete(SpendEntity spend) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource(CFG.spendJdbcUrl()));
        jdbcTemplate.update("DELETE FROM spend WHERE id = ?", spend.getId());
    }

    @Nonnull
    @Override
    public List<SpendEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource(CFG.spendJdbcUrl()));
        return requireNonNull(jdbcTemplate.query(
                "SELECT * FROM \"spend\"",
                SpendEntityRowExtractor.instance
        ));
    }


}
