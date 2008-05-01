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

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;

import java.io.Serializable;

public class Transcript implements Serializable {

    public static Transcript makeTranscript(Feature feature) {
        Transcript ret = new Transcript();
        for (FeatureRelationship fr : feature.getFeatureRelationshipsForObjectId()) {
            Feature otherFeat = fr.getFeatureBySubjectId();
            if (otherFeat.getCvTerm().getName().equals("polypeptide")) {
                ret.setProtein(otherFeat);
            }
        }
        return ret;
    }

    // private Feature feature;
    // private List<Feature> exons;
    private transient Feature protein;

    public Feature getProtein() {
        return protein;
    }

    public void setProtein(Feature protein) {
        this.protein = protein;
    }

}
