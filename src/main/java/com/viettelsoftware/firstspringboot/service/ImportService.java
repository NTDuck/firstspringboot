package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.controller.dto.CreateImportRequest;
import com.viettelsoftware.firstspringboot.entity.Import;
import lombok.NonNull;

public interface ImportService {

    @NonNull Import create(@NonNull CreateImportRequest request);

    @NonNull Import getById(long id);

    void process(long id);
}
