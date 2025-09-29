package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.*;

public class UserProfilePage {
    private final SelenideElement registerPasskeyButton = $("#\\:r7\\:");
    private final SelenideElement uploadNewPictureButton = $x("//label[@class='image__input-label']");
    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement nameInput = $("#name");
    private final SelenideElement showArchivedSwitch = $x("//input[@type='checkbox']");
    private final SelenideElement categoryInput = $("#category");
    private final ElementsCollection categories = $$x("//h2[text()='Categories']/../following-sibling::div[contains(@class, 'MuiGrid-item')]");
    private final ElementsCollection editCategoriesButtons = $$x("//button[@aria-label='Edit category']");
    private final ElementsCollection archiveCategoriesButtons = $$x("//button[@aria-label='Archive category']");


    public UserProfilePage setUsername(String username) {
        usernameInput.setValue(username);
        return this;
    }

    public UserProfilePage setName(String name) {
        nameInput.setValue(name);
        return this;
    }

    public UserProfilePage toggleShowArchived() {
        showArchivedSwitch.click();
        return this;
    }

    public UserProfilePage addCategory(String category) {
        categoryInput.setValue(category).pressEnter();
        return this;
    }

    public ElementsCollection getCategories() {
        return categories;
    }

    public UserProfilePage editCategory(int index, String newName) {
        editCategoriesButtons.get(index).click();
        // нужен дополнительный метод для ввода нового имени
        return this;
    }

    public UserProfilePage archiveCategory(int index) {
        archiveCategoriesButtons.get(index).click();
        return this;
    }

}
