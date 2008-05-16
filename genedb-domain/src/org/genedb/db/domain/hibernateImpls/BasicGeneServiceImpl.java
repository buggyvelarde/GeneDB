package org.genedb.db.domain.hibernateImpls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Chromosome;
import org.genedb.db.domain.objects.Exon;
import org.genedb.db.domain.objects.Gene;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.services.BasicGeneService;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.sequence.FeatureSynonym;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.util.StringUtils;

public class BasicGeneServiceImpl implements BasicGeneService {
    protected SessionFactory sessionFactory;
    protected static final Logger log = Logger.getLogger(BasicGeneServiceImpl.class);

    protected Feature findGeneFeatureByUniqueName(String name) {
        // Fetch all the data we're going to need in a single query
        Query query = sessionFactory.getCurrentSession().createQuery(
            "from Feature gene"
            +" left join fetch gene.featureLocsForFeatureId"
            +" left join fetch gene.featureSynonyms feature_synonym"
            +" inner join fetch feature_synonym.synonym synonym"
            +" inner join fetch synonym.cvTerm"
            +" inner join fetch gene.featureRelationshipsForObjectId gene_transcript"
            +" inner join fetch gene_transcript.featureBySubjectId transcript"
            +" inner join fetch transcript.featureRelationshipsForObjectId transcript_exon"
            +" inner join fetch transcript_exon.featureBySubjectId exon"
            +" where gene.uniqueName=:name and gene.cvTerm.name='gene'")
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        @SuppressWarnings("unchecked")
        List<Feature> features = query.setString("name", name).list();

        if (features.size() == 0)
            return null;
        else
            return features.get(0);
    }

    public BasicGene findGeneByUniqueName(String name) {
        return geneFromFeature(findGeneFeatureByUniqueName(name));
    }

    protected BasicGene geneFromFeature(Feature feat) {
        BasicGene ret = new Gene();

        ret.setSystematicId(feat.getUniqueName());
        ret.setFeatureId(feat.getFeatureId());
        
        List<String> synonyms = new ArrayList<String>();
        for (FeatureSynonym fs : feat.getFeatureSynonyms()) {
            String type = fs.getSynonym().getCvTerm().getName();
            if (type.equals("synonym")) {
                synonyms.add(fs.getSynonym().getName());
            }
        }
        ret.setSynonyms(synonyms);

        if (StringUtils.hasText(feat.getName())) {
            ret.setName(feat.getName());
        }

        for (FeatureRelationship fr : feat.getFeatureRelationshipsForObjectId()) {
            Feature otherFeat = fr.getFeatureBySubjectId();
            if (otherFeat.getCvTerm().getName().equals("mRNA")) {
                ret.addTranscript(makeTranscript(otherFeat));
            }
        }

        // Collect all the products of all the proteins of all the transcripts
        Set<String> products = new HashSet<String>();
        for (Transcript transcript: ret.getTranscripts()) {
            Feature protein = transcript.getProtein();
    
            for (FeatureCvTerm fcvt : protein.getFeatureCvTerms()) {
                CvTerm featCvTerm = fcvt.getCvTerm();
                if (featCvTerm.getCv().getName().equals("genedb_products")) {
                    products.add(featCvTerm.getName());
                }
            }
        }
        ret.setProducts(new ArrayList<String> (products));
        ret.setOrganism(feat.getOrganism().getCommonName());
        
        FeatureLoc loc = feat.getRankZeroFeatureLoc();
        Feature chromosomeFeature = loc.getFeatureBySrcFeatureId();
        Chromosome chromosome = new Chromosome(chromosomeFeature.getDisplayName(), chromosomeFeature.getSeqLen());
        ret.setChromosome(chromosome);
        ret.setStrand(loc.getStrand());
        ret.setFmin(loc.getFmin());
        ret.setFmax(loc.getFmax());

        return ret;
    }
    
    protected List<BasicGene> genesFromFeatures(List<Feature> features) {
        List<BasicGene> ret = new ArrayList<BasicGene>();
        for(Feature feature: features) {
            ret.add(geneFromFeature(feature));
        }
        return ret;
    }
    
    private static Transcript makeTranscript(Feature feature) {
        Transcript ret = new Transcript();
        ret.setFmin(feature.getRankZeroFeatureLoc().getFmin());
        ret.setFmax(feature.getRankZeroFeatureLoc().getFmax());
        ret.setName(feature.getDisplayName());
        
        Set<Exon> exons = new HashSet<Exon> ();
        for (FeatureRelationship fr : feature.getFeatureRelationshipsForObjectId()) {
            Feature relatedFeature = fr.getFeatureBySubjectId();
            String relatedFeatureName = relatedFeature.getCvTerm().getName();
            if (relatedFeatureName.equals("polypeptide")) {
                ret.setProtein(relatedFeature);
            }
            else if (relatedFeatureName.equals("exon")) {
                FeatureLoc otherFeatLoc = relatedFeature.getRankZeroFeatureLoc();
                exons.add(new Exon(otherFeatLoc.getFmin(), otherFeatLoc.getFmax()));
            }
        }
        ret.setExons(exons);
        return ret;
    }

    public List<String> findGeneNamesByPartialName(String partialName) {
        @SuppressWarnings("unchecked")
        List<String> names = sessionFactory.getCurrentSession().createQuery(
                "select f.uniqueName"
                +"from Feature f"
                +"where f.uniqueName like '%' || :partialName || '%'"
                +"and f.cvTerm.name='gene'")
                .setString("partialName", partialName)
                .list();

        if (names.size() == 0)
            return Collections.emptyList();
        else
            return names;
    }
 
    public Collection<BasicGene> findGenesOverlappingRange(String organismCommonName,
            String chromosomeUniqueName, int strand, long locMin, long locMax) {

        assert strand == 1 || strand == -1;
        @SuppressWarnings("unchecked")
        List<Feature> geneFeatures = sessionFactory.getCurrentSession().createQuery(
                "select f from Feature f"
                +"inner join f.featureLocsForFeatureId fl"
                +"    with fl.rank = 0"
                +"where fl.fmax >= :locMin and fl.fmin < :locMax"
                +"and fl.strand = :strand"
                +"and fl.featureBySrcFeatureId.uniqueName = :chr"
                +"and f.organism.commonName = :org"
                +"and f.cvTerm.name='gene'")
                .setLong   ("locMin", locMin)
                .setLong   ("locMax", locMax)
                .setInteger("strand", strand)
                .setString ("chr", chromosomeUniqueName)
                .setString ("org", organismCommonName)
                .list();
        return genesFromFeatures(geneFeatures);
    }

    public Collection<BasicGene> findGenesExtendingIntoRange(String organismCommonName,
        String chromosomeUniqueName, int strand, long locMin, long locMax) {

        assert strand == 1 || strand == -1;
        @SuppressWarnings("unchecked")
        List<Feature> geneFeatures = sessionFactory.getCurrentSession().createQuery(
                "select f from Feature f"
                +"inner join f.featureLocsForFeatureId fl"
                +"    with fl.rank = 0"
                +" where fl.fmax >= :locMin and fl.fmax < :locMax" // <- this line differs from above!
                +"and fl.strand = :strand"
                +"and fl.featureBySrcFeatureId.uniqueName = :chr"
                +"and f.organism.commonName = :org"
                +"and f.cvTerm.name='gene'")
                .setLong   ("locMin", locMin)
                .setLong   ("locMax", locMax)
                .setInteger("strand", strand)
                .setString ("chr", chromosomeUniqueName)
                .setString ("org", organismCommonName)
                .list();
        return genesFromFeatures(geneFeatures);
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}