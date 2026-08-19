package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.repository.AuditEventRepository;
import com.viettelsoftware.firstspringboot.service.dto.AuditEventsQueryFilterDto;
import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import com.viettelsoftware.firstspringboot.service.exception.CurrentAuthenticatedUserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        AuthenticatedUserDto user = AuthenticatedUserDto.builder()
                .id(1L)
                .name("admin")
                .roles(List.of("ROLE_ADMIN"))
                .build();
        lenient().when(authenticationService.getCurrentAuthenticatedUser()).thenReturn(Optional.of(user));
    }

    @Test
    void testAuditSuccess() {
        auditService.auditSuccess("TaskService", "CREATE_TASK");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository, times(1)).save(captor.capture());

        AuditEvent saved = captor.getValue();
        assertEquals("TaskService", saved.getService().getName());
        assertEquals("CREATE_TASK", saved.getAction());
        assertEquals(1L, saved.getActor().getUserId());
        assertEquals("admin", saved.getActor().getUsername());
        assertTrue(saved.isResult());
        assertNull(saved.getException());
    }

    @Test
    void testAuditFailure() {
        Exception exception = new RuntimeException("Database error");
        auditService.auditFailure("TaskService", "CREATE_TASK", exception);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository, times(1)).save(captor.capture());

        AuditEvent saved = captor.getValue();
        assertEquals("TaskService", saved.getService().getName());
        assertEquals("CREATE_TASK", saved.getAction());
        assertEquals(1L, saved.getActor().getUserId());
        assertEquals("admin", saved.getActor().getUsername());
        assertFalse(saved.isResult());
        assertEquals("Database error", saved.getException());
    }

    @Test
    void testAuditSuccessThrowsWhenUnauthenticated() {
        when(authenticationService.getCurrentAuthenticatedUser()).thenReturn(Optional.empty());

        assertThrows(CurrentAuthenticatedUserNotFoundException.class,
                () -> auditService.auditSuccess("TaskService", "CREATE_TASK"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetAuditEventsWithFilter() {
        AuditEvent event = AuditEvent.builder()
                .service(AuditEvent.Service.builder().name("TaskService").build())
                .actor(AuditEvent.Actor.builder().userId(1L).username("admin").build())
                .action("CREATE_TASK")
                .result(true)
                .build();

        PageImpl<AuditEvent> page = new PageImpl<>(List.of(event));
        when(auditEventRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        AuditEventsQueryFilterDto filter = AuditEventsQueryFilterDto.builder()
                .day(LocalDate.now())
                .serviceName("TaskService")
                .action("CREATE_TASK")
                .result(true)
                .build();

        Page<AuditEvent> result = auditService.getAuditEvents(filter, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(auditEventRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }
}
