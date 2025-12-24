package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.user.FriendshipEntity;
import guru.qa.niffler.data.entity.user.FriendShipId;
import guru.qa.niffler.data.repository.impl.FriendshipRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.FriendshipRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.FriendshipRepositorySpringJdbc;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository {

    static FriendshipRepository getInstance() {
        return switch (System.getProperty("repository", "jpa")) {
            case "jpa" -> new FriendshipRepositoryHibernate();
            case "jdbc" -> new FriendshipRepositoryJdbc();
            case "sjdbc" -> new FriendshipRepositorySpringJdbc();
            default -> throw new IllegalStateException("Unrecognized repository: " + System.getProperty("repository"));
        };
    }

    FriendshipEntity create(FriendshipEntity friendship);

    Optional<FriendshipEntity> findById(FriendShipId id);

    List<FriendshipEntity> findByRequester(String username);

    List<FriendshipEntity> findByAddressee(String username);

    FriendshipEntity update(FriendshipEntity friendship);

    void remove(FriendshipEntity friendship);
}