package com.reservas.ms_reservas.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RepositoryLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("com.reservas.ms_reservas.repository");

    @Around("execution(* org.springframework.data.repository.Repository+.*(..)) && bean(*Repository)")
    public Object logQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }
        String operation = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        long start = System.nanoTime();
        log.debug("Query {} args={}", operation, describe(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            log.debug("Query {} finished in {} ms", operation, (System.nanoTime() - start) / 1_000_000);
            return result;
        } catch (Throwable e) {
            log.debug("Query {} failed: {}", operation, e.getMessage());
            throw e;
        }
    }

    private static String describe(Object[] args) {
        return Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName() + ":" + summarize(arg))
                .toList()
                .toString();
    }

    private static String summarize(Object arg) {
        String text = String.valueOf(arg);
        return text.length() > 80 ? text.substring(0, 80) + "..." : text;
    }
}
