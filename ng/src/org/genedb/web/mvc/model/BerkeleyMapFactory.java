//package org.genedb.web.mvc.model;
//
//import org.apache.log4j.Logger;
//
//import java.io.File;
//
//import com.sleepycat.bind.EntryBinding;
//import com.sleepycat.bind.serial.SerialBinding;
//import com.sleepycat.bind.serial.StoredClassCatalog;
//import com.sleepycat.collections.StoredMap;
//import com.sleepycat.je.Database;
//import com.sleepycat.je.DatabaseConfig;
//import com.sleepycat.je.DatabaseException;
//import com.sleepycat.je.Environment;
//import com.sleepycat.je.EnvironmentConfig;
//import com.sleepycat.je.EnvironmentLockedException;
//
//
//public class BerkeleyMapFactory {
//
//    private Environment env;
//
//    private StoredClassCatalog javaCatalog;
//    private Database dtoDb;
//
//    private StoredMap<Integer, TranscriptDTO> dtoMap;
//
//    public StoredMap<Integer, TranscriptDTO> getDtoMap() {
//        openDb();
//        return dtoMap;
//    }
//
//    private String rootDirectory;
//
//    private boolean readOnly;
//
//    public StoredMap<Integer, String> getContextMapMap() {
//        openDb();
//        return contextMapMap;
//    }
//
//    public StoredMap<String, byte[]> getImageMap() {
//        openDb();
//        return imageMap;
//    }
//
//    private StoredMap<String, byte[]> imageMap;
//    private Database imageDb;
//
//    private StoredMap<Integer, String> contextMapMap;
//    private Database contextMapDb;
//
//    private final static Logger logger = Logger.getLogger(BerkeleyMapFactory.class);
//
//    private final String CLASS_CATALOG = "java_class_catalog";
//
//    private final String DTO_STORE = "dtos";
//    private String CONTEXT_MAP_STORE = "context";
//    private String IMAGE_MAP_STORE = "images";
//
//    // This field must only be accessed while holding a lock on this object
//    private boolean databaseIsOpen = false;
//
//    {
//        logger.debug("Instantiating BerkeleyMapFactory");
//    }
//
//    private synchronized void openDb() {
//        if (databaseIsOpen) {
//            return;
//        }
//
//        Runtime.getRuntime().addShutdownHook( new Thread() {
//            @Override
//            public void run() {
//                closeDb();
//            }
//        });
//
//        logger.debug("Opening environment in: " + rootDirectory);
//        File rootDirectoryFile = new File(rootDirectory);
//        if (!rootDirectoryFile.exists()) {
//            rootDirectoryFile.mkdirs();
//        } else {
//            File lock = new File(rootDirectoryFile, "je.lck");
//            if (lock.exists()) {
//                lock.delete();
//            }
//        }
//        logger.debug("Read-only status: " + readOnly);
//
//        EnvironmentConfig envConfig = new EnvironmentConfig();
//        envConfig.setTransactional(true);
//        envConfig.setAllowCreate(!readOnly);
//        envConfig.setReadOnly(readOnly);
//
//        try {
//            env = new Environment(new File(rootDirectory), envConfig);
//        } catch (EnvironmentLockedException e) {
//            throw new RuntimeException("Unable to open Berkeley databases", e);
//        } catch (DatabaseException e) {
//            throw new RuntimeException("Unable to open Berkeley databases", e);
//        }
//
//        DatabaseConfig dbConfig = new DatabaseConfig();
//        dbConfig.setTransactional(true);
//        dbConfig.setAllowCreate(!readOnly);
//        dbConfig.setReadOnly(readOnly);
//
//        try {
//            Database catalogDb = env.openDatabase(null, CLASS_CATALOG, dbConfig);
//            javaCatalog = new StoredClassCatalog(catalogDb);
//
//            dtoDb = env.openDatabase(null, DTO_STORE, dbConfig);
//            contextMapDb = env.openDatabase(null, CONTEXT_MAP_STORE, dbConfig);
//            imageDb = env.openDatabase(null, IMAGE_MAP_STORE, dbConfig);
//            databaseIsOpen = true;
//        }
//        catch (DatabaseException exp) {
//            throw new RuntimeException("Unable to open Berkeley databases", exp);
//        }
//
//        EntryBinding<String> stringBinding =
//            new SerialBinding<String>(javaCatalog, String.class);
//        EntryBinding<Integer> integerBinding =
//            new SerialBinding<Integer>(javaCatalog, Integer.class);
//        EntryBinding<TranscriptDTO> dtoValueBinding =
//            new SerialBinding<TranscriptDTO>(javaCatalog, TranscriptDTO.class);
//        EntryBinding<byte[]> byteArrayBinding =
//            new SerialBinding<byte[]>(javaCatalog, byte[].class);
//
//        dtoMap =
//            new StoredMap<Integer, TranscriptDTO>(dtoDb, integerBinding, dtoValueBinding, true);
//
//        contextMapMap =
//            new StoredMap<Integer, String>(contextMapDb, integerBinding, stringBinding, true);
//
//        imageMap =
//            new StoredMap<String, byte[]>(imageDb, stringBinding, byteArrayBinding, true);
//    }
//
//
//    public synchronized void closeDb() {
//        try {
//            dtoDb.close();
//        } catch (DatabaseException exp) {
//            System.err.println("Unable to close DTO Berkeley DB");
//        }
//
//       try {
//            contextMapDb.close();
//       } catch (DatabaseException exp) {
//           System.err.println("Unable to close context map Berkeley DB");
//       }
//
//       try {
//            imageDb.close();
//       } catch (DatabaseException exp) {
//           System.err.println("Unable to close image Berkeley DB");
//       }
//
//       try {
//           javaCatalog.close();
//       } catch (DatabaseException exp) {
//           System.err.println("Unable to close stored class catalog");
//       }
//
//       try {
//           env.close();
//       } catch (DatabaseException exp) {
//           System.err.println("Unable to close environment");
//       }
//
//        databaseIsOpen = false;
//    }
//
//
//    public void setRootDirectory(String rootDirectory) {
//        this.rootDirectory = rootDirectory;
//    }
//
//
//    public void setReadOnly(boolean readOnly) {
//        this.readOnly = readOnly;
//    }
//
//    public String getRootDirectory() {
//        return rootDirectory;
//    }
//}
