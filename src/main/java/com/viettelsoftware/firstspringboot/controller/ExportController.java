package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.dto.DownloadExportResponse;
import com.viettelsoftware.firstspringboot.dto.ExportResponse;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.exception.ExportAlreadyFailedException;
import com.viettelsoftware.firstspringboot.exception.ExportNotReadyException;
import com.viettelsoftware.firstspringboot.service.ExportService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exports")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/{id}")
    public @NonNull ExportResponse getExportStatus(@PathVariable("id") long id) {
        Export export = exportService.getById(id);
        return ExportResponse.from(export);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/{id}/download")
    public @NonNull DownloadExportResponse downloadExport(@PathVariable("id") long id) {
        Export export = exportService.getById(id);
        if (export.getStatus() == Export.Status.FAILED) {
            throw ExportAlreadyFailedException.of(id);
        }
        if (export.getStatus() != Export.Status.SUCCESS || export.getUrl() == null) {
            throw ExportNotReadyException.of(id);
        }
        return DownloadExportResponse.of(export.getUrl());
    }
}
