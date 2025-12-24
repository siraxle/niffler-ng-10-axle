package guru.qa.niffler.page.component;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.*;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Selenide.$x;

@ParametersAreNonnullByDefault
public class Header extends BaseComponent<Header> {

    private final SelenideElement menuButton = $x("//button[@aria-label='Menu']");
    private final SelenideElement profileButton = $x("//a[@href='/profile']");
    private final SelenideElement friendsButton = $x("//a[@href='/people/friends']");
    private final SelenideElement allPeopleButton = $x("//a[@href='/people/all']");
    private final SelenideElement signOutButton = $x("//button[contains(text(), 'Sign out')]");
    private final SelenideElement addSpendingButton = $x("//a[@href='/spending']");
    private final SelenideElement logoButton = $x("//a[@href='/main']");

    @Step("Открыть меню в хедере")
    @Nonnull
    public Header openMenu() {
        menuButton.click();
        return this;
    }

    @Step("Перейти на страницу друзей")
    @Nonnull
    public FriendsPage toFriendsPage() {
        openMenu();
        Selenide.sleep(300);
        friendsButton.should(Condition.visible);
        friendsButton.click();
        return new FriendsPage();
    }

    @Step("Перейти на страницу всех пользователей")
    @Nonnull
    public AllPeoplePage toAllPeoplesPage() {
        openMenu();
        allPeopleButton.click();
        return new AllPeoplePage();
    }

    @Step("Перейти на страницу профиля пользователя")
    @Nonnull
    public UserProfilePage toProfilePage() {
        openMenu();
        profileButton.click();
        return new UserProfilePage();
    }

    @Step("Выполнить выход из системы")
    @Nonnull
    public LoginPage signOut() {
        openMenu();
        signOutButton.click();
        return new LoginPage();
    }

    @Step("Перейти на страницу добавления траты")
    @Nonnull
    public EditSpendingPage toAddSpendingPage() {
        addSpendingButton.click();
        return new EditSpendingPage();
    }

    @Step("Перейти на главную страницу")
    @Nonnull
    public MainPage toMainPage() {
        logoButton.click();
        return new MainPage();
    }
}