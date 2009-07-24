/*
 * Copyright (c) 2006 Genome Research Limited.
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

import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.Supercontig;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Region;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Load an AGP file into the database
 *
 */

@Configurable
public class AGPLoader {

    private static final Logger logger = Logger.getLogger(AGPLoader.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private OrganismDao organismDao;

    // Configurable parameters
    private Organism organism;
    private Class<? extends TopLevelFeature> topLevelFeatureClass = Supercontig.class;
    private Class<? extends TopLevelFeature> entryClass = Contig.class;
    public enum OverwriteExisting {YES, NO}
    private OverwriteExisting overwriteExisting = OverwriteExisting.NO;
    private Boolean skipTopLevelLoad = false;
    
    /**
     * Set the organism into which to load data.
     *
     * @param organismCommonName the common name of the organism
     */
    public void setOrganismCommonName(String organismCommonName) {
        this.organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism == null) {
            throw new IllegalArgumentException(String.format("Organism '%s' not found", organismCommonName));
        }
    }

    /**
     * Set the class of top-level feature that this AGP file represents.
     * The default, if this method is not called, is <code>Supercontig</code>.
     *
     * @param topLevelFeatureClass
     */
    public void setTopLevelFeatureClass(Class<? extends TopLevelFeature> topLevelFeatureClass) {
        this.topLevelFeatureClass = topLevelFeatureClass;
    }

    /**
     * Set the class of feature that each entry in this AGP file represents.
     * The default, if this method is not called, is <code>Contig</code>.
     *
     * @param entryClass
     */
    public void setEntryClass(Class<? extends TopLevelFeature> entryClass) {
        this.entryClass = entryClass;
    }

    /**
     * Whether we should overwrite an existing top-level feature if it has
     * the same name as the one specified in this file. The default, if this
     * method is not called, is <code>NO</code>.
     *
     * If overwriteExisting is <code>NO</code>, the file will be skipped on the
     * grounds that it's already loaded. If it's <code>YES</code>, the previously
     * existing top-level feature, and features located on it, will
     * be deleted first.
     *
     * @param overwriteExisting <code>YES</code> if we should overwrite an
     * existing top-level feature, or <code>NO</code> if not.
     */
    public void setOverwriteExisting(OverwriteExisting overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }
    
    /**
     * Whether or not to load the toplevel features from the AGP file.
     * If set to true, the existing toplevel features will be kept.
     * 
     * This is used when only the contigs and gaps need to be loaded, 
     * and there are features on the toplevel features that must not be lost,
     * such as for unarchiving the Tcongolense contigs.
     * However in practice it is unlikely to be useful often.
     * 
     * @param skipTopLevelLoad
     */
    public void setSkipTopLevelLoad(Boolean skipTopLevelLoad) {
        this.skipTopLevelLoad = skipTopLevelLoad;
    }

    /**
     * This method is called once for each AGP file.
     * 
     * Each time a new toplevel feature is encountered it will be created (if necessary).
     * Each row represents an entry feature, usually a contig or a gap, 
     * that must be created as a child of the toplevel feature.
     *
     */
    @Transactional(rollbackFor=DataError.class) // Will also rollback for runtime exceptions, by default
    public void load(AGPFile agpFile) {

        Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);
 
