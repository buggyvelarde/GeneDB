package org.genedb.db.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.dao.CvDaoI;
import org.gmod.schema.dao.SequenceDaoI;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.sequence.FeatureSynonym;
import org.gmod.schema.sequence.Synonym;
import org.gmod.schema.sequence.feature.Gene;
import org.gmod.schema.sequence.feature.Polypeptide;
import org.gmod.schema.sequence.feature.PolypeptideDomain;
import org.gmod.schema.utils.CountedName;
import org.gmod.schema.utils.GeneNameOrganism;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * @author rh11
 *
 */
public class SequenceDao extends BaseDao implements SequenceDaoI {

    private static final Logger logger = Logger.getLogger(SequenceDao.class);

    private CvDaoI cvDao;

    public Feature getFeatureById(int id) {
        return (Feature) getHibernateTemplate().load(Feature.class, id);
    }

    public Feature getFeatureByUniqueName(String uniqueName, String featureType) {
        if(featureType.equals("gene"))
            return getFeatureByUniqueName(uniqueName, Gene.class);

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

    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByUniqueName(String name) {
        List features = getHibernateTemplate().findByNamedParam(
                "from Feature where uniqueName like :name", "name", name);
        return features;
    }

    public List<Feature> getFeaturesByAnyCurrentName(String name) {
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select f from Feature f, FeatureSynonym fs, Synonym s where f=fs.feature and fs.synonym=s and fs.current=true and s.name=:name",
                        "name", name);
        return features;
    }

