package com.viettelsoftware.firstspringboot.entity;

import com.viettelsoftware.firstspringboot.entity.abc.ImportExport;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@SuperBuilder
@Entity
@Table(name = "exports", indexes = {
        @Index(name = "idx_exports_status_created_at", columnList = "status, created_at")
})
public class Import extends ImportExport { }