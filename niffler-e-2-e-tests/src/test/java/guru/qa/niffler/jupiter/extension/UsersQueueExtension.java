package guru.qa.niffler.jupiter.extension;

import io.qameta.allure.Allure;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

//управляет очередью тестовых пользователей для предотвращения параллельных тестов с одними и теми же учетными записями.

public class UsersQueueExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UsersQueueExtension.class);

    public record StaticUser(String username, String password, String friend, String income, String outcome) {
    }

    private static final Queue<StaticUser> EMPTY_USERS = new ConcurrentLinkedQueue<>();
    private static final Queue<StaticUser> WITH_FRIEND_USERS = new ConcurrentLinkedQueue<>();
    private static final Queue<StaticUser> WITH_INCOME_REQUEST_USERS = new ConcurrentLinkedQueue<>();
    private static final Queue<StaticUser> WITH_OUTCOME_REQUEST_USERS = new ConcurrentLinkedQueue<>();

    static {
        EMPTY_USERS.add(new StaticUser("empty_user", "123456", null, null, null));
        WITH_FRIEND_USERS.add(new StaticUser("alice", "123456", "bob", null, null));
        WITH_INCOME_REQUEST_USERS.add(new StaticUser("diana", "123456", null, "charlie", null));
        WITH_OUTCOME_REQUEST_USERS.add(new StaticUser("charlie", "123456", null, null, "diana"));
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UserType {
        Type value() default Type.EMPTY;

        enum Type {
            EMPTY, WITH_FRIEND, WITH_INCOME_REQUEST, WITH_OUTCOME_REQUEST
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Map<UserType, StaticUser> usersMap = new HashMap<>();
        Arrays.stream(context.getRequiredTestMethod().getParameters())
                .filter(parameter -> AnnotationSupport.isAnnotated(parameter, UserType.class))
                .forEach(parameter -> {
                    UserType userType = parameter.getAnnotation(UserType.class);
                    Optional<StaticUser> user = Optional.empty();
                    StopWatch sw = StopWatch.createStarted();
                    // ОЖИДАНИЕ СВОБОДНОГО ПОЛЬЗОВАТЕЛЯ:
                    while (user.isEmpty() && sw.getTime(TimeUnit.SECONDS) < 30) {
                        Queue<StaticUser> queue = getQueueByUserType(userType);
                        user = Optional.ofNullable(queue.poll());
                    }
                    // ЕСЛИ ПОЛЬЗОВАТЕЛЬ НАЙДЕН - ДОБАВЛЯЕМ В MAP
                    user.ifPresentOrElse(
                            foundUser -> {
                                usersMap.put(userType, foundUser);
                            },
                            () -> {
                                throw new IllegalStateException("Can't find user after 30 sec");
                            }
                    );
                });
        Allure.getLifecycle().updateTestCase(testCase -> {
            testCase.setStart(new Date().getTime());
        });
        // СОХРАНЯЕМ MAP В КОНТЕКСТ ТЕСТА:
        context.getStore(NAMESPACE).put(context.getUniqueId(), usersMap);
    }

    private Queue<StaticUser> getQueueByUserType(UserType userType) {
        return switch (userType.value()) {
            case EMPTY -> EMPTY_USERS;
            case WITH_FRIEND -> WITH_FRIEND_USERS;
            case WITH_INCOME_REQUEST -> WITH_INCOME_REQUEST_USERS;
            case WITH_OUTCOME_REQUEST -> WITH_OUTCOME_REQUEST_USERS;
        };
    }


    //тут кладем user обратно в очередь
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Map<UserType, StaticUser> usersMap = context.getStore(NAMESPACE)
                .get(context.getUniqueId(), Map.class);
        if (usersMap != null) {
            for (Map.Entry<UserType, StaticUser> entry : usersMap.entrySet()) {
                Queue<StaticUser> queue = getQueueByUserType(entry.getKey());
                queue.add(entry.getValue());
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
            ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(StaticUser.class)
                && AnnotationSupport.isAnnotated(parameterContext.getParameter(), UserType.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
            ParameterResolutionException {
        // 1. Достаем Map всех пользователей этого теста
        Map<UserType, StaticUser> usersMap = extensionContext.getStore(NAMESPACE)
                .get(extensionContext.getUniqueId(), Map.class);
        // 2. Получаем аннотацию @UserType текущего параметра
        UserType userTypeAnnotation = parameterContext.getParameter().getAnnotation(UserType.class);
        // 3. Находим пользователя в Map по аннотации
        return usersMap.get(userTypeAnnotation);
    }
}
