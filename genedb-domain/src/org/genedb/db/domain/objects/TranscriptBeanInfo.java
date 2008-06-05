package org.genedb.db.domain.objects;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.apache.log4j.Logger;

public class TranscriptBeanInfo extends SimpleBeanInfo {
    private static final Logger logger = Logger.getLogger(TranscriptBeanInfo.class);
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            return new PropertyDescriptor[] {
                new PropertyDescriptor("name", Transcript.class),
                new PropertyDescriptor("fmin", Transcript.class),
                new PropertyDescriptor("fmax", Transcript.class),
                new PropertyDescriptor("gene", Transcript.class),
                // new PropertyDescriptor("exons", Transcript.class),
                // new PropertyDescriptor("colourId", Transcript.class),
                //new PropertyDescriptor("protein", Transcript.class),
                new PropertyDescriptor("products", Transcript.class),
            };
        }
        catch (IntrospectionException e) {
            logger.error(e);
            return null;
        }
    }
}
