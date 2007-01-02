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

package org.genedb.web.mvc.controller;

import org.gmod.schema.sequence.Feature;

import java.io.IOException;
import java.io.Writer;

public class FastaUtils {

    public static void exportFeatureFasta(Writer w, boolean spaces, Feature feat) throws IOException {
        exportFasta(w, feat.getCvTerm().getName()+":"+feat.getUniqueName(), feat.getResidues(), spaces);
    }
    
    public static void exportFastaRegion(Writer w, String header, boolean spaces, 
            Feature feat, Strand strand, int min, int max) throws IOException {
        // TODO - ignores strand
        //w.write('>' + header + '\n');
        byte[] seq = feat.getResidues(min, max);
        exportFasta(w, header, seq, spaces);
    }


    public static void exportFasta(Writer w, String header, byte[] seq, boolean spaces) throws IOException {
        w.write('>' + header + '\n');
        int count = 0;
        for (byte b : seq) {
            char c = (char) b;
            if (count % 60 == 0) {
                w.write('\n');
            } else {
                if (spaces && count % 10 == 0) {
                    w.write(' ');
                }
                count++;
            }
            w.write(c);
            count++;
        }
    }
    
}
