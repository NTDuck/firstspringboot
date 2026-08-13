package com.viettelsoftware.firstspringboot.task.repository;

import com.viettelsoftware.firstspringboot.task.entity.Task;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<@NonNull Task, @NonNull Long> { }
