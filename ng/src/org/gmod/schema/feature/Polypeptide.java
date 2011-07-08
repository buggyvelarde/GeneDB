package org.gmod.schema.feature;


import org.genedb.db.analyzers.AllNamesAnalyzer;
import org.gmod.schema.cfg.FeatureType;
import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureCvTerm;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.FeatureRelationship;
import org.gmod.schema.mapped.Organism;
import org.gmod.schema.utils.PeptideProperties;
import org.gmod.schema.utils.StrandedLocation;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@FeatureType(cv="sequence", term="polypeptide")
@Indexed
public class Polypeptide extends Region {
	
	
    private static Logger logger = Logger.getLogger(Polypeptide.class);
    
	@Transient
    private Transcript transcript;
    

    Polypeptide() {
        // empty
    }

    public Polypeptide(Organism organism, String uniqueName, boolean analysis,
            boolean obsolete, Timestamp dateAccessioned) {
        super(organism, uniqueName, analysis, obsolete, dateAccessioned);
    }

    public Polypeptide(Organism organism, String uniqueName) {
        this(organism, uniqueName, false, false, new Timestamp(System.currentTimeMillis()));
    }
    
    @Transient
    public Transcript getTranscript() {
        if (transcript != null) {
            return transcript;
        }

        for (FeatureRelationship relation : getFeatureRelationshipsForSubjectId()) {
            Feature transcriptFeature = relation.getObjectFeature();
            if (transcriptFeature instanceof Transcript) {
                transcript = (Transcript) transcriptFeature;
                break;
            }
        }
        if (transcript == null) {
            logger.error(String.format("The polypeptide '%s' has no associated transcript", getUniqueName()));
            return null;
        }
        return transcript;
    }
    
    @Transient
    public AbstractGene getGene() {
    	if (transcript == null) {
    		return null;
    	}
    	return transcript.getGene();
    }
    
    @Transient
    @Field(name = "gene", index = Index.UN_TOKENIZED, store = Store.YES)
    public String getGeneUniqueName() {
        AbstractGene gene = getGene();
        if (gene != null) {
        	return gene.getUniqueName();
        }
        return null;
    }
    
    @Transient
    @Field(name = "alternateTranscriptNumber", index = Index.UN_TOKENIZED, store = Store.YES)
    public int alternateTranscriptNumber() {
    	AbstractGene gene = getGene();
        if (gene != null) {
        	return gene.getNonObsoleteTranscripts().size();
        }
        return 0;
    }
    
    @Transient
    @Field(name = "alternateTranscripts", index = Index.UN_TOKENIZED, store = Store.YES)
    public String alternateTranscripts() {
    	AbstractGene gene = getGene();
    	if (gene != null) {
    		return gene.alternateTranscripts();
    	}
    	return null;
    }
    

    @Transient
    public List<String> getProducts() {
        List<String> products = new ArrayList<String>();
        for (FeatureCvTerm featureCvTerm : this.getFeatureCvTerms()) {
            if (featureCvTerm.getType().getCv().getName().equals("genedb_products")) {
                products.add(featureCvTerm.getType().getName());
            }
        }
        return products;
    }

    public void addProduct(String product) {
        addCvTerm("genedb_products", product);
    }

    /**
     * Get the ID number of the colour associated with this polypeptide.
     * It is often unassigned, in which case <code>null</code> is returned.
     *
     * @return
     */
    @Transient
    public Integer getColourId() {

        /* Sometimes there is no colour property at all,
        and sometimes there is a colour property with a null value.

        I don't know why this inconsistency exists. rh11 */

        String colourIdString = getProperty("genedb_misc", "colour");
        if (colourIdString == null || colourIdString.equals("")) {
            return null;
        }
        return Integer.valueOf(colourIdString);
    }

    /**
     * Get all the polypeptide regions of the specified type.
     * @param <T> the type of region. Must be a subclass of <code>PolypeptideRegion</code>
     * @param type a class object representing the region type. For example, <code>PolypeptideDomain.class</code>
     * @return a sorted set of those regions of the requested type
     */
    @Transient
    public <T extends PolypeptideRegion> SortedSet<T> getRegions(Class<T> type) {
        SortedSet<T> domains = new TreeSet<T>();

        for (FeatureLoc domainLoc: this.getFeatureLocsForSrcFeatureId()) {
            Feature domain = domainLoc.getFeature();
            if (type.isAssignableFrom(domain.getClass())) {
                domains.add(type.cast(domain));
            }
        }

        return domains;
    }

