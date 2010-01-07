package org.genedb.web.mvc.view;

import org.apache.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;

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
        logger.trace("Prefix is '"+prefix+"'");
        if ("redirect".equals(prefix)) {
            return new RedirectView(suffix, true);
        }
        if (viewResolverMap.containsKey(prefix)) {
            ViewResolver vr = viewResolverMap.get(prefix);
            logger.trace(String.format("Returning view '%s' to '%s'", suffix, vr));
            return vr.resolveViewName(suffix, locale);
        }
        if (viewMap.containsKey(prefix)) {
            View view = viewMap.get(prefix);
            logger.trace(String.format("Returning view of type '%s'", view.getClass()));
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
