package org.genedb.db.loading;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;

/**
 * This class deals with the mundane details of recursing over a directory
 * hierarchy looking for files whose names match a given pattern. It is the
 * superclass of the data-loading classes <code>LoadEmbl</code> and
 * <code>LoadOrthologues</code>.
 *
 * @author rh11
 */
public abstract class FileProcessor {
    private static final Logger logger = Logger.getLogger(FileProcessor.class);

    /**
     * Get the value of a system property, throwing an exception if the
     * property has not been set.
     *
     * @param key the name of the system property
     * @return the value of the property
     * @throws MissingPropertyException if there is no such property
     */
    protected static String getRequiredProperty(String key) throws MissingPropertyException {
        String value = System.getProperty(key);
        if (value == null) {
            throw new MissingPropertyException(key);
        }
        return value;
    }

    /**
     * Get the value of a system property, returning a default value instead
     * if the property has not been set.
     *
     * @param key the name of the system property
     * @param defaultValue the default value, to use if the property is not defined
     * @return the value of the property, or the supplied default if there is
     *          no such property
     */
    protected static String getPropertyWithDefault(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Check whether a specified system property has been set. This is used
     * for testing boolean-valued properties (flags). If the property is set,
     * but its value is the string "false", this method will also return false.
     *
     * @param key the name of the system property
     * @return <code>true</code> if the property is defined and its value is not
     *          equal to "false".
     */
    protected static boolean hasProperty(String key) {
        return null != System.getProperty(key) && !System.getProperty(key).equals("false");
    }

    /**
     * Recurse over the directory structure, starting at the specified directory. Whenever
     * a file is encountered whose name matches the given pattern, <code>processFile</code>
     * is called on that file.
     *
     * @param inputDirectoryName the starting directory. This can also be the path to a file
     *          in which case just that single file is processed whether or not its name matches
     *          the supplied pattern.
     * @param fileNamePattern a regular expression pattern, used to filter the files by name
     * @throws IOException
     * @throws ParsingException
     */
    protected void processFileOrDirectory(String inputDirectoryName, String fileNamePattern)
        throws IOException, ParsingException, SQLException
    {
        processFileOrDirectory(new File(inputDirectoryName), fileNamePattern);
    }

    private void processFileOrDirectory(File file, final String fileNamePattern)
        throws IOException, ParsingException, SQLException
    {
        if (file.isDirectory()) {
            String[] entries = file.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File file = new File(dir, name);

                    /*
                     * The Mac OS version of the 'tar' command will create a file ._foo
                     * containing the extended attribute data of each file foo that is archived.
                     * This has the annoying result that if, say, I edit some EMBL files in
                     * TextMate (which sets an extended attribute on those files) and then use
                     * tar to transfer these files to a Linux machine, the Linux machine
                     * will then have various files with the extension .embl that are not
                     * actually EMBL files and obviously cannot be parsed as such.
                     *
                     * Since the prefix ._ is almost never used for any other purpose, it
                     * seems reasonable to ignore such files here, and so avoid the problem.
                     */
                    if (name.startsWith("._")) {
                        return false;
                    }

                    return file.isDirectory() || (file.isFile() && name.matches(fileNamePattern));
                }});
            for (String entry: entries) {
                processFileOrDirectory(new File(file, entry), fileNamePattern);
            }
        } else {
            processFileAndHandleExceptions(file);
        }
    }

    private void processFileAndHandleExceptions(File inputFile)
        throws IOException, ParsingException, SQLException
    {
        try {
            Reader reader = new FileReader(inputFile);
            if (inputFile.getName().endsWith(".gz")) {
                reader = new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile)));
            }
            processFile(inputFile, reader);
            reader.close();
        } catch (ParsingException e) {
            e.setInputFile(inputFile);
            logger.error("Parsing error", e);

            skipRetryAbort(inputFile, e);
        }
        catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Data integrity violation", e);

            /*
             * The cause of a Spring exception is a Hibernate exception,
             * and the cause of *that* is the underlying JDBC exception.
             *
             * On the other hand, we might be using JDBC directly (e.g. VulgarLoader),
             * in which case the cause of the Spring exception is just the JDBC
             * exception.
             */
            Throwable cause = e.getCause().getCause();
            if (cause == null) {
                cause = e.getCause();
            }
            skipRetryAbort(inputFile, cause);
        }
        /*
         * We can also get a Hibernate exception directly here.
         * (I confess I don't understand why, but I have observed this. -rh11)
         */
        catch (org.hibernate.exception.ConstraintViolationException e) {
            logger.error("Constraint violation", e);

            /*
             * The cause of the Hibernate exception is the underlying JDBC exception.
             */
            Throwable cause = e.getCause();
            skipRetryAbort(inputFile, cause);
        }
    }

    /**
     * Process the file. Subclasses should implement this method to do whatever is
     * appropriate with the file.
     *
     * @param inputFile the input file
     * @param reader a reader for the
     * @throws IOException
     * @throws ParsingException
     */
    protected abstract void processFile(File inputFile, Reader reader)
        throws IOException, ParsingException;

    /**
     * Prompt the user to skip, retry or abort; and do the appropriate thing.
     * If the user chooses to skip, we return without doing anything.
     * If the user chooses to retry, we call {@link #processFile} recursively.
     * If the user chooses to abort, the exception we were passed is rethrown.
     * If we are unable to prompt the user (because neither a console nor a
     * windowing environment is available, or because the user kills the dialog
     * window) then an informative RuntimeException should be thrown.
     *
     * @param inputFile The input file
     * @param e The exception. Should be a ParsingException or a RuntimeException
     * @throws IOException Can only happen if the user retries, and an IOException
     *          is encountered when the input file is subsequently reloaded.
     * @throws ParsingException If we were passed a ParsingException, it will be
     *          rethrown if the user opts to abort. We may also pass on a ParsingException
     *          from a recursive <code>processEmblFile</code> call (resulting from
     *          a retry).
     */
    private void skipRetryAbort(File inputFile, Throwable e)
        throws IOException, ParsingException, SQLException
    {
        switch (skipRetryAbort.getResponse(e)) {
        case SKIP:
            logger.info(String.format("Skipping file '%s'", inputFile));
            break;
        case RETRY:
            logger.info(String.format("Retrying file '%s'", inputFile));
            processFileAndHandleExceptions(inputFile);
            break;
        case ABORT:
            if (e instanceof ParsingException) {
                throw (ParsingException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new RuntimeException("Unexpected exception (should not happen)", e);
            }
        }
    }

    private SkipRetryAbort skipRetryAbort = new SkipRetryAbort();

    /**
     * Configure this file processor always to skip a file that causes
     * an error, without prompting the user. This may be used to do a mass
     * load of dirty data for testing purposes. It should not be used for
     * a live data load.
     */
    public void alwaysSkip() {
        this.skipRetryAbort = new AlwaysSkip();
    }
}


class MissingPropertyException extends Exception {
    MissingPropertyException(String propertyName) {
        super(String.format("Required property '%s' is missing", propertyName));
    }
}