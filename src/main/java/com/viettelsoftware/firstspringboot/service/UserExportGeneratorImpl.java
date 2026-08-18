package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import lombok.NonNull;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class UserExportGeneratorImpl implements UserExportGenerator {

    @Autowired
    private UserService userService;

    @Override
    public @NonNull File generate() {
        List<User> users = userService.getUsers();
        try {
            File tempFile = File.createTempFile("users-export-", ".xlsx");
            tempFile.deleteOnExit();

            try (InputStream inputStream = getClass().getResourceAsStream("/templates/users_template.xlsx");
                 OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Resource `/templates/users_template.xlsx` not found");
                }

                Context context = new Context();
                context.putVar("users", users);

                JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);
            }
            return tempFile;

        } catch (IOException exception) {
            throw new RuntimeException("Failed to generate users export Excel", exception);
        }
    }
}
