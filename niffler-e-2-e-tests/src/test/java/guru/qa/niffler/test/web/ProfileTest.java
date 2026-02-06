package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.ScreenShotTest;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.UserProfilePage;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.impl.db.SpendDbClient;
import guru.qa.niffler.utils.RandomDataUtils;
import guru.qa.niffler.utils.ScreenDiffResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith({BrowserExtension.class})
public class ProfileTest {
    private static final Config CFG = Config.getInstance();

    @User(categories = @Category)
    @ApiLogin
    @Test
    void categoryShouldPresentInCategoriesList(UserJson user) {
        String categoryName = user.testData().categories().getFirst().name();
        Selenide.open(UserProfilePage.URL, UserProfilePage.class)
                .toggleShowArchived()
                .verifyCategoryVisible(categoryName);
    }

    @User(categories = @Category(archived = true))
    @ApiLogin
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
            Selenide.open(UserProfilePage.URL, UserProfilePage.class)
                    .toggleShowArchived()
                    .verifyCategoryVisible(categoryName);
        }
    }


    @User(categories = @Category(name = "test_category", archived = true))
    @ApiLogin
    @Test
    void activeCategoryShouldPresentInCategoriesList2(UserJson user) {
        String categoryName = user.testData().categories().getFirst().name();

        Selenide.open(UserProfilePage.URL, UserProfilePage.class)
                .toggleShowArchived()
                .verifyCategoryVisible(categoryName);
    }

    @User(categories = @Category(name = "Обучение"))
    @ApiLogin
    @Test
    void activeCategoryShouldBeVisibleWithoutToggle(UserJson user) {
        String categoryName = user.testData().categories().getFirst().name();

        Selenide.open(UserProfilePage.URL, UserProfilePage.class)
                .verifyCategoryVisible(categoryName);
    }

    @User
    @ApiLogin
    @Test
    void usernameShouldBeEditedInProfile(UserJson user) {
        final String username = RandomDataUtils.randomUsername();

        Selenide.open(UserProfilePage.URL, UserProfilePage.class)
                .setName(username)
                .saveChanges()
                .checkSnackbarText("Profile successfully updated");
    }

    @User
    @ApiLogin
    @Test
    @ScreenShotTest("img/expected-avatar.png")
    void checkAvatarComponentTest(UserJson user, BufferedImage expected) throws IOException {
        BufferedImage actual = Selenide.open(UserProfilePage.URL, UserProfilePage.class)
                .getAvatarImage();

        assertFalse(new ScreenDiffResult(expected, actual));
    }

}