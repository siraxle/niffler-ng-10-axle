package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.user.FriendShipId;
import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.mapper.FriendshipEntityRowMapper;
import guru.qa.niffler.data.repository.FriendshipRepository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class FriendshipRepositoryJdbc implements FriendshipRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public FriendshipEntity create(FriendshipEntity friendship) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "INSERT INTO friendship (requester_id, addressee_id, created_date, status) " +
                        "VALUES (?, ?, ?, ?)"
        )) {
            ps.setObject(1, friendship.getRequester().getId());
            ps.setObject(2, friendship.getAddressee().getId());
            ps.setDate(3, new java.sql.Date(friendship.getCreatedDate().getTime()));
            ps.setString(4, friendship.getStatus().name());
            ps.executeUpdate();

            return friendship;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendshipEntity> findById(FriendShipId id) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "SELECT f.*, u1.username as requester_username, u2.username as addressee_username " +
                        "FROM friendship f " +
                        "JOIN \"user\" u1 ON f.requester_id = u1.id " +
                        "JOIN \"user\" u2 ON f.addressee_id = u2.id " +
                        "WHERE f.requester_id = ? AND f.addressee_id = ?"
        )) {
            ps.setObject(1, id.getRequester());
            ps.setObject(2, id.getAddressee());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(FriendshipEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FriendshipEntity> findByRequester(String username) {
        List<FriendshipEntity> friendships = new ArrayList<>();
        String sql = "SELECT f.*, u1.username as requester_username, u2.username as addressee_username " +
                "FROM friendship f " +
                "JOIN \"user\" u1 ON f.requester_id = u1.id " +
                "JOIN \"user\" u2 ON f.addressee_id = u2.id " +
                "WHERE u1.username = ? ORDER BY f.created_date DESC";

        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    friendships.add(FriendshipEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friendships;
    }

    @Override
    public List<FriendshipEntity> findByAddressee(String username) {
        List<FriendshipEntity> friendships = new ArrayList<>();
        String sql = "SELECT f.*, u1.username as requester_username, u2.username as addressee_username " +
                "FROM friendship f " +
                "JOIN \"user\" u1 ON f.requester_id = u1.id " +
                "JOIN \"user\" u2 ON f.addressee_id = u2.id " +
                "WHERE u2.username = ? ORDER BY f.created_date DESC";

        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    friendships.add(FriendshipEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friendships;
    }

    @Override
    public FriendshipEntity update(FriendshipEntity friendship) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "UPDATE friendship SET created_date = ?, status = ? WHERE requester_id = ? AND addressee_id = ?"
        )) {
            ps.setDate(1, new Date(friendship.getCreatedDate().getTime()));
            ps.setString(2, friendship.getStatus().name());
            ps.setObject(3, friendship.getRequester().getId());
            ps.setObject(4, friendship.getAddressee().getId());

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Friendship not found");
            }
            return friendship;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(FriendshipEntity friendship) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "DELETE FROM friendship WHERE requester_id = ? AND addressee_id = ?"
        )) {
            ps.setObject(1, friendship.getRequester().getId());
            ps.setObject(2, friendship.getAddressee().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}