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

import static org.genedb.db.loading.EmblQualifiers.QUAL_CURATION;
import static org.genedb.db.loading.EmblQualifiers.QUAL_DB_XREF;
import static org.genedb.db.loading.EmblQualifiers.QUAL_D_PSU_DB_XREF;
import static org.genedb.db.loading.EmblQualifiers.QUAL_NOTE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRIVATE;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.FeatureProcessor;
import org.genedb.db.loading.FeatureUtils;
import org.genedb.db.loading.GeneDbGeneNamingStrategy;
import org.genedb.db.loading.GeneNamingStrategy;
import org.genedb.db.loading.MiningUtils;

import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeaturePropPub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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

    protected SequenceDao sequenceDao;
    
    protected CvDao cvDao;
    
    protected FeatureUtils featureUtils;

    protected Organism organism;

    protected final Log logger = LogFactory.getLog(this.getClass());

    protected GeneNamingStrategy gns = new GeneDbGeneNamingStrategy();

    protected Cv CV_RELATION;
    
    protected CvTerm REL_PART_OF;

    protected Cv CV_MISC;
    protected Cv CV_GENEDB;

    protected CvTerm MISC_NOTE;
    protected CvTerm MISC_CURATION;
    protected CvTerm MISC_PRIVATE;

    private String[] discard = {};
    
    private String[] requiredMultiple = {};

    private String[] requiredSingle = {};

    private String[] optionalMultiple = {};

    private String[] optionalSingle = {};

    protected Pub DUMMY_PUB;

    private Cv CV_SO;
    
    protected Db DB_GO;

    protected CvTerm REL_DERIVES_FROM;

    public Set<Db> warnedDbs = new HashSet<Db>();

    private GeneralDao generalDao;
    
    protected PubDao pubDao;

    public BaseFeatureProcessor() {
        // Deliberately empty
    }
    
    public BaseFeatureProcessor(String[] requiredSingle, String[] requiredMultiple, 
            String[] optionalSingle, String[] optionalMultiple, String[] discard) {
        super();
        this.requiredSingle = requiredSingle;
        this.requiredMultiple = requiredMultiple;
        this.optionalSingle = optionalSingle;
        this.optionalMultiple = optionalMultiple;
        this.discard = discard;
    }

    public void afterPropertiesSet() {
        CV_RELATION = cvDao.getCvByName("relationship").get(0);
       
        REL_PART_OF = cvDao.getCvTermByNameInCv("part_of", CV_RELATION).get(0);
        CV_SO = cvDao.getCvByName("sequence").get(0);
        CV_MISC = cvDao.getCvByName("autocreated").get(0);
        CV_RELATION = cvDao.getCvByName("relationship").get(0);
        CV_GENEDB = cvDao.getCvByName("genedb_misc").get(0);
        
        REL_PART_OF = cvDao.getCvTermByNameInCv("part_of", CV_RELATION).get(0);
        REL_DERIVES_FROM = cvDao.getCvTermByNameInCv("derives_from", CV_SO).get(0);
        MISC_NOTE = cvDao.getCvTermByNameInCv(QUAL_NOTE, CV_MISC).get(0);
        MISC_CURATION = cvDao.getCvTermByNameInCv(QUAL_CURATION, CV_MISC).get(0);
        MISC_PRIVATE = cvDao.getCvTermByNameInCv(QUAL_PRIVATE, CV_MISC).get(0);
        DB_GO = generalDao.getDbByName("GO");
        
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }


    public void setFeatureUtils(FeatureUtils featureUtils) {
        this.featureUtils = featureUtils;
    }


    public void process(final org.gmod.schema.sequence.Feature parent, final Feature feat, final int offset) {
        MiningUtils.sanityCheckAnnotation(feat, requiredSingle, requiredMultiple, 
                optionalSingle , optionalMultiple, discard, false, true);
        
      TransactionTemplate tt = new TransactionTemplate(sequenceDao.getPlatformTransactionManager());
      tt.execute(
              new TransactionCallbackWithoutResult() {
                  @Override
                  public void doInTransactionWithoutResult(TransactionStatus status) {
                      processStrandedFeature(parent, (StrandedFeature) feat, offset);
                  }
              });
        
        //processStrandedFeature(parent, (StrandedFeature) feat);
    }
    
    public abstract void processStrandedFeature(org.gmod.schema.sequence.Feature parent, StrandedFeature f, int offset);
    

    protected FeatureProp createFeatureProp(org.gmod.schema.sequence.Feature f, Annotation an, String annotationKey, String dbKey, Cv cv) {
        CvTerm cvTerm = cvDao.getCvTermByNameInCv(annotationKey,cv).get(0);
    
        String value = MiningUtils.getProperty(annotationKey, an, null);
        // TODO Other constructor?
        FeatureProp fp = new FeatureProp();
        fp.setRank(0);
        fp.setCvTerm(cvTerm);
        fp.setFeature(f);
        // fp.setFeaturepropPubs(arg0);
        fp.setValue(value);
    
        f.getFeatureProps().add(fp);
        return fp;
    }

    protected void createFeaturePropsFromNotes(org.gmod.schema.sequence.Feature f, Annotation an, String key, CvTerm cvTerm) {
        logger.debug("About to set '"+key+"' for feature '" + f.getUniqueName()
                + "'");
        // Cvterm cvTerm = daoFactory.getCvTermDao().findByNameInCv(key,
        // cv).get(0);
    
        List<String> values = MiningUtils.getProperties(key, an);
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
            fp.setCvTerm(cvTerm);
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
            fp.setFeaturePropPubs(fpubs);
            fp.setValue(note);
            sequenceDao.persist(fp);
            count++;
        }
        
    }
    
    protected void createDbXRefs(org.gmod.schema.sequence.Feature polypeptide, Annotation an) {
        List<String> xrefs = MiningUtils.getProperties(QUAL_DB_XREF, an);
        if (xrefs == null) {
            xrefs = new ArrayList<String>();
        }
            
        List <String> tmp = MiningUtils.getProperties(QUAL_D_PSU_DB_XREF, an);
        if (tmp != null) {
            xrefs.addAll(tmp);
        }
            
            // TODO EC numbers and literature aren't simple dbxrefs
    //        tmp = MiningUtils.getProperties(QUAL_D_LITERATURE, an);
    //        xrefs.addAll(tmp);
    
    //        tmp = MiningUtils.getProperties(QUAL_D_EC_NUMBER, an);
    //        for (String ecNum : tmp) {
    //            if (ecNum.startsWith("EC:")) {
    //                xrefs.add(ecNum);
    //            } else {
    //                xrefs.add("EC:" + ecNum);
    //            }
    //        }
            
        if (xrefs.size() == 0) {
                return;
            }
            
            for (String xref : xrefs) {
                if (xref.indexOf(":") == -1 ) {
                    logger.warn("Can't parse dbxref into db and acc '"+xref+"'. Skipping");
                    continue;
                }
                String[] parts = xref.split(":");
                String dbName = parts[0];
                String acc = parts[1];
                String description = null;
                if (acc.indexOf(";") != -1) {
                    parts = acc.split(";");
                    if (parts.length>0) {
                        acc = parts[0];
                    } else {
                        logger.warn("Can't parse dbxref properly '"+xref+"'. Skipping");
                        continue;
                    }
                    if (parts.length>1) {
                        description = parts[1];
                    }
                }
                Db db = generalDao.getDbByName(dbName);
                if (db == null) {
                    if (!warnedDbs.contains(db)) {
                        logger.warn("Can't find a db entry for the name of '"+dbName+"'. Skipping");
                        warnedDbs.add(db);
                    }
                    continue;
                }
                //logger.info("Trying to store '"+xref+"'. Got a db");
                 DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(db, acc);
                if (dbXRef == null) {
                    dbXRef = new DbXRef(); // TODO Use constructor?
                    dbXRef.setDb(db);
                    dbXRef.setAccession(acc);
                    dbXRef.setVersion("1"); // TODO - a bit arbitary
                    // TODO - Mark as needing looking up for description
                    generalDao.persist(dbXRef);
                    //logger.info("Creating DbXref for db '"+db+"' and acc '"+acc+"'");
                } else {
                    //logger.info("Using an existing dbXRef from the db");
                }
                //logger.info("dbXRef just before storage is '"+dbXRef+"'");
                FeatureDbXRef fdr = new FeatureDbXRef(dbXRef, polypeptide, true);
                //logger.info("Persisting new FeatureDbXRef");
                sequenceDao.persist(fdr);
                // TODO Store any user supplied notes
            }
            
        }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setCvDao(CvDao cvDao) {
        this.cvDao = cvDao;
    }

    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }


    protected org.gmod.schema.sequence.Feature tieFeatureByNameInQualifier(String qualifier, org.gmod.schema.sequence.Feature parent, StrandedFeature feat, Annotation an, Location loc) {
            org.gmod.schema.sequence.Feature ret = null;
            if (an.containsProperty(qualifier)) {
                //logger.warn("Trying to tie UTR via '"+qualifier+"'");
                // Hopefully the systematic name of a gene
                String sysId = MiningUtils.getProperty(qualifier, an, null);
                ret = sequenceDao.getFeatureByUniqueName(sysId);
                if (ret == null) {
    //                String utrName = this.gns.get5pUtr(gene.getUniquename(), 0);
    //                if ("three_prime_UTR".equals(type)) {
    //                    utrName = this.gns.get3pUtr(gene.getUniquename(), 0);
    //                }
    //                org.genedb.db.jpa.Feature utr = this.featureUtils
    //                .createFeature(type, utrName, this.organism);
    //                FeatureRelationship utrFr = featureUtils.createRelationship(
    //                        utr, gene, REL_PART_OF);
    //                FeatureLoc utrFl = featureUtils.createLocation(parent, utr, 
    //                        loc.getMin()-1, loc.getMax(), (short)feat.getStrand().getValue());
    //                daoFactory.persist(utr);
    //                daoFactory.persist(utrFr);
    //                daoFactory.persist(utrFl);
    //                handled = true;
    
                    // TODO Complain bitterly
                }
            }
            return ret;
        }

    public void setPubDao(PubDao pubDao) {
        this.pubDao = pubDao;
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
