package org.genedb.web.mvc.model;

import org.apache.log4j.Logger;

import java.io.File;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;


public class BerkeleyMapFactory {

    private Environment env;

    private StoredClassCatalog javaCatalog;
    private Database dtoDb;

    private StoredMap<String, TranscriptDTO> dtoMap;

    public StoredMap<String, TranscriptDTO> getDtoMap() {
        openDb();
        return dtoMap;
    }

    private String rootDirectory;

    private boolean readOnly;

    public StoredMap<String, String> getContextMapMap() {
        openDb();
        return contextMapMap;
    }


    private StoredMap<String, String> contextMapMap;
    private Database contextMapDb;

    private final static Logger logger = Logger.getLogger(BerkeleyMapFactory.class);

    private final String CLASS_CATALOG = "java_class_catalog";

    private final String DTO_STORE = "dtos";
    private String CONTEXT_MAP_STORE = "context";

    private void openDb() {
        if (env != null) {
            return;
        }

        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override
            public void run() {
                closeDb();
            }
        });


        logger.debug("Opening environment in: " + rootDirectory);
        logger.debug("Read-only status: " + readOnly);

        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(!readOnly);
        envConfig.setReadOnly(readOnly);

        try {
            env = new Environment(new File(rootDirectory), envConfig);
        } catch (EnvironmentLockedException e) {
            throw new RuntimeException("Unable to open Berkeley databases", e);
        } catch (DatabaseException e) {
            throw new RuntimeException("Unable to open Berkeley databases", e);
        }

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(!readOnly);
        dbConfig.setReadOnly(readOnly);

        try {
            Database catalogDb = env.openDatabase(null, CLASS_CATALOG, dbConfig);
            javaCatalog = new StoredClassCatalog(catalogDb);

            dtoDb = env.openDatabase(null, DTO_STORE, dbConfig);
            contextMapDb = env.openDatabase(null, CONTEXT_MAP_STORE, dbConfig);
        }
        catch (DatabaseException exp) {
            throw new RuntimeException("Unable to open Berkeley databases", exp);
        }

        EntryBinding<String> stringBinding =
            new SerialBinding<String>(javaCatalog, String.class);
        EntryBinding<TranscriptDTO> dtoValueBinding =
            new SerialBinding<TranscriptDTO>(javaCatalog, TranscriptDTO.class);

        dtoMap =
            new StoredMap<String, TranscriptDTO>(dtoDb, stringBinding, dtoValueBinding, true);

        contextMapMap =
            new StoredMap<String, String>(contextMapDb, stringBinding, stringBinding, true);
    }


    public void closeDb() {
        try {
            dtoDb.close();
            contextMapDb.close();
            javaCatalog.close();
            env.close();
        } catch (DatabaseException exp) {
            logger.error("Unable to shut down Berkeley DB cleanly", exp);
        }
    }


    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }


    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

}
