package com.viettelsoftware.firstspringboot.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Test
    void testLogBeforeAndAfter() {
        LoggingAspect aspect = new LoggingAspect();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("TaskService.getTasks()");
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        assertDoesNotThrow(() -> aspect.logBefore(joinPoint));
        assertDoesNotThrow(() -> aspect.logAfter(joinPoint));
    }
}
