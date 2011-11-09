/*
 * Copyright (c) 2009 Genome Research Limited.
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

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.util.SequenceUtils;

import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.Supercontig;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.gmod.schema.mapped.FeatureProp;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Loads an AGP file into the database. This loader can be called from the commandline. Two examples below:
 * 
 * ant load-agp -Dconfig=localcopy -Dorganism=Tcongolense -Dload.mode=2 -Dload.topLevel=chromosome -Dload.createMissingContigs=yes -Dfile=Tcongolense.agp
 * ant load-agp -Dconfig=localcopy -Dorganism=Tcruzi -Dload.mode=1 -Dload.topLevel=chromosome -Dload.createMissingContigs=no -Dfile=Tcruzi_20082009.agp
 * 
 *It can be run in two modes (specified by the -Dload.mode option). The default is 1.
 * 
 * <p>Mode 1 <default>: Loads in a 'new' assembly. This deletes any existing toplevel features with the same names (for this organism) and builds news ones 
 * using the information in the AGP file. It maps any existing features that were mapped on the child features (e.g. contigs) to the newly created toplevel features. 
 * Any reversed contigs are dealt with when creating the top level sequence and contigs that are not used in the new assembly are placed back in the bin (if there is one). 
 * One would usually expect to find all the contigs used in the assembly in the database already in this mode. If the createMissingContigs option is set to yes, however, 
 * any missing contigs are created IF there is any information about this contig hidden away as an archived feature in the bin.
 * </p>
 *
 * <p>Mode 2: Loads all the child features (eg. contigs) and gaps specified in the AGP file (if they do not exist) as new features and maps any existing annotation such as genes, 
 * transcripts and exons on the toplevel features to the newly added contigs. This is useful in some organisms where the toplevel features exist 
 * but the contigs have not been loaded in properly. If the createMissingContig option is set to yes, the loader will create new contigs (the sequence for these contigs can
 * be obtained by the relevant bit of sequence in the toplevel feature)
 * </p>
 * 
 * More documentation: 
 * 
 * @author nds@sanger.ac.uk
 * @contact path-help@sanger.ac.uk
 * 
 */


public class AGPLoader {

    private static final Logger logger = Logger.getLogger(AGPLoader.class);
    
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private OrganismDao organismDao;
    @Autowired
    private SequenceDao sequenceDao;

    /* Configurable parameters */
    private Organism organism;
    private Class<? extends TopLevelFeature> topLevelFeatureClass = Supercontig.class; //Supercontig (default) or chromosome
    private Class<? extends TopLevelFeature> childLevelFeatureClass = Contig.class;    //Contig (default) or supercontig
                                                                                       //Throughout this code, the child features will be reffered to as contigs even
                                                                                       //though they may be of another type
    private String mode ="1"; 
    private String createMissingContigs = "no"; //Default no.
    private String putUnusedContigsInBin = "no"; //Default
    private Session session;
    private NavigableMap<Integer,Feature> contigsByStart = new TreeMap<Integer,Feature>(); //The contigs in a particular chromosome, indexed by start
    private TopLevelFeature bin = null; //Bin chromosome
    private List<Feature> contigsAlreadyOnTLF = new ArrayList<Feature>(); //Contigs that were originally mapped onto the toplevel feature
  
   
    
    /***************************************************************************************************************************
     * LOADING CONTIGS,GAPS AND CHROMOSOMES
     ***************************************************************************************************************************/ 

