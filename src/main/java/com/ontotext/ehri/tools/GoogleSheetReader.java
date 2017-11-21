package com.ontotext.ehri.tools;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GoogleSheetReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleSheetReader.class);
    private static final Collection<String> SCOPES = Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY);
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static HttpTransport httpTransport;
    static {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            LOGGER.error("failed to initialize HTTP transport", e);
        }
    }

    /**
     * Return a string representation of the given spreadsheet values.
     * @param values The values as a list of lists of objects (rows containing cells containing values).
     * @param rowSep Row separator (e.g. "\n" for newline).
     * @param colSep Column separator (e.g. "\t" for tab).
     * @return A string representation of the given spreadsheet values.
     */
    public static String toString(List<List<Object>> values, String rowSep, String colSep) {
        StringBuilder valuesString = new StringBuilder();

        for (List<Object> row : values) {
            StringBuilder rowString = new StringBuilder();

            for (Object value : row) {
                rowString.append(colSep);
                rowString.append(value.toString());
            }

            valuesString.append(rowSep);
            valuesString.append(rowString.substring(colSep.length()));
        }

        return valuesString.substring(rowSep.length());
    }

    /**
     * Return the values of the given spreadsheet in the given range.
     * @param spreadsheetId The ID of the spreadsheet.
     * @param range The range to take.
     * @return The values as a list of lists of objects (rows containing cells containing values).
     * @throws IOException
     */
    public static List<List<Object>> values(String spreadsheetId, String range) {
        Sheets service = buildService();

        try {
            return service.spreadsheets().values().get(spreadsheetId, range).execute().getValues();
        } catch (IOException e) {
            LOGGER.error("failed to fetch values for Google Sheet with ID: " + spreadsheetId);
            return null;
        }
    }

    private static Sheets buildService() {
        GoogleCredential credential = authorize(Configuration.getString("google-key-path"));
        return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(Configuration.getString("google-app-name"))
                .build();
    }

    private static GoogleCredential authorize(String keyPath) {
        File keyFile = new File(keyPath);

        try (InputStream inputStream = new FileInputStream(keyFile)) {
            return GoogleCredential.fromStream(inputStream).createScoped(SCOPES);
        } catch (IOException e) {
            LOGGER.error("failed to authorize from key at: " + keyFile.getAbsolutePath(), e);
            return null;
        }
    }
}
