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

import org.genedb.web.mvc.controller.download.OutputFormat;

public class RegionCommand {
    String name;
    //Taxon organism;
    int min;
    int max;
    boolean truncateEndFeatures;
    OutputFormat of;
    public int getMax() {
        return this.max;
    }
    public void setMax(int max) {
        this.max = max;
    }
    public int getMin() {
        return this.min;
    }
    public void setMin(int min) {
        this.min = min;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public OutputFormat getOf() {
        return this.of;
    }
    public void setOf(OutputFormat of) {
        this.of = of;
    }
//    public Taxon getOrganism() {
//        return this.organism;
//    }
//    public void setOrganism(Taxon organism) {
//        this.organism = organism;
//    }
    public boolean isTruncateEndFeatures() {
        return this.truncateEndFeatures;
    }
    public void setTruncateEndFeatures(boolean truncateEndFeatures) {
        this.truncateEndFeatures = truncateEndFeatures;
    }

}
