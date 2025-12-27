package guru.qa.niffler.page.component;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.*;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@ParametersAreNonnullByDefault
public class Header extends BaseComponent<Header> {

    private final SelenideElement menuButton = $x("//button[@aria-label='Menu']");
    private final SelenideElement profileButton = $("a[href='/profile']");
    private final SelenideElement friendsButton = $("a[href='/people/friends']");
    private final SelenideElement allPeopleButton = $("a[href='/people/all']");
    private final SelenideElement signOutButton = $x("//button[contains(text(), 'Sign out')]");
    private final SelenideElement addSpendingButton = $("a[href='/spending']");
    private final SelenideElement logoButton = $("a[href='/main']");

    public Header(@Nonnull SelenideElement self) {
        super(self);
    }

    public Header() {
        super($x("//header"));
    }

    @Step("Открыть меню в хедере")
    @Nonnull
    public Header openMenu() {
        menuButton.click();
        Selenide.sleep(500);
        return this;
    }

    @Step("Закрыть меню в хедере")
    @Nonnull
    public Header closeMenu() {
        self.click();
        return this;
    }

    @Step("Перейти на страницу друзей")
    @Nonnull
    public FriendsPage toFriendsPage() {
        openMenu();
        friendsButton.shouldBe(Condition.visible).click();
        return new FriendsPage();
    }

    @Step("Перейти на страницу всех пользователей")
    @Nonnull
    public AllPeoplePage toAllPeoplesPage() {
        openMenu();
        allPeopleButton.shouldBe(Condition.visible).click();
        return new AllPeoplePage();
    }

    @Step("Перейти на страницу профиля пользователя")
    @Nonnull
    public UserProfilePage toProfilePage() {
        openMenu();
        profileButton.shouldBe(Condition.visible).click();
        return new UserProfilePage();
    }

    @Step("Выполнить выход из системы")
    @Nonnull
    public LoginPage signOut() {
        openMenu();
        signOutButton.shouldBe(Condition.visible).click();
        return new LoginPage();
    }

    @Step("Перейти на страницу добавления траты")
    @Nonnull
    public EditSpendingPage toAddSpendingPage() {
        addSpendingButton.shouldBe(Condition.visible).click();
        return new EditSpendingPage();
    }

    @Step("Перейти на главную страницу")
    @Nonnull
    public MainPage toMainPage() {
        logoButton.shouldBe(Condition.visible).click();
        return new MainPage();
    }
}