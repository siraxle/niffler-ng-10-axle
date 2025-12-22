package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.*;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$x;

public class Header {

    private final SelenideElement menuButton = $x("//button[@aria-label='Menu']");
    private final SelenideElement profileButton = $x("//a[@href='/profile']");
    private final SelenideElement friendsButton = $x("//a[@href='/people/friends']");
    private final SelenideElement allPeopleButton = $x("//a[@href='/people/all']");
    private final SelenideElement signOutButton = $x("//button[contains(text(), 'Sign out')]");
    private final SelenideElement addSpendingButton = $x("//a[@href='/spending']");
    private final SelenideElement logoButton = $x("//a[@href='/main']");

    @Step("Открыть меню в хедере")
    public Header openMenu() {
        menuButton.click();
        return this;
    }

    @Step("Перейти на страницу друзей")
    public FriendsPage toFriendsPage() {
        openMenu();
        friendsButton.click();
        return new FriendsPage();
    }

    @Step("Перейти на страницу всех пользователей")
    public AllPeoplePage toAllPeoplesPage() {
        openMenu();
        allPeopleButton.click();
        return new AllPeoplePage();
    }

    @Step("Перейти на страницу профиля пользователя")
    public UserProfilePage toProfilePage() {
        openMenu();
        profileButton.click();
        return new UserProfilePage();
    }

    @Step("Выполнить выход из системы")
    public LoginPage signOut() {
        openMenu();
        signOutButton.click();
        return new LoginPage();
    }

    @Step("Перейти на страницу добавления траты")
    public EditSpendingPage toAddSpendingPage() {
        addSpendingButton.click();
        return new EditSpendingPage();
    }

    @Step("Перейти на главную страницу")
    public MainPage toMainPage() {
        logoButton.click();
        return new MainPage();
    }
}