package com.viettelsoftware.firstspringboot.entity;

import lombok.*;
import javax.persistence.*;

// https://www.javaguides.net/2020/01/spring-boot-mariadb-crud-example-tutorial.html
@Getter
@Setter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @With
    @Column(name = "task", nullable = false)
    private @NonNull String description;
}
