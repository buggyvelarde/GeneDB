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



/**
 * This class represents a specific GO entry
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */
public enum GoEvidenceCode {

    IC  ("Inferred by Curator"),
    IDA ("Inferred from Direct Assay"),
    IEA ("Inferred from Electronic Annotation"),
    IEP ("Inferred from Expression Pattern"),
    IGI ("Inferred from Genetic Interaction"),
    IMP ("Inferred from Mutant Phenotype"),
    IPI ("Inferred from Physical Interaction"),
    ISS ("Inferred from Sequence or Structural Similarity"),
    NAS ("Non-traceable Author Statement"),
    ND  ("No biological Data available"),
    TAS ("Traceable Author Statement"),
    NR  ("Not Recorded"),
    RCA ("inferred from Reviewed Computational Analysis");

    private String description;
    private GoEvidenceCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
