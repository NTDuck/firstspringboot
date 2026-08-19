package com.viettelsoftware.firstspringboot.entity;

import com.viettelsoftware.firstspringboot.entity.abc.AuditableEntity;
import lombok.*;
import javax.persistence.*;

@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String keycloakId;

    private String name;
    private String email;
    private String firstName;
    private String lastName;
}
