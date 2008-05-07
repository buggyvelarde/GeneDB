package org.genedb.db.domain.hibernateImpls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.db.domain.objects.BasicGene;
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
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.util.StringUtils;

public class BasicGeneServiceImpl implements BasicGeneService {
    protected SessionFactory sessionFactory;
    protected static final Logger log = Logger.getLogger(BasicGeneServiceImpl.class);

    protected Feature findGeneFeatureByUniqueName(String name) {
        Query query = sessionFactory.getCurrentSession().createQuery(
                "from Feature f"
                +" where f.uniqueName=:name and f.cvTerm.name='gene'");
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
        ret.setOrganism(feat.getOrganism().getFullName());
        
        FeatureLoc loc = feat.getRankZeroFeatureLoc();
        ret.setChromosome(loc.getFeatureBySrcFeatureId().getUniqueName());
        ret.setStrand(loc.getStrand());
        ret.setFmin(loc.getFmin());
        ret.setFmax(loc.getFmax());

        return ret;
    }
    
    private static Transcript makeTranscript(Feature feature) {
        Transcript ret = new Transcript();
        Set<Exon> exons = new HashSet<Exon> ();
        for (FeatureRelationship fr : feature.getFeatureRelationshipsForObjectId()) {
            Feature otherFeat = fr.getFeatureBySubjectId();
            String otherFeatName = otherFeat.getCvTerm().getName();
            if (otherFeatName.equals("polypeptide")) {
                ret.setProtein(otherFeat);
            }
            else if (otherFeatName.equals("exon")) {
                FeatureLoc otherFeatLoc = otherFeat.getRankZeroFeatureLoc();
                exons.add(new Exon(otherFeatLoc.getFmin(), otherFeatLoc.getFmax()));
            }
        }
        ret.setExons(exons);
        return ret;
    }

    public List<String> findGeneNamesByPartialName(String search) {
        @SuppressWarnings("unchecked")
        List<String> names = sessionFactory.getCurrentSession().createQuery(
                "select f.uniqueName from Feature f where f.uniqueName like '%" + search
                        + "%' and f.cvTerm.name='gene'").list();

        if (names.size() == 0)
            return Collections.emptyList();
        else
            return names;
    }
 
    public Collection<BasicGene> findGenesOverlappingRange(String organismCommonName,
            String chromosomeUniqueName, int strand, long locMin, long locMax) {

        assert strand == 1 || strand == -1;
        @SuppressWarnings("unchecked")
        List<BasicGene> ret = sessionFactory.getCurrentSession().createQuery(
                "select f from Feature f"
                +" join f.featureLoc fl"
                +" join f.organism o"
                +" where fl.fmax > :locMin and fl.fmin < :locMax"
                +" and fl.strand = :strand"
                +" and o.commonName = :org"
                +" and f.cvTerm.name='gene'")
                .setLong   ("locMin", locMin)
                .setLong   ("locMax", locMax)
                .setInteger("strand", strand)
                .setString ("chr", chromosomeUniqueName)
                .setString ("org", organismCommonName)
                .list();
        return ret;
    }

    public Collection<BasicGene> findGenesExtendingIntoRange(String organismCommonName,
        String chromosomeUniqueName, int strand, long locMin, long locMax) {

        assert strand == 1 || strand == -1;
        @SuppressWarnings("unchecked")
        List<BasicGene> ret = sessionFactory.getCurrentSession().createQuery(
                "select f from Feature f"
                +" join f.featureLoc fl"
                +" join f.organism o"
                +" where fl.fmax > :locMin and fl.fmax <= :locMax and fl.fmin < :locMin"
                +" and fl.strand = :strand"
                +" and o.commonName = :org"
                +" and f.cvTerm.name='gene'")
                .setLong   ("locMin", locMin)
                .setLong   ("locMax", locMax)
                .setInteger("strand", strand)
                .setString ("chr", chromosomeUniqueName)
                .setString ("org", organismCommonName)
                .list();
        return ret;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}