package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.repository.ExportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplProcessorTest {

    @Mock
    private ExportRepository exportRepository;

    @Mock
    private TaskExportGenerator taskExportGenerator;

    @Mock
    private UserExportGenerator userExportGenerator;

    @Mock
    private ObjectStorageService objectStorageService;

    @InjectMocks
    private ExportServiceImpl.Processor processor;

    @Test
    void testProcessTaskExportSuccess() throws Exception {
        Export export = Export.builder()
                .type(Export.Type.TASK)
                .status(Export.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(export, "id", 1L);

        File tempFile = File.createTempFile("test-task-", ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[]{1, 2, 3});
        }

        when(exportRepository.findById(1L)).thenReturn(Optional.of(export));
        when(taskExportGenerator.generate()).thenReturn(tempFile);
        when(objectStorageService.createPresignedDownloadUrl(eq("tasks-1.xlsx"), any(Duration.class)))
                .thenReturn("http://localhost:9000/bucket/tasks-1.xlsx");

        processor.process(1L);

        verify(objectStorageService).put(eq("tasks-1.xlsx"), any(InputStream.class), eq(3L), anyString());
        ArgumentCaptor<Export> captor = ArgumentCaptor.forClass(Export.class);
        verify(exportRepository, atLeast(2)).save(captor.capture());

        Export finalSaved = captor.getValue();
        assertEquals(Export.Status.SUCCESS, finalSaved.getStatus());
        assertEquals("http://localhost:9000/bucket/tasks-1.xlsx", finalSaved.getUrl());
        assertNotNull(finalSaved.getCompletedAt());
    }

    @Test
    void testProcessUserExportSuccess() throws Exception {
        Export export = Export.builder()
                .type(Export.Type.USER)
                .status(Export.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(export, "id", 2L);

        File tempFile = File.createTempFile("test-user-", ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[]{4, 5, 6, 7});
        }

        when(exportRepository.findById(2L)).thenReturn(Optional.of(export));
        when(userExportGenerator.generate()).thenReturn(tempFile);
        when(objectStorageService.createPresignedDownloadUrl(eq("users-2.xlsx"), any(Duration.class)))
                .thenReturn("http://localhost:9000/bucket/users-2.xlsx");

        processor.process(2L);

        verify(objectStorageService).put(eq("users-2.xlsx"), any(InputStream.class), eq(4L), anyString());
        ArgumentCaptor<Export> captor = ArgumentCaptor.forClass(Export.class);
        verify(exportRepository, atLeast(2)).save(captor.capture());

        Export finalSaved = captor.getValue();
        assertEquals(Export.Status.SUCCESS, finalSaved.getStatus());
        assertEquals("http://localhost:9000/bucket/users-2.xlsx", finalSaved.getUrl());
    }

    @Test
    void testProcessExportFailure() {
        Export export = Export.builder()
                .type(Export.Type.TASK)
                .status(Export.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(export, "id", 3L);

        when(exportRepository.findById(3L)).thenReturn(Optional.of(export));
        when(taskExportGenerator.generate()).thenThrow(new RuntimeException("Generation failed"));

        processor.process(3L);

        ArgumentCaptor<Export> captor = ArgumentCaptor.forClass(Export.class);
        verify(exportRepository, atLeast(2)).save(captor.capture());

        Export finalSaved = captor.getValue();
        assertEquals(Export.Status.FAILED, finalSaved.getStatus());
        assertNotNull(finalSaved.getCompletedAt());
    }

    @Test
    void testProcessExportNotFound() {
        when(exportRepository.findById(999L)).thenReturn(Optional.empty());

        processor.process(999L);

        verify(exportRepository, never()).save(any());
        verifyNoInteractions(taskExportGenerator, userExportGenerator, objectStorageService);
    }
}
