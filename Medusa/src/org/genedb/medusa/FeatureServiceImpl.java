package org.genedb.medusa;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.hibernate.NonUniqueResultException;
import org.hibernate.SessionFactory;

import java.util.HashMap;
import java.util.Map;

public class FeatureServiceImpl implements FeatureService {
    protected SessionFactory sessionFactory;
    protected static final Logger log = Logger.getLogger(FeatureServiceImpl.class);

//    protected Feature findGeneFeatureByUniqueName(String name) {
//        // Fetch all the data we're going to need in a single query
//        Query query = sessionFactory.getCurrentSession().createQuery(
//            "from Feature gene"
//            +" left join fetch gene.featureLocs"
//            +" left join fetch gene.featureSynonyms feature_synonym"
//            +" inner join fetch feature_synonym.synonym synonym"
//            +" inner join fetch synonym.type"
//            +" inner join fetch gene.featureRelationshipsForObjectId gene_transcript"
//            +" inner join fetch gene_transcript.subjectFeature transcript"
//            +" inner join fetch transcript.featureRelationshipsForObjectId transcript_exon"
//            +" inner join fetch transcript_exon.subjectFeature exon"
//            +" where gene.uniqueName=:name and gene.type.name='gene'")
//            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
//        @SuppressWarnings("unchecked")
//        List<Feature> features = query.setString("name", name).list();
//
//        if (features.size() == 0)
//            return null;
//        else
//            return features.get(0);
//    }
//
//    public BasicGene findGeneByUniqueName(String name) {
//        return geneFromFeature(findGeneFeatureByUniqueName(name));
//    }
//
//    protected BasicGene geneFromFeature(Feature feat) {
//        BasicGene ret = new Gene();
//
//        ret.setUniqueName(feat.getUniqueName());
//        ret.setFeatureId(feat.getFeatureId());
//
//        List<String> synonyms = new ArrayList<String>();
//        for (FeatureSynonym fs : feat.getFeatureSynonyms()) {
//            String type = fs.getSynonym().getType().getName();
//            if (type.equals("synonym")) {
//                synonyms.add(fs.getSynonym().getName());
//            }
//        }
//        ret.setSynonyms(synonyms);
//
//        if (StringUtils.hasText(feat.getName())) {
//            ret.setName(feat.getName());
//        }
//
//        for (FeatureRelationship fr : feat.getFeatureRelationshipsForObjectId()) {
//            Feature otherFeat = fr.getFeatureBySubjectId();
//            if (otherFeat.getCvTerm().getName().equals("mRNA")) {
//                ret.addTranscript(makeTranscript(otherFeat));
//            }
//        }
//
//        ret.setOrganism(feat.getOrganism().getCommonName());
//
//        FeatureLoc loc = feat.getRankZeroFeatureLoc();
//        Feature chromosomeFeature = loc.getSourceFeature();
//        Chromosome chromosome = new Chromosome(chromosomeFeature.getDisplayName(), chromosomeFeature.getSeqLen());
//        ret.setChromosome(chromosome);
//        ret.setStrand(loc.getStrand());
//        ret.setFmin(loc.getFmin());
//        ret.setFmax(loc.getFmax());
//
//        return ret;
//    }
//
//    protected List<BasicGene> genesFromFeatures(List<Feature> features) {
//        List<BasicGene> ret = new ArrayList<BasicGene>();
//        for(Feature feature: features) {
//            ret.add(geneFromFeature(feature));
//        }
//        return ret;
//    }
//
//    private static Transcript makeTranscript(Feature feature) {
//        Transcript transcript = new Transcript();
//        transcript.setFmin(feature.getRankZeroFeatureLoc().getFmin());
//        transcript.setFmax(feature.getRankZeroFeatureLoc().getFmax());
//        transcript.setName(feature.getDisplayName());
//
//        Set<Exon> exons = new HashSet<Exon> ();
//        for (FeatureRelationship fr : feature.getFeatureRelationshipsForObjectId()) {
//            Feature relatedFeature = fr.getSubjectFeature();
//            String relatedFeatureName = relatedFeature.getType().getName();
//            if (relatedFeatureName.equals("polypeptide")) {
//                transcript.setProtein(relatedFeature);
//            }
//            else if (relatedFeatureName.equals("exon")) {
//                FeatureLoc otherFeatLoc = relatedFeature.getRankZeroFeatureLoc();
//                exons.add(new Exon(otherFeatLoc.getFmin(), otherFeatLoc.getFmax()));
//            }
//        }
//        transcript.setExons(exons);
//
//        Feature protein = transcript.getProtein();
//        List<String> products = new ArrayList<String>();
//        for (FeatureCvTerm fcvt : protein.getFeatureCvTerms()) {
//            CvTerm featCvTerm = fcvt.getCvTerm();
//            if (featCvTerm.getCv().getName().equals("genedb_products")) {
//                products.add(featCvTerm.getName());
//            }
//        }
//        transcript.setProducts(products);
//
//        return transcript;
//    }
//
//    public List<String> findGeneNamesByPartialName(String partialName) {
//        @SuppressWarnings("unchecked")
//        List<String> names = sessionFactory.getCurrentSession().createQuery(
//                "select f.uniqueName"
//                +"from Feature f"
//                +"where f.uniqueName like '%' || :partialName || '%'"
//                +"and f.type.name='gene'")
//                .setString("partialName", partialName)
//                .list();
//
//        if (names.size() == 0)
//            return Collections.emptyList();
//        else
//            return names;
//    }
//
//    public Collection<BasicGene> findGenesOverlappingRange(String organismCommonName,
//            String chromosomeUniqueName, int strand, long locMin, long locMax) {
//
//        assert strand == 1 || strand == -1;
//        @SuppressWarnings("unchecked")
//        List<Feature> geneFeatures = sessionFactory.getCurrentSession().createQuery(
//                "select f from Feature f"
//                +"inner join f.featureLocs fl"
//                +"    with fl.rank = 0"
//                +"where fl.fmax >= :locMin and fl.fmin < :locMax"
//                +"and fl.strand = :strand"
//                +"and fl.sourceFeature.uniqueName = :chr"
//                +"and f.organism.commonName = :org"
//                +"and f.type.name='gene'")
//                .setLong   ("locMin", locMin)
//                .setLong   ("locMax", locMax)
//                .setInteger("strand", strand)
//                .setString ("chr", chromosomeUniqueName)
//                .setString ("org", organismCommonName)
//                .list();
//        return genesFromFeatures(geneFeatures);
//    }
//
//    public Collection<BasicGene> findGenesExtendingIntoRange(String organismCommonName,
//        String chromosomeUniqueName, int strand, long locMin, long locMax) {
//
//        assert strand == 1 || strand == -1;
//        @SuppressWarnings("unchecked")
//        List<Feature> geneFeatures = sessionFactory.getCurrentSession().createQuery(
//                "select f from Feature f"
//                +"inner join f.featureLocs fl"
//                +"    with fl.rank = 0"
//                +" where fl.fmax >= :locMin and fl.fmax < :locMax" // <- this line differs from above!
//                +"and fl.strand = :strand"
//                +"and fl.featureBySrcFeatureId.uniqueName = :chr"
//                +"and f.organism.commonName = :org"
//                +"and f.type.name='gene'")
//                .setLong   ("locMin", locMin)
//                .setLong   ("locMax", locMax)
//                .setInteger("strand", strand)
//                .setString ("chr", chromosomeUniqueName)
//                .setString ("org", organismCommonName)
//                .list();
//        return genesFromFeatures(geneFeatures);
//    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    Map<String, String> conventionalLocation = new HashMap<String, String>();
    CvService cvService;

