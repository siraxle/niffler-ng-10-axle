package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendRepository;
import guru.qa.niffler.model.CurrencyValues;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class SpendRepositoryJdbc implements SpendRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public SpendEntity create(SpendEntity spend) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "INSERT INTO spend (username, spend_date, currency, amount, description, category_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, spend.getUsername());
            ps.setDate(2, new java.sql.Date(spend.getSpendDate().getTime()));
            ps.setString(3, spend.getCurrency().name());
            ps.setDouble(4, spend.getAmount());
            ps.setString(5, spend.getDescription());
            ps.setObject(6, spend.getCategory().getId());
            ps.executeUpdate();

            final UUID generatedKey;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedKey = rs.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            spend.setId(generatedKey);
            return spend;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<SpendEntity> findById(UUID id) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                        "FROM spend s JOIN category c ON s.category_id = c.id " +
                        "WHERE s.id = ?"
        )) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSpend(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SpendEntity> findByUsername(String username) {
        List<SpendEntity> spends = new ArrayList<>();
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id " +
                "WHERE s.username = ? ORDER BY s.spend_date DESC";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    spends.add(mapResultSetToSpend(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return spends;
    }

    @Override
    public List<SpendEntity> findByCategory(String categoryName, String username) {
        List<SpendEntity> spends = new ArrayList<>();
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id " +
                "WHERE c.name = ? AND s.username = ? ORDER BY s.spend_date DESC";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setString(1, categoryName);
            ps.setString(2, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    spends.add(mapResultSetToSpend(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return spends;
    }

    @Override
    public SpendEntity update(SpendEntity spend) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "UPDATE spend SET username = ?, spend_date = ?, currency = ?, amount = ?, description = ?, category_id = ? WHERE id = ?"
        )) {
            ps.setString(1, spend.getUsername());
            ps.setDate(2, new java.sql.Date(spend.getSpendDate().getTime()));
            ps.setString(3, spend.getCurrency().name());
            ps.setDouble(4, spend.getAmount());
            ps.setString(5, spend.getDescription());
            ps.setObject(6, spend.getCategory().getId());
            ps.setObject(7, spend.getId());

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Spend not found with id: " + spend.getId());
            }
            return spend;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(SpendEntity spend) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "DELETE FROM spend WHERE id = ?"
        )) {
            ps.setObject(1, spend.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SpendEntity> findAll() {
        List<SpendEntity> spends = new ArrayList<>();
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id " +
                "ORDER BY s.spend_date DESC";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                spends.add(mapResultSetToSpend(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return spends;
    }

    private SpendEntity mapResultSetToSpend(ResultSet rs) throws SQLException {
        SpendEntity spend = new SpendEntity();
        spend.setId(rs.getObject("id", UUID.class));
        spend.setUsername(rs.getString("username"));
        spend.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
        spend.setSpendDate(rs.getDate("spend_date"));
        spend.setAmount(rs.getDouble("amount"));
        spend.setDescription(rs.getString("description"));

        CategoryEntity category = new CategoryEntity();
        category.setId(rs.getObject("category_id", UUID.class));
        category.setName(rs.getString("category_name"));
        category.setUsername(rs.getString("category_username"));
        category.setArchived(rs.getBoolean("category_archived"));
        spend.setCategory(category);

        return spend;
    }
}