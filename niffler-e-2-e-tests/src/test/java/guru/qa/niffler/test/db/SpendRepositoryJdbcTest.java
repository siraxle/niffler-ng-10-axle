package guru.qa.niffler.test.db;

import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.CategoryRepository;
import guru.qa.niffler.data.repository.SpendRepository;
import guru.qa.niffler.data.repository.impl.CategoryRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.SpendRepositoryJdbc;
import guru.qa.niffler.model.CurrencyValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpendRepositoryJdbcTest {

    private final SpendRepository spendRepository = new SpendRepositoryJdbc();
    private final CategoryRepository categoryRepository = new CategoryRepositoryJdbc();
    private CategoryEntity testCategory;
    private String testUsername;

    @BeforeEach
    void setUp() {
        testUsername = "test_user_" + UUID.randomUUID().toString().substring(0, 8);
        testCategory = createTestCategory();
        testCategory = categoryRepository.create(testCategory);
    }

    @Test
    void createSpend() {
        SpendEntity spend = createTestSpend();

        SpendEntity created = spendRepository.create(spend);

        assertNotNull(created.getId());
        assertEquals(testUsername, created.getUsername());
        assertEquals(100.0, created.getAmount());
        assertEquals("Test spend", created.getDescription());
        assertEquals(testCategory.getId(), created.getCategory().getId());
    }

    @Test
    void findById() {
        SpendEntity spend = createTestSpend();
        SpendEntity created = spendRepository.create(spend);

        Optional<SpendEntity> found = spendRepository.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals(created.getAmount(), found.get().getAmount());
        assertEquals(created.getCategory().getId(), found.get().getCategory().getId());
    }

    @Test
    void findByUsername() {
        SpendEntity spend = createTestSpend();
        spendRepository.create(spend);

        List<SpendEntity> spends = spendRepository.findByUsername(testUsername);

        assertFalse(spends.isEmpty());
        assertEquals(testUsername, spends.get(0).getUsername());
    }

    @Test
    void findByCategory() {
        SpendEntity spend = createTestSpend();
        spendRepository.create(spend);

        List<SpendEntity> spends = spendRepository.findByCategory("Test Category", testUsername);

        assertFalse(spends.isEmpty());
        assertEquals("Test Category", spends.get(0).getCategory().getName());
        assertEquals(testUsername, spends.get(0).getUsername());
    }

    @Test
    void updateSpend() {
        SpendEntity spend = createTestSpend();
        SpendEntity created = spendRepository.create(spend);

        created.setAmount(200.0);
        created.setDescription("Updated spend");
        SpendEntity updated = spendRepository.update(created);

        assertEquals(200.0, updated.getAmount());
        assertEquals("Updated spend", updated.getDescription());
        assertEquals(created.getId(), updated.getId());
    }

    @Test
    void deleteSpend() {
        SpendEntity spend = createTestSpend();
        SpendEntity created = spendRepository.create(spend);

        spendRepository.delete(created);

        Optional<SpendEntity> found = spendRepository.findById(created.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void findAll() {
        SpendEntity spend1 = createTestSpend();
        SpendEntity spend2 = createTestSpend();
        spend2.setAmount(150.0);
        spend2.setDescription("Second spend");

        spendRepository.create(spend1);
        spendRepository.create(spend2);

        List<SpendEntity> allSpends = spendRepository.findAll();

        assertFalse(allSpends.isEmpty());
        assertTrue(allSpends.size() >= 2);
    }

    private CategoryEntity createTestCategory() {
        CategoryEntity category = new CategoryEntity();
        category.setName("Test Category");
        category.setUsername(testUsername);
        category.setArchived(false);
        return category;
    }

    private SpendEntity createTestSpend() {
        SpendEntity spend = new SpendEntity();
        spend.setUsername(testUsername);
        spend.setCurrency(CurrencyValues.USD);
        spend.setSpendDate(new Date());
        spend.setAmount(100.0);
        spend.setDescription("Test spend");
        spend.setCategory(testCategory);
        return spend;
    }
}