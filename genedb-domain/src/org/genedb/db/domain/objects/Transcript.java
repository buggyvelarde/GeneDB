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

package org.genedb.db.domain.objects;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureProp;

public class Transcript implements Serializable {
    private transient Feature protein;
    private Integer colourId; // May be null
    private Set<Exon> exons = Collections.emptySet();

    public Set<Exon> getExons() {
        return exons;
    }
    public void setExons(Set<Exon> exons) {
        this.exons = exons;
    }

    public Integer getColourId() {
        return colourId;
    }
    public void setColourId(Integer colourId) {
        this.colourId = colourId;
    }

    public Feature getProtein() {
        return protein;
    }

    /**
     * Sets the protein property, and also updates the colourId from the protein.
     * 
     * @param protein
     */
    public void setProtein(Feature protein) {
        this.protein = protein;
        for (FeatureProp proteinProp: protein.getFeatureProps()) {
            if (proteinProp.getCvTerm().getName().equals("colour"))
                setColourId(Integer.parseInt(proteinProp.getValue()));
        }
    }

}
