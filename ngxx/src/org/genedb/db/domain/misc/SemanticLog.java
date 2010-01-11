package org.genedb.db.domain.misc;

import org.apache.log4j.Logger;

public class SemanticLog {

    private final Logger logger = Logger.getLogger(this.getClass());

    public void log(String msg, Object... args) {

        logger.info(String.format(msg, args));

    }

}
