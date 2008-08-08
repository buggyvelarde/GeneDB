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

package org.genedb.db.loading;

import java.util.EnumMap;


/**
 * This class represents a specific GO entry 
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */
public enum GoEvidenceCode {

    IC, IDA, IEA, IEP, IGI, IMP, IPI, ISS, NAS, ND, TAS, NR, RCA;
    
    static EnumMap<GoEvidenceCode, String> descriptions = new EnumMap<GoEvidenceCode, String>(GoEvidenceCode.class);

    static {
        descriptions.put(GoEvidenceCode.IC,"Inferred by Curator");
        descriptions.put(GoEvidenceCode.IDA,"Inferred from Direct Assay");
        descriptions.put(GoEvidenceCode.IEA,"Inferred from Electronic Annotation");
        descriptions.put(GoEvidenceCode.IEP,"Inferred from Expression Pattern");
        descriptions.put(GoEvidenceCode.IGI,"Inferred from Genetic Interaction");
        descriptions.put(GoEvidenceCode.IMP,"Inferred from Mutant Phenotype");
        descriptions.put(GoEvidenceCode.IPI,"Inferred from Physical Interaction");
        descriptions.put(GoEvidenceCode.ISS,"Inferred from Sequence or Structural Similarity");
        descriptions.put(GoEvidenceCode.NAS,"Non-traceable Author Statement");
        descriptions.put(GoEvidenceCode.ND,"No biological Data available");
        descriptions.put(GoEvidenceCode.TAS,"Traceable Author Statement");
        descriptions.put(GoEvidenceCode.NR,"Not Recorded");
        descriptions.put(GoEvidenceCode.RCA, "inferred from Reviewed Computational Analysis");
    }

    public String getDescription() {
    return descriptions.get(this);
    }


//    /**
//     * @return
//     */
//    public boolean isWith() {
//        if (EV_IC.equals(getEvidence())) {
//            return false;
//        }
//        return true;
//    }

}
