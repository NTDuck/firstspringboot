package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.annotation.Auditable;
import com.viettelsoftware.firstspringboot.config.exception.InsufficientAuthorizationException;
import com.viettelsoftware.firstspringboot.controller.dto.CreateExportRequest;
import com.viettelsoftware.firstspringboot.controller.exception.ExportAlreadyFailedException;
import com.viettelsoftware.firstspringboot.controller.exception.ExportNotFoundException;
import com.viettelsoftware.firstspringboot.controller.exception.ExportNotReadyException;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.repository.ExportRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofDays(7);
    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExportRepository exportRepository;
    private final AuthenticationService authenticationService;
    private final Processor processor;

    @Override
    @Auditable
    @Transactional
    public @NonNull Export create(@NonNull CreateExportRequest request) {
        validateAuthenticatedUser();

        val export = Export.builder()
                .type(request.getType())
                .status(Export.Status.PENDING)
                .build();

        val saved = exportRepository.save(export);
        processor.process(saved.getId());
        return saved;
    }

    @Override
    @Auditable
    @Transactional(readOnly = true)
    public @NonNull Export getById(long id) {
        return exportRepository.findById(id)
                .orElseThrow(() -> ExportNotFoundException.of(id));
    }

    @Override
    @Auditable
    @Transactional(readOnly = true)
    public @NonNull String getDownloadUrl(long id) {
        val export = getById(id);

        if (export.getStatus() == Export.Status.FAILED) throw ExportAlreadyFailedException.of(id);
        if (export.getStatus() != Export.Status.SUCCESS || export.getUrl() == null) throw ExportNotReadyException.of(id);

        return export.getUrl();
    }

    @Override
    public void process(long id) {
        processor.process(id);
    }

    private void validateAuthenticatedUser() {
        if (authenticationService.getCurrentAuthenticatedUser().isEmpty()) {
            throw InsufficientAuthorizationException.builder()
                    .username("anonymous")
                    .operation("create export")
                    .build();
        }
    }

    @Slf4j
    @Component
    @RequiredArgsConstructor
    public static class Processor {

        private final ExportRepository exportRepository;
        private final TaskExportGenerator taskExportGenerator;
        private final UserExportGenerator userExportGenerator;
        private final ObjectStorageService objectStorageService;

        @Async
        public void process(long exportId) {
            val claimedExport = claimJob(exportId);
            if (claimedExport.isEmpty()) return;

            val export = claimedExport.get();
            val startTime = System.currentTimeMillis();
            File tempFile = null;
            try {
                tempFile = generateExportFile(export.getType());
                val objectKey = buildObjectKey(export.getType(), exportId);

                uploadExportFile(objectKey, tempFile);

                val presignedUrl = objectStorageService.createPresignedDownloadUrl(objectKey, PRESIGNED_URL_EXPIRATION);
                markSuccess(export, presignedUrl, startTime);

            } catch (Exception exception) {
                log.error("Error occurred while processing export `{}`: {}", exportId, exception.getMessage(), exception);
                markFailed(export, startTime);
            } finally {
                cleanupTempFile(tempFile);
            }
        }

        // MariaDB SELECT ... FOR UPDATE row-level lock for atomic job claiming
        @Transactional
        public Optional<Export> claimJob(long exportId) {
            val exportOptional = exportRepository.findByIdForUpdate(exportId);
            if (exportOptional.isEmpty()) return Optional.empty();

            val export = exportOptional.get();
            if (export.getStatus() != Export.Status.PENDING) return Optional.empty();

            export.setStatus(Export.Status.PROCESSING);
            return Optional.of(exportRepository.save(export));
        }

        private File generateExportFile(Export.Type type) {
            if (type == Export.Type.TASK) return taskExportGenerator.generate();
            return userExportGenerator.generate();
        }

        private String buildObjectKey(Export.Type type, long exportId) {
            val prefix = type == Export.Type.TASK ? "tasks" : "users";
            return String.format("%s-%d.xlsx", prefix, exportId);
        }

        private void uploadExportFile(String objectKey, File tempFile) throws Exception {
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(tempFile))) {
                objectStorageService.put(objectKey, inputStream, tempFile.length(), XLSX_CONTENT_TYPE);
            }
        }

        private void markSuccess(Export export, String presignedUrl, long startTime) {
            export.setStatus(Export.Status.SUCCESS);
            export.setUrl(presignedUrl);
            export.setCompletedAt(Instant.now());
            exportRepository.save(export);
        }

        private void markFailed(Export export, long startTime) {
            export.setStatus(Export.Status.FAILED);
            export.setCompletedAt(Instant.now());
            exportRepository.save(export);
        }

        private void cleanupTempFile(File tempFile) {
            if (tempFile == null || !tempFile.exists()) return;
            try {
                Files.deleteIfExists(tempFile.toPath());
            } catch (Exception ignored) {
                tempFile.delete();
            }
        }
    }
}
