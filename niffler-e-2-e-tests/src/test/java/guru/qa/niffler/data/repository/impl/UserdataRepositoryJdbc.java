package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.user.FriendshipStatus;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.mapper.UdUserEntityRowMapper;
import guru.qa.niffler.data.repository.UserDataUserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class UserdataRepositoryJdbc implements UserDataUserRepository {

    private static final Config CFG = Config.getInstance();

    @Override
    public UserEntity create(UserEntity user) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "INSERT INTO \"user\" (username, currency, firstname, surname, full_name, photo, photo_small) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getCurrency().name());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getSurname());
            ps.setString(5, user.getFullname());
            ps.setBytes(6, user.getPhoto());
            ps.setBytes(7, user.getPhotoSmall());
            ps.executeUpdate();

            final UUID generatedKey;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedKey = rs.getObject("id", UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            user.setId(generatedKey);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM \"user\" WHERE id = ?"
        )) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(UdUserEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM \"user\" WHERE username = ?"
        )) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(UdUserEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserEntity update(UserEntity user) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "UPDATE \"user\" SET username = ?, currency = ?, firstname = ?, surname = ?, full_name = ?, photo = ?, photo_small = ? WHERE id = ?"
        )) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getCurrency().name());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getSurname());
            ps.setString(5, user.getFullname());
            ps.setBytes(6, user.getPhoto());
            ps.setBytes(7, user.getPhotoSmall());
            ps.setObject(8, user.getId());

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("User not found with id: " + user.getId());
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(UserEntity user) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "DELETE FROM \"user\" WHERE id = ?"
        )) {
            ps.setObject(1, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserEntity> findAll() {
        List<UserEntity> users = new ArrayList<>();
        String sql = "SELECT * FROM \"user\" ORDER BY username";

        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(UdUserEntityRowMapper.instance.mapRow(rs, rs.getRow()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    @Override
    public void addInvitation(UserEntity requester, UserEntity addressee) {
        createFriendship(requester, addressee, FriendshipStatus.PENDING);
    }


    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        createFriendship(requester, addressee, FriendshipStatus.ACCEPTED);
        createFriendship(addressee, requester, FriendshipStatus.ACCEPTED);
    }

    private void createFriendship(UserEntity user, UserEntity friend, FriendshipStatus status) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "INSERT INTO friendship (requester_id, addressee_id, created_date, status) VALUES (?, ?, ?, ?)"
        )) {
            ps.setObject(1, user.getId());
            ps.setObject(2, friend.getId());
            ps.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            ps.setString(4, status.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeFriend(UserEntity user, UserEntity friend) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "DELETE FROM friendship WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)"
        )) {
            ps.setObject(1, user.getId());
            ps.setObject(2, friend.getId());
            ps.setObject(3, friend.getId());
            ps.setObject(4, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserEntity> findFriends(UserEntity user) {
        List<UserEntity> friends = new ArrayList<>();
        String sql = "SELECT u.* FROM \"user\" u " +
                "JOIN friendship f ON (u.id = f.addressee_id OR u.id = f.requester_id) " +
                "WHERE (f.requester_id = ? OR f.addressee_id = ?) AND f.status = ? AND u.id != ?";

        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ps.setObject(2, user.getId());
            ps.setString(3, FriendshipStatus.ACCEPTED.name());
            ps.setObject(4, user.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    friends.add(UdUserEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return friends;
    }

    @Override
    public List<UserEntity> findPendingInvitations(UserEntity user) {
        List<UserEntity> pendingInvitations = new ArrayList<>();
        String sql = "SELECT u.* FROM \"user\" u " +
                "JOIN friendship f ON u.id = f.requester_id " +
                "WHERE f.addressee_id = ? AND f.status = ?";

        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(sql)) {
            ps.setObject(1, user.getId());
            ps.setString(2, FriendshipStatus.PENDING.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pendingInvitations.add(UdUserEntityRowMapper.instance.mapRow(rs, rs.getRow()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pendingInvitations;
    }

    @Override
    public void acceptFriend(UserEntity acceptingUser, UserEntity invitingUser) {
        try {
            try (PreparedStatement updatePs = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                    "UPDATE friendship SET status = ? WHERE requester_id = ? AND addressee_id = ?"
            )) {
                updatePs.setString(1, FriendshipStatus.ACCEPTED.name());
                updatePs.setObject(2, invitingUser.getId());
                updatePs.setObject(3, acceptingUser.getId());
                int updated = updatePs.executeUpdate();

                if (updated == 0) {
                    throw new RuntimeException("Friendship invitation not found from " +
                            invitingUser.getUsername() + " to " + acceptingUser.getUsername());
                }
            }
            boolean reverseExists;
            try (PreparedStatement checkPs = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                    "SELECT 1 FROM friendship WHERE requester_id = ? AND addressee_id = ?"
            )) {
                checkPs.setObject(1, acceptingUser.getId());
                checkPs.setObject(2, invitingUser.getId());
                try (ResultSet rs = checkPs.executeQuery()) {
                    reverseExists = rs.next();
                }
            }
            if (!reverseExists) {
                try (PreparedStatement insertPs = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                        "INSERT INTO friendship (requester_id, addressee_id, created_date, status) VALUES (?, ?, ?, ?)"
                )) {
                    insertPs.setObject(1, acceptingUser.getId());
                    insertPs.setObject(2, invitingUser.getId());
                    insertPs.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                    insertPs.setString(4, FriendshipStatus.ACCEPTED.name());
                    insertPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to accept friend invitation", e);
        }
    }

}