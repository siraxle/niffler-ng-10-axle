package guru.qa.niffler.jupiter.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Расширение JUnit 5 для управления контекстом тестовых методов.
 *
 * <p>Обеспечивает доступ к текущему {@link ExtensionContext} через ThreadLocal,
 * что позволяет получать контекст выполнения теста из любого места в коде.</p>
 *
 * <p><b>Основное назначение:</b></p>
 * <ul>
 *   <li>Предоставление доступа к контексту теста из других extensions</li>
 *   <li>Передача данных между различными расширениями</li>
 *   <li>Получение информации о текущем выполняемом тесте</li>
 * </ul>
 *
 * <p><b>Пример использования:</b></p>
 * <pre>{@code
 * ExtensionContext context = TestMethodContextExtension.context();
 * }</pre>
 *
 * @author Author QAGURU
 * @version 1.0
 * @see ExtensionContext
 * @see BeforeEachCallback
 * @since 2024
 */
public class TestMethodContextExtension implements BeforeEachCallback, AfterEachCallback {

   private static final ThreadLocal<ExtensionContext> ctxStore = new ThreadLocal<>();

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        ctxStore.set(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        ctxStore.remove();
    }

    public static ExtensionContext context() {
        return ctxStore.get();
    }

}
