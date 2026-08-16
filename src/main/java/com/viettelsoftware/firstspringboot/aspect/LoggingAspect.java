package com.viettelsoftware.firstspringboot.aspect;

import lombok.NonNull;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    @Pointcut("within(com.viettelsoftware.firstspringboot..controller..*)" +
            " || within(com.viettelsoftware.firstspringboot..service..*)" +
            " || within(com.viettelsoftware.firstspringboot..repository..*)")
    private void relevantBeansPointcut() {}

    @Before(value = "publicMethodsFromLoggingPackage()")
    public void logBefore(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        logger.debug(">> {}() - {}", methodName, Arrays.toString(args));
    }

    private final @NonNull Logger logger = LoggerFactory.getLogger(this.getClass()); // TODO Log into db, perf drop when log increase -> index, partition
    // tech: apache poi, jxls, impl e.g. export route, cron job to sync keycloak users to current table ->
    // create user, fwd keycloak to create user
}
