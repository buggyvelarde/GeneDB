/*
 * Created on Aug 4, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.genedb.db.domain.objects;


import java.util.List;


/**
 * @author art
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Gene {
    
    private List<Transcript> transcripts;
    private String geneFeatureId;
    private String systematicId;
    private String name;
    private List<String> synonyms;
    private String previousSystematicId;
    
    private String organism;
    
    public String getSystematicId() {
    	return systematicId;
    }

	public String getName() {
		return name;
	}

	public String getOrganism() {
		return organism;
	}
    
}
