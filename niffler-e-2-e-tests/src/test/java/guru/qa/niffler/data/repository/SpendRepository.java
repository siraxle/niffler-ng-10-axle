package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.spend.SpendEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpendRepository {
    SpendEntity create(SpendEntity spend);
    Optional<SpendEntity> findById(UUID id);
    List<SpendEntity> findByUsername(String username);
    List<SpendEntity> findByCategory(String categoryName, String username);
    SpendEntity update(SpendEntity spend);
    void delete(SpendEntity spend);
    List<SpendEntity> findAll();
}