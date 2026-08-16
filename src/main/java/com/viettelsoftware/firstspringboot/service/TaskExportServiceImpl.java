package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
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
public class TaskExportServiceImpl implements TaskExportService {

    @Override
    public byte[] exportTasks(List<@NonNull Task> tasks) {
        try (InputStream is = getClass().getResourceAsStream("/templates/tasks_template.xlsx")) {
            if (is != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                Context context = new Context();
                context.putVar("tasks", tasks);
                JxlsHelper.getInstance().processTemplate(is, os, context);
                return os.toByteArray();
            }
        } catch (Exception ignored) {
            // Fallback to POI if template fails or is absent
        }
        return exportTasksWithPoi(tasks);
    }

    private byte[] exportTasksWithPoi(List<@NonNull Task> tasks) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Tasks");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Description");

            int rowIdx = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getDescription());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(os);
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export tasks to excel", e);
        }
    }
}
