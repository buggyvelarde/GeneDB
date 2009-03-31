package org.genedb.db.dao;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;

import org.apache.log4j.Logger;

import java.sql.Date;

public class AuditDao {

    private static final Logger logger = Logger.getLogger(AuditDao.class);

    public Date getLastChangeForTranscript(Transcript transcript) {

        Date date = getLastChangeForFeature(transcript.getFeatureId());

        if (transcript instanceof ProductiveTranscript) {
            Polypeptide pep = ((ProductiveTranscript)transcript).getProtein();
            Date proteinDate = getLastChangeForFeature(pep.getFeatureId());
            if (proteinDate.after(date)) {
                date = proteinDate;
            }
        }

        return date;
    }

    private Date getLastChangeForFeature(int featureId) {
        return null;
    }

}
