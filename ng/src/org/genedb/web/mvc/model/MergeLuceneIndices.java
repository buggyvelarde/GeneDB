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

    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {

        MergeLuceneIndices mli = new MergeLuceneIndices();

        mli.merge(args[0], "org.gmod.schema.mapped.Feature", args[1]);
        mli.merge(args[0], "org.gmod.schema.mapped.Organism", args[1]);
    }

    private void merge(String destinationDir, String indexName, String sourceDir) throws CorruptIndexException, LockObtainFailedException, IOException {
        IndexWriter destination = new IndexWriter(destinationDir +File.separatorChar+indexName, new SimpleAnalyzer(), true);

        File source = new File(sourceDir);

        File[] dirs = source.listFiles();
        Directory[] directories = new Directory[dirs.length];
        for (int i = 0; i < dirs.length; i++) {
            directories[i] = FSDirectory.getDirectory(dirs[i].getAbsolutePath()+File.separatorChar+indexName);
        }
        destination.addIndexesNoOptimize(directories);

        destination.optimize();
        destination.close();
    }

}
