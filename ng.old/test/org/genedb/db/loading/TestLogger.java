package org.genedb.db.loading;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.net.URL;

public class TestLogger {
    public static void configure() {
        URL url = EmblLoaderTestHelper.class.getResource("/log4j.test.properties");
        if (url == null) {
            throw new RuntimeException("Could not find classpath resource /log4j.test.properties");
        }
        System.out.printf("Configuring Log4J from '%s'\n", url);
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(url);
    }

    public static Logger getLogger(Class<?> loggerClass) {
        configure();
        return Logger.getLogger(loggerClass);
    }
}
