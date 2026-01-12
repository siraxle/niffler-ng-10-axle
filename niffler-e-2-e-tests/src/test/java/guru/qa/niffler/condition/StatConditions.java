package guru.qa.niffler.condition;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.WebElementCondition;
import com.codeborne.selenide.WebElementsCondition;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.codeborne.selenide.CheckResult.accepted;
import static com.codeborne.selenide.CheckResult.rejected;

public class StatConditions {
    public static WebElementCondition color(Color expectedColor) {
        return new  WebElementCondition("color") {

            @Override
            public CheckResult check(Driver driver, WebElement element) {
                final String rgba = element.getCssValue("background-color");
                return new CheckResult(
                        expectedColor.rgb.equals(rgba),
                        rgba
                );
            }
        };
    }


    public static WebElementsCondition color(Color... expectedColors) {

        return new WebElementsCondition() {
            private final String expectedRgba = Arrays.stream(expectedColors).map(c -> c.rgb).toList().toString();

            @Nonnull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {

                if (ArrayUtils.isEmpty(expectedColors)) {
                    throw new IllegalArgumentException("No expected colors given");
                }
                if (expectedColors.length != elements.size()) {
                    String message = String.format("List size mismatch (expected: %s, actual: %s)", expectedColors.length, elements.size());
                    return rejected(message, elements);
                }

                boolean passed = true;
                List<String> actualRgbaList = new ArrayList<>();

                for (int i = 0; i < elements.size(); i++) {
                    final  WebElement elementToCheck = elements.get(i);
                    final Color colorToCheck = expectedColors[i];
                    final String rgba = elementToCheck.getCssValue("background-color");
                    actualRgbaList.add(rgba);
                    if (passed) {
                        passed = colorToCheck.rgb.equals(rgba);
                    }
                }
                if (!passed) {
                    String actualRgba = actualRgbaList.toString();
                    String message = String.format("List colors mismatch (expected: %s, actual: %s)", expectedRgba, actualRgba);
                    return  rejected(message, actualRgba);
                }
                return accepted();
            }

            @Override
            public String toString() {
                return expectedRgba;
            }
        };
    }

    public static WebElementsCondition statBubbles(Bubble... expectedBubbles) {
        return new WebElementsCondition() {
            private final String expectedDescription = Arrays.toString(expectedBubbles);

            @Nonnull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {
                if (ArrayUtils.isEmpty(expectedBubbles)) {
                    throw new IllegalArgumentException("No expected bubbles given");
                }
                if (expectedBubbles.length != elements.size()) {
                    String message = String.format(
                            "Bubbles count mismatch (expected: %s, actual: %s)",
                            expectedBubbles.length,
                            elements.size()
                    );
                    return rejected(message, elements);
                }

                List<String> actualDescriptions = new ArrayList<>();
                boolean passed = true;

                for (int i = 0; i < elements.size(); i++) {
                    WebElement element = elements.get(i);
                    Bubble expected = expectedBubbles[i];

                    String actualColor = element.getCssValue("background-color");
                    String actualText = element.getText();

                    boolean colorMatches = expected.color().rgb.equals(actualColor);
                    boolean textMatches = expected.text().equals(actualText);

                    actualDescriptions.add("Bubble[color=" + actualColor + ", text=" + actualText + "]");

                    if (!colorMatches || !textMatches) {
                        passed = false;
                    }
                }

                if (!passed) {
                    String message = String.format(
                            "Bubbles mismatch (expected: %s, actual: %s)",
                            expectedDescription,
                            actualDescriptions
                    );
                    return rejected(message, actualDescriptions.toString());
                }
                return accepted();
            }

            @Override
            public String toString() {
                return "bubbles " + expectedDescription;
            }

        };
    }

    public static WebElementsCondition statBubblesInAnyOrder(Bubble... expectedBubbles) {
        return new WebElementsCondition() {
            private final String expectedDescription = Arrays.toString(expectedBubbles);

            @Nonnull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {
                if (ArrayUtils.isEmpty(expectedBubbles)) {
                    throw new IllegalArgumentException("No expected bubbles given");
                }
                if (expectedBubbles.length != elements.size()) {
                    String message = String.format(
                            "Bubbles count mismatch (expected: %s, actual: %s)",
                            expectedBubbles.length,
                            elements.size()
                    );
                    return rejected(message, elements);
                }

                List<Bubble> actualBubbles = new ArrayList<>();
                for (WebElement element : elements) {
                    String actualColor = element.getCssValue("background-color");
                    String actualText = element.getText();
                    actualBubbles.add(new Bubble(
                            Arrays.stream(Color.values())
                                    .filter(c -> c.rgb.equals(actualColor))
                                    .findFirst()
                                    .orElse(null),
                            actualText
                    ));
                }

                List<Bubble> expectedList = new ArrayList<>(Arrays.asList(expectedBubbles));
                List<Bubble> actualList = new ArrayList<>(actualBubbles);

                boolean matched = expectedList.size() == actualList.size()
                        && expectedList.containsAll(actualList);

                if (!matched) {
                    String message = String.format(
                            "Bubbles mismatch (expected in any order: %s, actual: %s)",
                            expectedDescription,
                            actualBubbles
                    );
                    return rejected(message, actualBubbles.toString());
                }
                return accepted();
            }

            @Override
            public String toString() {
                return "bubbles in any order " + expectedDescription;
            }
        };
    }

