package org.genedb.web.mvc.view;

import org.apache.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

public class UriSuffixServiceViewResolver implements ViewResolver, Ordered {

    private static final Logger logger = Logger.getLogger(UriSuffixServiceViewResolver.class);

    private ServiceView serviceView;

    private int order;

    public void setServiceView(ServiceView serviceView) {
        this.serviceView = serviceView;
    }

    @Override
    public View resolveViewName(String arg0, Locale arg1) throws Exception {

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        Assert.isInstanceOf(ServletRequestAttributes.class, attrs);
        ServletRequestAttributes servletAttrs = (ServletRequestAttributes) attrs;
        HttpServletRequest request = servletAttrs.getRequest();

        if (hasExtension(request)) {
            return serviceView;
        }

        return null;
    }

    /**
     * Generates and appropriate extension based on the existing HTTP request.
     *
     * @param request
     * @return
     */
    private boolean hasExtension(HttpServletRequest request) {
        String uri = request.getRequestURI();
        logger.debug("parsing uri: " + uri);
        boolean extension = false;
        if ((uri.endsWith(".xml")) || (uri.endsWith(".json"))) {
            extension = true;
        }
        return extension;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