    @Override
    public CvTerm findConventionalFeatureForProperty(CvTerm cvTerm) {
        String key = cvTerm.getCv().getName()+"::"+cvTerm.getName();
        String featureTypeName = conventionalLocation.get(key);
        if (featureTypeName == null) {
            log.error("Can't find where to store this");
        }

        CvTerm soType = cvService.findCvTermByCvAndName("sequence", featureTypeName);
        if (soType == null) {
            log.error("Can't find sequence type");
            throw new RuntimeException();
        }
        return soType;
    }

    @Override
    public Feature findFeature(String systematicId) {
        try {
            Feature feature = (Feature) sessionFactory.getCurrentSession().createQuery(
            "select f from Feature f where f.uniqueName = :systematicId")
            .setString("systematicId", systematicId)
            .uniqueResult();
            return feature;
        }
        catch (NonUniqueResultException exp) {
            log.error(String.format("Got more than 1 result when should have had one for an uniquename of '%s'", systematicId), exp);
            return null;
        }
    }

    @Override
    public Feature findGenePart(String systematicId, CvTerm featureType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String findTypeNameForSystematicId(String systematicId) {
        try {
            String soTypeName =  (String) sessionFactory.getCurrentSession().createQuery(
            "select f.cvTerm.name from Feature f where f.uniqueName = :systematicId")
            .setString("systematicId", systematicId)
            .uniqueResult();
            return soTypeName;
        }
        catch (NonUniqueResultException exp) {
            log.error(String.format("Got more than 1 result when should have had one for an uniquename of '%s'", systematicId), exp);
            return null;
        }
    }
}