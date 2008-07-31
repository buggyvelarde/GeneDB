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

package org.genedb.db.helpers;

import org.genedb.db.dao.OrganismDao;

import org.gmod.schema.mapped.Organism;

// import java.util.List;

public class NameLookup {

    private String lookup; // The name to lookup, using * for wildcards
    // private List<Taxon> taxons; // The organism(s) to search through
    private boolean needWildcards; // Whether to add wildcards at the beginning
                                    // and end
    private int start; // Which number to start at if paging
    private int pageSize; // How many results per page, if paging
    // private FeatureTypeFilter filter;
    private Organism organism;
    private String orglist;
    private OrganismDao organismDao;

    public String getLookup() {
        return this.lookup;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }

    public boolean isNeedWildcards() {
        return this.needWildcards;
    }

    public void setNeedWildcards(boolean needWildcards) {
        this.needWildcards = needWildcards;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getStart() {
        return this.start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism org) {
        this.organism = org;
    }

    public OrganismDao getOrganismDao() {
        return organismDao;
    }

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }

    public String getOrglist() {
        return orglist;
    }

    public void setOrglist(String orglist) {
        this.orglist = orglist;
        /*
         * List<Organism> org = new ArrayList<Organism>(); Organism o =
         * organismDao.getOrganismByCommonName(orglist); org.add(o);
         * setOrganisms(org);
         */
    }

    // public List<Taxon> getTaxons() {
    // return this.taxons;
    // }
    //
    // public void setTaxons(List<Taxon> taxons) {
    // this.taxons = taxons;
    // }

}
