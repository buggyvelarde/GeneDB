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

package org.genedb.db.loading;


import org.gmod.schema.dao.PhylogenyDaoI;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.phylogeny.Phylonode;
import org.gmod.schema.phylogeny.PhylonodeOrganism;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaxonNodeManager implements InitializingBean{
    
    private PhylogenyDaoI phylogenyDao;
    
    private Map<String, TaxonNode> labelTaxonNodeMap = new HashMap<String, TaxonNode>();
    private Map<String, TaxonNode> taxonTaxonNodeMap = new HashMap<String, TaxonNode>();
    private Map<String, TaxonNode> fullNameTaxonNodeMap = new HashMap<String, TaxonNode>();
    private Map<String, TaxonNode> dbNameTaxonNodeMap = new HashMap<String, TaxonNode>();
    private Map<String, TaxonNode> nickNameTaxonNodeMap = new HashMap<String, TaxonNode>();

    public void afterPropertiesSet() throws Exception {
        Set<TaxonNode> nodes = new HashSet<TaxonNode>();
        List<Phylonode> phylonodes = phylogenyDao.getAllPhylonodes();
        if (phylonodes == null || phylonodes.size() == 0) {
            throw new RuntimeException("Got empty list for phylonodes");
        }
        for (Phylonode phylonode: phylonodes) {
            TaxonNode tn = new TaxonNode(phylonode);
            nodes.add(tn);
            labelTaxonNodeMap.put(tn.getShortName(), tn);
            taxonTaxonNodeMap.put(tn.getTaxonId(), tn);
            fullNameTaxonNodeMap.put(tn.getFullName(), tn);
            dbNameTaxonNodeMap.put(tn.getDbName(), tn);
            nickNameTaxonNodeMap.put(tn.getNickName(), tn);
        }
        
        System.err.println("About to try and create relationships");
        // Set up all child/parent relationships
        while (nodes.size() > 0) {
            Set<TaxonNode> tempNodes = new HashSet<TaxonNode>();
            for (Iterator it = nodes.iterator(); it.hasNext();) {
                TaxonNode tn = (TaxonNode) it.next();
                Phylonode phylonode = tn.getPhylonode();
                Phylonode node = phylonode.getParent();
                if (node != null) {
                    if (labelTaxonNodeMap.containsKey(node.getLabel())) {
                        System.err.println("Match found for '"+node.getLabel()+"'");
                        TaxonNode parent = labelTaxonNodeMap.get(node.getLabel());
                        parent.addChild(tn);
                    } else {
                        System.err.println("No match for '"+node.getLabel()+"'");
                        tempNodes.add(tn);
                    }
                } else {
                    System.err.println("Skipping one - maybe Root?");
                    
                }
            }
            nodes = tempNodes;
        }
        
        
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
    
    
    public List<TaxonNode> getHeirachy(TaxonNode start) {
        TaxonNode node = start;
        List<TaxonNode> ret = new LinkedList<TaxonNode>();
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
    
    public TaxonNode getTaxonNodeByString(String name, boolean includeNickName) {
        TaxonNode ret = getTaxonNodeForLabel(name);
        if (ret != null) {
            return ret;
        }
        ret = taxonTaxonNodeMap.get(name);
        if (ret != null) {
            return ret;
        }
        ret = fullNameTaxonNodeMap.get(name);
        if (ret != null) {
            return ret;
        }
        ret = dbNameTaxonNodeMap.get(name);
        if (ret != null) {
            return ret;
        }
        if (!includeNickName) {
            return ret;
        }
        return nickNameTaxonNodeMap.get(name);
    }

    @Required
    public void setPhylogenyDao(PhylogenyDaoI phylogenyDao) {
        this.phylogenyDao = phylogenyDao;
    }

}
