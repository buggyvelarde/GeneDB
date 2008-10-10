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

package org.genedb.querying.tmpquery;

public enum BrowseCategory {
    biological_process("GO Biological Process"),
    cellular_component("GO Cellular Component"),
    molecular_function("GO Molecular Function"),
    genedb_products("Products"),
    ControlledCuration("Controlled Curation", "CC_%");

    private String lookupName;
    private String displayName;

    private BrowseCategory(String displayName) {
        this.displayName = displayName;
    }

    private BrowseCategory(String displayName, String lookupName) {
        this.displayName = displayName;
        this.lookupName = lookupName;
    }

    public String getLookupName() {
        if (lookupName != null) {
            return lookupName;
        }
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

}
