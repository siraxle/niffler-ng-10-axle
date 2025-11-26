package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.mapper.CategoryEntityRowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class CategoryDaoJdbc implements CategoryDao {
    private static final Config CFG = Config.getInstance();

    @Override
    public CategoryEntity create(CategoryEntity category) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
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
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM category WHERE id = ?"
        )) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(CategoryEntityRowMapper.instance.mapRow(rs, rs.getRow())) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CategoryEntity> findByUsernameAndCategoryName(String username, String categoryName) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM category WHERE username = ? AND name = ?"
        )) {
            ps.setString(1, username);
            ps.setString(2, categoryName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(CategoryEntityRowMapper.instance.mapRow(rs, rs.getRow()));
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
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM category WHERE username = ? ORDER BY name"
        )) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(CategoryEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return categories;
    }


    @Override
    public void delete(CategoryEntity category) {
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
    public CategoryEntity update(CategoryEntity category) {
        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(
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

    @Override
    public List<CategoryEntity> findAll() {
        List<CategoryEntity> categories = new ArrayList<>();
        String sql = "SELECT * FROM category ORDER BY username, name";

        try (PreparedStatement ps = holder(CFG.spendJdbcUrl()).connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(CategoryEntityRowMapper.instance.mapRow(rs, rs.getRow()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return categories;
    }

}
