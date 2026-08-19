package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.repository.AuditEventRepository;
import com.viettelsoftware.firstspringboot.repository.dto.AuditEventSpecifications;
import com.viettelsoftware.firstspringboot.service.dto.AuditEventsQueryFilterDto;
import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import com.viettelsoftware.firstspringboot.service.exception.CurrentAuthenticatedUserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
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
        val user = getAuthenticatedUser();
        val auditEvent = createAuditEvent(serviceName, action, user, true, null);

        auditEventRepository.save(auditEvent);
    }

    @Override
    @Transactional
    public void auditFailure(String serviceName, String action, @Nullable Exception exception) throws CurrentAuthenticatedUserNotFoundException {
        val user = getAuthenticatedUser();
        val exceptionMessage = exception != null ? exception.getMessage() : null;
        val auditEvent = createAuditEvent(serviceName, action, user, false, exceptionMessage);

        auditEventRepository.save(auditEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditEvents(AuditEventsQueryFilterDto filter, Pageable pageable) {
        val specification = buildSpecification(filter);
        return auditEventRepository.findAll(specification, pageable);
    }

    private AuthenticatedUserDto getAuthenticatedUser() {
        return authenticationService.getCurrentAuthenticatedUser()
                .orElseThrow(CurrentAuthenticatedUserNotFoundException::of);
    }

    private AuditEvent createAuditEvent(String serviceName, String action, AuthenticatedUserDto user, boolean result, @Nullable String exception) {
        return AuditEvent.builder()
                .service(AuditEvent.Service.builder()
                        .name(serviceName)
                        .build())
                .actor(AuditEvent.Actor.builder()
                        .userId(user.getId())
                        .username(user.getName())
                        .build())
                .action(action)
                .result(result)
                .exception(exception)
                .build();
    }

    private Specification<AuditEvent> buildSpecification(AuditEventsQueryFilterDto filter) {
        return Specification
                .where(AuditEventSpecifications.hasDay(filter.getDay()))
                .and(AuditEventSpecifications.hasServiceName(filter.getServiceName()))
                .and(AuditEventSpecifications.hasActorUserId(filter.getActorUserId()))
                .and(AuditEventSpecifications.hasActorUsername(filter.getActorUsername()))
                .and(AuditEventSpecifications.hasAction(filter.getAction()))
                .and(AuditEventSpecifications.hasResult(filter.getResult()));
    }
}
