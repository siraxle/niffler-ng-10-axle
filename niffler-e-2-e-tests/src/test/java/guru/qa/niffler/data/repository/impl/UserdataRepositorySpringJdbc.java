package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.user.FriendshipStatus;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.mapper.UdUserEntityRowMapper;
import guru.qa.niffler.data.repository.UserDataUserRepository;
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

public class UserdataRepositorySpringJdbc implements UserDataUserRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public UserdataRepositorySpringJdbc() {
        DataSource dataSource = dataSource(CFG.userdataJdbcUrl());
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public UserEntity create(UserEntity user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO \"user\" (username, currency, firstname, surname, full_name, photo, photo_small) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getCurrency().name());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getSurname());
            ps.setString(5, user.getFullname());
            ps.setBytes(6, user.getPhoto());
            ps.setBytes(7, user.getPhotoSmall());
            return ps;
        }, keyHolder);

        UUID generatedId = (UUID) keyHolder.getKeys().get("id");
        user.setId(generatedId);
        return user;
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        try {
            UserEntity user = jdbcTemplate.queryForObject(sql, UdUserEntityRowMapper.instance, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        String sql = "SELECT * FROM \"user\" WHERE username = ?";
        try {
            UserEntity user = jdbcTemplate.queryForObject(sql, UdUserEntityRowMapper.instance, username);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public UserEntity update(UserEntity user) {
        jdbcTemplate.update(
                "UPDATE \"user\" SET username = ?, currency = ?, firstname = ?, surname = ?, full_name = ?, photo = ?, photo_small = ? WHERE id = ?",
                user.getUsername(),
                user.getCurrency().name(),
                user.getFirstname(),
                user.getSurname(),
                user.getFullname(),
                user.getPhoto(),
                user.getPhotoSmall(),
                user.getId()
        );
        return user;
    }

    @Override
    public void remove(UserEntity user) {
        jdbcTemplate.update("DELETE FROM \"user\" WHERE id = ?", user.getId());
    }

    @Override
    public List<UserEntity> findAll() {
        String sql = "SELECT * FROM \"user\" ORDER BY username";
        return jdbcTemplate.query(sql, UdUserEntityRowMapper.instance);
    }

    @Override
    public void addInvitation(UserEntity requester, UserEntity addressee) {
        createFriendship(requester, addressee, FriendshipStatus.PENDING);
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        // Создаем двунаправленную дружбу
        createFriendship(requester, addressee, FriendshipStatus.ACCEPTED);
        createFriendship(addressee, requester, FriendshipStatus.ACCEPTED);
    }

    @Override
    public void removeFriend(UserEntity user, UserEntity friend) {
        jdbcTemplate.update(
                "DELETE FROM friendship WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)",
                user.getId(), friend.getId(), friend.getId(), user.getId()
        );
    }

    @Override
    public List<UserEntity> findFriends(UserEntity user) {
        String sql = "SELECT u.* FROM \"user\" u " +
                "JOIN friendship f ON (u.id = f.addressee_id OR u.id = f.requester_id) " +
                "WHERE (f.requester_id = ? OR f.addressee_id = ?) AND f.status = ? AND u.id != ?";

        return jdbcTemplate.query(sql, UdUserEntityRowMapper.instance,
                user.getId(), user.getId(), FriendshipStatus.ACCEPTED.name(), user.getId());
    }

    @Override
    public List<UserEntity> findPendingInvitations(UserEntity user) {
        String sql = "SELECT u.* FROM \"user\" u " +
                "JOIN friendship f ON u.id = f.requester_id " +
                "WHERE f.addressee_id = ? AND f.status = ?";

        return jdbcTemplate.query(sql, UdUserEntityRowMapper.instance,
                user.getId(), FriendshipStatus.PENDING.name());
    }

    private void createFriendship(UserEntity user, UserEntity friend, FriendshipStatus status) {
        jdbcTemplate.update(
                "INSERT INTO friendship (requester_id, addressee_id, created_date, status) VALUES (?, ?, ?, ?)",
                user.getId(),
                friend.getId(),
                new Timestamp(System.currentTimeMillis()),
                status.name()
        );
    }

    @Override
    public void acceptFriend(UserEntity acceptingUser, UserEntity invitingUser) {
        jdbcTemplate.update(
                "UPDATE friendship SET status = ? WHERE requester_id = ? AND addressee_id = ?",
                FriendshipStatus.ACCEPTED.name(),
                invitingUser.getId(),
                acceptingUser.getId()
        );

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE requester_id = ? AND addressee_id = ?",
                Integer.class,
                acceptingUser.getId(),
                invitingUser.getId()
        );

        if (count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO friendship (requester_id, addressee_id, created_date, status) VALUES (?, ?, ?, ?)",
                    acceptingUser.getId(),
                    invitingUser.getId(),
                    new Timestamp(System.currentTimeMillis()),
                    FriendshipStatus.ACCEPTED.name()
            );
        }
    }
}