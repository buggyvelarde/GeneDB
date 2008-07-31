package org.gmod.schema.feature;

import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.PeptideProperties;
import org.gmod.schema.utils.StrandedLocation;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="polypeptide")
public class Polypeptide extends Region {
    private static Logger logger = Logger.getLogger(Polypeptide.class);
    @Transient
    private Transcript transcript;

    @Transient
    private AbstractGene gene;

    Polypeptide() {
        // empty
    }

    public Polypeptide(Organism organism, String systematicId, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, systematicId, analysis, obsolete, dateAccessioned);
    }

    public Transcript getTranscript() {
        if (transcript != null) {
            return transcript;
        }

        for (FeatureRelationship relation : getFeatureRelationshipsForSubjectId()) {
            Feature transcriptFeature = relation.getObjectFeature();
            if (transcriptFeature instanceof Transcript) {
                transcript = (Transcript) transcriptFeature;
                break;
            }
        }
        if (transcript == null) {
            logger.error(String.format("The polypeptide '%s' has no associated transcript", getUniqueName()));
            return null;
        }
        return transcript;
    }

    public AbstractGene getGene() {
        if (gene != null) {
            return gene;
        }

        Transcript transcript = getTranscript();
        if (transcript == null) {
            return null;
        }

        gene = transcript.getGene();
        return gene;
    }

    @Transient
    public List<String> getProducts() {
        List<String> products = new ArrayList<String>();
        for (FeatureCvTerm featureCvTerm : this.getFeatureCvTerms()) {
            if (featureCvTerm.getType().getCv().getName().equals("genedb_products")) {
                products.add(featureCvTerm.getType().getName());
            }
        }
        return products;
    }

    /**
     * Get the ID number of the colour associated with this polypeptide.
     * It is often unassigned, in which case <code>null</code> is returned.
     *
     * @return
     */
    @Transient
    public Integer getColourId() {

        /* Sometimes there is no colour property at all,
        and sometimes there is a colour property with a null value.

        I don't know why this inconsistency exists. rh11 */

        String colourIdString = getProperty("genedb_misc", "colour");
        if (colourIdString == null) {
            return null;
        }
        return Integer.valueOf(colourIdString);
    }

    /**
     * Get all the polypeptide regions of the specified type.
     * @param <T> the type of region. Must be a subclass of <code>PolypeptideRegion</code>
     * @param type a class object representing the region type. For example, <code>PolypeptideDomain.class</code>
     * @return a sorted set of those regions of the requested type
     */
    @Transient
    public <T extends PolypeptideRegion> SortedSet<T> getRegions(Class<T> type) {
        SortedSet<T> domains = new TreeSet<T>();

        for (FeatureLoc domainLoc: this.getFeatureLocsForSrcFeatureId()) {
            Feature domain = domainLoc.getFeature();
            if (type.isAssignableFrom(domain.getClass())) {
                domains.add(type.cast(domain));
            }
        }

        return domains;
    }

    /**
     * Get the (predicted) MembraneStructure of this protein.
     * @return the (predicted) MembraneStructure of this protein, or <code>null</code>
     * if there is none.
     */
    @Transient
    public MembraneStructure getMembraneStructure() {
        Set<MembraneStructure> membraneStructures = getRegions(MembraneStructure.class);
        if (membraneStructures.isEmpty()) {
            return null;
        }
        if (membraneStructures.size() > 1) {
            throw new IllegalStateException(String.format("Found more than one MembraneStructure for polypeptide '%s'",
                getUniqueName()));
        }
        return membraneStructures.iterator().next();
    }

    /**
     * Calculate the predicted properties of this polypeptide.
     *
     * @return a <code>PeptideProperties</code> object containing the predicted
     * properties of this polypeptide.
     */
    public PeptideProperties calculateStats() {
        if (this.getResidues() == null) {
            logger.warn("No residues for '" + this.getUniqueName() + "'");
            return null;
        }
        String residuesString = new String(this.getResidues());

        SymbolList residuesSymbolList = null;
        PeptideProperties pp = new PeptideProperties();
        try {
            SymbolTokenization proteinTokenization = ProteinTools.getTAlphabet().getTokenization("token");
            residuesSymbolList = new SimpleSymbolList(proteinTokenization, residuesString);

            if (residuesSymbolList.length() == 0) {
                logger.error(String.format("Polypeptide feature '%s' has zero-length residues", this.getUniqueName()));
                return pp;
            }

             try {
                // if the sequence ends with a termination symbol (*), we need to remove it
                if (residuesSymbolList.symbolAt(residuesSymbolList.length()) == ProteinTools.ter()) {
                    residuesSymbolList = residuesSymbolList.subList(1, residuesSymbolList.length() - 1);
                }

             } catch (IndexOutOfBoundsException exception) {
                 throw new RuntimeException(exception);
             }
        } catch (BioException e) {
            logger.error("Can't translate into a protein sequence", e);
            return pp;
        }

        pp.setAminoAcids(Integer.toString(residuesSymbolList.length()));

        DecimalFormat twoDecimalPlaces = new DecimalFormat("#.##");

        try {
            double isoElectricPoint = new IsoelectricPointCalc().getPI(residuesSymbolList, false, false);
            pp.setIsoelectricPoint(twoDecimalPlaces.format(isoElectricPoint));
        } catch (Exception e) {
            logger.error(String.format("Error computing protein isoelectric point for '%s'", residuesSymbolList), e);
        }

        try {
            double massInDaltons = MassCalc.getMass(residuesSymbolList, SymbolPropertyTable.AVG_MASS, true);
            pp.setMass(twoDecimalPlaces.format(massInDaltons / 1000));
        } catch (Exception e) {
            logger.error("Error computing protein mass", e);
        }

        double charge = calculateCharge(residuesString);
        pp.setCharge(twoDecimalPlaces.format(charge));

        return pp;
    }


    /**
     * Calculate the charge of a polypeptide.
     *
     * @param residues a string representing the polypeptide residues, using the single-character code
     * @return the charge of this polypeptide (in what units?)
     */
    private double calculateCharge(String residues) {
        double charge = 0.0;
        for (char aminoAcid: residues.toCharArray()) {
            switch (aminoAcid) {
            case 'B': case 'Z': charge += -0.5; break;
            case 'D': case 'E': charge += -1.0; break;
            case 'H':           charge +=  0.5; break;
            case 'K': case 'R': charge +=  1.0; break;
            /*
             * EMBOSS seems to think that 'O' (presumably Pyrrolysine)
             * also contributes +1 to the charge. According to Wikipedia,
             * this obscure amino acid is found only in methanogenic archaea,
             * so it's unlikely to trouble us soon. Still, it can't hurt:
             */
            case 'O':           charge +=  1.0; break;
            }
        }
        return charge;
    }

    public static Polypeptide make(Feature parent, StrandedLocation location,
            String systematicId, Organism organism, Timestamp now) {

        Polypeptide polypeptide = new Polypeptide(organism, systematicId, false, false, now);
        parent.addLocatedChild(polypeptide, location);
        return polypeptide;
    }

}
