/*
 * Created on Aug 4, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.genedb.db.loading;

import org.gmod.schema.organism.Organism;
import org.gmod.schema.phylogeny.Phylonode;
import org.gmod.schema.phylogeny.PhylonodeProp;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author art
 *
 */
public class TaxonNode {

    private String taxonId;
    private String fullName;
    private String shortName;
    private TaxonNode parent;
    private String dbName;
    private String htmlName;
    private Organism organism;
    private Phylonode phylonode;
    private List<TaxonNode> children = new ArrayList<TaxonNode>();
    private boolean webLinkable = false;
    private String nickName;
    private Map<String, Map<String, Object>> appDetails = new HashMap<String, Map<String, Object>>(0);


    public TaxonNode(Phylonode phylonode) {
        this.phylonode = phylonode;
        this.shortName = phylonode.getLabel();
    }
    
//    public TaxonNode(TaxonNode parent, Phylonode phylonode, Organism organism) {
//        this.parent = parent;
//        this.parent.addChild(this);
//        this.phylonode = phylonode;
//        this.organism = organism;
//        this.fullName = this.organism.getGenus() + ' ' + this.organism.getSpecies();
//        this.shortName = phylonode.getLabel();
//        this.taxonId = getPhylonodeProperty("");
//        this.dbName = getPhylonodeProperty("");
//        this.htmlName = getPhylonodeProperty("");
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


    public boolean isRoot() {
        return (parent == null);
    }


    public boolean isLeaf() {
        return children.size() == 0;
    }


    public String getDbName() {
        return dbName;
    }

    public String getFullName() {
        return fullName;
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

    public String getShortName() {
        return this.shortName;
    }

    public String getHtmlName() {
        return this.htmlName;
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
				names.add(child.getShortName());
			}
		}
    	if (isOrganism()) {
    		names.add(getShortName());
    	}
    	return names;
    }
    
    public boolean isOrganism() {
    	return (organism != null);
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
        ret.append(getFullName());
        ret.append("' shortName='");
        ret.append(getShortName());
        ret.append("'");
        return ret.toString();
    }

    public String getNickName() {
        return nickName;
    }
}
