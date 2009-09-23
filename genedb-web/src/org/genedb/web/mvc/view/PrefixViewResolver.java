package org.genedb.web.mvc.view;

import org.apache.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;
import java.util.Map;

public class PrefixViewResolver implements ViewResolver, Ordered {

    private Logger logger = Logger.getLogger(PrefixViewResolver.class);

    private Map<String, ViewResolver> viewResolverMap;
    private Map<String, View> viewMap;

    public void setViewMap(Map<String, View> viewMap) {
        this.viewMap = viewMap;
    }

    private int order;

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        int index = viewName.indexOf(":");
        if (index == -1) {
            return null;
        }
        String prefix = viewName.substring(0, index);
        String suffix="";
        if (index < viewName.length() ) {
            suffix= viewName.substring(index+1);
        }
        logger.error("Prefix is '"+prefix+"'");
        if (viewResolverMap.containsKey(prefix)) {
            ViewResolver vr = viewResolverMap.get(prefix);
            logger.error(String.format("Returning view '%s' to '%s'", suffix, vr));
            return vr.resolveViewName(suffix, locale);
        }
        if (viewMap.containsKey(prefix)) {
            View view = null;//new View();
            //logger.error(String.format("Returning view '%s' to '%s'", suffix, vr));
            return view;
        }
        return null;
    }

    public void setViewResolverMap(Map<String, ViewResolver> prefixMap) {
        this.viewResolverMap = prefixMap;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
