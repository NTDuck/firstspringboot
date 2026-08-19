package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.controller.model.ImportResponse;
import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.service.ImportService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
