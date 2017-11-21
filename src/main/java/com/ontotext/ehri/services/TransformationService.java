package com.ontotext.ehri.services;

import com.ontotext.ehri.model.TransformationModel;
import com.ontotext.ehri.tools.*;
import net.sf.saxon.s9api.XsltExecutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

@Service
public class TransformationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationService.class);
    private static final XsltExecutable EAD1_TO_EAD2002 = XSLTRunner.compileStylesheet(Configuration.getString("ead1-to-ead2002-path"));

    public String transform(TransformationModel model, Date requestDate) {
        LOGGER.info("starting transformation with these parameters: " + model.toString());
        long start = System.currentTimeMillis();

        // convert input files to XML
        LOGGER.info("converting input files to XML");
        File inputDir = new File(Configuration.getString("input-dir"));
        XQueryRunner.convertToXML(inputDir);

        // create transformation-specific output directory
        File outputDir = new File(Configuration.getString("output-dir"));
        outputDir = new File(outputDir, Configuration.DATE_FORMAT.format(requestDate));
        if (! outputDir.isDirectory()) outputDir.mkdir();

        // create EAD directory under transformation-specific output directory
        File eadDir = new File(outputDir, Configuration.getString("ead-subdir"));
        if (! eadDir.isDirectory()) eadDir.mkdir();

        // EAD1 to EAD2002 conversion
        if (model.getOrganisation() == null && model.getMapping() == null && model.getXquery() == null) {
            LOGGER.info("performing EAD1 to EAD2002 conversion");
            XSLTRunner.runStylesheet(EAD1_TO_EAD2002, inputDir, eadDir);

        // generic transformation with Google Sheet mapping
        } else if (model.getMapping() == null && model.getXquery() == null) {
            LOGGER.info("performing generic transformation with Google Sheet mapping for organisation: " + model.getOrganisation());
            String sheetID = Configuration.mappingSheetID(model.getOrganisation());
            String sheetRange = Configuration.mappingSheetRange(model.getOrganisation());
            String mapping = GoogleSheetReader.toString(GoogleSheetReader.values(sheetID, sheetRange), "\n", "\t");
            XQueryRunner.genericTransform(mapping, inputDir, eadDir);

        // generic transformation with local sheet mapping
        } else if (model.getXquery() == null) {
            LOGGER.info("performing generic transformation with local sheet mapping: " + model.getMapping());
            File mappingDir = new File(Configuration.getString("mapping-dir"));
            File mappingFile = new File(mappingDir, model.getMapping());
            String mapping = ExcelReader.stringify(ExcelReader.readSheet(mappingFile.getAbsolutePath(), 0), "\t", "\n");
            XQueryRunner.genericTransform(mapping, inputDir, eadDir);

        // transformation with custom XQuery
        } else {
            LOGGER.info("performing transformation with custom XQuery: " + model.getXquery());
            File xqueryDir = new File(Configuration.getString("xquery-dir"));
            File xqueryFile = new File(xqueryDir, model.getXquery());
            XQueryRunner.customTransform(xqueryFile, inputDir, eadDir);
        }

        long time = System.currentTimeMillis() - start;
        LOGGER.info("finished transformation in " + time + " ms");
        return outputDir.getAbsolutePath();
    }
}
