package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    @Test
    void testAuditSuccess() {
        AuditEvent event = AuditEvent.builder()
                .serviceName("TestService")
                .actorUserId(1L)
                .actorUsername("admin")
                .action("TEST_ACTION")
                .result(true)
                .build();

        auditService.audit(event);

        assertNotNull(event.getTimestamp());
        assertEquals("admin", event.getActorUsername());
        verify(auditEventRepository, times(1)).save(event);
    }

    @Test
    void testSearchAuditLogs() {
        AuditEvent event = AuditEvent.builder()
                .id(1L)
                .timestamp(Instant.now())
                .serviceName("TaskService")
                .actorUserId(1L)
                .actorUsername("admin")
                .action("CREATE_TASK")
                .result(true)
                .build();

        PageImpl<AuditEvent> page = new PageImpl<>(List.of(event));
        when(auditEventRepository.searchAuditEvents(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        Page<AuditEvent> result = auditService.search(
                LocalDate.now(), "TaskService", 1L, "admin", "CREATE_TASK", true, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAuditEventsByServiceName() {
        AuditEvent event = AuditEvent.builder()
                .id(1L)
                .timestamp(Instant.now())
                .serviceName("TaskService")
                .actorUserId(1L)
                .actorUsername("admin")
                .action("CREATE_TASK")
                .result(true)
                .build();

        PageImpl<AuditEvent> page = new PageImpl<>(List.of(event));
        when(auditEventRepository.findByServiceName(eq("TaskService"), any())).thenReturn(page);

        Page<AuditEvent> result = auditService.getAuditEventsByServiceName("TaskService", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
