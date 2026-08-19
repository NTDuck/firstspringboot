package com.viettelsoftware.firstspringboot.repository.dto;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneOffset;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuditEventSpecifications {

    public static Specification<AuditEvent> hasDay(LocalDate day) {
        if (day == null) return null;

        val start = day
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        val end = day
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        return (root, query, builder) -> builder.and(
                builder.greaterThanOrEqualTo(
                        root.get("timestamp"), start),
                builder.lessThan(
                        root.get("timestamp"), end)
        );
    }

    public static Specification<AuditEvent> hasServiceName(String serviceName) {
        if (serviceName == null) return null;
        return (root, query, builder) ->
                builder.equal(root.get("serviceName"), serviceName);
    }

    public static Specification<AuditEvent> hasActorUserId(Long actorUserId) {
        if (actorUserId == null) return null;
        return (root, query, builder) ->
                builder.equal(root.get("actorUserId"), actorUserId);
    }

    public static Specification<AuditEvent> hasActorUsername(String actorUsername) {
        if (actorUsername == null) return null;
        return (root, query, builder) ->
                builder.equal(root.get("actorUsername"), actorUsername);
    }

    public static Specification<AuditEvent> hasAction(String action) {
        if (action == null) return null;
        return (root, query, builder) ->
                builder.equal(root.get("action"), action);
    }

    public static Specification<AuditEvent> hasResult(Boolean result) {
        if (result == null) return null;
        return (root, query, builder) ->
                builder.equal(root.get("result"), result);
    }
}