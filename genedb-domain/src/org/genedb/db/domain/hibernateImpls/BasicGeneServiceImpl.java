package org.genedb.db.domain.hibernateImpls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Gene;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.services.BasicGeneService;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureRelationship;
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

        if (StringUtils.hasText(feat.getName())) {
            ret.setName(feat.getName());
        }

        for (FeatureRelationship fr : feat.getFeatureRelationshipsForObjectId()) {
            Feature otherFeat = fr.getFeatureBySubjectId();
            if (otherFeat.getCvTerm().getName().equals("mRNA")) {
                ret.addTranscript(Transcript.makeTranscript(otherFeat));
            }
        }
        if (ret.getTranscripts().size() > 1) {
            log.error("Multiple transcripts for '" + feat.getUniqueName() + "' not handled yet");
        }

        Transcript transcript = ret.getTranscripts().get(0);
        Feature protein = transcript.getProtein();

        List<String> products = new ArrayList<String>();
        for (FeatureCvTerm fcvt : protein.getFeatureCvTerms()) {
            CvTerm featCvTerm = fcvt.getCvTerm();
            if (featCvTerm.getCv().getName().equals("genedb_products")) {
                products.add(featCvTerm.getName());
            }
        }
        ret.setProducts(products);
        ret.setOrganism(feat.getOrganism().getFullName());

        FeatureLoc loc = feat.getFeatureLoc();
        ret.setFmin(loc.getFmin());
        ret.setFmax(loc.getFmax());

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


    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}