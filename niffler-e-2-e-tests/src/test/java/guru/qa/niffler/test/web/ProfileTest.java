package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.CategoryExtension;
import guru.qa.niffler.jupiter.extension.TestMethodContextExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.UserProfilePage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({BrowserExtension.class, CategoryExtension.class, UsersQueueExtension.class})
public class ProfileTest {
    private static final Config CFG = Config.getInstance();

    @User(
            username = "cat",
            categories = @Category(name = "", archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesList(CategoryJson category) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("cat", "123456")
                .goToProfile()
                .toggleShowArchived() // включаем показ архивных
                .verifyCategoryVisible(category.name());
    }

    @User(
            username = "cat",
            categories = @Category(name = "", archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesList2(CategoryJson[] categories) {
        CategoryJson category = categories[0]; // берем первую категорию
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("cat", "123456")
                .goToProfile()
                .toggleShowArchived()
                .verifyCategoryVisible(category.name());
    }

    @User(
            categories = @Category(archived = true)
    )
    @Test
    void archivedCategoryShouldPresentInCategoriesList1(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToProfile()
                .toggleShowArchived();
        Selenide.open(CFG.frontUrl() + "profile", UserProfilePage.class)
                .verifyCategoryVisible(user.testData().categories().getFirst().name());
    }

    @User(
            username = "dog",
            categories = @Category(name = "", archived = false)
    )
    @Test
    void activeCategoryShouldPresentInCategoriesList(CategoryJson category) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("dog", "123456")
                .goToProfile()
                .verifyCategoryVisible(category.name());
    }

}