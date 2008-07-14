/*
 * Copyright (c) 2006 Genome Research Limited.
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

package org.genedb.db.loading;

import org.gmod.schema.mapped.Feature;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProteinUtils {
    
    private static Map<Symbol, Double> chargeFor;
    
    static {
        try {
            Alphabet aa = ProteinTools.getAlphabet();
            SymbolTokenization aaToken = aa.getTokenization("token");

            chargeFor = new HashMap<Symbol, Double>(7);

            // A,C,F,G,I,L,M,N,P,Q,S,T,U,V,W,X,Y are all zero
            chargeFor.put(aaToken.parseToken("B"), new Double(-0.5));
            chargeFor.put(aaToken.parseToken("D"), new Double(-1));
            chargeFor.put(aaToken.parseToken("E"), new Double(-1));
            chargeFor.put(aaToken.parseToken("H"), new Double(0.5));
            chargeFor.put(aaToken.parseToken("K"), new Double(1));
            chargeFor.put(aaToken.parseToken("R"), new Double(1));
            chargeFor.put(aaToken.parseToken("Z"), new Double(-0.5));
        }
        catch(IllegalSymbolException e){
            e.printStackTrace();
            throw new RuntimeException("Unexpected biojava error - illegal symbol");
        }
        catch(BioException e){
            e.printStackTrace();
            throw new RuntimeException("Unexpected biojava error - general");
        }
    }
    
    public static double getCharge(SymbolList aaSymList){
        
        double charge = 0.0;
        
        Map counts = residueCount(aaSymList);
        // iterate thru' all counts computing the partial contribution to charge
        Iterator countI = counts.keySet().iterator();

        while (countI.hasNext()) {
            Symbol sym = (Symbol) countI.next();
            Double chargeValue = chargeFor.get(sym);

            double symbolsCharge = chargeValue.doubleValue();
            int count = ((Integer) counts.get(sym)).intValue();

            charge += (symbolsCharge * count);
        }
        return charge;
    }
    
    private static Map<Symbol, Integer> residueCount(SymbolList aaSymList) {
        // iterate thru' aaSymList collating number of relevant residues
        Iterator residues = aaSymList.iterator();

        Map<Symbol, Integer> symbolCounts = new HashMap<Symbol, Integer>();

        while (residues.hasNext()) {
            Symbol sym =  (Symbol) residues.next();

            if (chargeFor.containsKey(sym)) {
                Integer currCount = symbolCounts.get(sym);

                if (currCount != null) {
                    currCount = currCount + 1;
                } else {
                    symbolCounts.put(sym, new Integer(1));
                }
            }
        }
        return symbolCounts;
    }
    
    
    public static void addDomain(Feature protein, String domainType, String acc) {
        
        
    }
    
}
