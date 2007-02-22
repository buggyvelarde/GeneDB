/*
 * Copyright (c) 2007 Genome Research Limited.
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

import org.gmod.schema.dao.OrganismDaoI;
import org.gmod.schema.dao.PhylogenyDaoI;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.phylogeny.Phylonode;
import org.gmod.schema.phylogeny.PhylonodeOrganism;

import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaxonNodeManager implements InitializingBean{
    
    private PhylogenyDaoI phylogenyDao;
    
    private OrganismDaoI organismDao;
    
    private Map<String, TaxonNode> labelTaxonNodeMap = new HashMap<String, TaxonNode>();

    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        // Find root node
        // Walk over children
//        Set nodes = new HashSet<TaxonNode>();
//        List<Phylonode> phylonodes = phylogenyDao.getAllNodes();
//        for (Phylonode phylonode: phylonodes) {
//            nodes.add(new TaxonNode(phylonode));
//        }
//        Collections.sort(phylonodes, new Comparator<Phylonode>() {
//            public int compare(Object arg0, Object arg1) {
//                // TODO Auto-generated method stub
//                return 0;
//            }
//            
//        });
        
    }
    
    
    boolean validateTaxons(List<String> taxons, List<String> problems) {
        boolean problem = false;
        Set<String> uniqs = new HashSet<String>(taxons.size());
        for (String taxon : taxons) {
            if (!validateTaxon(taxon)) {
                problems.add(taxon);
                problem = true;
            } else {
                uniqs.add(taxon);
            }
        }
        // If collections are same size no problems and no dupes so leave taxons be
        if (uniqs.size() < taxons.size()) {
            taxons.clear();
            taxons.addAll(uniqs);
        }
        return problem;
    }
    
    
    public List<TaxonNode> getHeirachy(String label) {
        List<TaxonNode> ret = new LinkedList<TaxonNode>();
        TaxonNode node = labelTaxonNodeMap.get(label);
        ret.add(node);
        while (!node.isRoot()) {
            node = node.getParent();
            ret.add(0, node);
        }
        return ret;
    }
    
    private Phylonode getPhlyonodeForOrganism(Organism org) {
        Set<PhylonodeOrganism> pos = org.getPhylonodeOrganisms();
        if (pos.size() != 1) {
            throw new RuntimeException("Found more than one phylonodeOrganism for '"+org.getCommonName()+"'");
        }
        PhylonodeOrganism po = pos.iterator().next();
        return po.getPhylonode(); 
    }
    
    boolean validateTaxon(String taxon) {
        return false; // FIXME
    }
    
    
    String getNameForTaxonId(String taxonId) {
        return null; // FIXME
    }
    
    public TaxonNode getTaxonNodeForLabel(String label) {
        return labelTaxonNodeMap.get(label);
    }

}
