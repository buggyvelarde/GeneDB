package org.genedb.web.mvc.view;

import org.apache.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;
import java.util.Map;

public class PrefixViewResolver implements ViewResolver, Ordered {

    private Logger logger = Logger.getLogger(PrefixViewResolver.class);

    private Map<String, ViewResolver> prefixMap;

    private int order;

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        int index = viewName.indexOf(":");
        if (index == -1) {
            return null;
        }
        String prefix = viewName.substring(0, index);
        String suffix= viewName.substring(index+1);
        logger.error("Prefix is '"+prefix+"'");
        if (prefixMap.containsKey(prefix)) {
            ViewResolver vr = prefixMap.get(prefix);
            logger.error(String.format("Returning view '%s' to '%s'", suffix, vr));
            return vr.resolveViewName(suffix, locale);
        }
        return null;
    }

    public void setPrefixMap(Map<String, ViewResolver> prefixMap) {
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
