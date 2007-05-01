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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.genedb.db.loading.SimilarityInstance;
import org.genedb.db.loading.SimilarityParser;

import org.gmod.schema.analysis.Analysis;
import org.gmod.schema.analysis.AnalysisFeature;
import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.Db;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.pub.Pub;
import org.gmod.schema.pub.PubDbXRef;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureCvTermDbXRef;
import org.gmod.schema.sequence.FeatureCvTermProp;
import org.gmod.schema.sequence.FeatureCvTermPub;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeaturePub;
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

    private SimilarityParser siParser;

    private int count;

    @Override
    public void processStrandedFeature(final Feature parent, final StrandedFeature cds, final int offset) {
        final Annotation an = cds.getAnnotation();

        //if (an.containsProperty(QUAL_PSEUDO)) {
        //    processPseudoGene();
        //} else {
            processCodingGene(cds, an, parent, offset);
        //}
    }

    @SuppressWarnings( { "unchecked" })
    private void processCodingGene(StrandedFeature cds, Annotation an,
            Feature parent, int offset) {
    	
    	String sysId = null;
    	
    	String soTypeGene = "gene";
    	String soTypeTranscript = "mRNA";
    	String soTypeExon = "exon";
    	boolean pseudo = an.containsProperty(QUAL_PSEUDO);
    	
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
                // TODO Tidy
                List<Feature> featureList = sequenceDao.getFeaturesByUniqueName(sharedId);
                if (featureList != null && featureList.size()==1) {
                	gene = featureList.get(0);
                }
                altSplicing = true;
            }

            if (gene == null) {
                if (altSplicing) {
                    gene = this.featureUtils.createFeature(soTypeGene, sharedId,
                            this.organism);
                    sequenceDao.persist(gene);
                    this.featureUtils.createSynonym(SYNONYM_SYS_ID, sharedId, gene,
                            true);
                } else {
                    gene = this.featureUtils.createFeature(soTypeGene, this.gns
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
            .createFeature(soTypeTranscript, mRnaName, this.organism);
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
                .createFeature(soTypeExon, this.gns.getExon(baseName,
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
            
            if(an.containsProperty("pseudo")) {
            	CvTerm pseudoGene = this.cvDao.getCvTermByNameAndCvName("pseudogene", "sequence");
            	gene.setCvTerm(pseudoGene);
            	//this.sequenceDao.merge(gene);
            	
            	CvTerm pseudogenicTranscript = this.cvDao.getCvTermByNameAndCvName("pseudogenic_transcript", "sequence");
            	mRNA.setCvTerm(pseudogenicTranscript);
            	//this.sequenceDao.merge(mRNA);
            }
            createProducts(polypeptide, an, "product", CV_PRODUCTS);

            // Store feature properties based on original annotation
            createFeatureProp(polypeptide, an, "colour", "colour", CV_GENEDB);
            //
            // Cvterm cvTerm =
            // daoFactory.getCvTermDao().findByNameInCv("note",
            // MISC).get(0);
            createFeaturePropsFromNotes(polypeptide, an, QUAL_NOTE, MISC_NOTE);
            
            processArtemisFile(polypeptide, an);
            
            createDbXRefs(polypeptide, an);

            createGoEntries(polypeptide, an);

            createControlledCuration(polypeptide,an,CV_CONTROLLEDCURATION);

            //TODO enable this and code for it in createSimilarity method
            //createSimilarity(polypeptide,mRNA,an);

            processClass(polypeptide,an);
            
            createEC_number(polypeptide,an);
            
            createLiterature(polypeptide,an);
            
            createCuration(polypeptide,an);
            
            createPrivate(polypeptide,an);
            //String nucleic = parent.getResidues().substring(loc.getMin(),
            // loc.getMax());
            // String protein = translate(nucleic);
            // polypeptide.setResidues(protein);

            // Now persist gene heirachy
            
            
        	if (an.containsProperty("pseudo")) {
        		CvTerm pseudogenicExon = this.cvDao.getCvTermByNameAndCvName("pseudogenic_exon", "sequence");
        		for (Feature feature : features) {
        			feature.setCvTerm(pseudogenicExon);
        			sequenceDao.persist(feature);
        		}
            } else {
            	for (Feature feature : features) {
        			sequenceDao.persist(feature);
        		}
            }
            for (FeatureLoc location : featureLocs) {
                sequenceDao.persist(location);
            }
            for (FeatureRelationship relationship : featureRelationships) {
                sequenceDao.persist(relationship);
            }
            //System.err.print(".");
        } catch (RuntimeException exp) {
            System.err.println("\n\nWas looking at '" + sysId + "'");
            throw exp;
        }
    }


    private void createPrivate(Feature polypeptide, Annotation an) {
    	List<String> privates = MiningUtils.getProperties("private", an);
    	if(privates != null) {
    		int rank = 0;
    		for (String pri : privates) {
    			FeatureProp fp = new FeatureProp(polypeptide,MISC_PRIVATE,pri,rank);
    			this.sequenceDao.persist(fp);
    			rank++;
    		}
    	}
	}

	private void createCuration(Feature polypeptide, Annotation an) {
		List<String> curations = MiningUtils.getProperties("curation", an);
    	if(curations != null) {
    		int rank = 0;
    		for (String curation : curations) {
    			FeatureProp fp = new FeatureProp(polypeptide,MISC_CURATION,curation,rank);
    			this.sequenceDao.persist(fp);
    			rank++;
    		}
    	}
	}

	private void createLiterature(Feature polypeptide, Annotation an) {
    	List<String> literatures = MiningUtils.getProperties("literature", an);
    	if(literatures != null) {
    		for (String literature : literatures) {
				String sections[] = literature.split(";");
				Pub pub = this.pubDao.getPubByUniqueName(sections[0]);
				if (pub == null) {
					CvTerm cvt = this.cvDao.getCvTermByNameAndCvName("unfetched", "genedb_literature");
					pub = new Pub(sections[0],cvt);
					this.pubDao.persist(pub);
				}
				FeaturePub fp = new FeaturePub(polypeptide,pub);
				this.sequenceDao.persist(fp);
			}
    	}
	}

	private void createEC_number(Feature polypeptide, Annotation an) {
    	List<String> ecNumbers = MiningUtils.getProperties("EC_number", an);
    	int rank = 0;
    	for (String ecNumber : ecNumbers) {
    		FeatureProp fp = new FeatureProp(polypeptide, MISC_EC_NUMBER, ecNumber, rank);
    		this.sequenceDao.persist(fp);
    		rank++;
    	}
	}


	private void processIndividualArtemisFile(Feature polypeptide, Annotation an, String propertyName) {
		processIndividualArtemisFile(polypeptide, an, propertyName, propertyName);
	}
	
	private void processIndividualArtemisFile(Feature polypeptide, Annotation an, String propertyName, String cvTermName) {
		List<String> bFile = MiningUtils.getProperties(propertyName, an);
		int rank = 0;
		CvTerm cvt = this.cvDao.getCvTermByNameAndCvName(cvTermName, "genedb_misc");
		for (String file : bFile) {
			file = file.replaceAll(" ", ""); // FIXME - Why - looks dubious
			FeatureProp fp = new FeatureProp(polypeptide,cvt,file,rank);
			this.sequenceDao.persist(fp);
			rank++;
		}
	}
	
	private void processArtemisFile(Feature polypeptide, Annotation an) {
    	processIndividualArtemisFile(polypeptide, an, "blast_file");
    	processIndividualArtemisFile(polypeptide, an, "blastn_file");
    	processIndividualArtemisFile(polypeptide, an, "blastp+go_file", "blastpgo_file");
    	processIndividualArtemisFile(polypeptide, an, "blastp_file");
    	processIndividualArtemisFile(polypeptide, an, "blastx_file");
    	processIndividualArtemisFile(polypeptide, an, "fasta_file");
    	processIndividualArtemisFile(polypeptide, an, "tblastn_file");
    	processIndividualArtemisFile(polypeptide, an, "tblastx_file");
	}


	/**
     * @param polypeptide
     * @param an
     */
    private void createSimilarity(Feature polypeptide, Feature transcript, Annotation an) {

        String cv = "SI_genedb_similarity";
        List<SimilarityInstance> similarities = this.siParser.getAllSimilarityInstance(an);
        int count = 0;
        if (similarities.size() > 0)  {
            for (SimilarityInstance si : similarities) {

                count++;

                Feature queryFeature = null;
                String cvTerm = null;

                if (si.getAlgorithm().equals("fasta")) {
                    queryFeature = polypeptide;
                    cvTerm = "protein_match";
                } else {
                    queryFeature = transcript;
                    cvTerm = "nucleotide_match";
                }
                /* look for analysis and create new if one does not already exsists */

                Analysis analysis = null;
                analysis = this.generalDao.getAnalysisByProgram(si.getAlgorithm());
                if (analysis == null){
                    analysis = new Analysis();
                    analysis.setAlgorithm(si.getAlgorithm());
                    analysis.setProgram(si.getAlgorithm());
                    analysis.setProgramVersion("1.0");
                    analysis.setSourceName(si.getAlgorithm());
                    Date epoch = new Date(0);
                    analysis.setTimeExecuted(epoch);
                    this.generalDao.persist(analysis);
                }

                /* create match feature 
                 * create new dbxref for match feature if one does not already exsists 
                 */ 

                Feature matchFeature = null;
                String uniqueName = null;

                uniqueName = "MATCH_" + queryFeature.getUniqueName() + "_" + count;

                matchFeature = this.featureUtils.createFeature(cvTerm, uniqueName, organism);
                this.sequenceDao.persist(matchFeature);

                CvTerm uId = this.cvDao.getCvTermByNameAndCvName("ungapped id",cv );
                FeatureProp ungappedId = new FeatureProp(matchFeature,uId,si.getUngappedId(),0);
                this.sequenceDao.persist(ungappedId);

                CvTerm olap = this.cvDao.getCvTermByNameAndCvName("overlap", cv);
                FeatureProp overlap = new FeatureProp(matchFeature,olap,si.getOverlap(),0);
                this.sequenceDao.persist(overlap);

                /* create analysisfeature 
                 * 
                 */
                Double score = null;
                if (si.getScore() != null) {
                    score = Double.parseDouble(si.getScore());
                } 

                Double evalue = null;
                if (si.getEvalue() != null) {
                    evalue = Double.parseDouble(si.getEvalue());
                } 

                Double id = null;
                if (si.getId() != null) {
                    id = Double.parseDouble(si.getId());
                } 

                AnalysisFeature analysisFeature = new AnalysisFeature(analysis,matchFeature,0.0,score,evalue,id);
                this.generalDao.persist(analysisFeature);

                /* create subject feature if one does not already exists. If two database are 
                 * referenced; seperate the primary and the secondary. Create feature.dbxref 
                 * for primary and featuredbxref for secondary. Also add organism, product, gene, 
                 * overlap and ungappedid as featureprop to this feature. Create featureloc from 
                 * subject XX-XXX aa and link it to matchFeature. set the rank of src_feature_id 
                 * of featureloc to 0. 
                 */
                Feature subjectFeature = null;
                String sections[] = si.getPriDatabase().split(":");
                String values[] = si.getSecDatabase().split(":");
                if(sections[0].equals("SWALL") && sections[1].contains("_")) {
                    subjectFeature = this.sequenceDao.getFeatureByUniqueName("UniProt:"+values[1],"similarity_region");
                } else if(sections[0].equals("SWALL")){
                    subjectFeature = this.sequenceDao.getFeatureByUniqueName("UniProt:"+sections[1],"similarity_region");
                } else {
                    subjectFeature = this.sequenceDao.getFeatureByUniqueName(si.getPriDatabase(),"similarity_region");
                }
                if (subjectFeature == null) {
                    subjectFeature = this.sequenceDao.getFeatureByUniqueName(si.getSecDatabase(),"similarity_region");
                }

                if (subjectFeature == null) {

                    /* hmm...looks like encountered this for the first time so create
                     * subject feature
                     */

                    DbXRef dbXRef = null;



                    String priDatabase = sections[0];
                    String priId = sections[1];

                    String secDatabase = values[0];
                    String secId = values[1];

                    String accession = null;
                    uniqueName = null;

                    Db db = null;
                    if (priDatabase.equals("SWALL")){
                        db = this.generalDao.getDbByName("UniProt");
                    } else {
                        db = this.generalDao.getDbByName(priDatabase);
                    }

                    if (priDatabase.equals(secDatabase)) {
                        if (priId.contains("_")) {
                            accession = secId;
                        } else {
                            accession = priId;
                        }
                        if (priDatabase.equals("SWALL")) {
                            priDatabase = "UniProt";
                        }
                        uniqueName = priDatabase + ":" + accession;
                        subjectFeature = this.featureUtils.createFeature("similarity_region", uniqueName, organism);

                        dbXRef = this.generalDao.getDbXRefByDbAndAcc(db, accession);
                        if (dbXRef == null) {
                            dbXRef = new DbXRef(db,accession);
                            this.generalDao.persist(dbXRef);
                        }
                        subjectFeature.setDbXRef(dbXRef);
                        subjectFeature.setSeqLen(Integer.parseInt(si.getLength()));
                        this.sequenceDao.persist(subjectFeature);
                    }  else {
                        if (priDatabase.equals("SWALL")) {
                            priDatabase = "UniProt";
                        }
                        subjectFeature = this.featureUtils.createFeature("similarity_region", priDatabase + ":" + sections[1], organism);

                        dbXRef = this.generalDao.getDbXRefByDbAndAcc(db, priId);
                        if (dbXRef == null) {
                            dbXRef = new DbXRef(db,priId);
                            this.generalDao.persist(dbXRef);
                        }
                        subjectFeature.setDbXRef(dbXRef);
                        subjectFeature.setSeqLen(Integer.parseInt(si.getLength()));
                        this.sequenceDao.persist(subjectFeature);

                        DbXRef secDbXRef = null;
                        Db secDb = this.generalDao.getDbByName(secDatabase);
                        secDbXRef = this.generalDao.getDbXRefByDbAndAcc(secDb, secId);
                        if (secDbXRef == null) {
                            secDbXRef = new DbXRef(secDb,secId);
                            this.generalDao.persist(secDbXRef);
                        }
                        FeatureDbXRef featureDbXRef = new FeatureDbXRef(secDbXRef,subjectFeature,true);
                        this.sequenceDao.persist(featureDbXRef);
                    }

                    /* once the dbxrefs are set create featureprop for gene, organism and product
                     * 
                     */
                    CvTerm org = this.cvDao.getCvTermByNameAndCvName("organism", cv);
                    FeatureProp propOrganism = new FeatureProp(subjectFeature,org,si.getOrganism(),0);
                    this.sequenceDao.persist(propOrganism);

                    CvTerm pro = this.cvDao.getCvTermByNameAndCvName("product", cv);
                    FeatureProp propProduct = new FeatureProp(subjectFeature,pro,si.getProduct(),1);
                    this.sequenceDao.persist(propProduct);

                    CvTerm gene = this.cvDao.getCvTermByNameAndCvName("gene", cv);
                    FeatureProp propGene = new FeatureProp(subjectFeature,gene,si.getGene(),2);
                    this.sequenceDao.persist(propGene);

                }

                /* create featureloc and attach 'em to matchFeature
                 * 
                 */
                short strand = 1;
                String sCoords[] = si.getSubject().split("-");
                FeatureLoc subjectFLoc = this.featureUtils.createLocation(subjectFeature, matchFeature,Integer.parseInt(sCoords[0]) ,Integer.parseInt(sCoords[1]), strand);
                subjectFLoc.setRank(0);
                this.sequenceDao.persist(subjectFLoc);

                String qCoords[] = si.getQuery().split("-");
                FeatureLoc queryFLoc = this.featureUtils.createLocation(queryFeature, matchFeature,Integer.parseInt(qCoords[0]) ,Integer.parseInt(qCoords[1]), strand);
                queryFLoc.setRank(1);
                this.sequenceDao.persist(queryFLoc);

            }
        }
    }

    private void processClass(Feature polypeptide, Annotation an) {
        List<String> classes = MiningUtils.getProperties("class", an);
        if (classes != null){
            for (String rileyClass : classes) {
                // Remove leading zeros from RILEY numbers
                String sections[] = rileyClass.split("\\.");
                StringBuilder sb = new StringBuilder();
                for (String string : sections) {
                    if (string.length() >= 2) {
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
                    if (looksLikePub(sections[0])) {
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
                            CvTerm cvterm = this.cvDao.getCvTermByNameAndCvName("Not Fetched", "genedb_literature");
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
            thingy("unixdate", cc.getDate(), CV_FEATURE_PROPERTY, fct, null);
            thingy("attribution", cc.getAttribution(), controlledCuration, fct, null);
            thingy("evidence", cc.getEvidence(), CV_GENEDB, fct, null);
            thingy("residue", cc.getResidue(), controlledCuration, fct, null);
            thingy("qualifier", cc.getQualifier(), CV_GENEDB, fct, "\\|");


            if (other) {
                other = false;
                for (String dbXRef2 : list) {
                    String sections[] = dbXRef2.split(":");
                    if (sections.length != 2) {
                        logger.error("Unable to parse a dbxref from '"+dbXRef2+"'");
                    } else {
                        if(!looksLikePub(sections[0])) {
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

            Pub pub = pubDao.getPubByUniqueName("null");
            String ref = go.getRef();
            // Reference
            Pub refPub = pub;
            if (ref != null && looksLikePub(ref)) {
                // The reference is a pubmed id - usual case
                refPub = findOrCreatePubFromPMID(ref);
                //FeatureCvTermPub fctp = new FeatureCvTermPub(refPub, fct);
                //sequenceDao.persist(fctp);
            }


//          logger.warn("pub is '"+pub+"'");

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
//          Pub refPub = null;
//          if (ref != null && ref.startsWith("PMID:")) {
//          // The reference is a pubmed id - usual case
//          refPub = findOrCreatePubFromPMID(ref);
//          FeatureCvTermPub fctp = new FeatureCvTermPub(refPub, fct);
//          sequenceDao.persist(fctp);
//          }

            // Evidence
            FeatureCvTermProp fctp = new FeatureCvTermProp(GO_KEY_EVIDENCE , fct, go.getEvidence().getDescription(), 0);
            sequenceDao.persist(fctp);

            // Qualifiers
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
        logger.warn("Returning pub='"+pub+"'");
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

    private void createProducts(Feature f, Annotation an, String annotationKey, Cv cv) {
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
                    cvTerm = new CvTerm(cv, dbXRef, product, product);
                    this.cvDao.persist(cvTerm);
                } else {
                    cvTerm = cvTermList.get(0);
                }
                Pub pub = DUMMY_PUB;
                FeatureCvTerm fct = new FeatureCvTerm(cvTerm, f, pub, not, 0);
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

    public void setCcParser(ControlledCurationParser ccParser) {
        this.ccParser = ccParser;
    }

    public void setSimilarityParser(SimilarityParser siParser) {
        this.siParser = siParser;
    }

}
