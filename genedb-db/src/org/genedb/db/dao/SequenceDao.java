package org.genedb.db.dao;


import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.dao.SequenceDaoI;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureSynonym;
import org.gmod.schema.sequence.Synonym;

import java.util.List;

public class SequenceDao extends BaseDao implements SequenceDaoI {
    
 
    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureById(int)
     */
    public Feature getFeatureById(int id) {
        return (Feature) getHibernateTemplate().load(Feature.class, id);
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureByUniqueName(java.lang.String)
     */
    public Feature getFeatureByUniqueName(String name) {
        List features = getHibernateTemplate().findByNamedParam(
                "from Feature f where f.uniqueName=:name", "name", name);
        if (features.size() > 0) {
            return (Feature) features.get(0);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureByAnyCurrentName(java.lang.String)
     */
    @SuppressWarnings({ "unchecked", "cast" })
    public List<Feature> getFeaturesByAnyCurrentName(String name) {
        List<Feature> features = (List<Feature>) getHibernateTemplate().findByNamedParam(
                "select f from Feature f, FeatureSynonym fs, Synonym s where f=fs.feature and fs.synonym=s and fs.current=true and s.name=:name",
                "name", name);
        return features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureByRange(int, int, int, org.genedb.db.jpa.Feature, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByRange(int min,int max,int strand,Feature feat,String type) {
        List<Feature> features;
        int fid = feat.getFeatureId();
        //int min = loc.getMin();
        //int max = loc.getMax();
        //String name = "mRNA";
        features = getHibernateTemplate().findByNamedParam("select f " +
                "from Feature f, FeatureLoc loc, CvTerm cvt where " +
                "f.featureId=loc.featureByFeatureId and f.cvTerm=cvt.cvTermId and cvt.name=:type and loc.strand="+strand+" and" +
                " loc.featureBySrcFeatureId="+fid+" and (" +
                " loc.fmin<=:min and loc.fmax>=:max)",
                new String[]{"type", "min", "max"},
                new Object[]{type, min, max});
        return features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureByAnyName(org.genedb.db.helpers.NameLookup, java.lang.String)
     */
    @SuppressWarnings({ "unchecked", "cast" })
    public List<Feature> getFeaturesByAnyName(String name,String featureType) {

//        // Add wildcards if needed
//        if (nl.isNeedWildcards()) {
//            String lookup = nl.getLookup();
//            if (!lookup.startsWith("*")) {
//                lookup = "*" + lookup;
//            }
//            if (!lookup.endsWith("*")) {
//                lookup += "*";
//            }
//            nl.setLookup(lookup);
//            nl.setNeedWildcards(false);
//        }
//
//        String lookup = nl.getLookup().replaceAll("\\*", "%");
//
//        // TODO Start for paging
//        getHibernateTemplate().setMaxResults(nl.getPageSize()); // TODO Check

        // TODO Taxon and filter
        List<Feature> features = (List<Feature>)
        getHibernateTemplate().findByNamedParam("select f from Feature f, FeatureSynonym fs, Synonym s, CvTerm cvt where f=fs.feature and fs.synonym=s and fs.current=true and f.cvTerm=cvt.cvTermId and cvt.name='" + featureType + "' and s.name like :name",
                "name", name);
        return features;
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureCvTermByFeatureAndCvTerm(org.genedb.db.jpa.Feature, org.genedb.db.hibernate3gen.CvTerm, boolean)
     */
    @SuppressWarnings("unchecked")
    public FeatureCvTerm getFeatureCvTermByFeatureAndCvTerm(Feature feature, CvTerm cvTerm, boolean not) {
        List<FeatureCvTerm> list = getHibernateTemplate().findByNamedParam("from FeatureCvTerm fct where fct.feature=:feature and fct.cvTerm=:cvTerm and fct.not=:not", 
                new String[]{"feature", "cvTerm", "not"}, 
                new Object[]{feature, cvTerm, not});

        return firstFromList(list, "feature", feature.getUniqueName(), "cvTerm", cvTerm, "not", not);
    }

    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getSynonymsByNameAndCvTerm(java.lang.String, org.genedb.db.hibernate3gen.CvTerm)
     */
    @SuppressWarnings("unchecked")
    public Synonym getSynonymByNameAndCvTerm(String name, CvTerm type) {
        List<Synonym> tmp = getHibernateTemplate().findByNamedParam(
                "from Synonym s where s.name=:name and s.cvTerm=:cvterm",
                new String[] {"name", "cvterm"},
                new Object[] {name, type});

        return firstFromList(tmp, "name", name, "cvterm", type.getName());
    }


    /* (non-Javadoc)
     * @see org.genedb.db.dao.SequenceDaoI#getFeatureSynonymsByFeatureAndSynonym(org.genedb.db.jpa.Feature, org.genedb.db.hibernate3gen.Synonym)
     */
    @SuppressWarnings("unchecked")
    public List<FeatureSynonym> getFeatureSynonymsByFeatureAndSynonym(Feature feature, Synonym synonym) {
        return getHibernateTemplate().findByNamedParam(
                "from FeatureSynonym fs where fs.feature=:feature and fs.synonym=:synonym",
                new String[] {"feature", "synonym"},
                new Object[] {feature, synonym});
    }

    @SuppressWarnings("unchecked")
    public List<Feature> getFeaturesByLocatedOnFeature(Feature parent) {
        List<Feature> features;
        //int fid = parent.getFeatureId();
        features = getHibernateTemplate().findByNamedParam("select f " +
                "from Feature f, FeatureLoc loc, CvTerm cvt where " +
                "f.featureId=loc.featureByFeatureId and" +
                " loc.featureBySrcFeatureId=:parent",
                new String[]{"parent"},
                new Object[]{parent});
        return features;
    }

    @SuppressWarnings("unchecked")
    public List<FeatureDbXRef> getFeatureDbXRefsByFeatureUniquename(String uniqueName) {
        if (uniqueName == null) {
            return getHibernateTemplate().find("select from FeatureDbXRef");
        }
        return getHibernateTemplate().findByNamedParam(
                "from FeatureDbXRef fdxr where fdxr.feature.uniqueName=:uniqueName",
                new String[] {"uniqueName"},
                new Object[] {uniqueName});
    }

    @SuppressWarnings("unchecked")
    public List<FeatureSynonym> getFeatureSynonymsByFeatureUniquename(String uniqueName) {
        if (uniqueName == null) {
            return getHibernateTemplate().find("select from FeatureSynonym");
        }
        return getHibernateTemplate().findByNamedParam(
                "from FeatureSynonym fs where fs.feature.uniqueName=:uniqueName",
                new String[] {"uniqueName"},
                new Object[] {uniqueName});
    }

}
