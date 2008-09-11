package org.genedb.db.dao;

import org.gmod.schema.feature.CytoplasmicRegion;
import org.gmod.schema.feature.GPIAnchorCleavageSite;
import org.gmod.schema.feature.MembraneStructure;
import org.gmod.schema.feature.MembraneStructureComponent;
import org.gmod.schema.feature.NonCytoplasmicRegion;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.feature.PolypeptideRegion;
import org.gmod.schema.feature.SignalPeptide;
import org.gmod.schema.feature.TransmembraneRegion;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureDbXRef;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.FeatureSynonym;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Synonym;
import org.gmod.schema.utils.CountedName;
import org.gmod.schema.utils.GeneNameOrganism;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rh11
 *
 */
public class SequenceDao extends BaseDao {

    private static final Logger logger = Logger.getLogger(SequenceDao.class);

    private CvDao cvDao;

    /**
     * Return the feature corresponding to this feature_id
     *
     * @param id the systematic id
     * @return the Feature, or null
     */
    public Feature getFeatureById(int id) {
        return (Feature) getHibernateTemplate().load(Feature.class, id);
    }

   /**
    * This method is deprecated. Use {@link #getFeatureByUniqueName(String,Class&lt;T&gt;)} instead.
    * @param name the uniquename
    * @param featureType the type of feature to return eg "gene". <b>NB</> String, not a type argument
    * @return
    */
    @Deprecated
    public Feature getFeatureByUniqueName(String uniqueName, String featureType) {

        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate().findByNamedParam(
            "from Feature where uniqueName=:name and cvTerm.name=:featureType",
            new String[] { "name", "featureType" }, new Object[] { uniqueName, featureType });

        if (features.size() > 0) {
            return features.get(0);
        }
        return null;
    }

    public <T extends Feature> T getFeatureByUniqueName(String uniqueName, Class<T> featureClass) {
        @SuppressWarnings("unchecked")
        List<T> features = getHibernateTemplate().findByNamedParam(
            "from "+featureClass.getName()+" where uniqueName=:name",
            "name", uniqueName);

        if (features.size() == 0) {
            logger.warn(String.format("Hibernate found no feature of type '%s' with uniqueName '%s'",
                featureClass.getSimpleName(), uniqueName));
            return null;
        }

        return features.get(0);
    }

    /**
     * Return a list of features whose uniqueName matches the given pattern.
     *
     * @param namePattern an SQL/HQL pattern
     * @return the Feature, or null
     */
    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByUniqueNamePattern(String namePattern) {
        List features = getHibernateTemplate().findByNamedParam(
                "from Feature where uniqueName like :name", "name", namePattern);
        return features;
    }

