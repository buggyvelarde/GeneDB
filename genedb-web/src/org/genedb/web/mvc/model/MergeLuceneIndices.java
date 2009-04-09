package org.genedb.web.mvc.model;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import java.io.File;
import java.io.IOException;


public class MergeLuceneIndices {

    private boolean recurse;


    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {

        MergeLuceneIndices mli = new MergeLuceneIndices();

        int startArgs = 0;
        if (args[0].equals("-r")) {
            mli.setRecurse(true);
            startArgs = 1;
        }

        mli.merge(args[startArgs], "org.gmod.schema.Feature", args[startArgs+1], true);
        mli.merge(args[startArgs], "org.gmod.schema.Organism", args[startArgs+1], false);
    }



    private void merge(String destinationDir, String indexName, String sourceDir, boolean create) throws CorruptIndexException, LockObtainFailedException, IOException {
        IndexWriter destination = new IndexWriter(destinationDir +File.separatorChar+indexName, new SimpleAnalyzer(), create);

        File source = new File(sourceDir);

        if (isRecurse()) {
            File[] dirs = source.listFiles();
            Directory[] directories = new Directory[dirs.length];
            for (int i = 0; i < dirs.length; i++) {
                directories[i] = FSDirectory.getDirectory(dirs[i]);
            }
            destination.addIndexesNoOptimize(directories);
        } else {
            destination.addIndexesNoOptimize(new Directory[]{FSDirectory.getDirectory(sourceDir)});
        }

        destination.optimize();
        destination.close();
    }

    public boolean isRecurse() {
        return recurse;
    }

    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }

}
