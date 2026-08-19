package com.viettelsoftware.firstspringboot.repository;

import com.viettelsoftware.firstspringboot.entity.Export;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportRepository extends JpaRepository<Export, Long> { }
