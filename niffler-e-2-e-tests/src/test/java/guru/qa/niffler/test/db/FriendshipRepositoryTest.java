package guru.qa.niffler.test.db;

import guru.qa.niffler.data.entity.user.FriendShipId;
import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.entity.user.FriendshipStatus;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.repository.FriendshipRepository;
import guru.qa.niffler.data.repository.UserDataRepository;
import guru.qa.niffler.data.repository.impl.FriendshipRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.UserDataRepositoryJdbc;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FriendshipRepositoryTest {

    private FriendshipRepository friendshipRepository;
    private UserDataRepository userDataRepository;
    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    void setUp() {
        friendshipRepository = new FriendshipRepositoryJdbc();
        userDataRepository = new UserDataRepositoryJdbc();

        user1 = createTestUser();
        user2 = createTestUser();

        userDataRepository.create(user1);
        userDataRepository.create(user2);
    }

    @Test
    void createFriendship() {
        FriendshipEntity friendship = createTestFriendship();

        FriendshipEntity created = friendshipRepository.create(friendship);

        assertNotNull(created);
        assertEquals(user1.getId(), created.getRequester().getId());
        assertEquals(user2.getId(), created.getAddressee().getId());
    }

    @Test
    void findById() {
        FriendshipEntity friendship = createTestFriendship();
        friendshipRepository.create(friendship);

        FriendShipId id = new FriendShipId();
        id.setRequester(user1.getId());
        id.setAddressee(user2.getId());

        Optional<FriendshipEntity> found = friendshipRepository.findById(id);

        assertTrue(found.isPresent());
        assertEquals(FriendshipStatus.PENDING, found.get().getStatus());
    }

    @Test
    void findByRequester() {
        FriendshipEntity friendship = createTestFriendship();
        friendshipRepository.create(friendship);

        List<FriendshipEntity> friendships = friendshipRepository.findByRequester(user1.getUsername());

        assertFalse(friendships.isEmpty());
        assertEquals(user1.getUsername(), friendships.get(0).getRequester().getUsername());
    }

    @Test
    void findByAddressee() {
        FriendshipEntity friendship = createTestFriendship();
        friendshipRepository.create(friendship);

        List<FriendshipEntity> friendships = friendshipRepository.findByAddressee(user2.getUsername());

        assertFalse(friendships.isEmpty());
        assertEquals(user2.getUsername(), friendships.get(0).getAddressee().getUsername());
    }

    @Test
    void updateFriendship() {
        FriendshipEntity friendship = createTestFriendship();
        FriendshipEntity created = friendshipRepository.create(friendship);

        created.setStatus(FriendshipStatus.ACCEPTED);
        FriendshipEntity updated = friendshipRepository.update(created);

        assertEquals(FriendshipStatus.ACCEPTED, updated.getStatus());
    }

    @Test
    void deleteFriendship() {
        FriendshipEntity friendship = createTestFriendship();
        friendshipRepository.create(friendship);

        friendshipRepository.delete(friendship);

        FriendShipId id = new FriendShipId();
        id.setRequester(user1.getId());
        id.setAddressee(user2.getId());

        Optional<FriendshipEntity> found = friendshipRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void bidirectionalFriendship() {
        // Создаем двунаправленную дружбу
        FriendshipEntity friendship1 = createTestFriendship();
        friendship1.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.create(friendship1);

        FriendshipEntity friendship2 = new FriendshipEntity();
        friendship2.setRequester(user2);
        friendship2.setAddressee(user1);
        friendship2.setCreatedDate(new Date());
        friendship2.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.create(friendship2);

        // Проверяем что оба пользователя видят друг друга в друзьях
        List<FriendshipEntity> user1Friendships = friendshipRepository.findByRequester(user1.getUsername());
        List<FriendshipEntity> user2Friendships = friendshipRepository.findByRequester(user2.getUsername());

        assertEquals(1, user1Friendships.size());
        assertEquals(1, user2Friendships.size());
    }

    @Test
    void friendshipStatusWorkflow() {
        FriendshipEntity friendship = createTestFriendship();
        friendshipRepository.create(friendship);

        // Проверяем начальный статус
        FriendShipId id = new FriendShipId();
        id.setRequester(user1.getId());
        id.setAddressee(user2.getId());
        assertEquals(FriendshipStatus.PENDING, friendshipRepository.findById(id).get().getStatus());

        // Принимаем дружбу
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.update(friendship);
        assertEquals(FriendshipStatus.ACCEPTED, friendshipRepository.findById(id).get().getStatus());
    }

    @Test
    void findByUserId() {
        // Создаем несколько дружб для пользователя
        UserEntity user3 = createTestUser();
        userDataRepository.create(user3);

        FriendshipEntity friendship1 = createTestFriendship(); // user1 -> user2
        friendshipRepository.create(friendship1);

        FriendshipEntity friendship2 = new FriendshipEntity();
        friendship2.setRequester(user1);
        friendship2.setAddressee(user3);
        friendship2.setCreatedDate(new Date());
        friendship2.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.create(friendship2);

        // Должны найти обе дружбы где user1 является requester
        List<FriendshipEntity> friendships = friendshipRepository.findByRequester(user1.getUsername());
        assertEquals(2, friendships.size());
    }

    @Test
    void integrationWithUserDataRepository() {
        // Проверяем что методы UserDataRepository корректно работают с FriendshipRepository
        userDataRepository.addIncomeInvitation(user1, user2);

        List<FriendshipEntity> pending = friendshipRepository.findByAddressee(user2.getUsername());
        assertFalse(pending.isEmpty());
        assertEquals(FriendshipStatus.PENDING, pending.get(0).getStatus());

        userDataRepository.acceptFriend(user2, user1);

        List<FriendshipEntity> accepted = friendshipRepository.findByRequester(user1.getUsername());
        assertEquals(FriendshipStatus.ACCEPTED, accepted.get(0).getStatus());
    }

    private FriendshipEntity createTestFriendship() {
        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setRequester(user1);
        friendship.setAddressee(user2);
        friendship.setCreatedDate(new Date());
        friendship.setStatus(FriendshipStatus.PENDING);
        return friendship;
    }

    private UserEntity createTestUser() {
        UserEntity user = new UserEntity();
        user.setUsername(RandomDataUtils.randomUsername());
        user.setCurrency(CurrencyValues.USD);
        user.setFirstname("Test");
        user.setSurname("User");
        user.setFullname("Test User");
        return user;
    }
}