    /**
     * Reads the AGP file and calls the appropriate method for each line in the file. Whenever a new chromosome is encountered, 
     * the lines following that are collected in a list until the next chromosome is encountered or its the end of the file. 
     * Then all the lines are processed in a 'batch'. This helps extract the chromosome 'length' (by reading the fmax value of 
     * the last contig in the chromosome) which is needed, in particular, to re-map any reversed contigs! We also take this 
     * opportunity to accumulate the residues of the contigs and gaps, so that the residue field can be set correctly for the 
     * newly built chromosomes. In mode 1, any contigs that are not used in the new assembly are put back in the bin (if there
     * is one).
     */
    @Transactional(rollbackFor=DataError.class) 
    public void load(AGPFile agpFile) {
        
        //PropertyConfigurator.configure("resources/classpath/log4j.loader.properties"); 
        
        /* Initialising values*/
        this.setSession(SessionFactoryUtils.doGetSession(sessionFactory, false));
        String prevTopLevelName = new String();
        TopLevelFeature topLevelFeature = null;
        List<AGPLine> agpLines = new ArrayList<AGPLine>();
        int tlfLength = 0, linesRead=0; /* Total length of topLevel feature and lines read */
        
        for (AGPLine line : agpFile.lines() ) {
            
            linesRead++;                             
            String currentTopLevelName = line.getTopLevelName();
         
            if ((!currentTopLevelName.equals(prevTopLevelName) && !prevTopLevelName.isEmpty()) || agpFile.isLastLine(linesRead)){ //Start of a new topLevelFeature or end of file
                if(agpFile.isLastLine(linesRead)){
                    if(!currentTopLevelName.equals(prevTopLevelName) && !prevTopLevelName.isEmpty() ){
                        /* If you are here, it means that the last line in the file is of a different
                         * top level feature (tlf). In this case, process the previous tlf and then process
                         * this one.
                         */
                        topLevelFeature = processTopLevel(prevTopLevelName, tlfLength, agpLines);
                        this.contigsByStart.clear();
                        tlfLength = 0;
                        agpLines.clear();
                       
                    }
                    agpLines.add(line);
                    tlfLength = line.getTopLevelEnd();
                    topLevelFeature = processTopLevel(currentTopLevelName, tlfLength, agpLines);                     
                    
                }else{                   
                    topLevelFeature = processTopLevel(prevTopLevelName, tlfLength, agpLines); 
                }
                                       
                /* Re-set values for next toplevelfeature */
                this.contigsByStart.clear();
                tlfLength = 0;
                agpLines.clear();                              
                session.flush(); //Or else this becomes very very slow 
                session.clear(); 
            }
            
         agpLines.add(line);         
         tlfLength = line.getTopLevelEnd();  
         prevTopLevelName = currentTopLevelName;
        }
    }
    
    
   /**
    * Process the top-level feature (tlf) according to the selected mode. In mode 1, if the top-level feature exists, then delete it
    * and create a new one. In mode 2, return the top-level feature that has the specified name.
    */
    private TopLevelFeature processTopLevel(String topLevelName, int tlfLength, List<AGPLine> agpLines) {

        TopLevelFeature existingTopLevelFeature = sequenceDao.getFeatureByUniqueNameAndOrganismCommonName(topLevelName, organism.getCommonName() , TopLevelFeature.class);
    	TopLevelFeature newTopLevelFeature;
    	if(mode.equals("1")){
    	    if (existingTopLevelFeature != null) {
    	        this.setContigsAlreadyOnTLF(this.getContigsOnThis(existingTopLevelFeature));
    	        sequenceDao.deleteFeatureLocsOn(existingTopLevelFeature); //Delete any featurelocs on this tlf
    	        existingTopLevelFeature.delete(false); //Delete the toplevel feature but not the features located on this tlf (hence, argument is false)!
    	        session.flush();
    	    }
    	
    	    newTopLevelFeature = TopLevelFeature.make(topLevelFeatureClass, topLevelName, organism);
    	    newTopLevelFeature.markAsTopLevelFeature();
    	    newTopLevelFeature.setSeqLen(tlfLength);
    	    session.persist(newTopLevelFeature);
    	    logger.info(String.format("Created new top level feature %s with ID=%d and length %d", topLevelName, newTopLevelFeature.getFeatureId(), newTopLevelFeature.getSeqLen()));

    	    
    	}else{ //mode 2
 
    	    if (existingTopLevelFeature == null) {
    	        throw new RuntimeException(String.format("Looking for top level feature '%s' which does not exist!", topLevelName));
    	    }
    	    this.setContigsAlreadyOnTLF(this.getContigsOnThis(existingTopLevelFeature));
    	    newTopLevelFeature = existingTopLevelFeature;
    	}
    	
    	
        /* Deal with child features and gaps for this toplevel feature*/
        String residue="";
        for(AGPLine entry: agpLines){
            Feature entryFeature;
            
            if(!entry.getEntryType().equals("N") && !entry.getEntryType().equals("U")){ //Contig/child
                entryFeature = processContigFeature(entry, newTopLevelFeature);  
                contigsByStart.put(new Integer(entryFeature.getStart()), entryFeature);
                residue = residue.concat(entryFeature.getResidues());
            }else{ //Gap
                entryFeature = processGapFeature(entry, newTopLevelFeature);  
                residue = residue.concat(entryFeature.getResidues());
                /*Reset the residue value for the gaps as they dont seem to have residues in the database. 
                 *Doesn't make sense to store them anyway. */                           
                int seqlen = entryFeature.getSeqLen();
                entryFeature.setResidues("");
                entryFeature.setSeqLen(seqlen);
            }

        }
        
        /* Remap features from topLevelFeature (tlf) to contigs (mode 2) or vice-versa (mode 1) */
        if(mode.equals("2")){
            remapFeaturesFromTLFToContigs(newTopLevelFeature); 
        }else if (mode.equals("1")){      
            newTopLevelFeature.setResidues(residue); 
            remapFeaturesFromContigsToTLF(newTopLevelFeature);
            if(putUnusedContigsInBin.equalsIgnoreCase("yes")){
                this.putUnusedContigsInBin(getContigsAlreadyOnTLF(), contigsByStart.values());   
            }
        } 
        logger.info(String.format("Processed %d child features for toplevelfeature %s", contigsByStart.size(), newTopLevelFeature.getUniqueName()));
       
        /* Doing some checks on the toplevel feature */
        if(newTopLevelFeature.getSeqLen()!=residue.length()){
              logger.warn(String.format("The length of tlf %s (%d) does not add up to the length of features' residues (%d)!", newTopLevelFeature.getUniqueName(), newTopLevelFeature.getSeqLen(), residue.length()));
          }
        if(newTopLevelFeature.getResidues()!=null && !newTopLevelFeature.getResidues().equalsIgnoreCase(residue)){
              logger.warn(String.format("The residue of top level feature %s does not appear to be correct. Check. ", newTopLevelFeature.getUniqueName()));
          } 
          

    	return newTopLevelFeature;   	
    }
    
    
    
