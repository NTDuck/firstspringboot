package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import lombok.NonNull;
import lombok.val;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Service
public class UserExportServiceImpl implements UserExportService {

    @Override
    public byte[] exportUsers(List<@NonNull User> users) {
        try (val inputStream = getClass().getResourceAsStream("/templates/users_template.xlsx");
             val outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource `/templates/users_template.xlsx` not found");
            }

            val context = new Context();
            context.putVar("users", users);

            JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);
            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new RuntimeException("Failed to export users to Excel", exception);
        }
    }
}
