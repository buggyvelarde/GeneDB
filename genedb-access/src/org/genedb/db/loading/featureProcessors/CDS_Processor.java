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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.genedb.db.loading.FeatureProcessor;
import org.genedb.db.loading.GoInstance;
import org.genedb.db.loading.GoParser;
import org.genedb.db.loading.MiningUtils;
import org.genedb.db.loading.Names;
import org.genedb.db.loading.NomenclatureHandler;
import org.genedb.db.loading.ParsedString;
import org.genedb.db.loading.ProcessingPhase;
import org.genedb.db.loading.SimilarityInstance;
import org.genedb.db.loading.SimilarityParser;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.MRNA;
import org.gmod.schema.mapped.Analysis;
import org.gmod.schema.mapped.AnalysisFeature;
import org.gmod.schema.mapped.Cv;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Db;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureDbXRef;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.FeaturePub;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Pub;
import org.gmod.schema.utils.StrandedLocation;
import org.gmod.schema.utils.LocationUtils;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

    private SimilarityParser siParser;

    //private int count;

	public CDS_Processor() {
		handledQualifiers = new String[]{"CDS:EC_number", "CDS:primary_name",
				"CDS:systematic_id", "CDS:previous_systematic_id", "CDS:product",
				"CDS:db_xref", //"CDS:similarity",
				"CDS:temporary_systematic_id",
				"CDS:fasta_file", "CDS:blast_file", "CDS:blastn_file", "CDS:colour",
				"CDS:blastpgo_file", "CDS:blastp_file", "CDS:blastx_file",
				"CDS:obsolete_name", "CDS:synonym", "CDS:reserved_name",
				"CDS:fastax_file", "CDS:tblastn_file", "CDS:tblastx_file",
				"CDS:literature", "CDS:curation", "CDS:private", "CDS:clustalx_file",
				"CDS:pseudo", "CDS:psu_db_xref", "CDS:note", "CDS:GO",
				"CDS:controlled_curation", "CDS:sigcleave_file"};
		unknownRileyClass = new ArrayList<String>();
	}


    @Override
    public void processStrandedFeature(final Feature parent, final StrandedFeature cds, final int offset) {
        final Annotation an = cds.getAnnotation();
        processCodingGene(cds, an, parent, offset);
    }

    @SuppressWarnings( { "unchecked" })
    private void processCodingGene(StrandedFeature cds, Annotation an,
            Feature parent, int offset) {

    	String sysId = null;

    	String soTypeGene = "gene";
    	String soTypeTranscript = "mRNA";
    	String soTypeExon = "exon";
    	boolean pseudo = an.containsProperty(QUAL_PSEUDO);
    	if (pseudo) {
        	soTypeGene = "pseudogene";
        	soTypeTranscript = "pseudogenic_transcript";
        	soTypeExon = "pseudogenic_exon";
    	}

        try {

        	org.biojava.bio.symbol.Location loc = cds.getLocation().translate(offset);
            //Annotation an = cds.getAnnotation();

            StrandedLocation location = LocationUtils.make(loc, cds.getStrand());
            Names names = this.nomenclatureHandler.findNames(an);
            String systematicId = names.getSystematicId();
            if (systematicId == null) {
                logger.warn(String.format("Skipping CDS '%s' as no id found", loc));
                return;
            }
            logger.debug("Looking at systematic id '" + sysId+"'");
            // Gene
            Gene gene = Gene.makeHeirachy(parent, location, systematicId, MRNA.class, true);
        	
            //int transcriptNum = 1;

//            List<Feature> features = new ArrayList<Feature>();
//            List<FeatureLoc> featureLocs = new ArrayList<FeatureLoc>();
//            List<FeatureRelationship> featureRelationships = new ArrayList<FeatureRelationship>();

            Cv CV_NAMING = cvDao.getCvByName("genedb_synonym_type");
            CvTerm SYNONYM_RESERVED = cvDao.getCvTermByNameInCv(QUAL_RESERVED, CV_NAMING).get(0);
            CvTerm SYNONYM_SYNONYM = cvDao.getCvTermByNameInCv(QUAL_SYNONYM, CV_NAMING).get(0);
            CvTerm SYNONYM_PRIMARY = cvDao.getCvTermByNameInCv(QUAL_PRIMARY, CV_NAMING).get(0);
            CvTerm SYNONYM_SYS_ID = cvDao.getCvTermByNameInCv(QUAL_SYS_ID, CV_NAMING).get(0);
            CvTerm SYNONYM_TMP_SYS = cvDao.getCvTermByNameInCv(QUAL_TEMP_SYS_ID, CV_NAMING).get(0);
            CvTerm SYNONYM_PROTEIN = cvDao.getCvTermByNameInCv("protein_name", CV_NAMING).get(0);
            this.DUMMY_PUB = pubDao.getPubByUniqueName("null");
            this.featureUtils.setDummyPub(this.DUMMY_PUB);

            Cv CV_PRODUCTS = cvDao.getCvByName("genedb_products");
            //Db DB_PRODUCTS = this.daoFactory.getDbDao().findByName("genedb_products_db");

            // Is this a multiply spliced gene?
            //Feature gene = null;
            boolean altSplicing = false;
            String sharedId = MiningUtils.getProperty("shared_id", an, null);
            if (sharedId != null) {
                // TODO Tidy
                List<Feature> featureList = sequenceDao.getFeaturesByUniqueNamePattern(sharedId);
                if (featureList != null && featureList.size()==1) {
                	//gene = featureList.get(0);
                }
                altSplicing = true;
            }

            if (altSplicing) {
            	throw new NotImplementedException(); // TODO
            }
            
            
            sequenceDao.persist(gene);
            
//            if (gene == null) {
//                if (altSplicing) {
//                    gene = this.featureUtils.createFeature(soTypeGene, sharedId,
//                            this.organism);
//                    sequenceDao.persist(gene);
//                    this.featureUtils.createSynonym(SYNONYM_SYS_ID, sharedId, gene,
//                            true);
//                } else {
//                    gene = this.featureUtils.createFeature(soTypeGene, this.gns
//                            .getGene(sysId), this.organism);
//                    if (names.getPrimary()!= null) {
//                        gene.setName(names.getPrimary());
//                    }
//                    sequenceDao.persist(gene);
//                    storeNames(names, SYNONYM_RESERVED, SYNONYM_SYNONYM,
//                            SYNONYM_PRIMARY, SYNONYM_SYS_ID, SYNONYM_TMP_SYS,
//                            gene);
//                }
//                // features.add(gene);
//                // this.daoFactory.persist(gene);
//
//                FeatureLoc geneFl = this.featureUtils.createLocation(parent, gene,
//                        loc.getMin()-1, loc.getMax(), strand);
//                gene.getFeatureLocsForFeatureId().add(geneFl);
//                sequenceDao.persist(geneFl);
//            } else {
//                if (altSplicing) {
//                    // Gene already exists and it's alternately spliced.
//                    // It may not have the right coords - may need extending
//                    int newMin = loc.getMin()-1;
//                    int newMax = loc.getMax();
//                    Collection<FeatureLoc> locs = gene.getFeatureLocsForFeatureId();
//                    FeatureLoc currentLoc = locs.iterator().next();  // FIXME - Assumes that only 1 feature loc.
//                    int currentMin = currentLoc.getFmin();
//                    int currentMax = currentLoc.getFmax();
//                    if (currentMin > newMin) {
//                        currentLoc.setFmin(newMin);
//                        logger.debug("Would like to change min to '"+newMin+"' from '"+currentMin+"' for '"+sysId+"'");
//                    }
//                    if (currentMax < newMax) {
//                        currentLoc.setFmax(newMax);
//                        logger.debug("Would like to change max to '"+newMax+"' from '"+currentMax+"' for '"+sysId+"'");
//                    }
//                }
//            }

//            String mRnaName = this.gns.getTranscript(sysId, transcriptNum);
//            String baseName = sysId;
//            if (altSplicing) {
//                mRnaName = this.gns.getGene(sysId);
//                baseName = mRnaName;
//            }
//            Feature mRNA = this.featureUtils
//            .createFeature(soTypeTranscript, mRnaName, this.organism);
//            if (!loc.isContiguous()) {
//                mRNA.setResidues(cds.getSymbols().seqString().getBytes());
//            }
//            sequenceDao.persist(mRNA);
//            if (altSplicing) {
//                storeNames(names, SYNONYM_RESERVED, SYNONYM_SYNONYM,
//                        SYNONYM_PRIMARY, SYNONYM_SYS_ID, SYNONYM_TMP_SYS, mRNA);
//            }
//            FeatureLoc mRNAFl = this.featureUtils.createLocation(parent, mRNA, loc
//                    .getMin()-1, loc.getMax(), strand);
//            mRNA.getFeatureLocsForFeatureId().add(mRNAFl);
//            FeatureRelationship mRNAFr = this.featureUtils.createRelationship(mRNA,
//                    gene, REL_PART_OF, 0);
//            // features.add(mRNA);
//            sequenceDao.persist(mRNAFl);
//            sequenceDao.persist(mRNAFr);
//            mRNA.addFeatureRelationshipsForSubjectId(mRNAFr);
//            // Store exons
//            Iterator<Location> it = loc.blockIterator();
//            int exonCount = 0;
//            while (it.hasNext()) {
//                exonCount++;
//                Location l = it.next();
//                Feature exon = this.featureUtils
//                .createFeature(soTypeExon, this.gns.getExon(baseName,
//                        transcriptNum, exonCount), this.organism);
//                FeatureRelationship exonFr = this.featureUtils.createRelationship(
//                        exon, mRNA, REL_PART_OF, exonCount -1);
//                FeatureLoc exonFl = this.featureUtils.createLocation(parent, exon, l
//                        .getMin()-1, l.getMax(), strand);
//
//                sequenceDao.persist(exon);
//                sequenceDao.persist(exonFl);
//                sequenceDao.persist(exonFr);
//                exon.addFeatureLocsForFeatureId(exonFl);
//                exon.addFeatureRelationshipsForSubjectId(exonFr);
//            }
//
//            Feature polypeptide = this.featureUtils
//            .createFeature("polypeptide", this.gns.getPolypeptide(
//                    baseName, transcriptNum), this.organism);
//            // TODO Protein name - derived from gene name in some cases.
//            // TODO where store protein name synonym - on polypeptide or
//            // gene
//            if (names.getProtein() != null) {
//                polypeptide.setName(names.getProtein());
//                this.featureUtils.createSynonym(SYNONYM_PROTEIN, names.getProtein(),
//                        polypeptide, true);
//            }
//            FeatureRelationship pepFr = this.featureUtils.createRelationship(
//                    polypeptide, mRNA, REL_DERIVES_FROM, 0);
//            //features.add(polypeptide);
//            FeatureLoc pepFl = this.featureUtils.createLocation(parent, polypeptide,
//                    loc.getMin()-1, loc.getMax(), strand);
//            // TODO store protein translation
//            // calculatePepstats(polypeptide); // TODO Uncomment once checked if currently working
//            sequenceDao.persist(polypeptide);
//            sequenceDao.persist(pepFl);
//            sequenceDao.persist(pepFr);
//            polypeptide.addFeatureLocsForFeatureId(pepFl);
//            polypeptide.addFeatureRelationshipsForSubjectId(pepFr);
//            createProducts(polypeptide, an, "product", CV_PRODUCTS);
//
//            // Store feature properties based on original annotation
//            sequenceDao.persist(createFeatureProp(polypeptide, an, "colour", "colour", CV_GENEDB));
//            //
//            // Cvterm cvTerm =
//            // daoFactory.getCvTermDao().findByNameInCv("note",
//            // MISC).get(0);
//
//            processQualifiers(polypeptide, an);
//
//            createTranslation(parent, polypeptide, an, loc);
//            //System.err.print(".");
        } catch (RuntimeException exp) {
            System.err.println("\n\nWas looking at '" + sysId + "'");
            throw exp;
        }
    }

    protected void processQualifiers(Feature polypeptide, Annotation an) {
        createFeaturePropsFromNotes(polypeptide, an, QUAL_NOTE, MISC_NOTE, 0);

        processArtemisFiles(polypeptide, an);

        createDbXRefs(polypeptide, an);

        createGoEntries(polypeptide, an);

        createControlledCuration(polypeptide,an,CV_CONTROLLEDCURATION);

        //TODO enable this and code for it in createSimilarity method
        Collection<FeatureRelationship> featureRels = polypeptide.getFeatureRelationshipsForSubjectId();
        Feature mRNA = null;
        for (FeatureRelationship featureRelationship : featureRels) {
			Feature feature = featureRelationship.getObjectFeature();
			if(feature.getType().getName().equals("mRNA")) {
				mRNA = feature;
			}
		}
        //createSimilarity(polypeptide,mRNA,an);

        processClass(polypeptide,an);

        createEC_number(polypeptide,an);

        createLiterature(polypeptide,an);

        createOtherNotes(polypeptide, an, "private", MISC_PRIVATE);
        createOtherNotes(polypeptide, an, "curation", MISC_CURATION);




    }

    private void createGoEntries(Feature polypeptide,Annotation an) {
    	List<GoInstance> gos = goParser.getNewStyleGoTerm(an);
        if (gos.size() == 0) {
            return;
        }

        for (GoInstance go : gos) {
        	featureUtils.createGoEntries(polypeptide, go, null);
        }

	}


