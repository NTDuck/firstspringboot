package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.repository.ExportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportAsyncProcessorTest {

    @Mock
    private ExportRepository exportRepository;

    @Mock
    private TaskExportGenerator taskExportGenerator;

    @Mock
    private UserExportGenerator userExportGenerator;

    @Mock
    private ObjectStorageService objectStorageService;

    @InjectMocks
    private ExportAsyncProcessor exportAsyncProcessor;

    @Test
    void testProcessTaskExportSuccess() {
        Export export = Export.builder()
                .id(1L)
                .type(Export.Type.TASK)
                .status(Export.Status.PENDING)
                .requestedBy(Export.RequestedBy.builder().username("testuser").userId(1L).build())
                .build();
        when(exportRepository.findById(1L)).thenReturn(Optional.of(export));
        when(taskExportGenerator.generate()).thenReturn(new byte[]{1, 2, 3});
        when(objectStorageService.createPresignedDownloadUrl(eq("tasks-1.xlsx"), any(Duration.class)))
                .thenReturn("http://localhost:9000/bucket/tasks-1.xlsx");

        exportAsyncProcessor.process(1L);

        verify(objectStorageService).put(eq("tasks-1.xlsx"), eq(new byte[]{1, 2, 3}), anyString());
        ArgumentCaptor<Export> captor = ArgumentCaptor.forClass(Export.class);
        verify(exportRepository, atLeast(2)).save(captor.capture());

        Export finalSaved = captor.getValue();
        assertEquals(Export.Status.SUCCESS, finalSaved.getStatus());
        assertEquals("http://localhost:9000/bucket/tasks-1.xlsx", finalSaved.getUrl());
        assertNotNull(finalSaved.getCompletedAt());
        assertNotNull(finalSaved.getTimeElapsed());
    }

    @Test
    void testProcessUserExportSuccess() {
        Export export = Export.builder()
                .id(2L)
                .type(Export.Type.USER)
                .status(Export.Status.PENDING)
                .requestedBy(Export.RequestedBy.builder().username("testuser").userId(1L).build())
                .build();
        when(exportRepository.findById(2L)).thenReturn(Optional.of(export));
        when(userExportGenerator.generate()).thenReturn(new byte[]{4, 5, 6});
        when(objectStorageService.createPresignedDownloadUrl(eq("users-2.xlsx"), any(Duration.class)))
                .thenReturn("http://localhost:9000/bucket/users-2.xlsx");

        exportAsyncProcessor.process(2L);

        verify(objectStorageService).put(eq("users-2.xlsx"), eq(new byte[]{4, 5, 6}), anyString());
        ArgumentCaptor<Export> captor = ArgumentCaptor.forClass(Export.class);
        verify(exportRepository, atLeast(2)).save(captor.capture());

        Export finalSaved = captor.getValue();
        assertEquals(Export.Status.SUCCESS, finalSaved.getStatus());
        assertEquals("http://localhost:9000/bucket/users-2.xlsx", finalSaved.getUrl());
    }

    @Test
    void testProcessExportFailure() {
        Export export = Export.builder()
                .id(3L)
                .type(Export.Type.TASK)
                .status(Export.Status.PENDING)
                .requestedBy(Export.RequestedBy.builder().username("testuser").userId(1L).build())
                .build();
        when(exportRepository.findById(3L)).thenReturn(Optional.of(export));
        when(taskExportGenerator.generate()).thenThrow(new RuntimeException("Generation failed"));

        exportAsyncProcessor.process(3L);

        ArgumentCaptor<Export> captor = ArgumentCaptor.forClass(Export.class);
        verify(exportRepository, atLeast(2)).save(captor.capture());

        Export finalSaved = captor.getValue();
        assertEquals(Export.Status.FAILED, finalSaved.getStatus());
        assertNotNull(finalSaved.getCompletedAt());
        assertNotNull(finalSaved.getTimeElapsed());
    }

    @Test
    void testProcessExportNotFound() {
        when(exportRepository.findById(999L)).thenReturn(Optional.empty());

        exportAsyncProcessor.process(999L);

        verify(exportRepository, never()).save(any());
        verifyNoInteractions(taskExportGenerator, userExportGenerator, objectStorageService);
    }
}
