package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.controller.dto.CreateExportRequest;
import com.viettelsoftware.firstspringboot.controller.exception.ExportAlreadyFailedException;
import com.viettelsoftware.firstspringboot.controller.exception.ExportNotFoundException;
import com.viettelsoftware.firstspringboot.controller.exception.ExportNotReadyException;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.repository.ExportRepository;
import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
        AuthenticatedUserDto user = AuthenticatedUserDto.builder().id(5L).name("john").roles(List.of()).build();
        when(authenticationService.getCurrentAuthenticatedUser()).thenReturn(Optional.of(user));

        Export saved = Export.builder()
                .type(Export.Type.TASK)
                .status(Export.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(saved, "id", 10L);

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
                .type(Export.Type.USER)
                .status(Export.Status.SUCCESS)
                .build();
        ReflectionTestUtils.setField(export, "id", 10L);

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
    void testGetDownloadUrlSuccess() {
        Export export = Export.builder()
                .type(Export.Type.TASK)
                .status(Export.Status.SUCCESS)
                .url("http://localhost:9000/bucket/tasks-1.xlsx")
                .build();
        ReflectionTestUtils.setField(export, "id", 1L);

        when(exportRepository.findById(1L)).thenReturn(Optional.of(export));

        String url = exportService.getDownloadUrl(1L);
        assertEquals("http://localhost:9000/bucket/tasks-1.xlsx", url);
    }

    @Test
    void testGetDownloadUrlWhenFailed() {
        Export export = Export.builder()
                .type(Export.Type.TASK)
                .status(Export.Status.FAILED)
                .build();
        ReflectionTestUtils.setField(export, "id", 2L);

        when(exportRepository.findById(2L)).thenReturn(Optional.of(export));

        assertThrows(ExportAlreadyFailedException.class, () -> exportService.getDownloadUrl(2L));
    }

    @Test
    void testGetDownloadUrlWhenNotReady() {
        Export export = Export.builder()
                .type(Export.Type.TASK)
                .status(Export.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(export, "id", 3L);

        when(exportRepository.findById(3L)).thenReturn(Optional.of(export));

        assertThrows(ExportNotReadyException.class, () -> exportService.getDownloadUrl(3L));
    }

    @Test
    void testProcessDelegatesToAsyncProcessor() {
        exportService.process(15L);
        verify(processor).process(15L);
    }
}
