package guru.qa.niffler.service;

import guru.qa.niffler.data.CategoryEntity;
import guru.qa.niffler.data.repository.CategoryRepository;
import guru.qa.niffler.ex.InvalidCategoryNameException;
import guru.qa.niffler.ex.TooManyCategoriesException;
import guru.qa.niffler.model.CategoryJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceAdditionalTest {

    private final String USERNAME = "testuser";

    @Test
    void getAllCategoriesShouldFilterArchivedWhenExcludeArchivedTrue(@Mock CategoryRepository categoryRepository) {
        List<CategoryEntity> entities = List.of(
                createCategoryEntity("Active 1", false),
                createCategoryEntity("Archived 1", true),
                createCategoryEntity("Active 2", false),
                createCategoryEntity("Archived 2", true)
        );

        when(categoryRepository.findAllByUsernameOrderByName(eq(USERNAME))).thenReturn(entities);

        CategoryService categoryService = new CategoryService(categoryRepository);

        List<CategoryJson> result = categoryService.getAllCategories(USERNAME, true);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> !c.archived()));
        assertTrue(result.stream().map(CategoryJson::name).allMatch(name -> name.startsWith("Active")));
    }

    @Test
    void getAllCategoriesShouldReturnAllCategoriesWhenExcludeArchivedFalse(@Mock CategoryRepository categoryRepository) {
        List<CategoryEntity> entities = List.of(
                createCategoryEntity("Active 1", false),
                createCategoryEntity("Archived 1", true),
                createCategoryEntity("Active 2", false),
                createCategoryEntity("Archived 2", true)
        );

        when(categoryRepository.findAllByUsernameOrderByName(eq(USERNAME))).thenReturn(entities);

        CategoryService categoryService = new CategoryService(categoryRepository);

        List<CategoryJson> result = categoryService.getAllCategories(USERNAME, false);

        assertEquals(4, result.size());
        verify(categoryRepository, times(1)).findAllByUsernameOrderByName(USERNAME);
    }

    @Test
    void updateShouldThrowExceptionWhenUnarchivingAndExceedsLimit(@Mock CategoryRepository categoryRepository) {
        UUID categoryId = UUID.randomUUID();
        CategoryEntity existingEntity = createCategoryEntity("Shops", false);
        existingEntity.setId(categoryId);
        existingEntity.setArchived(true);

        CategoryJson updateJson = new CategoryJson(
                categoryId,
                "Shops",
                USERNAME,
                false
        );

        when(categoryRepository.findByUsernameAndId(eq(USERNAME), eq(categoryId)))
                .thenReturn(Optional.of(existingEntity));
        when(categoryRepository.countByUsernameAndArchived(eq(USERNAME), eq(false)))
                .thenReturn(8L);

        CategoryService categoryService = new CategoryService(categoryRepository);

        TooManyCategoriesException ex = assertThrows(
                TooManyCategoriesException.class,
                () -> categoryService.update(updateJson)
        );

        assertEquals("Can`t unarchive category for user: '" + USERNAME + "'", ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateShouldAllowUnarchivingWhenUnderLimit(@Mock CategoryRepository categoryRepository) {
        UUID categoryId = UUID.randomUUID();
        CategoryEntity existingEntity = createCategoryEntity("Shops", false);
        existingEntity.setId(categoryId);
        existingEntity.setArchived(true);

        CategoryJson updateJson = new CategoryJson(
                categoryId,
                "Shops",
                USERNAME,
                false
        );

        when(categoryRepository.findByUsernameAndId(eq(USERNAME), eq(categoryId)))
                .thenReturn(Optional.of(existingEntity));
        when(categoryRepository.countByUsernameAndArchived(eq(USERNAME), eq(false)))
                .thenReturn(6L); // Under limit
        when(categoryRepository.save(any(CategoryEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        CategoryService categoryService = new CategoryService(categoryRepository);

        CategoryJson result = categoryService.update(updateJson);

        assertFalse(result.archived());
        verify(categoryRepository, times(1)).save(any());
    }

    @Test
    void updateShouldAllowArchiveWithoutLimitCheck(@Mock CategoryRepository categoryRepository) {
        UUID categoryId = UUID.randomUUID();
        CategoryEntity existingEntity = createCategoryEntity("Shops", false);
        existingEntity.setId(categoryId);
        existingEntity.setArchived(false);

        CategoryJson updateJson = new CategoryJson(
                categoryId,
                "Shops",
                USERNAME,
                true
        );

        when(categoryRepository.findByUsernameAndId(eq(USERNAME), eq(categoryId)))
                .thenReturn(Optional.of(existingEntity));
        when(categoryRepository.save(any(CategoryEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        CategoryService categoryService = new CategoryService(categoryRepository);

        CategoryJson result = categoryService.update(updateJson);

        assertTrue(result.archived());
        verify(categoryRepository, never()).countByUsernameAndArchived(any(), anyBoolean());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Archived", "ARCHIVED", "ArchIved", " archived "})
    void saveShouldDenyArchivedCategoryName(String invalidName, @Mock CategoryRepository categoryRepository) {
        CategoryJson categoryJson = new CategoryJson(
                null,
                invalidName,
                USERNAME,
                false
        );

        CategoryService categoryService = new CategoryService(categoryRepository);

        InvalidCategoryNameException ex = assertThrows(
                InvalidCategoryNameException.class,
                () -> categoryService.save(categoryJson)
        );

        assertEquals("Can`t add category with name: '" + invalidName + "'", ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void saveShouldThrowExceptionWhenExceedsLimit(@Mock CategoryRepository categoryRepository) {
        CategoryJson categoryJson = new CategoryJson(
                null,
                "New Category",
                USERNAME,
                false
        );

        when(categoryRepository.countByUsernameAndArchived(eq(USERNAME), eq(false)))
                .thenReturn(9L);

        CategoryService categoryService = new CategoryService(categoryRepository);

        TooManyCategoriesException ex = assertThrows(
                TooManyCategoriesException.class,
                () -> categoryService.save(categoryJson)
        );

        assertEquals("Can`t add over than 8 categories for user: '" + USERNAME + "'", ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void saveShouldCreateCategoryWhenUnderLimit(@Mock CategoryRepository categoryRepository) {
        String categoryName = "New Category";
        CategoryJson categoryJson = new CategoryJson(
                null,
                categoryName,
                USERNAME,
                false
        );

        when(categoryRepository.countByUsernameAndArchived(eq(USERNAME), eq(false)))
                .thenReturn(5L);

        CategoryEntity savedEntity = new CategoryEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setName(categoryName);
        savedEntity.setUsername(USERNAME);
        savedEntity.setArchived(false);

        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(savedEntity);

        CategoryService categoryService = new CategoryService(categoryRepository);

        CategoryEntity result = categoryService.save(categoryJson);

        assertNotNull(result.getId());
        assertEquals(categoryName, result.getName());
        assertEquals(USERNAME, result.getUsername());
        assertFalse(result.isArchived());

        ArgumentCaptor<CategoryEntity> captor = ArgumentCaptor.forClass(CategoryEntity.class);
        verify(categoryRepository).save(captor.capture());

        CategoryEntity entityToSave = captor.getValue();
        assertNull(entityToSave.getId());
        assertEquals(categoryName, entityToSave.getName());
        assertEquals(USERNAME, entityToSave.getUsername());
        assertFalse(entityToSave.isArchived());
    }

    @Test
    void getOrSaveShouldUseExistingCategoryWhenFound(@Mock CategoryRepository categoryRepository) {
        String categoryName = "Food";
        CategoryJson categoryJson = new CategoryJson(
                null,
                categoryName,
                USERNAME,
                false
        );

        CategoryEntity existingEntity = createCategoryEntity(categoryName, false);
        existingEntity.setId(UUID.randomUUID());

        when(categoryRepository.findByUsernameAndName(eq(USERNAME), eq(categoryName)))
                .thenReturn(Optional.of(existingEntity));

        CategoryService categoryService = new CategoryService(categoryRepository);

        CategoryEntity result = categoryService.getOrSave(categoryJson);

        assertEquals(existingEntity, result);
        verify(categoryRepository, never()).countByUsernameAndArchived(any(), anyBoolean());
        verify(categoryRepository, never()).save(any());
    }

    private CategoryEntity createCategoryEntity(String name, boolean archived) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(name);
        entity.setUsername(USERNAME);
        entity.setArchived(archived);
        return entity;
    }
}