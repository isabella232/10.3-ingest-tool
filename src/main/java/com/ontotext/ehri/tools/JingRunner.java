package com.ontotext.ehri.tools;

import com.thaiopensource.relaxng.util.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class JingRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(JingRunner.class);
    private static final Driver DRIVER = new Driver();

    public static void validateDirectory(File rng, File eadDir, File svrlDir) {
        if (! rng.isFile()) {
            LOGGER.error("cannot find RNG file: " + rng.getAbsolutePath());
            return;
        }

        if (! eadDir.isDirectory()) {
            LOGGER.error("cannot find EAD directory: " + eadDir.getAbsolutePath());
            return;
        }

        if (! svrlDir.isDirectory()) svrlDir.mkdir();

        for (File ead : eadDir.listFiles(XMLFileFilter.INSTANCE)) {
            File svrl = new File(svrlDir, ead.getName());
            validate(rng, ead, svrl);
        }
    }

    private static void validate(File rng, File xml, File svrl) {
        LOGGER.debug("validating \"" + xml.getAbsolutePath() + "\" with \"" + rng.getAbsolutePath() + "\"");
        String[] args = {
                //"-e", Configuration.ENCODING.name(),
                "-S", svrl.getAbsolutePath(),
                rng.getAbsolutePath(),
                xml.getAbsolutePath()
        };

        DRIVER.doMain(args);
        LOGGER.debug("validation report for \"" + xml.getAbsolutePath() + "\" written to \"" + svrl.getAbsolutePath() + "\"");
    }
}
