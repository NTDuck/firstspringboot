package com.viettelsoftware.firstspringboot.repository;

import com.viettelsoftware.firstspringboot.entity.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportRepository extends JpaRepository<Import, Long> { }
