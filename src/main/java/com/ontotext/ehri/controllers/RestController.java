package com.ontotext.ehri.controllers;

import com.ontotext.ehri.model.FileMetaModel;
import com.ontotext.ehri.model.ProviderConfigModel;
import com.ontotext.ehri.model.TransformationModel;
import com.ontotext.ehri.model.ValidationResultModel;
import com.ontotext.ehri.services.*;
import com.ontotext.ehri.tools.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;


@org.springframework.web.bind.annotation.RestController
@RequestMapping(value = "/rest")
public class RestController {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private TransformationService transformationService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private ProcessUpdateService processUpdateService;

    @Autowired
    private ConfigurationService configurationService;


    @RequestMapping(value = "/processUpdates", method = RequestMethod.GET)
    public Map<String, List<FileMetaModel>> processUpdate() throws IOException, URISyntaxException {
        Map<String, ProviderConfigModel> chiIngestConfig = configurationService.loadCHIIngestConfig();
        Map<String, List<FileMetaModel>> changes =  processUpdateService.checkForChanges(chiIngestConfig);
        Date now = processUpdateService.prepareForValidation(changes, chiIngestConfig);
        Map<String, File[]> eadFiles = processUpdateService.listValidationFolderFiles(now);
        processUpdateService.fixInputValidationFolder(eadFiles);



//        Map<String, File[]> preProcessed = processUpdateService.pythonPreProcessing(eadFiles, chiIngestConfig);
        Map<String, List<File>> compressedCollections = processUpdateService.compressFileCollection(eadFiles, now);

        Map<String, ValidationResultModel> validationResults  = validationService.validateDirectory(new TransformationModel(), now, null, false, chiIngestConfig);
        processUpdateService.addEADFileLocation(chiIngestConfig, compressedCollections);
        processUpdateService.processIngest(chiIngestConfig, validationResults);
//        processUpdateService.ingest2(chiIngestConfig, validationResults);
        processUpdateService.reportDatasetsWithErrors(validationResults);

        return changes;
    }


    @RequestMapping(value = "/process", method = RequestMethod.GET)
    public String transform(TransformationModel transformationModel) throws IOException {
        Date now = new Date();
        String transformationDir = transformationService.transform(transformationModel, now);
        String validation = validationService.validate(transformationModel, now, null, false, null);
        String[] validatonSplit = validation.split("\\|");
        validation = validationIterator(validatonSplit, validation, transformationDir);
        return validation;
    }

    @RequestMapping(value = "/validate", method = RequestMethod.GET)
    public String validateFiles() throws IOException {
        Date now = new Date();
        String validation = validationService.validate(new TransformationModel(), now, null, true, null);
        String[] validatonSplit = validation.split("\\|");
        String outputDir = new File(Configuration.getString("output-dir")).getAbsolutePath() + File.separator + Configuration.DATE_FORMAT.format(now);
        validation = validationIterator(validatonSplit, validation, outputDir);



        return validation;
    }

    private String validationIterator(String[] validatonSplit, String validation, String dir) {
        if (validationService != null && validation.length() > 0) {
            validation = "";
            for (String val : validatonSplit) {
                if (validation.isEmpty()) {
                    validation += dir + File.separator + "html" + File.separator + val;
                } else {
                    validation += "|" +  dir + File.separator + "html" + File.separator + val;
                }

            }
        }
        return validation;
    }

    @RequestMapping(value = "/list-input-dir-contents", method = RequestMethod.GET)
    public String listInputDirContents() {
        return resourceService.listInputDirContents();
    }

    @RequestMapping(value = "/list-output-dir-contents", method = RequestMethod.GET)
    public String listOutputDirContents() {
        return resourceService.listOutputDirContents();
    }

    @RequestMapping(value = "/list-mapping-dir-contents", method = RequestMethod.GET)
    public String listMappingDirContents() {
        return resourceService.listMappingDirContents();
    }

    @RequestMapping(value = "/list-xquery-dir-contents", method = RequestMethod.GET)
    public String listXqueryDirContents() {
        return resourceService.listXqueryDirContents();
    }

    @RequestMapping(value = "/list-organisations", method = RequestMethod.GET)
    public String listOrganisations() {
        return resourceService.listOrganisations();
    }

    @RequestMapping(value = "/mapping-sheet-ID", method = RequestMethod.GET)
    public String mappingSheetID(@RequestParam("organisation") String organisation) {
        return resourceService.mappingSheetID(organisation);
    }

    @RequestMapping(value = "/mapping-sheet-range", method = RequestMethod.GET)
    public String mappingSheetRange(@RequestParam("organisation") String organisation) {
        return resourceService.mappingSheetRange(organisation);
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String errorPage(){
        return "errorPage";
    }

    @RequestMapping(value = "/htmlReport", method = RequestMethod.GET)
    public String getHtmlReport(@RequestParam("path") String path) {
        File file = new File(path);
        String fileContent = "";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line;
            while ((line = br.readLine()) != null) {
                fileContent = fileContent + line + "\n";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent;
    }

}
