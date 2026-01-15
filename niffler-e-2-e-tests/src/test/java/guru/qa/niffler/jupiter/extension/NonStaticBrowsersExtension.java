package guru.qa.niffler.jupiter.extension;

import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.extension.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NonStaticBrowsersExtension implements
        BeforeEachCallback, AfterEachCallback,
        TestExecutionExceptionHandler,
        LifecycleMethodExecutionExceptionHandler {

    private final ThreadLocal<List<SelenideDriver>> threadLocalDrivers = ThreadLocal.withInitial(ArrayList::new);
    private final Map<String, List<SelenideDriver>> testDriversMap = new ConcurrentHashMap<>();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        threadLocalDrivers.get().clear(); // Очищаем для текущего потока
        SelenideLogger.addListener("Allure-selenide", new AllureSelenide().savePageSource(false).screenshots(false));

        // Сохраняем ID теста для доступа из других потоков (если нужно)
        String testId = getTestId(context);
        testDriversMap.put(testId, threadLocalDrivers.get());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        List<SelenideDriver> drivers = threadLocalDrivers.get();
        for (SelenideDriver driver : drivers) {
            if (driver.hasWebDriverStarted()) {
                driver.close();
            }
        }
        drivers.clear();

        // Очищаем из мапы
        String testId = getTestId(context);
        testDriversMap.remove(testId);
    }

    public void addDriver(SelenideDriver driver) {
        threadLocalDrivers.get().add(driver);
    }

    public List<SelenideDriver> getDrivers() {
        return threadLocalDrivers.get();
    }

    // Метод для получения драйверов теста по ID (если нужно из другого потока)
    public List<SelenideDriver> getDriversByTestId(String testId) {
        return testDriversMap.get(testId);
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        doScreenshot();
        throw throwable;
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        doScreenshot();
        throw throwable;
    }

    @Override
    public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        throw throwable;
    }

    private void doScreenshot() {
        List<SelenideDriver> drivers = threadLocalDrivers.get();
        for (SelenideDriver driver : drivers) {
            if (driver.hasWebDriverStarted()) {
                Allure.addAttachment(
                        "Screen fail for browser " + driver.getSessionId(),
                        new ByteArrayInputStream(
                                ((TakesScreenshot) driver.getWebDriver()).getScreenshotAs(OutputType.BYTES)
                        )
                );
            }
        }
    }

    private String getTestId(ExtensionContext context) {
        return context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName();
    }
}