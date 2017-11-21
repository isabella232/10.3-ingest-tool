package com.ontotext.ehri;

import com.ontotext.ehri.tools.ExcelReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExcelReaderTests {

    @Test
    public void testReadSheet() {
        String[][] sheet = ExcelReader.readSheet("/test.xls", 0);
        assertEquals("unexpected number of rows", 4, sheet.length);
        for (int i = 0; i < sheet.length; i++) assertEquals("unexpected number of cells in row " + i, 4, sheet[i].length);

        sheet = ExcelReader.readSheet("/test.xlsx", 0);
        assertEquals("unexpected number of rows", 4, sheet.length);
        for (int i = 0; i < sheet.length; i++) assertEquals("unexpected number of cells in row " + i, 4, sheet[i].length);
    }

    @Test
    public void testStringifySheet() {
        String sheetString = ExcelReader.stringify(ExcelReader.readSheet("/test.xls", 0), "\t", "\n");
        assertEquals("unexpected sheet string", "1\t2\t3\t\na\tb\tc\td\nа\tб\tв\tг\nTRUE\tFALSE\t\t\n", sheetString);

        sheetString = ExcelReader.stringify(ExcelReader.readSheet("/test.xlsx", 0), "\t", "\n");
        assertEquals("unexpected sheet string", "1\t2\t3\t\na\tb\tc\td\nа\tб\tв\tг\nTRUE\tFALSE\t\t\n", sheetString);
    }
}
