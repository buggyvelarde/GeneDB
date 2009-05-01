package org.genedb.db.loading.auxiliary;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Run the specified loader on the specified files or directories.
 * Valid loaders are Spring beans, defined in <code>PolyPeptideContext.xml</code>.
 * If the name of a specified file ends with <code>.gz</code>, it is assumed
 * to be GZip-compressed, and is decompressed before loading. If a directory
 * is specified, the contents of the directory are processed recursively,
 * ignoring files or subdirectories whose name ends with a tilde (<code>~</code>).
 * <p>
 * This class is ordinarily invoked from ant via a loader-specific target, for example:
 * <p>
 * <code>ant -Dload=test -Dorganism=Pfalciparum -Dfile=PlasmoAP.txt reload-plasmoap</code>
 *
 * @author art
 * @author rh11
 */
public class Load {
    private static void dieUsage() {
        System.err.println("Usage: java FileProcessor <loaderName> [<options>] <files>");
        System.exit(1);
    }
    private static void invalidOption(String invalidOption, Set<String> validOptions) {
        System.err.printf("Invalid option --%s\n", invalidOption);
        if (validOptions.isEmpty())
            System.err.println("(There are no valid options for this loader type)");
        else {
            System.err.println("Valid options are:");
            for (String validOption: validOptions)
                System.err.printf("\t--%s\n", validOption);
        }
        System.exit(1);
    }

    /**
     * Entry-point for command-line usage of auxiliary loaders.
     * @see Load
     */
    public static void main(String[] args) throws IOException {

        if (args.length < 1)
            dieUsage();
        String loaderBeanName = args[0];

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"Load.xml"});

        Loader loader = ctx.getBean(loaderBeanName, Loader.class);
        Load load = new Load(loader);

        Set<String> validOptions = loader.getOptionNames();
        int firstFilename = -1;
        for (int i=1; i < args.length; i++) {
            String option = args[i];
            if (!option.startsWith("--")) {
                firstFilename = i;
                break;
            }
            option = option.substring(2);

            int equalsIndex = option.indexOf('=');
            boolean optionAccepted;
            if (equalsIndex < 0)
                optionAccepted = loader.processOptionIfValid(option, null);
            else
                optionAccepted = loader.processOptionIfValid(option.substring(0, equalsIndex), option.substring(equalsIndex + 1));

            if (!optionAccepted)
                invalidOption(option, validOptions);
        }

        long startTime = new Date().getTime();

        if (loader.loadsFromFile()) {
            if (firstFilename == -1)
                dieUsage();
            String[] filePaths = Arrays.copyOfRange(args, firstFilename, args.length);

            for (String filePath: filePaths)
                load.load(filePath);
        }
        else {
            loader.load(null);
        }

        long elapsedMilliseconds = new Date().getTime() - startTime;
        float elapsedSeconds = (float) elapsedMilliseconds / 1000;

        System.out.printf("Total time taken: %.0fh %.0fm %.2fs\n",
            elapsedSeconds / 3600, (elapsedSeconds / 60) % 60, elapsedSeconds % 60);
    }

    private static final Logger logger = Logger.getLogger(Load.class);

    private Loader loader;
    Load (Loader loader) {
        this.loader = loader;
    }

    private static FilenameFilter doesNotEndWithTilde = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return !name.endsWith("~");
        }
    };

    public void load(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            logger.error("No such file/directory: "+filename);
            return;
        }

        if ( file.isDirectory() ) {
            for (String filteredFilename: file.list(doesNotEndWithTilde))
                load(filename+"/"+filteredFilename);
            return;
        }

        logger.info(String.format("Loading file '%s'", filename));

        InputStream inputStream = new FileInputStream(file);
        if (filename.endsWith(".gz")) {
            logger.info("Treating as a GZIP file");
            inputStream = new GZIPInputStream(inputStream);
        }

        loader.load(inputStream);
    }
}
