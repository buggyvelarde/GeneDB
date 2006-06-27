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

import org.genedb.db.jpa.Feature;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Writer;

public class EmblUtils {

    public static void exportEmbl(Writer w, Feature feat, int min, int max, boolean internal, boolean strict, boolean truncateEnds) throws IOException {
        exportHeader();
        exportTab();
        exportSequence(w, feat, min, max);
    }

    private static void exportSequence(Writer w, Feature feat, int min, int max) throws IOException {
        // TODO - ignores strand
        String seq = feat.getResidues().substring(min, max);
        exportSequence(w, seq);
    }

    private static void exportSequence(Writer w, String seq) throws IOException {
        // XX
        // SQ   Sequence 29663 BP; 9792 A; 5106 C; 5232 G; 9533 T; 0 other;
        //     gatcacgtac atcaccttgt aagaatttat ctgcaatagt ccttcggtat tgtacattgt        60
        //     ...
        //     tggttctgat attgaacaaa tagaactaca aaatatgcct actcctgtga aaaaataatt     29640
        //     ttctttatcg ttttcatgat ccc                                             29663
        // //
        pln(w, "XX");
        
        
        w.write("SQ   Sequence ");
        w.write(seq.length());
        w.write(" BP;");
        // TODO stats
        w.write('\n');
        
        char[] bases = seq.toCharArray();
        for (int i = 0; i < bases.length; i++) {
            char c = bases[i];
            if (i % 60 == 0) {
                String count = Integer.toString(i);
                w.write(StringUtils.leftPad(" ", 10, count));
                w.write('\n');
            } else {
                if (i % 10 == 0) {
                    w.write(' ');
                }
            }
            w.write(c);
        }
        if (seq.length() % 60 != 0) {
            // TODO cope with remainder on last line
            int used = seq.length() % 60;
            int toPad = 75; // 10 *6 + 1 *5 + 10
            String count = Integer.toString(seq.length());
            w.write(StringUtils.leftPad(" ", toPad, count));
            w.write('\n');
        }
        pln(w, "//");
    }

    private static void pln(Writer w, String line) throws IOException {
        w.write(line);
        w.write('\n');
    }
    
    private static void exportTab() {
        // TODO Auto-generated method stub
    }

    private static void exportHeader() {
        // TODO Auto-generated method stub
    }
    
}
