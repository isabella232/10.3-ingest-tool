package com.ontotext.ehri.tools;

import java.io.File;
import java.io.FileFilter;

public class XMLFileFilter implements FileFilter {
    public static final XMLFileFilter INSTANCE = new XMLFileFilter();

    @Override
    public boolean accept(File pathname) {
        return pathname.isFile() && (pathname.getName().endsWith(".xml") || pathname.getName().endsWith(".XML"));
    }
}