    /**
     * Process gap features. In both modes, if a gap does not exist, create a new one.
     * @param line
     * @param topLevelFeature
     */
    private Feature processGapFeature(AGPLine line, TopLevelFeature topLevelFeature){
        
        String uniqueName = line.getTopLevelName().concat(":gap:").concat(Integer.toString(line.getTopLevelStart())).concat("-").concat(Integer.toString(line.getTopLevelEnd()));
        Feature gap = sequenceDao.getFeatureByUniqueNameAndOrganismCommonName(uniqueName, organism.getCommonName() , Feature.class);
        
        if(gap==null){ //Gap does not exist, so create a new one      
            gap = topLevelFeature.addGap(line.getTopLevelStart(), line.getTopLevelEnd());
        }else{
            FeatureLoc floc = gap.getFeatureLoc(0, 0); //Featureloc gap onto the toplevelfeature
            if(floc==null || floc.getSourceFeature()==null || floc.getSourceFeature().getFeatureId()!=topLevelFeature.getFeatureId()){
                topLevelFeature.addLocatedChild(gap, line.getTopLevelStart(), line.getTopLevelEnd(), gap.getStrand(), new Integer(0), 0, 0);   
            }
        }
        
        /*Dealing with seqLength and residues of the gap. Setting the residues in the gap is useful to 'reconstruct' the residues of the chromosome */ 
        int seqlen = line.getTopLevelEnd()-line.getTopLevelStart();
        gap.setSeqLen(seqlen);
        String residues = new String();
        for(int i=0; i < seqlen; i++){
            residues = residues.concat("n");
        }
        gap.setResidues(residues);
        session.persist(gap);
        session.flush();
        logger.info(String.format("Gap %s at %d-%d", gap.getUniqueName(),line.getTopLevelStart(), line.getTopLevelEnd()));
        return gap;
    }
        
