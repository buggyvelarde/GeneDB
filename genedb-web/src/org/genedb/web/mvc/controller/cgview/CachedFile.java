package org.genedb.web.mvc.controller.cgview;

import java.io.File;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 *
 * @author Adrian Tivey
 */
public class CachedFile {
    
    private File file;
    
    private String relPath;
    
    private String pathPrefix;
    
    
    public CachedFile(File newFile, String pathPrefix, String relPath) {
        this.file = newFile;
        this.pathPrefix = pathPrefix;
        this.relPath = relPath;
    }

    public String getBrowserPath(HttpServletRequest request) {
        return request.getContextPath()+pathPrefix+relPath;
    }

    public File getFile() {
        return file;
    }

    public boolean exists() {
        return file.exists();
    }
    
    public void copyOut(Writer writer) {
        
        // TODO Copy content of file to writer
    }

}