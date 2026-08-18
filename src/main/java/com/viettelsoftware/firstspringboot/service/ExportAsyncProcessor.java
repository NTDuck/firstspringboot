package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.repository.ExportRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class ExportAsyncProcessor {

    @Autowired
    private ExportRepository exportRepository;

    @Autowired
    private TaskExportGenerator taskExportGenerator;

    @Autowired
    private UserExportGenerator userExportGenerator;

    @Autowired
    private ObjectStorageService objectStorageService;

    @Async
    public void process(@NonNull long exportId) {
        Optional<Export> exportOptional = exportRepository.findById(exportId);
        if (exportOptional.isEmpty()) {
            return;
        }

        Export export = exportOptional.get();
        export.setStatus(Export.Status.PROCESSING);
        exportRepository.save(export);

        long startTime = System.currentTimeMillis();
        try {
            byte[] fileContent;
            String objectKey;

            if (export.getType() == Export.Type.TASK) {
                fileContent = taskExportGenerator.generate();
                objectKey = String.format("tasks-%d.xlsx", exportId);
            } else {
                fileContent = userExportGenerator.generate();
                objectKey = String.format("users-%d.xlsx", exportId);
            }

            objectStorageService.put(
                    objectKey,
                    fileContent,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

            String presignedUrl = objectStorageService.createPresignedDownloadUrl(objectKey, Duration.ofDays(7));
            long elapsed = System.currentTimeMillis() - startTime;

            export.setStatus(Export.Status.SUCCESS);
            export.setUrl(presignedUrl);
            export.setCompletedAt(Instant.now());
            export.setTimeElapsed(elapsed);
            exportRepository.save(export);

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            export.setStatus(Export.Status.FAILED);
            export.setCompletedAt(Instant.now());
            export.setTimeElapsed(elapsed);
            exportRepository.save(export);
        }
    }
}