    /**
     * Process contigs according to the selected mode. If a contig with the specified name can be found in the database, we just make
     * sure it is featureloc'd correctly to the toplevelfeature. If it cannot be found, a new contig is created if the 'createMissingContig' option is 
     * set to yes.By parsing the name and strand in the AGP file, we can also find out if this contig needs to be reversed. 
     * In this case, we make use of the helper method reverseContig().
     * 
     * @param line
     * @param topLevelFeature
     */
    private Feature processContigFeature(AGPLine line, TopLevelFeature topLevelFeature) {
        
        boolean reverse_contig = false; 
     
        String uniqueName = line.getEntryName(); //Uniquename of contig
        
        if(line.getEntryStrand().equals("-")){
            uniqueName = reverseName(uniqueName);
            reverse_contig = true;
        }
        
        Feature contig = sequenceDao.getFeatureByUniqueNameAndOrganismCommonName(uniqueName, organism.getCommonName(), Feature.class);
        
        if(contig!=null){

            if(reverse_contig){
                contig = reverseContig((Contig)contig);
            }
            FeatureLoc floc = contig.getFeatureLoc(0, 0);
            if(floc==null || floc.getSourceFeature()==null || floc.getSourceFeature().getFeatureId()!=topLevelFeature.getFeatureId()){ //This test works for Tcruzi & Congolense. But need to may be 'set' properties instead of adding a new featureloc
                topLevelFeature.addLocatedChild(contig, line.getTopLevelStart(), line.getTopLevelEnd(), new Integer(0) /*strand*/, new Integer(0), 0, 0);
                String toplevel = contig.getFeatureProp("genedb_misc", "top_level_seq");
                if(toplevel!=null && toplevel.equals("true")){
                    contig.removeFeatureProp("genedb_misc", "top_level_seq"); //Contig no longer top level
                }
                logger.info("Found contig " + contig.getUniqueName());
            }else{
                logger.warn(String.format("Contig %s exists but featureloc (locgroup=0, rank=0) is not null or not as expected. Check.", uniqueName ));
            }
            
        }else if(contig==null){

            String residue = new String();
            
            if(mode.equals("2")){
      
                if(createMissingContigs.equalsIgnoreCase("yes")){
                    contig = TopLevelFeature.make(childLevelFeatureClass, uniqueName, organism);
                    topLevelFeature.addLocatedChild(contig, line.getTopLevelStart(), line.getTopLevelEnd(), new Integer(0) /*strand*/, new Integer(0), 0, 0); 
                    residue = topLevelFeature.getResidues(line.getTopLevelStart(), line.getTopLevelEnd());
                    contig.setResidues(residue);
                
                    logger.info(String.format("Created child feature %s (%d). Got residue from chromosome %s (%d-%d)", contig.getUniqueName(), contig.getFeatureId(), topLevelFeature.getUniqueName(),line.getTopLevelStart(), line.getTopLevelEnd()));
                }else{
                    throw new RuntimeException(String.format("Looking for child feature '%s' which does not exist!", uniqueName));
                }
            }else if(mode.equals("1")){
  
                if (line.getEntryStrand().equals("-")){
                    /* If it's a contig to be reversed, perhaps the non-reversed version is in the database? */
                    String rev_uniqueName = reverseName(uniqueName);
                    contig = sequenceDao.getFeatureByUniqueNameAndOrganismCommonName(rev_uniqueName, organism.getCommonName(), Feature.class);
                    if(contig!=null){
                        contig = reverseContig((Contig)contig);
                        
                    /* If not, we can create the non-reversed version (if we are allowed) using information from the bin and reverse it */
                    }else{
                        if(createMissingContigs.equalsIgnoreCase("yes")){
                            uniqueName = reverseName(uniqueName);
                            contig = TopLevelFeature.make(childLevelFeatureClass, uniqueName, organism);
                            FeatureLoc archivedFloc = this.getArchivedContigFeatureloc("%"+uniqueName+"%"); //Gets the feature loc of the contig (if it has previously been archived e.g. in a bin)
                            if(archivedFloc!=null){
                                residue = (archivedFloc.getSourceFeature()).getResidues(archivedFloc.getFmin(), archivedFloc.getFmax());
                                logger.info(String.format("Creating contig %s (%d). Getting residue from  %s (%d-%d)", 
                                                           contig.getUniqueName(), contig.getFeatureId(), archivedFloc.getSourceFeature().getUniqueName(), archivedFloc.getFmin(), archivedFloc.getFmax()));
                            }
                            contig.setResidues(residue);
                            session.persist(contig);
                            session.flush();
                            if(reverse_contig){
                                contig = reverseContig((Contig)contig);
                            }else{
                                //Just rename the contig to _reversed
                                contig.setUniqueName(rev_uniqueName.concat("_reversed"));
                                session.flush();
                            }
                            
                        }else{
                            throw new RuntimeException(String.format("Looking for contig '%s' which does not exist!", uniqueName));
                        }
                    }//End if contig==null and reverse
                    topLevelFeature.addLocatedChild(contig, line.getTopLevelStart(), line.getTopLevelEnd(), new Integer(0) /*strand?*/, new Integer(0), 0, 0); 
                    
                }else{ //On +ve strand
                    if(createMissingContigs.equalsIgnoreCase("yes")){
                        contig = TopLevelFeature.make(childLevelFeatureClass, uniqueName, organism);
                        FeatureLoc archivedFloc = this.getArchivedContigFeatureloc("%"+uniqueName+"%");
                        if(archivedFloc!=null){
                            residue = (archivedFloc.getSourceFeature()).getResidues(archivedFloc.getFmin(), archivedFloc.getFmax());
                            logger.info(String.format("Creating child feature %s (%d). Getting residue from bin %s (%d-%d)", 
                                                       contig.getUniqueName(), contig.getFeatureId(), archivedFloc.getSourceFeature().getUniqueName(), archivedFloc.getFmin(), archivedFloc.getFmax()));
                        }
                        contig.setResidues(residue);
                        
                        topLevelFeature.addLocatedChild(contig, line.getTopLevelStart(), line.getTopLevelEnd(), /*new Integer(line.getEntryStrand()).intValue()*/0, new Integer(0), 0, 0);
                        
                    }else{
                        throw new RuntimeException(String.format("Looking for contig '%s' which does not exist!", uniqueName));
                    }
                }

  
            }
            
        }
        session.persist(contig);
        session.flush();        
        return contig;    
    	
    }
    
