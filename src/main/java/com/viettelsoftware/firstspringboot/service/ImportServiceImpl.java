package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CreateImportRequest;
import com.viettelsoftware.firstspringboot.service.model.AuthenticatedUser;
import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.controller.exception.ImportNotFoundException;
import com.viettelsoftware.firstspringboot.controller.exception.InsufficientAuthorizationException;
import com.viettelsoftware.firstspringboot.repository.ImportRepository;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import lombok.*;
import org.apache.poi.ss.usermodel.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ImportServiceImpl implements ImportService {

    @Autowired
    private ImportRepository importRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private Processor processor;

    @Autowired
    private ObjectStorageService objectStorageService;

    @Override
    public @NonNull Import create(@NonNull CreateImportRequest request) {
        AuthenticatedUser user = authenticationService.getCurrentAuthenticatedUser();
        if (user == null) {
            throw new InsufficientAuthorizationException("anonymous", "create import");
        }

        Import.RequestedBy requestedBy = Import.RequestedBy.builder()
                .username(user.getName())
                .userId(user.getId())
                .build();

        Import importEntity = Import.builder()
                .type(request.getType())
                .status(Import.Status.PENDING)
                .requestedBy(requestedBy)
                .build();

        Import saved = importRepository.save(importEntity);

        String objectKey = String.format("imports-%d.xlsx", saved.getId());
        String presignedUrl = objectStorageService.createPresignedUploadUrl(objectKey, Duration.ofDays(1));
        saved.setUrl(presignedUrl);
        saved = importRepository.save(saved);

        processor.process(saved.getId());
        return saved;
    }

    @Override
    public @NonNull Import getById(long id) {
        return importRepository.findById(id)
                .orElseThrow(() -> ImportNotFoundException.of(id));
    }

    @Override
    public void process(long id) {
        processor.process(id);
    }

    @Component
    public static class Processor {

        private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

        private final @NonNull Logger logger = LoggerFactory.getLogger(this.getClass());

        @Autowired
        private ImportRepository importRepository;

        @Autowired
        private TaskRepository taskRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ObjectStorageService objectStorageService;

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
            Optional<Import> importOptional = importRepository.findById(importId);
            if (importOptional.isEmpty()) {
                return;
            }

            Import importEntity = importOptional.get();
            String objectKey = String.format("imports-%d.xlsx", importId);

            // Wait for the file to finish uploading
            boolean fileUploaded = false;
            for (int i = 0; i < 50; i++) {
                if (objectStorageService.exists(objectKey)) {
                    fileUploaded = true;
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (!fileUploaded) {
                return;
            }

            importEntity.setStatus(Import.Status.PROCESSING);
            importRepository.save(importEntity);

            long startTime = System.currentTimeMillis();
            try (InputStream inputStream = objectStorageService.get(objectKey)) {
                boolean isValid = validate(importEntity.getType(), inputStream, importEntity);
                if (!isValid) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    importEntity.setStatus(Import.Status.FAILED);
                    importEntity.setCompletedAt(Instant.now());
                    importEntity.setTimeElapsed(elapsed);
                    importRepository.save(importEntity);
                    return;
                }

                // Re-open stream for execution/upsert
                try (InputStream streamForUpsert = objectStorageService.get(objectKey)) {
                    upsertData(importEntity.getType(), streamForUpsert);
                }

                long elapsed = System.currentTimeMillis() - startTime;
                importEntity.setStatus(Import.Status.SUCCESS);
                importEntity.setCompletedAt(Instant.now());
                importEntity.setTimeElapsed(elapsed);
                importRepository.save(importEntity);

            } catch (Exception exception) {
                logger.error("Error occurred while processing import `{}`: {}", importId, exception.getMessage(), exception);
                long elapsed = System.currentTimeMillis() - startTime;
                importEntity.setStatus(Import.Status.FAILED);
                importEntity.setCompletedAt(Instant.now());
                importEntity.setTimeElapsed(elapsed);
                importRepository.save(importEntity);
            }
        }

        private boolean validate(Import.Type type, InputStream inputStream, Import importEntity) {
            try {
                Workbook workbook = validateFileFormat(inputStream);
                if (workbook == null) {
                    return false;
                }

                Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
                if (sheet == null || sheet.getLastRowNum() < 1) {
                    return false;
                }

                if (type == Import.Type.TASK) {
                    return validateTasks(sheet);
                } else if (type == Import.Type.USER) {
                    return validateUsers(sheet);
                }
                return false;
            } catch (Exception exception) {
                logger.warn("Import `{}` validation failed: {}", importEntity.getId(), exception.getMessage());
                return false;
            }
        }

        private Workbook validateFileFormat(InputStream inputStream) {
            try {
                return WorkbookFactory.create(inputStream);
            } catch (Exception exception) {
                logger.warn("Malformed or corrupted file format: {}", exception.getMessage());
                return null;
            }
        }

        private boolean validateTasks(Sheet sheet) {
            Map<String, Integer> colMap = getHeaderColumnMap(sheet.getRow(0));
            if (!colMap.containsKey("description") && !colMap.containsKey("task")) {
                return false;
            }

            int descCol = colMap.getOrDefault("description", colMap.get("task"));
            Integer idCol = colMap.get("id");

            List<TaskRowData> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                String desc = getCellValueAsString(row.getCell(descCol));
                Long id = idCol != null ? parseLongOrNull(getCellValueAsString(row.getCell(idCol))) : null;

                if (!validateTaskRow(desc)) {
                    return false;
                }

                rows.add(new TaskRowData(id, desc));
            }

            if (rows.isEmpty()) {
                return false;
            }

            return validateTaskCrossRow(rows);
        }

        private boolean validateTaskRow(String description) {
            if (description == null || description.isEmpty() || description.length() > 255) {
                return false;
            }
            if (Character.isWhitespace(description.charAt(0)) || Character.isWhitespace(description.charAt(description.length() - 1))) {
                return false;
            }
            return true;
        }

        private boolean validateTaskCrossRow(List<TaskRowData> rows) {
            Set<Long> seenIds = new HashSet<>();
            for (TaskRowData row : rows) {
                if (row.id != null) {
                    if (!seenIds.add(row.id)) {
                        return false; // Duplicate ID across rows
                    }
                }
            }
            return true;
        }

        private boolean validateUsers(Sheet sheet) {
            Map<String, Integer> colMap = getHeaderColumnMap(sheet.getRow(0));
            Integer nameCol = colMap.containsKey("name") ? colMap.get("name") : colMap.get("username");
            if (nameCol == null) {
                return false;
            }

            Integer keycloakIdCol = colMap.get("keycloak id");
            if (keycloakIdCol == null) {
                keycloakIdCol = colMap.get("keycloakid");
            }
            Integer emailCol = colMap.get("email");
            Integer firstNameCol = colMap.get("first name");
            if (firstNameCol == null) {
                firstNameCol = colMap.get("firstname");
            }
            Integer lastNameCol = colMap.get("last name");
            if (lastNameCol == null) {
                lastNameCol = colMap.get("lastname");
            }
            Integer idCol = colMap.get("id");

            List<UserRowData> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                Long id = idCol != null ? parseLongOrNull(getCellValueAsString(row.getCell(idCol))) : null;
                String keycloakId = keycloakIdCol != null ? getCellValueAsString(row.getCell(keycloakIdCol)) : "";
                String name = getCellValueAsString(row.getCell(nameCol));
                String email = emailCol != null ? getCellValueAsString(row.getCell(emailCol)) : "";
                String firstName = firstNameCol != null ? getCellValueAsString(row.getCell(firstNameCol)) : "";
                String lastName = lastNameCol != null ? getCellValueAsString(row.getCell(lastNameCol)) : "";

                if (!validateUserRow(name, email, keycloakId, firstName, lastName)) {
                    return false;
                }

                rows.add(new UserRowData(id, keycloakId, name, email, firstName, lastName));
            }

            if (rows.isEmpty()) {
                return false;
            }

            return validateUserCrossRow(rows);
        }

        private boolean validateUserRow(String name, String email, String keycloakId, String firstName, String lastName) {
            if (name == null || name.isBlank() || name.length() < 3 || name.length() > 255) {
                return false;
            }
            if (email != null && !email.isBlank()) {
                if (email.length() > 255 || !EMAIL_PATTERN.matcher(email).matches()) {
                    return false;
                }
            }
            if (keycloakId != null && keycloakId.length() > 255) {
                return false;
            }
            if (firstName != null && firstName.length() > 255) {
                return false;
            }
            if (lastName != null && lastName.length() > 255) {
                return false;
            }
            return true;
        }

        private boolean validateUserCrossRow(List<UserRowData> rows) {
            Set<Long> seenIds = new HashSet<>();
            Set<String> seenKeycloakIds = new HashSet<>();
            Set<String> seenNames = new HashSet<>();
            Set<String> seenEmails = new HashSet<>();

            for (UserRowData row : rows) {
                if (row.id != null) {
                    if (!seenIds.add(row.id)) {
                        return false;
                    }
                }
                if (row.keycloakId != null && !row.keycloakId.isBlank()) {
                    if (!seenKeycloakIds.add(row.keycloakId)) {
                        return false;
                    }
                }
                if (row.name != null && !row.name.isBlank()) {
                    if (!seenNames.add(row.name.toLowerCase())) {
                        return false;
                    }
                }
                if (row.email != null && !row.email.isBlank()) {
                    if (!seenEmails.add(row.email.toLowerCase())) {
                        return false;
                    }
                }
            }
            return true;
        }

        private void upsertData(Import.Type type, InputStream inputStream) throws Exception {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            if (type == Import.Type.TASK) {
                Map<String, Integer> colMap = getHeaderColumnMap(sheet.getRow(0));
                int descCol = colMap.getOrDefault("description", colMap.get("task"));
                Integer idCol = colMap.get("id");

                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null || isRowEmpty(row)) continue;

                    String desc = getCellValueAsString(row.getCell(descCol));
                    Long id = idCol != null ? parseLongOrNull(getCellValueAsString(row.getCell(idCol))) : null;

                    if (id != null && taskRepository.existsById(id)) {
                        Task existing = taskRepository.findById(id).orElseThrow();
                        existing.setDescription(desc);
                        taskRepository.save(existing);
                    } else {
                        Task newTask = Task.builder().description(desc).build();
                        taskRepository.save(newTask);
                    }
                }
            } else if (type == Import.Type.USER) {
                Map<String, Integer> colMap = getHeaderColumnMap(sheet.getRow(0));
                int nameCol = colMap.containsKey("name") ? colMap.get("name") : colMap.get("username");
                Integer keycloakIdCol = colMap.get("keycloak id");
                if (keycloakIdCol == null) keycloakIdCol = colMap.get("keycloakid");
                Integer emailCol = colMap.get("email");
                Integer firstNameCol = colMap.get("first name");
                if (firstNameCol == null) firstNameCol = colMap.get("firstname");
                Integer lastNameCol = colMap.get("last name");
                if (lastNameCol == null) lastNameCol = colMap.get("lastname");
                Integer idCol = colMap.get("id");

                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null || isRowEmpty(row)) continue;

                    Long id = idCol != null ? parseLongOrNull(getCellValueAsString(row.getCell(idCol))) : null;
                    String keycloakId = keycloakIdCol != null ? getCellValueAsString(row.getCell(keycloakIdCol)) : "";
                    String name = getCellValueAsString(row.getCell(nameCol));
                    String email = emailCol != null ? getCellValueAsString(row.getCell(emailCol)) : "";
                    String firstName = firstNameCol != null ? getCellValueAsString(row.getCell(firstNameCol)) : "";
                    String lastName = lastNameCol != null ? getCellValueAsString(row.getCell(lastNameCol)) : "";

                    String resolvedKeycloakId = keycloakId;
                    if (resolvedKeycloakId.isBlank()) {
                        resolvedKeycloakId = UUID.randomUUID().toString();
                    }

                    // Sync to Keycloak if client configured
                    syncUserToKeycloak(resolvedKeycloakId, name, email, firstName, lastName);

                    final String finalKeycloakId = resolvedKeycloakId;
                    userRepository.findByKeycloakId(finalKeycloakId)
                            .map(existing -> {
                                existing.setName(name);
                                existing.setEmail(email);
                                existing.setFirstName(firstName);
                                existing.setLastName(lastName);
                                return userRepository.save(existing);
                            })
                            .orElseGet(() -> {
                                User newUser = User.builder()
                                        .keycloakId(finalKeycloakId)
                                        .name(name)
                                        .email(email)
                                        .firstName(firstName)
                                        .lastName(lastName)
                                        .build();
                                return userRepository.save(newUser);
                            });
                }
            }
        }

        private void syncUserToKeycloak(String keycloakId, String username, String email, String firstName, String lastName) {
            if (keycloakServerUrl == null || keycloakServerUrl.isBlank()) {
                return;
            }
            try {
                Keycloak keycloak = KeycloakBuilder.builder()
                        .serverUrl(keycloakServerUrl)
                        .realm(adminRealm)
                        .username(adminUsername)
                        .password(adminPassword)
                        .clientId(adminClientId)
                        .build();

                UserRepresentation userRep = new UserRepresentation();
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
            Map<String, Integer> map = new HashMap<>();
            if (headerRow == null) return map;
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String val = getCellValueAsString(cell).trim().toLowerCase();
                    if (!val.isEmpty()) {
                        map.put(val, i);
                    }
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
                    double num = cell.getNumericCellValue();
                    if (num == (long) num) {
                        return String.valueOf((long) num);
                    }
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
                Cell cell = row.getCell(c);
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

        private static class TaskRowData {
            private final Long id;
            private final String description;

            public TaskRowData(Long id, String description) {
                this.id = id;
                this.description = description;
            }
        }

        private static class UserRowData {
            private final Long id;
            private final String keycloakId;
            private final String name;
            private final String email;
            private final String firstName;
            private final String lastName;

            public UserRowData(Long id, String keycloakId, String name, String email, String firstName, String lastName) {
                this.id = id;
                this.keycloakId = keycloakId;
                this.name = name;
                this.email = email;
                this.firstName = firstName;
                this.lastName = lastName;
            }
        }
    }
}
