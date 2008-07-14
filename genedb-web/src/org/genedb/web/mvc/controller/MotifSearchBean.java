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

import org.gmod.schema.mapped.Organism;

public class MotifSearchBean {
    
    private boolean protein;
    private String pattern;
    private String extJ;
    private String extX;
    private Organism organism;
    
    public String getExtJ() {
        return this.extJ;
    }
    
    public void setExtJ(String extJ) {
        this.extJ = extJ;
    }
    
    public String getExtX() {
        return this.extX;
    }
    
    public void setExtX(String extX) {
        this.extX = extX;
    }
    
    public String getPattern() {
        return this.pattern;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Organism getOrganism() {
    
        return this.organism;
    }

    public void setOrganism(Organism organism) {
    
        this.organism = organism;
    }

    public boolean isProtein() {
    
        return this.protein;
    }

    public void setProtein(boolean protein) {
    
        this.protein = protein;
    }

}
