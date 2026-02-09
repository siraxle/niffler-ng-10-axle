package guru.qa.niffler.jupiter.extension;

import com.github.jknack.handlebars.internal.lang3.ArrayUtils;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.impl.api.SpendApiClient;
import guru.qa.niffler.service.impl.db.SpendDbClient;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.jupiter.extension.TestMethodContextExtension.context;
import static guru.qa.niffler.utils.RandomDataUtils.randomeCategoryName;

public class CategoryExtension implements BeforeEachCallback, ParameterResolver, AfterTestExecutionCallback {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CategoryExtension.class);
        private final SpendClient spendClient = new SpendApiClient();
//    private final SpendClient spendClient = new SpendDbClient();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(userAnnotation -> {
            if (ArrayUtils.isNotEmpty(userAnnotation.categories())) {

                Optional<UserJson> testUser = UserExtension.getUserJson();
                final String username = testUser.isPresent() ? testUser.get().username() : userAnnotation.username();

                List<CategoryJson> result = new ArrayList<>();

                for (Category categoryAnno : userAnnotation.categories()) {
                    CategoryJson category = new CategoryJson(
                            null,
                            "".equals(categoryAnno.name()) ? randomeCategoryName() : categoryAnno.name(),
                            username,
                            categoryAnno.archived()
                    );
                    CategoryJson created = spendClient.createCategory(category);
                    if (categoryAnno.archived()) {
                        CategoryJson archivedCategory = new CategoryJson(
                                created.id(),
                                created.name(),
                                created.username(),
                                true
                        );
                        created = spendClient.updateCategory(archivedCategory);
                    }
                    result.add(created);
                }

                if (testUser.isPresent()) {
                    testUser.get().testData().categories().addAll(result);
                } else {
                    context.getStore(NAMESPACE).put(
                            context.getUniqueId(),
                            result.stream().toArray(CategoryJson[]::new)
                    );
                }
            }
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().isAssignableFrom(CategoryJson[].class);
    }

    @Override
    public CategoryJson[] resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return createdCategories();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        CategoryJson[] categories = createdCategories();
        if (categories != null) {
            for (CategoryJson category : categories) {
                if (!category.archived()) {
                    CategoryJson archivedCategory = new CategoryJson(
                            category.id(),
                            category.name(),
                            category.username(),
                            true
                    );
                    spendClient.updateCategory(archivedCategory);
                }
            }
        }
    }

    public static CategoryJson[] createdCategories() {
        final ExtensionContext methodContext = context();
        return methodContext.getStore(NAMESPACE)
                .get(methodContext.getUniqueId(), CategoryJson[].class);
    }

}