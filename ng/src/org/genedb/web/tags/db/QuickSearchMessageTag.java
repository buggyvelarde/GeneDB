package org.genedb.web.tags.db;

import static javax.servlet.jsp.PageContext.APPLICATION_SCOPE;
import static org.genedb.web.mvc.controller.TaxonManagerListener.TAXON_NODE_MANAGER;

import org.genedb.db.taxon.TaxonNameType;
import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.querying.tmpquery.QuickSearchQuery;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.TreeMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class QuickSearchMessageTag extends SimpleTagSupport {

    private static final Logger logger = Logger.getLogger(QuickSearchMessageTag.class);

    private TreeMap<String, Integer> taxonGroup;

    String currentTaxonName;

    @SuppressWarnings("unchecked")
    @Override
   public void doTag() throws JspException, IOException {
        //Get the current result size, if any
        Integer resultsSize = (Integer)getJspContext().findAttribute("resultsSize");

        //Get the taxon group map
         //= (TreeMap) getJspContext().findAttribute("taxonGroup");

        QuickSearchQuery query = (QuickSearchQuery)getJspContext().findAttribute("query");

        //= (String)getJspContext().findAttribute("taxonNodeName");

        currentTaxonName = checkForHtmlShortName(currentTaxonName);
        
        logger.error("query = '" + query + "'");
        logger.error("resultsSize = '" + resultsSize + "'");
        logger.error("taxonGroup = '" + taxonGroup + "'");
        logger.error("currentTaxonName = '" + currentTaxonName + "'");
        
        try {
        
	        String message = null;
	        if (noResultsFound(resultsSize, taxonGroup)){
	            message = printNoResultsFound(query, currentTaxonName);
	
	        }else if (noResultsFound(resultsSize) && taxonGroup.size()>0){
	            message = printTaxonResultsFound(query, currentTaxonName, taxonGroup);
	
	        } else if (!noResultsFound(resultsSize) && taxonGroup.size() == 1) {
	            message = printResultsFound(query, currentTaxonName, resultsSize);
	
	        }else if(!noResultsFound(resultsSize) && taxonGroup.size() > 1){
	            message = printManyTaxonResultsFound(resultsSize, taxonGroup);
	        }
	
	        if (!StringUtils.isEmpty(message)){
	            getJspContext().getOut().write(message);
	        }
        } catch (Exception e) {
        	logger.error(e.getMessage());
        	e.printStackTrace();
        }
    }

    /**
     * Check for Html Short and return if present, else return the current as it is
     * @param currentTaxonName
     * @return
     */
    private String checkForHtmlShortName(String currentTaxonName){
        TaxonNodeManager taxonNodeManager = (TaxonNodeManager) getJspContext().getAttribute(TAXON_NODE_MANAGER, APPLICATION_SCOPE);
        TaxonNode node = taxonNodeManager.getTaxonNodeForLabel(currentTaxonName);
        if(node!= null){
            String htmlShort = node.getName(TaxonNameType.HTML_SHORT);
            if (!StringUtils.isEmpty(htmlShort)){
                return htmlShort;
            }
        }
        return currentTaxonName;
    }

    /**
     * No results found in arguments
     * @param resultsSize
     * @param taxonGroup
     * @return
     */
    private boolean noResultsFound(Integer resultsSize, TreeMap<String, Integer> taxonGroup ){
        if ((resultsSize==null || resultsSize.intValue()==0)
                && taxonGroup.size()==0){
            return true;
        }
        return false;
    }

    /**
     * No results found in argument
     * @param resultsSize
     * @param taxonGroup
     * @return
     */
    private boolean noResultsFound(Integer resultsSize ){
        if ((resultsSize==null || resultsSize.intValue()==0)) {
            return true;
        }
        return false;
    }

    /**
     * Print No Result Found Message for Exact or wildcard search
     * @param query
     * @param currentTaxonNodeName
     * @return
     */
    private String printNoResultsFound(QuickSearchQuery query, String currentTaxonNodeName){
        StringBuffer sb = new StringBuffer();

        sb.append("<font color=\"red\">");
        sb.append("No");

        if (query.getSearchText().indexOf("*")!= -1){
            sb.append(" Wildcard");
        }else{
            sb.append(" Exact");
        }
        sb.append(" match found for <i><b>");
        sb.append(query.getSearchText());
        sb.append("</b></i>");
        sb.append(" in <i><b>");
        if (StringUtils.isEmpty(currentTaxonNodeName)
                || (!StringUtils.isEmpty(currentTaxonNodeName)
                        && currentTaxonNodeName.equalsIgnoreCase("root"))){
            currentTaxonNodeName = "all organism";
        }
        sb.append(currentTaxonNodeName);
        sb.append("</b></i></font>");
        return sb.toString();
    }

    private String printTaxonResultsFound(
            QuickSearchQuery query, String currentTaxonNodeName, TreeMap<String, Integer> taxonGroup ){
        StringBuffer sb = new StringBuffer();

        sb.append("<font color=\"red\">");
        sb.append("No");

        if (query.getSearchText().indexOf("*")!= -1){
            sb.append(" Wildcard");
        }else{
            sb.append(" Exact");
        }
        sb.append(" match found for <i><b>");
        sb.append(query.getSearchText());
        sb.append("</b></i>");
        sb.append(" in <i><b>");
        if (StringUtils.isEmpty(currentTaxonNodeName)
                || (!StringUtils.isEmpty(currentTaxonNodeName)
                        && currentTaxonNodeName.equalsIgnoreCase("root"))){
            currentTaxonNodeName = "all organism";
        }
        sb.append(currentTaxonNodeName);
        sb.append("</b></i>");
        sb.append(", however the following ");
        sb.append(taxonGroup.size());
        sb.append(" organisms have matches.");
        sb.append("</font>");
        return sb.toString();
    }

    /**
     * Print number of matches found in a specific number of organisms
     * @param query
     * @param currentTaxonNodeName
     * @param resultsSize
     * @return
     */
    private String printResultsFound(QuickSearchQuery query, String currentTaxonNodeName, int resultsSize){
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isEmpty(currentTaxonNodeName)
                || (!StringUtils.isEmpty(currentTaxonNodeName) && currentTaxonNodeName.equalsIgnoreCase("root"))){
            sb.append("Found ");
            sb.append(resultsSize);
            sb.append(" matches for <i><b>");
            sb.append(query.getSearchText());
            sb.append("</b></i>.");
        }else{
            sb.append("All ");
            sb.append(resultsSize);
            sb.append(" matches for <i><b>");
            sb.append(query.getSearchText());
            sb.append("</b></i>, found in organism <i><b>");
            sb.append(currentTaxonNodeName);
            sb.append("</b></i>.");
        }
        return sb.toString();
    }

    /**
     * Print number of matches found in x number of organisms
     * @param resultsSize
     * @param taxonGroup
     * @return
     */
    private String printManyTaxonResultsFound(int resultsSize, TreeMap<String, Integer> taxonGroup){
        StringBuffer sb = new StringBuffer();
        sb.append(resultsSize);
        sb.append(" matches found in ");
        sb.append(taxonGroup.size());
        sb.append(" organisms.");
        return sb.toString();

    }

    public void setTaxonGroup(TreeMap<String, Integer> taxonGroup) {
        this.taxonGroup = taxonGroup;
    }

    public void setCurrentTaxonName(String currentTaxonName) {
        this.currentTaxonName = currentTaxonName;
    }
}
