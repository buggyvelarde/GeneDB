package org.genedb.web.gui;

import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.db.domain.objects.SimplePolypeptideRegion;
import org.genedb.db.domain.objects.SimpleRegionGroup;

import org.gmod.schema.feature.GPIAnchorCleavageSite;
import org.gmod.schema.feature.MembraneStructure;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.PolypeptideRegion;
import org.gmod.schema.feature.SignalPeptide;

import java.awt.Color;
import java.util.Collection;

public class ProteinMapDiagram extends TrackedDiagram {

    private String organism, polypeptideUniqueName;
    private MembraneStructure membraneStructure;

    public ProteinMapDiagram(Polypeptide polypeptide, Collection<PolypeptideRegionGroup> regionGroups) {
        super(0, polypeptide.getSeqLen());
        this.organism = polypeptide.getOrganism().getCommonName();
        this.polypeptideUniqueName = polypeptide.getUniqueName();
        this.membraneStructure = polypeptide.getMembraneStructure();

        this.packSubfeatures = true;
        this.numberOfBlankTracksAboveCompoundFeature = 2;

        BoundarySet<PolypeptideRegionGroup> boundaries = new BoundarySet<PolypeptideRegionGroup>(regionGroups);

        addRegion           (polypeptide, boundaries, "Signal peptide", "Sig. pep.",
            Color.GREEN,        SignalPeptide.class);
        addRegionToNTerminus(polypeptide, boundaries, "GPI anchor", "GPI",
            new Color(255, 165, 0), GPIAnchorCleavageSite.class);

        allocateTracks(boundaries, false);
    }

    private <T extends PolypeptideRegion> void addRegion(Polypeptide polypeptide,
            BoundarySet<PolypeptideRegionGroup> boundaries, String title, String abbreviation,
            Color color, Class<T> regionClass) {

        Collection<T> regions = polypeptide.getRegions(regionClass);
        if (!regions.isEmpty()) {
            PolypeptideRegionGroup regionGroup = new SimpleRegionGroup(title, abbreviation);
            for(T region: regions) {
                regionGroup.addRegion(SimplePolypeptideRegion.build(region, title, null, color));
            }
            boundaries.addFeature(regionGroup);
        }

    }

    private <T extends PolypeptideRegion> void addRegionToNTerminus(Polypeptide polypeptide,
            BoundarySet<PolypeptideRegionGroup> boundaries, String title, String abbreviation,
            Color color, Class<T> regionClass) {

        Collection<T> regions = polypeptide.getRegions(regionClass);
        if (!regions.isEmpty()) {
            PolypeptideRegionGroup regionGroup = new SimpleRegionGroup(title, abbreviation);
            for(T region: regions) {
                SimplePolypeptideRegion simplePolypeptideRegion = new SimplePolypeptideRegion(
                    region.getFmin(), polypeptide.getSeqLen(), region.getUniqueName(), title, null, color);
                regionGroup.addRegion(simplePolypeptideRegion);
            }
            boundaries.addFeature(regionGroup);
        }

    }

    String getOrganism() {
        return organism;
    }

    String getPolypeptideUniqueName() {
        return polypeptideUniqueName;
    }

    MembraneStructure getMembraneStructure() {
        return membraneStructure;
    }

    public boolean isEmpty() {
        return (getAllocatedCompoundFeatures().isEmpty() && membraneStructure == null)
            || getSize() <= 0;
    }
}
