package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.controller.dto.CreateExportRequest;
import com.viettelsoftware.firstspringboot.entity.Export;
import lombok.NonNull;

public interface ExportService {

    @NonNull Export create(@NonNull CreateExportRequest request);

    @NonNull Export getById(long id);

    @NonNull String getDownloadUrl(long id);

    void process(long id);
}
