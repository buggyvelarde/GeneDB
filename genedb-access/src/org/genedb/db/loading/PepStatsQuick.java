/*
 * Copyright (c) 2002 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

/**
 *
 *
 * @author <a href="mailto:pjm@sanger.ac.uk">Paul Mooney</a>
*/
package org.genedb.db.loading;

import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;

import java.util.ArrayList;
import java.util.List;

class PepStatsQuick {

    private static SymbolTokenization aaToken;
    private static IsoelectricPointCalc iep;


    static {
        Alphabet aa = ProteinTools.getAlphabet();

        try {
            aaToken = aa.getTokenization("token");
        }
        catch (BioException exp) {
            System.err.println("ERROR: Couldn't get SymbolTokenization");
            throw new RuntimeException();
        }

        iep = new IsoelectricPointCalc();
    }
    
    private static PepStatsQuick INSTANCE = new PepStatsQuick();
    
    private PepStatsQuick() {
	// Singleton pattern
    }


	public static PepStatValues calculatePepstats(String residues) {

//        if (gene.isPartial()) {
//            gene.setPepStatsWarning("No peptide properties calculated for partial sequences");
//            return;
//        }
//
//        if (gene.isPseudo()) {
//            gene.setPepStatsWarning("No peptide properties calculated for pseudogenes");
//            return;
//        }

	    PepStatValues ret = INSTANCE.new PepStatValues();
	    
        if ( residues == null || residues.length()==0) {
            ret.addWarning("No protein statistics calculated - protein sequence unknown");
            ret.setValid(false);
            return ret;
        }
        
        residues = replaceResidues(residues, ret);

        try {
            SymbolList aaSymList = new SimpleSymbolList(aaToken, residues);

            ret.setWeight(MassCalc.getMass(aaSymList, SymbolPropertyTable.AVG_MASS, false));
            
            ret.setCharge(ChargeCalc.getCharge(residues));
            
            ret.setIsoelectricPoint((float) iep.getPI(aaSymList, true, true));
        }
        catch (BioException e) {
            //LogUtils.bprintln("WARN: gene '"+gene.getId()+"' had problems when generating pepstats. Maybe stop codons?q");
            ret.addWarning("Unable to generate peptide properties for this sequence");
            ret.setValid(false);
            //e.printStackTrace();
        }
        return ret;

    }

    /*
     */
    public static String replaceResidues(String peptideString, PepStatValues ret) {
        String retString = peptideString;
        List<Character> replacedChars = new ArrayList<Character>();

        if ( retString.indexOf("-") != -1 ) {
            retString = retString.replaceAll("-", "A");
            replacedChars.add('-');
        }

        if ( retString.indexOf("~") != -1 ) {
            retString = retString.replaceAll("~", "A");
            replacedChars.add('~');
        }

        if ( retString.indexOf("X") != -1 ) {
            retString = retString.replaceAll("X", "A");
            replacedChars.add('X');
        }

        StringBuffer replacedWarning = new StringBuffer();

        for(int i = 0; i < replacedChars.size(); ++i){
            if( replacedWarning.length() == 0){
                replacedWarning.append(replacedChars.get(i));
            }
            else {
                replacedWarning.append(", "+replacedChars.get(i));
            }
        }

        if( replacedWarning.length() > 0 ){
            ret.addWarning("These protein statistics were calculated by replacing "+
                                  "one or more unknown residues ("+replacedWarning.toString()+
                                  "), from ambiguous sequence data, by alanine (A)");
        }

        return retString;
    }


    public class PepStatValues {
	private int length;
	private float avResidueWeight;
	private double weight;
	private float isoelectricPoint;
	private float charge;
	private String warning;
	private boolean valid = true;

	public boolean isValid() {
	    return this.valid;
	}

	public void setValid(boolean valid) {
	    this.valid = valid;
	}

	public float getAvResidueWeight() {
	    return this.avResidueWeight;
	}

	public void setAvResidueWeight(float avResidueWeight) {
	    this.avResidueWeight = avResidueWeight;
	}
	public float getCharge() {
	    return this.charge;
	}
	public void setCharge(float charge) {
	    this.charge = charge;
	}
	public float getIsoelectricPoint() {
	    return this.isoelectricPoint;
	}
	public void setIsoelectricPoint(float isoelectricPoint) {
	    this.isoelectricPoint = isoelectricPoint;
	}
	public int getLength() {
	    return this.length;
	}
	public void setLength(int length) {
	    this.length = length;
	}
	public String getWarning() {
	    return this.warning;
	}
	public void setWarning(String warning) {
	    this.warning = warning;
	}
	public double getWeight() {
	    return this.weight;
	}
	public void setWeight(double weight) {
	    this.weight = weight;
	}
	private void addWarning(String msg) {
	    if (warning == null) {
		warning = new String();
	    } else {
		warning += ". ";
	    }
	    warning += msg;
	}

    }


}
