package com.viettelsoftware.firstspringboot.repository;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditEventRepository extends JpaRepository<@NonNull AuditEvent, @NonNull Long> {

    @Query("SELECT a FROM AuditEvent a WHERE " +
           "(:startOfDay IS NULL OR a.timestamp >= :startOfDay) AND " +
           "(:endOfDay IS NULL OR a.timestamp < :endOfDay) AND " +
           "(:serviceName IS NULL OR a.serviceName = :serviceName) AND " +
           "(:actorUserId IS NULL OR a.actorUserId = :actorUserId) AND " +
           "(:actorUsername IS NULL OR a.actorUsername = :actorUsername) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:result IS NULL OR a.result = :result)")
    @NonNull Page<@NonNull AuditEvent> searchAuditEvents(
            @Param("startOfDay") @Nullable Instant startOfDay,
            @Param("endOfDay") @Nullable Instant endOfDay,
            @Param("serviceName") @Nullable String serviceName,
            @Param("actorUserId") @Nullable Long actorUserId,
            @Param("actorUsername") @Nullable String actorUsername,
            @Param("action") @Nullable String action,
            @Param("result") @Nullable Boolean result,
            @NonNull Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.timestamp >= :startOfDay AND a.timestamp < :endOfDay")
    @NonNull Page<@NonNull AuditEvent> findByTimestampBetween(
            @Param("startOfDay") @NonNull Instant startOfDay,
            @Param("endOfDay") @NonNull Instant endOfDay,
            @NonNull Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.serviceName = :serviceName")
    @NonNull Page<@NonNull AuditEvent> findByServiceName(
            @Param("serviceName") @NonNull String serviceName,
            @NonNull Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.actorUserId = :actorUserId")
    @NonNull Page<@NonNull AuditEvent> findByActorUserId(
            @Param("actorUserId") @NonNull Long actorUserId,
            @NonNull Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.actorUsername = :actorUsername")
    @NonNull Page<@NonNull AuditEvent> findByActorUsername(
            @Param("actorUsername") @NonNull String actorUsername,
            @NonNull Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.action = :action")
    @NonNull Page<@NonNull AuditEvent> findByAction(
            @Param("action") @NonNull String action,
            @NonNull Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.result = :result")
    @NonNull Page<@NonNull AuditEvent> findByResult(
            @Param("result") @NonNull Boolean result,
            @NonNull Pageable pageable);
}
