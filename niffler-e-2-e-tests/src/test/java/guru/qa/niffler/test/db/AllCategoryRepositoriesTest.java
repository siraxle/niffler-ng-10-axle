package guru.qa.niffler.test.db;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.repository.CategoryRepository;
import guru.qa.niffler.data.repository.impl.CategoryRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.CategoryRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.CategoryRepositorySpringJdbc;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Тестирование всех реализаций CategoryRepository")
public class AllCategoryRepositoriesTest {

    private static final Config CFG = Config.getInstance();

    static Stream<Arguments> repositories() {
        return Stream.of(
                arguments("CategoryRepositoryJdbc", new CategoryRepositoryJdbc()),
                arguments("CategoryRepositorySpringJdbc", new CategoryRepositorySpringJdbc()),
                arguments("CategoryRepositoryHibernate", new CategoryRepositoryHibernate())
        );
    }

    private XaTransactionTemplate xaTxTemplate;
    private String testUsername;
    private String testCategoryName;

    @BeforeEach
    void setUp() {
        xaTxTemplate = new XaTransactionTemplate(CFG.spendJdbcUrl());
        testUsername = RandomDataUtils.randomUsername();
        testCategoryName = RandomDataUtils.randomeCategoryName() + "_" + RandomDataUtils.randomUUID().substring(0, 8);
    }

    @AfterEach
    void tearDown() {
        // Очистка данных для всех репозиториев
        repositories().forEach(arg -> {
            CategoryRepository repo = (CategoryRepository) arg.get()[1];
            // Создаем отдельную транзакцию для очистки
            XaTransactionTemplate cleanupTx = new XaTransactionTemplate(CFG.spendJdbcUrl());
            cleanupTx.execute(() -> {
                List<CategoryEntity> allCategories = repo.findAll();
                for (CategoryEntity category : allCategories) {
                    if (testUsername.equals(category.getUsername())) {
                        repo.remove(category);
                    }
                }
                return null;
            });
        });
    }

