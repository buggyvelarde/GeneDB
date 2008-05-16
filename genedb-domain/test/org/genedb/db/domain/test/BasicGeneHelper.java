package org.genedb.db.domain.test;

import java.util.HashSet;
import java.util.Set;

import org.genedb.db.domain.objects.BasicGene;
import org.genedb.db.domain.objects.Exon;
import org.genedb.db.domain.objects.Transcript;

public class BasicGeneHelper extends BasicGene {
    public BasicGeneHelper(BasicGene gene) {
        super(gene);
    }
    
    public BasicGeneHelper transcript (String name, int fmin, int fmax) {
        Transcript transcript = new Transcript();
        transcript.setName(name);
        transcript.setFmin(fmin);
        transcript.setFmax(fmax);
        
        // with a single exon
        Set<Exon> exons = new HashSet<Exon> ();
        exons.add(new Exon(fmin, fmax));
        transcript.setExons(exons);
        
        this.addTranscript(transcript);
        return this;
    }
}
