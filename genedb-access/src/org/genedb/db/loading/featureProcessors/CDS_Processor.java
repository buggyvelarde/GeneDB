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

import org.genedb.db.loading.FeatureProcessor;
import org.genedb.db.loading.GoInstance;
import org.genedb.db.loading.GoParser;
import org.genedb.db.loading.MiningUtils;
import org.genedb.db.loading.Names;
import org.genedb.db.loading.NomenclatureHandler;
import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureRelationship;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;

import java.util.ArrayList;
import java.util.Collection;
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
public class CDS_Processor extends BaseFeatureProcessor implements FeatureProcessor {

    private NomenclatureHandler nomenclatureHandler;

    private GoParser goParser;

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
            this.DUMMY_PUB = pubDao.getPubByUniqueName("null");
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
                mRNA.setResidues(cds.getSymbols().seqString());
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
            // TODO protein props - store here or just in query version
            sequenceDao.persist(polypeptide);

            createProducts(polypeptide, an, "product", "product", CV_PRODUCTS);

            // Store feature properties based on original annotation
            createFeatureProp(polypeptide, an, "colour", "colour", CV_MISC);
            // 
            // Cvterm cvTerm =
            // daoFactory.getCvTermDao().findByNameInCv("note",
            // MISC).get(0);
            createFeaturePropsFromNotes(polypeptide, an, QUAL_NOTE, MISC_NOTE);

            createDbXRefs(polypeptide, an);

            createGoEntries(polypeptide, an);

            // String nucleic = parent.getResidues().substring(loc.getMin(),
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

    public void setGoParser(GoParser goParser) {
        this.goParser = goParser;
    }

    private void createGoEntries(Feature polypeptide, Annotation an) {
        List<GoInstance> gos = this.goParser.getNewStyleGoTerm(an);
        if (gos == null || gos.size() == 0) {
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

            // Find or create feature_cvterm
            Pub pub = pubDao.getPubByUniqueName(id);

            if (pub == null) {
                pub = DUMMY_PUB; // FIXME - probably not right!!
            }


            boolean not = false; // TODO - Should get from GO object
            FeatureCvTerm fct = sequenceDao.getFeatureCvTermByFeatureAndCvTerm(polypeptide, cvTerm, not);
            if (fct == null) {
                fct = new FeatureCvTerm(cvTerm, polypeptide, pub, not);
                sequenceDao.persist(fct);
                //logger.info("Persisting new FeatureCvTerm for '"+polypeptide.getUniquename()+"' with a cvterm of '"+cvTerm.getName()+"'");
            } else {
                logger.info("Already got FeatureCvTerm for '"+polypeptide.getUniqueName()+"' with a cvterm of '"+cvTerm.getName()+"'");
            }
        }

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
            this.featureUtils.createSynonym(SYNONYM_RESERVED, names.getReserved(),
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

    private void createProducts(
            Feature f, Annotation an,
            String annotationKey, String dbKey, Cv cv) {

//      List<String> products = MiningUtils.getProperties(annotationKey, an);
//      int i = 0;
//      for (String product : products) {
//      FeatureProp fp = new FeatureProp();
//      fp.setRank(i);
//      fp.setCvterm(cvTerm);
//      fp.setFeature(f);
//      // fp.setFeaturepropPubs(arg0);
//      fp.setValue(value);

//      f.getFeatureProps().add(fp);
//      ret.add(fp);
//      }

//      List<FeatureProp> ret = new ArrayList<FeatureProp>(3);

//      List<CvTerm> cvTerms = daoFactory.getCvTermDao().findByNameInCv(
//      annotationKey, cv);

//      CvTerm cvTerm;
//      if (cvTerms == null || cvTerms.size() == 0) {
//      cvTerm = new CvTerm();
//      cvTerm.setCv(cv);
//      cvTerm.setName(annotationKey);
//      cvTerm.setDefinition(annotationKey);
//      } else {
//      cvTerm = cvTerms.get(0);
//      }


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

}
