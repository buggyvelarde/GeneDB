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
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRIVATE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_EC_NUMBER;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.GeneralDao;
import org.genedb.db.dao.PubDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.ControlledCurationInstance;
import org.genedb.db.loading.ControlledCurationParser;
import org.genedb.db.loading.DbUtilsBean;
import org.genedb.db.loading.FeatureProcessor;
import org.genedb.db.loading.FeatureUtils;
import org.genedb.db.loading.GeneDbGeneNamingStrategy;
import org.genedb.db.loading.GeneNamingStrategy;
import org.genedb.db.loading.MiningUtils;
import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.organism.Organism;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.pub.PubDbXRef;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureCvTermDbXRef;
import org.gmod.schema.sequence.FeatureCvTermProp;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeaturePropPub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    protected DbUtilsBean dbUtilsBean;

    protected Organism organism;

    protected final Log logger = LogFactory.getLog(this.getClass());

    protected GeneNamingStrategy gns = new GeneDbGeneNamingStrategy();

    protected Cv CV_RELATION;

    protected CvTerm REL_PART_OF;

//    protected Cv CV_MISC;
    protected Cv CV_GENEDB;
    protected Cv CV_CONTROLLEDCURATION;
    protected Cv CV_PRODUCT;
    protected Cv CV_FEATURE_PROPERTY;

    protected CvTerm MISC_NOTE;
    protected CvTerm MISC_CURATION;
    protected CvTerm MISC_PRIVATE;
    protected CvTerm MISC_EC_NUMBER;

    private String[] discard = {};

    private String[] requiredMultiple = {};

    private String[] requiredSingle = {};

    private String[] optionalMultiple = {};

    private String[] optionalSingle = {};

    protected Pub DUMMY_PUB;

    protected Cv CV_SO;

    protected Db DB_GO;

    protected CvTerm REL_DERIVES_FROM;

    protected CvTerm GO_KEY_EVIDENCE;
    protected CvTerm GO_KEY_DATE;
    protected CvTerm GO_KEY_QUALIFIER;

    public Set<Db> warnedDbs = new HashSet<Db>();

    protected GeneralDao generalDao;

    protected PubDao pubDao;

    protected Set<String> seenQualifiers = new HashSet<String>();
    String[] handledQualifiers = {};
    List<String> unknownRileyClass;

    protected ControlledCurationParser ccParser;

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

        REL_PART_OF = cvDao.getCvTermByNameInCv("proper_part_of", CV_RELATION).get(0); // FIXME Is this right
        CV_SO = cvDao.getCvByName("sequence").get(0);
        //CV_MISC = cvDao.getCvByName("autocreated").get(0);
        CV_FEATURE_PROPERTY = cvDao.getCvByName("feature_property").get(0);
        CV_RELATION = cvDao.getCvByName("relationship").get(0);
        CV_GENEDB = cvDao.getCvByName("genedb_misc").get(0);
        CV_CONTROLLEDCURATION = cvDao.getCvByName("CC_genedb_controlledcuration").get(0);
        CV_PRODUCT = cvDao.getCvByName("genedb_products").get(0);
        //REL_PART_OF = cvDao.getCvTermByNameInCv("part_of", CV_RELATION).get(0);
        REL_DERIVES_FROM = cvDao.getCvTermByNameInCv("derives_from", CV_SO).get(0);
        MISC_NOTE = cvDao.getCvTermByNameInCv("comment", CV_FEATURE_PROPERTY).get(0);
        MISC_CURATION = cvDao.getCvTermByNameInCv(QUAL_CURATION, CV_GENEDB).get(0);
        MISC_PRIVATE = cvDao.getCvTermByNameInCv(QUAL_PRIVATE, CV_GENEDB).get(0);
        MISC_EC_NUMBER = cvDao.getCvTermByNameInCv(QUAL_EC_NUMBER, CV_GENEDB).get(0);
        DB_GO = generalDao.getDbByName("GO");

        DUMMY_PUB = pubDao.getPubByUniqueName("null");
        //logger.warn("Just looked up DUMMY_PUB and it is '"+DUMMY_PUB+"'");

        //Cv goKeys = cvDao.getCvByName("genedb_fcvt_prop_keys").get(0);
        GO_KEY_EVIDENCE = cvDao.getCvTermByNameInCv("evidence", CV_GENEDB).get(0);
        GO_KEY_QUALIFIER = cvDao.getCvTermByNameInCv("qualifier", CV_GENEDB).get(0);
        GO_KEY_DATE = cvDao.getCvTermByNameInCv("date", CV_FEATURE_PROPERTY).get(0);

    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }


    public void setFeatureUtils(FeatureUtils featureUtils) {
        this.featureUtils = featureUtils;
    }


    public void process(final Feature parent, final org.biojava.bio.seq.Feature feat, final int offset) {
        MiningUtils.sanityCheckAnnotation(feat, requiredSingle, requiredMultiple,
                optionalSingle , optionalMultiple, discard, false, true);

      TransactionTemplate tt = new TransactionTemplate(sequenceDao.getPlatformTransactionManager());
      tt.execute(
              new TransactionCallbackWithoutResult() {
                @Override
                  public void doInTransactionWithoutResult(TransactionStatus status) {
                      @SuppressWarnings("unchecked") Set<String> keySet = feat.getAnnotation().asMap().keySet();
                      for (String key : keySet) {
                          if ("internal_data".equals(key)) {
                              continue; // Don't store internal data - a biojava artifact
                          }
                          seenQualifiers.add(feat.getType()+":"+key);
                      }
                      processStrandedFeature(parent, (StrandedFeature) feat, offset);
                  }
              });

        //processStrandedFeature(parent, (StrandedFeature) feat);
    }

    public abstract void processStrandedFeature(Feature parent, StrandedFeature f, int offset);


    protected FeatureProp createFeatureProp(Feature f, Annotation an, String annotationKey, String dbKey, Cv cv) {
        List<CvTerm> cvTerms = cvDao.getCvTermByNameInCv(annotationKey,cv);

        if (cvTerms == null || cvTerms.size() == 0) {
            throw new RuntimeException("No cvterm of name '"+annotationKey+"' in cv '"+cv.getName()+"'");
        }
        CvTerm cvTerm = cvTerms.get(0);
        String value = MiningUtils.getProperty(annotationKey, an, null);
        FeatureProp fp = new FeatureProp(f, cvTerm, value, 0);

        f.getFeatureProps().add(fp);
        return fp;
    }

    protected int createFeaturePropsFromNotes(org.gmod.schema.sequence.Feature f, Annotation an, String key, CvTerm cvTerm, int startRank) {
        logger.debug("About to set '"+key+"' for feature '" + f.getUniqueName()
                + "'");
        // Cvterm cvTerm = daoFactory.getCvTermDao().findByNameInCv(key,
        // cv).get(0);

        List<String> values = MiningUtils.getProperties(key, an);
        if (values == null || values.size() == 0) {
            return 0;
        }
        List<String> notes = new ArrayList<String>();
        for (String note : values) {
            String[] notesArray = note.split(";");
            for (int i = 0; i < notesArray.length; i++) {
                notes.add(notesArray[i]);
            }
        }

        int rank = startRank;
        for (String note : notes) {
            FeatureProp fp = new FeatureProp(f, cvTerm, note, rank);
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
            sequenceDao.persist(fp);
            rank++;
        }
        return rank - startRank;
    }

    protected void createDbXRefs(org.gmod.schema.sequence.Feature polypeptide, Annotation an) {
        List<String> xrefs = MiningUtils.getProperties(QUAL_DB_XREF, an);
        if (xrefs.size() == 0) {
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
            int index = xref.indexOf(":");
            if (index == -1 ) {
                logger.error("Can't parse dbxref into db and acc '"+xref+"'. Skipping");
                continue;
            }
            String dbName = xref.substring(0, index);
            String acc = xref.substring(index+1);
            //String description = null;
            if (acc.indexOf(";") != -1) {
                String[] parts = acc.split(";");
                if (parts.length>0) {
                    acc = parts[0];
                } else {
                    logger.warn("Can't parse dbxref properly '"+xref+"'. Skipping");
                    continue;
                }
                if (parts.length>1) {
                    //description = parts[1];
                }
            }

            Db db = dbUtilsBean.getDbByName(dbName);
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
                dbXRef = new DbXRef(db, acc);
                // TODO - Mark as needing looking up for description
                generalDao.persist(dbXRef);
                //logger.info("Creating DbXref for db '"+db+"' and acc '"+acc+"'");
                //logger.info("dbXRef just before storage is '"+dbXRef+"'");
                FeatureDbXRef fdr = new FeatureDbXRef(dbXRef, polypeptide, true);
                logger.info("Persisting new FeatureDbXRef, dbXRef='"+dbXRef.getAccession()+"', feature='"+polypeptide.getDisplayName()+"'");
                sequenceDao.persist(fdr);
            } else {
                logger.info("Using an existing dbXRef from the db");
                FeatureDbXRef fdr = sequenceDao.getFeatureDbXRefByFeatureAndDbXRef(polypeptide, dbXRef);
                if (fdr == null) {
                    fdr = new FeatureDbXRef(dbXRef, polypeptide, true);
                    logger.info("Persisting new FeatureDbXRef, dbXRef='"+dbXRef.getAccession()+"', feature='"+polypeptide.getDisplayName()+"'");
                    sequenceDao.persist(fdr);
                }

            }
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
                // TODO Tidy
   //             System.err.println("Systematic id is " + sysId);
   //             List<Feature> features = sequenceDao.getFeaturesByUniqueName(sysId);
   //             if (features == null || features.size()!=1) {
   //             	logger.error("Can't tie feature by name");
   //             	return null;
   //            }
    //            ret = features.get(0);
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
                Feature gene = sequenceDao.getFeatureByUniqueName(sysId, "gene");
                return gene;
            }

            return ret;
        }

    public void setPubDao(PubDao pubDao) {
        this.pubDao = pubDao;
    }

    public void setDbUtilsBean(DbUtilsBean dbUtilsBean) {
        this.dbUtilsBean = dbUtilsBean;
    }

    public List<String> getUnknownRileyClass() {
        return unknownRileyClass;
    }

    public Map<String, Boolean> getQualifierHandlingStatus() {
        Map<String, Boolean> ret = new HashMap<String, Boolean>();
        for (String seenQualifier : seenQualifiers) {
            ret.put(seenQualifier, false);
        }
        for (String handledQualifier : handledQualifiers) {
            if (ret.containsKey(handledQualifier)) {
                ret.put(handledQualifier, true);
            } else {
                logger.warn("'"+this.getClass().getSimpleName()+"' can handle '"+handledQualifier+"' but has never seen one");
            }
        }
        return ret;
    }

    public ProcessingPhase getProcessingPhase() {
        // TODO Auto-generated method stub
        return null;
    }

    public void createControlledCuration(Feature polypeptide, Annotation an, Cv controlledCuration) {

        List<ControlledCurationInstance> ccs = ccParser.getAllControlledCurationFromAnnotation(an);
        if (ccs == null) {
            return;
        }

        Set<ControlledCurationInstance> lhs = new LinkedHashSet<ControlledCurationInstance>();
        lhs.addAll(ccs);
        if (lhs.size() != ccs.size()) {
            logger.warn("Removed '"+(ccs.size()-lhs.size())+"' controlled_curations as apparently duplicates");
            ccs.clear();
            ccs.addAll(lhs);
        }

        int rank = 0;

        for (ControlledCurationInstance cc : ccs) {
            boolean other = false;
            DbXRef dbXRef = null;
            CvTerm cvt = this.cvDao.getCvTermByNameAndCvName(cc.getTerm(), "quality");
            if (cvt == null) {
                cvt = this.cvDao.getCvTermByNameAndCvName(cc.getTerm(), "CC_%");

                if (cvt == null ) {
                    // Got an unrecognized term
                    if (cc.getCv() != null) {
                        logger.error("Got an unrecognized term '"+cc.getTerm()+"' in controlled_curation but it should already exist in '"+cc.getCv()+"'");
                        continue;
                    }

                    Db db = generalDao.getDbByName("CCGEN");
                    dbXRef = new DbXRef(db, "CCGEN_" + cc.getTerm());
                    generalDao.persist(dbXRef);

                    cvt = new CvTerm(controlledCuration, dbXRef, cc.getTerm(), cc.getTerm());
                    generalDao.persist(cvt);
                }
            }


            Pub pub = null;
            String[] list = null;
            // Handle dbxrefs
            if (cc.getDbXRef() != null) {
                list = cc.getDbXRef().split("\\|");
                for (String dbXRef2 : list) {
                    String sections[] = dbXRef2.split(":");
                    if (featureUtils.looksLikePub(dbXRef2)) {
                        //findOrCreatePubFromPMID(sections[0]); // FIXME - could this be a shortcut for below
                        DbXRef dbxref = null;
                        Db db = this.generalDao.getDbByName("MEDLINE");
                        dbxref = this.generalDao.getDbXRefByDbAndAcc(db, sections[1]);
                        if (dbxref == null) {
                            dbxref = new DbXRef(db, sections[1]);
                            generalDao.persist(dbxref);
                        }
                        pub = this.pubDao.getPubByUniqueName(dbXRef2);
                        if (pub == null) {
                            CvTerm cvterm = this.cvDao.getCvTermByNameAndCvName("unfetched", "genedb_literature");
                            //logger.warn("cvterm='"+cvterm+"'");
                            pub = new Pub(dbXRef2, cvterm);
                            this.pubDao.persist(pub);
                            //pubProp = new PubProp(cvt,pub,DbXRef,0);
                            //this.pubDao.persist(pubProp);
                            PubDbXRef pubDb = new PubDbXRef(pub, dbxref, true);
                            this.pubDao.persist(pubDb);
                        }
                    } else {
                        pub = DUMMY_PUB;
                        other = true;
                    }
                }
            } else {
                pub = DUMMY_PUB; // FIXME - probably not right!!
            }


            boolean not = false; // TODO - Should get from GO object
            List<FeatureCvTerm> fcts = sequenceDao.getFeatureCvTermsByFeatureAndCvTermAndNot(polypeptide, cvt, not);
            FeatureCvTerm fct;// = new FeatureCvTerm(cvt, polypeptide, pub, not);
            //sequenceDao.persist(fct);

            if (fcts == null || fcts.size()==0) {
                fct = new FeatureCvTerm(cvt, polypeptide, pub, not,rank);
                sequenceDao.persist(fct);
                logger.info("Persisting new FeatureCvTerm for '"+polypeptide.getUniqueName()+"' with a cvterm of '"+cvt.getName()+"'");
            } else {
                if(fcts.size() > 1){
                    fct = fcts.get(fcts.size() - 1);
                } else {
                    fct = fcts.get(0);
                }
                int r = fct.getRank();
                r++;
                fct = new FeatureCvTerm(cvt,polypeptide, pub, not, r);
                sequenceDao.persist(fct);
                logger.info("Already got FeatureCvTerm for '"+polypeptide.getUniqueName()+"' with a cvterm of '"+cvt.getName()+"'");
            }

            //  FIXME Pass in unix date
            thingy("date", cc.getDate(), CV_FEATURE_PROPERTY, fct, null);
            thingy("attribution", cc.getAttribution(), CV_GENEDB, fct, null);
            thingy("evidence", cc.getEvidence(), CV_GENEDB, fct, null);
            thingy("residue", cc.getResidue(), CV_GENEDB, fct, null);
            thingy("qualifier", cc.getQualifier(), CV_GENEDB, fct, "\\|");


            if (other) {
                other = false;
                for (String dbXRef2 : list) {
                    String sections[] = dbXRef2.split(":");
                    if (sections.length != 2) {
                        logger.error("Unable to parse a dbxref from '"+dbXRef2+"'");
                    } else {
                        if(!featureUtils.looksLikePub(sections[0])) {
                            DbXRef dbxref = null;
                            Db db = this.generalDao.getDbByName(sections[0].toUpperCase());
                            if (db == null) {
                                logger.error("Can't find db by name of '"+db+"' when persisting controlled curation so skipping");
                            } else {
                                dbxref = this.generalDao.getDbXRefByDbAndAcc(db, sections[1]);
                                if (dbxref == null) {
                                    dbxref = new DbXRef(db, sections[1]);
                                    this.generalDao.persist(dbxref);
                                } else {
                                    FeatureCvTermDbXRef fcvDb = new FeatureCvTermDbXRef(dbxref,fct);
                                    this.sequenceDao.persist(fcvDb);
                                }
                            }
                        }
                    }
                }
            }
            rank++;
        }
    }

    private void thingy(String key, String value, Cv controlledCuration, FeatureCvTerm fct, String split) {
        if (value != null) {
            List<CvTerm> cvtL = this.cvDao.getCvTermByNameInCv(key, controlledCuration);
            if (cvtL == null || cvtL.size() == 0) {
                throw new RuntimeException("Expected cvterm '"+key+"' not found");
            }
            CvTerm cvTerm = cvtL.get(0);

            String[] qualifiers = {value};
            if (split != null) {
                qualifiers = value.split(split);
            }
            for (int i = 0; i < qualifiers.length; i++) {
                FeatureCvTermProp fcvp = new FeatureCvTermProp(cvTerm, fct, qualifiers[i], i);
                sequenceDao.persist(fcvp);
            }
        }
    }

    public void setCcParser(ControlledCurationParser ccParser) {
        this.ccParser = ccParser;
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
//	protected String translate(String nucleic, Annotation an, TaxonNode tn) {
//		if (translation != null && translation.length() > 0 ) {
//			this.setSequence(SequenceType.SEQ_PROTEIN, translation);
//			return;
//		}
//		//tn.
//
//
//		Map translationDetails = tn.getAppDetails("translation");
//		String table = (String) translationDetails.get("table");
//		if ( table != null) {
//			TranslationTable tt = RNATools.getGeneticCode(table);
//			if (tt == null) {
//				throw new RuntimeException("WARN: Attempted to set unrecognized translation table '"+table+"'");
//			}
//		}
//
//		int cdStartNum = 1;
//		if (cdStart != null && cdStart.length() != 0) {
//			cdStartNum = Integer.parseInt(cdStart);
//		}
//		if (cdStartNum < 1 || cdStartNum > 3) {
//			LogUtils.bprintln("WARN: Ignoring unexpected value of codon_start ("
//					+ cdStart + ") in " + getId());
//			cdStartNum = 1;
//		}
//		if (cdStartNum != 1 && !isPartial()) {
//			LogUtils.bprintln("WARN: Got non '1' value for codon_start ("
//					+ cdStart + ") but no /partial in " + getId());
//			setPartial(true);
//		}
//
//		if (cdStartNum != 1) {
//			setCodonStart(cdStartNum);
//		}
//
//		SeqTrans.SeqTransResult result =
//		SeqTrans.getInstance().translate(this, getTranslationTableNumber(),
//		getCodonStart().intValue(), codon, except);
//		setProteinWarning(result.getWarning());
//		setSequence(SequenceType.SEQ_PROTEIN, result.getSeq());
//
//	}
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
