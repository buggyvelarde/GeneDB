package org.genedb.db.loading;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

public class EmblLoaderReloadTest extends EmblLoaderSyntheticTest {
    @BeforeClass
    public static void setupAndLoad() throws IOException, ParsingException {
        EmblLoaderSyntheticTest.setupAndLoad();
        helper.reload();
    }

    @AfterClass
    public static void cleanUp() {
        helper.cleanUp();
    }

}
