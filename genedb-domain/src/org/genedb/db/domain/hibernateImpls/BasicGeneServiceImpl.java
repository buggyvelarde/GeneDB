package org.genedb.db.domain.hibernateImpls;

import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Chromosome;
import org.genedb.db.domain.objects.Exon;
import org.genedb.db.domain.objects.Gap;
import org.genedb.db.domain.objects.Gene;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.objects.TranscriptComponent;
import org.genedb.db.domain.services.BasicGeneService;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.FeatureSynonym;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicGeneServiceImpl implements BasicGeneService {
    protected SessionFactory sessionFactory;
    protected static final Logger log = Logger.getLogger(BasicGeneServiceImpl.class);

    protected Feature findGeneFeatureByUniqueName(String name) {
        // Fetch all the data we're going to need in a single query
        Query query = sessionFactory.getCurrentSession().createQuery(
            "select gene from Feature gene"
            +" left join fetch gene.featureLocs"
            +" left join fetch gene.featureSynonyms feature_synonym"
            +" left join fetch feature_synonym.synonym synonym"
            +" left join fetch synonym.type"
            +" inner join fetch gene.featureRelationshipsForObjectId gene_transcript"
            +" inner join fetch gene_transcript.subjectFeature transcript"
            +" inner join fetch transcript.featureRelationshipsForObjectId transcript_exon"
            +" inner join fetch transcript_exon.subjectFeature exon"
            +" where gene.uniqueName=:name and gene.type.name='gene'")
            .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
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

        ret.setUniqueName(feat.getUniqueName());
        ret.setFeatureId(feat.getFeatureId());

        List<String> synonyms = new ArrayList<String>();
        for (FeatureSynonym fs : feat.getFeatureSynonyms()) {
            String type = fs.getSynonym().getType().getName();
            if (type.equals("synonym")) {
                synonyms.add(fs.getSynonym().getName());
            }
        }
        ret.setSynonyms(synonyms);

        if (StringUtils.hasText(feat.getName())) {
            ret.setName(feat.getName());
        }

        for (FeatureRelationship fr : feat.getFeatureRelationshipsForObjectId()) {
            Feature otherFeat = fr.getSubjectFeature();
            if (otherFeat instanceof org.gmod.schema.feature.Transcript) {
                ret.addTranscript(makeTranscript(otherFeat));
            }
        }

        ret.setOrganism(feat.getOrganism().getCommonName());

        FeatureLoc loc = feat.getRankZeroFeatureLoc();
        Feature chromosomeFeature = loc.getSourceFeature();
        Chromosome chromosome = new Chromosome(chromosomeFeature.getDisplayName(), chromosomeFeature.getFeatureId(), chromosomeFeature.getSeqLen());
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

    protected Gap gapFromFeature(Feature feat) {
        FeatureLoc loc = feat.getRankZeroFeatureLoc();
        return new Gap(feat.getUniqueName(), loc.getFmin(), loc.getFmax());
    }


    protected List<Gap> gapsFromFeatures(List<Feature> features) {
        List<Gap> ret = new ArrayList<Gap>();
        for(Feature feature: features) {
            ret.add(gapFromFeature(feature));
        }
        return ret;
    }

    private static Transcript makeTranscript(Feature feature) {
        Transcript transcript = new Transcript();
        transcript.setFmin(feature.getRankZeroFeatureLoc().getFmin());
        transcript.setFmax(feature.getRankZeroFeatureLoc().getFmax());
        transcript.setUniqueName(feature.getDisplayName());

        Set<TranscriptComponent> exons = new HashSet<TranscriptComponent> ();
        for (FeatureRelationship fr : feature.getFeatureRelationshipsForObjectId()) {
            Feature relatedFeature = fr.getSubjectFeature();
            String relatedFeatureName = relatedFeature.getType().getName();
            if (relatedFeatureName.equals("polypeptide")) {
                transcript.setProtein(relatedFeature);
            }
            else if (relatedFeatureName.equals("exon")) {
                FeatureLoc otherFeatLoc = relatedFeature.getRankZeroFeatureLoc();
                exons.add(new Exon(otherFeatLoc.getFmin(), otherFeatLoc.getFmax()));
            }
        }
        transcript.setComponents(exons);

        Feature protein = transcript.getProtein();
        if (protein != null) {
            List<String> products = new ArrayList<String>();
            for (FeatureCvTerm fcvt : protein.getFeatureCvTerms()) {
                CvTerm featCvTerm = fcvt.getType();
                if (featCvTerm.getCv().getName().equals("genedb_products")) {
                    products.add(featCvTerm.getName());
                }
            }
            transcript.setProducts(products);
        }

        return transcript;
    }

    public List<String> findGeneNamesByPartialName(String partialName) {
        @SuppressWarnings("unchecked")
        List<String> names = sessionFactory.getCurrentSession().createQuery(
                "select f.uniqueName"
                +"from Feature f"
                +"where f.uniqueName like '%' || :partialName || '%'"
                +"and f.type.name='gene'")
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
                +" inner join f.featureLocs fl"
                +"    with fl.rank = 0"
                +" where fl.fmax >= :locMin and fl.fmin < :locMax"
                +" and fl.strand = :strand"
                +" and fl.sourceFeature.uniqueName = :chr"
                +" and f.organism.commonName = :org"
                +" and f.type.name='gene'")
                .setLong   ("locMin", locMin)
                .setLong   ("locMax", locMax)
                .setInteger("strand", strand)
                .setString ("chr", chromosomeUniqueName)
                .setString ("org", organismCommonName)
                .list();
        return genesFromFeatures(geneFeatures);
    }

    public Collection<Gap> findGapsOverlappingRange(String organismCommonName,
        String chromosomeUniqueName, long locMin, long locMax) {

    @SuppressWarnings("unchecked")
    List<Feature> gaps = sessionFactory.getCurrentSession().createQuery(
            "select f from Gap f"
            +" inner join f.featureLocs fl"
            +"    with fl.rank = 0"
            +" where fl.fmax >= :locMin and fl.fmin < :locMax"
            +" and fl.sourceFeature.uniqueName = :chr"
            +" and f.organism.commonName = :org")
            .setLong   ("locMin", locMin)
            .setLong   ("locMax", locMax)
            .setString ("chr", chromosomeUniqueName)
            .setString ("org", organismCommonName)
            .list();
        return gapsFromFeatures(gaps);
    }

    public Collection<Gap> findGapsOnChromosome(String organismCommonName,
            String chromosomeUniqueName) {

        @SuppressWarnings("unchecked")
        List<Feature> gaps = sessionFactory.getCurrentSession().createQuery(
            "select f from Gap f"
            +" inner join f.featureLocs fl"
            +"    with fl.rank = 0"
            +" and fl.sourceFeature.uniqueName = :chr"
            +" and f.organism.commonName = :org")
            .setString ("chr", chromosomeUniqueName)
            .setString ("org", organismCommonName)
            .list();
        return gapsFromFeatures(gaps);
    }

    public Collection<BasicGene> findGenesExtendingIntoRange(String organismCommonName,
        String chromosomeUniqueName, int strand, long locMin, long locMax) {

        assert strand == 1 || strand == -1;
        @SuppressWarnings("unchecked")
        List<Feature> geneFeatures = sessionFactory.getCurrentSession().createQuery(
                "select f from Feature f"
                +" inner join f.featureLocs fl"
                +"    with fl.rank = 0"
                +" where fl.fmax >= :locMin and fl.fmax < :locMax" // <- this line differs from above!
                +" and fl.strand = :strand"
                +" and fl.sourceFeature.uniqueName = :chr"
                +" and f.organism.commonName = :org"
                +" and f.type.name='gene'")
                .setLong   ("locMin", locMin)
                .setLong   ("locMax", locMax)
                .setInteger("strand", strand)
                .setString ("chr", chromosomeUniqueName)
                .setString ("org", organismCommonName)
                .list();
        return genesFromFeatures(geneFeatures);
    }

    public Collection<BasicGene> findGenesOnStrand(String organismCommonName,
        String chromosomeUniqueName, int strand) {
        assert strand == 1 || strand == -1;
        @SuppressWarnings("unchecked")
        List<Feature> geneFeatures = sessionFactory.getCurrentSession().createQuery(
                "select f from Feature f"
                +" inner join f.featureLocs fl"
                +"    with fl.rank = 0"
                +" where fl.strand = :strand"
                +" and fl.sourceFeature.uniqueName = :chr"
                +" and f.organism.commonName = :org"
                +" and f.type.name='gene'")
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