    /**
     * Get the (predicted) domains of this polypeptide.
     * @return a sorted set of domains
     */
    public SortedSet<PolypeptideDomain> getDomains() {
        return getRegions(PolypeptideDomain.class);
    }

    /**
     * Get the (predicted) MembraneStructure of this protein.
     * @return the (predicted) MembraneStructure of this protein, or <code>null</code>
     * if there is none.
     */
    @Transient
    public MembraneStructure getMembraneStructure() {
        Set<MembraneStructure> membraneStructures = getRegions(MembraneStructure.class);
        if (membraneStructures.isEmpty()) {
            return null;
        }
        if (membraneStructures.size() > 1) {
            throw new IllegalStateException(String.format("Found more than one MembraneStructure for polypeptide '%s'",
                getUniqueName()));
        }
        return membraneStructures.iterator().next();
    }

    /**
     * Calculate the predicted properties of this polypeptide.
     *
     * @return a <code>PeptideProperties</code> object containing the predicted
     * properties of this polypeptide.
     */
    public PeptideProperties calculateStats() {
        if (this.getResidues() == null) {
            logger.warn("No residues for '" + this.getUniqueName() + "'");
            return null;
        }
        String residuesString = new String(this.getResidues());

        SymbolList residuesSymbolList = null;
        PeptideProperties pp = new PeptideProperties();
        try {
            SymbolTokenization proteinTokenization = ProteinTools.getTAlphabet().getTokenization("token");
            residuesSymbolList = new SimpleSymbolList(proteinTokenization, residuesString);

            if (residuesSymbolList.length() == 0) {
                logger.error(String.format("Polypeptide feature '%s' has zero-length residues", this.getUniqueName()));
                return pp;
            }

             try {
                // if the sequence ends with a termination symbol (*), we need to remove it
                if (residuesSymbolList.symbolAt(residuesSymbolList.length()) == ProteinTools.ter()) {
                    if (residuesSymbolList.length() == 1) {
                        logger.error(String.format("Polypeptide feature '%s' only has termination symbol", this.getUniqueName()));
                        return pp;
                    }
                    residuesSymbolList = residuesSymbolList.subList(1, residuesSymbolList.length() - 1);
                }

             } catch (IndexOutOfBoundsException exception) {
                 throw new RuntimeException(exception);
             }
        } catch (BioException e) {
            logger.error("Can't translate into a protein sequence", e);
            return pp;
        }

        pp.setAminoAcids(residuesSymbolList.length());

        try {
            double isoElectricPoint = new IsoelectricPointCalc().getPI(residuesSymbolList, false, false);
            pp.setIsoelectricPoint(isoElectricPoint);
        } catch (Exception e) {
            logger.error(String.format("Error computing protein isoelectric point for '%s'", residuesSymbolList), e);
        }

        double mass2 = calculateMass(residuesSymbolList);
        if (mass2 != -1) {
            //mass = mass2;
            pp.setMass(mass2);
        }

        double charge = calculateCharge(residuesString);
        pp.setCharge(charge);

        return pp;
    }

    private double calculateMass(SymbolList residuesSymbolList) {
        try {
            double massInDaltons = MassCalc.getMass(residuesSymbolList, SymbolPropertyTable.AVG_MASS, true);
            return massInDaltons;
        } catch (Exception exp) {
            logger.error(String.format("Error computing protein mass in '%s' because '%s'", getUniqueName(), exp.getMessage()));
        }
        return -1.0;
    }


    /**
     * Calculate the charge of a polypeptide.
     *
     * @param residues a string representing the polypeptide residues, using the single-character code
     * @return the charge of this polypeptide (in what units?)
     */
    private double calculateCharge(String residues) {
        double charge = 0.0;
        for (char aminoAcid: residues.toCharArray()) {
            switch (aminoAcid) {
            case 'B': case 'Z': charge += -0.5; break;
            case 'D': case 'E': charge += -1.0; break;
            case 'H':           charge +=  0.5; break;
            case 'K': case 'R': charge +=  1.0; break;
            /*
             * EMBOSS seems to think that 'O' (presumably Pyrrolysine)
             * also contributes +1 to the charge. According to Wikipedia,
             * this obscure amino acid is found only in methanogenic archaea,
             * so it's unlikely to trouble us soon. Still, it can't hurt:
             */
            case 'O':           charge +=  1.0; break;
            }
        }
        return charge;
    }