    /***************************************************************************************************************************
     * REMAPPING FEATURES
     ***************************************************************************************************************************/    
    /**
     * Remaps existing features on the toplevel feature onto the newly added child feature (mode=2)
     * We use the start/stop positions of the child feature on the tlf and those of the feature to locate it within the contig.
     * If we find that a feature begins in one contig and ends in another, we add two featurelocs to reflect this (both rank 0, 
     * but different locgroups) with fmin/fmax set to partial as relevant.
     */
    private void remapFeaturesFromTLFToContigs(TopLevelFeature tlf){
        logger.info("Starting to remap features from top level feature to child level features...");
        int count = 0;
        
        for(Feature f: this.getAllFeaturesExceptContigsAndGaps(tlf)){
            count++;
            FeatureLoc floc = f.getFeatureLocOnThisSrcFeature(tlf);    //f.getFeatureLoc(0, 0); //Featureloc of feature on the chromosome
            
            // Get the max locgroup for this feature
            int maxLocGroup = 0;
            for(FeatureLoc fl: f.getFeatureLocs()){
                if(fl.getLocGroup() > maxLocGroup){
                    maxLocGroup = fl.getLocGroup();
                }
                
            }
            
            if(floc!=null){ //Sanity check
                Feature contig = contigsByStart.isEmpty() ? null : contigsByStart.floorEntry(floc.getFmin()).getValue(); //The contig that the feature starts on

                if(contig!=null){
                    if (floc.getFmax() > contig.getStop() ) { //Fmax partial
                        Feature fmincontig = contig;
                        Feature fmaxcontig = contigsByStart.isEmpty() ? null : contigsByStart.floorEntry(floc.getFmax()).getValue(); //The contig the feature ends on 

                        if(fmaxcontig!=null && !fmincontig.getUniqueName().equals(fmaxcontig.getUniqueName())){ //More sanity checks

                            if(fmaxcontig.getStrand()!=fmincontig.getStrand()){
                                logger.warn(String.format("Feature %s lies on two contigs (%s, %s) on two strands!", f.getUniqueName(), fmincontig.getUniqueName(), fmaxcontig.getUniqueName()));
                            
                            }else{
                                
                                if(fmaxcontig.getStart()!=fmincontig.getStop()){ //Contigs not adjacent
                                    logger.warn(String.format("Feature %s lies on two contigs (%s, %s) that are not adjacent, so double check the featurelocs!", f.getUniqueName(), fmincontig.getUniqueName(), fmaxcontig.getUniqueName()));
                                }

                                logger.info(String.format("Feature %s(%d-%d) located on %s : (%d-%d) and %s : (%d-%d)",
                                        f.getUniqueName(), floc.getFmin(), floc.getFmax(), 
                                        fmincontig.getUniqueName(), (floc.getFmin()-fmincontig.getStart()), (fmincontig.getStop()-fmincontig.getStart()),
                                        fmaxcontig.getUniqueName(), 0, (floc.getFmax()-fmaxcontig.getStart())));
    
                                //Feature split over two child features
                                if(floc.getFmin() < fmincontig.getStop()){
                                    FeatureLoc fminfloc = fmincontig.addLocatedChild(f, (floc.getFmin()-fmincontig.getStart()), (fmincontig.getStop()-fmincontig.getStart()), floc.getStrand(), floc.getPhase(), maxLocGroup+1, 0);
                                    fminfloc.setFmaxPartial(true);
                                    session.persist(fminfloc);
                                }
                                if(floc.getFmax() < fmaxcontig.getStop()){
                                    FeatureLoc fmaxfloc = fmaxcontig.addLocatedChild(f, 0, (floc.getFmax()-fmaxcontig.getStart()), floc.getStrand(), floc.getPhase(), maxLocGroup+2, 0);                                
                                    fmaxfloc.setFminPartial(true);
                                    session.persist(fmaxfloc);
                                }
                                if(floc.getFmin() > fmincontig.getStop() || floc.getFmax() > fmaxcontig.getStop()){
                                    logger.warn(String.format("Parts of the feature %s fall into the gaps between the child features.", f.getUniqueName()));
                                }
                                
                               
                            }
                        }else{
                            logger.warn(String.format("!!Feature %s fmax partial but no possible contig for fmax", f.getUniqueName()));
                        }
                        
                        
                  

                    }else{ //Nothing is partial

                        FeatureLoc newFloc = contig.addLocatedChild(f, floc.getFmin()-contig.getStart(), floc.getFmax()-contig.getStart(), floc.getStrand(), floc.getPhase(), maxLocGroup+1, 0); 
                        session.persist(newFloc);
                    }

                }else{
                    logger.warn(String.format("Feature %s does NOT appear to lie on a contig!",f.getUniqueName() ));
                }
            }
        }
        logger.info(String.format("Remapped %d features (that were not contigs or gaps) onto contigs from the tlf", count));
    }

    
    /**
     * This method does the opposite of the above. For each contig on this toplevel feature, it maps any features that are on 
     * the contig to the chromosome (mode 1). If the featureloc of the feature on the contig is partial, then it finds the second featureloc
     * corresponding to this situation and calculates the appropriate fmin and fmax values for the chromosome.
     * 
     */
 
