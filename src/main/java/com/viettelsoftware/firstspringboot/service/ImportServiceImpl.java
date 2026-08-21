package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.annotation.Auditable;
import com.viettelsoftware.firstspringboot.config.exception.InsufficientAuthorizationException;
import com.viettelsoftware.firstspringboot.controller.dto.CreateImportRequest;
import com.viettelsoftware.firstspringboot.controller.exception.ImportNotFoundException;
import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.ImportRepository;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.poi.ss.usermodel.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private static final Duration UPLOAD_URL_EXPIRATION = Duration.ofDays(1);

    private final ImportRepository importRepository;
    private final AuthenticationService authenticationService;
    private final Processor processor;
    private final ObjectStorageService objectStorageService;

    @Override
    @Auditable
    @Transactional
    public @NonNull Import create(@NonNull CreateImportRequest request) {
        validateAuthenticatedUser();

        val importEntity = Import.builder()
                .type(request.getType())
                .status(Import.Status.PENDING)
                .build();

        var saved = importRepository.save(importEntity);

        val objectKey = buildObjectKey(saved.getId());
        val presignedUrl = objectStorageService.createPresignedUploadUrl(objectKey, UPLOAD_URL_EXPIRATION);
        saved.setUrl(presignedUrl);
        saved = importRepository.save(saved);

        processor.process(saved.getId());
        return saved;
    }

    @Override
    @Auditable
    @Transactional(readOnly = true)
    public @NonNull Import getById(long id) {
        return importRepository.findById(id)
                .orElseThrow(() -> ImportNotFoundException.of(id));
    }

    @Override
    public void process(long id) {
        processor.process(id);
    }

    private void validateAuthenticatedUser() {
        if (authenticationService.getCurrentAuthenticatedUser().isEmpty()) {
            throw InsufficientAuthorizationException.builder()
                    .username("anonymous")
                    .operation("create import")
                    .build();
        }
    }

    private static String buildObjectKey(long importId) {
        return String.format("imports-%d.xlsx", importId);
    }

    @Slf4j
    @Component
    @RequiredArgsConstructor
    public static class Processor {

        private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

        private final ImportRepository importRepository;
        private final TaskRepository taskRepository;
        private final UserRepository userRepository;
        private final ObjectStorageService objectStorageService;

        @Value("${keycloak.admin.server-url:}")
        private String keycloakServerUrl;

        @Value("${keycloak.admin.realm:master}")
        private String adminRealm;

        @Value("${keycloak.admin.target-realm:firstspringbootrealm}")
        private String targetRealm;

        @Value("${keycloak.admin.username:admin}")
        private String adminUsername;

        @Value("${keycloak.admin.password:admin}")
        private String adminPassword;

        @Value("${keycloak.admin.client-id:admin-cli}")
        private String adminClientId;

        @Async
        public void process(long importId) {
            val objectKey = buildObjectKey(importId);

            if (!waitForFileUpload(objectKey)) return;

            val claimedImport = claimJob(importId);
            if (claimedImport.isEmpty()) return;

            val importEntity = claimedImport.get();
            val startTime = System.currentTimeMillis();

            try {
                if (!validateImportFile(importEntity.getType(), objectKey, importId)) {
                    markFailed(importEntity, startTime);
                    return;
                }

                upsertImportData(importEntity.getType(), objectKey);
                markSuccess(importEntity, startTime);

            } catch (Exception exception) {
                log.error("Error occurred while processing import `{}`: {}", importId, exception.getMessage(), exception);
                markFailed(importEntity, startTime);
            }
        }

        // MariaDB SELECT ... FOR UPDATE row-level lock for atomic job claiming
        @Transactional
        public Optional<Import> claimJob(long importId) {
            val importOptional = importRepository.findByIdForUpdate(importId);
            if (importOptional.isEmpty()) return Optional.empty();

            val importEntity = importOptional.get();
            if (importEntity.getStatus() != Import.Status.PENDING) return Optional.empty();

            importEntity.setStatus(Import.Status.PROCESSING);
            return Optional.of(importRepository.save(importEntity));
        }

        private boolean waitForFileUpload(String objectKey) {
            for (int i = 0; i < 50; i++) {
                if (objectStorageService.exists(objectKey)) return true;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return false;
        }

        private void markSuccess(Import importEntity, long startTime) {
            importEntity.setStatus(Import.Status.SUCCESS);
            importEntity.setCompletedAt(Instant.now());
            importRepository.save(importEntity);
        }

        private void markFailed(Import importEntity, long startTime) {
            importEntity.setStatus(Import.Status.FAILED);
            importEntity.setCompletedAt(Instant.now());
            importRepository.save(importEntity);
        }

        private boolean validateImportFile(Import.Type type, String objectKey, long importId) {
            try (InputStream inputStream = objectStorageService.get(objectKey)) {
                return validate(type, inputStream, importId);
            } catch (Exception exception) {
                log.warn("Import `{}` file reading failed: {}", importId, exception.getMessage());
                return false;
            }
        }

        private boolean validate(Import.Type type, InputStream inputStream, long importId) {
            try {
                val workbook = WorkbookFactory.create(inputStream);
                if (workbook.getNumberOfSheets() < 1) return false;

                val sheet = workbook.getSheetAt(0);
                if (sheet == null || sheet.getLastRowNum() < 1) return false;

                if (type == Import.Type.TASK) return validateTasks(sheet);
                if (type == Import.Type.USER) return validateUsers(sheet);

                return false;
            } catch (Exception exception) {
                log.warn("Import `{}` validation failed: {}", importId, exception.getMessage());
                return false;
            }
        }

        private boolean validateTasks(Sheet sheet) {
            val colMap = getHeaderColumnMap(sheet.getRow(0));
            if (!colMap.containsKey("description") && !colMap.containsKey("task")) return false;

            val descCol = colMap.getOrDefault("description", colMap.get("task"));
            val idCol = colMap.get("id");

            val rows = new ArrayList<TaskRowData>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                val row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                val desc = getCellValueAsString(row.getCell(descCol));
                val id = idCol != null ? parseLongOrNull(getCellValueAsString(row.getCell(idCol))) : null;

                if (!validateTaskRow(desc)) return false;

                rows.add(new TaskRowData(id, desc));
            }

            if (rows.isEmpty()) return false;
            return validateTaskCrossRow(rows);
        }

        private boolean validateTaskRow(String description) {
            if (description == null || description.isEmpty() || description.length() > 255) return false;
            if (Character.isWhitespace(description.charAt(0)) || Character.isWhitespace(description.charAt(description.length() - 1))) return false;
            return true;
        }

        private boolean validateTaskCrossRow(List<TaskRowData> rows) {
            val seenIds = new HashSet<Long>();
            for (val row : rows) {
                if (row.getId() != null && !seenIds.add(row.getId())) return false;
            }
            return true;
        }

        private boolean validateUsers(Sheet sheet) {
            val colMap = getHeaderColumnMap(sheet.getRow(0));
            val nameCol = colMap.containsKey("name") ? colMap.get("name") : colMap.get("username");
            if (nameCol == null) return false;

            val keycloakIdCol = colMap.getOrDefault("keycloak id", colMap.get("keycloakid"));
            val emailCol = colMap.get("email");
            val firstNameCol = colMap.getOrDefault("first name", colMap.get("firstname"));
            val lastNameCol = colMap.getOrDefault("last name", colMap.get("lastname"));
            val idCol = colMap.get("id");

            val rows = new ArrayList<UserRowData>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                val row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                val id = idCol != null ? parseLongOrNull(getCellValueAsString(row.getCell(idCol))) : null;
                val keycloakId = keycloakIdCol != null ? getCellValueAsString(row.getCell(keycloakIdCol)) : "";
                val name = getCellValueAsString(row.getCell(nameCol));
                val email = emailCol != null ? getCellValueAsString(row.getCell(emailCol)) : "";
                val firstName = firstNameCol != null ? getCellValueAsString(row.getCell(firstNameCol)) : "";
                val lastName = lastNameCol != null ? getCellValueAsString(row.getCell(lastNameCol)) : "";

                if (!validateUserRow(name, email, keycloakId, firstName, lastName)) return false;

                rows.add(new UserRowData(id, keycloakId, name, email, firstName, lastName));
            }

            if (rows.isEmpty()) return false;
            return validateUserCrossRow(rows);
        }

        private boolean validateUserRow(String name, String email, String keycloakId, String firstName, String lastName) {
            if (name == null || name.isBlank() || name.length() < 3 || name.length() > 255) return false;
            if (email != null && !email.isBlank() && (email.length() > 255 || !EMAIL_PATTERN.matcher(email).matches())) return false;
            if (keycloakId != null && keycloakId.length() > 255) return false;
            if (firstName != null && firstName.length() > 255) return false;
            if (lastName != null && lastName.length() > 255) return false;
            return true;
        }

        private boolean validateUserCrossRow(List<UserRowData> rows) {
            val seenIds = new HashSet<Long>();
            val seenKeycloakIds = new HashSet<String>();
            val seenNames = new HashSet<String>();
            val seenEmails = new HashSet<String>();

            for (val row : rows) {
                if (row.getId() != null && !seenIds.add(row.getId())) return false;
                if (row.getKeycloakId() != null && !row.getKeycloakId().isBlank() && !seenKeycloakIds.add(row.getKeycloakId())) return false;
                if (row.getName() != null && !row.getName().isBlank() && !seenNames.add(row.getName().toLowerCase())) return false;
                if (row.getEmail() != null && !row.getEmail().isBlank() && !seenEmails.add(row.getEmail().toLowerCase())) return false;
            }
            return true;
        }

        private void upsertImportData(Import.Type type, String objectKey) throws Exception {
            try (InputStream inputStream = objectStorageService.get(objectKey)) {
                val workbook = WorkbookFactory.create(inputStream);
                val sheet = workbook.getSheetAt(0);

                if (type == Import.Type.TASK) {
                    upsertTasks(sheet);
                } else if (type == Import.Type.USER) {
                    upsertUsers(sheet);
                }
            }
        }

        private void upsertTasks(Sheet sheet) {
            val colMap = getHeaderColumnMap(sheet.getRow(0));
            val descCol = colMap.getOrDefault("description", colMap.get("task"));
            val idCol = colMap.get("id");

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                val row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                val desc = getCellValueAsString(row.getCell(descCol));
                val id = idCol != null ? parseLongOrNull(getCellValueAsString(row.getCell(idCol))) : null;

                upsertTask(id, desc);
            }
        }

        private void upsertTask(Long id, String description) {
            if (id != null && taskRepository.existsById(id)) {
                val existing = taskRepository.findById(id).orElseThrow();
                existing.setDescription(description);
                taskRepository.save(existing);
                return;
            }

            val newTask = Task.builder().description(description).build();
            taskRepository.save(newTask);
        }

        private void upsertUsers(Sheet sheet) {
            val colMap = getHeaderColumnMap(sheet.getRow(0));
            val nameCol = colMap.containsKey("name") ? colMap.get("name") : colMap.get("username");
            val keycloakIdCol = colMap.getOrDefault("keycloak id", colMap.get("keycloakid"));
            val emailCol = colMap.get("email");
            val firstNameCol = colMap.getOrDefault("first name", colMap.get("firstname"));
            val lastNameCol = colMap.getOrDefault("last name", colMap.get("lastname"));
            val idCol = colMap.get("id");

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                val row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                val id = idCol != null ? parseLongOrNull(getCellValueAsString(row.getCell(idCol))) : null;
                val keycloakId = keycloakIdCol != null ? getCellValueAsString(row.getCell(keycloakIdCol)) : "";
                val name = getCellValueAsString(row.getCell(nameCol));
                val email = emailCol != null ? getCellValueAsString(row.getCell(emailCol)) : "";
                val firstName = firstNameCol != null ? getCellValueAsString(row.getCell(firstNameCol)) : "";
                val lastName = lastNameCol != null ? getCellValueAsString(row.getCell(lastNameCol)) : "";

                upsertUser(id, keycloakId, name, email, firstName, lastName);
            }
        }

        private void upsertUser(Long id, String keycloakId, String name, String email, String firstName, String lastName) {
            val resolvedKeycloakId = keycloakId.isBlank() ? UUID.randomUUID().toString() : keycloakId;

            syncUserToKeycloak(resolvedKeycloakId, name, email, firstName, lastName);

            userRepository.findByKeycloakId(resolvedKeycloakId)
                    .map(existing -> {
                        existing.setName(name);
                        existing.setEmail(email);
                        existing.setFirstName(firstName);
                        existing.setLastName(lastName);
                        return userRepository.save(existing);
                    })
                    .orElseGet(() -> {
                        val newUser = User.builder()
                                .keycloakId(resolvedKeycloakId)
                                .name(name)
                                .email(email)
                                .firstName(firstName)
                                .lastName(lastName)
                                .build();
                        return userRepository.save(newUser);
                    });
        }

        private void syncUserToKeycloak(String keycloakId, String username, String email, String firstName, String lastName) {
            if (keycloakServerUrl == null || keycloakServerUrl.isBlank()) return;

            try {
                val keycloak = KeycloakBuilder.builder()
                        .serverUrl(keycloakServerUrl)
                        .realm(adminRealm)
                        .username(adminUsername)
                        .password(adminPassword)
                        .clientId(adminClientId)
                        .build();

                val userRep = new UserRepresentation();
                userRep.setUsername(username);
                userRep.setEmail(email);
                userRep.setFirstName(firstName);
                userRep.setLastName(lastName);
                userRep.setEnabled(true);

                try (Response response = keycloak.realm(targetRealm).users().create(userRep)) {
                    // Created or existing
                }
            } catch (Exception ignored) {
                // Best-effort sync
            }
        }

        private Map<String, Integer> getHeaderColumnMap(Row headerRow) {
            val map = new HashMap<String, Integer>();
            if (headerRow == null) return map;

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                val cell = headerRow.getCell(i);
                if (cell == null) continue;

                val val = getCellValueAsString(cell).trim().toLowerCase();
                if (!val.isEmpty()) {
                    map.put(val, i);
                }
            }
            return map;
        }

        private String getCellValueAsString(Cell cell) {
            if (cell == null) return "";

            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    val num = cell.getNumericCellValue();
                    if (num == (long) num) return String.valueOf((long) num);
                    return String.valueOf(num);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return cell.getStringCellValue().trim();
                    } catch (Exception e) {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                default:
                    return "";
            }
        }

        private boolean isRowEmpty(Row row) {
            for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                val cell = row.getCell(c);
                if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValueAsString(cell).isBlank()) {
                    return false;
                }
            }
            return true;
        }

        private Long parseLongOrNull(String str) {
            if (str == null || str.isBlank()) return null;
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @lombok.Getter
        @lombok.AllArgsConstructor
        private static class TaskRowData {
            private final Long id;
            private final String description;
        }

        @lombok.Getter
        @lombok.AllArgsConstructor
        private static class UserRowData {
            private final Long id;
            private final String keycloakId;
            private final String name;
            private final String email;
            private final String firstName;
            private final String lastName;
        }
    }
}
