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
        descriptions.put(GoEvidenceCode.IC,"inferred by curator");
        descriptions.put(GoEvidenceCode.IDA,"inferred from direct assay");
        descriptions.put(GoEvidenceCode.IEA,"inferred from electronic annotation");
        descriptions.put(GoEvidenceCode.IEP,"inferred from expression pattern");
        descriptions.put(GoEvidenceCode.IGI,"inferred from genetic interaction");
        descriptions.put(GoEvidenceCode.IMP,"inferred from mutant phenotype");
        descriptions.put(GoEvidenceCode.IPI,"inferred from physical interaction");
        descriptions.put(GoEvidenceCode.ISS,"inferred from sequence or structural similarity");
        descriptions.put(GoEvidenceCode.NAS,"non-traceable author statement");
        descriptions.put(GoEvidenceCode.ND,"no biological data available");
        descriptions.put(GoEvidenceCode.TAS,"traceable author statement");
        descriptions.put(GoEvidenceCode.NR,"not recorded");
        descriptions.put(GoEvidenceCode.RCA, "reviewed computational analysis");
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
