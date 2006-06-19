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
package org.genedb.db.loading.featureProcessors;

import org.genedb.db.dao.DaoFactory;
import org.genedb.db.hibernate3gen.Cv;
import org.genedb.db.hibernate3gen.CvTerm;
import org.genedb.db.hibernate3gen.FeatureProp;
import org.genedb.db.hibernate3gen.FeaturePropPub;
import org.genedb.db.hibernate3gen.Organism;
import org.genedb.db.loading.FeatureProcessor;
import org.genedb.db.loading.FeatureUtils;
import org.genedb.db.loading.GeneDbGeneNamingStrategy;
import org.genedb.db.loading.GeneNamingStrategy;
import org.genedb.db.loading.MiningUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is the main entry point for GeneDB data miners. It's designed to
 * be called from the command-line, or a Makefile.
 * 
 * Usage: GenericRunner organism [-show_ids] [-show_contigs]
 * 
 * 
 * @author Adrian Tivey (art)
 */
public abstract class BaseFeatureProcessor implements FeatureProcessor {

    protected FeatureUtils featureUtils;

    protected DaoFactory daoFactory;

    protected Organism organism;

    protected final Log logger = LogFactory.getLog(this.getClass());

    protected GeneNamingStrategy gns = new GeneDbGeneNamingStrategy();

    protected Cv CV_RELATION;
    
    protected CvTerm REL_PART_OF;

    protected Cv CV_MISC;

    protected CvTerm MISC_NOTE;

    private String[] requiredMultiple = {};

    private String[] requiredSingle = {};

    private String[] optionalMultiple = {};

    private String[] optionalSingle = {};

    public BaseFeatureProcessor() {
        // Deliberately empty
    }
    
    public BaseFeatureProcessor(String[] requiredSingle, String[] requiredMultiple, String[] optionalSingle, String[] optionalMultiple) {
        super();
        this.requiredSingle = requiredSingle;
        this.requiredMultiple = requiredMultiple;
        this.optionalSingle = optionalSingle;
        this.optionalMultiple = optionalMultiple;
    }

    public void afterPropertiesSet() {
        CV_RELATION = this.daoFactory.getCvDao().findByName("relationship").get(0);
       
        REL_PART_OF = this.daoFactory.getCvTermDao().findByNameInCv("part_of", CV_RELATION).get(0);
//      CV_SO = this.daoFactory.getCvDao().findByName("sequence").get(0);
        CV_MISC = daoFactory.getCvDao().findByName("autocreated").get(0);
//        CV_RELATION = this.daoFactory.getCvDao().findByName("relationship").get(0);
//        
//        REL_PART_OF = this.daoFactory.getCvTermDao().findByNameInCv("part_of", CV_RELATION).get(0);
//        REL_DERIVES_FROM = this.daoFactory.getCvTermDao().findByNameInCv(
//                "derives_from", CV_SO).get(0);
        MISC_NOTE = daoFactory.getCvTermDao().findByNameInCv("note", CV_MISC)
                .get(0);
    }

    public void setDaoFactory(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }


    public void setFeatureUtils(FeatureUtils featureUtils) {
        this.featureUtils = featureUtils;
    }


    public void process(org.genedb.db.jpa.Feature parent, Feature feat) {
        MiningUtils.sanityCheckAnnotation(feat, requiredSingle, requiredMultiple, 
                optionalSingle , optionalMultiple, true, true);
        processStrandedFeature(parent, (StrandedFeature) feat);
    }
    
    public abstract void processStrandedFeature(org.genedb.db.jpa.Feature parent, StrandedFeature f);
    

    protected FeatureProp createFeatureProp(org.genedb.db.jpa.Feature f, Annotation an, String annotationKey, String dbKey, Cv cv) {
        CvTerm cvTerm = daoFactory.getCvTermDao().findByNameInCv(annotationKey,
                cv).get(0);
    
        String value = MiningUtils.getProperty(annotationKey, an, null);
    
        FeatureProp fp = new FeatureProp();
        fp.setRank(0);
        fp.setCvterm(cvTerm);
        fp.setFeature(f);
        // fp.setFeaturepropPubs(arg0);
        fp.setValue(value);
    
        f.getFeatureProps().add(fp);
        return fp;
    }

