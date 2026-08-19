package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.controller.dto.DownloadExportResponse;
import com.viettelsoftware.firstspringboot.controller.dto.ExportResponse;
import com.viettelsoftware.firstspringboot.service.ExportService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exports")
public class ExportController {

    private final ExportService exportService;

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/{id}")
    public @NonNull ExportResponse getExportStatus(@PathVariable("id") long id) {
        val export = exportService.getById(id);
        return ExportResponse.from(export);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/{id}/download")
    public @NonNull DownloadExportResponse downloadExport(@PathVariable("id") long id) {
        val downloadUrl = exportService.getDownloadUrl(id);
        return DownloadExportResponse.of(downloadUrl);
    }
}
