package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
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
            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new RuntimeException("Failed to export users to Excel", exception);
        }
    }
}
