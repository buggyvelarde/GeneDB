/*
 * Copyright (c) 2002-2008 Genome Research Limited.
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

import java.util.Map;
import java.util.HashMap;

/**
 * This class represents a GO evidence code:
 *
 * @author art
 * @author rh11
 */
public enum GoEvidenceCode {

    // Experimental evidence codes
    EXP ("Inferred from Experiment"),
    IDA ("Inferred from Direct Assay"),
    IPI ("Inferred from Physical Interaction"),
    IMP ("Inferred from Mutant Phenotype"),
    IGI ("Inferred from Genetic Interaction"),
    IEP ("Inferred from Expression Pattern"),

    // Computational analysis evidence codes
    ISS ("Inferred from Sequence or Structural Similarity"),
    ISO ("Inferred from Sequence Orthology"),
    ISA ("Inferred from Sequence Alignment"),
    ISM ("Inferred from Sequence Model"),
    IGC ("Inferred from Genomic Context"),
    RCA ("Inferred from Reviewed Computational Analysis"),

    // Author statement evidence codes
    TAS ("Traceable Author Statement"),
    NAS ("Non-traceable Author Statement"),

    // Curator statement evidence codes
    IC  ("Inferred by Curator"),
    ND  ("No biological Data available"),

    // Automatically assigned evidence codes
    IEA ("Inferred from Electronic Annotation"),

    // Obsolete evidence codes
    NR  ("Not Recorded");

    private String description;
    private static Map<String,GoEvidenceCode> byName;
    private static void addToMap(GoEvidenceCode gec) {
        if (byName == null) {
            byName = new HashMap<String,GoEvidenceCode>();
        }
        byName.put(gec.toString(), gec);
        byName.put(gec.description, gec);
        byName.put(gec.description.toLowerCase(), gec);
    }
    private GoEvidenceCode(String description) {
        this.description = description;
        addToMap(this);
    }

    public String getDescription() {
        return description;
    }
    
    public static GoEvidenceCode parse(String ev) {
        return byName.get(ev);
    }
}
