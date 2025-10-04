package guru.qa.niffler.jupiter.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

//1. Быть уверенным, что SuiteExtension будет выполняться перед каждым тестовым классом
//2. Если мы выполним какой-то код перед загрузкой самого первого тестового класса, то это и будет beforeSuite()
//3. При этом, для 2, 3, и т.д. (до N) тестовых классов, больше не будем вызывать beforeSuite()
//4. Когда все тесты завершаться вызовем afterSuite()

public interface SuiteExtension extends BeforeAllCallback {
    @Override
    default void beforeAll(ExtensionContext context) throws Exception {
       final ExtensionContext rootContext = context.getRoot();
       rootContext.getStore(ExtensionContext.Namespace.GLOBAL)
               .getOrComputeIfAbsent(
                       this.getClass(),
                       key -> { //попадаем в самый первый раз
                           beforeSuite(rootContext);
                           return new ExtensionContext.Store.CloseableResource() {
                               @Override
                               public void close() throws Throwable {
                                   afterSuite();
                               }
                           };
                       }
               );
    }

    default void beforeSuite(ExtensionContext context) {
    }

    default void afterSuite() {
    }
}
