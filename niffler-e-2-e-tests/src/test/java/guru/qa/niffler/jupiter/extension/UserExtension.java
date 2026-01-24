package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.TestData;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.impl.api.UsersApiClient;
import guru.qa.niffler.service.impl.db.UsersDbClient;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static guru.qa.niffler.jupiter.extension.TestMethodContextExtension.context;

public class UserExtension implements BeforeEachCallback, ParameterResolver {
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);
    public static final String DEFAULT_PASSWORD = "123456";

//        private final UsersClient usersClient = new UsersDbClient();
        private final UsersClient usersClient = new UsersApiClient();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(userAnnotation -> {
            final String username = "".equals(userAnnotation.username())
                    ? RandomDataUtils.randomUsername()
                    : userAnnotation.username();

            Optional<UserJson> existingUser = usersClient.findUserByUsername(username);
            UserJson user;
            List<UserJson> incomeInvitations = new ArrayList<>();
            List<UserJson> outcomeInvitations = new ArrayList<>();
            List<UserJson> friends = new ArrayList<>();
            List<CategoryJson> categories = new ArrayList<>();
            List<SpendJson> spendings = new ArrayList<>();

            if (existingUser.isPresent()) {
                user = existingUser.get();
                if (userAnnotation.incomeInvitations() > 0) {
                    incomeInvitations = usersClient.addIncomeInvitation(user, userAnnotation.incomeInvitations());
                }
                if (userAnnotation.outcomeInvitations() > 0) {
                    outcomeInvitations = usersClient.addOutcomeInvitation(user, userAnnotation.outcomeInvitations());
                }
                if (userAnnotation.friends() > 0) {
                    friends = usersClient.createFriends(user, userAnnotation.friends());
                }
            } else {
                user = usersClient.createUser(username, DEFAULT_PASSWORD);
                incomeInvitations = usersClient.addIncomeInvitation(user, userAnnotation.incomeInvitations());
                outcomeInvitations = usersClient.addOutcomeInvitation(user, userAnnotation.outcomeInvitations());
                friends = usersClient.createFriends(user, userAnnotation.friends());
            }

            final TestData testData = new TestData(
                    DEFAULT_PASSWORD,
                    incomeInvitations,
                    outcomeInvitations,
                    friends,
                    categories, // категории
                    spendings  // спендинги
            );
            setUser(user, testData);
        });
    }

    static void setUser(UserJson user, TestData testData) {
        final ExtensionContext methodContext = context();
        methodContext.getStore(NAMESPACE).put(
                methodContext.getUniqueId(),
                user.addTestData(testData)
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(UserJson.class);
    }

    @Override
    public UserJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return getUserJson().orElseThrow();
    }

    public static Optional<UserJson> getUserJson() {
        final ExtensionContext methodContext = context();
        return Optional.ofNullable(methodContext.getStore(NAMESPACE)
                .get(methodContext.getUniqueId(), UserJson.class));
    }
}
