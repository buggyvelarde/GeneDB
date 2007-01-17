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

import static org.genedb.db.loading.EmblQualifiers.QUAL_NOTE;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PRIMARY;
import static org.genedb.db.loading.EmblQualifiers.QUAL_PSEUDO;
import static org.genedb.db.loading.EmblQualifiers.QUAL_RESERVED;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYNONYM;
import static org.genedb.db.loading.EmblQualifiers.QUAL_SYS_ID;
import static org.genedb.db.loading.EmblQualifiers.QUAL_TEMP_SYS_ID;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;

import org.genedb.db.loading.ControlledCurationInstance;
import org.genedb.db.loading.ControlledCurationParser;
import org.genedb.db.loading.FeatureProcessor;
import org.genedb.db.loading.FeatureUtils;
import org.genedb.db.loading.GoInstance;
import org.genedb.db.loading.GoParser;
import org.genedb.db.loading.MiningUtils;
import org.genedb.db.loading.Names;
import org.genedb.db.loading.NomenclatureHandler;
import org.genedb.db.loading.ProcessingPhase;
import org.genedb.db.loading.ProteinUtils;
import org.genedb.db.loading.RankableUtils;

import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.pub.PubDbXRef;
import org.gmod.schema.pub.PubProp;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureCvTermDbXRef;
import org.gmod.schema.sequence.FeatureCvTermProp;
import org.gmod.schema.sequence.FeatureCvTermPub;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.utils.PeptideProperties;

/**
 * This class is the main entry point for GeneDB data miners. It's designed to
 * be called from the command-line, or a Makefile.
 *
 * Usage: GenericRunner organism [-show_ids] [-show_contigs]
 *
 *
 * @author Adrian Tivey (art)
 */
public class CDS_Processor extends BaseFeatureProcessor implements FeatureProcessor {

    private NomenclatureHandler nomenclatureHandler;

    private GoParser goParser;

    private ControlledCurationParser ccParser;

    private int count;

    @Override
    public void processStrandedFeature(final Feature parent, final StrandedFeature cds, final int offset) {
        final Annotation an = cds.getAnnotation();

        if (an.containsProperty(QUAL_PSEUDO)) {
            processPseudoGene();
        } else {
            processCodingGene(cds, an, parent, offset);
        }
    }

