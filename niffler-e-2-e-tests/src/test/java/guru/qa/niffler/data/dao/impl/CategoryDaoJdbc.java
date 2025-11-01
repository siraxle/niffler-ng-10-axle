package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.entity.spend.CategoryEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryDaoJdbc implements CategoryDao {
    private final Connection connection;

    public CategoryDaoJdbc(Connection connection) {
        this.connection = connection;
    }

    @Override
    public CategoryEntity create(CategoryEntity category) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO category (username, name, archived)" +
                        "VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, category.getUsername());
            ps.setString(2, category.getName());
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
    public Optional<CategoryEntity> findById(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM category WHERE id = ?"
        )) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSetToCategoryEntity(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CategoryEntity> findByUsernameAndCategoryName(String username, String categoryName) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM category WHERE username = ? AND name = ?"
        )) {
            ps.setString(1, username);
            ps.setString(2, categoryName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategoryEntity(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CategoryEntity> findAllByUserName(String username) {
        List<CategoryEntity> categories = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM category WHERE username = ? ORDER BY name"
        )) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategoryEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return categories;
    }

    @Override
    public void delete(CategoryEntity category) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM category WHERE id = ?"
        )) {
            ps.setObject(1, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CategoryEntity update(CategoryEntity category) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE category SET username = ?, name = ?, archived = ? WHERE id = ?"
        )) {
            ps.setString(1, category.getUsername());
            ps.setString(2, category.getName());
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

    private CategoryEntity mapResultSetToCategoryEntity(ResultSet rs) throws SQLException {
        CategoryEntity category = new CategoryEntity();
        category.setId(rs.getObject("id", UUID.class));
        category.setUsername(rs.getString("username"));
        category.setName(rs.getString("name"));
        category.setArchived(rs.getBoolean("archived"));
        return category;
    }
}
