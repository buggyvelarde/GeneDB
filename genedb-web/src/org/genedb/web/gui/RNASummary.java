package org.genedb.web.gui;

import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.gmod.schema.sequence.FeatureLoc;

import java.io.Serializable;


/**
 * Represents a gene or RNA. Stores enough info to
 * represent it on an imagemap ie name, colour, location
 * 
 * @author art
 */
public class RNASummary implements Serializable {

    private int colour;
    private String type;
    private String id;
    private String name;
    private Location location;
    private String description;
    private StrandedFeature.Strand strand;
    private String organism;

    public String getOrganism() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public RNASummary(String id,String name, Location location,String type,StrandedFeature.Strand strand,
    		String organism,String description,int colour) {
        this.id = id;
        this.name= name;
        this.location = location;
        this.type = type;
        this.strand = strand;
        this.organism = organism;
        this.description = description;
        this.colour = colour;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public int getColour() {
        return colour;
    }

    public StrandedFeature.Strand getStrand() {
        return strand;
    }

}