    public static Polypeptide make(Feature parent, StrandedLocation location,
            String systematicId, Organism organism, Timestamp now) {

        Polypeptide polypeptide = new Polypeptide(organism, systematicId, false, false, now);
        parent.addLocatedChild(polypeptide, location);
        return polypeptide;
    }

    @Transient
    @Field(name="gpiAnchored", index=Index.UN_TOKENIZED, store=Store.NO)
    public boolean isGPIAnchored() {
        return hasProperty("genedb_misc", "GPI_anchored");
    }

    /**
     * Add an orthologue link from the specified polypeptide to this one.
     * @param source the source polypeptide
     * @return the newly-created FeatureRelationship object
     */
    public FeatureRelationship addOrthologue(Polypeptide source) {
        return this.addFeatureRelationship(source, "sequence", "orthologous_to");
    }

    /**
     * Add an paralogue link from the specified polypeptide to this one.
     * @param source the source polypeptide
     * @return the newly-created FeatureRelationship object
     */
    public FeatureRelationship addParalogue(Polypeptide source) {
        return this.addFeatureRelationship(source, "sequence", "paralogous_to");
    }

    @Transient
    @Field(name="signalP", index=Index.UN_TOKENIZED, store=Store.NO)
    public boolean isSignalP() {
        if (hasProperty("genedb_misc", "SignalP_prediction")
        || hasProperty("genedb_misc", "signal_peptide_probability")
        || hasProperty("genedb_misc", "signal_anchor_probability")) {
            return true;
        }
        return false;
    }


    @Transient
    @Field(name="apicoplast", index=Index.UN_TOKENIZED, store=Store.NO)
    public boolean isApicoplast() {
        String s = getProperty("genedb_misc", "PlasmoAP_score");
        int score;
        try {
            score = Integer.parseInt(s);
        }
        catch (RuntimeException exp) {
            return false;
        }
        if (score > 4) {
            return true;
        }
        return false;
    }

    @Transient
    @Field(index=Index.UN_TOKENIZED, store=Store.NO)
    public String getNumberTMDomains() {
        return String.format("%05d", this.getRegions(TransmembraneRegion.class).size());
    }



    @Transient
    @Field(index=Index.TOKENIZED, store=Store.YES)
    public String getSequenceLength(){
        return String.format("%06d",  this.getSeqLen());
    }

    /**
     * FIXED - This method is no longer duplicated (and also in the ProductiveTranscript class)
     * @return
     */
    @Transient
    @Analyzer(impl = AllNamesAnalyzer.class)
    @Field(name = "product", index = Index.TOKENIZED, store = Store.YES)
    public String getProductsAsSpaceSeparatedString() {
        List<String> products = getProducts();
        if (products == null) {
            return null;
        }
        
        return StringUtils.collectionToDelimitedString(products, " ");
    }

    /**
     * FIXED - This method is no longer duplicated (and also in the ProductiveTranscript class)
     * @return
     */
    @Transient
    @Analyzer(impl = AllNamesAnalyzer.class)
    @Field(name = "expandedProduct", index = Index.TOKENIZED, store = Store.YES)
    public String getProductsAsSeparatedString() {
        List<String> products = getProducts();
        if (products == null) {
            return null;
        }
        
        // we only munge in the expandedProduct lucene field, because
        // we are assuming this exists just for display
        List<String> munged = Lists.newArrayList();
        for (String product : products) {
        	if (product.contains("-")) {
        		munged.add(product.replace("-", ""));
        	}
		}
        products.addAll(munged);
        
        return StringUtils.collectionToDelimitedString(products, " ");
    }


    @Transient
    @Field(index=Index.UN_TOKENIZED, store=Store.NO)
    public String getMass() {
        try {
            PeptideProperties pp = calculateStats();
            if (pp.isHasMass()) {
                long mass = Math.round(pp.getMassInDaltons());
                return String.format("%09d", mass);
            }
            return "";
        }
        catch (RuntimeException exp) {
            return "";
        }
    }

