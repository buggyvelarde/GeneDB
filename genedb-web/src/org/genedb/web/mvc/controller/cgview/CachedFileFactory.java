package org.genedb.web.mvc.controller.cgview;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletConfigAware;

/**
 * 
 *
 * @author Adrian Tivey
 */
public class CachedFileFactory implements ServletConfigAware, InitializingBean {
    
    public static final String DEFAULT_PATH_MIDFIX = "/tmp";
    
    public String pathMidfix = DEFAULT_PATH_MIDFIX;
    
    private File tmpDir;
    
    private ServletContext context;
    
    private boolean useSubDirs = false;
    
    
    
    public CachedFile getCachedFile(String name) {
        File parent = tmpDir;
        if (useSubDirs) {
            String nameWithoutExtension = name;
            if (nameWithoutExtension.indexOf(".") != -1) {
                nameWithoutExtension = name.substring(0, name.lastIndexOf("."));
            }
            String md5 = "gggggggg"; // FIXME
            final char s = File.separatorChar;
            String subPath = md5.substring(0,2)+s+md5.substring(2,4)+s+md5.substring(4,6)+s+md5.substring(6);
            parent = new File(tmpDir, subPath);
            if (!parent.exists()) {
                parent.mkdirs();
            }
        }
        
        File newFile = new File(parent, name);
        String relPath = newFile.getAbsolutePath().substring(tmpDir.getAbsolutePath().length()); // FIXME - breaks if s is \ not /
        CachedFile cf = new CachedFile(newFile, pathMidfix, relPath);
        return cf;
    }

    public void afterPropertiesSet() throws Exception {
        tmpDir = new File(context.getRealPath(pathMidfix));
    }
    
    public void setServletConfig(ServletConfig sc) {
        context = sc.getServletContext();
    }

}