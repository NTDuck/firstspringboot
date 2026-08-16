package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import lombok.NonNull;

import java.util.List;

public interface TaskExportService {
    byte[] exportTasks(List<@NonNull Task> tasks);
}
