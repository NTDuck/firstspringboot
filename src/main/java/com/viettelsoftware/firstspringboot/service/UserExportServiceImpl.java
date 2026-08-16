package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class UserExportServiceImpl implements UserExportService {

    @Override
    public byte[] exportUsers(List<@NonNull User> users) {
        try (InputStream is = getClass().getResourceAsStream("/templates/users_template.xlsx")) {
            if (is != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                Context context = new Context();
                context.putVar("users", users);
                JxlsHelper.getInstance().processTemplate(is, os, context);
                return os.toByteArray();
            }
        } catch (Exception ignored) {
            // Fallback to POI if template fails or is absent
        }
        return exportUsersWithPoi(users);
    }

    private byte[] exportUsersWithPoi(List<@NonNull User> users) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Keycloak ID");
            headerRow.createCell(2).setCellValue("Name");
            headerRow.createCell(3).setCellValue("Email");
            headerRow.createCell(4).setCellValue("First Name");
            headerRow.createCell(5).setCellValue("Last Name");

            int rowIdx = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getKeycloakId());
                row.createCell(2).setCellValue(user.getName());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(user.getFirstName());
                row.createCell(5).setCellValue(user.getLastName());
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(os);
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export users to excel", e);
        }
    }
}
