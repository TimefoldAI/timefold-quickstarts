package org.acme.callcenter.solver.retry;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(RetryTestExtension.class)
@TestTemplate
public @interface Retry {
    /**
     * Specify the max attempts to rerun the method in case of failures.
     */
    int value() default 0;

    /**
     * Specify the max attempts to rerun the method in case of failures.
     */
    int maxAttempts() default 0;
}
