package org.genedb.db.loading.interpro;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.genedb.db.loading.PropertyOverrideHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LoadInterPro {
    /**
     * @param args
     */
    public static void main(String[] filePaths) throws IOException {

        if (filePaths.length == 0) {
            System.err.println("No input files specified");
            System.exit(-1);
        }

        Properties overrideProps = new Properties();
        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"NewRunner.xml"});

        InterProLoader runner = (InterProLoader) ctx.getBean("iploader", InterProLoader.class);

        long startTime = new Date().getTime();

        for (String filePath: filePaths)
            runner.load(filePath);

        long elapsedMilliseconds = new Date().getTime() - startTime;
        float elapsedSeconds = (float) elapsedMilliseconds / 1000;

        System.out.printf("Total time taken: %.0fh %.0fm %.2fs\n",
            elapsedSeconds / 3600, elapsedSeconds / 60, elapsedSeconds % 60);
    }
}
