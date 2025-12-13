package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.CategoryExtension;
import guru.qa.niffler.jupiter.extension.UserExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.impl.db.SpendDbClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

@ExtendWith({BrowserExtension.class, UserExtension.class, CategoryExtension.class, UsersQueueExtension.class})
public class ProfileTest {
    private static final Config CFG = Config.getInstance();

    @User(
            username = "cat",
            categories = @Category(archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesList(UserJson user, CategoryJson[] categories) {
        CategoryJson category = categories[0];
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .toggleShowArchived() // включаем показ архивных
                .verifyCategoryVisible(category.name());
    }

    @User(
            categories = @Category(archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesListAndDb(UserJson user, CategoryJson[] categories) {
        CategoryJson category = categories[0];

        SpendClient spendClient = new SpendDbClient();
        Optional<CategoryJson> categoryInDb = spendClient.findCategoryByNameAndUsername(
                category.name(),
                category.username()
        );
        if (categoryInDb.isEmpty()) {
            throw new AssertionError("Категория '" + category.name() + "' не найдена в БД для пользователя " + category.username());
        } else {
            System.out.println("Категория найдена в БД " + category.name());
            Selenide.open(CFG.frontUrl(), LoginPage.class)
                    .login(user.username(), user.testData().password())
                    .goToProfile()
                    .toggleShowArchived()
                    .verifyCategoryVisible(category.name());
        }
    }

    @User(
            categories = @Category(archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesList3(UserJson user, CategoryJson[] categories) {
        CategoryJson category = categories[0];

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .toggleShowArchived()
                .verifyCategoryVisible(category.name());
        ;
    }

    @User(
            username = "cat",
            categories = @Category()
    )
    @Test
    void activeCategoryShouldPresentInCategoriesList(UserJson user, CategoryJson[] categories) {
        CategoryJson category = categories[0];

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .verifyCategoryVisible(category.name());
    }

    @User(
            categories = @Category(name = "test_category", archived = true)
    )
    @Test
    void activeCategoryShouldPresentInCategoriesList2(UserJson user, CategoryJson[] categories) {
        CategoryJson category = categories[0];

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .toggleShowArchived()
                .verifyCategoryVisible(category.name());
    }

    @User(
            categories = @Category(name = "Обучение")
    )
    @Test
    void activeCategoryShouldBeVisibleWithoutToggle(UserJson user, CategoryJson[] categories) {
        CategoryJson category = categories[0];

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .verifyCategoryVisible(category.name());
    }

}