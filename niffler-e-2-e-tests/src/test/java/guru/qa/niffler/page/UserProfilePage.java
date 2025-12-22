package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class UserProfilePage {

    // Поля профиля
    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement nameInput = $("#name");
    private final SelenideElement saveChangesButton = $("button[type='submit']"); // Более стабильный локатор

    // Аватар
    private final SelenideElement avatarInput = $("#image__input");
    private final SelenideElement uploadPictureButton = $(".image__input-label");
    private final SelenideElement registerPasskeyButton = $("#\\:rn\\:");

    // Категории
    private final SelenideElement categoryInput = $("input[name='category']");
    private final SelenideElement showArchivedSwitch = $("input[type='checkbox']");
    private final ElementsCollection categoryChips = $$(".MuiChip-root");
    private final ElementsCollection editCategoryButtons = $$("button[aria-label='Edit category']");
    private final ElementsCollection archiveCategoryButtons = $$("button[aria-label='Archive category']");
    private final ElementsCollection categoryCommon = $$(".MuiChip-filled.MuiChip-colorPrimary");

    @Step("Установить имя: {name}")
    public UserProfilePage setName(String name) {
        nameInput.setValue(name);
        return this;
    }

    @Step("Сохранить изменения в профиле")
    public UserProfilePage saveChanges() {
        saveChangesButton.click();
        return this;
    }

    @Step("Получить имя пользователя")
    public String getUsername() {
        return usernameInput.getValue();
    }

    @Step("Получить имя")
    public String getName() {
        return nameInput.getValue();
    }

    @Step("Переключить отображение архивных категорий")
    public UserProfilePage toggleShowArchived() {
        showArchivedSwitch.click();
        return this;
    }

    @Step("Добавить категорию: {category}")
    public UserProfilePage addCategory(String category) {
        categoryInput.setValue(category).pressEnter();
        return this;
    }

    @Step("Проверить, что категория видна: {categoryName}")
    public UserProfilePage verifyCategoryVisible(String categoryName) {
        categoryChips.findBy(text(categoryName)).shouldBe(visible);
        return this;
    }

    @Step("Редактировать категорию: {categoryName} -> {newName}")
    public UserProfilePage editCategory(String categoryName, String newName) {
        int index = getCategoryIndex(categoryName);
        if (index >= 0) {
            editCategoryButtons.get(index).click();
            // Здесь нужно реализовать логику редактирования в диалоге
        }
        return this;
    }

    @Step("Архивировать категорию: {categoryName}")
    public UserProfilePage archiveCategory(String categoryName) {
        int index = getCategoryIndex(categoryName);
        if (index >= 0) {
            archiveCategoryButtons.get(index).click();
        }
        return this;
    }

    @Step("Загрузить аватар: {filePath}")
    public UserProfilePage uploadAvatar(String filePath) {
        avatarInput.uploadFromClasspath(filePath);
        return this;
    }

    @Step("Нажать кнопку загрузки изображения")
    public UserProfilePage clickUploadPictureButton() {
        uploadPictureButton.click();
        return this;
    }

    @Step("Зарегистрировать Passkey")
    public UserProfilePage registerPasskey() {
        registerPasskeyButton.click();
        return this;
    }

    @Step("Проверить, что профиль содержит текст: {expectedTexts}")
    public UserProfilePage verifyProfileContainsText(String... expectedTexts) {
        for (String text : expectedTexts) {
            $("body").shouldHave(text(text));
        }
        return this;
    }

    @Step("Проверить наличие категории: {categoryName}")
    public boolean isCategoryPresent(String categoryName) {
        return categoryChips.findBy(text(categoryName)).exists();
    }

    @Step("Получить количество категорий")
    public int getCategoriesCount() {
        return categoryChips.size();
    }

    @Step("Проверить количество категорий: ожидается {expectedCount}")
    public UserProfilePage verifyCategoriesCount(int expectedCount) {
        categoryChips.shouldHave(com.codeborne.selenide.CollectionCondition.size(expectedCount));
        return this;
    }

    @Step("Проверить, что кнопка сохранения активна")
    public UserProfilePage verifySaveButtonEnabled() {
        saveChangesButton.shouldBe(com.codeborne.selenide.Condition.enabled);
        return this;
    }

    @Step("Проверить, что кнопка сохранения неактивна")
    public UserProfilePage verifySaveButtonDisabled() {
        saveChangesButton.shouldBe(com.codeborne.selenide.Condition.disabled);
        return this;
    }

    private int getCategoryIndex(String categoryName) {
        for (int i = 0; i < categoryChips.size(); i++) {
            if (categoryChips.get(i).getText().equals(categoryName)) {
                return i;
            }
        }
        return -1;
    }

    @Step("Выбрать категорию: {categoryName}")
    public UserProfilePage selectCategory(String categoryName) {
        categoryChips.findBy(text(categoryName)).click();
        return this;
    }

    @Step("Проверить имя: ожидается {expectedName}")
    public UserProfilePage verifyName(String expectedName) {
        nameInput.shouldHave(value(expectedName));
        return this;
    }

    @Step("Проверить поле Name")
    public UserProfilePage checkName(String name) {
        nameInput.shouldHave(value(name));
        return this;
    }

    @Step("Проверить, что категория {category} существует")
    public @Nonnull UserProfilePage checkCategoryExists(String category) {
        categoryCommon.find(text(category)).shouldBe(visible);
        return this;
    }

    @Step("Проверить, что изменение юзернейма недоступно")
    public @Nonnull UserProfilePage checkUsernameDisabled() {
        usernameInput.shouldBe(disabled);
        return this;
    }
}