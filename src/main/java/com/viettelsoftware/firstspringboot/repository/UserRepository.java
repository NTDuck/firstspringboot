package com.viettelsoftware.firstspringboot.repository;

import com.viettelsoftware.firstspringboot.entity.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<@NonNull User, @NonNull Long> {
    Optional<@NonNull User> findByKeycloakId(@NonNull String keycloakId);
}