    @SuppressWarnings( { "unchecked" })
    private void processCodingGene(StrandedFeature cds, Annotation an,
            Feature parent, int offset) {
        String sysId = null;
        try {
            Location loc = cds.getLocation().translate(offset);
            Names names = this.nomenclatureHandler.findNames(an);
            sysId = names.getSystematicId();
            logger.debug("Looking at systematic id '" + sysId+"'");
            int transcriptNum = 1;

            short strand = (short) cds.getStrand().getValue();

            List<Feature> features = new ArrayList<Feature>();
            List<FeatureLoc> featureLocs = new ArrayList<FeatureLoc>();
            List<FeatureRelationship> featureRelationships = new ArrayList<FeatureRelationship>();

            Cv CV_NAMING = cvDao.getCvByName("genedb_synonym_type").get(0);
            CvTerm SYNONYM_RESERVED = cvDao.getCvTermByNameInCv(QUAL_RESERVED, CV_NAMING).get(0);
            CvTerm SYNONYM_SYNONYM = cvDao.getCvTermByNameInCv(QUAL_SYNONYM, CV_NAMING).get(0);
            CvTerm SYNONYM_PRIMARY = cvDao.getCvTermByNameInCv(QUAL_PRIMARY, CV_NAMING).get(0);
            CvTerm SYNONYM_SYS_ID = cvDao.getCvTermByNameInCv(QUAL_SYS_ID, CV_NAMING).get(0);
            CvTerm SYNONYM_TMP_SYS = cvDao.getCvTermByNameInCv(QUAL_TEMP_SYS_ID, CV_NAMING).get(0);
            CvTerm SYNONYM_PROTEIN = cvDao.getCvTermByNameInCv("protein_name", CV_NAMING).get(0);
            this.DUMMY_PUB = pubDao.getPubByUniqueName("NULL");
            this.featureUtils.setDummyPub(this.DUMMY_PUB);

            Cv CV_PRODUCTS = cvDao.getCvByName("genedb_products").get(0);
            //Db DB_PRODUCTS = this.daoFactory.getDbDao().findByName("genedb_products_db");

            // Is this a multiply spliced gene?
            Feature gene = null;
            boolean altSplicing = false;
            String sharedId = MiningUtils.getProperty("shared_id", an, null);
            if (sharedId != null) {
                gene = sequenceDao.getFeatureByUniqueName(sharedId);
                altSplicing = true;
            }

            if (gene == null) {
                if (altSplicing) {
                    gene = this.featureUtils.createFeature("gene", sharedId,
                            this.organism);
                    sequenceDao.persist(gene);
                    this.featureUtils.createSynonym(SYNONYM_SYS_ID, sharedId, gene,
                            true);
                } else {
                    gene = this.featureUtils.createFeature("gene", this.gns
                            .getGene(sysId), this.organism);
                    if (names.getPrimary()!= null) {
                        gene.setName(names.getPrimary());
                    }
                    sequenceDao.persist(gene);
                    storeNames(names, SYNONYM_RESERVED, SYNONYM_SYNONYM,
                            SYNONYM_PRIMARY, SYNONYM_SYS_ID, SYNONYM_TMP_SYS,
                            gene);
                }
                // features.add(gene);
                // this.daoFactory.persist(gene);

                FeatureLoc geneFl = this.featureUtils.createLocation(parent, gene,
                        loc.getMin()-1, loc.getMax(), strand);
                gene.getFeatureLocsForFeatureId().add(geneFl);
                featureLocs.add(geneFl);
            } else {
                if (altSplicing) {
                    // Gene already exists and it's alternately spliced.
                    // It may not have the right coords - may need extending
                    int newMin = loc.getMin()-1;
                    int newMax = loc.getMax();
                    Collection<FeatureLoc> locs = gene.getFeatureLocsForFeatureId();
                    FeatureLoc currentLoc = locs.iterator().next();  // FIXME - Assumes that only 1 feature loc.
                    int currentMin = currentLoc.getFmin();
                    int currentMax = currentLoc.getFmax();
                    if (currentMin > newMin) {
                        currentLoc.setFmin(newMin);
                        logger.debug("Would like to change min to '"+newMin+"' from '"+currentMin+"' for '"+sysId+"'");
                    }
                    if (currentMax < newMax) {
                        currentLoc.setFmax(newMax);
                        logger.debug("Would like to change max to '"+newMax+"' from '"+currentMax+"' for '"+sysId+"'");
                    }
                }
            }

            String mRnaName = this.gns.getTranscript(sysId, transcriptNum);
            String baseName = sysId;
            if (altSplicing) {
                mRnaName = this.gns.getGene(sysId);
                baseName = mRnaName;
            }
            Feature mRNA = this.featureUtils
            .createFeature("mRNA", mRnaName, this.organism);
            if (!loc.isContiguous()) {
                mRNA.setResidues(cds.getSymbols().seqString().getBytes());
            }
            sequenceDao.persist(mRNA);
            if (altSplicing) {
                storeNames(names, SYNONYM_RESERVED, SYNONYM_SYNONYM,
                        SYNONYM_PRIMARY, SYNONYM_SYS_ID, SYNONYM_TMP_SYS, mRNA);
            }
            FeatureLoc mRNAFl = this.featureUtils.createLocation(parent, mRNA, loc
                    .getMin()-1, loc.getMax(), strand);
            mRNA.getFeatureLocsForFeatureId().add(mRNAFl);
            FeatureRelationship mRNAFr = this.featureUtils.createRelationship(mRNA,
                    gene, REL_PART_OF, 0);
            // features.add(mRNA);
            featureLocs.add(mRNAFl);
            featureRelationships.add(mRNAFr);

            // Store exons
            Iterator<Location> it = loc.blockIterator();
            int exonCount = 0;
            while (it.hasNext()) {
                exonCount++;
                Location l = it.next();
                Feature exon = this.featureUtils
                .createFeature("exon", this.gns.getExon(baseName,
                        transcriptNum, exonCount), this.organism);
                FeatureRelationship exonFr = this.featureUtils.createRelationship(
                        exon, mRNA, REL_PART_OF, exonCount -1);
                FeatureLoc exonFl = this.featureUtils.createLocation(parent, exon, l
                        .getMin()-1, l.getMax(), strand);
                features.add(exon);
                featureLocs.add(exonFl);
                featureRelationships.add(exonFr);
            }

            Feature polypeptide = this.featureUtils
            .createFeature("polypeptide", this.gns.getPolypeptide(
                    baseName, transcriptNum), this.organism);
            // TODO Protein name - derived from gene name in some cases.
            // TODO where store protein name synonym - on polypeptide or
            // gene
            if (names.getProtein() != null) {
                polypeptide.setName(names.getProtein());
                this.featureUtils.createSynonym(SYNONYM_PROTEIN, names.getProtein(),
                        polypeptide, true);
            }
            FeatureRelationship pepFr = this.featureUtils.createRelationship(
                    polypeptide, mRNA, REL_DERIVES_FROM, 0);
            //features.add(polypeptide);
            FeatureLoc pepFl = this.featureUtils.createLocation(parent, polypeptide,
                    loc.getMin()-1, loc.getMax(), strand);
            featureLocs.add(pepFl);
            featureRelationships.add(pepFr);
            // TODO store protein translation
            // calculatePepstats(polypeptide); // TODO Uncomment once checked if currently working
            sequenceDao.persist(polypeptide);

            createProducts(polypeptide, an, "product", CV_PRODUCTS);

            // Store feature properties based on original annotation
            createFeatureProp(polypeptide, an, "colour", "colour", CV_MISC);
            //
            // Cvterm cvTerm =
            // daoFactory.getCvTermDao().findByNameInCv("note",
            // MISC).get(0);
            createFeaturePropsFromNotes(polypeptide, an, QUAL_NOTE, MISC_NOTE);

            createDbXRefs(polypeptide, an);

            createGoEntries(polypeptide, an);

            createControlledCuration(polypeptide,an,CV_CONTROLLEDCURATION);
            
            processClass(polypeptide,an);

            //String nucleic = parent.getResidues().substring(loc.getMin(),
            // loc.getMax());
            // String protein = translate(nucleic);
            // polypeptide.setResidues(protein);

            // Now persist gene heirachy
            for (Feature feature : features) {
                // System.err.print("Trying to persist
                // "+feature.getUniquename());
                sequenceDao.persist(feature);
                // System.err.println(" ... done");
            }
            for (FeatureLoc location : featureLocs) {
                sequenceDao.persist(location);
            }
            for (FeatureRelationship relationship : featureRelationships) {
                sequenceDao.persist(relationship);
            }
            System.err.print(".");
        } catch (RuntimeException exp) {
            System.err.println("\n\nWas looking at '" + sysId + "'");
            throw exp;
        }
    }

