package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.entity.user.FriendShipId;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository {
    FriendshipEntity create(FriendshipEntity friendship);
    Optional<FriendshipEntity> findById(FriendShipId id);
    List<FriendshipEntity> findByRequester(String username);
    List<FriendshipEntity> findByAddressee(String username);
    FriendshipEntity update(FriendshipEntity friendship);
    void remove(FriendshipEntity friendship);
}