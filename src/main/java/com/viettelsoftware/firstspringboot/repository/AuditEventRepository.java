package com.viettelsoftware.firstspringboot.repository;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEventRepository extends
        JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent> { }
