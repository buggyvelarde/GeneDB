package org.genedb.db.loading.interpro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.genedb.db.loading.PropertyOverrideHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Run the InterPro loader on the specified files or directories.
 *
 * @author art
 * @author rh11
 */
public class LoadInterPro {
    public static void main(String[] filePaths) throws IOException {

        if (filePaths.length == 0) {
            System.err.println("No input files specified");
            System.exit(-1);
        }

        Properties overrideProps = new Properties();
        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"InterProContext.xml"});

        InterProLoader loader = (InterProLoader) ctx.getBean("iploader", InterProLoader.class);
        LoadInterPro lip = new LoadInterPro(loader);

        long startTime = new Date().getTime();

        for (String filePath: filePaths)
            lip.load(filePath);

        long elapsedMilliseconds = new Date().getTime() - startTime;
        float elapsedSeconds = (float) elapsedMilliseconds / 1000;

        System.out.printf("Total time taken: %.0fh %.0fm %.2fs\n",
            elapsedSeconds / 3600, elapsedSeconds / 60, elapsedSeconds % 60);
    }

    private static final Logger logger = Logger.getLogger(LoadInterPro.class);

    private InterProLoader loader;
    LoadInterPro(InterProLoader loader) {
        this.loader = loader;
    }

    private static FilenameFilter doesNotEndWithTilde = new FilenameFilter() {
        public boolean accept(@SuppressWarnings("unused") File dir, String name) {
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

        logger.info(String.format("Loading InterPro file '%s'", filename));

        InputStream inputStream = new FileInputStream(file);
        if (filename.endsWith(".gz")) {
            logger.info("Treating as a GZIP file");
            inputStream = new GZIPInputStream(inputStream);
        }

        InterProFile interProFile = new InterProFile(inputStream);
        loader.load(interProFile);
    }
}
