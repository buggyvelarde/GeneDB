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
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
*/
package org.genedb.db.loading;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.GeneticCodes;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolListViews;
import org.biojava.bio.symbol.TranslationTable;


public class SeqTrans {

    private static SeqTrans instance = new SeqTrans();


    public String translate(String sequence, int transTable, int codonStart, String codon, String exception) {


//        if ( gene.isPseudo()) {
//            result.appendWarning("This CDS is annotated as pseudo. This is because, either:");
//            result.appendWarning("The CDS translation contains stop codons");
//            result.appendWarning("The CDS is frameshifted and the translation presented has been obtained by the inclusion of a join(s)");
//        }
//        if ( gene.isPartial()) {
//            result.appendWarning("This is a partial protein sequence.");
//        }


        byte[] in = sequence.getBytes();


        // FIXME - Remove stop codon at end of sequence
        //SpeciesConfig config = SpeciesFactory.getSpeciesConfig(gene.getShortOrganism());
        String result = translate(in, transTable, codonStart);
        if ( result.endsWith("*") ) {
            result = result.substring(0, result.length() - 1 );
            //result.setSeq(ret);
        } else {
//            if (!gene.isPartial()) {
//                result.appendWarning("Protein doesn't end with a stop codon");
//            }
        }

//        if (result.indexOf("*")!=-1) {
//            result.appendWarning("Protein contains stop codons inside the peptide sequence");
//        }

//        if ( !ret.startsWith("M") && !gene.isPartial()) {
//            result.appendWarning("Protein doesn't start with a start codon");
//        }

       //result.setSeq(ret);
       // FIXME - Add  warning if translation contains stop codons

        return result;
    }


    public String translate(byte[] bytes, int translTable, int codonStart) {

        String in = new String(bytes);
        String startCodon = in.substring(0,3);

        if (codonStart != 1) {
            in = in.substring(codonStart-1);
            startCodon = "";
        }
        TranslationTable tt = RNATools.getGeneticCode(Integer.toString(translTable));
//        Set altStartCodons = GeneticCodes.getAltStartCodons(Integer.toString(translTable));
//        if (altStartCodons.contains(startCodon)) {
//            in = "ATG"+ in.substring(3);
//        }


        try {
            SymbolList geneSeq = DNATools.createDNA(in);

            int mod = geneSeq.length() % 3;
            if ( mod != 0 ) {
                geneSeq = geneSeq.subList(1, geneSeq.length() - mod);
            }

            // transcribe to RNA
            SymbolList seq = GeneticCodes.transcribe(geneSeq);

            // view the RNA sequence as codons
            seq = SymbolListViews.windowedSymbolList(seq, 3);
            SymbolList protein = SymbolListViews.translate(seq, tt);
            return protein.seqString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
