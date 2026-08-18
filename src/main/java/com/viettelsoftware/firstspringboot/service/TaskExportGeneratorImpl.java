package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import lombok.NonNull;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class TaskExportGeneratorImpl implements TaskExportGenerator {

    @Autowired
    private TaskService taskService;

    @Override
    public @NonNull File generate() {
        List<Task> tasks = taskService.getTasks();
        try {
            File tempFile = File.createTempFile("tasks-export-", ".xlsx");
            tempFile.deleteOnExit();

            try (InputStream inputStream = getClass().getResourceAsStream("/templates/tasks_template.xlsx");
                 OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Resource `/templates/tasks_template.xlsx` not found");
                }

                Context context = new Context();
                context.putVar("tasks", tasks);

                JxlsHelper.getInstance().processTemplate(inputStream, outputStream, context);
            }
            return tempFile;

        } catch (IOException exception) {
            throw new RuntimeException("Failed to generate tasks export Excel", exception);
        }
    }
}
