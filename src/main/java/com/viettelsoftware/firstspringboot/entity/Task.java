package com.viettelsoftware.firstspringboot.entity;

import com.viettelsoftware.firstspringboot.entity.abc.AuditableEntity;
import lombok.*;
import javax.persistence.*;

// https://www.javaguides.net/2020/01/spring-boot-mariadb-crud-example-tutorial.html
@Getter
@Setter
@Builder
@AllArgsConstructor(staticName = "of")
@Entity
@Table(name = "tasks")
public class Task extends AuditableEntity {

    @Column(nullable = false)
    private String description;
}
