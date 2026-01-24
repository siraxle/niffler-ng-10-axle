package guru.qa.niffler.page;


import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.config.Config;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public abstract class BasePage<T extends  BasePage<?>> {

    protected final SelenideElement snackBar = $(".MuiAlert-message");
    public static final Config CFG = Config.getInstance();

    @SuppressWarnings("unchecked")
    public T checkSnackbarText(String text) {
        snackBar.shouldHave(text(text));
        return  (T) this;
    }


}
