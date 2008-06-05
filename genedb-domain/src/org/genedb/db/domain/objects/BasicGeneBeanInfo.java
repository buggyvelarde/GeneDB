package org.genedb.db.domain.objects;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.apache.log4j.Logger;

public class BasicGeneBeanInfo extends SimpleBeanInfo {
    private static final Logger logger = Logger.getLogger(BasicGeneBeanInfo.class);

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            return new PropertyDescriptor[] { new PropertyDescriptor("name", BasicGene.class),
                    new PropertyDescriptor("name",       BasicGene.class),
                    new PropertyDescriptor("uniqueName", BasicGene.class),
                    new PropertyDescriptor("synonyms",   BasicGene.class),
            };
        } catch (IntrospectionException e) {
            logger.error(e);
            return null;
        }
    }

}
