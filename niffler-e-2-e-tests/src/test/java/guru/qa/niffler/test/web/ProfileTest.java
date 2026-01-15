package guru.qa.niffler.test.web;

import com.codeborne.selenide.SelenideDriver;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.ScreenShotTest;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.impl.db.SpendDbClient;
import guru.qa.niffler.utils.RandomDataUtils;
import guru.qa.niffler.utils.ScreenDiffResult;
import guru.qa.niffler.utils.SelenideUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith({BrowserExtension.class})
public class ProfileTest {
    private static final Config CFG = Config.getInstance();
    private final SelenideDriver driver = new SelenideDriver(SelenideUtils.chromeConfig);

    @User(
            username = "dog",
            categories = @Category(archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesList(UserJson user) {
        String categoryName = user.testData().categories().getFirst().name();
        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .toggleShowArchived()
                .verifyCategoryVisible(categoryName);
    }

    @User(
            categories = @Category(archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesListAndDb(UserJson user) {
        String categoryName = user.testData().categories().getFirst().name();
        String username = user.username();

        SpendClient spendClient = new SpendDbClient();
        Optional<guru.qa.niffler.model.CategoryJson> categoryInDb = spendClient.findCategoryByNameAndUsername(
                categoryName,
                username
        );

        if (categoryInDb.isEmpty()) {
            throw new AssertionError("Категория '" + categoryName + "' не найдена в БД для пользователя " + username);
        } else {
            System.out.println("Категория найдена в БД " + categoryName);
            driver.open(CFG.frontUrl(), LoginPage.class)
                    .login(username, user.testData().password())
                    .getHeader()
                    .toProfilePage()
                    .toggleShowArchived()
                    .verifyCategoryVisible(categoryName);
        }
    }

    @User(
            categories = @Category(archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesList3(UserJson user) {
        String categoryName = user.testData().categories().getFirst().name();

        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .toggleShowArchived()
                .verifyCategoryVisible(categoryName);
    }

    @User(
            username = "cat",
            categories = @Category()
    )
    @Test
    void activeCategoryShouldPresentInCategoriesList(UserJson user) {
        String categoryName = user.testData().categories().getFirst().name();

        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .verifyCategoryVisible(categoryName);
    }

    @User(
            categories = @Category(name = "test_category", archived = true)
    )
    @Test
    void activeCategoryShouldPresentInCategoriesList2(UserJson user) {
        String categoryName = user.testData().categories().getFirst().name();

        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .toggleShowArchived()
                .verifyCategoryVisible(categoryName);
    }

    @User(
            categories = @Category(name = "Обучение")
    )
    @Test
    void activeCategoryShouldBeVisibleWithoutToggle(UserJson user) {
        String categoryName = user.testData().categories().getFirst().name();

        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .verifyCategoryVisible(categoryName);
    }

    @User()
    @Test
    void usernameShouldBeEditedInProfile(UserJson user) {
        final String username = RandomDataUtils.randomUsername();

        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .setName(username)
                .saveChanges()
                .checkSnackbarText("Profile successfully updated");
    }

    @User()
    @Test
    @ScreenShotTest("img/expected-avatar.png")
    void checkAvatarComponentTest(UserJson user, BufferedImage expected) throws IOException {
        BufferedImage actual = driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .getAvatarImage();

        assertFalse(new ScreenDiffResult(expected, actual));
    }

}