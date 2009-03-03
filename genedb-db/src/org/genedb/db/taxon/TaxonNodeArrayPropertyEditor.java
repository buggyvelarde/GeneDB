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

package org.genedb.db.taxon;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;

public class TaxonNodeArrayPropertyEditor extends PropertyEditorSupport {
    
    private TaxonNodeManager taxonNodeManager;

	@Override
    public String getAsText() {
        TaxonNode[] nodes = (TaxonNode[]) getValue();
        if (nodes == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (TaxonNode node : nodes) {
            if (!first) {
                builder.append(":");
            }
            builder.append(node.getLabel());
            first = false;
        }
        return builder.toString();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (! StringUtils.hasText(text)) {
            return; // TODO Should this set Root
            //throw new IllegalArgumentException("Text must not be empty or null");
        }
        String[] parts = text.split(":");
//        System.err.println("Split into '"+parts.length+"' parts");
        List<TaxonNode> nodeList = new ArrayList<TaxonNode>(0); 
        for (String part : parts) {
            TaxonNode node = taxonNodeManager.getTaxonNodeByString(part, true);
            if (node == null) {
                throw new IllegalArgumentException("Can't parse '"+part+"' as a organism identifier");
            }
            nodeList.add(node);
        }
        TaxonNode[] nodes = nodeList.toArray(new TaxonNode[1]);
//        System.err.println("Setting as '"+w.length+"' parts");
        setValue(nodes);
//        System.err.println("Exiting normally");
    }

    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }

    
    
}
