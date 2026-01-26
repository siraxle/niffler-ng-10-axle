package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.TestData;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.impl.api.UsersApiClient;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.jupiter.extension.TestMethodContextExtension.context;

public class UserExtension implements BeforeEachCallback, ParameterResolver {
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);
    public static final String DEFAULT_PASSWORD = "123456";

    private final UsersClient usersClient = new UsersApiClient();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnno -> {
                    if ("".equals(userAnno.username())) {
                        final String username = RandomDataUtils.randomUsername();
                        final UserJson user = usersClient.createUser(username, DEFAULT_PASSWORD);
                        final List<UserJson> incomeInvitations = usersClient.addIncomeInvitation(user, userAnno.incomeInvitations());
                        final List<UserJson> outcomeInvitations = usersClient.addOutcomeInvitation(user, userAnno.outcomeInvitations());
                        final List<UserJson> friends = usersClient.createFriends(user, userAnno.friends());

                        final TestData testData = new TestData(
                                DEFAULT_PASSWORD,
                                incomeInvitations,
                                outcomeInvitations,
                                friends,
                                new ArrayList<>(),
                                new ArrayList<>()
                        );

                        setUser(user.addTestData(testData));
                    }
                });
    }

    static void setUser(UserJson user) {
        final ExtensionContext methodContext = context();
        methodContext.getStore(NAMESPACE).put(
                methodContext.getUniqueId(),
                user
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().isAssignableFrom(UserJson.class);
    }

    @Override
    public UserJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return getUserJson().orElseThrow();
    }

    public static Optional<UserJson> getUserJson() {
        final ExtensionContext methodContext = context();
        return Optional.ofNullable(methodContext.getStore(NAMESPACE)
                .get(methodContext.getUniqueId(), UserJson.class));
    }
}