    public List<Feature> getFeaturesByRange(int min, int max, int strand, Feature feat, String type) {
        int fid = feat.getFeatureId();
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select f "
                                + "from Feature f, FeatureLoc loc, CvTerm cvt where "
                                + "f.featureId=loc.featureByFeatureId and f.cvTerm=cvt.cvTermId and cvt.name=:type and loc.strand="
                                + strand + " and" + " loc.featureBySrcFeatureId=" + fid + " and ("
                                + " loc.fmin<=:min and loc.fmax>=:max)",
                        new String[] { "type", "min", "max" }, new Object[] { type, min, max });
        return features;
    }

    public List<Feature> getFeaturesByAnyName(String name, String featureType) {
        // TODO Taxon and filter
        String lookup = name.replaceAll("\\*", "%");

        logger.info("lookup is " + lookup);
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select f from Feature f, FeatureSynonym fs, Synonym s, CvTerm cvt where f=fs.feature and fs.synonym=s and fs.current=true and f.cvTerm=cvt.cvTermId and cvt.name=:featureType and s.name like :lookup",
                        new String[] { "lookup", "featureType" },
                        new Object[] { lookup, featureType });
        return features;
    }

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

    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByCvNameAndCvTermNameAndOrganisms(String cvName,
            String cvTermName, String orgs) {
        logger.info("Querying with cvName='" + cvName + "' cvTermName='" + cvTermName + "' orgs='"
                + orgs + "'");
        return getHibernateTemplate()
                .findByNamedParam(
                        "select f"
                                + " from CvTerm cvt,FeatureCvTerm fct,Feature f "
                                + "where f.organism.commonName in ("
                                + orgs
                                + ") and f=fct.feature and cvt=fct.cvTerm and cvt.cv.name=:cvName and cvt.name=:cvTermName",
                        new String[] { "cvName", "cvTermName" },
                        new Object[] { cvName, cvTermName });
    }

    public Synonym getSynonymByNameAndCvTerm(String name, CvTerm type) {
        @SuppressWarnings("unchecked")
        List<Synonym> list = getHibernateTemplate().findByNamedParam(
                "from Synonym s where s.name=:name and s.cvTerm=:cvterm",
                new String[] { "name", "cvterm" }, new Object[] { name, type });

        return firstFromList(list, "name", name, "cvterm", type.getName());
    }

    @SuppressWarnings("unchecked")
    public List<FeatureSynonym> getFeatureSynonymsByFeatureAndSynonym(Feature feature,
            Synonym synonym) {
        return getHibernateTemplate().findByNamedParam(
                "from FeatureSynonym fs where fs.feature=:feature and fs.synonym=:synonym",
                new String[] { "feature", "synonym" }, new Object[] { feature, synonym });
    }

    public List<Feature> getFeaturesByLocatedOnFeature(Feature parent) {
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate().findByNamedParam(
                "select f " + "from Feature f, FeatureLoc loc, CvTerm cvt where "
                        + "f.featureId=loc.featureByFeatureId and"
                        + " loc.featureBySrcFeatureId=:parent", new String[] { "parent" },
                new Object[] { parent });
        return features;
    }

    @SuppressWarnings("unchecked")
    public List<FeatureDbXRef> getFeatureDbXRefsByFeatureUniquename(String uniqueName) {
        if (uniqueName == null) {
            return getHibernateTemplate().find("select from FeatureDbXRef");
        }
        return getHibernateTemplate().findByNamedParam(
                "from FeatureDbXRef fdxr where fdxr.feature.uniqueName=:uniqueName",
                new String[] { "uniqueName" }, new Object[] { uniqueName });
    }

    @SuppressWarnings("unchecked")
    public List<FeatureSynonym> getFeatureSynonymsByFeatureUniquename(String uniqueName) {
        if (uniqueName == null) {
            return getHibernateTemplate().find("select from FeatureSynonym");
        }
        return getHibernateTemplate().findByNamedParam(
                "from FeatureSynonym fs where fs.feature.uniqueName=:uniqueName",
                new String[] { "uniqueName" }, new Object[] { uniqueName });
    }

    @SuppressWarnings("unchecked")
    public List<List<?>> getFeatureByGO(String go) {
        String temp[] = go.split(":");
        String number = temp[1];
        List<Feature> polypeptides;
        List<CvTerm> goName;
        List<Feature> features = new ArrayList<Feature>();
        polypeptides = getHibernateTemplate().findByNamedParam(
                "select f " + "from Feature f, DbXRef d, CvTerm c, FeatureCvTerm fc where "
                        + "d.accession=:number and d.dbXRefId=c.dbXRef and c.cvTermId=fc.cvTerm "
                        + "and fc.feature=f.featureId", new String[] { "number" },
                new Object[] { number });
        for (Feature polypep : polypeptides) {
            logger.info(polypep.getUniqueName());
            List<Feature> genes = getHibernateTemplate()
                    .findByNamedParam(
                            "select f "
                                    + "from Feature f,FeatureRelationship f1,FeatureRelationship f2 where "
                                    + "f2.featureBySubjectId=:polypep and f2.featureByObjectId=f1.featureBySubjectId "
                                    + "and f1.featureByObjectId=f", new String[] { "polypep" },
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
                "select f from Feature f " + "where f.cvTerm.name=:name", new String[] { "name" },
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
                                + " f.uniqueName like :lookup and f.cvTerm.name=:featureType and f.organism.commonName in ( "
                                + orgNames + " )", new String[] { "lookup", "featureType" },
                        new Object[] { lookup, featureType, });

        return features;
    }

    // Maybe replace this with lucene query
    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByAnyNameOrProductAndOrganism(String nl, String orgs,
            String featureType) {

        String lookup = nl.replaceAll("\\*", "%");

        logger.info("Lookup='" + lookup + "' featureType='" + featureType + "' orgs='" + orgs
                        + "'");
        // The list of orgs is being included literally as it didn't seem to
        // work as a parameter
        return getHibernateTemplate()
                .findByNamedParam(
                        "select f from Feature f, FeatureProp fp where ("
                        + " f.uniqueName like :lookup or ("
                        + "   fp.cvTerm.cv.name = 'genedb_products'"
                        + "   and fp.cvTerm.name like :lookup and fp.feature = f"
                        + " )"
                        + ")"
                        + " and f.cvTerm.name = :featureType"
                        + " and f.organism.commonName in ( " + orgs + " )",
                        new String[] { "lookup", "featureType" },
                        new Object[] { lookup, featureType, });
    }

    @SuppressWarnings("unchecked")
    // FIXME - Remove hard coded value - make more general?
    public List<CountedName> getProducts() {
        return getHibernateTemplate().find(
                "select new CountedName(cvt.name,count(f.uniqueName))"
                        + " from CvTerm cvt,FeatureCvTerm fct,Feature f "
                        + "where f=fct.feature and cvt=fct.cvTerm and cvt.cv=15 group by cvt.name");
    }

    public List<Feature> getFeaturesByCvTermName(String cvTermName) {
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate().findByNamedParam(
                "select f.feature from FeatureCvTerm f where f.cvTerm.name like :cvTermName",
                "cvTermName", cvTermName);
        return features;
    }

    // FIXME - Use top level properties instead
    public List<Feature> getTopLevelFeatures() {
        String name = "chromosome%";
        @SuppressWarnings("unchecked")
        List<Feature> topLevels = getHibernateTemplate().findByNamedParam(
                "from Feature where cvTerm.name like :name", "name", name);
        return topLevels;
    }

    public List<Feature> getFeaturesByCvTermNameAndCvName(String cvTermName, String cvName) {
        @SuppressWarnings("unchecked")
        List<Feature> features = getHibernateTemplate()
                .findByNamedParam(
                        "select f.feature from FeatureCvTerm f where f.cvTerm.name like :cvTermName and f.cvTerm.cv.name like :cvName",
                        new String[] { "cvTermName", "cvName" },
                        new Object[] { cvTermName, cvName });
        return features;
    }

    /**
     * Given the specification of a CvTerm, find all the genes that
     * have transcripts that have polypeptides that have this CvTerm
     * associated to them. In practice, this is used to get a list of
     * genes that have a particular Gene Ontology annotation, for example.
     */
    public List<GeneNameOrganism> getGeneNameOrganismsByCvTermNameAndCvName(String cvTermName, String cvName) {
        @SuppressWarnings("unchecked")
        List<GeneNameOrganism> features = getHibernateTemplate()
                .findByNamedParam(
                        "select new org.gmod.schema.utils.GeneNameOrganism( " +
                        "transcript_gene.featureByObjectId.uniqueName, transcript_gene.featureByObjectId.organism.abbreviation) " +
                        "from " +
                        "FeatureRelationship transcript_gene, FeatureRelationship polypeptide_transcript " +
                        "where transcript_gene.featureBySubjectId=polypeptide_transcript.featureByObjectId and " +
                        "polypeptide_transcript.cvTerm.name='derives_from' and " +
                        "polypeptide_transcript.featureBySubjectId in ( " +
                        "select fct.feature from FeatureCvTerm fct where " +
                        "fct.cvTerm.name like :cvTermName and fct.cvTerm.cv.name like :cvName) " +
                        "order by transcript_gene.featureByObjectId.organism.abbreviation",
                        new String[] { "cvTermName", "cvName" },
                        new Object[] { cvTermName, cvName });
        return features;
    }

    @SuppressWarnings("unchecked") // findByNamedParam(query) returns a bare List
    public List<String> getPossibleMatches(String name, CvTerm cvTerm, int limit) {
        HibernateTemplate ht = new HibernateTemplate(getSessionFactory());
        ht.setMaxResults(limit);

        return ht
                .findByNamedParam(
                        "select f.uniqueName from Feature f where lower(f.uniqueName) like lower(:name) and f.cvTerm = :cvTerm",
                        new String[] { "name", "cvTerm" },
                        new Object[] { "%" + name + "%", cvTerm });
    }

    public List<Feature> getFeaturesByOrganism(Organism org) {
        @SuppressWarnings("unchecked") // findByNamedParam(query) returns a bare List
        List<Feature> features = getHibernateTemplate().findByNamedParam(
                "from Feature f where f.organism=:org", "org", org);
        return features;
    }

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

    public List<Feature> getFeaturesByLocation(int min, int max, String type, String organism,
            Feature parent) {
        @SuppressWarnings("unchecked") // findByNamedParam(query) returns a bare List
        List<Feature> features = getHibernateTemplate().findByNamedParam(
                "select f from Feature f , FeatureLoc fl " + "where fl.fmin>=:min "
                        + "and fl.fmax<=:max and fl.featureByFeatureId=f.featureId "
                        + "and fl.featureBySrcFeatureId=:parent and f.cvTerm.name=:type "
                        + "and f.organism.commonName=:organism",
                new String[] { "min", "max", "type", "organism", "parent" },
                new Object[] { min, max, type, organism, parent });

        return features;
    }

    public FeatureRelationship getFeatureRelationshipBySubjectObjectAndRelation(Feature subject,
            Feature object, CvTerm relation) {
        @SuppressWarnings("unchecked") // findByNamedParam(query) returns a bare List
        List<FeatureRelationship> frs = getHibernateTemplate().findByNamedParam(
                "from FeatureRelationship fr "
                        + "where fr.featureBySubjectId=:subject and fr.featureByObjectId=:object "
                        + "and fr.cvTerm=:relation",
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
    private String makeNameUnique(String uniqueName) {
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

    private CvTerm polypeptideDomainType, scoreType, descriptionType;
    /**
     * Create a new polypeptide domain feature
     *
     * @param polypeptide the polypeptide to which this domain feature should be attached
     * @param id a string identifying the domain
     * @param score an indication, from the algorithm that predicted this domain,
     *          of the confidence of the prediction. Usually a number.
     * @param description description of the doman
     * @param start the start of the domain, relative to the polypeptide, in interbase coordinates
     * @param end the end of the domain, relative to the polypeptide, in interbase coordinates
     * @param dbxref a database reference for this domain, if applicable. Can be null.
     * @return the newly-created polypeptide domain
     */
    public PolypeptideDomain createPolypeptideDomain(Polypeptide polypeptide,
            String id, String score, String description, int start, int end, DbXRef dbxref) {
        if (polypeptideDomainType == null)
            polypeptideDomainType = cvDao.getCvTermByNameAndCvName("polypeptide_domain", "sequence");
        if (scoreType == null)
            scoreType = cvDao.getCvTermByNameAndCvName("score", "null");
        if (descriptionType == null)
            descriptionType = cvDao.getCvTermByNameAndCvName("description", "feature_property");

        String domainUniqueName = makeNameUnique(String.format("%s:%s",polypeptide.getUniqueName(), id));
        PolypeptideDomain domain = new PolypeptideDomain(
            polypeptide.getOrganism(), polypeptideDomainType, domainUniqueName);
        FeatureLoc domainLoc = new FeatureLoc(polypeptide, domain, start, false, end, false, (short)0/*strand*/, null, 0, 0);
        domain.addFeatureLocsForFeatureId(domainLoc);

        FeatureProp scoreProp = new FeatureProp(domain, scoreType, score, 0);
        domain.addFeatureProp(scoreProp);

        FeatureProp descriptionProp = new FeatureProp(domain, descriptionType, description, 0);
        domain.addFeatureProp(descriptionProp);

        domain.setDbXRef(dbxref);

        persist(domain);

        return domain;
        // TODO Add interproDbxref as additional parameter?
    }

    /* Invoked by Spring */
    public void setCvDao(CvDaoI cvDao) {
        this.cvDao = cvDao;
    }
}
