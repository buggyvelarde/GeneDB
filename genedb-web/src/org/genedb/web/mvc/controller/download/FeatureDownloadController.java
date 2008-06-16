package org.genedb.web.mvc.controller.download;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.genedb.db.dao.SequenceDao;
import org.genedb.web.mvc.controller.BrowseCategory;
import org.genedb.web.mvc.controller.TaxonNodeBindingFormController;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.feature.MRNA;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class FeatureDownloadController extends TaxonNodeBindingFormController{
    
    private SequenceDao sequenceDao;
    private String sequenceView;
    
    public static class FeatureDownloadBean {
        private String featureName;
        private String featureType;
        private DownloadType downloadType;
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
        public DownloadType getDownloadType() {
            return downloadType;
        }
        public void setDownloadType(DownloadType downloadType) {
            this.downloadType = downloadType;
        }
    }
    
    @Override
    protected Map<String,DownloadType[]> referenceData(HttpServletRequest request) throws Exception {
        Map<String,DownloadType[]> reference = new HashMap<String,DownloadType[]>();
        reference.put("downloadTypes", DownloadType.values());
        return reference;
    }
    
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException be) throws Exception {
        
        response.setContentType("text/plain");
        FeatureDownloadBean fdb = (FeatureDownloadBean) command;
        Map<String, Object> model = new HashMap<String, Object>(1);
        Feature feature = sequenceDao.getFeatureByUniqueName(fdb.getFeatureName(), fdb.getFeatureType());
        
        MRNA transcript;
        String residues;
        String sequence;
        
        if(feature instanceof MRNA) {
            transcript = (MRNA) feature;
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
                    residues = new String(transcript.getProtein().getResidues());
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
        return new ModelAndView(sequenceView,model);
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
