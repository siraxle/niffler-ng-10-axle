package guru.qa.niffler.test.db;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendRepository;
import guru.qa.niffler.data.repository.impl.SpendRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.SpendRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.SpendRepositorySpringJdbc;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Тестирование всех реализаций SpendRepository")
public class AllSpendRepositoriesTest {

    private static final Config CFG = Config.getInstance();

    static Stream<Arguments> repositories() {
        return Stream.of(
                arguments("SpendRepositoryJdbc", new SpendRepositoryJdbc()),
                arguments("SpendRepositorySpringJdbc", new SpendRepositorySpringJdbc()),
                arguments("SpendRepositoryHibernate", new SpendRepositoryHibernate())
        );
    }

    private XaTransactionTemplate xaTxTemplate;
    private String testUsername;
    private String testCategoryName;
    private String testSpendDescription;

    @BeforeEach
    void setUp() {
        xaTxTemplate = new XaTransactionTemplate(CFG.spendJdbcUrl());

        testUsername = RandomDataUtils.randomUsername();
        testCategoryName = RandomDataUtils.randomeCategoryName() + "_" + RandomDataUtils.randomUUID().substring(0, 8);
        testSpendDescription = RandomDataUtils.randomeSentence(3);
    }

    @AfterEach
    void tearDown() {
        // Очистка данных для всех репозиториев
        repositories().forEach(arg -> {
            SpendRepository repo = (SpendRepository) arg.get()[1];
            xaTxTemplate.execute(() -> {
                List<SpendEntity> allSpends = repo.findAll();
                for (SpendEntity spend : allSpends) {
                    if (testUsername.equals(spend.getUsername())) {
                        repo.remove(spend);
                    }
                }
                return null;
            });
        });
    }

