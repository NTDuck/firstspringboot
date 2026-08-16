package com.viettelsoftware.firstspringboot.scheduler;

import com.viettelsoftware.firstspringboot.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakUserSyncCronjobTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private KeycloakUserSyncCronjob cronjob;

    @Test
    void testSyncKeycloakUsersHandledGracefullyWhenServerDown() {
        ReflectionTestUtils.setField(cronjob, "keycloakServerUrl", "http://localhost:9999");
        ReflectionTestUtils.setField(cronjob, "adminRealm", "master");
        ReflectionTestUtils.setField(cronjob, "targetRealm", "firstspringbootrealm");
        ReflectionTestUtils.setField(cronjob, "adminUsername", "admin");
        ReflectionTestUtils.setField(cronjob, "adminPassword", "pass");
        ReflectionTestUtils.setField(cronjob, "adminClientId", "admin-cli");

        assertDoesNotThrow(() -> cronjob.syncKeycloakUsers());
        verify(userService, never()).createUser(any());
    }
}
