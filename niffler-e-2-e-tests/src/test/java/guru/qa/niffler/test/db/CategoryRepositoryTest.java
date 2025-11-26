package guru.qa.niffler.test.db;

import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.repository.CategoryRepository;
import guru.qa.niffler.data.repository.impl.CategoryRepositoryJdbc;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CategoryRepositoryTest {

    private CategoryRepository categoryRepository;
    private String testUsername;

    @BeforeEach
    void setUp() {
        categoryRepository = new CategoryRepositoryJdbc();
        testUsername = RandomDataUtils.randomUsername();
    }

    @Test
    void createCategory() {
        CategoryEntity category = createTestCategory();

        CategoryEntity created = categoryRepository.create(category);

        assertNotNull(created.getId());
        assertEquals(category.getName(), created.getName());
        assertEquals(testUsername, created.getUsername());
    }

    @Test
    void findById() {
        CategoryEntity category = createTestCategory();
        CategoryEntity created = categoryRepository.create(category);

        Optional<CategoryEntity> found = categoryRepository.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
    }

    @Test
    void findByUsernameAndName() {
        String categoryName = RandomDataUtils.randomeCategoryName();
        CategoryEntity category = createTestCategory();
        category.setName(categoryName);
        categoryRepository.create(category);

        Optional<CategoryEntity> found = categoryRepository.findByUsernameAndName(testUsername, categoryName);

        assertTrue(found.isPresent());
        assertEquals(categoryName, found.get().getName());
    }

    @Test
    void findByUsername() {
        CategoryEntity category1 = createTestCategory();
        CategoryEntity category2 = createTestCategory();
        category2.setName(RandomDataUtils.randomeCategoryName());

        categoryRepository.create(category1);
        categoryRepository.create(category2);

        List<CategoryEntity> categories = categoryRepository.findByUsername(testUsername);

        assertEquals(2, categories.size());
    }

    @Test
    void updateCategory() {
        CategoryEntity category = createTestCategory();
        CategoryEntity created = categoryRepository.create(category);

        created.setArchived(true);
        CategoryEntity updated = categoryRepository.update(created);

        assertTrue(updated.isArchived());
    }

    @Test
    void deleteCategory() {
        CategoryEntity category = createTestCategory();
        CategoryEntity created = categoryRepository.create(category);

        categoryRepository.delete(created);

        Optional<CategoryEntity> found = categoryRepository.findById(created.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void findAll() {
        CategoryEntity category1 = createTestCategory();
        CategoryEntity category2 = createTestCategory();
        category2.setName(RandomDataUtils.randomeCategoryName());

        categoryRepository.create(category1);
        categoryRepository.create(category2);

        List<CategoryEntity> allCategories = categoryRepository.findAll();

        assertFalse(allCategories.isEmpty());
    }

    private CategoryEntity createTestCategory() {
        CategoryEntity category = new CategoryEntity();
        category.setName(RandomDataUtils.randomeCategoryName());
        category.setUsername(testUsername);
        category.setArchived(false);
        return category;
    }
}