package org.genedb.web.mvc.model;

import org.genedb.util.MD5Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sleepycat.collections.StoredMap;

public class BmfComparison {

    Map<Integer, String> transcriptMapNew = Maps.newHashMap();
    Map<Integer, String> contextmapMapNew = Maps.newHashMap();
    Map<String, String> imageMapNew = Maps.newHashMap();


    Map<Integer, String> transcriptMapOrig = Maps.newHashMap();
    Map<Integer, String> contextmapMapOrig = Maps.newHashMap();
    Map<String, String> imageMapOrig = Maps.newHashMap();


    public BmfComparison(String newDirName, String oldDirName) throws IOException {

        BerkeleyMapFactory bmfNew = new BerkeleyMapFactory();
        bmfNew.setRootDirectory(newDirName);
        bmfNew.setReadOnly(true);
        populateMaps(bmfNew, transcriptMapNew, contextmapMapNew, imageMapNew);

        BerkeleyMapFactory bmfOrig = new BerkeleyMapFactory();
        bmfOrig.setRootDirectory(oldDirName);
        bmfOrig.setReadOnly(true);
        populateMaps(bmfOrig, transcriptMapOrig, contextmapMapOrig, imageMapOrig);
    }

    private void populateMaps(BerkeleyMapFactory bmf,
            Map<Integer, String> transcriptMap,
            Map<Integer, String> contextmapMap,
            Map<String, String> imageMap) throws IOException {

        populateMapTranscriptDTO(transcriptMap, bmf.getDtoMap());
        populateMapString(contextmapMap, bmf.getContextMapMap());
        populateMapByteArray(imageMap, bmf.getImageMap());
    }

    private void populateMapTranscriptDTO(Map<Integer, String> map,
            StoredMap<Integer, TranscriptDTO> dtoMap) throws IOException {

        for (Map.Entry<Integer, TranscriptDTO> entry : dtoMap.entrySet()) {
            TranscriptDTO dto = entry.getValue();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(dto);
            out.close();
            String md5 = MD5Util.getMD5(buffer.toByteArray());
            map.put(entry.getKey(), md5);
        }
    }

    private void populateMapByteArray(Map<String, String> map, StoredMap<String, byte[]> bmfMap) {
        for (Map.Entry<String, byte[]> entry : bmfMap.entrySet()) {
            String md5 = MD5Util.getMD5(entry.getValue());
            map.put(entry.getKey(), md5);
        }
    }


    private void populateMapString(Map<Integer, String> map, StoredMap<Integer, String> bmfMap) {
        for (Map.Entry<Integer, String> entry : bmfMap.entrySet()) {
            String md5 = MD5Util.getMD5(entry.getValue());
            map.put(entry.getKey(), md5);
        }
    }



    private void compare() throws Exception {

        compareMaps("TranscriptDTOs", transcriptMapNew, transcriptMapOrig);
        compareMaps("Context Maps", contextmapMapNew, contextmapMapOrig);
        compareMaps("Protein Maps", imageMapNew, imageMapOrig);
    }

    private <T> void compareMaps(String label,
            Map<T, String> modified,
            Map<T, String> orig) {

        List<T> newIds = Lists.newArrayList();
        List<T> missingIds = Lists.newArrayList();
        List<T> changedIds = Lists.newArrayList();
        List<T> sameIds = Lists.newArrayList();

        for (T key : modified.keySet()) {
            if (!orig.containsKey(key)) {
                newIds.add(key);
                continue;
            }
            if (modified.get(key).equals(orig.get(key))) {
                sameIds.add(key);
            } else {
                changedIds.add(key);
            }
            orig.remove(key);
        }

        missingIds.addAll(orig.keySet());

        System.out.println("\n\n\n"+label);
        displayLabels("Newly added ids", newIds);
        displayLabels("Removed ids", missingIds);
        displayLabels("Changed ids", changedIds);
        displayLabels("Unchanged ids", sameIds);
    }

    private <T> void displayLabels(String string, List<T> ids) {
        System.out.println("\n\n"+string + "  "+ids.size());
        for (T key : ids) {
            System.out.println(key);
        }
    }

    public static void main(String[] args) throws Exception {
        BmfComparison tdc = new BmfComparison(args[0], args[1]);
        tdc.compare();
    }


}
