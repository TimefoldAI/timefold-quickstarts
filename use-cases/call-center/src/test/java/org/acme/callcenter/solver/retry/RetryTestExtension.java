package org.acme.callcenter.solver.retry;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

public class RetryTestExtension implements TestTemplateInvocationContextProvider, TestExecutionExceptionHandler {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(RetryTestExtension.class);

    public RetryTestExtension() {
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        TestExecutionContext executionContext = getExecutionContext(extensionContext.getRequiredTestMethod(),
                extensionContext);
        if (executionContext.failures == 0) {
            throw new TestAbortedException("Retry execution ignored.");
        }
        // The stream executes one successful run or maxAttempts unsuccessful runs at most
        return StreamSupport.stream(spliteratorUnknownSize(executionContext, ORDERED), false);
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        TestExecutionContext executionContext = getExecutionContext(extensionContext.getRequiredTestMethod(),
                extensionContext.getParent().orElseThrow(() -> new IllegalStateException("The parent context is required.")));
        executionContext.incFailures();
        throw throwable;
    }

    private static TestExecutionContext getExecutionContext(Method method, ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE).getOrComputeIfAbsent(method.getName(), __ -> {
            Retry retry = AnnotationSupport.findAnnotation(method, Retry.class)
                    .orElseThrow(() -> new IllegalStateException("@Retry is missing."));
            return new TestExecutionContext(Math.max(retry.value(), retry.maxAttempts()));
        }, TestExecutionContext.class);
    }

    private static class TestExecutionContext implements Iterator<TestTemplateInvocationContext> {
        private final int maxAttempts;
        private int executions = 0;
        private int failures = 0;

        public TestExecutionContext(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public void incExecutions() {
            executions++;
        }

        public void incFailures() {
            failures++;
        }

        @Override
        public boolean hasNext() {
            boolean hadSuccessful = executions > failures;
            // Stop when there in one successful or the max attempts is exhausted
            return !hadSuccessful && failures < maxAttempts;
        }

        @Override
        public TestTemplateInvocationContext next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            executions++;
            return new TestTemplateInvocationContext() {
            };
        }
    }
}
