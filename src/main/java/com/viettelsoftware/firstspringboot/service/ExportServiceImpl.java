package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CreateExportRequest;
import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.exception.ExportNotFoundException;
import com.viettelsoftware.firstspringboot.repository.ExportRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private ExportRepository exportRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ExportAsyncProcessor exportAsyncProcessor;

    @Override
    public @NonNull Export create(@NonNull CreateExportRequest request) {
        CurrentUser user = authService.getCurrentUser();
        String username = user != null && user.getName() != null ? user.getName() : "";
        Long userId = user != null ? user.getId() : 0L;

        Export.RequestedBy requestedBy = Export.RequestedBy.builder()
                .username(username)
                .userId(userId)
                .build();

        Export export = Export.builder()
                .type(request.getType())
                .status(Export.Status.PENDING)
                .requestedBy(requestedBy)
                .build();

        Export saved = exportRepository.save(export);
        exportAsyncProcessor.process(saved.getId());
        return saved;
    }

    @Override
    public @NonNull Export getById(@NonNull long id) {
        return exportRepository.findById(id)
                .orElseThrow(() -> ExportNotFoundException.of(id));
    }

    @Override
    public void process(@NonNull long id) {
        exportAsyncProcessor.process(id);
    }
}
