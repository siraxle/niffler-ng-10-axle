package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.*;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Selenide.$x;

@ParametersAreNonnullByDefault
public class Header extends BaseComponent<Header> {

    private final SelenideElement menuButton;
    private final SelenideElement profileButton;
    private final SelenideElement friendsButton;
    private final SelenideElement allPeopleButton;
    private final SelenideElement signOutButton;
    private final SelenideElement addSpendingButton;
    private final SelenideElement logoButton;

    public Header() {
        super($x("//header"));
        this.menuButton = self.$x(".//button[@aria-label='Menu']");
        this.profileButton = self.$x(".//a[@href='/profile']");
        this.friendsButton = self.$x(".//a[@href='/people/friends']");
        this.allPeopleButton = self.$x(".//a[@href='/people/all']");
        this.signOutButton = self.$x(".//button[contains(text(), 'Sign out')]");
        this.addSpendingButton = self.$x(".//a[@href='/spending']");
        this.logoButton = self.$x(".//a[@href='/main']");
    }

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