/*
 * Copyright (c) 2006-2008 Genome Research Limited.
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
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureProp;

public class Transcript extends LocatedFeature implements Serializable {
    private transient Feature protein;
    private String name;
    private Integer colourId; // May be null
    private BasicGene gene;
    private SortedSet<TranscriptComponent> components;
    private List<String> products;
    private int fmin, fmax;

    @Override
    public String getUniqueName() {
        return name;
    }
    public void setUniqueName(String name) {
        this.name = name;
    }

    @Override
    public int getFmin() {
        return fmin;
    }
    public void setFmin(int fmin) {
        this.fmin = fmin;
    }

    @Override
    public int getFmax() {
        return fmax;
    }
    public void setFmax(int fmax) {
        this.fmax = fmax;
    }

    public SortedSet<TranscriptComponent> getComponents() {
        return components;
    }
    public void setComponents(SortedSet<TranscriptComponent> components) {
        this.components = components;
    }
    public void setComponents(Set<TranscriptComponent> components) {
        this.components = new TreeSet<TranscriptComponent> (components);
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

    public BasicGene getGene() {
        return gene;
    }
    public void setGene(BasicGene gene) {
        this.gene = gene;
    }
    public List<String> getProducts() {
        return products;
    }
    public void setProducts(List<String> products) {
        this.products = products;
    }
}