    @Transient
    @Field(index=Index.TOKENIZED, store=Store.NO)
    public String getEcNums() {
        List<String> ecNums = new ArrayList<String>();
        for (FeatureProp fp : getFeatureProps()) {
            CvTerm type = fp.getType();
            if (type.getName().equals("EC_number") && type.getCv().getName().equals("genedb_misc")) {
                ecNums.add(fp.getValue());
            }
        }
        return StringUtils.collectionToDelimitedString(ecNums, " ");
    }


    @Transient
    @Field(index=Index.TOKENIZED, store=Store.YES)
    public String getAllCuration() {
        List<String> curation = new ArrayList<String>();
        for (FeatureProp fp : getFeatureProps()) {
            CvTerm type = fp.getType();
            if (type.getCv().getName().equals("genedb_misc") && type.getName().equals("curation")) {
                curation.add(fp.getValue());
            }
            if (type.getCv().getName().equals("feature_property") && type.getName().equals("comment")) {
                curation.add(fp.getValue());
            }
        }
        
        // we add terms from the CC_genedb_controlledcuration featurecvterm here
        curation.addAll(populateFromFeatureCvTerms("CC_genedb_controlledcuration"));
        
        return StringUtils.collectionToDelimitedString(curation, " ");
    }



    @Transient
    @Field(index=Index.TOKENIZED, store=Store.NO)
    public String getGo() {
        List<String> go = new ArrayList<String>();
        go.addAll(populateFromFeatureCvTerms("biological_process"));
        go.addAll(populateFromFeatureCvTerms("molecular_function"));
        go.addAll(populateFromFeatureCvTerms("cellular_component"));
        return StringUtils.collectionToDelimitedString(go, " ");
    }

    private Collection<String> populateFromFeatureCvTerms(String cvNamePrefix) {
        List<String> ret = new ArrayList<String>();
        for (FeatureCvTerm fct : getFeatureCvTermsFilteredByCvNameStartsWith(cvNamePrefix)) {
           ret.add(String.format("%s %s", fct.getCvTerm().getName(), fct.getCvTerm().getDbXRef().getAccession()));
        }
        return ret;
    }

    @Transient
    @Field(index=Index.TOKENIZED, store=Store.NO)
    public String getPfam(){
        StringBuilder ret = new StringBuilder();
        for (PolypeptideDomain domain : this.getDomains()) {
            DbXRef dbXRef = domain.getDbXRef();
            if(dbXRef.getDb().getName().equals("Pfam")){
                ret.append(String.format("%s %s ", dbXRef.getAccession(), dbXRef.getDescription()));
            }
        }
        return ret.length() > 0 ? ret.toString() : null;
    }
    /**
     * Get a collection of the ProteinMatch features that represent similarities
     * between this polypeptide and another (as defined by a /similarity qualifier
     * in a PSU EMBL file).
     *
     * @return a collection of the ProteinMatch features that represent similarities
     * between this polypeptide and another.
     */
    @Transient
    public Collection<ProteinMatch> getSimilarityMatches() {
        List<ProteinMatch> proteinMatches = new ArrayList<ProteinMatch>();
        for (FeatureLoc featureLoc: this.getFeatureLocsForSrcFeatureId()) {
            if (featureLoc.getRank() != 0) {
                continue;
            }
            Feature feature = featureLoc.getFeature();
            if (feature instanceof ProteinMatch) {
                proteinMatches.add((ProteinMatch) feature);
            } else {
                logger.debug(String.format("getSimilarityMatches: %s is '%s', not ProteinMatch",
                    feature, feature.getClass()));
            }
        }
        return proteinMatches;
    }

    /**
     * Get a collection of the ProteinMatch features that represent orthologue clusters
     * to which this polypeptide belongs.
     * @return a collection of the ProteinMatch features that represent orthologue clusters
     * to which this polypeptide belongs
     */
    @Transient
    public Collection<ProteinMatch> getOrthologueClusters() {
        return getRelatedFeatures(ProteinMatch.class, "sequence", "orthologous_to");
    }

    /**
     * Get a collection of all the polypeptides to which this polypeptide
     * @return
     */
    @Transient
    public Collection<Polypeptide> getDirectOrthologues() {
        return getRelatedFeatures(Polypeptide.class, "sequence", "orthologous_to");
    }
}

