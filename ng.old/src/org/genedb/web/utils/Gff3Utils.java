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

package org.genedb.web.utils;

import org.genedb.web.mvc.controller.Strand;
import org.genedb.web.mvc.controller.Taxon;

import org.gmod.schema.mapped.Feature;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.List;

public class Gff3Utils {

    public void exportGff3(Writer w, Taxon taxon, boolean internal) throws IOException {
        // TODO Get all top level features and call other export Gff3 method with top not true
        // Write sequence for all top level features as fasta
        List<Feature> top = null;
        for (Feature feature : top) {
            exportGff3(w, feature, false, internal);
        }
        for (Feature feature : top) {
            //FastaUtils.exportFeatureFasta(w, false, feature);
        }
    }


    public static String URLencode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        }
        catch (UnsupportedEncodingException exp) {
            System.err.println("Got an unexpected encoding");
            System.exit(-1);
        }
        return "";
    }

    public void exportGff3(Writer w, Feature feat, boolean top, boolean internal) throws IOException {
        // Write line for this feature

        // TODO dummy values above
        String seqId = null;
        String source = null;
        int start = -1;
        int end = -1;
        String score = null;
        Strand strand = null;
        int phase = -4;
        String attributes = null;
        // TODO dummy values above
        writeGFFLine(w, seqId, source, feat.getType().getName(), start, end, score, strand, phase, attributes);
        // Get immediate children of feature
        // Loop over children, call this but with top not true
        // If top, write FASTA sequence
    }

    public static void writeGFFLine(Writer w, String seqid, String source, String type, int start,
            int end, String score, Strand strand, int phase, String attributes) throws IOException {
        w.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
                seqid,
                source,
                type,
                start,
                end,
                score,
                strand,
                phase,
                attributes));
    }
}