    private void remapFeaturesFromContigsToTLF(TopLevelFeature tlf){
        logger.info(String.format("Starting to remap features from contigs to chromosome %s...", tlf.getUniqueName()));
        session.flush();
        for (Feature contig: getContigsOnThis(tlf)){
              
                for(Feature feature: this.getFeaturesOnThis(contig)){      
                    
                    FeatureLoc feature_floc = feature.getFeatureLocOnThisSrcFeature(contig); //This should get the featureloc of this feature on the contig
                    feature_floc.setLocGroup(1); //If the locgroup is zero, set it to one.
                    session.flush();
                    
                    if(feature_floc!=null){ //Sanity check
                        FeatureLoc contig_floc = contig.getFeatureLoc(0,0); //This should get the featureloc of this contig on the top-level feature
                        Feature contig_srcfeature = contig_floc.getSourceFeature(); //This should be the toplevel feature!
 
                        if(!feature_floc.isFmaxPartial() && !feature_floc.isFminPartial()){
 
                            FeatureLoc new_floc = contig_srcfeature.addLocatedChild(feature, feature_floc.getFmin()+contig_floc.getFmin(), feature_floc.getFmax()+contig_floc.getFmin(), feature_floc.getStrand(), feature_floc.getPhase(), 0, 0);
                            session.persist(new_floc);
                            logger.info(String.format("New featureloc for %s is %s", feature.getUniqueName(), new_floc.toString()));
        
                        }else{
                            
                            FeatureLoc second_feature_floc = feature.getFeatureLoc(2, 0); //This should get the featureloc on the second contig
                            FeatureLoc second_contig_floc = second_feature_floc.getSourceFeature().getFeatureLoc(0, 0);
                           
                            if(feature_floc.isFmaxPartial() && 
                               second_feature_floc.isFminPartial() &&
                               contig_srcfeature.getFeatureId()==second_contig_floc.getSourceFeature().getFeatureId()){
 
                                FeatureLoc new_floc = contig_srcfeature.addLocatedChild(feature, 
                                                                                     feature_floc.getFmin()+contig_floc.getFmin(),
                                                                                     second_feature_floc.getFmax()+second_contig_floc.getFmin(),
                                                                                     feature_floc.getStrand(), 
                                                                                     feature_floc.getPhase(),
                                                                                     0,
                                                                                     0);
                                logger.info(String.format("Feature was partially on two contigs (%s, %s). The floc on tlf %s is %s",
                                        second_feature_floc.getSourceFeature().getUniqueName(), contig.getUniqueName(), tlf.getUniqueName(), new_floc.toString() ));
                                session.persist(new_floc);
                            }
                        }
                    }else{
                        logger.error(String.format("Featureloc of %s on %s is null!",feature.getUniqueName(), contig.getUniqueName()));
                    }
                }   
        }
       session.flush();
        
    }
    
   
    /***************************************************************************************************************************
     * HELPER METHODS
     ***************************************************************************************************************************/ 
    
    /**
     * Takes the name of a contig and returns its reversed version
     */
    private String reverseName(String name){
        Pattern REV_PATTERN = Pattern.compile("(\\S+)_reversed");
        Matcher matcher = REV_PATTERN.matcher(name);
        if (matcher.matches()){
            return matcher.group(1);
        }
        return new String(name.concat("_reversed"));   
    }
    
    
    /**
     *  Returns all the features that start on this toplevelfeature (hence, fmin not partial)
     */
    private List<Feature> getFeaturesOnThis(Feature tlf){
        session.refresh(tlf); //Only way to get this to recognise the new featurelocs was to do a refresh (redo this!)
        List<Feature> featuresOnTLF = new ArrayList<Feature>();
        for (FeatureLoc featureLoc: tlf.getFeatureLocsForSrcFeatureId()) {
            if (featureLoc.getRank() != 0) {
                continue;
            }
            Feature feature = featureLoc.getFeature();
            String unwantedArchivedFeatureName = tlf.getUniqueName().concat(":archived:source:1");
            if (!featureLoc.isFminPartial() && !feature.getUniqueName().equals(unwantedArchivedFeatureName)){ 
                featuresOnTLF.add(feature);
            } 
        }
        logger.info(String.format("Found %d features (in total) mapped on  %s", featuresOnTLF.size(), tlf.getUniqueName() ));
        return featuresOnTLF;        
    } 
  
    /**
     * Uses the method above to return just the contigs mapped on the tlf
     */
    private List<Feature> getContigsOnThis(Feature tlf){
        List<Feature> features = new ArrayList<Feature>(); 
        for (Feature feature: this.getFeaturesOnThis(tlf)) {     
            if (feature.getClass()==Contig.class){ 
                features.add(feature);
            } 
        }
        return features;     
    }
    
    /**
     * Another helper method to return everything except contigs and gaps (saves having to do this check in the main methods above)
     */
    private List<Feature> getAllFeaturesExceptContigsAndGaps(Feature tlf){
        List<Feature> features = new ArrayList<Feature>(); 
        for (Feature feature: this.getFeaturesOnThis(tlf)) {     
            if (feature.getClass()!=Contig.class && feature.getClass()!=Gap.class){ 
                features.add(feature);
            } 
        }
        return features;   
    }
    
   /**
    * Gets the featureLoc of an archived contig on the bin. This is particularly helpful with Congolense where all the contigs are archived
    * @param namePattern
    * @return
    */
    private FeatureLoc getArchivedContigFeatureloc(String namePattern){
        FeatureProp featureProp = (FeatureProp)session.createQuery("from FeatureProp where value like :namePattern") 
                                  .setString("namePattern", namePattern)
                                  .uniqueResult();
        
        if(featureProp!=null){
            FeatureLoc featureLoc = (FeatureLoc)session.createQuery("from FeatureLoc where feature_id=:feature_id") 
                                    .setInteger("feature_id", featureProp.getFeature().getFeatureId())
                                    .uniqueResult();
            if(featureLoc!=null){
                return featureLoc;
            }
        }
        logger.warn(String.format("Not found featureloc for %s in bin",namePattern));
        return null;            
    }
    