    private void processClass(Feature polypeptide, Annotation an) {
    	List<String> classes = MiningUtils.getProperties("class", an);
    	if (classes != null){
	    	for (String rileyClass : classes) {
				if("1.5.0".equals(rileyClass)){
					System.out.println("i am here ... ");
				}
	    		String sections[] = rileyClass.split("\\.");
				StringBuilder sb = new StringBuilder();
				for (String string : sections) {
					if(string.length() >= 2){
						if(string.charAt(0) == '0'){
							sb.append(string.substring(1) + ".");
						} else {
							sb.append(string + ".");
						}
					} else {
						sb.append(string + ".");
					}
				}
				rileyClass = sb.toString().substring(0, sb.toString().length()-1);
	    		Db db = this.generalDao.getDbByName("RILEY");
	    		DbXRef dbXRef = this.generalDao.getDbXRefByDbAndAcc(db,rileyClass);
	    		CvTerm cvTerm = this.cvDao.getCvTermByDbXRef(dbXRef);
	    		Pub pub = DUMMY_PUB;
	    		FeatureCvTerm fct = new FeatureCvTerm(cvTerm,polypeptide,pub,false,0);
	    		this.sequenceDao.persist(fct);
			}
    	}
	}

	private void createControlledCuration(Feature polypeptide, Annotation an, Cv controlledCuration) {

        List<ControlledCurationInstance> ccs = this.ccParser.getAllControlledCurationFromAnnotation(an);
        if (ccs == null || ccs.size() == 0) {
                return;
        }
        int rank = 0;
        for (ControlledCurationInstance cc : ccs) {
                boolean other = false;
                DbXRef dbXRef = null;
                CvTerm cvt = this.cvDao.getCvTermByNameAndCvName(cc.getTerm(), "CC_%");

                if (cvt == null ) {
                    Db db = generalDao.getDbByName("CCGEN");
                    dbXRef = new DbXRef(db, "CCGEN_" + cc.getTerm());
                    generalDao.persist(dbXRef);
                    
                    Cv cv = controlledCuration;
                    if (cc.getCv() != null) {
                        cv = this.cvDao.getCvByName("CC" + cc.getCv()).get(0);
                    }
                    cvt = new CvTerm(cv, dbXRef, cc.getTerm(), cc.getTerm());
                    generalDao.persist(cvt);
                }

                Pub pub = null;
                PubProp pubProp = null;
                List<String> list = new ArrayList<String>();
                if (cc.getDbXRef() != null){
                    if (cc.getDbXRef().contains("|")){
                        StringTokenizer st = new StringTokenizer(cc.getDbXRef(),"|");
                        while (st.hasMoreTokens()){
                            list.add(st.nextToken());
                        }
                    } else {
                        list.add(cc.getDbXRef());
                    }
                    for (String DbXRef : list) {
                        String sections[] = DbXRef.split(":");
                        if("PMID".equals(sections[0])) {
                            DbXRef dbxref = null;
                            Db db = this.generalDao.getDbByName("MEDLINE");
                            dbxref = this.generalDao.getDbXRefByDbAndAcc(db, sections[1]);
                            if (dbxref == null) {
                                dbxref = new DbXRef(db, sections[1]);
                                generalDao.persist(dbxref);
                            }
                            pub = this.pubDao.getPubByUniqueName(DbXRef);
                            if (pub == null) {
                                CvTerm cvterm = this.cvDao.getCvTermByNameAndCvName("Not Fetched", "genedb_literature");
                                //logger.warn("cvterm='"+cvterm+"'");
                                pub = new Pub(DbXRef, cvterm);
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
                    fct = new FeatureCvTerm(cvt,polypeptide,pub,not,r);
                    sequenceDao.persist(fct);
                    logger.info("Already got FeatureCvTerm for '"+polypeptide.getUniqueName()+"' with a cvterm of '"+cvt.getName()+"'");
                }

                if (cc.getDate() != null) {
                    List<CvTerm> cvtL = this.cvDao.getCvTermByNameInCv("date", controlledCuration);
                    CvTerm cvTerm = null;
                    if (cvtL == null || cvtL.size() == 0) {
                        Db d = generalDao.getDbByName("GeneDB_Spombe");
                        DbXRef dbxref = new DbXRef();
                        dbxref.setDb(d);
                        dbxref.setAccession("date");
                        dbxref.setVersion("1");
                        sequenceDao.persist(dbxref);
                        cvTerm = new CvTerm();
                        cvTerm.setCv(controlledCuration);
                        cvTerm.setDbXRef(dbxref);
                        cvTerm.setName("date");
                        cvTerm.setDefinition("date value from controlled_curation qualifier");
                        sequenceDao.persist(cvTerm);
                    } else {
                        cvTerm = cvtL.get(0);
                    }
                    FeatureCvTermProp fcvp = new FeatureCvTermProp(cvTerm,fct,cc.getDate(),0);
                    sequenceDao.persist(fcvp);
                }
                if (cc.getAttribution() != null) {
                    List<CvTerm> cvtL = this.cvDao.getCvTermByNameInCv("attribution", controlledCuration);
                    CvTerm cvTerm = null;
                    if (cvtL == null || cvtL.size() == 0) {
                        Db d = generalDao.getDbByName("GeneDB_Spombe");
                        DbXRef dbxref = new DbXRef();
                        dbxref.setDb(d);
                        dbxref.setAccession("attribution");
                        dbxref.setVersion("1");
                        sequenceDao.persist(dbxref);
                        cvTerm = new CvTerm();
                        cvTerm.setCv(controlledCuration);
                        cvTerm.setDbXRef(dbxref);
                        cvTerm.setName("attribution");
                        cvTerm.setDefinition("attribution value from controlled_curation qualifier");
                        sequenceDao.persist(cvTerm);
                    } else {
                        cvTerm = cvtL.get(0);
                    }
                    FeatureCvTermProp fcvp = new FeatureCvTermProp(cvTerm,fct,cc.getAttribution(),0);
                    sequenceDao.persist(fcvp);
                }
                if (cc.getEvidence() != null) {
                    List<CvTerm> cvtL = this.cvDao.getCvTermByNameInCv("evidence", controlledCuration);
                    CvTerm cvTerm = null;
                    if (cvtL == null || cvtL.size() == 0) {
                        Db d = generalDao.getDbByName("GeneDB_Spombe");
                        DbXRef dbxref = new DbXRef();
                        dbxref.setDb(d);
                        dbxref.setAccession("evidence");
                        dbxref.setVersion("1");
                        sequenceDao.persist(dbxref);
                        cvTerm = new CvTerm();
                        cvTerm.setCv(controlledCuration);
                        cvTerm.setDbXRef(dbxref);
                        cvTerm.setName("evidence");
                        cvTerm.setDefinition("evidence value from controlled_curation qualifier");
                        sequenceDao.persist(cvTerm);
                    } else {
                        cvTerm = cvtL.get(0);
                    }
                    FeatureCvTermProp fcvp = new FeatureCvTermProp(cvTerm,fct,cc.getEvidence(),0);
                    sequenceDao.persist(fcvp);
                }
                if (cc.getQualifier() != null) {
                    List<CvTerm> cvtL = this.cvDao.getCvTermByNameInCv("qualifier", controlledCuration);
                    CvTerm cvTerm = null;
                    if (cvtL == null || cvtL.size() == 0) {
                        Db d = generalDao.getDbByName("GeneDB_Spombe");
                        DbXRef dbxref = new DbXRef();
                        dbxref.setDb(d);
                        dbxref.setAccession("qualifier");
                        dbxref.setVersion("1");
                        sequenceDao.persist(dbxref);
                        cvTerm = new CvTerm();
                        cvTerm.setCv(controlledCuration);
                        cvTerm.setDbXRef(dbxref);
                        cvTerm.setName("qualifier");
                        cvTerm.setDefinition("qualifier value from controlled_curation qualifier");
                        sequenceDao.persist(cvTerm);
                    } else {
                        cvTerm = cvtL.get(0);
                    }
                    if(cc.getQualifier().contains("|")){
                        StringTokenizer st = new StringTokenizer(cc.getQualifier(),"|");
                        int i = 0;
                        while(st.hasMoreTokens()){
                            FeatureCvTermProp fcvp = new FeatureCvTermProp(cvTerm,fct,st.nextToken(),i);
                            i++;
                            sequenceDao.persist(fcvp);
                        }
                    } else {
                        FeatureCvTermProp fcvp = new FeatureCvTermProp(cvTerm,fct,cc.getQualifier(),rank);
                        sequenceDao.persist(fcvp);
                    }
                }
                if (cc.getResidue() != null) {
                    List<CvTerm> cvtL = this.cvDao.getCvTermByNameInCv("residue", controlledCuration);
                    CvTerm cvTerm = null;
                    if (cvtL == null || cvtL.size() == 0) {
                        Db d = generalDao.getDbByName("GeneDB_Spombe");
                        DbXRef dbxref = new DbXRef();
                        dbxref.setDb(d);
                        dbxref.setAccession("residue");
                        dbxref.setVersion("1");
                        sequenceDao.persist(dbxref);
                        cvTerm = new CvTerm();
                        cvTerm.setCv(controlledCuration);
                        cvTerm.setDbXRef(dbxref);
                        cvTerm.setName("residue");
                        cvTerm.setDefinition("residue value from controlled_curation qualifier");
                        sequenceDao.persist(cvTerm);
                    } else {
                        cvTerm = cvtL.get(0);
                    }
                    FeatureCvTermProp fcvp = new FeatureCvTermProp(cvTerm,fct,cc.getResidue(),0);
                    sequenceDao.persist(fcvp);
                }
                if (other) {
                    other = false;
                    for (String DbXRef : list) {
                        String sections[] = DbXRef.split(":");
                        if (sections.length != 2) {
                            logger.error("Unable to parse a dbxref from '"+DbXRef+"'");
                        } else {
                            if(!("PMID".equals(sections[0]))){
                                DbXRef dbxref = null;
                                Db db = this.generalDao.getDbByName(sections[0].toUpperCase());
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
                rank++;
        }
   }

        public void setGoParser(GoParser goParser) {
        this.goParser = goParser;
    }




    private void createGoEntries(Feature polypeptide, Annotation an) {
        List<GoInstance> gos = this.goParser.getNewStyleGoTerm(an);
        if (gos.size() == 0) {
            return;
        }

        for (GoInstance go : gos) {
            // Find db_xref for go id
            String id = go.getId();
            //logger.debug("Investigating storing GO '"+id+"' on '"+polypeptide.getUniquename()+"'");

            CvTerm cvTerm = cvDao.getGoCvTermByAccViaDb(id);
            if (cvTerm == null) {
                logger.warn("Unable to find a CvTerm for the GO id of '"+id+"'. Skipping");
                continue;
            }

            Pub pub = pubDao.getPubByUniqueName("NULL");
            String ref = go.getRef();
            // Reference
            Pub refPub = pub;
            if (ref != null && ref.startsWith("PMID:")) {
                // The reference is a pubmed id - usual case
                refPub = findOrCreatePubFromPMID(ref);
                //FeatureCvTermPub fctp = new FeatureCvTermPub(refPub, fct);
                //sequenceDao.persist(fctp);
            }
            
            
//            logger.warn("pub is '"+pub+"'");

            boolean not = go.getQualifierList().contains("not"); // FIXME - Working?
            List<FeatureCvTerm> fcts = sequenceDao.getFeatureCvTermsByFeatureAndCvTermAndNot(polypeptide, cvTerm, not);
            int rank = 0;
            if (fcts.size() != 0) {
                rank = RankableUtils.getNextRank(fcts);
            }
            //logger.warn("fcts size is '"+fcts.size()+"' and rank is '"+rank+"'");
            FeatureCvTerm fct = new FeatureCvTerm(cvTerm, polypeptide, refPub, not, rank);
            sequenceDao.persist(fct);

            // Reference
//            Pub refPub = null;
//            if (ref != null && ref.startsWith("PMID:")) {
//                // The reference is a pubmed id - usual case
//                refPub = findOrCreatePubFromPMID(ref);
//                FeatureCvTermPub fctp = new FeatureCvTermPub(refPub, fct);
//                sequenceDao.persist(fctp);
//            }

            // Evidence
            FeatureCvTermProp fctp = new FeatureCvTermProp(GO_KEY_EVIDENCE , fct, go.getEvidence().getDescription(), 0);
            sequenceDao.persist(fctp);

            // Qualifiers
            CvTerm qualifierTerm = null;
            int qualifierRank = 0;
            List<String> qualifiers = go.getQualifierList();
            for (String qualifier : qualifiers) {
                fctp = new FeatureCvTermProp(GO_KEY_QUALIFIER , fct, qualifier, qualifierRank);
                qualifierRank++;
                sequenceDao.persist(fctp);
            }

            // With/From
           String xref = go.getWithFrom();
           if (xref != null) {
                   int index = xref.indexOf(':');
                   if (index == -1 ) {
                           logger.error("Got an apparent dbxref but can't parse");
                   } else {
                           List<DbXRef> dbXRefs= findOrCreateDbXRefsFromString(xref);
                           for (DbXRef dbXRef : dbXRefs) {
                               if (dbXRef != null) {
                                   FeatureCvTermDbXRef fcvtdbx = new FeatureCvTermDbXRef(dbXRef, fct);
                                   sequenceDao.persist(fcvtdbx);
                               }
                           }
                   }
           }

            //logger.info("Persisting new FeatureCvTerm for '"+polypeptide.getUniquename()+"' with a cvterm of '"+cvTerm.getName()+"'");
        }

    }

    
    private PeptideProperties calculatePepstats(Feature polypeptide) {

        String seqString = FeatureUtils.getResidues(polypeptide);
        Alphabet protein = ProteinTools.getAlphabet();
        SymbolTokenization proteinToke = null;
        SymbolList seq = null;
        PeptideProperties pp = new PeptideProperties();
        try {
            proteinToke = protein.getTokenization("token");
            seq = new SimpleSymbolList(proteinToke, seqString);
        } catch (BioException e) {

        }
        IsoelectricPointCalc ipc = new IsoelectricPointCalc();
        Double cal = 0.0;
        try {
            cal = ipc.getPI(seq, false, false);
        } catch (IllegalAlphabetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BioException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DecimalFormat df = new DecimalFormat("#.##");
        pp.setIsoelectricPoint(df.format(cal));
        
        CvTerm MISC_ISOELECTRIC = cvDao.getCvTermByNameAndCvName("isoelectric_point", "genedb_misc"); 
        CvTerm MISC_MASS = cvDao.getCvTermByNameAndCvName("molecular mass", "genedb_misc"); 
        CvTerm MISC_CHARGE = cvDao.getCvTermByNameAndCvName("protein_charge", "genedb_misc"); 
        
        
        FeatureProp fp = new FeatureProp(polypeptide, MISC_ISOELECTRIC, df.format(cal), 0);
        
        
        pp.setAminoAcids(Integer.toString(seqString.length()));
        MassCalc mc = new MassCalc(SymbolPropertyTable.AVG_MASS,false);
        try {
            cal = mc.getMass(seq)/1000;
        } catch (IllegalSymbolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pp.setMass(df.format(cal));
        
        fp = new FeatureProp(polypeptide, MISC_MASS, df.format(cal), 0);
        
        cal = ProteinUtils.getCharge(seq);
        pp.setCharge(df.format(cal));
        
        
        fp = new FeatureProp(polypeptide, MISC_CHARGE, df.format(ProteinUtils.getCharge(seq)), 0);
        
        return pp;
    }
    
    private boolean looksLikePub(String xref) {
        if (xref.startsWith("PMID:")) {
            return true;
        }
        return false;
    }
    
    private void findPubOrDbXRefFromString(String xrefString, List<Pub> pubs, List<DbXRef> dbXRefs) {
        boolean makePubs = (pubs != null) ? true : false;
        String[] xrefs = xrefString.split("\\|");
        for (String xref : xrefs) {
            if (makePubs && looksLikePub(xref)) {
                pubs.add(findOrCreatePubFromPMID(xref));
            } else {
                DbXRef dbXRef = findOrCreateDbXRefFromString(xref);
                if (dbXRef != null) {
                    dbXRefs.add(dbXRef);
                }
            }
        }
    }
    
    private List<DbXRef> findOrCreateDbXRefsFromString(String xref) {
        List<DbXRef> ret = new ArrayList<DbXRef>();
        StringTokenizer st = new StringTokenizer(xref, "|");
        while (st.hasMoreTokens()) {
            ret.add(findOrCreateDbXRefFromString(st.nextToken()));
        }
        return ret;
    }
        
        
    private DbXRef findOrCreateDbXRefFromString(String xref) {
        int index = xref.indexOf(':');
        if (index == -1) {
                logger.error("Can't parse '"+xref+"' as a dbxref as no colon");
                return null;
        }
        String dbName = xref.substring(0, index);
        String accession = xref.substring(index+1);
        Db db = generalDao.getDbByName(dbName);
        if (db == null) {
            logger.error("Can't find database named '"+dbName+"'");
            return null;
        }
        DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(db, accession);
        if (dbXRef == null) {
            dbXRef = new DbXRef(db, accession);
            sequenceDao.persist(dbXRef);
        }
        return dbXRef;
    }

    protected Pub findOrCreatePubFromPMID(String ref) {
        logger.warn("Looking for '"+ref+"'");
        Db DB_PUBMED = generalDao.getDbByName("MEDLINE");
        int colon = ref.indexOf(":");
        String accession = ref;
        if (colon != -1) {
                accession = ref.substring(colon+1);
        }
        DbXRef dbXRef = generalDao.getDbXRefByDbAndAcc(DB_PUBMED, accession);
        Pub pub;
        if (dbXRef == null) {
                dbXRef = new DbXRef(DB_PUBMED, accession);
                generalDao.persist(dbXRef);
                CvTerm cvTerm = cvDao.getCvTermById(1); //TODO -Hack
                pub = new Pub("PMID:"+accession, cvTerm);
                generalDao.persist(pub);
                PubDbXRef pubDbXRef = new PubDbXRef(pub, dbXRef, true);
                generalDao.persist(pubDbXRef);
        } else {
                logger.warn("DbXRef wasn't null");
                pub = pubDao.getPubByDbXRef(dbXRef);
        }
        logger.warn("returning pub='"+pub+"'");
        return pub;
    }
    
    private void storeNames(Names names, CvTerm SYNONYM_RESERVED,
            CvTerm SYNONYM_SYNONYM, CvTerm SYNONYM_PRIMARY,
            CvTerm SYNONYM_SYS_ID, CvTerm SYNONYM_TMP_SYS,
            Feature gene) {
        if (names.isIdTemporary()) {
            this.featureUtils.createSynonym(SYNONYM_TMP_SYS,
                    names.getSystematicId(), gene, true);
        } else {
            this.featureUtils.createSynonym(SYNONYM_SYS_ID, names.getSystematicId(),
                    gene, true);
        }
        if (names.getPrimary() != null) {
            gene.setName(names.getPrimary());
            this.featureUtils.createSynonym(SYNONYM_PRIMARY, names.getPrimary(),
                    gene, true);
        }
        if (names.getReserved() != null) {
                this.featureUtils.createSynonyms(SYNONYM_RESERVED, names.getReserved(),
                                gene, true);
        }
        if (names.getSynonyms() != null) {
            this.featureUtils.createSynonyms(SYNONYM_SYNONYM, names.getSynonyms(),
                    gene, true);
        }
        if (names.getObsolete() != null) {

        }
        if (names.getPreviousSystematicIds() != null) {
            this.featureUtils.createSynonyms(SYNONYM_SYS_ID, names
                    .getPreviousSystematicIds(), gene, false);
        }
    }

    private void processPseudoGene() {
        // TODO Pseudogenes
        return;
    }

    private void createProducts(Feature f, Annotation an,String annotationKey, Cv cv) {
        List<String> products = MiningUtils.getProperties(annotationKey, an);
        boolean not = false;
        if (products != null && products.size() != 0 ){
            for (String product : products) {
                CvTerm cvTerm = null;
                List<CvTerm> cvTermList = this.cvDao.getCvTermByNameInCv(product, cv);
                if(cvTermList == null || cvTermList.size() == 0){
                    cvTerm = new CvTerm();
                    DbXRef dbXRef = new DbXRef(this.generalDao.getDbByName("PRODUCT"), product);
                    this.generalDao.persist(dbXRef);
                    cvTerm = new CvTerm();
                    cvTerm.setCv(cv);
                    cvTerm.setDbXRef(dbXRef);
                    cvTerm.setName(product);
                    cvTerm.setDefinition(product);
                    this.cvDao.persist(cvTerm);
                } else {
                    cvTerm = cvTermList.get(0);
                }
                Pub pub = DUMMY_PUB;
                FeatureCvTerm fct = new FeatureCvTerm(cvTerm,f,pub,not,0);
                this.sequenceDao.persist(fct);
            }
        }
    }

//  private ParsedString parseDbXref(String in, String prefix) {
//  ParsedString ret;
//  String lookFor = "(" + prefix;
//  int index = in.indexOf(lookFor);
//  if (index != -1) {
//  int rbracket = in.indexOf(")", index);
//  String part = in.substring(index + 1, rbracket);
//  in = in.substring(0, index) + in.substring(rbracket + 1);
//  ret = new ParsedString(in, part);
//  } else {
//  ret = new ParsedString(in, null);
//  }
//  return ret;
//  }

//  private String translate(String nucleic) {
//  if (translation != null && translation.length() > 0 ) {
//  this.setSequence(SequenceType.SEQ_PROTEIN, translation);
//  return;
//  }

//  if ( table != null) {
//  try {
//  int num = Integer.parseInt(table);
//  if (GeneticCodes.isValidTransTable(num)) {
//  setTranslationTableNum(num);
//  } else {
//  System.err.println("WARN: Attempted to set unrecognized translation
//  table ("+table+") in "+getId());
//  }
//  }
//  catch (NumberFormatException exp) {
//  System.err.println("WARN: Attempted to set unrecognized translation
//  table ("+table+") in "+getId());
//  }
//  }

//  int cdStartNum = 1;
//  if (cdStart != null && cdStart.length() != 0) {
//  cdStartNum = Integer.parseInt(cdStart);
//  }
//  if (cdStartNum < 1 || cdStartNum > 3) {
//  LogUtils.bprintln("WARN: Ignoring unexpected value of codon_start ("
//  + cdStart + ") in " + getId());
//  cdStartNum = 1;
//  }
//  if (cdStartNum != 1 && !isPartial()) {
//  LogUtils.bprintln("WARN: Got non '1' value for codon_start ("
//  + cdStart + ") but no /partial in " + getId());
//  setPartial(true);
//  }

//  if (cdStartNum != 1) {
//  setCodonStart(cdStartNum);
//  }

//  SeqTrans.SeqTransResult result =
//  SeqTrans.getInstance().translate(this, getTranslationTableNumber(),
//  getCodonStart().intValue(), codon, except);
//  setProteinWarning(result.getWarning());
//  setSequence(SequenceType.SEQ_PROTEIN, result.getSeq());

//  }


    public void setNomenclatureHandler(NomenclatureHandler nomenclatureHandler) {
        this.nomenclatureHandler = nomenclatureHandler;
    }

    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.FIRST;
    }

        public ControlledCurationParser getCcParser() {
                return ccParser;
        }

        public void setCcParser(ControlledCurationParser ccParser) {
                this.ccParser = ccParser;
        }

}
