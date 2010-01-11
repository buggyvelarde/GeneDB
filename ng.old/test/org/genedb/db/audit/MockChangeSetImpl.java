package org.genedb.db.audit;

import org.gmod.schema.mapped.Feature;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class MockChangeSetImpl implements ChangeSet {

    Map<Class<? extends Feature>, List<Integer>> changedMap = Maps.newHashMap();
    Map<Class<? extends Feature>, List<Integer>> deletedMap = Maps.newHashMap();
    Map<Class<? extends Feature>, List<Integer>> newMap = Maps.newHashMap();

    private Collection<Integer> getFeatureIds(Map<Class<? extends Feature>, List<Integer>> map, Class<? extends Feature> featureClass) {
        if (map.containsKey(featureClass)) {
            return map.get(featureClass);
        }
        return Collections.emptySet();
    }

//    private void setFeatureIds(Map<Class<? extends Feature>, List<Integer>> map, List<Integer> ids) {
//
//
//        if (map.containsKey(featureClass)) {
//            return map.get(featureClass);
//        }
//    }

    @Override
    public Collection<Integer> changedFeatureIds(Class<? extends Feature> featureClass) {
        return getFeatureIds(changedMap, featureClass);
    }


    @Override
    public Collection<Integer> deletedFeatureIds(Class<? extends Feature> featureClass) {
        return getFeatureIds(deletedMap, featureClass);
    }

    @Override
    public Collection<Integer> newFeatureIds(Class<? extends Feature> featureClass) {
        return getFeatureIds(newMap, featureClass);
    }

    @Override
    public void commit() throws SQLException {
        // Do nothing
    }

    public Map<Class<? extends Feature>, List<Integer>> getChangedMap() {
        return changedMap;
    }

    public void setChangedMap(
            Map<Class<? extends Feature>, List<Integer>> changedMap) {
        this.changedMap = changedMap;
    }

    public Map<Class<? extends Feature>, List<Integer>> getDeletedMap() {
        return deletedMap;
    }

    public void setDeletedMap(
            Map<Class<? extends Feature>, List<Integer>> deletedMap) {
        this.deletedMap = deletedMap;
    }

    public Map<Class<? extends Feature>, List<Integer>> getNewMap() {
        return newMap;
    }

    public void setNewMap(Map<Class<? extends Feature>, List<Integer>> newMap) {
        this.newMap = newMap;
    }

    public void clearAll(){
        newMap.clear();
        changedMap.clear();
        deletedMap.clear();
    }

}
