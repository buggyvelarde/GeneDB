package org.genedb.web.mvc.view;

import org.apache.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;
import java.util.Map;

public class PrefixViewResolver implements ViewResolver, Ordered {

    private Logger logger = Logger.getLogger(PrefixViewResolver.class);

    private Map<String, View> prefixMap;

    private int order;

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        int index = viewName.indexOf(":");
        if (index == -1) {
            return null;
        }
        String prefix = viewName.substring(0, index);
        logger.trace("Prefix is '"+prefix+"'");
        if (prefixMap.containsKey(prefix)) {
            View ret = prefixMap.get(prefix);
            logger.trace(String.format("Returning view '%s'", ret));
            return ret;
        }
        return null;
    }

    public void setPrefixMap(Map<String, View> prefixMap) {
        this.prefixMap = prefixMap;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