        String prevTopLevelName = null;
        TopLevelFeature topLevelFeature = null;
        for (AGPLine line: agpFile.lines()) {
        	
            String id = line.getTopLevelName();

            if (!id.equals(prevTopLevelName)) { //start of new topLevelFeature
            	topLevelFeature = addTopLevel(id, session);  //create topLevelFeature
            	prevTopLevelName = id;
            }
            
            addEntryFeature(line, topLevelFeature, session);          
        }
    }
    
    
    private TopLevelFeature addTopLevel(String id, Session session) {

    	TopLevelFeature existingTopLevelFeature = (TopLevelFeature) session.createCriteria(Feature.class)
    	.add(Restrictions.eq("organism", organism))
    	.add(Restrictions.eq("uniqueName", id))
    	.uniqueResult();

    	if (skipTopLevelLoad == true) {
       		logger.debug(String.format("Skipping deleting topLevel feature %s", id));
    		return existingTopLevelFeature;
    	}
    	
    	if (existingTopLevelFeature != null) {
    		switch (overwriteExisting) {
    		case YES:
    			existingTopLevelFeature.delete();
    			break;
    		case NO:
    			logger.error(String.format("The organism '%s' already has feature '%s'",
    					organism.getCommonName(), id));
    			return null;
    		}
    	}
    	TopLevelFeature topLevelFeature = null;
    	if (topLevelFeatureClass != null) {
    		topLevelFeature = TopLevelFeature.make(topLevelFeatureClass, id, organism);
    		topLevelFeature.markAsTopLevelFeature();
    		session.persist(topLevelFeature);
    	}
      	
    	return topLevelFeature;
    }
    
    private void addEntryFeature (AGPLine line, TopLevelFeature topLevelFeature, Session session) {
    	
        // Remove existing feature if overwriteExisting = YES
        String uniqueName = line.getEntryName();
        if (line.getEntryType().equals("N")) { // this is a gap
        	uniqueName = line.getTopLevelName().concat(":gap:").concat(Integer.toString(line.getTopLevelStart())).concat("-").concat(Integer.toString(line.getTopLevelEnd()));
        }
    	Feature existingFeature = (Feature) session.createCriteria(Feature.class)
    	.add(Restrictions.eq("organism", organism))
    	.add(Restrictions.eq("uniqueName", uniqueName))
    	.uniqueResult();

    	if (existingFeature != null) {
    		switch (overwriteExisting) {
    		case YES:
    			logger.debug(String.format("Deleting existing feature %s", uniqueName));
    			existingFeature.delete();
    			session.flush();
    			break;
    		case NO:
    			logger.error(String.format("The organism '%s' already has feature '%s'",
    					organism.getCommonName(), uniqueName));
       		}
    	}
    	
        //create entry feature eg contig or gap

        if (line.getEntryType().equals("N")) { // this is a gap

            logger.debug(String.format("Creating gap at %d-%d", line.getTopLevelStart(), line.getTopLevelEnd()));
            Gap gap = topLevelFeature.addGap(line.getTopLevelStart(), line.getTopLevelEnd());
            session.persist(gap);
        }
        else { // non-gap features
        	          	
            logger.debug(String.format("Creating contig %s at %d-%d", line.getEntryName(), line.getTopLevelStart(), line.getTopLevelEnd()));
        	TopLevelFeature entry = TopLevelFeature.make(entryClass, line.getEntryName(), organism);
            topLevelFeature.addLocatedChild(entry, line.getTopLevelStart(), line.getTopLevelEnd());
            session.persist(entry);
        }   
    }
}

/* 
 * Class that represents an AGP file as an arrayList of AGPLines
 */
class AGPFile {
    private static final Logger logger = Logger.getLogger(AGPFile.class);   
    
    private List<AGPLine> lines = new ArrayList<AGPLine>();
    public int lastContigEnd = 1; //use this to calculate the start of the following gap
    
    public AGPFile(BufferedReader reader) throws IOException {

    	String line;
    	int lineNumber = 0;
        while (null != (line = reader.readLine())) { //While not end of file
 
        	lineNumber++;
        	StringBuilder sb = new StringBuilder(line);
            sb.append(line);
            sb.append('\n');
            logger.trace(sb);
            
            AGPLine newLine = new AGPLine(lineNumber,  lastContigEnd, line);
            lines.add(newLine);
            lastContigEnd = newLine.getTopLevelEnd();
        }       
    }

    public Collection<AGPLine> lines() {
        return lines;
    }
}	

