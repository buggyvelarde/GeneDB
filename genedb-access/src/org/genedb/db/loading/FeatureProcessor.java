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

package org.genedb.db.loading;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.organism.Organism;

public interface FeatureProcessor {

    public void process(org.gmod.schema.sequence.Feature parent, org.biojava.bio.seq.Feature feat, int offset);
    
    public void setFeatureUtils(FeatureUtils featureUtils);
    
    public void setOrganism(Organism organism);
    
    public void afterPropertiesSet();

    public void setCvDao(CvDao cvDao);

    public void setGeneralDao(GeneralDao generalDao);

    public void setSequenceDao(SequenceDao sequenceDao);
    
    public void setPubDao(PubDao pubDao);
    
    public ProcessingPhase getProcessingPhase();
    
}
