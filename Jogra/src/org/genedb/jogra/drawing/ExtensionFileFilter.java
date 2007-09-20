package org.genedb.jogra.drawing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter {

    private String extension;
    
    public ExtensionFileFilter(String extension) {
        super();
        this.extension = extension;
    }


    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String path = file.getName().toLowerCase();
        if (path.endsWith(extension)) {
            return true;
        }
        return false;
    }

    
    @Override
    public String getDescription() {
        return "Excel (.xls) files";
    }

}
