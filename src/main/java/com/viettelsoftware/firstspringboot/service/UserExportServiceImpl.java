package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.entity.User;
import lombok.NonNull;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class UserExportServiceImpl implements UserExportService {

    @Autowired
    private UserService userService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthService authService;

    @Override
    public byte[] exportUsers() {
        List<User> users = userService.getUsers();
        try (InputStream inputStream = getClass().getResourceAsStream("/templates/users_template.xlsx");
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource `/templates/users_template.xlsx` not found");
            }

            Context context = new Context();
            context.putVar("users", users);

            JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);

            audit("EXPORT_USERS");
            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new RuntimeException("Failed to export users to Excel", exception);
        }
    }

    private void audit(@NonNull String action) {
        CurrentUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        AuditEvent auditEvent = AuditEvent.builder()
                .serviceName(UserExportService.class.getSimpleName())
                .actorUserId(currentUser.getId())
                .actorUsername(currentUser.getName())
                .action(action)
                .result(true)
                .exception(null)
                .build();

        auditService.audit(auditEvent);
    }
}