//	private String extractFromId(String in) {
//        if (in.contains(":")) {
//            in.substring(in.indexOf(":"));
//        }
//        return in;
//    }

    private void createTranslation(Feature parent, Feature polypeptide, Annotation an, StrandedLocation loc) {
        String nucleic = new String(parent.getResidues(), loc.getMin(), loc.getMax()-loc.getMin()); // TODO Check offsets
        //String protein =null;//= translate(nucleic, an); FIXME
        polypeptide.setResidues(nucleic.getBytes());
	}



	private void createOtherNotes(Feature polypeptide, Annotation an, String key, CvTerm cvTerm) {
    	List<String> notes = MiningUtils.getProperties(key, an);
    	int rank = 0;
    	for (String note : notes) {
    		FeatureProp fp = new FeatureProp(polypeptide, cvTerm, note, rank);
    		this.sequenceDao.persist(fp);
    		rank++;
    	}
	}

	private void createLiterature(Feature polypeptide, Annotation an) {
    	List<String> literatures = MiningUtils.getProperties("literature", an);
    	if(literatures != null) {
    		Set<String> ids = new HashSet<String>();
    		for (String literature : literatures) {
				String sections[] = literature.split(";");
				String id = sections[0];
				if (ids.contains(id)) {
					logger.warn("Ignoring duplicate /literature of '"+id+"'");
				} else {
					ids.add(id);
					Pub pub = this.pubDao.getPubByUniqueName(id);
					if (pub == null) {
						CvTerm cvt = this.cvDao.getCvTermByNameAndCvName("unfetched", "genedb_literature");
						pub = new Pub(id, cvt);
						this.pubDao.persist(pub);
					}
					FeaturePub fp = new FeaturePub(polypeptide,pub);
					this.sequenceDao.persist(fp);
				}
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

	private void processArtemisFiles(Feature polypeptide, Annotation an) {
    	processIndividualArtemisFile(polypeptide, an, "blast_file");
    	processIndividualArtemisFile(polypeptide, an, "blastn_file");
    	processIndividualArtemisFile(polypeptide, an, "blastp+go_file", "blastpgo_file");
    	processIndividualArtemisFile(polypeptide, an, "blastp_file");
    	processIndividualArtemisFile(polypeptide, an, "blastx_file");
    	processIndividualArtemisFile(polypeptide, an, "fasta_file");
    	processIndividualArtemisFile(polypeptide, an, "tblastn_file", "tBlastn_file");
    	processIndividualArtemisFile(polypeptide, an, "tblastx_file", "tBlastx_file");
    	processIndividualArtemisFile(polypeptide, an, "clustalx_file");
    	processIndividualArtemisFile(polypeptide, an, "pepstats_file");
    	processIndividualArtemisFile(polypeptide, an, "sigcleave_file");
	}


//	/**
//     * @param polypeptide
//     * @param an
//     */
//    private void createSimilarity(Feature polypeptide, Feature transcript, Annotation an) {
//
//        String cv = "genedb_misc";
//        List<SimilarityInstance> similarities = this.siParser.getAllSimilarityInstance(an);
//        int count = 0;
//        if (similarities.size() > 0)  {
//            for (SimilarityInstance si : similarities) {
//
//                count++;
//
//                Feature queryFeature = null;
//                String cvTerm = null;
//
//                if (si.getAlgorithm().equals("fasta")) {
//                    queryFeature = polypeptide;
//                    cvTerm = "protein_match";
//                } else {
//                    queryFeature = transcript;
//                    cvTerm = "nucleotide_match";
//                }
//                /* look for analysis and create new if one does not already exists */
//
//                Analysis analysis = null;
//                analysis = this.generalDao.getAnalysisByProgram(si.getAlgorithm());
//                if (analysis == null){
//                    analysis = new Analysis();
//                    analysis.setAlgorithm(si.getAlgorithm());
//                    analysis.setProgram(si.getAlgorithm());
//                    analysis.setProgramVersion("1.0");
//                    analysis.setSourceName(si.getAlgorithm());
//                    Date epoch = new Date(0);
//                    analysis.setTimeExecuted(epoch);
//                    this.generalDao.persist(analysis);
//                }
//
//                /* create match feature
//                 * create new dbxref for match feature if one does not already exsists
//                 */
//
//                Feature matchFeature = null;
//                String uniqueName = null;
//
//                uniqueName = "MATCH_" + queryFeature.getUniqueName() + "_" + count;
//
//                matchFeature = this.featureUtils.createFeature(cvTerm, uniqueName, organism);
//                this.sequenceDao.persist(matchFeature);
//
//                CvTerm uId = this.cvDao.getCvTermByNameAndCvName("ungapped id",cv );
//                FeatureProp ungappedId = new FeatureProp(matchFeature,uId,si.getUngappedId(),0);
//                this.sequenceDao.persist(ungappedId);
//
//                CvTerm olap = this.cvDao.getCvTermByNameAndCvName("overlap", cv);
//                FeatureProp overlap = new FeatureProp(matchFeature,olap,si.getOverlap(),0);
//                this.sequenceDao.persist(overlap);
//
//                /* create analysisfeature
//                 *
//                 */
//                Double score = null;
//                if (si.getScore() != null) {
//                    score = Double.parseDouble(si.getScore());
//                }
//
//                Double evalue = null;
//                if (si.getEvalue() != null) {
//                    evalue = Double.parseDouble(si.getEvalue());
//                }
//
//                Double id = null;
//                if (si.getId() != null) {
//                    id = Double.parseDouble(si.getId());
//                }
//
//                AnalysisFeature analysisFeature = new AnalysisFeature(analysis,matchFeature,0.0,score,evalue,id);
//                this.generalDao.persist(analysisFeature);
//
//                /* create subject feature if one does not already exists. If two database are
//                 * referenced; seperate the primary and the secondary. Create feature.dbxref
//                 * for primary and featuredbxref for secondary. Also add organism, product, gene,
//                 * overlap and ungappedid as featureprop to this feature. Create featureloc from
//                 * subject XX-XXX aa and link it to matchFeature. set the rank of src_feature_id
//                 * of featureloc to 0.
//                 */
//                Feature subjectFeature = null;
//
//                String sections[] = parseDbString(si.getPriDatabase());
//                String values[] = parseDbString(si.getSecDatabase());
//                if (sections[0].equals("SWALL") && sections[1].contains("_")) {
//                   subjectFeature = this.sequenceDao.getFeatureByUniqueName("UniProt:"+values[1],"region");
//                } else if(sections[0].equals("SWALL")){
//                    subjectFeature = this.sequenceDao.getFeatureByUniqueName("UniProt:"+sections[1],"region");
//                } else {
//                    subjectFeature = this.sequenceDao.getFeatureByUniqueName(si.getPriDatabase(),"region");
//                }
//                if (subjectFeature == null) {
//                    subjectFeature = this.sequenceDao.getFeatureByUniqueName(si.getSecDatabase(),"region");
//                }
//
//                if (subjectFeature == null) {
//
//                    /* hmm...looks like encountered this for the first time so create
//                     * subject feature
//                     */
//
//                    DbXRef dbXRef = null;
//
//
//                    String priDatabase = sections[0];
//                    String priId = sections[1];
//
//                    String secDatabase = values[0];
//                   String secId = values[1];
//
//                    String accession = null;
//                    uniqueName = null;
//
//                    Db db = null;
//                    if (priDatabase.equals("SWALL")){
//                        db = this.generalDao.getDbByName("UniProt");
//                    } else {
//                        db = this.generalDao.getDbByName(priDatabase);
//                    }
//
//                    if (priDatabase.equals(secDatabase)) {
//                        if (priId.contains("_")) {
//                            accession = secId;
//                        } else {
//                            accession = priId;
//                        }
//                        if (priDatabase.equals("SWALL")) {
//                            priDatabase = "UniProt";
//                        }
//                        uniqueName = priDatabase + ":" + accession;
//                        subjectFeature = this.featureUtils.createFeature("region", uniqueName, organism);
//
//                        dbXRef = this.generalDao.getDbXRefByDbAndAcc(db, accession);
//                        if (dbXRef == null) {
//                            dbXRef = new DbXRef(db,accession);
//                            this.generalDao.persist(dbXRef);
//                        }
//                        subjectFeature.setDbXRef(dbXRef);
//                        subjectFeature.setSeqLen(Integer.parseInt(si.getLength()));
//                        this.sequenceDao.persist(subjectFeature);
//                    }  else {
//                        if (priDatabase.equals("SWALL")) {
//                            priDatabase = "UniProt";
//                        }
//                        subjectFeature = this.featureUtils.createFeature("region", priDatabase + ":" + sections[1], organism);
//
//                        dbXRef = this.generalDao.getDbXRefByDbAndAcc(db, priId);
//                        if (dbXRef == null) {
//                            dbXRef = new DbXRef(db,priId);
//                            this.generalDao.persist(dbXRef);
//                        }
//                        subjectFeature.setDbXRef(dbXRef);
//                        subjectFeature.setSeqLen(Integer.parseInt(si.getLength()));
//                        this.sequenceDao.persist(subjectFeature);
//
//                        DbXRef secDbXRef = null;
//                        Db secDb = this.generalDao.getDbByName(secDatabase);
//                        secDbXRef = this.generalDao.getDbXRefByDbAndAcc(secDb, secId);
//                        if (secDbXRef == null) {
//                            secDbXRef = new DbXRef(secDb,secId);
//                            this.generalDao.persist(secDbXRef);
//                        }
//                        FeatureDbXRef featureDbXRef = new FeatureDbXRef(secDbXRef,subjectFeature,true);
//                        this.sequenceDao.persist(featureDbXRef);
//                    }
//
//                    /* once the dbxrefs are set create featureprop for gene, organism and product
//                     *
//                     */
//                    CvTerm org = this.cvDao.getCvTermByNameAndCvName("organism", cv);
//                    FeatureProp propOrganism = new FeatureProp(subjectFeature,org,si.getOrganism(),0);
//                    this.sequenceDao.persist(propOrganism);
//
//                    CvTerm pro = this.cvDao.getCvTermByNameAndCvName("product", cv);
//                    FeatureProp propProduct = new FeatureProp(subjectFeature,pro,si.getProduct(),1);
//                    this.sequenceDao.persist(propProduct);
//
//                    CvTerm gene = this.cvDao.getCvTermByNameAndCvName("gene", cv);
//                    FeatureProp propGene = new FeatureProp(subjectFeature,gene,si.getGene(),2);
//                    this.sequenceDao.persist(propGene);
//
//                }
//
//                /* create featureloc and attach 'em to matchFeature
//                 *
//                 */
//                short strand = 1;
//                String sCoords[] = si.getSubject().split("-");
//                FeatureLoc subjectFLoc = this.featureUtils.createLocation(subjectFeature, matchFeature,Integer.parseInt(sCoords[0]) ,Integer.parseInt(sCoords[1]), strand);
//                subjectFLoc.setRank(0);
//                this.sequenceDao.persist(subjectFLoc);
//
//                String qCoords[] = si.getQuery().split("-");
//                FeatureLoc queryFLoc = this.featureUtils.createLocation(queryFeature, matchFeature,Integer.parseInt(qCoords[0]) ,Integer.parseInt(qCoords[1]), strand);
//                queryFLoc.setRank(1);
//                this.sequenceDao.persist(queryFLoc);
//
//            }
//        }
//    }

    private String[] parseDbString(String db) {
    	String[] ret = db.split(":");
    	if (ret.length != 2) {
    		System.err.println("Unable to parse '"+db+"' as a database adding SWALL as db");
    		ret[0] = "SWALL";
    		ret[1] = db;
    	}
    	return ret;
    }

    private void processClass(Feature polypeptide, Annotation an) {
        List<String> classes = MiningUtils.getProperties("class", an);
        for (String rileyClass : classes) {
        	// Remove leading zeros from RILEY numbers
        	if (rileyClass.contains(".")) {
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
        	}
        	Db db = this.generalDao.getDbByName("RILEY");
        	DbXRef dbXRef = this.generalDao.getDbXRefByDbAndAcc(db,rileyClass);
        	if (dbXRef == null) {
        		if (!unknownRileyClass.contains(rileyClass)) {
        			unknownRileyClass.add(rileyClass);
        		}
        	} else {
	        	CvTerm cvTerm = this.cvDao.getCvTermByDbXRef(dbXRef);
	        	Pub pub = DUMMY_PUB;
	        	FeatureCvTerm fct = new FeatureCvTerm(cvTerm,polypeptide,pub,false,0);
	        	this.sequenceDao.persist(fct);
        	}
        }
    }


    public void setGoParser(GoParser goParser) {
        this.goParser = goParser;
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

    private void createProducts(Feature f, Annotation an, String annotationKey, Cv cv) {
        List<String> products = MiningUtils.getProperties(annotationKey, an);
        boolean not = false;
        if (products != null && products.size() != 0 ) {
        	Set<String> unique = new HashSet<String>();
        	unique.addAll(products);
        	if (unique.size() != products.size()) {
        		logger.warn("Removed duplicate products");
        		products.clear();
        		products.addAll(unique);
        	}
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
                f.addFeatureCvTerm(fct);
            }
        }
    }

  private ParsedString parseDbXref(String in, String prefix) {
	  ParsedString ret;
	  String lookFor = "(" + prefix;
	  int index = in.indexOf(lookFor);
	  if (index != -1) {
	  int rbracket = in.indexOf(")", index);
	  String part = in.substring(index + 1, rbracket);
	  in = in.substring(0, index) + in.substring(rbracket + 1);
	  ret = new ParsedString(in, part);
	  } else {
	  ret = new ParsedString(in, null);
	  }
	  return ret;
  }

//  private String translate(String nucleic) {
//		  if (translation != null && translation.length() > 0 ) {
//		  this.setSequence(SequenceType.SEQ_PROTEIN, translation);
//		  return;
//		  }
//
//		  if ( table != null) {
//		  try {
//		  int num = Integer.parseInt(table);
//		  if (GeneticCodes.isValidTransTable(num)) {
//		  setTranslationTableNum(num);
//		  } else {
//		  System.err.println("WARN: Attempted to set unrecognized translation table ("+table+") in "+getId());
//		  }
//		  }
//		  catch (NumberFormatException exp) {
//		  System.err.println("WARN: Attempted to set unrecognized translation table ("+table+") in "+getId());
//		  }
//		  }
//
//		  int cdStartNum = 1;
//		  if (cdStart != null && cdStart.length() != 0) {
//		  cdStartNum = Integer.parseInt(cdStart);
//		  }
//		  if (cdStartNum < 1 || cdStartNum > 3) {
//		  LogUtils.bprintln("WARN: Ignoring unexpected value of codon_start ("
//		  + cdStart + ") in " + getId());
//		  cdStartNum = 1;
//		  }
//		  if (cdStartNum != 1 && !isPartial()) {
//		  LogUtils.bprintln("WARN: Got non '1' value for codon_start ("
//		  + cdStart + ") but no /partial in " + getId());
//		  setPartial(true);
//		  }
//
//		  if (cdStartNum != 1) {
//		  setCodonStart(cdStartNum);
//		  }
//
//		  SeqTrans.SeqTransResult result =
//		  SeqTrans.getInstance().translate(this, getTranslationTableNumber(),
//		  getCodonStart().intValue(), codon, except);
//		  setProteinWarning(result.getWarning());
//		  setSequence(SequenceType.SEQ_PROTEIN, result.getSeq());
//
//  }


    public void setNomenclatureHandler(NomenclatureHandler nomenclatureHandler) {
        this.nomenclatureHandler = nomenclatureHandler;
    }

    @Override
    public ProcessingPhase getProcessingPhase() {
        return ProcessingPhase.FIRST;
    }

    public void setSiParser(SimilarityParser siParser) {
        this.siParser = siParser;
    }

}
