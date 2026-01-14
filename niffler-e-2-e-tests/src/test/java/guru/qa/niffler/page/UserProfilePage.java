package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

@ParametersAreNonnullByDefault
public class UserProfilePage extends BasePage<UserProfilePage> {

    // Поля профиля
    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement nameInput = $("#name");
    private final SelenideElement saveChangesButton = $("button[type='submit']"); // Более стабильный локатор
    private final SelenideElement closeButton = $x("//h2[contains(text(), 'Archive category')]/..//button[text() = 'Close']"); // Более стабильный локатор
    private final SelenideElement archiveButton = $x("//h2[contains(text(), 'Archive category')]/..//button[text() = 'Archive']"); // Более стабильный локатор

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
    private final SelenideElement avatarImage = $x("(//div[contains(@class, 'MuiAvatar-root')])[2]");

    @Step("Установить имя: {name}")
    @Nonnull
    public UserProfilePage setName(String name) {
        nameInput.setValue(name);
        return this;
    }

    @Step("Сохранить изменения в профиле")
    @Nonnull
    public UserProfilePage saveChanges() {
        saveChangesButton.click();
        return this;
    }

    @Step("Получить имя пользователя")
    @Nonnull
    public String getUsername() {
        return Objects.requireNonNull(usernameInput.getValue());
    }

    @Step("Получить имя")
    @Nonnull
    public String getName() {
        return Objects.requireNonNull(nameInput.getValue());
    }

    @Step("Переключить отображение архивных категорий")
    @Nonnull
    public UserProfilePage toggleShowArchived() {
        showArchivedSwitch.click();
        return this;
    }

    @Step("Добавить категорию: {category}")
    @Nonnull
    public UserProfilePage addCategory(String category) {
        categoryInput.setValue(category).pressEnter();
        return this;
    }

    @Step("Проверить, что категория видна: {categoryName}")
    @Nonnull
    public UserProfilePage verifyCategoryVisible(String categoryName) {
        categoryChips.findBy(text(categoryName)).shouldBe(visible);
        return this;
    }

    @Step("Редактировать категорию: {categoryName} -> {newName}")
    @Nonnull
    public UserProfilePage editCategory(String categoryName, String newName) {
        int index = getCategoryIndex(categoryName);
        if (index >= 0) {
            editCategoryButtons.get(index).click();
            // Здесь нужно реализовать логику редактирования в диалоге
        }
        return this;
    }

    @Step("Архивировать категорию: {categoryName}")
    @Nonnull
    public UserProfilePage archiveCategory(String categoryName) {
        Selenide.sleep(2000);
        int index = getCategoryIndex(categoryName);
        if (index >= 0) {
            archiveCategoryButtons.get(index).click();
        }
        Selenide.sleep(2000);
        clickArchiveInArchiveCategoryAlert();
        return this;
    }

    @Step("Загрузить аватар: {filePath}")
    @Nonnull
    public UserProfilePage uploadAvatar(String filePath) {
        avatarInput.uploadFromClasspath(filePath);
        return this;
    }

    @Step("Нажать кнопку загрузки изображения")
    @Nonnull
    public UserProfilePage clickUploadPictureButton() {
        uploadPictureButton.click();
        return this;
    }

    @Step("Зарегистрировать Passkey")
    @Nonnull
    public UserProfilePage registerPasskey() {
        registerPasskeyButton.click();
        return this;
    }

    @Step("Проверить, что профиль содержит текст: {expectedTexts}")
    @Nonnull
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
    @Nonnull
    public UserProfilePage verifyCategoriesCount(int expectedCount) {
        categoryChips.shouldHave(com.codeborne.selenide.CollectionCondition.size(expectedCount));
        return this;
    }

    @Step("Проверить, что кнопка сохранения активна")
    @Nonnull
    public UserProfilePage verifySaveButtonEnabled() {
        saveChangesButton.shouldBe(com.codeborne.selenide.Condition.enabled);
        return this;
    }

    @Step("Проверить, что кнопка сохранения неактивна")
    @Nonnull
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
    @Nonnull
    public UserProfilePage selectCategory(String categoryName) {
        categoryChips.findBy(text(categoryName)).click();
        return this;
    }

    @Step("Проверить имя: ожидается {expectedName}")
    @Nonnull
    public UserProfilePage verifyName(String expectedName) {
        nameInput.shouldHave(value(expectedName));
        return this;
    }

    @Step("Проверить поле Name")
    @Nonnull
    public UserProfilePage checkName(String name) {
        nameInput.shouldHave(value(name));
        return this;
    }

    @Step("Проверить, что категория {category} существует")
    @Nonnull
    public UserProfilePage checkCategoryExists(String category) {
        categoryCommon.find(text(category)).shouldBe(visible);
        return this;
    }

    @Step("Проверить, что изменение юзернейма недоступно")
    @Nonnull
    public UserProfilePage checkUsernameDisabled() {
        usernameInput.shouldBe(disabled);
        return this;
    }

    @Step("Закрыть Archive category окно")
    @Nonnull
    public UserProfilePage clickCloseInArchiveCategoryAlert() {
        closeButton.click();
        return this;
    }

    @Step("Подтвердить архивацию в Archive category окне")
    @Nonnull
    public UserProfilePage clickArchiveInArchiveCategoryAlert() {
        archiveButton.click();
        return this;
    }

    @Step("Получить скриншот аватара")
    @Nonnull
    public BufferedImage getAvatarImage() throws IOException {
        return ImageIO.read(avatarImage.screenshot());
    }

}