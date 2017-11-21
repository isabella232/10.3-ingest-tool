package com.ontotext.ehri.tools;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class XSLTRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(XSLTRunner.class);
    private static final Configuration CONFIGURATION = new Configuration();
    private static final Processor PROCESSOR = new Processor(CONFIGURATION);
    private static final XsltCompiler COMPILER = PROCESSOR.newXsltCompiler();

    private static Serializer serializer = PROCESSOR.newSerializer();

    public static XsltExecutable compileStylesheet(String stylesheetPath) {

        try (InputStream inputStream = TextReader.openInputStream(stylesheetPath)) {
            Source source = new StreamSource(inputStream);

            // set system identifier to stylesheet location so that relative URIs can be resolved
            File stylesheetFile = TextReader.resolvePath(stylesheetPath);
            if (stylesheetFile.isFile()) source.setSystemId(stylesheetFile.toURI().toURL().toExternalForm());

            return COMPILER.compile(source);
        } catch (IOException | SaxonApiException e) {
            LOGGER.error("failed to compile stylesheet: " + stylesheetPath, e);
            return null;
        }
    }

    public static void runStylesheet(XsltExecutable stylesheet, File inputDir, File outputDir) {
        if (! (inputDir.isDirectory() && outputDir.isDirectory())) {
            LOGGER.error("cannot find input or output directory:"
                    + "\n\tinput directory: " + inputDir.getAbsolutePath()
                    + "\n\toutput directory: " + outputDir.getAbsolutePath()
            );
            return;
        }

        XsltTransformer transformer = stylesheet.load();

        for (File inputFile : inputDir.listFiles(XMLFileFilter.INSTANCE)) {
            File outputFile = new File(outputDir, inputFile.getName());
            runTransformer(transformer, inputFile, outputFile);
        }

        try {
            transformer.close();
        } catch (SaxonApiException e) {
            LOGGER.error("failed to close transformer", e);
        }
    }

    private static void runTransformer(XsltTransformer transformer, File input, File output) {
        serializer.setOutputFile(output);
        transformer.setDestination(serializer);

        try {
            transformer.setSource(new StreamSource(input));
            transformer.transform();
        } catch (SaxonApiException e) {
            LOGGER.error("failed to run stylesheet on file: " + input.getAbsolutePath(), e);
        }
    }
}
