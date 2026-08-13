package com.viettelsoftware.firstspringboot.task.entity;

import lombok.*;
import javax.persistence.*;

// https://www.javaguides.net/2020/01/spring-boot-mariadb-crud-example-tutorial.html
@Getter
@Setter
@Builder
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @NonNull long id;

    @Column(name = "task", nullable = false)
    private @NonNull String description;
}
