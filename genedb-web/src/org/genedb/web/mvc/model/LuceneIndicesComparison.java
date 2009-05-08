package org.genedb.web.mvc.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.genedb.util.MD5Util;
import org.gmod.schema.mapped.Feature;

/**
 * 
 * @author sangerinstitute
 *
 */
public class LuceneIndicesComparison {
    
    private HashMap<Integer, String> obsoleteIndexMap = new HashMap<Integer, String>();
    private HashMap<Integer, String> uptodateIndexMap = new HashMap<Integer, String>();
    
    public static void main(String args[])throws IOException{
        if (args.length != 2 ) {
            throw new IllegalArgumentException(
                    "\nUsage: java LuceneIndicesComparison <obsoleteIndexDirectory> <uptodateIndexDirectory>");
        }
        
        LuceneIndicesComparison comparer =  new LuceneIndicesComparison(args[0], args[1]);
        comparer.compare();
    }
    
    public LuceneIndicesComparison(String previousDirectory, String recentDirectory)throws IOException{
        addDocumentComparison(previousDirectory, Feature.class, obsoleteIndexMap);
        addDocumentComparison(recentDirectory, Feature.class, uptodateIndexMap);
    }
    
    @SuppressWarnings("unchecked")
    private void addDocumentComparison(
            String directoryName, Class entityType, HashMap< Integer, String> indexMap)throws IOException{
        File dir = new File(directoryName, entityType.getCanonicalName());
        FSDirectory directory = FSDirectory.getDirectory(dir);
        
        IndexReader indexReader = IndexReader.open(directory);
        int numDocs = indexReader.numDocs();
        int deleted = 0;
        for (int i=0; i< numDocs; i++){
            if (!indexReader.isDeleted(i)){
                Document document = indexReader.document(i);
                addMD5(document, indexMap);
            }else{
                ++deleted;
            }
        }
        System.out.println(String.format("Found %d docs with %d deleted at %s", numDocs, deleted, dir.getCanonicalFile()));
    }
    
    private void addMD5(Document doc, HashMap< Integer, String> indexMap)throws IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(buffer);
        out.writeObject(doc);
        out.close();
        String md5 = MD5Util.getMD5(buffer.toByteArray());
        Field field = doc.getField("featureId");
        Integer featureId = Integer.valueOf(field.stringValue());
        indexMap.put(featureId, md5);
    }
    
    public void compare(){
        List<Integer> newFeatures= new ArrayList<Integer>();
        List<Integer> sameFeatures=new ArrayList<Integer>();
        List<Integer> changedFeatures=new ArrayList<Integer>();
        
        System.out.println("Up-to-date Index: " + uptodateIndexMap.size());
        System.out.println("Obsolete Index: " + obsoleteIndexMap.size());
        
        for(Integer key : uptodateIndexMap.keySet()){
            if (!obsoleteIndexMap.containsKey(key)){
                newFeatures.add(key);
            }else if (obsoleteIndexMap.get(key).equals(uptodateIndexMap.get(key))){
                sameFeatures.add(key);
                obsoleteIndexMap.remove(key);
            }else{
                changedFeatures.add(key);
                obsoleteIndexMap.remove(key);
            }
        }
        
        //what ever is left here
        List<Integer>  deletedFeatures = new ArrayList<Integer>(obsoleteIndexMap.keySet());
        
        print("New Features: %d", newFeatures);
        print("Same Features: %d", sameFeatures);
        print("Changed Features: %d", changedFeatures);
        print("Deleted Features: %d", deletedFeatures);
    }
    
    private void print(String caption, List<Integer>  featureIds){
        System.out.println(String.format(caption, featureIds.size()));
        for(Integer i: featureIds){
            System.out.println("\t" + i);
        }
    }
}
