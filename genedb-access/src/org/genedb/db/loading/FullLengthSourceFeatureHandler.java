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

import static org.genedb.db.loading.EmblFeatureKeys.FT_SOURCE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_CHROMOSOME;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRIVATE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SO_TYPE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYS_ID;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeVetoException;
import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.mapped.Organism;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This class is the main entry point for GeneDB data miners. It's designed to
 * be called from the command-line, or a Makefile.
 * 
 * Usage: GenericRunner organism [-show_ids] [-show_contigs]
 * 
 * 
 * @author Adrian Tivey (art)
 */
public class FullLengthSourceFeatureHandler extends BaseFeatureHandler implements FeatureHandler {

    //private List<FeatureListener> listeners = new ArrayList<FeatureListener>(0);
    
    
 //   public void afterPropertiesSet() {
//        CV_SO = this.daoFactory.getCvDao().findByName("sequence").get(0);
//        CV_MISC = daoFactory.getCvDao().findByName("autocreated").get(0);
//        CV_RELATION = this.daoFactory.getCvDao().findByName("relationship").get(0);
//        
//        REL_PART_OF = this.daoFactory.getCvTermDao().findByNameInCv("part_of", CV_RELATION).get(0);
//        REL_DERIVES_FROM = this.daoFactory.getCvTermDao().findByNameInCv(
//                "derives_from", CV_SO).get(0);
//        MISC_NOTE = daoFactory.getCvTermDao().findByNameInCv("note", CV_MISC)
//                .get(0);
 //   }


    // private String translate(String nucleic) {
    // if (translation != null && translation.length() > 0 ) {
    // this.setSequence(SequenceType.SEQ_PROTEIN, translation);
    // return;
    // }

    // if ( table != null) {
    // try {
    // int num = Integer.parseInt(table);
    // if (GeneticCodes.isValidTransTable(num)) {
    // setTranslationTableNum(num);
    // } else {
    // System.err.println("WARN: Attempted to set unrecognized translation
    // table ("+table+") in "+getId());
    // }
    // }
    // catch (NumberFormatException exp) {
    // System.err.println("WARN: Attempted to set unrecognized translation
    // table ("+table+") in "+getId());
    // }
    // }

    // int cdStartNum = 1;
    // if (cdStart != null && cdStart.length() != 0) {
    // cdStartNum = Integer.parseInt(cdStart);
    // }
    // if (cdStartNum < 1 || cdStartNum > 3) {
    // LogUtils.bprintln("WARN: Ignoring unexpected value of codon_start ("
    // + cdStart + ") in " + getId());
    // cdStartNum = 1;
    // }
    // if (cdStartNum != 1 && !isPartial()) {
    // LogUtils.bprintln("WARN: Got non '1' value for codon_start ("
    // + cdStart + ") but no /partial in " + getId());
    // setPartial(true);
    // }

    // if (cdStartNum != 1) {
    // setCodonStart(cdStartNum);
    // }

    // SeqTrans.SeqTransResult result =
    // SeqTrans.getInstance().translate(this, getTranslationTableNumber(),
    // getCodonStart().intValue(), codon, except);
    // setProteinWarning(result.getWarning());
    // setSequence(SequenceType.SEQ_PROTEIN, result.getSeq());

    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.genedb.db.loading.FeatureHandler#processSources(org.biojava.bio.seq.Sequence)
     */
    @SuppressWarnings("unchecked")
    public org.gmod.schema.mapped.Feature process(File file, Sequence seq)
            throws ChangeVetoException, BioException {
        FeatureHolder fh = seq.filter(new FeatureFilter.ByType(FT_SOURCE));

        List<Feature> sources = new ArrayList<Feature>();

        Iterator fit = fh.features();
        while (fit.hasNext()) {
            sources.add((Feature) fit.next());
        }
        Collections.sort(sources, Feature.byLocationOrder);

        Feature fullLengthSource = null;
        for (Feature source : sources) {
            Location loc = source.getLocation();
            if (loc.getMin() > 1) {
                break;
            }
            if (loc.getMax() == seq.length()) {
                // Got a full-length source feature
                if (fullLengthSource != null) {
                    // error - there can be only one
                }
                fullLengthSource = source;
            } else {
            	System.err.println("Got a source with max of '"+loc.getMax()+"' but looking for '"+seq.length()+"'");
            }
        }
        if (fullLengthSource == null) {
            // log
            throw new RuntimeException("Can't find full length source");
        }

        // FeatureUtils.dumpFeature(fullLengthSource, "Full length");

        MiningUtils.sanityCheckAnnotation(fullLengthSource, new String[] {
                QUAL_SYS_ID, QUAL_SO_TYPE }, new String[] {},
                new String[] { QUAL_CHROMOSOME },
                new String[] { QUAL_PRIVATE }, new String[]{}, false, true);

        Annotation an = fullLengthSource.getAnnotation();
        String foundType = MiningUtils.getProperty(QUAL_SO_TYPE, an, null);
        String uniqueName = MiningUtils.getProperty(QUAL_SYS_ID, an, null);
        // System.err.println("Would like to create a '"+foundType+"' with name
        // '"+uniqueName+"'");

        Timestamp now = new Timestamp(new Date().getTime()); 
        Chromosome topLevel = Chromosome.make(uniqueName, organism, now);

        topLevel.setResidues(seq.seqString().getBytes());

        sequenceDao.persist(topLevel);
        
        sources.remove(fullLengthSource);
        seq.removeFeature(fullLengthSource);

        for (Feature feature : sources) {
            FeatureUtils.dumpFeature(feature, null);
            seq.removeFeature(feature);
        }
        return topLevel;
    }


//    private void fireEvent(FeatureEvent fe) {
//        for (FeatureListener fl : listeners) {
//            // TODO
//        }
//    }

    @Override
    public void processStrandedFeature(org.gmod.schema.mapped.Feature parent, StrandedFeature feat, int offset) {
        // Dummy method
    }

}
