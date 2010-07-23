package org.gmod.schema.bulk;

import java.sql.SQLException;

/**
 * Used by BulkProcessor.TranscriptIterator and BulkProcessor.PolypeptideIterator
 * to wrap database exceptions.
 *
 * @author rh11
 *
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, SQLException cause) {
        super(message, cause);
    }

    public DatabaseException(SQLException cause) {
        super(cause);
    }

}
