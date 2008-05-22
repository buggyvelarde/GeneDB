package org.genedb.web.mvc.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FileCheckingInternalResourceViewResolver extends
	InternalResourceViewResolver {
    
    private Set<String> validViews = new HashSet<String>();
    private boolean valid = false;
    
    public void postConstruction() {
	String prefix = getPrefix().substring(1);
	final String suffix = getSuffix();
	ApplicationContext appCxt = getWebApplicationContext();
	Resource viewRoot = appCxt.getResource(prefix);
	try {
	    File viewRootDir = viewRoot.getFile();
	    String[] fileNames = viewRootDir.list(new FilenameFilter() {
	        @SuppressWarnings("unused")
		public boolean accept(File file, String arg1) {
		    if (file.getName().endsWith(suffix)) {
			return true;
		    }
		    return false;
		}
	    }
	    );
	    for (String fileName : fileNames) {
		validViews.add(fileName);
		System.err.println(fileName);
	    }
	} catch (IOException exp) {
	    //
	    exp.printStackTrace();
	}
	
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
	if (valid && !validViews.contains(viewName)) {
	    return null;
	}
	return super.resolveViewName(viewName, locale);
    }
}
