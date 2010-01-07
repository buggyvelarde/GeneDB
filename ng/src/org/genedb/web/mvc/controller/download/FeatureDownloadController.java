package org.genedb.web.mvc.controller.download;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.genedb.db.dao.SequenceDao;
import org.genedb.web.mvc.controller.TaxonNodeBindingFormController;
import org.genedb.web.utils.DownloadUtils;

import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

public class FeatureDownloadController extends TaxonNodeBindingFormController{

    private SequenceDao sequenceDao;
    private String sequenceView;

    public static class FeatureDownloadBean {
        private String featureName;
        private String featureType;
        private SequenceType downloadType;
        public String getFeatureName() {
            return featureName;
        }
        public void setFeatureName(String featureName) {
            this.featureName = featureName;
        }
        public String getFeatureType() {
            return featureType;
        }
        public void setFeatureType(String featureType) {
            this.featureType = featureType;
        }
        public SequenceType getDownloadType() {
            return downloadType;
        }
        public void setDownloadType(SequenceType downloadType) {
            this.downloadType = downloadType;
        }
    }

    //TODO
    protected Map<String,SequenceType[]> referenceData(HttpServletRequest request) {
        Map<String,SequenceType[]> reference = new HashMap<String,SequenceType[]>();
        reference.put("downloadTypes", SequenceType.values());
        return reference;
    }

    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response,
            Object command, BindException be) throws Exception {

        response.setContentType("text/plain");
        FeatureDownloadBean fdb = (FeatureDownloadBean) command;
        Map<String, Object> model = new HashMap<String, Object>(1);
        Feature feature = sequenceDao.getFeatureByUniqueName(fdb.getFeatureName(), fdb.getFeatureType());

        String residues;
        String sequence;

        if(feature instanceof Transcript) {
            Transcript transcript = (Transcript) feature;
            switch (fdb.getDownloadType()) {
                case UNSPLICED_DNA:
                    //TODO don't exactly know how to handle this in case of
                    //multiple transcript (spliced genes)
                    break;

                case SPLICED_DNA:
                    residues = new String(transcript.getResidues());
                    sequence = DownloadUtils.writeFasta(transcript.getGeneUniqueName(), residues);
                    model.put("sequence", sequence.toUpperCase());
                    break;

                case PROTEIN:
                    if (!(transcript instanceof ProductiveTranscript)) {
                        be.reject("download.no-protein");
                        return showForm(request, response, be);
                    }
                    ProductiveTranscript productiveTranscript = (ProductiveTranscript) transcript;

                    residues = new String(productiveTranscript.getProtein().getResidues());
                    sequence = DownloadUtils.writeFasta(transcript.getGeneUniqueName(), residues);
                    model.put("sequence", sequence.toUpperCase());
                    break;

                case EMBL:
                    int str = transcript.getStrand();
                    boolean strand = false;
                    if(str == 1) {
                        strand = true;
                    }
                    String embl = DownloadUtils.writeEmblEntry(fdb.getFeatureType(), strand,
                            transcript.getStart(), transcript.getStop(), new HashMap<String,String>());
                    model.put("sequence", embl);
                    break;
            }

        }
        return new ModelAndView(sequenceView, model);
    }


    // TODO
    private ModelAndView showForm(HttpServletRequest request,
            HttpServletResponse response, BindException be) {
        throw new NotImplementedException("Missing code");
    }

    public SequenceDao getSequenceDao() {
        return sequenceDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public String getSequenceView() {
        return sequenceView;
    }

    public void setSequenceView(String sequenceView) {
        this.sequenceView = sequenceView;
    }
}
