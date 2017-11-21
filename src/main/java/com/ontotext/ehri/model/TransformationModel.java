package com.ontotext.ehri.model;

public class TransformationModel {
    private String organisation;
    private String fileType;
    private String xquery;
    private String mapping;
    private String language;

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getXquery() {
        return xquery;
    }

    public void setXquery(String xquery) {
        this.xquery = xquery;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "organisation='" + organisation + '\'' +
                ", fileType='" + fileType + '\'' +
                ", xquery='" + xquery + '\'' +
                ", mapping='" + mapping + '\'' +
                ", language='" + language + '\'';
    }
}
