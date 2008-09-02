package org.genedb.web.mvc.model;

import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;

import org.apache.log4j.Logger;

public class TranscriptDtoFactory {

    private transient Logger logger = Logger.getLogger(TranscriptDtoFactory.class);

    public TranscriptDTO makeDto(Transcript transcript) {

        logger.warn(transcript.getClass());
        if (transcript instanceof ProductiveTranscript) {
            ProductiveTranscriptDTO ret = new ProductiveTranscriptDTO();
            ret.populate((ProductiveTranscript) transcript);
            return ret;
        }

        TranscriptDTO ret = new TranscriptDTO();
        ret.populate(transcript);
        return ret;
    }

}
