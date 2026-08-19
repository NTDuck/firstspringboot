package com.viettelsoftware.firstspringboot.aspect;

import com.viettelsoftware.firstspringboot.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private AuditAspect auditAspect;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void testAuditAspectSuccess() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) AuditAspectTest.class);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.proceed()).thenReturn("result");

        Object result = auditAspect.audit(joinPoint);

        assertEquals("result", result);
        verify(auditService, times(1)).auditSuccess("AuditAspectTest", "TESTMETHOD");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void testAuditAspectFailure() throws Throwable {
        RuntimeException ex = new RuntimeException("Aspect error");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) AuditAspectTest.class);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.proceed()).thenThrow(ex);

        assertThrows(RuntimeException.class, () -> auditAspect.audit(joinPoint));
        verify(auditService, times(1)).auditFailure("AuditAspectTest", "TESTMETHOD", ex);
    }
}
