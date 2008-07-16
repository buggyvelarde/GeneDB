package org.genedb.web.gui;

import org.genedb.db.domain.objects.PolypeptideRegionGroup;

import org.gmod.schema.feature.Polypeptide;

import java.util.List;

public class ProteinMapDiagram extends TrackedDiagram {

    private String organism, polypeptideUniqueName;

    public ProteinMapDiagram(Polypeptide polypeptide, List<PolypeptideRegionGroup> regionGroups) {
        super(0, polypeptide.getSeqLen());
        this.organism = polypeptide.getOrganism().getCommonName();
        this.polypeptideUniqueName = polypeptide.getUniqueName();

        BoundarySet<PolypeptideRegionGroup> boundaries = new BoundarySet<PolypeptideRegionGroup>(regionGroups);
        allocateTracks(boundaries, false);
    }

    String getOrganism() {
        return organism;
    }

    String getPolypeptideUniqueName() {
        return polypeptideUniqueName;
    }
}
