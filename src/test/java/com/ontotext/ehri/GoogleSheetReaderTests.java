package com.ontotext.ehri;

import com.ontotext.ehri.tools.GoogleSheetReader;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GoogleSheetReaderTests {

    @Test
    public void testValues() {
        String spreadsheetId = "1H8bgPSWTvvfICZ6znvFpf4iDCib39KZ0jfgTYHmv5e0";
        checkValue(spreadsheetId, "A1:A1", "target-path");
        checkValue(spreadsheetId, "D1:D1", "value");
        checkValue(spreadsheetId, "A2:A2", "/");
        checkValue(spreadsheetId, "B2:B2", "ead");
    }

    private static void checkValue(String spreadsheetId, String range, Object expected) {
        List<List<Object>> values = GoogleSheetReader.values(spreadsheetId, range);
        assertFalse("no rows", values.isEmpty());
        assertFalse("no cells at first row", values.get(0).isEmpty());
        assertEquals("unexpected value at \"" + range + "\" in spreadsheet \"" + spreadsheetId + "\"", expected, values.get(0).get(0));
    }
}
