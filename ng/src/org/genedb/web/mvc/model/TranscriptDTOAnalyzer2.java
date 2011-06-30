//package org.genedb.web.mvc.model;
//
//import org.genedb.util.MutableInteger;
//
//import java.util.Map;
//
//import com.google.common.collect.Maps;
//import com.sleepycat.collections.StoredMap;
//
//import groovy.ui.Console;
//
//public class TranscriptDTOAnalyzer2 {
//
//    private BerkeleyMapFactory bmf;
//
//    public TranscriptDTOAnalyzer2(String dirName) {
//        bmf = new BerkeleyMapFactory();
//        bmf.setRootDirectory(dirName);
//        bmf.setReadOnly(true);
//    }
//
//    public static void main(String[] args) throws Exception {
//        TranscriptDTOAnalyzer2 tda = new TranscriptDTOAnalyzer2(args[0]);
//        tda.investigate(args);
//    }
//
//
//
//    private void investigate(String... args) throws Exception {
//        Map<String, MutableInteger> counts = Maps.newHashMap();
//        StoredMap<Integer, TranscriptDTO> dtoMap = bmf.getDtoMap();
//        int total = 0;
//        for (StoredMap.Entry<Integer, TranscriptDTO> entry : dtoMap.entrySet()) {
//            FeatureDTO t = entry.getValue();
//            String key = t.getOrganismCommonName();
//            if (args != null && args.length>=2  && key.equals(args[1])) {
//                System.err.println("" + entry.getKey() + " : "+ t.getUniqueName());
//            }
//            MutableInteger mi;
//            if (counts.containsKey(key)) {
//                mi = counts.get(key);
//            } else {
//                mi = new MutableInteger(0);
//                counts.put(key, mi);
//            }
//            mi.increment(1);
//            total++;
//        }
//        System.err.println("Gone thru' loop "+total);
//        for (Map.Entry<String, MutableInteger> entry : counts.entrySet()) {
//            System.err.println(entry.getKey() + " : "+ entry.getValue().intValue());
//        }
//    }
//
//}
