package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.controller.model.CreateExportRequest;
import com.viettelsoftware.firstspringboot.service.model.AuthenticatedUser;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.controller.exception.ExportNotFoundException;
import com.viettelsoftware.firstspringboot.repository.ExportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    private ExportRepository exportRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ExportServiceImpl.Processor processor;

    @InjectMocks
    private ExportServiceImpl exportService;

    @Test
    void testCreateExport() {
        AuthenticatedUser user = AuthenticatedUser.builder().id(5L).name("john").roles(List.of()).build();
        when(authenticationService.getCurrentAuthenticatedUser()).thenReturn(user);

        Export saved = Export.builder()
                .id(10L)
                .type(Export.Type.TASK)
                .status(Export.Status.PENDING)
                .requestedBy(Export.RequestedBy.builder().username("john").userId(5L).build())
                .build();
        when(exportRepository.save(any(Export.class))).thenReturn(saved);

        Export result = exportService.create(CreateExportRequest.of(Export.Type.TASK));

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(exportRepository).save(any(Export.class));
        verify(processor).process(10L);
    }

    @Test
    void testGetByIdSuccess() {
        Export export = Export.builder()
                .id(10L)
                .type(Export.Type.USER)
                .status(Export.Status.SUCCESS)
                .requestedBy(Export.RequestedBy.builder().username("john").userId(5L).build())
                .build();
        when(exportRepository.findById(10L)).thenReturn(Optional.of(export));

        Export result = exportService.getById(10L);
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void testGetByIdNotFound() {
        when(exportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ExportNotFoundException.class, () -> exportService.getById(999L));
    }

    @Test
    void testProcessDelegatesToAsyncProcessor() {
        exportService.process(15L);
        verify(processor).process(15L);
    }
}