    @DisplayName("Создание и поиск категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createAndFindCategory(String repoName, SpendRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = createTestCategory(repository, testUsername, testCategoryName);

            assertNotNull(savedCategory.getId(), "ID категории не должен быть null");
            assertTrue(savedCategory.getName().contains(testCategoryName), "Имя категории должно содержать базовое имя");
            assertEquals(testUsername, savedCategory.getUsername(), "Username должен совпадать");
            assertFalse(savedCategory.isArchived(), "Категория должна создаваться не архивированной");

            Optional<CategoryEntity> foundCategory = repository.findCategoryById(savedCategory.getId());
            assertTrue(foundCategory.isPresent(), "Категория должна находиться по ID");
            assertEquals(savedCategory.getId(), foundCategory.get().getId(), "ID должны совпадать");
            assertEquals(savedCategory.getName(), foundCategory.get().getName(), "Имена должны совпадать");

            Optional<CategoryEntity> foundByUsernameAndName = repository
                    .findCategoryByUsernameAndSpendName(testUsername, savedCategory.getName());
            assertTrue(foundByUsernameAndName.isPresent(), "Категория должна находиться по username и имени");
            assertEquals(savedCategory.getId(), foundByUsernameAndName.get().getId(), "ID должны совпадать");

            return null;
        });
    }

    @DisplayName("Создание и поиск траты")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createAndFindSpend(String repoName, SpendRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = createTestCategory(repository, testUsername, testCategoryName);
            SpendEntity savedSpend = createTestSpend(repository, testUsername, savedCategory, testSpendDescription);

            assertNotNull(savedSpend.getId(), "ID траты не должен быть null");
            assertEquals(testUsername, savedSpend.getUsername(), "Username должен совпадать");
            assertEquals(testSpendDescription, savedSpend.getDescription(), "Описание должно совпадать");
            assertEquals(100.0, savedSpend.getAmount(), "Сумма должна совпадать");
            assertEquals(CurrencyValues.USD, savedSpend.getCurrency(), "Валюта должна совпадать");
            assertNotNull(savedSpend.getCategory(), "Категория не должна быть null");
            assertEquals(savedCategory.getId(), savedSpend.getCategory().getId(), "ID категории должны совпадать");

            Optional<SpendEntity> foundSpend = repository.findById(savedSpend.getId());
            assertTrue(foundSpend.isPresent(), "Трата должна находиться по ID");
            assertEquals(savedSpend.getId(), foundSpend.get().getId(), "ID должны совпадать");
            assertEquals(savedSpend.getDescription(), foundSpend.get().getDescription(), "Описания должны совпадать");

            return null;
        });
    }

    @DisplayName("Поиск трат по username")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByUsername(String repoName, SpendRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = createTestCategory(repository, testUsername, testCategoryName);

            SpendEntity spend1 = new SpendEntity();
            spend1.setUsername(testUsername);
            spend1.setCurrency(CurrencyValues.EUR);
            spend1.setSpendDate(new Date());
            spend1.setAmount(150.0);
            spend1.setDescription("First spend");
            spend1.setCategory(savedCategory);
            repository.create(spend1);

            SpendEntity spend2 = new SpendEntity();
            spend2.setUsername(testUsername);
            spend2.setCurrency(CurrencyValues.USD);
            spend2.setSpendDate(new Date(System.currentTimeMillis() - 86400000));
            spend2.setAmount(200.0);
            spend2.setDescription("Second spend");
            spend2.setCategory(savedCategory);
            repository.create(spend2);

            List<SpendEntity> spends = repository.findByUsername(testUsername);
            assertEquals(2, spends.size(), "Должно быть 2 траты");
            assertEquals("First spend", spends.get(0).getDescription(), "Первая трата должна быть первой");
            assertEquals("Second spend", spends.get(1).getDescription(), "Вторая трата должна быть второй");

            return null;
        });
    }

    @DisplayName("Поиск трат по username и описанию")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByUsernameAndSpendDescription(String repoName, SpendRepository repository) {
        xaTxTemplate.execute(() -> {
            String uniquePart = RandomDataUtils.randomUUID().substring(0, 8);
            String description = "Purchase of " + uniquePart + " items";

            CategoryEntity savedCategory = createTestCategory(repository, testUsername, testCategoryName);
            SpendEntity spend = createTestSpend(repository, testUsername, savedCategory, description);

            List<SpendEntity> found = repository.findByUsernameAndSpendDescription(testUsername, uniquePart);
            assertEquals(1, found.size(), "Должна быть найдена 1 трата");
            assertEquals(spend.getId(), found.get(0).getId(), "ID должны совпадать");
            assertEquals(description, found.get(0).getDescription(), "Описания должны совпадать");

            List<SpendEntity> notFound = repository.findByUsernameAndSpendDescription(testUsername, "NonExistingText");
            assertTrue(notFound.isEmpty(), "Не должно быть найдено трат по несуществующему тексту");

            return null;
        });
    }

    @DisplayName("Поиск трат по категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByCategory(String repoName, SpendRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = createTestCategory(repository, testUsername, testCategoryName);
            SpendEntity spend = createTestSpend(repository, testUsername, savedCategory, testSpendDescription);

            List<SpendEntity> found = repository.findByCategory(savedCategory.getName(), testUsername);
            assertEquals(1, found.size(), "Должна быть найдена 1 трата");
            assertEquals(spend.getId(), found.get(0).getId(), "ID должны совпадать");

            List<SpendEntity> notFound = repository.findByCategory("WrongCategory", testUsername);
            assertTrue(notFound.isEmpty(), "Не должно быть найдено трат по несуществующей категории");

            return null;
        });
    }

    @DisplayName("Обновление траты")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void updateSpend(String repoName, SpendRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = createTestCategory(repository, testUsername, testCategoryName);
            SpendEntity savedSpend = createTestSpend(repository, testUsername, savedCategory, testSpendDescription);

            String updatedDescription = "Updated: " + testSpendDescription;
            savedSpend.setDescription(updatedDescription);
            savedSpend.setAmount(250.0);
            SpendEntity updatedSpend = repository.update(savedSpend);

            assertEquals(updatedDescription, updatedSpend.getDescription(), "Описание должно быть обновлено");
            assertEquals(250.0, updatedSpend.getAmount(), "Сумма должна быть обновлена");

            Optional<SpendEntity> found = repository.findById(savedSpend.getId());
            assertTrue(found.isPresent(), "Трата должна находиться по ID после обновления");
            assertEquals(updatedDescription, found.get().getDescription(), "Обновленное описание должно сохраниться");

            return null;
        });
    }

    @DisplayName("Удаление траты и категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void removeSpendAndCategory(String repoName, SpendRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = createTestCategory(repository, testUsername, testCategoryName);
            SpendEntity savedSpend = createTestSpend(repository, testUsername, savedCategory, testSpendDescription);

            repository.remove(savedSpend);
            Optional<SpendEntity> deletedSpend = repository.findById(savedSpend.getId());
            assertFalse(deletedSpend.isPresent(), "Трата должна быть удалена");

            repository.removeCategory(savedCategory);
            Optional<CategoryEntity> deletedCategory = repository.findCategoryById(savedCategory.getId());
            assertFalse(deletedCategory.isPresent(), "Категория должна быть удалена");

            return null;
        });
    }

    @DisplayName("Получение всех трат")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findAll(String repoName, SpendRepository repository) {
        xaTxTemplate.execute(() -> {
            String otherUsername = RandomDataUtils.randomUsername();
            String otherCategoryName = RandomDataUtils.randomeCategoryName() + "_" + RandomDataUtils.randomUUID().substring(0, 8);

            CategoryEntity category1 = createTestCategory(repository, testUsername, testCategoryName);
            CategoryEntity category2 = createTestCategory(repository, otherUsername, otherCategoryName);

            createTestSpend(repository, testUsername, category1, "First user spend");
            createTestSpend(repository, otherUsername, category2, "Second user spend");

            List<SpendEntity> allSpends = repository.findAll();
            assertFalse(allSpends.isEmpty(), "Список трат не должен быть пустым");
            assertTrue(allSpends.size() >= 2, "Должно быть минимум 2 траты");

            long testUserSpends = allSpends.stream()
                    .filter(spend -> testUsername.equals(spend.getUsername()))
                    .count();
            long otherUserSpends = allSpends.stream()
                    .filter(spend -> otherUsername.equals(spend.getUsername()))
                    .count();

            assertTrue(testUserSpends >= 1, "Должна быть минимум 1 трата для testUser");
            assertTrue(otherUserSpends >= 1, "Должна быть минимум 1 трата для otherUser");

            return null;
        });
    }

    @DisplayName("Проверка начального состояния архивации категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void categoryInitialArchivedState(String repoName, SpendRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = createTestCategory(repository, testUsername, testCategoryName);
            assertFalse(savedCategory.isArchived(), "Категория должна создаваться не архивированной");
            return null;
        });
    }

    private CategoryEntity createTestCategory(SpendRepository repository, String username, String categoryName) {
        CategoryEntity category = new CategoryEntity();
        category.setName(categoryName);
        category.setUsername(username);
        category.setArchived(false);
        return repository.createCategory(category);
    }

    private SpendEntity createTestSpend(SpendRepository repository, String username, CategoryEntity category, String description) {
        SpendEntity spend = new SpendEntity();
        spend.setUsername(username);
        spend.setCurrency(CurrencyValues.USD);
        spend.setSpendDate(new Date());
        spend.setAmount(100.0);
        spend.setDescription(description);
        spend.setCategory(category);
        return repository.create(spend);
    }
}