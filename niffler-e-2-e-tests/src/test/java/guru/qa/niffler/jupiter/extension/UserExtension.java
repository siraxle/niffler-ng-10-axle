package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.TestData;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.SpendApiClient;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.UsersDbClient;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.jupiter.extension.TestMethodContextExtension.context;

public class UserExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);
    public static final String DEFAULT_PASSWORD = "123456";

    private final UsersClient usersClient = new UsersDbClient();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(userAnnotation -> {
            if ("".equals(userAnnotation.username())) {
                final String username = RandomDataUtils.randomUsername();
                final UserJson user = usersClient.createUser(username, DEFAULT_PASSWORD);
                final List<UserJson> incomeInvitations = usersClient.addIncomeInvitation(user, userAnnotation.incomeInvitations());
                final List<UserJson> outcomeInvitations = usersClient.addOutcomeInvitation(user, userAnnotation.outcomeInvitations());
                final List<UserJson> friends = usersClient.createFriends(user, userAnnotation.friends());

                final TestData testData = new TestData(DEFAULT_PASSWORD,
                        incomeInvitations,
                        outcomeInvitations,
                        friends,
                        new ArrayList<>(),
                        new ArrayList<>());

                context.getStore(NAMESPACE).put(
                        context.getUniqueId(),
                        user.addTestData(testData)
                );
            }
        });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(UserJson.class);
    }

    @Override
    public UserJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return createdUser().orElseThrow();
    }

    public static Optional<UserJson> createdUser() {
        final ExtensionContext methodContext = TestMethodContextExtension.context();
        return Optional.ofNullable(methodContext.getStore(NAMESPACE)
                .get(methodContext.getUniqueId(), UserJson.class));
    }
}
