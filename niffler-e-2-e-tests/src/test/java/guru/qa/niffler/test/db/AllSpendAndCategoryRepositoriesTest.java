package guru.qa.niffler.test.db;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendAndCategoryRepository;
import guru.qa.niffler.data.repository.impl.SpendAndCategoryRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.SpendAndCategoryRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.SpendAndCategoryRepositorySpringJdbc;
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

@DisplayName("Тестирование всех реализаций SpendAndCategoryRepository")
public class AllSpendAndCategoryRepositoriesTest {

    private static final Config CFG = Config.getInstance();

    static Stream<Arguments> repositories() {
        return Stream.of(
                arguments("SpendAndCategoryRepositoryJdbc", new SpendAndCategoryRepositoryJdbc()),
                arguments("SpendAndCategoryRepositorySpringJdbc", new SpendAndCategoryRepositorySpringJdbc()),
                arguments("SpendAndCategoryRepositoryHibernate", new SpendAndCategoryRepositoryHibernate())
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
        repositories().forEach(arg -> {
            SpendAndCategoryRepository repo = (SpendAndCategoryRepository) arg.get()[1];
            XaTransactionTemplate cleanupTx = new XaTransactionTemplate(CFG.spendJdbcUrl());
            cleanupTx.execute(() -> {
                // Очистка трат
                List<SpendEntity> allSpends = repo.findAllSpends();
                for (SpendEntity spend : allSpends) {
                    if (testUsername.equals(spend.getUsername())) {
                        repo.removeSpend(spend);
                    }
                }
                // Очистка категорий
                List<CategoryEntity> allCategories = repo.findAllCategories();
                for (CategoryEntity category : allCategories) {
                    if (testUsername.equals(category.getUsername())) {
                        repo.removeCategory(category);
                    }
                }
                return null;
            });
        });
    }

    @DisplayName("Создание категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createCategoryTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity created = repository.createCategory(category);

            assertNonnull(created.getId());
            assertEquals(testCategoryName, created.getName());
            assertEquals(testUsername, created.getUsername());
            assertFalse(created.isArchived());

            return null;
        });
    }

    @DisplayName("Поиск категории по ID")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findCategoryByIdTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity created = repository.createCategory(category);

            Optional<CategoryEntity> found = repository.findCategoryById(created.getId());
            assertTrue(found.isPresent());
            assertEquals(created.getId(), found.get().getId());
            assertEquals(testCategoryName, found.get().getName());
            assertEquals(testUsername, found.get().getUsername());

            return null;
        });
    }

    @DisplayName("Поиск категории по username и имени")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findCategoryByUsernameAndNameTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            repository.createCategory(category);

            Optional<CategoryEntity> found = repository.findCategoryByUsernameAndName(testUsername, testCategoryName);
            assertTrue(found.isPresent());
            assertEquals(testCategoryName, found.get().getName());
            assertEquals(testUsername, found.get().getUsername());

            return null;
        });
    }

    @DisplayName("Поиск категорий по username")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findCategoriesByUsernameTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            String categoryName1 = testCategoryName + "_1";
            String categoryName2 = testCategoryName + "_2";

            CategoryEntity category1 = createTestCategory(testUsername, categoryName1);
            CategoryEntity category2 = createTestCategory(testUsername, categoryName2);

            repository.createCategory(category1);
            repository.createCategory(category2);

            List<CategoryEntity> found = repository.findCategoriesByUsername(testUsername);
            assertEquals(2, found.size());

            boolean hasCategory1 = found.stream()
                    .anyMatch(c -> categoryName1.equals(c.getName()));
            boolean hasCategory2 = found.stream()
                    .anyMatch(c -> categoryName2.equals(c.getName()));

            assertTrue(hasCategory1);
            assertTrue(hasCategory2);

            return null;
        });
    }

    @DisplayName("Обновление категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void updateCategoryTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity created = repository.createCategory(category);

            String updatedName = testCategoryName + "_updated";
            created.setName(updatedName);
            created.setArchived(true);

            CategoryEntity updated = repository.updateCategory(created);
            assertEquals(updatedName, updated.getName());
            assertTrue(updated.isArchived());
            assertEquals(testUsername, updated.getUsername());

            return null;
        });
    }

    @DisplayName("Удаление категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void removeCategoryTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity created = repository.createCategory(category);

            repository.removeCategory(created);

            Optional<CategoryEntity> found = repository.findCategoryByUsernameAndName(testUsername, testCategoryName);
            assertFalse(found.isPresent());

            return null;
        });
    }

    @DisplayName("Получение всех категорий")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findAllCategoriesTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            String otherUsername = RandomDataUtils.randomUsername();
            String otherCategoryName = RandomDataUtils.randomeCategoryName() + "_" + RandomDataUtils.randomUUID().substring(0, 8);

            CategoryEntity category1 = createTestCategory(testUsername, testCategoryName);
            CategoryEntity category2 = createTestCategory(otherUsername, otherCategoryName);

            repository.createCategory(category1);
            repository.createCategory(category2);

            List<CategoryEntity> allCategories = repository.findAllCategories();
            assertFalse(allCategories.isEmpty());
            assertTrue(allCategories.size() >= 2);

            long testUserCategories = allCategories.stream()
                    .filter(c -> testUsername.equals(c.getUsername()))
                    .count();
            long otherUserCategories = allCategories.stream()
                    .filter(c -> otherUsername.equals(c.getUsername()))
                    .count();

            assertTrue(testUserCategories >= 1);
            assertTrue(otherUserCategories >= 1);

            return null;
        });
    }

    @DisplayName("Создание и поиск траты")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createAndFindSpendTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = repository.createCategory(createTestCategory(testUsername, testCategoryName));
            SpendEntity savedSpend = createTestSpend(repository, testUsername, savedCategory, testSpendDescription);

            assertNonnull(savedSpend.getId());
            assertEquals(testUsername, savedSpend.getUsername());
            assertEquals(testSpendDescription, savedSpend.getDescription());
            assertEquals(100.0, savedSpend.getAmount());
            assertEquals(CurrencyValues.USD, savedSpend.getCurrency());
            assertNonnull(savedSpend.getCategory());
            assertEquals(savedCategory.getId(), savedSpend.getCategory().getId());

            Optional<SpendEntity> foundSpend = repository.findSpendById(savedSpend.getId());
            assertTrue(foundSpend.isPresent());
            assertEquals(savedSpend.getId(), foundSpend.get().getId());
            assertEquals(savedSpend.getDescription(), foundSpend.get().getDescription());

            return null;
        });
    }

    @DisplayName("Поиск трат по username")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findSpendsByUsernameTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = repository.createCategory(createTestCategory(testUsername, testCategoryName));

            SpendEntity spend1 = createTestSpend(repository, testUsername, savedCategory, "First spend");
            SpendEntity spend2 = createTestSpend(repository, testUsername, savedCategory, "Second spend");

            List<SpendEntity> spends = repository.findSpendsByUsername(testUsername);
            assertEquals(2, spends.size());

            return null;
        });
    }

    @DisplayName("Поиск трат по username и описанию")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findSpendsByUsernameAndDescriptionTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            String uniquePart = RandomDataUtils.randomUUID().substring(0, 8);
            String description = "Purchase of " + uniquePart + " items";

            CategoryEntity savedCategory = repository.createCategory(createTestCategory(testUsername, testCategoryName));
            SpendEntity spend = createTestSpend(repository, testUsername, savedCategory, description);

            List<SpendEntity> found = repository.findSpendsByUsernameAndDescription(testUsername, uniquePart);
            assertEquals(1, found.size());
            assertEquals(spend.getId(), found.get(0).getId());

            List<SpendEntity> notFound = repository.findSpendsByUsernameAndDescription(testUsername, "NonExistingText");
            assertTrue(notFound.isEmpty());

            return null;
        });
    }

    @DisplayName("Поиск трат по категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findSpendsByCategoryTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = repository.createCategory(createTestCategory(testUsername, testCategoryName));
            SpendEntity spend = createTestSpend(repository, testUsername, savedCategory, testSpendDescription);

            List<SpendEntity> found = repository.findSpendsByCategory(savedCategory.getName(), testUsername);
            assertEquals(1, found.size());
            assertEquals(spend.getId(), found.get(0).getId());

            List<SpendEntity> notFound = repository.findSpendsByCategory("WrongCategory", testUsername);
            assertTrue(notFound.isEmpty());

            return null;
        });
    }

    @DisplayName("Обновление траты")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void updateSpendTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = repository.createCategory(createTestCategory(testUsername, testCategoryName));
            SpendEntity savedSpend = createTestSpend(repository, testUsername, savedCategory, testSpendDescription);

            String updatedDescription = "Updated: " + testSpendDescription;
            savedSpend.setDescription(updatedDescription);
            savedSpend.setAmount(250.0);
            SpendEntity updatedSpend = repository.updateSpend(savedSpend);

            assertEquals(updatedDescription, updatedSpend.getDescription());
            assertEquals(250.0, updatedSpend.getAmount());

            Optional<SpendEntity> found = repository.findSpendById(savedSpend.getId());
            assertTrue(found.isPresent());
            assertEquals(updatedDescription, found.get().getDescription());

            return null;
        });
    }

    @DisplayName("Удаление траты")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void removeSpendTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = repository.createCategory(createTestCategory(testUsername, testCategoryName));
            SpendEntity savedSpend = createTestSpend(repository, testUsername, savedCategory, testSpendDescription);

            repository.removeSpend(savedSpend);
            Optional<SpendEntity> deletedSpend = repository.findSpendById(savedSpend.getId());
            assertFalse(deletedSpend.isPresent());

            return null;
        });
    }

    @DisplayName("Получение всех трат")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findAllSpendsTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            String otherUsername = RandomDataUtils.randomUsername();
            String otherCategoryName = RandomDataUtils.randomeCategoryName() + "_" + RandomDataUtils.randomUUID().substring(0, 8);

            CategoryEntity category1 = repository.createCategory(createTestCategory(testUsername, testCategoryName));
            CategoryEntity category2 = repository.createCategory(createTestCategory(otherUsername, otherCategoryName));

            createTestSpend(repository, testUsername, category1, "First user spend");
            createTestSpend(repository, otherUsername, category2, "Second user spend");

            List<SpendEntity> allSpends = repository.findAllSpends();
            assertFalse(allSpends.isEmpty());
            assertTrue(allSpends.size() >= 2);

            long testUserSpends = allSpends.stream()
                    .filter(spend -> testUsername.equals(spend.getUsername()))
                    .count();
            long otherUserSpends = allSpends.stream()
                    .filter(spend -> otherUsername.equals(spend.getUsername()))
                    .count();

            assertTrue(testUserSpends >= 1);
            assertTrue(otherUserSpends >= 1);

            return null;
        });
    }

    @DisplayName("Полный CRUD сценарий для категории в одной транзакции")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void fullCrudCategoryScenarioInSingleTransactionTest(String repoName, SpendAndCategoryRepository repository) {
        String localTestUsername = RandomDataUtils.randomUsername();
        String localTestCategoryName = RandomDataUtils.randomeCategoryName() + "_" + RandomDataUtils.randomUUID().substring(0, 8);
        XaTransactionTemplate localXaTxTemplate = new XaTransactionTemplate(CFG.spendJdbcUrl());

        localXaTxTemplate.execute(() -> {
            // 1. CREATE
            CategoryEntity category = createTestCategory(localTestUsername, localTestCategoryName);
            CategoryEntity created = repository.createCategory(category);
            assertNonnull(created.getId());

            // 2. READ (поиск по ID)
            Optional<CategoryEntity> foundById = repository.findCategoryById(created.getId());
            assertTrue(foundById.isPresent());

            // 3. READ (поиск по username и name)
            Optional<CategoryEntity> foundByUsernameAndName = repository.findCategoryByUsernameAndName(localTestUsername, localTestCategoryName);
            assertTrue(foundByUsernameAndName.isPresent());

            // 4. READ (поиск по username)
            List<CategoryEntity> foundByUsername = repository.findCategoriesByUsername(localTestUsername);
            assertEquals(1, foundByUsername.size());

            // 5. UPDATE
            CategoryEntity toUpdate = foundByUsernameAndName.get();
            String updatedName = localTestCategoryName + "_updated";
            toUpdate.setName(updatedName);
            toUpdate.setArchived(true);

            CategoryEntity updated = repository.updateCategory(toUpdate);
            assertEquals(updatedName, updated.getName());
            assertTrue(updated.isArchived());

            // 6. DELETE
            repository.removeCategory(updated);

            // 7. Проверка DELETE
            Optional<CategoryEntity> afterDelete = repository.findCategoryByUsernameAndName(localTestUsername, updatedName);
            assertFalse(afterDelete.isPresent());

            return null;
        });
    }

    @DisplayName("Комплексный сценарий: создание категории и траты")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void complexScenarioCategoryAndSpendTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            // Создаем категорию
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity createdCategory = repository.createCategory(category);
            assertNonnull(createdCategory.getId());

            // Создаем трату с этой категорией
            SpendEntity spend = createTestSpend(repository, testUsername, createdCategory, "Test spend with category");

            // Ищем трату по категории
            List<SpendEntity> spendsByCategory = repository.findSpendsByCategory(testCategoryName, testUsername);
            assertEquals(1, spendsByCategory.size());
            assertEquals(spend.getId(), spendsByCategory.get(0).getId());

            // Обновляем трату
            spend.setAmount(200.0);
            SpendEntity updatedSpend = repository.updateSpend(spend);
            assertEquals(200.0, updatedSpend.getAmount());

            // Удаляем трату
            repository.removeSpend(updatedSpend);
            Optional<SpendEntity> deletedSpend = repository.findSpendById(updatedSpend.getId());
            assertFalse(deletedSpend.isPresent());

            // Удаляем категорию
            repository.removeCategory(createdCategory);
            Optional<CategoryEntity> deletedCategory = repository.findCategoryById(createdCategory.getId());
            assertFalse(deletedCategory.isPresent());

            return null;
        });
    }


    @DisplayName("Создание траты с новой категорией")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createSpendWithNewCategoryTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            // Создаем Spend с категорией без ID
            CategoryEntity category = repository.createCategory(createTestCategory(testUsername, testCategoryName));
            SpendEntity spend = new SpendEntity();
            spend.setUsername(testUsername);
            spend.setCurrency(CurrencyValues.USD);
            spend.setSpendDate(new Date());
            spend.setAmount(100.0);
            spend.setDescription("Spend with new category");
            spend.setCategory(category);

            // Это должно создать категорию автоматически (если логика в репозитории это поддерживает)
            SpendEntity createdSpend = repository.createSpend(spend);

            assertNonnull(createdSpend.getId());
            assertNonnull(createdSpend.getCategory());
            assertNonnull(createdSpend.getCategory().getId());
            assertEquals(testCategoryName, createdSpend.getCategory().getName());
            assertEquals(testUsername, createdSpend.getCategory().getUsername());

            return null;
        });
    }

    @DisplayName("Категория не должна быть null при создании траты")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void spendCategoryNonnullTest(String repoName, SpendAndCategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity savedCategory = repository.createCategory(createTestCategory(testUsername, testCategoryName));

            SpendEntity spend = new SpendEntity();
            spend.setUsername(testUsername);
            spend.setCurrency(CurrencyValues.USD);
            spend.setSpendDate(new Date());
            spend.setAmount(100.0);
            spend.setDescription("Spend without category");
            spend.setCategory(savedCategory); // категория с ID

            SpendEntity createdSpend = repository.createSpend(spend);
            assertNonnull(createdSpend.getCategory());
            assertNonnull(createdSpend.getCategory().getId());

            return null;
        });
    }

    private CategoryEntity createTestCategory(String username, String categoryName) {
        CategoryEntity category = new CategoryEntity();
        category.setName(categoryName);
        category.setUsername(username);
        category.setArchived(false);
        return category;
    }

    private SpendEntity createTestSpend(SpendAndCategoryRepository repository, String username, CategoryEntity category, String description) {
        SpendEntity spend = new SpendEntity();
        spend.setUsername(username);
        spend.setCurrency(CurrencyValues.USD);
        spend.setSpendDate(new Date());
        spend.setAmount(100.0);
        spend.setDescription(description);
        spend.setCategory(category);
        return repository.createSpend(spend);
    }
}