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

package org.genedb.db.taxon;


import org.genedb.db.dao.PhylogenyDao;

import org.gmod.schema.mapped.Phylonode;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaxonNodeManager implements InitializingBean{

    private PhylogenyDao phylogenyDao;
    private SessionFactory sessionFactory;

    private Map<String, TaxonNode> labelTaxonNodeMap = new HashMap<String, TaxonNode>();
    private Map<String, TaxonNode> taxonTaxonNodeMap = new HashMap<String, TaxonNode>();
    private Map<String, TaxonNode> fullNameTaxonNodeMap = new HashMap<String, TaxonNode>();
    private Map<String, TaxonNode> nickNameTaxonNodeMap = new HashMap<String, TaxonNode>();

    public void afterPropertiesSet() throws Exception {
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        try {
        //System.err.println("Session is '"+session+"'");
        Set<TaxonNode> nodes = new HashSet<TaxonNode>();
        List<Phylonode> phylonodes = phylogenyDao.getAllPhylonodes();
        if (phylonodes == null || phylonodes.size() == 0) {
            throw new RuntimeException("Got empty list for phylonodes");
        }
        for (Phylonode phylonode: phylonodes) {
            TaxonNode tn = new TaxonNode(phylonode);
            nodes.add(tn);
            labelTaxonNodeMap.put(tn.getLabel(), tn);
            taxonTaxonNodeMap.put(tn.getTaxonId(), tn);
            fullNameTaxonNodeMap.put(tn.getName(TaxonNameType.FULL), tn);
            String tmp = tn.getName(TaxonNameType.NICKNAME);
            if (tmp != null) {
                nickNameTaxonNodeMap.put(tn.getName(TaxonNameType.NICKNAME), tn);
            }
        }

        // Set up all child/parent relationships
        while (nodes.size() > 0) {
            Set<TaxonNode> tempNodes = new HashSet<TaxonNode>();
            for (TaxonNode tn: nodes) {
                Phylonode phylonode = tn.getPhylonode();
                Phylonode parent = phylonode.getParent();
                if (parent != null) {
                    if (labelTaxonNodeMap.containsKey(parent.getLabel())) {
                        TaxonNode parentTn = labelTaxonNodeMap.get(parent.getLabel());
                        parentTn.addChild(tn);
                    } else {
                        //System.err.println("No match for '"+node.getLabel()+"'");
                        tempNodes.add(tn);
                    }
                } else {
                    System.err.println("Skipping one - maybe Root?");

                }
            }
            nodes = tempNodes;
        }
        //System.err.println("Session is '"+session+"'");
        }
        finally {
              TransactionSynchronizationManager.unbindResource(sessionFactory);
              SessionFactoryUtils.closeSession(session);
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


    public List<TaxonNode> getHierarchy(TaxonNode start) {
        TaxonNode node = start;
        List<TaxonNode> ret = new LinkedList<TaxonNode>();
        ret.add(node);
        while (!node.isRoot()) {
            node = node.getParent();
            ret.add(0, node);
        }
        return ret;
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
        if (!includeNickName) {
            return ret;
        }
        return nickNameTaxonNodeMap.get(name);
    }

    @Required
    public void setPhylogenyDao(PhylogenyDao phylogenyDao) {
        this.phylogenyDao = phylogenyDao;
    }

    @Required
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