    protected void createFeaturePropsFromNotes(org.genedb.db.jpa.Feature f, Annotation an, CvTerm cvTerm) {
        logger.debug("About to set notes for feature '" + f.getUniquename()
                + "'");
        // Cvterm cvTerm = daoFactory.getCvTermDao().findByNameInCv(key,
        // cv).get(0);
    
        List<String> values = MiningUtils.getProperties("note", an);
        if (values == null || values.size() == 0) {
            return;
        }
        List<String> notes = new ArrayList<String>();
        for (String note : values) {
            String[] notesArray = note.split(";");
            for (int i = 0; i < notesArray.length; i++) {
                notes.add(notesArray[i]);   
            }
        }
    
        int count = 0;
        for (String note : notes) {
            FeatureProp fp = new FeatureProp();
            fp.setRank(count);
            fp.setCvterm(cvTerm);
            fp.setFeature(f);
            // TODO Parse info from (PMID:...) if present
            // TODO cope with more than one
            Set<FeaturePropPub> fpubs = new HashSet<FeaturePropPub>();
            //ParsedString ps;
            // do {
            // ps = parseDbXref(note, "PMID:");
            // note = ps.getMain();
            // String pmid = ps.getExtract();
            // Pub pub = daoFactory.getPubDao().findOrCreateByPmid(pmid);
            // FeaturePub fpub = new FeaturePub();
            // fpub.setFeature(f);
            // fpub.setPub(pub);
            // // FIXME - should add fpubs.add(fpub);
            // } while (ps.isSplit());
            fp.setFeaturepropPubs(fpubs);
            fp.setValue(note);
            count++;
        }
    }

//    private ParsedString parseDbXref(String in, String prefix) {
//            ParsedString ret;
//            String lookFor = "(" + prefix;
//            int index = in.indexOf(lookFor);
//            if (index != -1) {
//                int rbracket = in.indexOf(")", index);
//                String part = in.substring(index + 1, rbracket);
//                in = in.substring(0, index) + in.substring(rbracket + 1);
//                ret = new ParsedString(in, part);
//            } else {
//                ret = new ParsedString(in, null);
//            }
//            return ret;
//        }
    //
    //    // private String translate(String nucleic) {
    //    // if (translation != null && translation.length() > 0 ) {
    //    // this.setSequence(SequenceType.SEQ_PROTEIN, translation);
    //    // return;
    //    // }
    //
    //    // if ( table != null) {
    //    // try {
    //    // int num = Integer.parseInt(table);
    //    // if (GeneticCodes.isValidTransTable(num)) {
    //    // setTranslationTableNum(num);
    //    // } else {
    //    // System.err.println("WARN: Attempted to set unrecognized translation
    //    // table ("+table+") in "+getId());
    //    // }
    //    // }
    //    // catch (NumberFormatException exp) {
    //    // System.err.println("WARN: Attempted to set unrecognized translation
    //    // table ("+table+") in "+getId());
    //    // }
    //    // }
    //
    //    // int cdStartNum = 1;
    //    // if (cdStart != null && cdStart.length() != 0) {
    //    // cdStartNum = Integer.parseInt(cdStart);
    //    // }
    //    // if (cdStartNum < 1 || cdStartNum > 3) {
    //    // LogUtils.bprintln("WARN: Ignoring unexpected value of codon_start ("
    //    // + cdStart + ") in " + getId());
    //    // cdStartNum = 1;
    //    // }
    //    // if (cdStartNum != 1 && !isPartial()) {
    //    // LogUtils.bprintln("WARN: Got non '1' value for codon_start ("
    //    // + cdStart + ") but no /partial in " + getId());
    //    // setPartial(true);
    //    // }
    //
    //    // if (cdStartNum != 1) {
    //    // setCodonStart(cdStartNum);
    //    // }
    //
    //    // SeqTrans.SeqTransResult result =
    //    // SeqTrans.getInstance().translate(this, getTranslationTableNumber(),
    //    // getCodonStart().intValue(), codon, except);
    //    // setProteinWarning(result.getWarning());
    //    // setSequence(SequenceType.SEQ_PROTEIN, result.getSeq());
    //
    //    // }
    //
    //    /*
    //     * (non-Javadoc)
    //     * 
    //     * @see org.genedb.db.loading.FeatureHandler#processSources(org.biojava.bio.seq.Sequence)
    //     */
    //    @SuppressWarnings("unchecked")
    //    public org.genedb.db.hibernate.Feature processSources(Sequence seq)
    //            throws ChangeVetoException, BioException {
    //        FeatureHolder fh = seq.filter(new FeatureFilter.ByType(FT_SOURCE));
    //
    //        List<Feature> sources = new ArrayList<Feature>();
    //
    //        Iterator fit = fh.features();
    //        while (fit.hasNext()) {
    //            sources.add((Feature) fit.next());
    //        }
    //        Collections.sort(sources, Feature.byLocationOrder);
    //
    //        Feature fullLengthSource = null;
    //        for (Feature source : sources) {
    //            Location loc = source.getLocation();
    //            if (loc.getMin() > 1) {
    //                break;
    //            }
    //            if (loc.getMin() == 1 && loc.getMax() == seq.length()) {
    //                // Got a full-length source feature
    //                if (fullLengthSource != null) {
    //                    // error - there can be only one
    //                }
    //                fullLengthSource = source;
    //            }
    //        }
    //        if (fullLengthSource == null) {
    //            // log
    //            throw new RuntimeException("Can't find full length source");
    //        }
    //
    //        // FeatureUtils.dumpFeature(fullLengthSource, "Full length");
    //
    //        MiningUtils.sanityCheckAnnotation(fullLengthSource, new String[] {
    //                QUAL_SYS_ID, QUAL_SO_TYPE }, new String[] {},
    //                new String[] { QUAL_CHROMOSOME },
    //                new String[] { QUAL_PRIVATE }, false, true);
    //
    //        Annotation an = fullLengthSource.getAnnotation();
    //        String foundType = MiningUtils.getProperty(QUAL_SO_TYPE, an, null);
    //        String uniqueName = MiningUtils.getProperty(QUAL_SYS_ID, an, null);
    //        // System.err.println("Would like to create a '"+foundType+"' with name
    //        // '"+uniqueName+"'");
    //
    //        org.genedb.db.hibernate.Feature topLevel = this.featureUtils
    //                .createFeature(foundType, uniqueName, this.organism);
    //        // System.err.println("Got a feature to persist");
    //
    //        topLevel.setResidues(seq.seqString());
    //        topLevel.setMd5checksum(FeatureUtils.calcMD5(seq.seqString())); // FIXME
    //        // -
    //        // should
    //        // be
    //        // set
    //        // by
    //        // setResidues
    //
    //        this.daoFactory.persist(topLevel);
    //        // System.err.println("Have persisted feature");
    //
    //        sources.remove(fullLengthSource);
    //        seq.removeFeature(fullLengthSource);
    //
    //        for (Feature feature : sources) {
    //            FeatureUtils.dumpFeature(feature, null);
    //            seq.removeFeature(feature);
    //        }
    //        return topLevel;
    //    }
    //
    //    public void setDaoFactory(DaoFactory daoFactory) {
    //        this.daoFactory = daoFactory;
    //    }
    //
    //    public void setOrganism(Organism organism) {
    //        this.organism = organism;
    //    }
    //
    //    private void test(String t) {
    //        ParsedString ps = parseDbXref(t, "PMID:");
    //        System.err.println("test='" + t + "'");
    //        System.err.println("Main='" + ps.getMain() + "'");
    //        System.err.println("Extract='" + ps.getExtract() + "'");
    //    }
    //
    //    public static void main(String[] args) {
    //        TRna_Processor sfh = new TRna_Processor();
    //        sfh.test("string a (PMID:12345)");
    //        sfh.test("string a(PMID:12345)");
    //    }
    //
    //    public void setFeatureUtils(FeatureUtils featureUtils) {
    //        this.featureUtils = featureUtils;
    //    }
    //
    //    public void setNomenclatureHandler(NomenclatureHandler nomenclatureHandler) {
    //        this.nomenclatureHandler = nomenclatureHandler;
    //    }
    //
    //    public void addFeatureListener(FeatureListener fl) {
    //        listeners.add(fl);
    //    }
    //
    //    public void removeFeatureListener(FeatureListener fl) {
    //        listeners.remove(fl);
    //    }
    //
    //    private void fireEvent(FeatureEvent fe) {
    //        for (FeatureListener fl : listeners) {
    //            // TODO
    //        }
    //    }

}
