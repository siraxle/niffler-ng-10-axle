package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.SpendApiClient;
import guru.qa.niffler.service.SpendDbClient;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Date;

public class SpendingExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SpendingExtension.class);
    private final SpendDbClient spendDbClient = new SpendDbClient();
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(userAnnotation -> {
            if (userAnnotation.spendings().length > 0) {
                Spending anno = userAnnotation.spendings()[0];

                String username = userAnnotation.username().isEmpty()
                        ? RandomDataUtils.randomUsername()
                        : userAnnotation.username();

                String description = anno.description().isEmpty()
                        ? RandomDataUtils.randomeSentence(3)
                        : anno.description();

                final SpendJson created = spendDbClient.createSpend(
                        new SpendJson(
                                null,
                                new Date(),
                                new CategoryJson(
                                        null,
                                        anno.category(),
                                        username,
                                        false
                                ),
                                anno.currency(),
                                anno.amount(),
                                description,
                                username
                        )
                );
                context.getStore(NAMESPACE).put(context.getUniqueId(), created);
            }
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(SpendJson.class);
    }

    @Override
    public SpendJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE)
                .get(extensionContext.getUniqueId(), SpendJson.class);
    }
}
