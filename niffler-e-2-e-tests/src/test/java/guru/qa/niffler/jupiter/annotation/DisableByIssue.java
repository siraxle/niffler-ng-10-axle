package guru.qa.niffler.jupiter.annotation;

import guru.qa.niffler.jupiter.extension.IssueExtension_;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ExtendWith(IssueExtension_.class)
public @interface DisableByIssue {
    String value();
}