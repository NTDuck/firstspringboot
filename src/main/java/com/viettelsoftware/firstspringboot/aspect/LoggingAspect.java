package com.viettelsoftware.firstspringboot.aspect;

import lombok.NonNull;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    private final @NonNull Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(com.viettelsoftware.firstspringboot..controller..*)" +
            " || within(com.viettelsoftware.firstspringboot..service..*)" +
            " || within(com.viettelsoftware.firstspringboot..repository..*)")
    public void relevantBeansPointcut() {}

    @Before("relevantBeansPointcut()")
    public void logBefore(@NonNull JoinPoint joinPoint) {
        val timestamp = Instant.now();
        val method = joinPoint.getSignature().toShortString();
        val args = joinPoint.getArgs();
        logger.info("[[BEFORE]] [{}] method: {}, params: {}", timestamp, method, Arrays.toString(args));
    }

    @After("relevantBeansPointcut()")
    public void logAfter(@NonNull JoinPoint joinPoint) {
        val timestamp = Instant.now();
        val method = joinPoint.getSignature().toShortString();
        val args = joinPoint.getArgs();
        logger.info("[[AFTER]] [{}] method: {}, params: {}", timestamp, method, Arrays.toString(args));
    }
}
