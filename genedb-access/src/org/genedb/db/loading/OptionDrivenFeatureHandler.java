/*
 * Copyright (c) 2006 Genome Research Limited.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB. If not, write to the Free
 * Software Foundation Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307
 * USA
 */

/**
 * 
 * 
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */
package org.genedb.db.loading;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.utils.ChangeVetoException;

import java.io.File;

/**
 * This class is the main entry point for GeneDB data miners. It's designed to
 * be called from the command-line, or a Makefile.
 * 
 * Usage: GenericRunner organism [-show_ids] [-show_contigs]
 * 
 * 
 * @author Adrian Tivey (art)
 */
public class OptionDrivenFeatureHandler extends BaseFeatureHandler implements FeatureHandler {

    //private List<FeatureListener> listeners = new ArrayList<FeatureListener>(0);
    /*
     * (non-Javadoc)
     * 
     * @see org.genedb.db.loading.FeatureHandler#processSources(org.biojava.bio.seq.Sequence)
     */
    @SuppressWarnings("unchecked")
    public org.gmod.schema.sequence.Feature process(File file, Sequence seq)
            throws ChangeVetoException, BioException {
        
        String type = getOptions().get("so-type");

        String fileName = file.getName();
        String uniqueName = fileName.substring(0, fileName.lastIndexOf("."));
        
        
        // TODO Get chromosome from path, maybe

        org.gmod.schema.sequence.Feature topLevel = this.featureUtils
                .createFeature(type, uniqueName, this.organism);
        // System.err.println("Got a feature to persist");

        topLevel.setResidues(seq.seqString().getBytes());

        sequenceDao.persist(topLevel);
        // System.err.println("Have persisted feature");

        return topLevel;
            
    }

//    private void fireEvent(FeatureEvent fe) {
//        for (FeatureListener fl : listeners) {
//            // TODO
//        }
//    }

    @Override
    public void processStrandedFeature(org.gmod.schema.sequence.Feature parent, StrandedFeature feat, int offset) {
        // Dummy method
    }

}
