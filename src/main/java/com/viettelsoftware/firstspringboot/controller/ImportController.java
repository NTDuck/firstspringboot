package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.controller.dto.ImportResponse;
import com.viettelsoftware.firstspringboot.service.ImportService;
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
@RequestMapping("/api/v1/imports")
public class ImportController {

    private final ImportService importService;

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/{id}")
    public @NonNull ImportResponse getImportStatus(@PathVariable("id") long id) {
        val importEntity = importService.getById(id);
        return ImportResponse.from(importEntity);
    }
}
