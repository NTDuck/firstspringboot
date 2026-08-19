package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.controller.dto.CreateImportRequest;
import com.viettelsoftware.firstspringboot.controller.exception.ImportNotFoundException;
import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.repository.ImportRepository;
import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceImplTest {

    @Mock
    private ImportRepository importRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ImportServiceImpl.Processor processor;

    @Mock
    private ObjectStorageService objectStorageService;

    @InjectMocks
    private ImportServiceImpl importService;

    @Test
    void testCreateImport() {
        AuthenticatedUserDto user = AuthenticatedUserDto.builder().id(5L).name("john").roles(List.of()).build();
        when(authenticationService.getCurrentAuthenticatedUser()).thenReturn(Optional.of(user));

        Import saved = Import.builder()
                .type(Import.Type.TASK)
                .status(Import.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(saved, "id", 10L);

        when(importRepository.save(any(Import.class))).thenReturn(saved);
        when(objectStorageService.createPresignedUploadUrl(eq("imports-10.xlsx"), any(Duration.class)))
                .thenReturn("http://localhost:9000/bucket/imports-10.xlsx?upload=true");

        Import result = importService.create(CreateImportRequest.of(Import.Type.TASK));

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(importRepository, atLeastOnce()).save(any(Import.class));
        verify(processor).process(10L);
    }

    @Test
    void testGetByIdSuccess() {
        Import importEntity = Import.builder()
                .type(Import.Type.USER)
                .status(Import.Status.SUCCESS)
                .build();
        ReflectionTestUtils.setField(importEntity, "id", 10L);

        when(importRepository.findById(10L)).thenReturn(Optional.of(importEntity));

        Import result = importService.getById(10L);
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void testGetByIdNotFound() {
        when(importRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ImportNotFoundException.class, () -> importService.getById(999L));
    }

    @Test
    void testProcessDelegatesToProcessor() {
        importService.process(15L);
        verify(processor).process(15L);
    }
}
