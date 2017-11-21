package com.ontotext.ehri.tools;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class ExcelReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelReader.class);
    private static final DataFormatter FORMATTER = new DataFormatter();

    private static String parseCell(Cell cell) {
        return FORMATTER.formatCellValue(cell);
    }

    private static String[] parseRow(Row row) {
        String[] cells = new String[row.getLastCellNum()];
        for (int i = 0; i < cells.length; i++) cells[i] = parseCell(row.getCell(i));
        return cells;
    }

    private static String[] parseRow(Row row, int numColumns) {
        String[] cells = new String[numColumns];

        for (int i = 0; i < cells.length; i++) {
            if (i < row.getLastCellNum()) cells[i] = parseCell(row.getCell(i));
            else cells[i] = "";
        }

        return cells;
    }

    private static boolean isValidCell(Cell cell) {

        switch (cell.getCellTypeEnum()) {
            case STRING: return true;
            case NUMERIC: return true;
            case BOOLEAN: return true;
            default: return false;
        }
    }

    private static int lastCellIndex(Row row) {
        for (int i = row.getLastCellNum() - 1; i >= 0; i--) if (isValidCell(row.getCell(i))) return i;
        return 0;
    }

    private static int numColumns(Sheet sheet) {
        int maxLastCellIndex = 0;

        for (Row row : sheet) {
            int lastCellIndex = lastCellIndex(row);
            if (lastCellIndex > maxLastCellIndex) maxLastCellIndex = lastCellIndex;
        }

        return maxLastCellIndex + 1;
    }

    private static String[][] parseSheet(Sheet sheet) {
        int numColumns = numColumns(sheet);
        String[][] rows = new String[sheet.getLastRowNum() + 1][];
        for (int i = 0; i < rows.length; i++) rows[i] = parseRow(sheet.getRow(i), numColumns);
        return rows;
    }

    public static String[][] readSheet(String excelPath, int sheetIndex) {
        InputStream inputStream = TextReader.openInputStream(excelPath);
        if (inputStream == null) {
            LOGGER.error("failed to open Excel file: " + excelPath);
            return null;
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            return parseSheet(workbook.getSheetAt(sheetIndex));
        } catch (InvalidFormatException | IOException e) {
            LOGGER.error("failed to read sheet at index " + sheetIndex + " from: " + excelPath, e);
            return null;
        }
    }

    public static String[][][] readSheets(String excelPath) {
        InputStream inputStream = TextReader.openInputStream(excelPath);
        if (inputStream == null) {
            LOGGER.error("failed to open Excel file: " + excelPath);
            return null;
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            String[][][] sheets = new String[workbook.getNumberOfSheets()][][];
            for (int i = 0; i < sheets.length; i++) sheets[i] = parseSheet(workbook.getSheetAt(i));
            return sheets;
        } catch (InvalidFormatException | IOException e) {
            LOGGER.error("failed to read sheets from: " + excelPath, e);
            return null;
        }
    }

    public static String stringify(String[][] sheet, String cellSeparator, String rowSeparator) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String[] row : sheet) {
            stringBuilder.append(String.join(cellSeparator, row));
            stringBuilder.append(rowSeparator);
        }

        return stringBuilder.toString();
    }

    public static String stringify(String[][][] sheets, String cellSeparator, String rowSeparator, String sheetSeparator) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String[][] sheet : sheets) {
            stringBuilder.append(stringify(sheet, cellSeparator, rowSeparator));
            stringBuilder.append(sheetSeparator);
        }

        return stringBuilder.toString();
    }
}
