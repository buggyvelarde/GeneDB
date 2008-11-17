package org.gmod.schema.utils;

import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.DbXRef;

import java.util.Collection;

public interface SimilarityI {

    public abstract String getAnalysisProgram();

    public abstract String getAnalysisProgramVersion();

    public abstract Analysis getAnalysis();

    public abstract String getOrganismName();

    public abstract String getGeneName();

    public abstract String getProduct();

    public abstract DbXRef getPrimaryDbXRef();

    public abstract Collection<DbXRef> getSecondaryDbXRefs();

    public abstract int getLength();

    public abstract Double getRawScore();

    public abstract Double getEValue();

    public abstract int getOverlap();

    public abstract int getQueryStart();

    public abstract int getQueryEnd();

    public abstract int getTargetStart();

    public abstract int getTargetEnd();

    public abstract Double getId();

    public abstract Double getUngappedId();

}