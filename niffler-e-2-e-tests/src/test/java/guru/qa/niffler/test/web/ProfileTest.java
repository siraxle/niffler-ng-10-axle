package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.CategoryExtension;
import guru.qa.niffler.jupiter.extension.UserExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.impl.db.SpendDbClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

@ExtendWith({BrowserExtension.class, UserExtension.class, CategoryExtension.class})
public class ProfileTest {
    private static final Config CFG = Config.getInstance();

    @User(
            username = "cat",
            categories = @Category(archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesList(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .toggleShowArchived()
                .verifyCategoryVisible(user.testData().categories().getFirst().name());
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
            Selenide.open(CFG.frontUrl(), LoginPage.class)
                    .login(username, user.testData().password())
                    .goToProfile()
                    .toggleShowArchived()
                    .verifyCategoryVisible(categoryName);
        }
    }

    @User(
            categories = @Category(archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesList3(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .toggleShowArchived()
                .verifyCategoryVisible(user.testData().categories().getFirst().name());
    }

    @User(
            username = "cat",
            categories = @Category()
    )
    @Test
    void activeCategoryShouldPresentInCategoriesList(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .verifyCategoryVisible(user.testData().categories().getFirst().name());
    }

    @User(
            categories = @Category(name = "test_category", archived = true)
    )
    @Test
    void activeCategoryShouldPresentInCategoriesList2(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .toggleShowArchived()
                .verifyCategoryVisible(user.testData().categories().getFirst().name());
    }

    @User(
            categories = @Category(name = "Обучение")
    )
    @Test
    void activeCategoryShouldBeVisibleWithoutToggle(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .verifyCategoryVisible(user.testData().categories().getFirst().name());
    }
}