/* 
 * Class that represents a line of an AGP file
 */
class AGPLine {
    private static final Logger logger = Logger.getLogger(AGPLine.class);
    
	private String topLevelName, entryName, entryType, topLevelStrand, entryStrand, gapType, gapLinkage;
	private int topLevelStart, topLevelEnd, entryStart, entryEnd, lineNumber, gapLength;

    // The columns we're interested in:
	// All lines:
    private static final int COL_TL_NAME        = 0;
    private static final int COL_TL_START   	= 1;
    private static final int COL_TL_END   		= 2;
    private static final int COL_TYPE         	= 4;
    // Contig lines:
    // T.congo.pschr.1 1       70710   1       D       congo779a09.p1k 1       70709   -1   
    private static final int COL_ENTRY_NAME     = 5;
    private static final int COL_ENTRY_START    = 6;
    private static final int COL_ENTRY_END      = 7;
    private static final int COL_ENTRY_STRAND	= 8;
    //Gap lines:
    // T.congo.pschr.1 70711   75387   2       N       4676    contig  no 
    private static final int COL_GAP_LENGTH = 5;
    private static final int COL_GAP_TYPE = 6;
    private static final int COL_GAP_LINKAGE = 7;
    
    //private int lastContigEnd = 1;
    
    public AGPLine(int lineNumber, int lastContigEnd, String line) {
        this(lineNumber,  lastContigEnd, line.split("\t"));
    }
        
    public AGPLine(int lineNumber,  int lastContigEnd, String[] rowFields) {

    	if (rowFields.length == 9) { //contig line
    		
    		if (rowFields[4].equals("N")) { //gap line
        		this.topLevelName = rowFields[COL_TL_NAME];
        		this.topLevelStart = Integer.parseInt(rowFields[COL_TL_START])-1; //subtract 1 to convert to interbase coordinates
        		this.topLevelEnd = Integer.parseInt(rowFields[COL_TL_END]);
        		this.entryType = rowFields[COL_TYPE];  
        		this.gapLength = Integer.parseInt(rowFields[COL_GAP_LENGTH]);
        		this.gapType = rowFields[COL_ENTRY_END];
        		this.gapLinkage = rowFields[COL_GAP_LINKAGE];
        		this.lineNumber = lineNumber;

        		this.entryStart = lastContigEnd;
        		this.entryEnd   = lastContigEnd + this.gapLength;

    		}
    		else { //contig line
    			this.topLevelName = rowFields[COL_TL_NAME];
    			this.topLevelStart = Integer.parseInt(rowFields[COL_TL_START])-1;//subtract 1 to convert to interbase coordinates
    			this.topLevelEnd = Integer.parseInt(rowFields[COL_TL_END]);
    			this.entryType = rowFields[COL_TYPE];  		
    			this.entryName = rowFields[COL_ENTRY_NAME];   		
    			this.entryStart = Integer.parseInt(rowFields[COL_ENTRY_START])-1;
    			this.entryEnd = Integer.parseInt(rowFields[COL_ENTRY_END]);
    			this.entryStrand = rowFields[COL_ENTRY_STRAND];
    			this.lineNumber = lineNumber;
    			lastContigEnd = topLevelEnd;
    		}
    	}
    	else {
			logger.error(String.format("Don't know how to parse line %d of AGP file with %d columns", lineNumber, rowFields.length));
    	}

    }

	public String getTopLevelName() {
		return topLevelName;
	}
	public String getEntryName() {
		return entryName;
	}
	public String getTopLevelStrand() {
		return topLevelStrand;
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
	public int getEntryStart() {
		return entryStart;
	}
	public int getEntryEnd() {
		return entryEnd;
	}
	public String getEntryType() {
		return entryType;
	}
	public String getGapType() {
		return gapType;
	}	
	public int getGapLength() {
		return gapLength;
	}
	public String getGapLinkage() {
		return gapLinkage;
	}
	
}

