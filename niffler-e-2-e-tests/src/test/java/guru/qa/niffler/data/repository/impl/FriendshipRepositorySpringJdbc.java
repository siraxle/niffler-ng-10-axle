package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.user.FriendShipId;
import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.mapper.FriendshipEntityRowMapper;
import guru.qa.niffler.data.repository.FriendshipRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.data.tpl.DataSources.dataSource;

public class FriendshipRepositorySpringJdbc implements FriendshipRepository {

    private static final Config CFG = Config.getInstance();
    private final JdbcTemplate jdbcTemplate;

    public FriendshipRepositorySpringJdbc() {
        DataSource dataSource = dataSource(CFG.userdataJdbcUrl());
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public FriendshipEntity create(FriendshipEntity friendship) {
        jdbcTemplate.update(
                "INSERT INTO friendship (requester_id, addressee_id, created_date, status) VALUES (?, ?, ?, ?)",
                friendship.getRequester().getId(),
                friendship.getAddressee().getId(),
                new Timestamp(friendship.getCreatedDate().getTime()),
                friendship.getStatus().name()
        );
        return friendship;
    }

    @Override
    public Optional<FriendshipEntity> findById(FriendShipId id) {
        String sql = "SELECT f.*, u1.username as requester_username, u2.username as addressee_username " +
                "FROM friendship f " +
                "JOIN \"user\" u1 ON f.requester_id = u1.id " +
                "JOIN \"user\" u2 ON f.addressee_id = u2.id " +
                "WHERE f.requester_id = ? AND f.addressee_id = ?";

        try {
            FriendshipEntity friendship = jdbcTemplate.queryForObject(sql, FriendshipEntityRowMapper.instance,
                    id.getRequester(), id.getAddressee());
            return Optional.ofNullable(friendship);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<FriendshipEntity> findByRequester(String username) {
        String sql = "SELECT f.*, u1.username as requester_username, u2.username as addressee_username " +
                "FROM friendship f " +
                "JOIN \"user\" u1 ON f.requester_id = u1.id " +
                "JOIN \"user\" u2 ON f.addressee_id = u2.id " +
                "WHERE u1.username = ? ORDER BY f.created_date DESC";

        return jdbcTemplate.query(sql, FriendshipEntityRowMapper.instance, username);
    }

    @Override
    public List<FriendshipEntity> findByAddressee(String username) {
        String sql = "SELECT f.*, u1.username as requester_username, u2.username as addressee_username " +
                "FROM friendship f " +
                "JOIN \"user\" u1 ON f.requester_id = u1.id " +
                "JOIN \"user\" u2 ON f.addressee_id = u2.id " +
                "WHERE u2.username = ? ORDER BY f.created_date DESC";

        return jdbcTemplate.query(sql, FriendshipEntityRowMapper.instance, username);
    }

    @Override
    public FriendshipEntity update(FriendshipEntity friendship) {
        jdbcTemplate.update(
                "UPDATE friendship SET created_date = ?, status = ? WHERE requester_id = ? AND addressee_id = ?",
                new Timestamp(friendship.getCreatedDate().getTime()),
                friendship.getStatus().name(),
                friendship.getRequester().getId(),
                friendship.getAddressee().getId()
        );
        return friendship;
    }

    @Override
    public void remove(FriendshipEntity friendship) {
        jdbcTemplate.update(
                "DELETE FROM friendship WHERE requester_id = ? AND addressee_id = ?",
                friendship.getRequester().getId(),
                friendship.getAddressee().getId()
        );
    }
}