package com.viettelsoftware.firstspringboot.service;

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
import java.util.List;

@Service
public class TaskExportGeneratorImpl implements TaskExportGenerator {

    @Autowired
    private TaskService taskService;

    @Override
    public byte @NonNull [] generate() {
        List<Task> tasks = taskService.getTasks();
        try (InputStream inputStream = getClass().getResourceAsStream("/templates/tasks_template.xlsx");
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource `/templates/tasks_template.xlsx` not found");
            }

            Context context = new Context();
            context.putVar("tasks", tasks);

            JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);
            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new RuntimeException("Failed to generate tasks export Excel", exception);
        }
    }
}
