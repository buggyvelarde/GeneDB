package org.genedb.db.loading.auxiliary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.gmod.schema.feature.Polypeptide;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class PlasmoAPLoader extends Loader {
    private static final Logger logger = Logger.getLogger(PlasmoAPLoader.class);

    @Override
    public void doLoad(InputStream inputStream, Session session) throws IOException {
        Transaction transaction = session.getTransaction();

        PlasmoAPFile file = new PlasmoAPFile(inputStream);
        int n=1;
        for (String key: file.keys()) {
            logger.info(String.format("[%d/%d] Loading PlasmoAP results for key '%s'", n++, file.keys().size(), key));
            Polypeptide polypeptide = getPolypeptideByMangledName(key);
            if (polypeptide == null) {
                logger.error(String.format("Could not find polypeptide '%s'", key));
                continue;
            }

            transaction.begin();
            loadScore(polypeptide, file.scoreForKey(key));
            transaction.commit();
            /*
             * If the session isn't cleared out every so often, it
             * starts to get pretty slow after a while if we're loading
             * a large file. It's important that this come immediately
             * after a flush. (Commit will trigger a flush unless you've
             * set FlushMode.MANUAL, which we assume you haven't.)
             */
            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.clear();
            }
        }
    }

    private void loadScore(Polypeptide polypeptide, String score) {
        logger.debug(String.format("Adding score '%s'", score));
        sequenceDao.persist(sequenceDao.createPlasmoAPScore(polypeptide, score));
    }
}

class PlasmoAPFile {
    private Map<String,String> scoresByKey = new HashMap<String,String>();
    public PlasmoAPFile (InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while (null != (line = reader.readLine())) {
            String[] fields = line.split("\t");
            scoresByKey.put(fields[0], fields[1]);
        }
    }
    public Collection<String> keys() {
        return scoresByKey.keySet();
    }
    public String scoreForKey(String key) {
        return scoresByKey.get(key);
    }
}