package com.ontotext.ehri.services;

import com.ontotext.ehri.tools.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ResourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceService.class);
    private static final String SEPARATOR = "|";

    static {
        checkResourceDirs();
    }

    private static void checkResourceDirs() {
        LOGGER.info("checking resource directories");
        checkDir(Configuration.getString("input-dir"));
        checkDir(Configuration.getString("output-dir"));
        checkDir(Configuration.getString("mapping-dir"));
        checkDir(Configuration.getString("xquery-dir"));
    }

    private static void checkDir(String path) {
        File dir = new File(path);
        if (dir.isDirectory()) return;

        LOGGER.info("creating directory: " + dir.getAbsolutePath());
        dir.mkdir();
    }

    public String listInputDirContents() {
        return listDirContents(Configuration.getString("input-dir"), SEPARATOR);
    }

    public String listOutputDirContents() {
        return listDirContents(Configuration.getString("output-dir"), SEPARATOR);
    }

    public String listMappingDirContents() {
        return listDirContents(Configuration.getString("mapping-dir"), SEPARATOR);
    }

    public String listXqueryDirContents() {
        return listDirContents(Configuration.getString("xquery-dir"), SEPARATOR);
    }

    private static String listDirContents(String path, String separator) {
        return String.join(separator, listDirContents(path));
    }

    private static String[] listDirContents(String path) {
        File dir = new File(path);
        File[] contents = dir.listFiles();

        String[] names = new String[contents.length];
        for (int i = 0; i < contents.length; i++) {
            names[i] = contents[i].getName();
        }

        return names;
    }

    public String listOrganisations() {
        return String.join(SEPARATOR, Configuration.organisations());
    }

    public String mappingSheetID(String organisation) {
        return Configuration.mappingSheetID(organisation);
    }

    public String mappingSheetRange(String organisation) {
        return Configuration.mappingSheetRange(organisation);
    }
}
