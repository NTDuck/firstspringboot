package com.viettelsoftware.firstspringboot.aspect;

import com.viettelsoftware.firstspringboot.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(com.viettelsoftware.firstspringboot.annotation.Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {

        val serviceName = joinPoint.getSignature()
                .getDeclaringType()
                .getSimpleName();

        // Formats as SCREAMING_SNAKE_CASE
        val action = joinPoint.getSignature()
                .getName()
                .toUpperCase()
                .replace(" ", "_");

        try {
            val result = joinPoint.proceed();

            auditService.auditSuccess(serviceName, action);
            return result;

        } catch (Exception exception) {
            auditService.auditFailure(serviceName, action, exception);
            throw exception;
        }
    }
}