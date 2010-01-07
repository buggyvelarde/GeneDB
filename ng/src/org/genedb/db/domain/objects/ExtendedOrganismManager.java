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

import java.util.HashMap;
import java.util.Map;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.PhylogenyDao;

import org.gmod.schema.mapped.Cv;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.Phylonode;
import org.gmod.schema.mapped.PhylonodeOrganism;
import org.gmod.schema.mapped.Phylotree;

public class ExtendedOrganismManager {

    private PhylogenyDao phylogenyDao;
    private CvDao cvDao;

    private Map<String, ExtendedOrganism> nameMap = new HashMap<String, ExtendedOrganism>();

    private CvTerm rootType;
    private CvTerm interiorType;
    private CvTerm leafType;

    public ExtendedOrganism getByName(String name) {
        return nameMap.get(name);
    }

    public void afterPropertiesSet() {
        Cv cv = cvDao.getCvByName("wibble"); // FIXME
        rootType = cvDao.getCvTermByNamePatternInCv("root", cv).get(0);
        interiorType = cvDao.getCvTermByNamePatternInCv("interior", cv).get(0);
        leafType = cvDao.getCvTermByNamePatternInCv("leaf", cv).get(0);

        Phylotree tree = phylogenyDao.getPhyloTreeByName("wibble"); // FIXME
        Phylonode rootNode = phylogenyDao.getPhyloNodesByCvTermInTree(rootType, tree).get(0);

        ExtendedOrganism root = new ExtendedOrganism("Home", null);
        nameMap.put("Home", root);

        processChildren(rootNode, root);
    }

    /**
     * Iterate over the children of node, creating a new ExtendedOrganism for
     * each one. This method calls itself recursively to process all children.
     *
     * @param node
     *                the parent node to process the children of
     * @param parent
     *                the corresponding ExtendedOrganism
     */
    private void processChildren(Phylonode node, ExtendedOrganism parent) {

        for (Phylonode childNode : node.getPhylonodes()) {
            ExtendedOrganism child;

            if (childNode.getCvTerm().equals(leafType)) {
                PhylonodeOrganism po = childNode.getPhylonodeOrganisms().iterator().next();
                if (po == null) {
                    // TODO Complain bitterly
                }
                Organism org = po.getOrganism();
                child = new ExtendedOrganism(org.getAbbreviation(), parent);
                // TODO Pick up organism specific values
            } else {
                child = new ExtendedOrganism(childNode.getLabel(), parent);
            }
            // TODO Pick up phylonode specific values
            nameMap.put(child.getShortName(), child);
            processChildren(childNode, child);
        }
    }

}
