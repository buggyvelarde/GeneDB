package org.genedb.db.loading.polypeptide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.TransmembraneRegion;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class TMHMMLoader extends Loader {

    private static Logger logger = Logger.getLogger(TMHMMLoader.class);

    @Override
    public void doLoad(InputStream inputStream, Session session) throws IOException {
        Transaction transaction = session.getTransaction();

        TMHMMFile file = new TMHMMFile(inputStream);
        int n=1;
        for (String key: file.keys()) {
            logger.info(String.format("[%d/%d] Loading helices for key '%s'", n++, file.keys().size(), key));
            Polypeptide polypeptide = getPolypeptideByMangledName(key);

            transaction.begin();
            for (TMHelix helix: file.helicesForKey(key)) {
                loadHelix(polypeptide, helix);
            }
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

    private void loadHelix(Polypeptide polypeptide, TMHelix helix) {
        logger.debug(String.format("Adding transmembrane region for '%s' at %d-%d",
            helix.getKey(), helix.getFmin(), helix.getFmax()));
        TransmembraneRegion tmr = sequenceDao.createTransmembraneRegion(polypeptide,
                helix.getFmin(), helix.getFmax());
        sequenceDao.persist(tmr);
    }
}

class TMHelix {
    private String key;
    private int fmin, fmax;
    public TMHelix(String key, int fmin, int fmax) {
        this.key = key;
        this.fmin = fmin;
        this.fmax = fmax;
    }
    public String getKey() {
        return key;
    }
    public int getFmin() {
        return fmin;
    }
    public int getFmax() {
        return fmax;
    }
}

class TMHMMFile {
    private Map<String,Collection<TMHelix>> helicesByKey = new HashMap<String,Collection<TMHelix>>();
    public TMHMMFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while (null != (line = reader.readLine())) {
            if (line.startsWith("#"))
                continue;
            String[] fields = line.split("\t");
            if (fields[2].equals("TMhelix")) {
                String key = fields[0];
                String startString = fields[3];
                String stopString = fields[4];

                TMHelix helix = new TMHelix(key, Integer.parseInt(startString) - 1, Integer.parseInt(stopString));

                if (!helicesByKey.containsKey(key))
                    helicesByKey.put(key, new ArrayList<TMHelix>());
                helicesByKey.get(key).add(helix);
            }
        }
    }
    public Set<String> keys() {
        return helicesByKey.keySet();
    }
    public Collection<TMHelix> helicesForKey(String key) {
        return helicesByKey.get(key);
    }
}