package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;

import javax.persistence.Entity;

/*
 * This term is specified by accession number, because the term name
 * has recently been changed from 'transmembrane' to 'transmembrane_region',
 * so we might see either depending on which version of Sequence Ontology
 * is present.
 */
@Entity
@FeatureType(cv="sequence", accession="0001077")
public class TransmembraneRegion extends IntramembraneRegion {

    public TransmembraneRegion() {
        // empty
    }

    public TransmembraneRegion(Organism organism, CvTerm cvTerm, String uniqueName) {
        super(organism, cvTerm, uniqueName, true, false);
    }

}
