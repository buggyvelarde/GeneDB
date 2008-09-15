package org.genedb.web.mvc.model;

import org.springframework.context.support.AbstractXmlApplicationContext;

public class MyApplicationContext extends AbstractXmlApplicationContext {

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:applicationContext.xml"};//, "classpath:genedb-servlet.xml"};
    }

}
