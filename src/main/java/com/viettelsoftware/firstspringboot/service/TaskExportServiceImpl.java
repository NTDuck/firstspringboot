package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.entity.Task;
import lombok.NonNull;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TaskExportServiceImpl implements TaskExportService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MinIOStorageService minIOStorageService;

    @Override
    public @NonNull String exportTasks() {
        List<Task> tasks = taskService.getTasks();
        byte[] excelBytes;
        try (InputStream inputStream = getClass().getResourceAsStream("/templates/tasks_template.xlsx");
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource `/templates/tasks_template.xlsx` not found");
            }

            Context context = new Context();
            context.putVar("tasks", tasks);

            JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);
            excelBytes = outputStream.toByteArray();

        } catch (IOException exception) {
            throw new RuntimeException("Failed to export tasks to Excel", exception);
        }

        String timestamp = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String objectName = String.format("tasks-%s.xlsx", timestamp);

        String presignedUrl = minIOStorageService.uploadFileAndGetPresignedUrl(
                objectName,
                excelBytes,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        audit("EXPORT_TASKS");
        return presignedUrl;
    }

    private void audit(@NonNull String action) {
        CurrentUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        AuditEvent auditEvent = AuditEvent.builder()
                .serviceName(TaskExportService.class.getSimpleName())
                .actorUserId(currentUser.getId())
                .actorUsername(currentUser.getName())
                .action(action)
                .result(true)
                .exception(null)
                .build();

        auditService.audit(auditEvent);
    }
}
