package com.viettelsoftware.firstspringboot.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class LoggingAspectLogbackTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Test
    void testLoggingAspectWritesToFileAsync() throws Exception {
        LoggingAspect aspect = new LoggingAspect();
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.toShortString()).thenReturn("TestService.testMethod()");
        lenient().when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1"});

        aspect.logBefore(joinPoint);
        aspect.logAfter(joinPoint);

        Thread.sleep(200);

        File logFile = new File("logs/application.log");
        assertTrue(logFile.exists(), "Log file logs/application.log should exist");
        assertTrue(logFile.length() > 0, "Log file should not be empty");
    }
}