    public static WebElementsCondition statBubblesContains(Bubble... expectedBubbles) {
        return new WebElementsCondition() {
            private final String expectedDescription = Arrays.toString(expectedBubbles);

            @Nonnull
            @Override
            public CheckResult check(Driver driver, List<WebElement> elements) {
                if (ArrayUtils.isEmpty(expectedBubbles)) {
                    throw new IllegalArgumentException("No expected bubbles given");
                }

                // Собираем фактические пузырьки
                List<Bubble> actualBubbles = new ArrayList<>();
                for (WebElement element : elements) {
                    String actualColor = element.getCssValue("background-color");
                    String actualText = element.getText();
                    // Находим соответствующий Color enum по RGB
                    Color color = Arrays.stream(Color.values())
                            .filter(c -> c.rgb.equals(actualColor))
                            .findFirst()
                            .orElse(null);
                    actualBubbles.add(new Bubble(color, actualText));
                }

                // Проверяем, что все ожидаемые пузырьки есть среди фактических
                List<Bubble> expectedList = Arrays.asList(expectedBubbles);
                List<Bubble> actualList = new ArrayList<>(actualBubbles);

                boolean containsAll = actualList.containsAll(expectedList);

                if (!containsAll) {
                    String message = String.format(
                            "Bubbles not found (expected among others: %s, actual: %s)",
                            expectedDescription,
                            actualBubbles
                    );
                    return rejected(message, actualBubbles.toString());
                }
                return accepted();
            }

            @Override
            public String toString() {
                return "contain bubbles " + expectedDescription;
            }

        };
    }

    public static WebElementsCondition spends(SpendJson... expectedSpends) {
        return new WebElementsCondition() {
            private final String expectedDescription = Arrays.toString(expectedSpends);

            @Nonnull
            @Override
            public CheckResult check(Driver driver, List<WebElement> rows) {
                if (ArrayUtils.isEmpty(expectedSpends)) {
                    throw new IllegalArgumentException("No expected spends given");
                }
                if (expectedSpends.length != rows.size()) {
                    String message = String.format(
                            "Spends count mismatch (expected: %s, actual: %s)",
                            expectedSpends.length,
                            rows.size()
                    );
                    return rejected(message, rows);
                }

                List<String> actualDescriptions = new ArrayList<>();

                for (int i = 0; i < rows.size(); i++) {
                    WebElement row = rows.get(i);
                    SpendJson expectedSpend = expectedSpends[i];

                    List<WebElement> cells = row.findElements(By.cssSelector("td"));
                    if (cells.size() < 5) {
                        return rejected("Row doesn't have enough cells", cells.toString());
                    }

                    // Проверяем категорию (2я ячейка, индекс 1)
                    if (!cells.get(1).getText().equals(expectedSpend.category().name())) {
                        String message = String.format(
                                "Spend category mismatch (expected: %s, actual: %s)",
                                expectedSpend.category().name(), cells.get(1).getText()
                        );
                        return rejected(message, cells.get(1).getText());
                    }

                    // Проверяем amount + currency (3я ячейка, индекс 2)
                    String expectedAmountWithCurrency = formatAmountWithCurrency(expectedSpend);
                    if (!cells.get(2).getText().equals(expectedAmountWithCurrency)) {
                        String message = String.format(
                                "Spend amount mismatch (expected: %s, actual: %s)",
                                expectedAmountWithCurrency, cells.get(2).getText()
                        );
                        return rejected(message, cells.get(2).getText());
                    }

                    // Проверяем описание (4я ячейка, индекс 3)
                    if (!cells.get(3).getText().equals(expectedSpend.description())) {
                        String message = String.format(
                                "Spend description mismatch (expected: %s, actual: %s)",
                                expectedSpend.description(), cells.get(3).getText()
                        );
                        return rejected(message, cells.get(3).getText());
                    }

                    // Проверяем дату (5я ячейка, индекс 4)
                    String expectedDate = formatDate(expectedSpend.spendDate());
                    if (!cells.get(4).getText().equals(expectedDate)) {
                        String message = String.format(
                                "Spend date mismatch (expected: %s, actual: %s)",
                                expectedDate, cells.get(4).getText()
                        );
                        return rejected(message, cells.get(4).getText());
                    }

                    actualDescriptions.add(formatSpendDescription(expectedSpend));
                }

                return accepted();
            }

            private String formatAmountWithCurrency(SpendJson spend) {
                double amount = spend.amount();
                String amountStr = amount == Math.floor(amount) ?
                        String.valueOf((int) amount) :
                        String.valueOf(amount);

                return amountStr + " " + getCurrencySymbol(spend.currency());
            }

            private String getCurrencySymbol(CurrencyValues currency) {
                switch (currency) {
                    case RUB: return "₽";
                    case USD: return "$";
                    case EUR: return "€";
                    case KZT: return "₸";
                    default: return currency.name();
                }
            }

            private String formatDate(Date date) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                return sdf.format(date);
            }

            private String formatSpendDescription(SpendJson spend) {
                return String.format("Spend[category=%s, amount=%s, description=%s, date=%s]",
                        spend.category().name(),
                        formatAmountWithCurrency(spend),
                        spend.description(),
                        formatDate(spend.spendDate())
                );
            }

            @Override
            public String toString() {
                return "spends " + expectedDescription;
            }

        };
    }

}
