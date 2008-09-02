package org.genedb.db.loading.alternative;


import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class LoadEmbl {
    private static final Logger logger = Logger.getLogger(LoadEmbl.class);
    /**
     * @param args
     * @throws MissingPropertyException
     * @throws ParsingException
     * @throws IOException
     */
    public static void main(String[] args) throws MissingPropertyException, IOException, ParsingException {
        if (args.length > 0) {
            logger.warn("Ignoring command-line arguments");
        }
        String organismCommonName = getRequiredProperty("load.organismCommonName");
        String inputDirectory = getRequiredProperty("load.inputDirectory");
        String fileNamePattern = getPropertyWithDefault("load.fileNamePattern", ".*\\.embl");

        LoadEmbl loadEmbl = new LoadEmbl(organismCommonName);
        loadEmbl.processInputDirectory(inputDirectory, fileNamePattern);
    }

    private static String getRequiredProperty(String key) throws MissingPropertyException {
        String value = System.getProperty(key);
        if (value == null) {
            throw new MissingPropertyException(key);
        }
        return value;
    }

    private static String getPropertyWithDefault(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private ApplicationContext applicationContext;
    private EmblLoader loader;
    private LoadEmbl(String organismCommonName) {
        this.applicationContext = new ClassPathXmlApplicationContext(new String[] {"Load.xml"});

        this.loader = (EmblLoader) applicationContext.getBean("emblLoader", EmblLoader.class);
        loader.setOrganismCommonName(organismCommonName);
    }

    private void processInputDirectory(String inputDirectoryName, String fileNamePattern) throws IOException, ParsingException {
        processInputDirectory(new File(inputDirectoryName), fileNamePattern);
    }

    private void processInputDirectory(File inputDirectory, final String fileNamePattern) throws IOException, ParsingException {
        String[] entries = inputDirectory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);
                return file.isDirectory() || (file.isFile() && name.matches(fileNamePattern));
            }
        });

        for (String entry: entries) {
            File file = new File(inputDirectory, entry);
            if (file.isDirectory()) {
                processInputDirectory(file, fileNamePattern);
            } else {
                processEmblFile(file);
            }
        }
    }

    private void processEmblFile(File inputFile) throws IOException, ParsingException {
        EmblFile emblFile = new EmblFile(inputFile);
        try {
            loader.load(emblFile);
        } catch (ParsingException e) {
            e.setLocation(inputFile);
            throw e;
        }

    }
}

class MissingPropertyException extends Exception {
    MissingPropertyException(String propertyName) {
        super(String.format("Required property '%s' is missing", propertyName));
    }
}