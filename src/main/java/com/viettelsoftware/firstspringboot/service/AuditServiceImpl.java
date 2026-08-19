package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.repository.AuditEventRepository;
import com.viettelsoftware.firstspringboot.repository.dto.AuditEventSpecifications;
import com.viettelsoftware.firstspringboot.service.dto.AuditEventsQueryFilterDto;
import com.viettelsoftware.firstspringboot.service.exception.CurrentAuthenticatedUserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository auditEventRepository;

    private final AuthenticationService authenticationService;

    @Override
    @Transactional
    public void auditSuccess(String serviceName, String action) throws CurrentAuthenticatedUserNotFoundException {
        val user = authenticationService.getCurrentAuthenticatedUser()
                .orElseThrow(CurrentAuthenticatedUserNotFoundException::of);

        val auditEvent = AuditEvent.builder()
                .service(AuditEvent.Service.builder()
                        .name(serviceName)
                        .build())
                .actor(AuditEvent.Actor.builder()
                        .userId(user.getId())
                        .username(user.getName())
                        .build())
                .action(action)
                .result(true)
                .exception(null)
                .build();

        auditEventRepository.save(auditEvent);
    }

    @Override
    @Transactional
    public void auditFailure(String serviceName, String action, Exception exception) throws CurrentAuthenticatedUserNotFoundException {
        val user = authenticationService.getCurrentAuthenticatedUser()
                .orElseThrow(CurrentAuthenticatedUserNotFoundException::of);

        val auditEvent = AuditEvent.builder()
                .service(AuditEvent.Service.builder()
                        .name(serviceName)
                        .build())
                .actor(AuditEvent.Actor.builder()
                        .userId(user.getId())
                        .username(user.getName())
                        .build())
                .action(action)
                .result(true)
                .exception(null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditEvents(AuditEventsQueryFilterDto filter, Pageable pageable) {
        val specification = Specification
                .where(AuditEventSpecifications.hasDay(
                        filter.getDay()))
                .and(AuditEventSpecifications.hasServiceName(
                        filter.getServiceName()))
                .and(AuditEventSpecifications.hasActorUserId(
                        filter.getActorUserId()))
                .and(AuditEventSpecifications.hasActorUsername(
                        filter.getActorUsername()))
                .and(AuditEventSpecifications.hasAction(
                        filter.getAction()))
                .and(AuditEventSpecifications.hasResult(
                        filter.getResult()));

        return auditEventRepository.findAll(specification, pageable);
    }
}
