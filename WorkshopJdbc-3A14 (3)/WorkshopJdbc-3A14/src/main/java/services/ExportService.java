package services;

import entities.Employee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    public static void exportToExcel(List<Employee> employees, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "First Name", "Last Name", "Phone", "Position"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Alternating row colors
        CellStyle altRowStyle = workbook.createCellStyle();
        altRowStyle.cloneStyleFrom(dataStyle);
        altRowStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Fill data
        int rowNum = 1;
        for (Employee emp : employees) {
            Row row = sheet.createRow(rowNum);
            CellStyle rowStyle = (rowNum % 2 == 0) ? altRowStyle : dataStyle;

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(emp.getId());
            cell0.setCellStyle(rowStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(emp.getFirstName());
            cell1.setCellStyle(rowStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(emp.getLastName());
            cell2.setCellStyle(rowStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(emp.getPhone());
            cell3.setCellStyle(rowStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(emp.getPosition());
            cell4.setCellStyle(rowStyle);

            rowNum++;
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public static String generateFileName() {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "Employees_Export_" + timestamp + ".xlsx";
    }
}