    @DisplayName("Создание категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createCategoryTest(String repoName, CategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity created = repository.create(category);

            assertNotNull(created.getId());
            assertEquals(testCategoryName, created.getName());
            assertEquals(testUsername, created.getUsername());
            assertFalse(created.isArchived());

            return null;
        });
    }

    @DisplayName("Поиск категории по ID")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByIdTest(String repoName, CategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity created = repository.create(category);

            Optional<CategoryEntity> found = repository.findById(created.getId());
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
    void findByUsernameAndNameTest(String repoName, CategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            repository.create(category);

            Optional<CategoryEntity> found = repository.findByUsernameAndName(testUsername, testCategoryName);
            assertTrue(found.isPresent());
            assertEquals(testCategoryName, found.get().getName());
            assertEquals(testUsername, found.get().getUsername());

            return null;
        });
    }

    @DisplayName("Поиск категорий по username")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findByUsernameTest(String repoName, CategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category1 = createTestCategory(testUsername, testCategoryName + "_1");
            CategoryEntity category2 = createTestCategory(testUsername, testCategoryName + "_2");

            repository.create(category1);
            repository.create(category2);

            List<CategoryEntity> found = repository.findByUsername(testUsername);
            assertEquals(2, found.size());

            boolean hasCategory1 = found.stream()
                    .anyMatch(c -> (testCategoryName + "_1").equals(c.getName()));
            boolean hasCategory2 = found.stream()
                    .anyMatch(c -> (testCategoryName + "_2").equals(c.getName()));

            assertTrue(hasCategory1);
            assertTrue(hasCategory2);

            return null;
        });
    }

    @DisplayName("Обновление категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void updateCategoryTest(String repoName, CategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity created = repository.create(category);

            String updatedName = testCategoryName + "_updated";
            created.setName(updatedName);
            created.setArchived(true);

            CategoryEntity updated = repository.update(created);
            assertEquals(updatedName, updated.getName());
            assertTrue(updated.isArchived());
            assertEquals(testUsername, updated.getUsername());

            return null;
        });
    }

    @DisplayName("Удаление категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void removeCategoryTest(String repoName, CategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity created = repository.create(category);

            repository.remove(created);

            Optional<CategoryEntity> found = repository.findByUsernameAndName(testUsername, testCategoryName);
            assertFalse(found.isPresent());

            return null;
        });
    }

    @DisplayName("Получение всех категорий")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void findAllTest(String repoName, CategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            String otherUsername = RandomDataUtils.randomUsername();
            String otherCategoryName = RandomDataUtils.randomeCategoryName() + "_" + RandomDataUtils.randomUUID().substring(0, 8);

            CategoryEntity category1 = createTestCategory(testUsername, testCategoryName);
            CategoryEntity category2 = createTestCategory(otherUsername, otherCategoryName);

            repository.create(category1);
            repository.create(category2);

            List<CategoryEntity> allCategories = repository.findAll();
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

    @DisplayName("Полный CRUD сценарий в одной транзакции")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void fullCrudScenarioInSingleTransactionTest(String repoName, CategoryRepository repository) {
        String localTestUsername = RandomDataUtils.randomUsername();
        String localTestCategoryName = RandomDataUtils.randomeCategoryName() + "_" + RandomDataUtils.randomUUID().substring(0, 8);
        XaTransactionTemplate localXaTxTemplate = new XaTransactionTemplate(CFG.spendJdbcUrl());

        localXaTxTemplate.execute(() -> {
            // 1. CREATE
            CategoryEntity category = createTestCategory(localTestUsername, localTestCategoryName);
            CategoryEntity created = repository.create(category);
            assertNotNull(created.getId());
            assertEquals(localTestCategoryName, created.getName());
            assertEquals(localTestUsername, created.getUsername());

            // 2. READ (поиск по ID)
            Optional<CategoryEntity> foundById = repository.findById(created.getId());
            assertTrue(foundById.isPresent());
            assertEquals(created.getId(), foundById.get().getId());

            // 3. READ (поиск по username и name)
            Optional<CategoryEntity> foundByUsernameAndName = repository.findByUsernameAndName(localTestUsername, localTestCategoryName);
            assertTrue(foundByUsernameAndName.isPresent());
            assertEquals(localTestCategoryName, foundByUsernameAndName.get().getName());

            // 4. READ (поиск по username)
            List<CategoryEntity> foundByUsername = repository.findByUsername(localTestUsername);
            assertFalse(foundByUsername.isEmpty());
            assertEquals(1, foundByUsername.size());

            // 5. UPDATE
            CategoryEntity toUpdate = foundByUsernameAndName.get();
            String updatedName = localTestCategoryName + "_updated";
            toUpdate.setName(updatedName);
            toUpdate.setArchived(true);

            CategoryEntity updated = repository.update(toUpdate);
            assertEquals(updatedName, updated.getName());
            assertTrue(updated.isArchived());

            // 6. Проверка UPDATE
            Optional<CategoryEntity> afterUpdate = repository.findByUsernameAndName(localTestUsername, updatedName);
            assertTrue(afterUpdate.isPresent());
            assertTrue(afterUpdate.get().isArchived());

            // 7. DELETE
            repository.remove(afterUpdate.get());

            // 8. Проверка DELETE
            Optional<CategoryEntity> afterDelete = repository.findByUsernameAndName(localTestUsername, updatedName);
            assertFalse(afterDelete.isPresent());

            Optional<CategoryEntity> afterDeleteById = repository.findById(created.getId());
            assertFalse(afterDeleteById.isPresent());

            return null;
        });
    }

    @DisplayName("Создание нескольких категорий в одной транзакции")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void createMultipleCategoriesInSingleTransactionTest(String repoName, CategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            String category1Name = testCategoryName + "_1";
            String category2Name = testCategoryName + "_2";

            CategoryEntity category1 = createTestCategory(testUsername, category1Name);
            CategoryEntity category2 = createTestCategory(testUsername, category2Name);

            CategoryEntity created1 = repository.create(category1);
            CategoryEntity created2 = repository.create(category2);

            assertNotNull(created1.getId());
            assertNotNull(created2.getId());
            assertNotEquals(created1.getId(), created2.getId());

            List<CategoryEntity> allCategories = repository.findAll();
            long createdCategoriesCount = allCategories.stream()
                    .filter(c -> category1Name.equals(c.getName()) || category2Name.equals(c.getName()))
                    .count();
            assertTrue(createdCategoriesCount >= 2);

            // Удаляем созданные категории в той же транзакции
            repository.remove(created1);
            repository.remove(created2);

            return null;
        });
    }

    @DisplayName("Сценарий отката транзакции при ошибке")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void transactionRollbackScenarioTest(String repoName, CategoryRepository repository) {
        String rollbackUsername = RandomDataUtils.randomUsername();
        String rollbackCategoryName = RandomDataUtils.randomeCategoryName();
        XaTransactionTemplate rollbackTx = new XaTransactionTemplate(CFG.spendJdbcUrl());

        try {
            rollbackTx.execute(() -> {
                // 1. Создаем категорию
                CategoryEntity category = createTestCategory(rollbackUsername, rollbackCategoryName);
                CategoryEntity created = repository.create(category);
                assertNotNull(created.getId());

                // 2. Имитируем ошибку (например, null name)
                CategoryEntity invalidCategory = new CategoryEntity();
                // Не устанавливаем name - вызовет ошибку при вставке
                invalidCategory.setUsername("someuser");
                invalidCategory.setArchived(false);

                // Эта операция должна вызвать исключение
                repository.create(invalidCategory);

                return null;
            });
        } catch (Exception e) {
            // Ожидаемое поведение - транзакция откатилась

            // Проверяем в отдельной транзакции, что категория не создалась
            XaTransactionTemplate checkTx = new XaTransactionTemplate(CFG.spendJdbcUrl());
            checkTx.execute(() -> {
                Optional<CategoryEntity> notCreated = repository.findByUsernameAndName(rollbackUsername, rollbackCategoryName);
                assertFalse(notCreated.isPresent(),
                        "Категория не должна быть создана после отката транзакции");
                return null;
            });
        }
    }

    @DisplayName("Проверка архивации категории")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void categoryArchivingTest(String repoName, CategoryRepository repository) {
        xaTxTemplate.execute(() -> {
            CategoryEntity category = createTestCategory(testUsername, testCategoryName);
            CategoryEntity created = repository.create(category);
            assertFalse(created.isArchived(), "Категория должна создаваться не архивированной");

            // Архивируем категорию
            created.setArchived(true);
            CategoryEntity archived = repository.update(created);
            assertTrue(archived.isArchived(), "Категория должна быть архивирована после обновления");

            // Проверяем через поиск
            Optional<CategoryEntity> found = repository.findByUsernameAndName(testUsername, testCategoryName);
            assertTrue(found.isPresent());
            assertTrue(found.get().isArchived(), "Найденная категория должна быть архивированной");

            return null;
        });
    }

    @DisplayName("Проверка уникальности категорий для пользователя")
    @ParameterizedTest(name = "{0}")
    @MethodSource("repositories")
    void categoryUniquenessForUserTest(String repoName, CategoryRepository repository) {
        String uniqueCategoryName = "unique_" + RandomDataUtils.randomeCategoryName() + "_" + RandomDataUtils.randomUUID().substring(0, 8);
        String uniqueUsername = RandomDataUtils.randomUsername();

        XaTransactionTemplate tx = new XaTransactionTemplate(CFG.spendJdbcUrl());

        // Проверяем что первая категория создается успешно
        CategoryEntity firstCategory = null;
        try {
            firstCategory = tx.execute(() -> {
                CategoryEntity category = createTestCategory(uniqueUsername, uniqueCategoryName);
                return repository.create(category);
            });
            assertNotNull(firstCategory);
            assertNotNull(firstCategory.getId());
        } catch (Exception e) {
            fail("Первая категория должна создаваться успешно: " + e.getMessage());
        }

        // Пытаемся создать вторую категорию с тем же именем для того же пользователя
        // Для Hibernate это может вызвать проблемы с транзакцией, поэтому используем отдельный подход
        boolean uniquenessEnforced = false;
        Exception uniquenessException = null;

        try {
            XaTransactionTemplate secondTx = new XaTransactionTemplate(CFG.spendJdbcUrl());
            secondTx.execute(() -> {
                CategoryEntity duplicateCategory = createTestCategory(uniqueUsername, uniqueCategoryName);
                repository.create(duplicateCategory);
                return null;
            });
        } catch (Exception e) {
            uniquenessException = e;
            // Проверяем сообщение об ошибке
            String errorMessage = e.toString().toLowerCase();
            if (errorMessage.contains("unique") ||
                    errorMessage.contains("duplicate") ||
                    errorMessage.contains("ix_category_username") ||
                    (e.getCause() != null && e.getCause().toString().toLowerCase().contains("unique"))) {
                uniquenessEnforced = true;
            }
        }

        // Для Hibernate могут быть проблемы с транзакцией после нарушения уникальности
        // Проверяем косвенно - через количество категорий у пользователя
        XaTransactionTemplate checkTx = new XaTransactionTemplate(CFG.spendJdbcUrl());
        List<CategoryEntity> userCategories = checkTx.execute(() ->
                repository.findByUsername(uniqueUsername));

        long categoriesWithSameName = userCategories.stream()
                .filter(c -> uniqueCategoryName.equals(c.getName()))
                .count();

        // Уникальность считается обеспеченной если:
        // 1. Выброшено исключение о нарушении уникальности ИЛИ
        // 2. В БД только одна категория с таким именем
        boolean uniquenessVerified = uniquenessEnforced || categoriesWithSameName == 1;

        assertTrue(uniquenessVerified,
                "Уникальность категорий для пользователя должна обеспечиваться. " +
                        "Исключение: " + (uniquenessException != null ? uniquenessException.getMessage() : "нет") +
                        ", категорий с именем '" + uniqueCategoryName + "': " + categoriesWithSameName);

        // Очистка
        if (firstCategory != null) {
            XaTransactionTemplate cleanupTx = new XaTransactionTemplate(CFG.spendJdbcUrl());
            CategoryEntity finalFirstCategory = firstCategory;
            cleanupTx.execute(() -> {
                repository.remove(finalFirstCategory);
                return null;
            });
        }
    }

    private CategoryEntity createTestCategory(String username, String categoryName) {
        CategoryEntity category = new CategoryEntity();
        category.setName(categoryName);
        category.setUsername(username);
        category.setArchived(false);
        return category;
    }
}