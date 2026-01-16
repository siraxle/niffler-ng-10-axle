package guru.qa.niffler.page.component;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.condition.Bubble;
import guru.qa.niffler.condition.Color;
import guru.qa.niffler.condition.StatConditions;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static com.codeborne.selenide.Selenide.$;
import static guru.qa.niffler.condition.StatConditions.color;
import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
public class StatComponent extends BaseComponent<StatComponent> {

    public StatComponent() {
        super($("#stat"));
    }

    private final ElementsCollection bubbles = self.$("#legend-container").$$("li");
    private final SelenideElement chart = $("canvas[role='img']");

    @Step("Check that statistic bubbles contain texts {0}")
    @Nonnull
    public StatComponent checkStatisticBubblesContains(String... texts) {
        bubbles.should(CollectionCondition.texts(texts));
        return this;
    }

    @Step("Get screenshot of stat chart")
    @Nonnull
    public BufferedImage chartScreenshot() throws IOException {
        return ImageIO.read(requireNonNull(chart.screenshot()));
    }

    @Step("Check that stat bubbles contains colors {expectedColors}")
    @Nonnull
    public StatComponent checkBubbles(Color... expectedColors) {
        bubbles.should(color(expectedColors));
        return this;
    }

    @Step("Check that stat bubbles match {expectedBubbles}")
    @Nonnull
    public StatComponent checkBubbles(Bubble... expectedBubbles) {
        bubbles.should(StatConditions.statBubbles(expectedBubbles));
        return this;
    }

    @Step("Check that stat bubbles match {expectedBubbles} in any order")
    @Nonnull
    public StatComponent checkBubblesInAnyOrder(Bubble... expectedBubbles) {
        bubbles.should(StatConditions.statBubblesInAnyOrder(expectedBubbles));
        return this;
    }

    @Step("Check that stat bubbles contain {expectedBubbles}")
    @Nonnull
    public StatComponent checkBubblesContains(Bubble... expectedBubbles) {
        bubbles.should(StatConditions.statBubblesContains(expectedBubbles));
        return this;
    }


}
