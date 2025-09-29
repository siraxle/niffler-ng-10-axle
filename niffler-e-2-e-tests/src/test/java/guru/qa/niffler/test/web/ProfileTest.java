package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.GenerateCategory;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.CategoryExtension;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;

@ExtendWith({BrowserExtension.class, CategoryExtension.class})
public class ProfileTest {
    private static final Config CFG = Config.getInstance();

    @GenerateCategory(username = "cat", category = "", archived = true)
    @Test
    void archivedCategoryShouldPresentInCategoriesList(CategoryJson category) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("cat", "123456")
                .goToProfile()
                .toggleShowArchived() // включаем показ архивных
                .getCategories()
                .findBy(text(category.name()))
                .shouldBe(visible);
    }

    @GenerateCategory(username = "dog", category = "", archived = false)
    @Test
    void activeCategoryShouldPresentInCategoriesList(CategoryJson category) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("dog", "123456")
                .goToProfile()
                .getCategories()
                .findBy(text(category.name()))
                .shouldBe(visible);
    }
}