    /**
     * Return a list of features with any current (ie non-obsolete) name or synonym
     *
     * @param name the lookup name
     * @return a (possibly empty) List<Feature> of children with this current name
     */
    public List<Feature> getFeaturesByAnyCurrentName(String name) {
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select f from Feature f, FeatureSynonym fs, Synonym s where f=fs.feature and fs.synonym=s and fs.current=true and s.name=:name",
                        "name", name);
        return features;
    }

    /**
     * Return a list of features located on a source Feature, within a given range
     *
     * @param min the minimum (interbase) coordinate
     * @param max the maximum (interbase) coordinate
     * @param strand
     * @param parent the source feature
     * @param type
     * @return a List of the features completely contained within this range
     */
    public List<Feature> getFeaturesByRange(int min, int max, int strand, Feature feat, String type) {
        int fid = feat.getFeatureId();
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select f "
                                + "from Feature f, FeatureLoc loc where "
                                + "f = loc.featureByFeatureId and f.type.name=:type and loc.strand="
                                + strand + " and" + " loc.featureBySrcFeatureId=" + fid + " and ("
                                + " loc.fmin<=:min and loc.fmax>=:max)",
                        new String[] { "type", "min", "max" }, new Object[] { type, min, max });
        return features;
    }

    /**
     * Return a list of features with this name or synonym (including obsolete names). The
     * name can contain an SQL wildcard (%)
     *
     * @param name the lookup name
     * @param featureType the type of feature to return eg "gene"
     * @return a (possibly empty) List<Feature> of children with this name
     */
    public List<Feature> getFeaturesByAnyName(String name, String featureType) {
        // TODO Taxon and filter
        String lookup = name.replaceAll("\\*", "%");

        logger.info("lookup is " + lookup);
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select f from Feature f, FeatureSynonym fs, Synonym s, CvTerm cvt where f=fs.feature and fs.synonym=s and fs.current=true and f.type=cvt.cvTermId and cvt.name=:featureType and s.name like :lookup",
                        new String[] { "lookup", "featureType" },
                        new Object[] { lookup, featureType });
        return features;
    }

    /**
     * Return the FeatureCvTerm that links a given Feature and CvTerm, with a given value of 'not'
     *
     * @param feature the Feature to test the link for
     * @param cvTerm the CvTerm to test the link for
     * @param not test for the not flag in the FeatureCvTerm
     * @return the Feature, or null
     */
    @SuppressWarnings("unchecked")
    public List<FeatureCvTerm> getFeatureCvTermsByFeatureAndCvTermAndNot(Feature feature,
            CvTerm cvTerm, boolean not) {
        List<FeatureCvTerm> list = getHibernateTemplate()
                .findByNamedParam(
                        "from FeatureCvTerm fct where fct.feature=:feature and fct.cvTerm=:cvTerm and fct.not=:not",
                        new String[] { "feature", "cvTerm", "not" },
                        new Object[] { feature, cvTerm, not });

        return list;
    }

    public List<Feature> getFeaturesByCvNameAndCvTermNameAndOrganisms(String cvName,
            String cvTermName, String orgs) {
        logger.info(String.format("Querying with cvName='%s', cvTermName='%s', orgs in (%s)",
            cvName, cvTermName, orgs));

        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select feature"
                        +" from FeatureCvTerm fct"
                        +" where fct.feature.organism.commonName in ("+orgs+")"
                        +" and fct.cvTerm.cv.name=:cvName and fct.cvTerm.name=:cvTermName",
                        new String[] { "cvName", "cvTermName" },
                        new Object[] { cvName, cvTermName });
        return features;
    }

    public List<Feature> getFeaturesByCvNamePatternAndCvTermNameAndOrganisms(String cvNamePattern,
            String cvTermName, String orgs) {
        logger.info(String.format("Querying with cvName like '%s', cvTermName='%s', orgs in (%s)",
            cvNamePattern, cvTermName, orgs));
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                    "select feature"
                    +" from FeatureCvTerm fct"
                    +" where fct.feature.organism.commonName in ("+orgs+")"
                    +" and fct.cvTerm.cv.name like :cvNamePattern and fct.cvTerm.name=:cvTermName",
                    new String[] { "cvNamePattern", "cvTermName" }, new Object[] { cvNamePattern, cvTermName });
        return features;
    }

    /**
     * Return a synonym of the given name and type if it exists
     *
     * @param name the name to lookup
     * @param type the type of the Synonym
     * @return a Synonym, or null
     */
    public Synonym getSynonymByNameAndCvTerm(String name, CvTerm type) {
        @SuppressWarnings("unchecked")
        List<Synonym> list = getHibernateTemplate().findByNamedParam(
                "from Synonym s where s.name=:name and s.type=:cvterm",
                new String[] { "name", "cvterm" }, new Object[] { name, type });

        return firstFromList(list, "name", name, "cvterm", type.getName());
    }

    /**
     * Return a list of FeatureSynonyms which link a given Feature and Synonym
     *
     * @param feature the test Feature
     * @param synonym the test Synonym
     * @return a (possibly empty) list of feature synonyms
     */
    @SuppressWarnings("unchecked")
    public List<FeatureSynonym> getFeatureSynonymsByFeatureAndSynonym(Feature feature,
            Synonym synonym) {
        return getHibernateTemplate().findByNamedParam(
                "from FeatureSynonym fs where fs.feature=:feature and fs.synonym=:synonym",
                new String[] { "feature", "synonym" }, new Object[] { feature, synonym });
    }

    /*
     * Deleted doc comment that was obviously wrong. - rh11
     * TODO work out what this actually does, and document it.
     */
    @SuppressWarnings("unchecked")
    public List<List<?>> getFeatureByGO(final String go) {
        String[] temp = go.split(":");
        String number = temp[1];
        List<Feature> polypeptides;
        List<CvTerm> goName;
        List<Feature> features = new ArrayList<Feature>();
        polypeptides = getHibernateTemplate().findByNamedParam(
                "select f " + "from Feature f, CvTerm c, FeatureCvTerm fc where "
                        + "c.dbXRef.accession=:number and fc.cvTerm = c "
                        + "and fc.feature=f", new String[] { "number" },
                new Object[] { number });
        for (Feature polypep : polypeptides) {
            logger.info(polypep.getUniqueName());
            List<Feature> genes = getHibernateTemplate()
                    .findByNamedParam(
                            "select f "
                                    + "from Feature f,FeatureRelationship f1,FeatureRelationship f2 where "
                                    + "f2.subjectFeature=:polypep and f2.objectFeature=f1.subjectFeature "
                                    + "and f1.objectFeature=f", new String[] { "polypep" },
                            new Object[] { polypep });
            if (genes.size() > 0) {
                features.add(genes.get(0));
            }
        }
        goName = getHibernateTemplate().findByNamedParam(
                "select cv " + "from CvTerm cv where cv.dbXRef.accession=:number",
                new String[] { "number" }, new Object[] { number });

        List<Feature> flocs = new ArrayList<Feature>();
        String name = "chromosome";
        flocs = getHibernateTemplate().findByNamedParam(
                "select f from Feature f " + "where f.type.name=:name", new String[] { "name" },
                new Object[] { name });
        List<List<?>> data = new ArrayList<List<?>>();
        data.add(features);
        data.add(flocs);
        data.add(goName);
        return data;
    }

    @SuppressWarnings("unchecked")
    public FeatureDbXRef getFeatureDbXRefByFeatureAndDbXRef(Feature feature, DbXRef dbXRef) {
        List<FeatureDbXRef> results = getHibernateTemplate().findByNamedParam(
                "from FeatureDbXRef fdxr where fdxr.feature=:feature and fdxr.dbXRef=:dbXRef",
                new String[] { "feature", "dbXRef" }, new Object[] { feature, dbXRef });
        return firstFromList(results, feature, dbXRef);
    }

    /**
     * Return a list of feature uniquename based on cvterm for auto-completion
     *
     * @param name the Feature uniquename
     * @param orgNames the comma seperated organism common names
     * @param featureType the type of Features to return e.g gene
     * @param limit the number of maximum results to return
     * @return a (possibly empty) List<Feature> of Feature
     */
    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByAnyNameAndOrganism(String nl, String orgNames,
            String featureType) {

        String lookup = nl.replaceAll("\\*", "%");

        logger.info("Lookup='" + lookup + "' featureType='" + featureType + "' orgs='" + orgNames
                + "'");
        // The list of orgs is being included literally as it didn't seem to
        // work as a parameter
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select f from Feature f where"
                                + " f.uniqueName like :lookup and f.type.name=:featureType and f.organism.commonName in ( "
                                + orgNames + " )", new String[] { "lookup", "featureType" },
                        new Object[] { lookup, featureType, });

        return features;
    }

    /**
     * Get a list of all products, together with the number of
     * times each is used.
     *
     * @return a list of <code>CountedName</code> objects
     */
    @SuppressWarnings("unchecked")
    // FIXME - Remove hard coded value - make more general?
    public List<CountedName> getAllProductsWithCount() {
        return getHibernateTemplate().find(
                "select new CountedName(cvt.name, count(fct.feature.uniqueName))"
                        + " from CvTerm cvt, FeatureCvTerm fct"
                        + " where cvt=fct.cvTerm and cvt.cv=15 group by cvt.name");
    }

    /**
     * Return a list of features that have this particular cvterm
     *
     * @param cvTermName the CvTerm name
     * @return a (possibly empty) List<Feature> of children
     */
    public List<Feature> getFeaturesByCvTermName(String cvTermName) {
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate().findByNamedParam(
                "select f.feature from FeatureCvTerm f where f.type.name like :cvTermName",
                "cvTermName", cvTermName);
        return features;
    }

    // FIXME - Use top level properties instead
    /**
     * Return a list of top-level features
     *
     * @return a (possibly empty) List<Feature> of children
     */
    public List<Feature> getTopLevelFeatures() {
        String name = "chromosome%";
        @SuppressWarnings("unchecked")
        List<Feature> topLevels = getHibernateTemplate().findByNamedParam(
                "from Feature where cvTerm.name like :name", "name", name);
        return topLevels;
    }

    /**
     * Return a list of features that have this particular cvterm
     *
     * @param cvTermName the CvTerm name
     * @param cvName the CV to which the term belongs
     * @return a (possibly empty) List<Feature> of matching features
     */
    public List<Feature> getFeaturesByCvTermNameAndCvName(String cvTermName, String cvName) {
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select f.feature from FeatureCvTerm f where f.type.name like :cvTermName"
                        +" and f.type.cv.name like :cvName",
                        new String[] { "cvTermName", "cvName" },
                        new Object[] { cvTermName, cvName });
        return features;
    }

    /**
     * Given the specification of a CvTerm, find all the genes that
     * belong to an organism and have transcripts that have polypeptides
     * that have this CvTerm associated to them. In practice, this is used
     * to get a list of genes that have a particular Gene Ontology annotation,
     * for example.
     *
     * @param cvTermName the CvTerm name
     * @param cvName the Cv name
     * @param organism the Organism common name. can be null in which case search spans
     * across all organisms
     * @return a (possibly empty) List<GeneNameOrganism> of matches
     */
    @SuppressWarnings("unchecked")
    public List<GeneNameOrganism> getGeneNameOrganismsByCvTermNameAndCvName(String cvTermName, String cvName,
            String organism) {

        List<GeneNameOrganism> features;
        if(organism != null) {

            features = getHibernateTemplate()
                    .findByNamedParam(
                            "select new org.gmod.schema.utils.GeneNameOrganism( " +
                            "transcript_gene.objectFeature.uniqueName, transcript_gene.objectFeature.organism.abbreviation) " +
                            "from " +
                            "FeatureRelationship transcript_gene, FeatureRelationship polypeptide_transcript " +
                            "where transcript_gene.subjectFeature=polypeptide_transcript.objectFeature and " +
                            "polypeptide_transcript.type.name='derives_from' and " +
                            " transcript_gene.objectFeature.organism.commonName in ("+organism+") and " +
                            "polypeptide_transcript.subjectFeature in ( " +
                            "select fct.feature from FeatureCvTerm fct where " +
                            "fct.cvTerm.name=:cvTermName and fct.cvTerm.cv.name=:cvName) " +
                            "order by transcript_gene.objectFeature.organism.abbreviation",
                            new String[] { "cvTermName", "cvName" },
                            new Object[] { cvTermName, cvName });
        } else {
            features = getHibernateTemplate()
            .findByNamedParam(
                    "select new org.gmod.schema.utils.GeneNameOrganism( " +
                    "transcript_gene.objectFeature.uniqueName, transcript_gene.objectFeature.organism.abbreviation) " +
                    "from " +
                    "FeatureRelationship transcript_gene, FeatureRelationship polypeptide_transcript " +
                    "where transcript_gene.subjectFeature=polypeptide_transcript.objectFeature and " +
                    "polypeptide_transcript.type.name='derives_from' and " +
                    "polypeptide_transcript.subjectFeature in ( " +
                    "select fct.feature from FeatureCvTerm fct where " +
                    "fct.cvTerm.name=:cvTermName and fct.cvTerm.cv.name=:cvName) " +
                    "order by transcript_gene.objectFeature.organism.abbreviation",
                    new String[] { "cvTermName", "cvName" },
                    new Object[] { cvTermName, cvName });
        }

        return features;
    }

    @SuppressWarnings("unchecked")
    public List<GeneNameOrganism> getGeneNameOrganismsByCvTermNameAndCvNamePattern(String cvTermName, String cvNamePattern,
            String organism) {

        List<GeneNameOrganism> features;
        if(organism != null) {

            features = getHibernateTemplate()
                    .findByNamedParam(
                            "select new org.gmod.schema.utils.GeneNameOrganism( " +
                            "transcript_gene.objectFeature.uniqueName, transcript_gene.objectFeature.organism.abbreviation) " +
                            "from " +
                            "FeatureRelationship transcript_gene, FeatureRelationship polypeptide_transcript " +
                            "where transcript_gene.subjectFeature=polypeptide_transcript.objectFeature and " +
                            "polypeptide_transcript.type.name='derives_from' and " +
                            " transcript_gene.objectFeature.organism.commonName in ("+organism+") and " +
                            "polypeptide_transcript.subjectFeature in ( " +
                            "select fct.feature from FeatureCvTerm fct where " +
                            "fct.cvTerm.name=:cvTermName and fct.cvTerm.cv.name like :cvNamePattern) " +
                            "order by transcript_gene.objectFeature.organism.abbreviation",
                            new String[] { "cvTermName", "cvNamePattern" },
                            new Object[] { cvTermName, cvNamePattern });
        } else {
            features = getHibernateTemplate()
            .findByNamedParam(
                    "select new org.gmod.schema.utils.GeneNameOrganism( " +
                    "transcript_gene.objectFeature.uniqueName, transcript_gene.objectFeature.organism.abbreviation) " +
                    "from " +
                    "FeatureRelationship transcript_gene, FeatureRelationship polypeptide_transcript " +
                    "where transcript_gene.subjectFeature=polypeptide_transcript.objectFeature and " +
                    "polypeptide_transcript.type.name='derives_from' and " +
                    "polypeptide_transcript.subjectFeature in ( " +
                    "select fct.feature from FeatureCvTerm fct where " +
                    "fct.cvTerm.name=:cvTermName and fct.cvTerm.cv.name like :cvNamePattern) " +
                    "order by transcript_gene.objectFeature.organism.abbreviation",
                    new String[] { "cvTermName", "cvNamePattern" },
                    new Object[] { cvTermName, cvNamePattern });
        }

        return features;
    }

    /**
     * Return a list of feature uniquename based on cvterm for auto-completion
     *
     * @param name the Feature uniquename
     * @param cvTerm the CvTerm
     * @param limit the number of maximum results to return
     * @return a (possibly empty) List<String> of feature uniquename
     */
    public List<String> getPossibleMatches(String name, CvTerm cvTerm, int limit) {
        HibernateTemplate ht = new HibernateTemplate(getSessionFactory());
        ht.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<String> result = ht.findByNamedParam(
                        "select f.uniqueName from Feature f where lower(f.uniqueName) like lower(:name) and f.type = :cvTerm",
                        new String[] { "name", "cvTerm" },
                        new Object[] { "%" + name + "%", cvTerm });
        return result;
    }

    /**
     * Return a list of feature based on organism
     *
     * @param organism the Organism
     * @return a (possibly empty) List<String> of feature
     */
    public List<Feature> getFeaturesByOrganism(Organism org) {
        @SuppressWarnings("unchecked") // findByNamedParam(query) returns a bare List
        List<Feature> features = getHibernateTemplate().findByNamedParam(
                "from Feature f where f.organism=:org", "org", org);
        return features;
    }

    /**
     * Return the features corresponding to uniquenames in the list
     *
     * @param names the list of uniquenames
     * @return the list of Features, or null
     */
    public List<Feature> getFeaturesByUniqueNames(List<String> names) {
        boolean notFirst = false;
        StringBuilder featureIds = new StringBuilder();
        for (String name : names) {
            if (notFirst) {
                featureIds.append(", ");
            } else {
                notFirst = true;
            }
            featureIds.append('\'');
            featureIds.append(name);
            featureIds.append('\'');
        }
        String query = "from Feature f where f.uniqueName in (" + featureIds.toString() + ")";

        @SuppressWarnings("unchecked") // find(query) returns a bare List
        List<Feature> features = getHibernateTemplate().find(query);

        return features;
    }

    /**
     * Return a list of features located within a given range
     *
     * @param min the minimum (interbase) coordinate
     * @param max the maximum (interbase) coordinate
     * @param type (gene, protein, mRNA etc)
     * @param organism
     * @param parent (chromosome or contig)
     * @return a ;ist of features completely contained within this range
     */
    public List<Feature> getFeaturesByLocation(int min, int max, String type, String organism,
            Feature parent) {
        @SuppressWarnings("unchecked") // findByNamedParam(query) returns a bare List
        List<Feature> features = getHibernateTemplate().findByNamedParam(
                "select f from Feature f , FeatureLoc fl " + "where fl.fmin>=:min "
                        + "and fl.fmax<=:max and fl.featureByFeatureId=f.featureId "
                        + "and fl.featureBySrcFeatureId=:parent and f.type.name=:type "
                        + "and f.organism.commonName=:organism",
                new String[] { "min", "max", "type", "organism", "parent" },
                new Object[] { min, max, type, organism, parent });

        return features;
    }

    /**
     * Return the FeatureRelationship containing a particular subject, object and the relation
     *
     * @param subject the subject Feature
     * @param object the object Feature
     * @param relation the cvterm corresponding to the relation
     * @return the FeatureRelationship, or null
     */
    public FeatureRelationship getFeatureRelationshipBySubjectObjectAndRelation(Feature subject,
            Feature object, CvTerm relation) {
        @SuppressWarnings("unchecked") // findByNamedParam(query) returns a bare List
        List<FeatureRelationship> frs = getHibernateTemplate().findByNamedParam(
                "from FeatureRelationship fr "
                        + "where fr.subjectFeature=:subject and fr.objectFeature=:object "
                        + "and fr.type=:relation",
                new String[] { "subject", "object", "relation" },
                new Object[] { subject, object, relation });

        if (!frs.isEmpty()) {
            return frs.get(0);
        }
        return null;
    }

    private static final String QUERY_UNIQUENAME
        = "select uniqueName from Feature where uniqueName = :uniqueName";

    private boolean featureExists(String uniqueName) {
        List<?> names = getHibernateTemplate().findByNamedParam(
            QUERY_UNIQUENAME, "uniqueName", uniqueName);
        return !names.isEmpty();
    }

    /**
     * Given a candidate uniqueName for a feature, return a derived
     * name that does not exist in the database. If the given name
     * does not exist, it is guaranteed to be returned unchanged;
     * otherwise it will have the string <code>:n</code> appended,
     * where <code>n</code> is the least positive integer such that
     * the name does not exist.
     *
     * @param uniqueName the proposed uniqueName
     * @return a derived uniqueName that does not exist in the database
     */
    public String makeNameUnique(String uniqueName) {
        /*
         * Any features which have been persisted but not flushed
         * will fail to be found here, unless we flush them first.
         */
        flush();

        if (!featureExists(uniqueName)) {
            logger.debug(String.format("Feature named '%s' does not already exist", uniqueName));
            return uniqueName;
        }

        String nameToUse;
        for (int n=1; featureExists(nameToUse = String.format("%s:%d", uniqueName, n)); n++);
        logger.debug(String.format("Feature '%s' will be named '%s'", uniqueName, nameToUse));
        return nameToUse;
    }

    /*
     * The object-creation methods below are experimental. The thought is that
     * it's redundant to have to specify the CvTerm when creating a feature,
     * because the feature class determines it. I'm not sure whether it's
     * possible to have the constructors do this: certainly they can't get
     * access to the Hibernate session in any straightforward fashion. One
     * possibility is to get the session factory from JNDI, where Hibernate
     * should bind it. (In fact I don't think that is currently working: we get
     * an error saying it can't be bound. This is presumably easy to fix.)
     *
     * If we can write constructors that get the CvTerm themselves, that would
     * perhaps be a better solution to the basic problem. It's obviously
     * inconvenient to have a lot of class-specific methods here that should
     * really be associated with the classes themselves. On the other hand,
     * methods here can do more sophisticated construction involving several
     * objects, factory-style, as {@link #createPolypeptideDomain} shows.
     *
     * The message (to my future self and to anyone else who works on this) is
     * to keep an open mind about whether this is a good idea or not. Perhaps we
     * should ALSO have constructors that work out the CvTerm for themselves,
     * and just have non-trivial factory methods defined here.
     *
     * There is also some overlap of intent with
     * org.genedb.db.loading.FeatureUtils, which should be resolved, perhaps by
     * migrating the factory methods of FeatureUtils into here..
     *
     * -rh11
     */

    private CvTerm polypeptideDomainType;
    private CvTerm scoreType;
    private CvTerm descriptionType;
    /**
     * Create a new polypeptide domain feature
     *
     * @param domainUniqueName
     * @param polypeptide the polypeptide to which this domain feature should be attached
     * @param score an indication, from the algorithm that predicted this domain,
     *          of the confidence of the prediction. Usually a number.
     * @param description description of the domain
     * @param start the start of the domain, relative to the polypeptide, in interbase coordinates
     * @param end the end of the domain, relative to the polypeptide, in interbase coordinates
     * @param dbxref a database reference for this domain, if applicable. Can be null.
     * @return the newly-created polypeptide domain
     */
    public PolypeptideDomain createPolypeptideDomain(String domainUniqueName, Polypeptide polypeptide,
            String score, String description, int start, int end, DbXRef dbxref) {
        if (polypeptideDomainType == null) {
            polypeptideDomainType = cvDao.getCvTermByNameAndCvName("polypeptide_domain", "sequence");
        }
        if (scoreType == null) {
            scoreType = cvDao.getCvTermByNameAndCvName("score", "null");
        }
        if (descriptionType == null) {
            descriptionType = cvDao.getCvTermByNameAndCvName("description", "feature_property");
        }

        PolypeptideDomain domain = new PolypeptideDomain(
            polypeptide.getOrganism(), polypeptideDomainType, domainUniqueName);
        FeatureLoc domainLoc = new FeatureLoc(polypeptide, domain, start, false, end, false, (short)0/*strand*/, null, 0, 0);
        domain.addFeatureLoc(domainLoc);

        FeatureProp scoreProp = new FeatureProp(domain, scoreType, score, 0);
        domain.addFeatureProp(scoreProp);

        FeatureProp descriptionProp = new FeatureProp(domain, descriptionType, description, 0);
        domain.addFeatureProp(descriptionProp);

        domain.setDbXRef(dbxref);

        persist(domain);

        return domain;
        // TODO Add interproDbxref as additional parameter?
    }

    public MembraneStructure createMembraneStructure(Polypeptide polypeptide) {
        return createPolypeptideRegion(MembraneStructure.class, polypeptide, 0, polypeptide.getSeqLen());
    }

    public TransmembraneRegion createTransmembraneRegion(MembraneStructure membraneStructure, int start, int end) {
        return createMembraneStructureComponent(TransmembraneRegion.class, start, end, membraneStructure);
    }
    public CytoplasmicRegion createCytoplasmicRegion(MembraneStructure membraneStructure, int start, int end) {
        return createMembraneStructureComponent(CytoplasmicRegion.class, start, end, membraneStructure);
    }
    public NonCytoplasmicRegion createNonCytoplasmicRegion(MembraneStructure membraneStructure, int start, int end) {
        return createMembraneStructureComponent(NonCytoplasmicRegion.class, start, end, membraneStructure);
    }

    private <T extends MembraneStructureComponent> T createMembraneStructureComponent(Class<T> componentClass,
            int start, int end, MembraneStructure membraneStructure) {
        return createPolypeptideRegion(componentClass, membraneStructure.getPolypeptide(), start, end, membraneStructure);
    }
    private <T extends PolypeptideRegion> T createPolypeptideRegion(Class<T> regionClass,
            Polypeptide polypeptide, int start, int end, PolypeptideRegion containingRegion) {
        T region = createPolypeptideRegion(regionClass, polypeptide, start, end);
        addPart(containingRegion, region);
        return region;
    }

    private CvTerm partOfType;
    private FeatureRelationship addPart(Feature whole, Feature part) {
        if (partOfType == null) {
            partOfType = cvDao.getCvTermByNameAndCvName("part_of", "relationship");
        }
        FeatureRelationship featureRelationship = new FeatureRelationship(part, whole, partOfType, 0);
        part.addFeatureRelationshipsForSubjectId(featureRelationship);
        whole.addFeatureRelationshipsForObjectId(featureRelationship);
        return featureRelationship;
    }

    private <T extends PolypeptideRegion> T createPolypeptideRegion(Class<T> regionClass,
            Polypeptide polypeptide, int start, int end) {
        CvTerm regionTerm = cvDao.getCvTermForAnnotatedClass(regionClass);
        String regionUniqueName = String.format("%s:%d-%d", polypeptide.getUniqueName(), start, end);

        T region;
        try {
            region = regionClass.getConstructor(Organism.class, CvTerm.class, String.class)
                .newInstance(polypeptide.getOrganism(), regionTerm, regionUniqueName);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Failed to instantiate %s", regionClass), e);
        }

        FeatureLoc regionLoc = new FeatureLoc(polypeptide, region, start, false, end, false, (short)0/*strand*/, null, 0, 0);
        region.addFeatureLoc(regionLoc);

        return region;
    }

    private CvTerm signalPeptideType;
    private CvTerm cleavageSiteProbabilityType;
    public SignalPeptide createSignalPeptide(Polypeptide polypeptide, int loc, String probability) {
        if (signalPeptideType == null) {
            signalPeptideType = cvDao.getCvTermByDbAcc("sequence", "0000418");
        }
        if (cleavageSiteProbabilityType == null) {
            cleavageSiteProbabilityType = cvDao.getCvTermByNameAndCvName("cleavage_site_probability", "genedb_misc");
        }

        String regionUniqueName = String.format("%s:sigp%d", polypeptide.getUniqueName(), loc);
        SignalPeptide signalPeptide = new SignalPeptide(polypeptide.getOrganism(), signalPeptideType, regionUniqueName);
        FeatureLoc signalPeptideLoc = new FeatureLoc(polypeptide, signalPeptide, 0, false, loc, false, (short)0/*strand*/, null, 0, 0);
        signalPeptide.addFeatureLoc(signalPeptideLoc);

        FeatureProp probabilityProp = new FeatureProp(signalPeptide, cleavageSiteProbabilityType, probability, 0);
        signalPeptide.addFeatureProp(probabilityProp);

        return signalPeptide;
    }

    private CvTerm gpiAnchoredType;
    private CvTerm gpiAnchorCleavageSiteType;
    private CvTerm gpiCleavageSiteScoreType;
    public FeatureProp createGPIAnchoredProperty(Polypeptide polypeptide) {
        if (gpiAnchoredType == null) {
            gpiAnchoredType = cvDao.getCvTermByNameAndCvName("GPI_anchored", "genedb_misc");
        }

        FeatureProp featureProp = new FeatureProp(polypeptide, gpiAnchoredType, "true", 0);
        polypeptide.addFeatureProp(featureProp);
        return featureProp;
    }
    public GPIAnchorCleavageSite createGPIAnchorCleavageSite(Polypeptide polypeptide, int anchorLocation, String score) {
        if (gpiAnchorCleavageSiteType == null) {
            gpiAnchorCleavageSiteType = cvDao.getCvTermByNameAndCvName("GPI_anchor_cleavage_site", "genedb_feature_type");
        }
        if (gpiCleavageSiteScoreType == null) {
            gpiCleavageSiteScoreType = cvDao.getCvTermByNameAndCvName("GPI_cleavage_site_score", "genedb_misc");
        }

        String cleavageSiteUniqueName = String.format("%s:gpi", polypeptide.getUniqueName());
        GPIAnchorCleavageSite cleavageSite = new GPIAnchorCleavageSite(polypeptide.getOrganism(), gpiAnchorCleavageSiteType, cleavageSiteUniqueName);
        FeatureLoc cleavageSiteLoc = new FeatureLoc(polypeptide, cleavageSite, anchorLocation, false, anchorLocation, false, (short)0/*strand*/, null, 0, 0);
        cleavageSite.addFeatureLoc(cleavageSiteLoc);

        FeatureProp scoreProp = new FeatureProp(cleavageSite, gpiCleavageSiteScoreType, score, 0);
        cleavageSite.addFeatureProp(scoreProp);

        return cleavageSite;
    }

    private CvTerm plasmoAPScoreType;
    public FeatureProp createPlasmoAPScore(Polypeptide polypeptide, String score) {
        if (plasmoAPScoreType == null) {
            plasmoAPScoreType = cvDao.getCvTermByNameAndCvName("PlasmoAP_score", "genedb_misc");
        }

        FeatureProp featureProp = new FeatureProp(polypeptide, plasmoAPScoreType, score, 0);
        polypeptide.addFeatureProp(featureProp);
        return featureProp;
    }


    /* Invoked by Spring */
    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }
}
