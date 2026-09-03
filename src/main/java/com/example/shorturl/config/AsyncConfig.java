package com.example.shorturl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async configuration for the URL shortener application.
 * <p>
 * Provides a dedicated thread pool for click-logging tasks with:
 * <ul>
 *   <li>{@link ThreadPoolExecutor.CallerRunsPolicy} to apply back-pressure
 *       instead of dropping tasks when the queue is full</li>
 *   <li>Graceful shutdown — waits up to 30s for in-flight tasks to complete</li>
 *   <li>MDC context propagation so log correlation IDs survive thread handoff</li>
 *   <li>Global {@link AsyncUncaughtExceptionHandler} for void-returning @Async methods</li>
 * </ul>
 *
 * <h3>Tuning Guidelines</h3>
 * <ul>
 *   <li><b>corePoolSize</b> — set to ~CPU cores for I/O-bound tasks;
 *       increase if tasks spend most time waiting on DB/network</li>
 *   <li><b>maxPoolSize</b> — upper bound for burst traffic;
 *       keep modest to avoid thread thrashing</li>
 *   <li><b>queueCapacity</b> — buffer between core and max;
 *       monitor queue depth in production to right-size</li>
 *   <li><b>keepAliveSeconds</b> — how long idle threads above corePoolSize survive;
 *       60s is a sensible default for bursty workloads</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    // ─── Thread Pool: Click Logging ────────────────────────────────────

    @Bean(name = "clickLoggingExecutor")
    public Executor clickLoggingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // ── Sizing (tune per workload) ──
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);

        // ── Thread identity ──
        executor.setThreadNamePrefix("ClickLogger-");

        // ── Back-pressure: caller's thread runs the task when queue is full,
        //    which naturally throttles the producer instead of throwing RejectedExecutionException ──
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // ── Graceful shutdown: finish in-flight tasks before the JVM exits ──
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // ── Keep-alive: let threads above corePoolSize expire after 60s idle ──
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(false); // core threads stay alive

        // ── MDC propagation: copy logging context (e.g. requestId) into async threads ──
        executor.setTaskDecorator(new MdcTaskDecorator());

        executor.initialize();
        return executor;
    }

    // ─── Global Async Exception Handler ────────────────────────────────

    /**
     * Catches uncaught exceptions from void-returning {@code @Async} methods.
     * Without this, exceptions are silently swallowed by the executor.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) ->
                log.error("Uncaught async exception in {}.{}(): {}",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName(),
                        throwable.getMessage(),
                        throwable);
    }

    // ─── MDC Task Decorator ────────────────────────────────────────────

    /**
     * Copies the calling thread's {@link MDC} context map into the async
     * worker thread, then clears it after execution to prevent leaks.
     * <p>
     * This ensures that log correlation IDs (e.g. requestId, traceId)
     * propagate correctly across thread boundaries.
     */
    private static class MdcTaskDecorator implements TaskDecorator {

        @Override
        public Runnable decorate(Runnable runnable) {
            // Capture MDC from the calling thread
            Map<String, String> contextMap = MDC.getCopyOfContextMap();

            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        }
    }
}