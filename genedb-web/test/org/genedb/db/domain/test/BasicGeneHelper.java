package org.genedb.db.domain.test;

import java.util.HashSet;
import java.util.Set;

import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Exon;
import org.genedb.db.domain.objects.Transcript;
import org.genedb.db.domain.objects.TranscriptComponent;

/**
 * Extends BasicGene with a method {@link #transcript}, which makes
 * it easy to create mock genes with multiple transcripts.
 *
 * At present there is no support for creating multiple-exon
 * transcripts, as it has not yet been needed.
 *
 * @author rh11
 */
public class BasicGeneHelper extends BasicGene {
    public BasicGeneHelper(BasicGene gene) {
        super(gene);
    }

    /**
     * Add a single-exon transcript to this gene.
     *
     * @param name the name of the transcript
     * @param fmin location of the start of the transcript
     * @param fmax location of the end of the transcript
     * @return this object, so that calls to this method can be chained
     */
    public BasicGeneHelper transcript (String name, int fmin, int fmax) {
        Transcript transcript = new Transcript();
        transcript.setUniqueName(name);
        transcript.setFmin(fmin);
        transcript.setFmax(fmax);

        // with a single exon
        Set<TranscriptComponent> exons = new HashSet<TranscriptComponent> ();
        exons.add(new Exon(fmin, fmax));
        transcript.setComponents(exons);

        this.addTranscript(transcript);
        return this;
    }
}
