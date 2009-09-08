package org.genedb.db.loading.auxiliary;

import org.gmod.schema.feature.Polypeptide;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds feature properties to the polypeptide feature associated with a list of gene or protein systematic ids
 * The systematic ids should be listed, one per line in a file supplied on the command line
 * The type of the features listed (eg gene, polypeptide) can be specified using -Dload.featureType,
 * but the default is 'polypeptide' (optional)
 *
 * The type of the feature_property is specified using -Dload.propType (required)
 * The value of the feature_property is specified using -Dload.propValue and must be enclosed
 * in quotes if it contains spaces (required)
 *
 * -Dload.errorsAreNotFatal may be used if the file contains deprecated systematic ids (optional)
 *
 * Even if the file contains multiple copies of the same systematic id the feature_property will only be
 * added once per feature per load session
 *
 * Example of use:
 * ant add-featprops -Dconfig=bigtest2 -Dorganism=Pfalciparum -Dload.featureType=gene -Dload.propType=private
 * -Dload.propValue="Tinas private value" -Dload.errorsAreNotFatal=true -Dfile=featureprop.test
 *
 * @author te3
 *
 */
public class AddFeatureProperty extends Loader {
    private static final Logger logger = Logger.getLogger(AddFeatureProperty.class);

    Boolean errorsAreNotFatal = true;
    Collection<String> featuresSeen = new HashSet<String>();
    String featureType = "polypeptide";
    String propType;
    String propValue;

    @Override
    protected Set<String> getOptionNames() {
    Set<String> options = new HashSet<String>();
    Collections.addAll(options, "featureType", "propType", "propValue", "errorsAreNotFatal");
        return options;
    }
    @Override
    protected boolean processOption(String optionName, String optionValue) {

        if (optionName.equals("featureType")) {
            featureType = optionValue;
            return true;
        }
        else if (optionName.equals("propType")) {
            propType = optionValue;
            return true;
        }
        else if (optionName.equals("propValue")) {
            propValue = optionValue;
            return true;
        }
        else if (optionName.equals("errorsAreNotFatal")) {
            errorsAreNotFatal = Boolean.valueOf(optionValue);
            return true;
        }
        return false;
    }

    public void doLoad(InputStream inputStream, Session session) throws IOException {

    inputFile file = new inputFile(inputStream);

        int n=1;
        for (featProp prop: file.props()) {
            logger.info(String.format("[%d/%d] Adding feature property for %s '%s'", n++, file.props().size(), featureType, prop.getFeatureUniquename()));
            loadprop(prop, session);
            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.flush();
                session.clear();
            }
        }
    }

    private void loadprop(featProp prop, Session session) {

        Polypeptide polypeptide;
        if (featureType.equals("gene")) {
            polypeptide = getPolypeptideForGene(prop.getFeatureUniquename());
        }
        else if (featureType.equals("polypeptide") || featureType.equals("protein")) {
            polypeptide = getPolypeptideByMangledName(prop.getFeatureUniquename());
        }
        else {
            logger.error(String.format("Feature type should be gene or polypeptide not %s", featureType));
            return;
        }

        if (polypeptide == null) {
            logger.error(String.format("Could not find polypeptide for key '%s'", prop.getFeatureUniquename()));
            return;
        }


        //Add featureproperties if not already seen this polypeptide
        if (!featuresSeen.contains(polypeptide.getUniqueName())) {
            //String type = "curation";
            //String value = "GO terms evidenced by RCA were predicted using the Extended Similarity Group software (PMID:18655063)";
               //String type2 = "private";
            //String value2 = "curator_dpd;date_20090805;feat terms added based on reviewed ESG output";
            addCurations(polypeptide, propType, propValue, session);
            featuresSeen.add(polypeptide.getUniqueName());
        }
    }

    private void addCurations(Polypeptide polypeptide, String type, String value, Session session) {

        int rank = polypeptide.getFeaturePropsFilteredByCvNameAndTermName("genedb_misc", type).size() + 1;
        logger.info(String.format("Adding /%s=\"%s\" with rank %d", type, value, rank));
        try {
            session.persist(polypeptide.addFeatureProp(value, "genedb_misc", type, rank++));
        } catch (Exception e) {
            if (errorsAreNotFatal) {
                logger.error(String.format("Error adding feature prop to feature '%s'", polypeptide.getUniqueName(), e));
            } else {
                throw new RuntimeException("Error adding feature prop", e);
            }
        }
    }
}


/* Class corresponding to feat Association file */

class inputFile {
    private static final Logger logger = Logger.getLogger(inputFile.class);

    private List<featProp> props = new ArrayList<featProp>();

    public inputFile(InputStream inputStream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        int lineNumber = 0;
        while (null != (line = reader.readLine())) { //While not end of file
            if(0 < line.length()){
                lineNumber++;
                StringBuilder sb = new StringBuilder(line);
                sb.append('\n');
                logger.trace(sb);
                featProp prop = new featProp(lineNumber, line);
                props.add(prop);
            }
        }
    }

    public Collection<featProp> props() {
        return props;
    }
}

/* Each featProp corresponds to a line in the input file */
class featProp {

    private String featureUniquename;
    private int lineNumber;

    public featProp(int lineNumber, String row) {

        this.lineNumber = lineNumber;
        this.featureUniquename = row;

    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFeatureUniquename() {
        return featureUniquename;
    }

}
