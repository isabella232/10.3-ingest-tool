package com.ontotext.ehri.model;

/**
 * Created by Boyan on 15-Aug-17.
 */
public class ProviderConfigModel {

    private String name;
    private String ingestPropertyFile;
    private String repositoryName;
    private String repository;
    private String eadFileLocation;
    private String enrichments;
    private String log;
    private String validationErrors;
    private String eadFolderName;
    private String allowUpdate;
    private String tolerant;

    public String getTolerant() {
        return tolerant;
    }

    public void setTolerant(String tolerant) {
        this.tolerant = tolerant;
    }

    public String getAllowUpdate() {
        return allowUpdate;
    }

    public void setAllowUpdate(String allowUpdate) {
        this.allowUpdate = allowUpdate;
    }
    private String preProcessingScriptsDir;

    public String getEadFolderName() {
        return eadFolderName;
    }

    public void setEadFolderName(String eadFolderName) {
        this.eadFolderName = eadFolderName;
    }

    public String getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(String validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIngestPropertyFile() {
        return ingestPropertyFile;
    }

    public void setIngestPropertyFile(String ingestPropertyFile) {
        this.ingestPropertyFile = ingestPropertyFile;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getEadFileLocation() {
        return eadFileLocation;
    }

    public void setEadFileLocation(String eadFileLocation) {
        this.eadFileLocation = eadFileLocation;
    }

    public String getEnrichments() {
        return enrichments;
    }

    public void setEnrichments(String enrichments) {
        this.enrichments = enrichments;
    }

    public String getPreProcessingScriptsDir() {
        return preProcessingScriptsDir;
    }

    public void setPreProcessingScriptsDir(String preProcessingScriptsDir) {
        this.preProcessingScriptsDir = preProcessingScriptsDir;
    }
}
