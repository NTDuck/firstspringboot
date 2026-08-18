package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.dto.ImportResponse;
import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.service.ImportService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/imports")
public class ImportController {

    @Autowired
    private ImportService importService;

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/{id}")
    public @NonNull ImportResponse getImportStatus(@PathVariable("id") long id) {
        Import importEntity = importService.getById(id);
        return ImportResponse.from(importEntity);
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_POST')")
    @PostMapping("/{id}/process")
    public ResponseEntity<Void> processImport(@PathVariable("id") long id) {
        importService.process(id);
        return ResponseEntity.accepted().build();
    }
}
