package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.DisableByIssue;
import guru.qa.niffler.service.GhAPIClient;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.SearchOption;

public class IssueExtension implements ExecutionCondition {
    private final GhAPIClient ghApiClient = new GhAPIClient();

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return AnnotationSupport.findAnnotation( //ищем аннотацию над методом
                        context.getRequiredTestMethod(),
                        DisableByIssue.class)
                .or(() -> AnnotationSupport.findAnnotation(//ищем аннотацию над классом
                        context.getRequiredTestClass(),
                        DisableByIssue.class,
                        SearchOption.INCLUDE_ENCLOSING_CLASSES))
                // если нашли, то проверяем статус issue
                .map(
                        byIssue -> "open".equals(ghApiClient.issueState(byIssue.value()))
                                ? ConditionEvaluationResult.disabled("Disabled by Issue: " + byIssue.value())
                                : ConditionEvaluationResult.enabled("Issue closed"))
                // если не нашли, то возвращаем enabled по умолчанию
                .orElseGet(
                        () -> ConditionEvaluationResult.enabled("Annotation @DisabledByIssue not found")
                );
    }
}
