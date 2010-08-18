package org.genedb.web.mvc.model;

import java.io.File;


public class MergeBerkeleyIndices {

    private boolean recurse;


    public static void main(String[] args) {

        MergeBerkeleyIndices mbi = new MergeBerkeleyIndices();

        int startArgs = 0;
        if (args[0].equals("-r")) {
            mbi.setRecurse(true);
            startArgs = 1;
        }

        BerkeleyMapFactory destination = new BerkeleyMapFactory();
        destination.setRootDirectory(args[startArgs]);
        System.out.println("destination: " + args[startArgs]);
        destination.setReadOnly(false);

        File sourceDir = new File(args[startArgs+1]);
        System.out.println("source:" + sourceDir);

        if (mbi.isRecurse()) {
            File[] dirs = sourceDir.listFiles();
            for (File dir : dirs) {
            	
            	// gv1 - skip files which are not directories... e.g. ".DS_Store"
            	if (! dir.isDirectory()) {
            		continue;
            	}
            	
                mbi.merge(dir, destination);
            }
        } else {
            mbi.merge(sourceDir, destination);
        }


        destination.closeDb();
    }


    private void merge(File sourceDir, BerkeleyMapFactory destination) {
        BerkeleyMapFactory source = new BerkeleyMapFactory();
        source.setRootDirectory(sourceDir.getAbsolutePath());
        source.setReadOnly(true);
        
        //System.out.println("Merging : " + source.getRootDirectory() + " to destination  berkley db" + destination.getRootDirectory());
        
        destination.getDtoMap().putAll(source.getDtoMap());
        destination.getContextMapMap().putAll(source.getContextMapMap());
        destination.getImageMap().putAll(source.getImageMap());

        source.closeDb();
    }

    public boolean isRecurse() {
        return recurse;
    }

    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }

}
