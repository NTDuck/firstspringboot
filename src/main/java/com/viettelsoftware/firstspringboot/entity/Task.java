package com.viettelsoftware.firstspringboot.entity;

import com.viettelsoftware.firstspringboot.entity.abc.AuditableEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task extends AuditableEntity {

    @Column(nullable = false)
    private String description;
}
