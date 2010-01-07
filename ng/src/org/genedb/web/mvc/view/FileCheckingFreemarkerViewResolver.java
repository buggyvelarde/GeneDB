package org.genedb.web.mvc.view;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FileCheckingFreemarkerViewResolver implements ViewResolver, ApplicationContextAware {

    private Logger logger = Logger.getLogger(FileCheckingFreemarkerViewResolver.class);

    private Map<String, FreeMarkerView> validViews = Maps.newHashMap();

//    private String initialPrefix;

    private String prefix;

    private int order;

    public void setOrder(int order) {
        this.order = order;
    }


    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }


    public void setAppCxt(WebApplicationContext appCxt) {
        this.appCxt = appCxt;
    }


    private String suffix;

    private WebApplicationContext appCxt;

    public void postConstruction() {
        String initialPrefix = prefix.substring(1);
        logger.error("Prefix is "+initialPrefix);
        String basePath = appCxt.getServletContext().getRealPath("/");
        String viewRoot = basePath + initialPrefix;

        logger.error(String.format("Checking views under '%s'", viewRoot));
        File viewRootDir = new File(viewRoot);
        List<File> subFiles = getAllSubfiles(viewRootDir);
        for (File file : subFiles) {
            String filePath = file.getAbsolutePath();
            String f2 = filePath.substring(viewRoot.length(), filePath.length()-4);
            FreeMarkerView view = new FreeMarkerView();
            view.setUrl(filePath);
            view.setApplicationContext(appCxt);
            validViews.put(f2, view);
            logger.error(String.format("Peri Adding view names '%s'", f2));
        }
    }


    private List<File> getAllSubfiles(File root) {
        List<File> ret = Lists.newArrayList();
        File[] children = root.listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                ret.addAll(getAllSubfiles(child));
            } else {
                if (child.getName().endsWith(suffix)) {
                    ret.add(child);
                }
            }
        }
        return ret;
    }


    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        logger.error(String.format("Looking up '%s'", viewName));
    if (!validViews.containsKey(viewName)) {
        return null;
    }
    return validViews.get(viewName);
    }


    @Override
    public void setApplicationContext(ApplicationContext cxt) throws BeansException {
       appCxt = (WebApplicationContext) cxt;
    }
}
