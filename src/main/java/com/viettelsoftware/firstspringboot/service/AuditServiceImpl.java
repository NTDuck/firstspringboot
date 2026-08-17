package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.AuditQuery;
import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.repository.AuditEventRepository;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuthService authService;

    @Override
    public void audit(@NonNull AuditEvent event) {
        auditEventRepository.save(event);
    }

    @Override
    public @NonNull Page<@NonNull AuditEvent> search(@NonNull AuditQuery query, @NonNull Pageable pageable) {
        val specification = buildSpecificationFromQuery(query);
        return auditEventRepository.findAll(specification, pageable);
    }

    private @NonNull Specification<@NonNull AuditEvent> buildSpecificationFromQuery(@NonNull AuditQuery query) {
        return (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getDay() != null) {
                Instant startOfDay = query.getDay().atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant endOfDay = query.getDay().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(builder.between(root.get("timestamp"), startOfDay, endOfDay));
            }

            if (query.getServiceName() != null && !query.getServiceName().isBlank()) {
                predicates.add(builder.equal(root.get("serviceName"), query.getServiceName()));
            }

            if (query.getActorUserId() != null) {
                predicates.add(builder.equal(root.get("actorUserId"), query.getActorUserId()));
            }

            if (query.getActorUsername() != null && !query.getActorUsername().isBlank()) {
                predicates.add(builder.equal(root.get("actorUsername"), query.getActorUsername()));
            }

            if (query.getAction() != null && !query.getAction().isBlank()) {
                predicates.add(builder.equal(root.get("action"), query.getAction()));
            }

            if (query.getResult() != null) {
                predicates.add(builder.equal(root.get("result"), query.getResult()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
