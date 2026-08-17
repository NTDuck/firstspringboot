package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
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
public class TaskExportServiceImpl implements TaskExportService {

    @Override
    public byte[] exportTasks(List<@NonNull Task> tasks) {
        try (val inputStream = getClass().getResourceAsStream("/templates/tasks_template.xlsx");
             val outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource `/templates/tasks_template.xlsx` not found");
            }

            val context = new Context();
            context.putVar("tasks", tasks);

            JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);
            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new RuntimeException("Failed to export tasks to Excel", exception);
        }
    }
}
