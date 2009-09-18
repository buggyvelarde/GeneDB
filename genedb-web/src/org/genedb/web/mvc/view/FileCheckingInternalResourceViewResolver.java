package org.genedb.web.mvc.view;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Lists;

public class FileCheckingInternalResourceViewResolver extends InternalResourceViewResolver {

    private Logger logger = Logger.getLogger(FileCheckingInternalResourceViewResolver.class);

    private Set<String> validViews = new HashSet<String>();

    public void postConstruction() {
        String prefix = getPrefix().substring(1);
        logger.debug("Prefix is "+prefix);
        WebApplicationContext appCxt = getWebApplicationContext();
        String basePath = appCxt.getServletContext().getRealPath("/");
        String viewRoot = basePath + prefix;

        logger.info(String.format("Checking views under '%s'", viewRoot));
        File viewRootDir = new File(viewRoot);
        List<File> subFiles = getAllSubfiles(viewRootDir);
        for (File file : subFiles) {
            String filePath = file.getAbsolutePath();
            String f2 = filePath.substring(viewRoot.length(), filePath.length()-4);
            validViews.add(f2);
            logger.info(String.format("Adding view names '%s'", f2));
        }
    }


    private List<File> getAllSubfiles(File root) {
        List<File> ret = Lists.newArrayList();
        File[] children = root.listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                ret.addAll(getAllSubfiles(child));
            } else {
                if (child.getName().endsWith(getSuffix())) {
                    ret.add(child);
                }
            }
        }
        return ret;
    }


    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
    if (!validViews.contains(viewName)) {
        return null;
    }
    return super.resolveViewName(viewName, locale);
    }
}
