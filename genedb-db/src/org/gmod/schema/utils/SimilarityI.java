package org.gmod.schema.utils;

import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.DbXRef;

import java.util.Collection;

/**
 * A SimilarityI object represents a similarity (as defined by a /similarity
 * qualifier in an EMBL file. The method {@link Feature#addSimilarity(SimilarityI)}
 * will add a similarity to the database.
 * <p>
 * The only implementation of this interface is {@link org.genedb.db.loading.Similarity}.
 * The interface is defined here in genedb-db in order that we don't create a dependency
 * of genedb-db on genedb-access.
 *
 * @author rh11
 *
 */
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