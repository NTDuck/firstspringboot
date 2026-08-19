package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserExportGeneratorImpl implements UserExportGenerator {

    private static final String TEMPLATE_PATH = "/templates/users_template.xlsx";

    private final UserService userService;

    @Override
    public @NonNull File generate() {
        val users = userService.getUsers();

        try {
            val tempFile = File.createTempFile("users-export-", ".xlsx");
            tempFile.deleteOnExit();

            renderTemplateToFile(users, tempFile);
            return tempFile;

        } catch (IOException exception) {
            throw new RuntimeException("Failed to generate users export Excel", exception);
        }
    }

    private void renderTemplateToFile(List<User> users, File destinationFile) throws IOException {
        try (val inputStream = getClass().getResourceAsStream(TEMPLATE_PATH);
             val outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile))) {

            if (inputStream == null) throw new FileNotFoundException(String.format("Resource `%s` not found", TEMPLATE_PATH));

            val context = new Context();
            context.putVar("users", users);

            JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);
        }
    }
}
