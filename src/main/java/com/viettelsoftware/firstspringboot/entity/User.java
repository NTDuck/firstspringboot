package com.viettelsoftware.firstspringboot.entity;

import lombok.*;
import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @NonNull long id;

    @With
    @Column(name = "keycloak_id", nullable = false, unique = true)
    private @NonNull String keycloakId;

    @With
    @Column(name = "name")
    private @NonNull String name;

    @With
    @Column(name = "email")
    private @NonNull String email;

    @With
    @Column(name = "first_name")
    private @NonNull String firstName;

    @With
    @Column(name = "last_name")
    private @NonNull String lastName;
}
