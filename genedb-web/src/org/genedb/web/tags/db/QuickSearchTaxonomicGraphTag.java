package org.genedb.web.tags.db;

import static javax.servlet.jsp.PageContext.APPLICATION_SCOPE;
import static org.genedb.web.mvc.controller.TaxonManagerListener.TAXON_NODE_MANAGER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;

public class QuickSearchTaxonomicGraphTag extends SimpleTagSupport {

    private String top = "Root"; // FIXME
    
    
    @Override
    public void doTag() throws JspException, IOException {
        PageContext pageContext = (PageContext)getJspContext();
        
        TaxonNodeManager tnm = (TaxonNodeManager) 
            getJspContext().getAttribute(TAXON_NODE_MANAGER, APPLICATION_SCOPE);
        
        String currentTaxonNodeName = pageContext.getRequest().getParameter("taxons");
        
        TreeMap<String, Integer> taxonGroup = (TreeMap)getJspContext().findAttribute("taxonGroup");
        
        String hasResult = pageContext.getRequest().getParameter("hasresults");
        
        boolean displayAllMatchingTaxonsWhenResultsEmpty = 
            taxonGroup!= null 
            && taxonGroup.size()>0 
            && hasResult!=null && hasResult.equals("false");
            
        
        TaxonNode currentNode = null;
        if (StringUtils.isEmpty(currentTaxonNodeName) || displayAllMatchingTaxonsWhenResultsEmpty){
            currentNode = tnm.getTaxonNodeForLabel(top);
            if (currentNode == null) {
                throw new JspException("Homepage Tag: Can't identify taxonNode for '"+top+"'");
            }
        }else{
            currentNode = tnm.getTaxonNodeForLabel(currentTaxonNodeName);
        }
        
        //Create the graph to be populated and manipulated
        QuickSearchTaxonNode quickSearchTaxonNode = new QuickSearchTaxonNode();
        
        //populate with taxons
        populate(quickSearchTaxonNode, currentNode, taxonGroup);
        
        //sort in taxonomic order of closeness to the organism sought after 
        if (displayAllMatchingTaxonsWhenResultsEmpty){
            sortInOrderOfCurrentTaxon(currentTaxonNodeName, quickSearchTaxonNode);
        }
        
        //Get the writer
        JspWriter out = getJspContext().getOut();
                
        String  htmlList = transform(quickSearchTaxonNode, pageContext.getRequest());
        
        out.write(htmlList);
    }

    
    public void populate(QuickSearchTaxonNode quickSearchTaxonNode, TaxonNode taxonNode, TreeMap<String, Integer> taxonGroup){
        //populate label
        quickSearchTaxonNode.setLabel(taxonNode.getLabel());
        
        if (taxonGroup.containsKey(taxonNode.getLabel())){
            quickSearchTaxonNode.setMatch(taxonGroup.get(taxonNode.getLabel()));
        }
        
        //populate children
        for(TaxonNode childNode: taxonNode.getChildren()){
            //if (childNode.getChildren().size()==0 && !taxonGroup.containsKey(childNode.getLabel())){
              //  continue;
            //}
            if (childNode.isChildrenPopulated() ){
                QuickSearchTaxonNode myChild = new QuickSearchTaxonNode();
                quickSearchTaxonNode.getChildren().add(myChild);
                myChild.setParent(quickSearchTaxonNode);
                populate(myChild, childNode, taxonGroup);
            }
        }
        
        //Sort list
        Collections.sort(quickSearchTaxonNode.getChildren(), new Comparator<QuickSearchTaxonNode>() {
            @Override
            public int compare(QuickSearchTaxonNode arg0, QuickSearchTaxonNode arg1) {
                String label0 = arg0.getLabel();
                String label1 = arg1.getLabel();
                return label0.compareToIgnoreCase(label1);
            }
        });
    }
    
    /**
     * Find the node in the tree whose label/name is given
     * @param taxonNodeName
     * @param tree
     * @return
     */
    private QuickSearchTaxonNode findCurrentQuickTaxonNode(
            String taxonNodeName, QuickSearchTaxonNode tree){
        if(tree.getLabel().equals(taxonNodeName)){
            return tree;
        }
        for(QuickSearchTaxonNode child : tree.getChildren()){
            QuickSearchTaxonNode found = findCurrentQuickTaxonNode(taxonNodeName, child);
            if (found!= null){
                return found;
            }
        }        
        return null;
    }
    