    /**
     * Given the newly added contigs and the list of original contigs on a chromosome, this methods puts any unused contigs 
     * back in the bin (if there is one)
     * It is assumed at this point that these contigs (that need to be put back in the bin) would have had their locgroup
     * 0 featureloc deleted.
     * 
     * @param originalContigs
     * @param newContigs
     */
    private void putUnusedContigsInBin(List<Feature> originalContigs, Collection<Feature> newContigs){
        
        this.bin = (TopLevelFeature)sequenceDao.getFeatureByUniqueNamePatternAndOrganismCommonName("%bin%", organism.getCommonName(), topLevelFeatureClass);
        if(bin==null){
            logger.error("Trying to put unused contigs back in the bin but there is no bin!");
            return;
        }
        
        originalContigs.removeAll(newContigs); 
        logger.info(String.format("Contigs to put back in the bin are: %s", StringUtils.collectionToCommaDelimitedString(originalContigs)));
        
        for(Feature feature: originalContigs){
            if(bin.getSeqLen()>0 && feature.getSeqLen()>0 && feature.getResidues()!=null && bin.getResidues()!=null){
                bin.addLocatedChild(feature, bin.getSeqLen(), bin.getSeqLen() + feature.getSeqLen(), new Integer(1), new Integer(0), 0, 0);
                bin.setResidues(bin.getResidues().concat(feature.getResidues()));
            }else{
                logger.error(String.format("Not got enough information to relocate contig %s in the bin %s", feature.getUniqueName(), bin.getUniqueName()));
            }
        }
    }
    
    /**
     * This method takes a contig as an argument and returns a new reversed version of it. It makes a new contig with name_reversed, reverse comp 
     * the sequence and maps all the features on it to the right locations in the reversed contig.Currently we only remap features that fall squarely
     * on the contig. Discuss later as to how features that span two contigs need to be dealt with. The method then deletes the old contig as it is not
     * necessary to store the same contig in two different orientations. 
     * 
     * @param contig that should be reversed
     */
  
    private Feature reverseContig(Contig contig){
        logger.info(String.format("**Contig sent into reverse method %s", contig.getUniqueName()));
        session.flush();
        
        String uniquename = reverseName(contig.getUniqueName());
        Feature contig_rev = TopLevelFeature.make(Contig.class, uniquename, organism); //make a new contig
        contig_rev.setResidues(SequenceUtils.reverseComplement((contig.getResidues()).toLowerCase())); //reverse complement the sequence 
        contig_rev.setSeqLen(contig.getSeqLen());
        session.persist(contig_rev);
 
        for(Feature feature: this.getFeaturesOnThis(contig)){
          
             final FeatureLoc feature_floc = feature.getFeatureLocOnThisSrcFeature(contig); 
           
             if(!feature_floc.isFminPartial() && !feature_floc.isFmaxPartial()){
                 //Change the values in this featureloc
                 int fmin = contig_rev.getSeqLen()-feature_floc.getFmax();
                 int fmax = contig_rev.getSeqLen()-feature_floc.getFmin();
                 Short strand = (short) -feature_floc.getStrand(); 
               
                 feature_floc.setFmin(fmin);
                 feature_floc.setFmax(fmax);
                 feature_floc.setStrand(strand);
                 feature_floc.setSourceFeature(contig_rev);
                 feature_floc.setLocGroup(1); 
                 
                 session.saveOrUpdate(feature_floc); //This saveOrUpdate does not seem to get noticed until I force a refresh??
                 session.flush();
                 logger.info(String.format("Feature %s (originally on %s) remapped onto reversed contig %s", feature.getUniqueName(), contig.getUniqueName(), feature_floc.toString())); 
                 
             }else{
                 logger.warn(String.format("Feature %s is partial and cannot be remapped onto reversed contig", feature.getUniqueName()));
             }
            
        }
        session.delete(contig); //Delete old contig as we don't need to store the same contig in two different orientations
        session.flush();
        return contig_rev;
    }
    
    
    /***************************************************************************************************************************
     * SETTER/GETTER METHODS
     ***************************************************************************************************************************/ 
    
    /**
     * Set the organism into which to load data using the common name provided.
     */
    public void setOrganismCommonName(String organismCommonName) {
        this.organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism == null) {
            throw new IllegalArgumentException(String.format("Organism '%s' not found", organismCommonName));
        }
    }

    /** 
     * Set the class of the top-level features that this AGP file represents. With AGP files,
     * it's usually chromosomes but they could also be supercontigs. The default, if this method 
     * is not called, is <code>Chromosome</code>. 
     */
    public void setTopLevelFeatureClass(Class<? extends TopLevelFeature> topLevelFeatureClass) {
        this.topLevelFeatureClass = topLevelFeatureClass;
    }
    
    /** 
     * Set the class of the child-level features that this AGP file represents. With AGP files,
     * it's usually contigs but they could also be supercontigs. The default, if this method 
     * is not called, is <code>contig</code>. 
     */
    public void setChildLevelFeatureClass(Class<? extends TopLevelFeature> childLevelFeatureClass) {
        this.childLevelFeatureClass = childLevelFeatureClass;
    }
    
    /**
     * Sets the mode in which this loader should work. 
     * The two modes are explained above. Default is 1. 
     */
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    /**
     * Sets the createMissingContigs option.
     * @param createMissingContigs
     */
    public void setCreateMissingContigs(String createMissingContigs){
        this.createMissingContigs = createMissingContigs;
    }
    
    /**
     * Sets the hibernate session 
     */
    private void setSession(Session session){
        this.session = session;
    }
    
    /**
     * Gets the value of contigsAlreadyOnTLF
     */
    public List<Feature> getContigsAlreadyOnTLF() {
        return contigsAlreadyOnTLF;
    }

    /**
     * Sets the value of contigsAlreadyOnTLF
     */
    public void setContigsAlreadyOnTLF(List<Feature> contigsAlreadyOnTLF) {
        this.contigsAlreadyOnTLF = contigsAlreadyOnTLF;
    }
    
    
    
  
}


