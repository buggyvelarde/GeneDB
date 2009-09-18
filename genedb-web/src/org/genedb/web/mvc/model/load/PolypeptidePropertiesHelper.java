package org.genedb.web.mvc.model.load;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;
import org.gmod.schema.utils.PeptideProperties;


/**
 *
 * lo2@author sangerinstitute
 * Much of these methods contain snippets from the Polypeptide class
 */
public class PolypeptidePropertiesHelper {
    private static Logger logger = Logger.getLogger(PolypeptidePropertiesHelper.class);
    /**
     * Calculate the predicted properties of this polypeptide.
     *
     * @return a <code>PeptideProperties</code> object containing the predicted
     * properties of this polypeptide.
     */
    public static PeptideProperties calculateStats(FeatureMapper polypeptideMapper) {
        if (polypeptideMapper.getResidues() == null) {
            logger.warn("No residues for '" + polypeptideMapper.getUniqueName() + "'");
            return null;
        }
        String residuesString = new String(polypeptideMapper.getResidues());

        SymbolList residuesSymbolList = null;
        PeptideProperties pp = new PeptideProperties();
        try {
            SymbolTokenization proteinTokenization = ProteinTools.getTAlphabet().getTokenization("token");
            residuesSymbolList = new SimpleSymbolList(proteinTokenization, residuesString);

            if (residuesSymbolList.length() == 0) {
                logger.error(String.format("Polypeptide feature '%s' has zero-length residues", polypeptideMapper.getUniqueName()));
                return pp;
            }

            try {
                // if the sequence ends with a termination symbol (*), we need to remove it
                if (residuesSymbolList.symbolAt(residuesSymbolList.length()) == ProteinTools.ter()) {
                    if (residuesSymbolList.length() == 1) {
                        logger.error(String.format("Polypeptide feature '%s' only has termination symbol", polypeptideMapper.getUniqueName()));
                        return pp;
                    }
                    residuesSymbolList = residuesSymbolList.subList(1, residuesSymbolList.length() - 1);
                }

            } catch (IndexOutOfBoundsException exception) {
                throw new RuntimeException(exception);
            }
        } catch (BioException e) {
            logger.error("Can't translate into a protein sequence", e);
            return pp;
        }

        pp.setAminoAcids(residuesSymbolList.length());

        try {
            double isoElectricPoint = new IsoelectricPointCalc().getPI(residuesSymbolList, false, false);
            pp.setIsoelectricPoint(isoElectricPoint);
        } catch (Exception e) {
            logger.error(String.format("Error computing protein isoelectric point for '%s'", residuesSymbolList), e);
        }

        double mass2 = calculateMass(polypeptideMapper, residuesSymbolList);
        if (mass2 != -1) {
            //mass = mass2;
            pp.setMass(mass2);
        }

        double charge = calculateCharge(residuesString);
        pp.setCharge(charge);

        return pp;
    }

    private static double calculateMass(FeatureMapper polypeptideMapper, SymbolList residuesSymbolList) {
        try {
            double massInDaltons = MassCalc.getMass(residuesSymbolList, SymbolPropertyTable.AVG_MASS, true);
            return massInDaltons;
        } catch (Exception exp) {
            logger.error(String.format("Error computing protein mass in '%s' because '%s'", polypeptideMapper.getUniqueName(), exp.getMessage()));
        }
        return -1.0;
    }


    /**
     * Calculate the charge of a polypeptide.
     *
     * @param residues a string representing the polypeptide residues, using the single-character code
     * @return the charge of this polypeptide (in what units?)
     */
    private static double calculateCharge(String residues) {
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

}
