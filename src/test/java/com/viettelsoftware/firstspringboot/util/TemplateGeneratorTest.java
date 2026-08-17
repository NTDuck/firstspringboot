package com.viettelsoftware.firstspringboot.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

class TemplateGeneratorTest {

    @Test
    void generateTemplates() throws Exception {
        File dir = new File("src/main/resources/templates");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate tasks_template.xlsx
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks");
            Row r0 = sheet.createRow(0);
            Cell c00 = r0.createCell(0);
            c00.setCellValue("ID");
            Cell c01 = r0.createCell(1);
            c01.setCellValue("Description");

            Row r1 = sheet.createRow(1);
            Cell c10 = r1.createCell(0);
            c10.setCellValue("${task.id}");
            Cell c11 = r1.createCell(1);
            c11.setCellValue("${task.description}");

            Drawing<?> drawing = sheet.createDrawingPatriarch();
            CreationHelper factory = workbook.getCreationHelper();

            ClientAnchor a0 = factory.createClientAnchor();
            a0.setCol1(0); a0.setCol2(2); a0.setRow1(0); a0.setRow2(2);
            Comment com0 = drawing.createCellComment(a0);
            com0.setString(factory.createRichTextString("jx:area(lastCell=\"B2\")"));
            c00.setCellComment(com0);

            ClientAnchor a1 = factory.createClientAnchor();
            a1.setCol1(0); a1.setCol2(2); a1.setRow1(1); a1.setRow2(3);
            Comment com1 = drawing.createCellComment(a1);
            com1.setString(factory.createRichTextString("jx:each(items=\"tasks\" var=\"task\" lastCell=\"B2\")"));
            c10.setCellComment(com1);

            try (FileOutputStream fos = new FileOutputStream(new File(dir, "tasks_template.xlsx"))) {
                workbook.write(fos);
            }
        }

        // Generate users_template.xlsx
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");
            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("ID");
            r0.createCell(1).setCellValue("Keycloak ID");
            r0.createCell(2).setCellValue("Name");
            r0.createCell(3).setCellValue("Email");
            r0.createCell(4).setCellValue("First Name");
            r0.createCell(5).setCellValue("Last Name");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("${user.id}");
            r1.createCell(1).setCellValue("${user.keycloakId}");
            r1.createCell(2).setCellValue("${user.name}");
            r1.createCell(3).setCellValue("${user.email}");
            r1.createCell(4).setCellValue("${user.firstName}");
            r1.createCell(5).setCellValue("${user.lastName}");

            Drawing<?> drawing = sheet.createDrawingPatriarch();
            CreationHelper factory = workbook.getCreationHelper();

            ClientAnchor a0 = factory.createClientAnchor();
            a0.setCol1(0); a0.setCol2(2); a0.setRow1(0); a0.setRow2(2);
            Comment com0 = drawing.createCellComment(a0);
            com0.setString(factory.createRichTextString("jx:area(lastCell=\"F2\")"));
            r0.getCell(0).setCellComment(com0);

            ClientAnchor a1 = factory.createClientAnchor();
            a1.setCol1(0); a1.setCol2(2); a1.setRow1(1); a1.setRow2(3);
            Comment com1 = drawing.createCellComment(a1);
            com1.setString(factory.createRichTextString("jx:each(items=\"users\" var=\"user\" lastCell=\"F2\")"));
            r1.getCell(0).setCellComment(com1);

            try (FileOutputStream fos = new FileOutputStream(new File(dir, "users_template.xlsx"))) {
                workbook.write(fos);
            }
        }
    }
}