    /**
     * Sort by re-arranging the sibling taxon nodes,by making each relevant node the first in line of siblings 
     * @param currentTaxonNodeName
     * @param quickSearchTaxonNode
     */
    private void sortInOrderOfCurrentTaxon(String currentTaxonNodeName, QuickSearchTaxonNode quickSearchTaxonNode){
        QuickSearchTaxonNode currentNode = findCurrentQuickTaxonNode(currentTaxonNodeName, quickSearchTaxonNode);
        sortInOrderOfCurrentTaxon(currentNode);
    }
    
    /**
     * Re-arrange the siblings, make the current node or it's parent, the first in line 
     * @param quickSearchTaxonNode
     */
    private void sortInOrderOfCurrentTaxon(QuickSearchTaxonNode quickSearchTaxonNode){
        QuickSearchTaxonNode parent = quickSearchTaxonNode.getParent();
        if (parent!= null){
            parent.getChildren().remove(quickSearchTaxonNode);
            parent.getChildren().add(0, quickSearchTaxonNode);
            sortInOrderOfCurrentTaxon(parent);
        }
    }
    
    /**
     * Transform taxons to String
     * @param quickSearchTaxonNode
     * @param sb
     */
    public String transform(QuickSearchTaxonNode quickSearchTaxonNode, ServletRequest request){
        String tree = "";
        for(QuickSearchTaxonNode child : quickSearchTaxonNode.getChildren()){
            tree = tree + transform(child, request);
        }
        
        //Get the leafs where a match is found
        if (quickSearchTaxonNode.getChildren().size() == 0 && quickSearchTaxonNode.getMatch() != 0){
            tree = "<li style=\"width: 150px;\">" + createUrlHref(quickSearchTaxonNode, request) + "</li>\n";
            
         //Get parent nodes where a descendant has a match
        }else if (isMatchFoundInDescendant(quickSearchTaxonNode)){
            String label = quickSearchTaxonNode.getLabel();
            if (label!= null && label.equalsIgnoreCase("root")){
                tree = "<i>All Organisms</i>\n"  +  "<ul>\n" + tree + "\n</ul>";
            }else{
                tree = "<li><i>" + label + "</i>\n"  +  "<ul>\n" + tree + "\n</ul>" + "</li>";
            }
        }
        return tree;
    }
    
    /**
     * Find a descendant with a match
     * @param ancestor
     * @return
     */
    public boolean isMatchFoundInDescendant(QuickSearchTaxonNode ancestor){
        for(QuickSearchTaxonNode child : ancestor.getChildren()){
            if (child.getMatch() > 0){
                return true;
            }
            if (isMatchFoundInDescendant(child)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Create the URL for target _parent
     * @param value
     * @return
     */
    private String createUrlHref(QuickSearchTaxonNode quickSearchTaxonNode, ServletRequest request){
        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"");
        sb.append(((HttpServletRequest)request).getContextPath());
        sb.append("/QuickSearchQuery");
        sb.append("?q=quickSearchQuery");
        sb.append("&taxons=");
        sb.append(quickSearchTaxonNode.getLabel());
        sb.append("&searchText=");
        sb.append(request.getParameter("searchText"));
        sb.append("&allNames=");
        sb.append(request.getParameter("allNames"));
        sb.append("&pseudogenes=");
        sb.append(request.getParameter("pseudogenes"));
        sb.append("&product=");
        sb.append(request.getParameter("product"));
        sb.append("\"");
        sb.append(" target=\"_parent\">");
        sb.append("<small>");
        sb.append(quickSearchTaxonNode.getLabel());
        sb.append("</small>");
        sb.append("</a>");
        sb.append("<small>");
        sb.append("(");
        sb.append(quickSearchTaxonNode.getMatch());
        sb.append(")");
        sb.append("</small>");
        return sb.toString();
    }
    
    private class QuickSearchTaxonNode{
        private String label;
        private int match;
        
        private QuickSearchTaxonNode parent;
        private List<QuickSearchTaxonNode> children = new ArrayList<QuickSearchTaxonNode>();
        
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getMatch() {
            return match;
        }

        public void setMatch(int match) {
            this.match = match;
        }

        public QuickSearchTaxonNode getParent() {
            return parent;
        }

        public void setParent(QuickSearchTaxonNode parent) {
            this.parent = parent;
        }

        public List<QuickSearchTaxonNode> getChildren() {
            return children;
        }

        public void setChildren(List<QuickSearchTaxonNode> children) {
            this.children = children;
        }
        
        public String toString(){
            if (children.size() == 0){
                return String.format("<li>%s(%d)</li>", label, match);
            }else{
                String values = null;
                for(QuickSearchTaxonNode node : children){
                    values = node.toString() + "\n";
                }
                return String.format("<ul>\n%s</ul>", values);
            }
        }
    }
}