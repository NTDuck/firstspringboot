package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CreateExportRequest;
import com.viettelsoftware.firstspringboot.entity.Export;
import lombok.NonNull;

public interface ExportService {

    @NonNull Export create(@NonNull CreateExportRequest request);

    @NonNull Export getById(@NonNull long id);

    void process(@NonNull long id);
}