/***************************************************************************************************************************
 * CLASSES TO REPRESENT INFORMATION IN AN AGP FILE 
 ***************************************************************************************************************************/
class AGPFile {
    
    private List<AGPLine> lines = new ArrayList<AGPLine>();
    
    public AGPFile(BufferedReader reader) throws IOException {

        String line;
        int lineNumber = 0;
        while (null != (line = reader.readLine())) { //While not end of file      
            lineNumber++;
            AGPLine newLine = new AGPLine(lineNumber, line);
            lines.add(newLine);   
        }       
    }

    public Collection<AGPLine> lines() {
        return lines;
    }
    
    public boolean isLastLine(int lineNum){
        if(lineNum==this.lines().size()){           
            return true;        
        }
        return false;
    }
    
    public boolean hasOnlyOneLine(){
        if (this.lines().size()==1) {return true;}
        return false;
        
    }

}       

/* 
 * Class that represents a line of an AGP file
 */
class AGPLine {
    private static final Logger logger = Logger.getLogger(AGPLine.class);

    private String topLevelName, entryName, entryType, entryStrand, gapType, gapLinkage;
    private int topLevelStart, topLevelEnd, entryStart, entryEnd;

    public AGPLine(int lineNumber, String line){
 
       /*final Pattern CONTIG_PATTERN = Pattern.compile("(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t([ADFGOPW])\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)"); */
       final Pattern CONTIG_PATTERN = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+([ADFGOPW])\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)"); 
       /*final Pattern GAP_PATTERN = Pattern.compile("(\\S+)\\t(\\S+)\\t(\\S+)\\t(\\S+)\\t([UN])\\t(\\S+)\\t(\\S+)\\t(\\S+)");*/
       final Pattern GAP_PATTERN = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+([UN])\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)");

        Matcher matcher_contig = CONTIG_PATTERN.matcher(line);
        Matcher matcher_gap = GAP_PATTERN.matcher(line);

        //It appears that apart from U and N, all other letters in this fifth field
        //can be considered and stored as contigs. 
        //http://www.ncbi.nlm.nih.gov/projects/genome/assembly/agp/AGP_Specification.shtml#FORMAT
        
        if (matcher_contig.matches()){ /*&& (matcher_contig.group(5).equals("D") ||
                                         matcher_contig.group(5).equals("A"))) { //Making sure it's a contig line */
                                           
            this.topLevelName = matcher_contig.group(1);
            this.topLevelStart = Integer.parseInt(matcher_contig.group(2))-1;//subtract 1 to convert to interbase coordinates
            this.topLevelEnd = Integer.parseInt(matcher_contig.group(3)); 
            this.entryType = matcher_contig.group(5);           
            this.entryName = matcher_contig.group(6).trim();   
            this.entryStart = Integer.parseInt(matcher_contig.group(7));
            this.entryEnd = Integer.parseInt(matcher_contig.group(8));
            this.entryStrand = matcher_contig.group(9);
 
        }else if (matcher_gap.matches() ){ //Making sure it's a gap line

            this.topLevelName = matcher_gap.group(1);
            this.topLevelStart = (Integer.parseInt(matcher_gap.group(2)))-1;//subtract 1 to convert to interbase coordinates //imt
            this.topLevelEnd = Integer.parseInt(matcher_gap.group(3)); //int
            this.entryType = matcher_gap.group(5); 
            this.gapType = matcher_gap.group(7);
            this.gapLinkage = matcher_gap.group(8);

        }else{
            logger.error(String.format("Unable to parse line %d: %s", lineNumber, line));
        }

    }

    public String getTopLevelName() {
        return topLevelName;
    }

    public String getEntryName() {
        return entryName;
    }

    public int getTopLevelStart() {
        return topLevelStart;
    }

    public int getTopLevelEnd() {
        return topLevelEnd;
    }

    public String getEntryStrand() {
        return entryStrand;
    }

    public String getEntryType() {
        return entryType;
    }
    
    public int getEntryStart(){
        return entryStart;
    }
    
    public int getEntryEnd(){
        return entryEnd;
    }

    public String getGapType() {
        return gapType;
    }       

    public String getGapLinkage() {
        return gapLinkage;
    }
    
    @Override
    public String toString(){
        return String.format("%s\t%d\t%d\t%s\t%s\n", topLevelName, topLevelStart, topLevelEnd, entryType, entryName);
    }

}





