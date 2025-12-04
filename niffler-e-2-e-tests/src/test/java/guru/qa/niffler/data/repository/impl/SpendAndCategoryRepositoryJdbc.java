package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.mapper.SpendEntityRowMapper;
import guru.qa.niffler.data.repository.SpendAndCategoryRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class SpendAndCategoryRepositoryJdbc implements SpendAndCategoryRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public SpendEntity createSpend(SpendEntity spend) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "INSERT INTO spend (username, spend_date, currency, amount, description, category_id) VALUES (?, ?, ?, ?, ?, ?)",
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
    public SpendEntity updateSpend(SpendEntity spend) {
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
    public Optional<SpendEntity> findSpendById(UUID id) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                        "FROM spend s JOIN category c ON s.category_id = c.id WHERE s.id = ?"
        )) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(SpendEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SpendEntity> findSpendsByUsername(String username) {
        List<SpendEntity> spends = new ArrayList<>();
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id WHERE s.username = ? ORDER BY s.spend_date DESC";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    spends.add(SpendEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return spends;
    }

    @Override
    public List<SpendEntity> findSpendsByUsernameAndDescription(String username, String description) {
        List<SpendEntity> spends = new ArrayList<>();
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id WHERE s.username = ? AND s.description ILIKE ? ORDER BY s.spend_date DESC";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, "%" + description + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    spends.add(SpendEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return spends;
    }

    @Override
    public List<SpendEntity> findSpendsByCategory(String categoryName, String username) {
        List<SpendEntity> spends = new ArrayList<>();
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id WHERE c.name = ? AND s.username = ? ORDER BY s.spend_date DESC";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setString(1, categoryName);
            ps.setString(2, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    spends.add(SpendEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return spends;
    }

    @Override
    public void removeSpend(SpendEntity spend) {
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
    public List<SpendEntity> findAllSpends() {
        List<SpendEntity> spends = new ArrayList<>();
        String sql = "SELECT s.*, c.id as category_id, c.name as category_name, c.username as category_username, c.archived as category_archived " +
                "FROM spend s JOIN category c ON s.category_id = c.id ORDER BY s.spend_date DESC";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                spends.add(SpendEntityRowMapper.instance.mapRow(rs, rs.getRow()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return spends;
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "INSERT INTO category (name, username, archived) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getUsername());
            ps.setBoolean(3, category.isArchived());
            ps.executeUpdate();

            final UUID generatedKey;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedKey = rs.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            category.setId(generatedKey);
            return category;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CategoryEntity updateCategory(CategoryEntity category) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "UPDATE category SET name = ?, username = ?, archived = ? WHERE id = ?"
        )) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getUsername());
            ps.setBoolean(3, category.isArchived());
            ps.setObject(4, category.getId());

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Category not found with id: " + category.getId());
            }
            return category;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM category WHERE id = ?"
        )) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CategoryEntity category = new CategoryEntity();
                    category.setId(rs.getObject("id", UUID.class));
                    category.setName(rs.getString("name"));
                    category.setUsername(rs.getString("username"));
                    category.setArchived(rs.getBoolean("archived"));
                    return Optional.of(category);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CategoryEntity> findCategoryByUsernameAndName(String username, String name) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM category WHERE username = ? AND name = ?"
        )) {
            ps.setString(1, username);
            ps.setString(2, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CategoryEntity category = new CategoryEntity();
                    category.setId(rs.getObject("id", UUID.class));
                    category.setName(rs.getString("name"));
                    category.setUsername(rs.getString("username"));
                    category.setArchived(rs.getBoolean("archived"));
                    return Optional.of(category);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CategoryEntity> findCategoriesByUsername(String username) {
        List<CategoryEntity> categories = new ArrayList<>();
        String sql = "SELECT * FROM category WHERE username = ? ORDER BY name";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategoryEntity category = new CategoryEntity();
                    category.setId(rs.getObject("id", UUID.class));
                    category.setName(rs.getString("name"));
                    category.setUsername(rs.getString("username"));
                    category.setArchived(rs.getBoolean("archived"));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return categories;
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "DELETE FROM category WHERE id = ?"
        )) {
            ps.setObject(1, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CategoryEntity> findAllCategories() {
        List<CategoryEntity> categories = new ArrayList<>();
        String sql = "SELECT * FROM category ORDER BY username, name";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CategoryEntity category = new CategoryEntity();
                category.setId(rs.getObject("id", UUID.class));
                category.setName(rs.getString("name"));
                category.setUsername(rs.getString("username"));
                category.setArchived(rs.getBoolean("archived"));
                categories.add(category);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return categories;
    }
}