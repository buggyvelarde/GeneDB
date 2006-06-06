/*
 * Copyright (c) 2003 Genome Research Limited.
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
 * Calculates charge for a given peptide. It's very simplistic and code
 * was taken from Emboss (Eamino.dat) and IsoelectricPointCalc in Biojava.
 *
 * @author <a href="mailto:pjm@sanger.ac.uk">Paul Mooney</a>
*/

package org.genedb.db.loading;


public class ChargeCalc {

    private static float one = 1.0f;
    private static float half = 0.5f;
    private static float minusHalf = -0.5f;
    private static float minusOne = -1.0f;

    public static float getCharge(String sequence) {

        char[] chars = sequence.toCharArray();
        
        float charge = 0.0f;
        for (int i = 0; i < chars.length; i++) {
	    switch (chars[i]) {
	    case 'B':
		charge += minusHalf;
		break;
	    case 'D':
		charge += minusOne;
		break;
	    case 'E':
		charge += minusOne;
		break;
	    case 'H':
		charge += half;
		break;
	    case 'K':
		charge += one;
		break;
	    case 'R':
		charge += one;
		break;
	    case 'Z':
		charge += minusHalf;
		break;
	    default:
		// A,C,F,G,I,L,M,N,P,Q,S,T,U,V,W,X,Y are all zero
	    }
        }
        return charge;
    }

}
