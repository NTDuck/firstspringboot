package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
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
public class TaskExportGeneratorImpl implements TaskExportGenerator {

    private static final String TEMPLATE_PATH = "/templates/tasks_template.xlsx";

    private final TaskService taskService;

    @Override
    public @NonNull File generate() {
        val tasks = taskService.getTasks();

        try {
            val tempFile = File.createTempFile("tasks-export-", ".xlsx");
            tempFile.deleteOnExit();

            renderTemplateToFile(tasks, tempFile);
            return tempFile;

        } catch (IOException exception) {
            throw new RuntimeException("Failed to generate tasks export Excel", exception);
        }
    }

    private void renderTemplateToFile(List<Task> tasks, File destinationFile) throws IOException {
        try (val inputStream = getClass().getResourceAsStream(TEMPLATE_PATH);
             val outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile))) {

            if (inputStream == null) throw new FileNotFoundException(String.format("Resource `%s` not found", TEMPLATE_PATH));

            val context = new Context();
            context.putVar("tasks", tasks);

            JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);
        }
    }
}
