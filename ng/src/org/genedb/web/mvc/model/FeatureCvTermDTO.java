package org.genedb.web.mvc.model;

import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureCvTermDbXRef;
import org.gmod.schema.mapped.FeatureCvTermProp;
import org.gmod.schema.mapped.Pub;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ontology_term")
public class FeatureCvTermDTO implements Serializable {

    private static final long serialVersionUID = -2231825983085114418L;
    private String typeName;
    private String typeAccession;
    private List<String> pubs;
    private String withFrom;
    private List<DbXRefDTO> dbXRefDtoList;
    private Multimap<String, String> props = Multimaps.newArrayListMultimap();
    private long count;

    public FeatureCvTermDTO(FeatureCvTerm fct) {

        for (FeatureCvTermProp fctProp : fct.getFeatureCvTermProps()) {
            props.put(fctProp.getType().getName(), fctProp.getValue());
        }

        typeName = fct.getCvTerm().getName();
        typeAccession = fct.getCvTerm().getDbXRef().getAccession();


        // TODO Double check logic with original page
        pubs = new ArrayList<String>();
        for (Pub pub : fct.getPubs()) {
            pubs.add(pub.getUniqueName());
        }

        if (fct.getPub() != null) {
            withFrom = fct.getPub().getUniqueName();
        }

        dbXRefDtoList = new ArrayList<DbXRefDTO>();
        for (FeatureCvTermDbXRef featureCvTermDbXRef : fct.getFeatureCvTermDbXRefs()) {
            dbXRefDtoList.add(new DbXRefDTO(featureCvTermDbXRef.getDbXRef()));
        }

    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeAccession() {
        return typeAccession;
    }

    public List<String> getPubs() {
        return pubs;
    }

    public String getWithFrom() {
        return withFrom;
    }

    public List<DbXRefDTO> getDbXRefDtoList() {
        return dbXRefDtoList;
    }

    public Map<String, Collection<String>> getProps() {
        return props.asMap();
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

}
