/*
 * Created on Aug 4, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.genedb.db.domain.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * @author art
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ExtendedOrganism {

    private String taxonId;
    private String fullName;
    private String shortName;
    private String nickname;
    private ExtendedOrganism parent;
    private String dbName;
    private String htmlName;
    private List<ExtendedOrganism> children = new ArrayList<ExtendedOrganism>();

    public ExtendedOrganism() {
        super();
    }

    public ExtendedOrganism(String shortName, ExtendedOrganism parent) {
        this.shortName = shortName;
        this.parent = parent;
        this.parent.addChild(this);
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public void setTaxonId(String taxonId) {
        this.taxonId = taxonId;
    }

    public ExtendedOrganism getParent() {
        return this.parent;
    }

    public void setParent(ExtendedOrganism parent) {
        this.parent = parent;
        this.parent.addChild(this);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    void addChild(ExtendedOrganism child) {
        this.children.add(child);
    }

    /**
     * @param detailed
     * @return
     */
    public String describe(boolean detailed) {
        StringBuffer ret = new StringBuffer();
        ret.append("taxon id: ");
        ret.append(getTaxonId());
        ret.append("     name: ");
        ret.append(getFullName());
        return ret.toString();
    }

    public List<ExtendedOrganism> getChildren() {
        return this.children;
    }

    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getHtmlName() {
        return this.htmlName;
    }

    public void setHtmlName(String htmlName) {
        this.htmlName = htmlName;
    }
}
