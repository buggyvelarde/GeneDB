package org.genedb.db.loading.auxiliary;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureDbXRef;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OPIReferenceLoader extends Loader {
    private static final Logger logger = Logger.getLogger(OPIReferenceLoader.class);

    private static final String proxyURL = "http://wwwcache.sanger.ac.uk:3128";
    private static final String opiURL = "http://chemlims.com/OPI/MServlet.ChemInfo?module=Gene&DataSet=1&saveAll=YES";

    private Proxy getProxy() throws MalformedURLException {
        if (proxyURL == null) {
            return Proxy.NO_PROXY;
        }
        URL url = new URL(proxyURL);
        SocketAddress proxyAddress = new InetSocketAddress(url.getHost(), url.getPort());
        return new Proxy(Proxy.Type.HTTP, proxyAddress);
    }

    private URLConnection getOPIConnection() throws MalformedURLException, IOException {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        new URL(opiURL).openConnection(getProxy()).getContent(); // Set session cookie

        /* Issue POST request */
        HttpURLConnection opiConnection = (HttpURLConnection) new URL(opiURL).openConnection(getProxy());
        opiConnection.setDoOutput(true);
        Writer writer = new OutputStreamWriter(opiConnection.getOutputStream());
        writer.write("act=saveCSV");
        writer.close();
        return opiConnection;
    }

    private BufferedReader getOPIBufferedReader() throws MalformedURLException, IOException {
        InputStream inputStream = getOPIConnection().getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return new BufferedReader(inputStreamReader);
    }

    @Override
    public boolean loadsFromFile() {
        return false;
    }

    @Override
    protected void doLoad(InputStream inputStream, Session session)
    throws MalformedURLException, IOException {
        assert inputStream == null;

        BufferedReader br = getOPIBufferedReader();
        boolean firstLine = true;
        String line;
        List<String[]> fieldsList = new ArrayList<String[]>();
        while (null != (line = br.readLine())) {
            if (firstLine) {
                // ignore header line
                firstLine = false;
            }
            else {
                if (line.length() == 0) {
                    logger.warn("Ignoring empty line");
                } else {
                    fieldsList.add(parseLine(line));
                }
            }
        }
        br.close();

        Transaction transaction = session.getTransaction();
        int n=1;
        for (String[] fields: fieldsList) {
            String gene = fields[1];
            String description = fields[2];

            logger.debug(String.format("[%d/%d] Loading OPI reference for %s (%s)", n++, fieldsList.size(), gene, description));
            transaction.begin();
            loadReference(gene, description);
            transaction.commit();

            if (n % 100 == 1) {
                logger.info("Clearing session");
                session.clear();
            }
        }
    }

    private void loadReference(String gene, String description) {
        Polypeptide polypeptide = this.getPolypeptideForGene(gene);
        if (polypeptide != null) {
            DbXRef dbXRef = objectManager.getDbXRef("OPI", gene, description);
            FeatureDbXRef featureDbXRef = new FeatureDbXRef(dbXRef, polypeptide, true);
            sequenceDao.persist(featureDbXRef);
        }
    }

    private static Pattern csvFieldPattern = Pattern.compile("\\G(?:\"([^\"]*)\"|([^,]*))(?:,|\\z)");
    private String[] parseLine(String line) {
        String[] ret = new String[4];
        Matcher matcher = csvFieldPattern.matcher(line);
        int i=0;
        while (matcher.find()) {
            if (i >= 4) {
                throw new IllegalArgumentException(String.format("Failed to parse line '%s': found too many fields",
                    line));
            }
            String quotedField   = matcher.group(1);
            String unquotedField = matcher.group(2);
            if (quotedField != null) {
                ret[i] = quotedField;
            }
            else if (unquotedField != null) {
                ret[i] = unquotedField;
            }
            logger.trace(String.format("Field %d is '%s'", i, ret[i]));
            i++;
        }
        if (i != 4) {
            throw new IllegalArgumentException(String.format("Failed to parse line '%s': found too few fields",
                line));
        }
        return ret;
    }
}
