package org.genedb.web.mvc.model;

import com.sleepycat.collections.StoredMap;

import groovy.ui.Console;

public class TranscriptDTOAnalyzer {

    private BerkeleyMapFactory bmf;

    public TranscriptDTOAnalyzer(String dirName) {
        bmf = new BerkeleyMapFactory();
        bmf.setRootDirectory(dirName);
        bmf.setReadOnly(true);
    }

    public static void main(String[] args) throws Exception {
        TranscriptDTOAnalyzer tda = new TranscriptDTOAnalyzer(args[0]);
        String[] arguments = args;
        if (arguments.length <= 1) {
            arguments = new String[]{"PF07_0048"};
        }
        tda.startConsole(arguments);
    }


    public void startConsole(String... geneNames) throws Exception {
        Console console = new Console();
        for (int i = 0; i < geneNames.length; i++) {
            console.setVariable("gene"+(i+1), lookUpFeature(geneNames[i]));
        }
        console.run();
    }


    private TranscriptDTO lookUpFeature(String uniqueName) throws Exception {
        String lookupName = uniqueName;
        StoredMap<Integer, TranscriptDTO> dtoMap = bmf.getDtoMap();
        if (dtoMap.containsKey(lookupName)) {
            return dtoMap.get(lookupName);
        }
        lookupName = uniqueName + ":mRNA";
        if (dtoMap.containsKey(lookupName)) {
            return dtoMap.get(lookupName);
        }
        lookupName = uniqueName + ":pseudogenic_transcript";
        if (dtoMap.containsKey(lookupName)) {
            return dtoMap.get(lookupName);
        }
        System.err.println("Unable to find '"+uniqueName+"'");
        return null;
    }

}
