package com.viettelsoftware.firstspringboot.services.repositories;

import com.viettelsoftware.firstspringboot.entities.Task;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<@NonNull Task, @NonNull Long> { }
