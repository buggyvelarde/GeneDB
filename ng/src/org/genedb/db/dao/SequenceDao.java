package org.genedb.db.dao;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.CytoplasmicRegion;
import org.gmod.schema.feature.GPIAnchorCleavageSite;
import org.gmod.schema.feature.HelixTurnHelix;
import org.gmod.schema.feature.MembraneStructure;
import org.gmod.schema.feature.MembraneStructureComponent;
import org.gmod.schema.feature.NonCytoplasmicRegion;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideDomain;
import org.gmod.schema.feature.PolypeptideRegion;
import org.gmod.schema.feature.SignalPeptide;
import org.gmod.schema.feature.TransmembraneRegion;
import org.gmod.schema.mapped.Analysis;
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
import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
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
        return (Feature) getSession().load(Feature.class, id);
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
        List<Feature> features = getSession().createQuery(
            "from Feature where uniqueName=:uniqueName and type.name=:featureType")
            .setString("uniqueName", uniqueName).setString("featureType", featureType)
            .list();

        if (features.size() > 0) {
            return features.get(0);
        }
        return null;
    }

    /**
     * Get the feature with the specified unique name and type.
     * If there is no such feature, logs a message at level <code>INFO</code>
     * and returns <code>null</code>.
     *
     * @param <T>
     * @param uniqueName the unique name of the feature
     * @param featureClass the type of feature, e.g. <code>Polypeptide.class</code>
     * @return the feature, or <code>null</code> if there isn't such a feature
     * @throws RuntimeException if there is more than one feature with the
     *          specified unique name and type
     */
    public <T extends Feature> T getFeatureByUniqueName(String uniqueName, Class<T> featureClass) {
        @SuppressWarnings("unchecked")
        List<T> features = getSession().createQuery(
            "from "+featureClass.getName()+" where uniqueName=:uniqueName")
            .setString("uniqueName", uniqueName)
            .list();

        if (features.size() == 0) {
            logger.info(String.format("Hibernate found no feature of type '%s' with uniqueName '%s'",
                featureClass.getSimpleName(), uniqueName));
            return null;
        }
        if (features.size() > 1) {
            throw new RuntimeException(String.format("Found more than one feature of type '%s' with uniqueName '%s'",
                featureClass.getSimpleName(), uniqueName));
        }

        return features.get(0);
    }
    
    public Feature getFeatureByUniqueName(String uniqueName) {
        @SuppressWarnings("unchecked")
        List<Feature> features = getSession().createQuery(
            "from Feature where uniqueName=:uniqueName")
            .setString("uniqueName", uniqueName)
            .list();

        if (features.size() == 0) {
            logger.info(String.format("Hibernate found no feature with uniqueName '%s'",
                uniqueName));
            return null;
        }
        if (features.size() > 1) {
            throw new RuntimeException(String.format("Found more than one feature with uniqueName '%s'",
                uniqueName));
        }

        return features.get(0);
    }
    
    public AbstractGene getGene(Feature f) {
    	
    	logger.info("getGene("+ f.getUniqueName() +")");
    	
    	if (f instanceof AbstractGene) {
    		logger.info("      FOUND!");
    		return (AbstractGene)f;
    	}
    	
    	for (FeatureRelationship fr : f.getFeatureRelationshipsForSubjectId()) {
    		if ((fr.getType().getName().equals("part_of") && fr.getType().getCv().getName().equals("relationship")) 
    				|| (fr.getType().getName().equals("derives_from") && fr.getType().getCv().getName().equals("sequence"))) {
    			
    			AbstractGene gene = getGene(fr.getObjectFeature());
    			if (gene != null) {
    				return gene;
    			}
    			
    		}
    	}
    	
    	
    	return null;
    }
    
    
    

    /**
     * Get the feature with the specified unique name and type, from the
     * specified organism.
     * If there is no such feature, logs a message at level <code>INFO</code>
     * and returns <code>null</code>.
     *
     * @param <T>
     * @param uniqueName the unique name of the feature
     * @param organismCommonName the common name of the organism
     * @param featureClass the type of feature, e.g. <code>Polypeptide.class</code>
     * @return the feature, or <code>null</code> if there isn't such a feature
     * @throws RuntimeException if there is more than one feature with the
     *          specified unique name and type
     */
    public <T extends Feature> T getFeatureByUniqueNameAndOrganismCommonName(String uniqueName, String organismCommonName, Class<T> featureClass) {
        @SuppressWarnings("unchecked")
        List<T> features = getSession().createQuery(
            "from "+featureClass.getName()+" where uniqueName=:uniqueName and organism.commonName = :organism")
            .setString("uniqueName", uniqueName)
            .setString("organism", organismCommonName)
            .list();

        if (features.size() == 0) {
            logger.info(String.format("Hibernate found no feature of type '%s' with uniqueName '%s' in organism '%s'",
                featureClass.getSimpleName(), uniqueName, organismCommonName));
            return null;
        }
        if (features.size() > 1) {
            throw new RuntimeException(String.format("Found more than one feature of type '%s' with uniqueName '%s' in organism '%s'",
                featureClass.getSimpleName(), uniqueName, organismCommonName));
        }

        return features.get(0);
    }

    /**
     * Get the feature with the specified unique name patter and type, from the
     * specified organism.
     * If there is no such feature, logs a message at level <code>INFO</code>
     * and returns <code>null</code>.
     *
     * @param <T>
     * @param uniqueNamePatter an HQL/SQL pattern
     * @param organismCommonName the common name of the organism
     * @param featureClass the type of feature, e.g. <code>Polypeptide.class</code>
     * @return the feature, or <code>null</code> if there isn't such a feature
     * @throws RuntimeException if there is more than one feature with the
     *          specified unique name and type
     */
    public <T extends Feature> T getFeatureByUniqueNamePatternAndOrganismCommonName(String uniqueNamePattern, String organismCommonName, Class<T> featureClass) {
        @SuppressWarnings("unchecked")
        List<T> features = getSession().createQuery(
            "from "+featureClass.getName()+" where uniqueName like :uniqueNamePattern" +
            " and organism.commonName = :organism")
            .setString("uniqueNamePattern", uniqueNamePattern)
            .setString("organism", organismCommonName)
            .list();

        if (features.size() == 0) {
            logger.info(String.format("Hibernate found no feature of type '%s' with uniqueName pattern '%s' in organism '%s'",
                featureClass.getSimpleName(), uniqueNamePattern, organismCommonName));
            return null;
        }
        if (features.size() > 1) {
            throw new RuntimeException(String.format("Found more than one feature of type '%s' with uniqueName '%s' in organism '%s'",
                featureClass.getSimpleName(), uniqueNamePattern, organismCommonName));
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
        List features = getSession().createQuery(
                "from Feature where uniqueName like :name")
                .setString("name", namePattern).list();
        return features;
    }

    /**
     * Return a list of features with any current (ie non-obsolete) synonym
     *
     * @param name the lookup name
     * @return a (possibly empty) List<Feature> of children with this current name
     */
    public List<Feature> getFeaturesByAnyCurrentName(String name) {
        @SuppressWarnings("unchecked")
        List<Feature> features = getSession().createQuery(
                        "select fs.feature from FeatureSynonym fs where fs.current=true and fs.synonym.name=:name")
                        .setString("name", name).list();
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
        List<Feature> features = getSession().createQuery(
                        "select f "
                        + "from Feature f, FeatureLoc loc where "
                        + "f = loc.feature and f.type.name=:type and loc.strand="
                        + strand + " and" + " loc.sourceFeature=" + fid + " and ("
                        + " loc.fmin<=:min and loc.fmax>=:max)")
                    .setString("type", type)
                    .setInteger("min", min)
                    .setInteger("max", max)
                    .list();
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
        List<FeatureCvTerm> list = getSession().createQuery(
                        "from FeatureCvTerm fct where fct.feature=:feature and fct.cvTerm=:cvTerm and fct.not=:not")
                .setParameter("feature", feature)
                .setParameter("cvTerm", cvTerm)
                .setBoolean("not", not)
                .list();

        return list;
    }
    
    
    @SuppressWarnings("unchecked")
    public List<FeatureCvTerm> getFeatureCvTermsByFeatureAndCvName(Feature feature, CvTerm cvName, boolean not) {
        List<FeatureCvTerm> list = getSession().createQuery(
                        "from FeatureCvTerm fct where fct.feature=:feature and fct.cvTerm.cv.name=:cvName ")
                .setParameter("feature", feature)
                .setParameter("cvName", cvName)
                .setBoolean("not", not)
                .list();
        return list;
    }

    public List<Feature> getFeaturesByCvNameAndCvTermNameAndOrganisms(String cvName,
            String cvTermName, String orgs) {
        logger.info(String.format("Querying with cvName='%s', cvTermName='%s', orgs in (%s)",
            cvName, cvTermName, orgs));

        @SuppressWarnings("unchecked")
        List<Feature> features = getSession().createQuery(
                        "select feature"
                        +" from FeatureCvTerm fct"
                        +" where fct.feature.organism.commonName in ("+orgs+")"
                        +" and fct.cvTerm.cv.name=:cvName and fct.cvTerm.name=:cvTermName")
                  .setString("cvName", cvName)
                  .setString("cvTermName", cvTermName)
                  .list();

        return features;
    }

    public List<Feature> getFeaturesByCvNamePatternAndCvTermNameAndOrganisms(String cvNamePattern,
            String cvTermName, String orgs) {
        logger.info(String.format("Querying with cvName like '%s', cvTermName='%s', orgs in (%s)",
            cvNamePattern, cvTermName, orgs));
        @SuppressWarnings("unchecked")
        List<Feature> features = getSession().createQuery(
                    "select feature"
                    +" from FeatureCvTerm fct"
                    +" where fct.feature.organism.commonName in ("+orgs+")"
                    +" and fct.cvTerm.cv.name like :cvNamePattern and fct.cvTerm.name=:cvTermName")
                .setString("cvNamePattern", cvNamePattern)
                .setString("cvTermName", cvTermName)
                .list();

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
        List<Synonym> list = getSession().createQuery(
                "from Synonym s where s.name=:name and s.type=:type")
            .setString("name", name)
            .setParameter("type", type)
            .list();

        return firstFromList(list, "name", name, "type", type);
    }

    /**
     * Return a list of FeatureSynonyms which link a given Feature and Synonym
     *
     * @param feature the test Feature
     * @param synonym the test Synonym
     * @return a (possibly empty) list of feature synonyms
     */
    public List<FeatureSynonym> getFeatureSynonymsByFeatureAndSynonym(Feature feature,
            Synonym synonym) {
        return performQuery(FeatureSynonym.class,
                "from FeatureSynonym fs where fs.feature=:feature and fs.synonym=:synonym",
                new String[] { "feature", "synonym" }, new Object[] { feature, synonym });
    }

    /*
     * Deleted doc comment that was obviously wrong. - rh11
     * TODO work out what this actually does, and document it.
     */
    public List<List<?>> getFeatureByGO(final String go) {
        String[] temp = go.split(":");
        String number = temp[1];
        List<Feature> polypeptides;
        List<CvTerm> goName;
        List<Feature> features = new ArrayList<Feature>();

        polypeptides = performQuery(Feature.class,
                "select f " + "from Feature f, CvTerm c, FeatureCvTerm fc where "
                        + "c.dbXRef.accession=:number and fc.cvTerm = c "
                        + "and fc.feature=f", new String[] { "number" },
                new Object[] { number });

        for (Feature polypep : polypeptides) {
            logger.info(polypep.getUniqueName());
            List<Feature> genes = performQuery(Feature.class,
                            "select f "
                                    + "from Feature f,FeatureRelationship f1,FeatureRelationship f2 where "
                                    + "f2.subjectFeature=:polypep and f2.objectFeature=f1.subjectFeature "
                                    + "and f1.objectFeature=f",
                            new String[] { "polypep" },
                            new Object[] { polypep });
            if (genes.size() > 0) {
                features.add(genes.get(0));
            }
        }
        goName = performQuery(CvTerm.class,
                "select cv " + "from CvTerm cv where cv.dbXRef.accession=:number",
                new String[] { "number" }, new Object[] { number });

        List<Feature> flocs = new ArrayList<Feature>();
        String name = "chromosome";
        flocs = performQuery(Feature.class,
                "select f from Feature f " + "where f.type.name=:name", new String[] { "name" },
                new Object[] { name });
        List<List<?>> data = new ArrayList<List<?>>();
        data.add(features);
        data.add(flocs);
        data.add(goName);
        return data;
    }

    public FeatureDbXRef getFeatureDbXRefByFeatureAndDbXRef(Feature feature, DbXRef dbXRef) {
        List<FeatureDbXRef> results = performQuery(FeatureDbXRef.class,
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
    public List<Feature> getFeaturesByAnyNameAndOrganism(String nl, String orgNames,
            String featureType) {

        String lookup = nl.replaceAll("\\*", "%");

        logger.info("Lookup='" + lookup + "' featureType='" + featureType + "' orgs='" + orgNames
                + "'");
        // The list of orgs is being included literally as it didn't seem to
        // work as a parameter
        List<Feature> features = performQuery(Feature.class,
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
    // FIXME - Remove hard coded value - make more general?
    public List<CountedName> getAllProductsWithCount() {
        return performQuery(CountedName.class,
                "select new CountedName(cvt.name, count(fct.feature.uniqueName))"
                        + " from CvTerm cvt, FeatureCvTerm fct"
                        + " where cvt=fct.cvTerm and cvt.cv=15 group by cvt.name");
    }


    /**
     * Retrieve a count of how many times a given featureCvTerm appears in a
     * given organism
     *
     * @return the count
     */
    public Long getFeatureCvTermCountInOrganism(String name, Organism o) {
        Query query = createQuery(
                "select count(f) from FeatureCvTerm fct, Feature f" +
                //" where f.organism=:organism and fct.feature = f and fct.cvTerm.name = :name",
                " where fct.feature = f and f.organism=:organism and fct.cvTerm.name = :name ",
                new String[]{"name", "organism"},
                new Object[]{name, o});
        return (Long) query.uniqueResult();
    }

    /**
     * Return a list of features that have this particular cvterm
     *
     * @param cvTermName the CvTerm name
     * @return a (possibly empty) List<Feature> of children
     */
    public List<Feature> getFeaturesByCvTermName(String cvTermName) {
        List<Feature> features = performQuery(Feature.class,
                "select fct.feature from FeatureCvTerm fct where fct.type.name like :cvTermName",
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
        String name = "%chromosome%";
        List<Feature> topLevels = performQuery(Feature.class,
                "from Feature where cvTerm.name like :name", "name", name);
        return topLevels;
    }
    
    public List<Feature> getTopLevelFeaturesInOrganism(Organism organism) {
    	
    	CvTerm cvterm = cvDao.getCvTermByNameAndCvName("top_level_seq", "genedb_misc", true);
    	
    	List<Feature> topLevels = performQuery(Feature.class,
    			"select f from Feature f, FeatureProp fp " +
    			" where fp.feature = f " +
    			" and f.organism = :organism  " +
    			" and fp.cvTerm = :cvterm " +
    			" order by f.uniqueName ", 
    			new String[] {"organism", "cvterm"}, new Object[] {organism, cvterm});
    	
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
        List<Feature> features = performQuery(Feature.class,
                        "select f.feature from FeatureCvTerm f where f.cvTerm.name like :cvTermName"
                        +" and f.cvTerm.cv.name like :cvName",
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
    public List<GeneNameOrganism> getGeneNameOrganismsByCvTermNameAndCvName(String cvTermName, String cvName,
            String organism) {

        List<GeneNameOrganism> geneNameOrganisms;
        if(organism != null) {

            geneNameOrganisms = performQuery(GeneNameOrganism.class,
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
            geneNameOrganisms = performQuery(GeneNameOrganism.class,
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

        return geneNameOrganisms;
    }

    public List<GeneNameOrganism> getGeneNameOrganismsByCvTermNameAndCvNamePattern(String cvTermName, String cvNamePattern,
            String organism) {

        List<GeneNameOrganism> geneNameOrganisms;
        if(organism != null) {

            geneNameOrganisms = performQuery(GeneNameOrganism.class,
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
            geneNameOrganisms = performQuery(GeneNameOrganism.class,
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

        return geneNameOrganisms;
    }

    /**
     * Return a list of feature uniquename based on cvterm for auto-completion
     *
     * @param name the Feature uniquename
     * @param cvTerm the CvTerm
     * @param limit the maximum number of results to return
     * @return a (possibly empty) List<String> of feature uniquename
     */
    public List<String> getPossibleMatches(String name, CvTerm cvTerm, int limit) {
        @SuppressWarnings("unchecked")
        List<String> result = createQuery(
                        "select f.uniqueName from Feature f where lower(f.uniqueName) like lower(:name) and f.type = :cvTerm",
                        new String[] { "name", "cvTerm" },
                        new Object[] { "%" + name + "%", cvTerm })
                .setMaxResults(limit)
                .list();
        return result;
    }

    /**
     * Return a list of feature based on organism
     *
     * @param organism the Organism
     * @return a (possibly empty) List<String> of feature
     */
    public List<Feature> getFeaturesByOrganism(Organism org) {
        List<Feature> features = performQuery(Feature.class,
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

        List<Feature> features = performQuery(Feature.class, query);

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
        List<Feature> features = performQuery(Feature.class,
                "select f from Feature f , FeatureLoc fl " + "where fl.fmin>=:min "
                        + "and fl.fmax<=:max and fl.feature=f.featureId "
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
        List<FeatureRelationship> frs = performQuery(FeatureRelationship.class,
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

    private boolean featureExists(String uniqueName) {
        List<?> names = performQuery(String.class,
            "select uniqueName from Feature where uniqueName = :uniqueName",
            "uniqueName", uniqueName);
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
        for (int n=1; featureExists(nameToUse = String.format("%s:%d", uniqueName, n)); n++)
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
	return createPolypeptideDomain(domainUniqueName, polypeptide, score, description, start, end,
				       dbxref, null, null);
    }

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
     * @param evalue the E-value assigned to this domain by the prediction algorithm. Can be null.
     * @param analysis the analysis object to which to which the polypeptide domain should
                by attached via an analysisfeature
     * @return the newly-created polypeptide domain
     */
    public PolypeptideDomain createPolypeptideDomain(String domainUniqueName, Polypeptide polypeptide,
            String score, String description, int start, int end, DbXRef dbxref, String evalue,
	    Analysis analysis) {
        if (polypeptideDomainType == null) {
            polypeptideDomainType = cvDao.getCvTermByNameAndCvName("polypeptide_domain", "sequence");
        }
        if (descriptionType == null) {
            descriptionType = cvDao.getCvTermByNameAndCvName("description", "feature_property");
        }

        PolypeptideDomain domain = new PolypeptideDomain(
            polypeptide.getOrganism(), polypeptideDomainType, domainUniqueName);
        FeatureLoc domainLoc = new FeatureLoc(polypeptide, domain, start, false, end, false, (short)0/*strand*/, null, 0, 0);
        domain.addFeatureLoc(domainLoc);


        FeatureProp descriptionProp = new FeatureProp(domain, descriptionType, description, 0);
        domain.addFeatureProp(descriptionProp);

        domain.setDbXRef(dbxref);

	// Add analysisfeature
	if (analysis != null) {
	    domain.createAnalysisFeature(analysis, score, evalue);
	}

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

	return createSignalPeptide(polypeptide, loc, probability, null);
    }

    public SignalPeptide createSignalPeptide(Polypeptide polypeptide, int loc, String probability, Analysis analysis) {
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

	// Add analysisfeature
	if (analysis != null) {
	    signalPeptide.createAnalysisFeature(analysis);
	} else {
	    throw new RuntimeException("Could not create analysisfeature because analysis object is null");
	}
        return signalPeptide;
    }

    //Helix-turn-helix 22.6.2009 NDS

    private CvTerm helixTurnHelixType;
    private CvTerm maxScoreAtCvTerm;
    private CvTerm stdDeviationsCvTerm;

    public HelixTurnHelix createHelixTurnHelix(Polypeptide polypeptide, int start, int end, String score, int maxScoreAt, String stdDeviations, Analysis analysis) {

        if (helixTurnHelixType == null) {
            /* Looks for the cvterm where the dxref_id corresponds to a dbxref record
             * whose accession is 0001081 and the database is 'SO' */
            helixTurnHelixType = cvDao.getCvTermByDbAcc("SO", "0001081");
            helixTurnHelixType.getCvTermId();
        }

        String uniqueName = String.format("%s:%d-%d", polypeptide.getUniqueName(), start, end);
        HelixTurnHelix helixTurnHelix = new HelixTurnHelix(polypeptide.getOrganism(), helixTurnHelixType, uniqueName, true /*analysis*/, false /*obsolete*/);

        /* Add featureloc */
        FeatureLoc hthLoc = new FeatureLoc(polypeptide /*sourcefeature*/, helixTurnHelix, start /*fmin*/, end /*fmax*/, 0 /*strand*/, null /*phase*/, 0 /*rank*/);
        helixTurnHelix.addFeatureLoc(hthLoc);

        /* Add feature properties */
        helixTurnHelix.addFeatureProp(new Integer(maxScoreAt).toString(), "genedb_misc", "Maximum_score_at", 0 /*rank*/);
        helixTurnHelix.addFeatureProp(stdDeviations, "genedb_misc", "Standard_deviations", 0 /*rank*/);

        /* Add analysisfeature */
        if (analysis != null) {
            helixTurnHelix.createAnalysisFeature(analysis,score,null);
        } else {
            throw new RuntimeException("Could not create analysisfeature because analysis object is null");
        }
        return helixTurnHelix;
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

    /**
     * Delete all the features that are located on the specified source feature.
     * Also deletes features that are located on features located on the specified
     * source feature, e.g. ProteinMatch features located on a Polypeptide.
     *
     * @param sourceFeature
     */
    public void deleteFeaturesLocatedOn(Feature sourceFeature) {
        logger.trace(String.format("Deleting features located on '%s' (ID=%d)",
            sourceFeature.getUniqueName(), sourceFeature.getFeatureId()));

        // Similarity protein matches involve two features, a ProteinMatch
        // and a Region, that are not directly located on the top-level
        // feature. We need to deal with these separately.

        /*
         * An apparent bug in the HQL processing of Hibernate 3.3.1 GA
         * causes the corresponding HQL delete statements to be translated
         * to invalid SQL. For reference, the HQL might read as follows:
         *
         * delete Feature f where f in (
         *   select region
         *   from FeatureLoc matchOnPolypeptide
         *   left join matchOnPolypeptide.sourceFeature as polypeptide
         *           with polypeptide.class = Polypeptide
         *   left join matchOnPolypeptide.feature as match
         *    ,   FeatureLoc matchOnRegion
         *        left join matchOnRegion.sourceFeature as region
         *    ,   FeatureLoc polypeptideOnToplevel
         *   where matchOnPolypeptide.locGroup = 0
         *   and matchOnPolypeptide.rank = 0
         *   and matchOnRegion.feature = matchOnPolypeptide.feature
         *   and matchOnRegion.locGroup = 0
         *   and matchOnRegion.rank = 1
         *   and polypeptideOnToplevel.feature = polypeptide
         *   and polypeptideOnToplevel.sourceFeature.uniqueName = 'super1'
         * );
         *
         * delete Feature f where f in (
         *   select match
         *   from FeatureLoc matchOnPolypeptide
         *   left join matchOnPolypeptide.sourceFeature as polypeptide
         *           with polypeptide.class = Polypeptide
         *   left join matchOnPolypeptide.feature as match
         *    ,   FeatureLoc polypeptideOnToplevel
         *   where matchOnPolypeptide.locGroup = 0
         *   and matchOnPolypeptide.rank = 0
         *   and polypeptideOnToplevel.feature = polypeptide
         *   and polypeptideOnToplevel.sourceFeature.uniqueName = 'super1'
         * );
         *
         * I have reported this as HHH-3651
         *  (http://opensource.atlassian.com/projects/hibernate/browse/HHH-3651)
         */

        int numberOfRowsDeleted = getSession().createSQLQuery(
            " delete from feature where feature_id in (" +
            "   select feature.feature_id" +
            "   from feature" +
            "      , featureloc match_on_polypeptide" +
            "   join feature polypeptide" +
            "         on match_on_polypeptide.srcfeature_id = polypeptide.feature_id" +
            "   join featureloc match_on_region" +
            "         on match_on_region.feature_id = match_on_polypeptide.feature_id" +
            "   join featureloc polypeptide_on_toplevel" +
            "         on match_on_polypeptide.srcfeature_id = polypeptide_on_toplevel.feature_id" +
            "   where polypeptide.type_id in (" +
            "         select cvterm.cvterm_id" +
            "         from cvterm join cv on cv.cv_id = cvterm.cv_id" +
            "         where cv.name = 'sequence'" +
            "         and cvterm.name = 'polypeptide'" +
            "   )" +
            "   and match_on_polypeptide.locgroup = 0" +
            "   and match_on_polypeptide.rank = 0" +
            "   and match_on_region.locgroup = 0" +
            "   and match_on_region.rank = 1" +
            "   and polypeptide_on_toplevel.srcfeature_id = ?" +
            "   and feature.feature_id in (" +
            "     match_on_region.srcfeature_id" +
            "   , match_on_region.feature_id" +
            "   )" +
            " );")
            .setInteger(0, sourceFeature.getFeatureId())
            .executeUpdate();

        logger.debug(String.format("Deleted %d similarity features from '%s'",
            numberOfRowsDeleted, sourceFeature.getUniqueName()));

        int numberOfFirstLevelFeaturesDeleted = getSession().createQuery(
            "delete Feature f where f in (" +
            "  select fl.feature from FeatureLoc fl where fl.sourceFeature = :sourceFeature" +
            ")"
        ).setParameter("sourceFeature", sourceFeature)
        .executeUpdate();
        logger.debug(String.format("Deleted %d first-level features from '%s'",
            numberOfFirstLevelFeaturesDeleted, sourceFeature.getUniqueName()));
    }

    /**
     * Delete all the featureLocs that point to this sourceFeature
     *
     * @param sourceFeature
     */


    public void deleteFeatureLocsOn(Feature sourceFeature){
        logger.info(String.format("Deleting all the feature locs pointing at '%s' (ID=%d)",
                sourceFeature.getUniqueName(), sourceFeature.getFeatureId()));

        int numberOfRowsDeleted = getSession().createSQLQuery(
            " delete from featureloc where srcfeature_id= ? ;")
            .setInteger(0, sourceFeature.getFeatureId())
            .executeUpdate();

        logger.info(String.format("Deleted %d featurelocs pointing to '%s'",
                numberOfRowsDeleted, sourceFeature.getUniqueName()));

    }



    /* Invoked by Spring */
    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

	public List<Feature> getFeaturesByPreviousSystematicId(String id) {
        List<Feature> features = performQuery(Feature.class,
        		"select fs.feature from FeatureSynonym fs" +
        		" where fs.synonym.type.name='previous_systematic_id'" +
        		" and fs.synonym.name=:id",
                new String[] { "id" },
                new Object[] { id });

        return features;
	}
}
