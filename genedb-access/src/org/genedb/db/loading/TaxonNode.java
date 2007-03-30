/*
 * Created on Aug 4, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.genedb.db.loading;

import org.gmod.schema.organism.Organism;
import org.gmod.schema.organism.OrganismProp;
import org.gmod.schema.phylogeny.Phylonode;
import org.gmod.schema.phylogeny.PhylonodeOrganism;
import org.gmod.schema.phylogeny.PhylonodeProp;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author art
 *
 */
public class TaxonNode {

    private String taxonId;
    private TaxonNode parent;
    private Phylonode phylonode;
    private List<TaxonNode> children = new ArrayList<TaxonNode>();
    private boolean webLinkable = false;
    private boolean organism = false;
    private Map<String, Map<String, Object>> appDetails = new HashMap<String, Map<String, Object>>(0);
    private Map<TaxonNameType, String> names = new HashMap<TaxonNameType, String>(7);
    

    public TaxonNode(Phylonode phylonode) {
        this.phylonode = phylonode;
        names.put(TaxonNameType.LABEL, phylonode.getLabel());
        names.put(TaxonNameType.FULL, phylonode.getLabel()); // will be overriden for orgs later
        
        Collection<PhylonodeOrganism> pos = phylonode.getPhylonodeOrganisms();
		//System.err.println("Looking at '"+shortName+"'");
        if (pos != null && pos.size() > 0) {
        	if (pos.size() > 1) {
        		System.err.println("We have too many PhylonodeOrganisms");
        	} else {
        		Organism org = pos.iterator().next().getOrganism();
        		organism = true;
        		//System.err.println("Found organism for '"+shortName+"'");
                // TODO What organism props do we want?
                
                this.taxonId = getOrganismProperty(org, "taxonId");
                String curatorName = getOrganismProperty(org, "curatorName");
                String curatorEmail = getOrganismProperty(org, "curatorEmail");
                String nickname = getOrganismProperty(org, "nickname");
                //String curatorName = getOrganismProperty(org, "curatorName");
                names.put(TaxonNameType.DB_NAME, getOrganismProperty(org, "dbname"));
                names.put(TaxonNameType.HTML_SHORT, getOrganismProperty(org, "htmlShortName"));
                names.put(TaxonNameType.HTML_FULL, getOrganismProperty(org, "htmlFullName"));
                //int translationTable = Integer.parseInt(getOrganismProperty(org, "translationTable"));
                //int mitochondrialTranslationTable = Integer.parseInt(getOrganismProperty(org, "mitochondrialTranslationTable"));
                String fullName = org.getGenus() + ' ' + org.getSpecies();
                names.put(TaxonNameType.FULL, fullName);
              
        	}
        }
        
        
    }
    
//    public TaxonNode(TaxonNode parent, Phylonode phylonode, Organism organism) {
//        this.fullName = this.organism.getGenus() + ' ' + this.organism.getSpecies();
//        if (this.organism != null) {
//            this.webLinkable = true;
//        } else {
//            String webLink = getPhylonodeProperty("");
//            if ("true".equals(webLink)) {
//                webLinkable = true;
//            }
//        }
//    }

    
    private String getPhylonodeProperty(String key) {
        for (PhylonodeProp prop : phylonode.getPhylonodeProps()) {
            if (prop.getCvTerm().getName().equals(key)) {
                return prop.getValue();
            }
        }
        return null;
    }
    

    private String getOrganismProperty(Organism org, String key) {
        for (OrganismProp prop : org.getOrganismProps()) {
            if (prop.getCvTerm().getName().equals(key)) {
                return prop.getValue();
            }
        }
        return null;
    }


    public boolean isRoot() {
        return (parent == null);
    }


    public boolean isLeaf() {
        return children.size() == 0;
    }


    public String getLabel() {
        return getName(TaxonNameType.LABEL);
    }
    

    public String getName(TaxonNameType tnt) {
        return names.get(tnt);
//        if (name == null) {
//            throw new RuntimeException("Name '"+tnt+"' not configured for '"+getLabel()+"'");
//        }
//        return name;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public TaxonNode getParent() {
        return this.parent;
    }

    private void setParent(TaxonNode parent) {
		this.parent = parent;
	}

	void addChild(TaxonNode child) {
        this.children.add(child);
        child.setParent(this);
    }

    public List<TaxonNode> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    public Phylonode getPhylonode() {
        return this.phylonode;
    }
    
    public boolean isWebLinkable() {
        return webLinkable;
    }
    
    public Map<String, Object> getAppDetails(String key) {
        if (appDetails.containsKey(key)) {
            return Collections.unmodifiableMap(appDetails.get(key));
        }
        return Collections.emptyMap();
    }
    
    public List<String> getAllChildrenNames() {
    	List<TaxonNode> allChildren = getAllChildren();
    	List<String> names = new ArrayList<String>();
    	StringBuilder ret = new StringBuilder();
    	for (TaxonNode child : allChildren) {
			if (child.isOrganism()) {
				names.add(child.getLabel());
			}
		}
    	if (isOrganism()) {
    		names.add(getLabel());
    	}
    	return names;
    }
    
    public boolean isOrganism() {
    	return organism;
    }
    
    private List<TaxonNode> getAllChildren() {
    	List<TaxonNode> ret = new ArrayList<TaxonNode>();
    	
    	List<TaxonNode> immediateChildren = getChildren();
    	for (TaxonNode child : immediateChildren) {
			ret.add(child);
			ret.addAll(child.getAllChildren());
		}
		return ret;
	}

	/**
     * @param detailed
     * @return
     */
    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append("taxon id='");
        ret.append(getTaxonId());
        ret.append("' fullName='");
        ret.append(getName(TaxonNameType.FULL));
        ret.append("' label='");
        ret.append(getLabel());
        ret.append("' organism=");
        ret.append(organism);
        ret.append("' ");
        return ret.toString();
    }

}
