package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.controller.dto.CreateExportRequest;
import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.controller.exception.ExportNotFoundException;
import com.viettelsoftware.firstspringboot.config.exception.InsufficientAuthorizationException;
import com.viettelsoftware.firstspringboot.repository.ExportRepository;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private ExportRepository exportRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private Processor processor;

    @Override
    public @NonNull Export create(@NonNull CreateExportRequest request) {
        AuthenticatedUserDto user = authenticationService.getCurrentAuthenticatedUser();
        if (user == null) {
            throw new InsufficientAuthorizationException("anonymous", "create export");
        }

        Export.RequestedBy requestedBy = Export.RequestedBy.builder()
                .username(user.getName())
                .userId(user.getId())
                .build();

        Export export = Export.builder()
                .type(request.getType())
                .status(Export.Status.PENDING)
                .requestedBy(requestedBy)
                .build();

        Export saved = exportRepository.save(export);
        processor.process(saved.getId());
        return saved;
    }

    @Override
    public @NonNull Export getById(long id) {
        return exportRepository.findById(id)
                .orElseThrow(() -> ExportNotFoundException.of(id));
    }

    @Override
    public void process(long id) {
        processor.process(id);
    }

    @Component
    public static class Processor {

        private final @NonNull Logger logger = LoggerFactory.getLogger(this.getClass());

        @Autowired
        private ExportRepository exportRepository;

        @Autowired
        private TaskExportGenerator taskExportGenerator;

        @Autowired
        private UserExportGenerator userExportGenerator;

        @Autowired
        private ObjectStorageService objectStorageService;

        @Async
        public void process(long exportId) {
            Optional<Export> exportOptional = exportRepository.findById(exportId);
            if (exportOptional.isEmpty()) {
                return;
            }

            Export export = exportOptional.get();
            export.setStatus(Export.Status.PROCESSING);
            exportRepository.save(export);

            long startTime = System.currentTimeMillis();
            File tempFile = null;
            try {
                String objectKey;

                if (export.getType() == Export.Type.TASK) {
                    tempFile = taskExportGenerator.generate();
                    objectKey = String.format("tasks-%d.xlsx", exportId);
                } else {
                    tempFile = userExportGenerator.generate();
                    objectKey = String.format("users-%d.xlsx", exportId);
                }

                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(tempFile))) {
                    objectStorageService.put(
                            objectKey,
                            inputStream,
                            tempFile.length(),
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    );
                }

                String presignedUrl = objectStorageService.createPresignedDownloadUrl(objectKey, Duration.ofDays(7));
                long elapsed = System.currentTimeMillis() - startTime;

                export.setStatus(Export.Status.SUCCESS);
                export.setUrl(presignedUrl);
                export.setCompletedAt(Instant.now());
                export.setTimeElapsed(elapsed);
                exportRepository.save(export);

            } catch (Exception exception) {
                logger.error("Error occurred while processing export `{}`: {}", exportId, exception.getMessage(), exception);
                long elapsed = System.currentTimeMillis() - startTime;
                export.setStatus(Export.Status.FAILED);
                export.setCompletedAt(Instant.now());
                export.setTimeElapsed(elapsed);
                exportRepository.save(export);
            } finally {
                if (tempFile != null && tempFile.exists()) {
                    try {
                        Files.deleteIfExists(tempFile.toPath());
                    } catch (Exception ignored) {
                        tempFile.delete();
                    }
                }
            }
        }
    }
}
