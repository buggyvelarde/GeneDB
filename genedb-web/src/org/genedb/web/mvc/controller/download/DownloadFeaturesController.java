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

package org.genedb.web.mvc.controller.download;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.TaxonNode;
import org.genedb.web.mvc.controller.HistoryItem;
import org.genedb.web.mvc.controller.HistoryManager;
import org.genedb.web.mvc.controller.HistoryManagerFactory;
import org.genedb.web.mvc.controller.TaxonNodeBindingFormController;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureRelationship;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



/**
 * Looks up a feature by uniquename, and possibly synonyms
 * 
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class DownloadFeaturesController extends TaxonNodeBindingFormController {

    private SequenceDao sequenceDao;
    private HistoryManagerFactory historyManagerFactory;
    
//	@Override
//	protected Map referenceData(HttpServletRequest request) throws Exception {
//		Map map = super.referenceData(request);
//		return getDownloadMethods(map);
//	}

//	protected Map getDownloadMethods(Map in) {
//		Map map = in;
//		if (map == null) {
//			map = new HashMap();
//		}
//		List downloadMethods = new ArrayList();
//		downloadMethods.add("Choose...");
//		downloadMethods.add("Sequence");
//		downloadMethods.add("Annotation");
//		map.put("downloadMethods", downloadMethods);
//		return map;
//	}

    private String createExcel(List<String> ids,String file,String[] columns) {
    	int rcount = 2;
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet();
		List<Feature> features = sequenceDao.getFeaturesByUniqueNames(ids);
		for(Feature feature : features) {
			HSSFRow row = sheet.createRow(rcount++);
			HSSFCell cell1 = row.createCell((short) 1);
			HSSFCell cell2 = row.createCell((short) 2);
			HSSFCell cell3 = row.createCell((short) 3);
			HSSFCell cell4 = row.createCell((short) 4);
			HSSFCell cell5 = row.createCell((short) 5);
			HSSFCell cell6 = row.createCell((short) 6);
			HSSFCell cell7 = row.createCell((short) 7);
			HSSFCell cell8 = row.createCell((short) 8);
			HSSFCell cell9 = row.createCell((short) 9);
			HSSFCell cell10 = row.createCell((short) 10);
			
			int count = 1;
			for (String column: columns) {
				switch (count) {
					case 1:
							cell1.setCellValue(getValue(column,feature));
							break;
					case 2:
							cell2.setCellValue(getValue(column,feature));
							break;
					case 3:
							cell3.setCellValue(getValue(column,feature));
							break;
					case 4:
							cell4.setCellValue(getValue(column,feature));
							break;
					case 5:
							cell5.setCellValue(getValue(column,feature));
							break;
					case 6:
							cell6.setCellValue(getValue(column,feature));
							break;
					case 7:
							cell7.setCellValue(getValue(column,feature));
							break;
					case 8:
							cell8.setCellValue(getValue(column,feature));
							break;
					case 9:
							cell9.setCellValue(getValue(column,feature));
							break;
					case 10:
							cell10.setCellValue(getValue(column,feature));
							break;
				}
				count++;
			}
		}
		
		String f = getServletContext().getRealPath("/includes/excel/" + file);
		FileOutputStream stream=null;
		try {
			stream = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			workbook.write(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    private String createCsv(List<String> ids,String file,String[] columns) {
    	List<Feature> features = sequenceDao.getFeaturesByUniqueNames(ids);
    	BufferedWriter out = null;
    	try {
    		out = new BufferedWriter(new FileWriter(getServletContext().getRealPath("/includes/excel/" + file)));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		
		StringBuffer whole = new StringBuffer();
		
		for (Feature feature : features) {
			StringBuffer row = new StringBuffer();
			for (String column : columns) {
				if (column.equals("organism")) {
					row.append(feature.getOrganism().getCommonName()+",");
				} else if (column.equals("type")) {
					row.append(feature.getCvTerm().getName()+",");
				} else if (column.equals("name")) {
					row.append(feature.getUniqueName()+",");
				} else if (column.equals("pname")) {
					row.append(feature.getName()+",");
				} else if (column.equals("product")) {
					String product = "";
		    		Iterator<FeatureRelationship> iter = feature.getFeatureRelationshipsForObjectId().iterator();
		    		while(iter.hasNext()) {
		    			FeatureRelationship fr = iter.next();
		    			Iterator<FeatureRelationship> frs = fr.getFeatureBySubjectId().getFeatureRelationshipsForObjectId().iterator();
		    			while(frs.hasNext()) {
		    				FeatureRelationship frel = frs.next();
		    				//logger.info("Type = " + frel.getFeatureBySubjectId().getCvTerm().getName());
		    				if(frel.getFeatureBySubjectId().getCvTerm().getName().equals("polypeptide")) {
		    					Feature f = frel.getFeatureBySubjectId();
		    					//logger.info("Polypeptide is " + f.getUniqueName());
		    					Iterator<FeatureCvTerm> fcts = f.getFeatureCvTerms().iterator();
		    					while(fcts.hasNext()) {
		    						FeatureCvTerm fct = fcts.next();
		    						if(fct.getCvTerm().getCv().getName().equals("genedb_products")) {
		    							product = fct.getCvTerm().getName();
		    						}
		    					}
		    				}
		    			}
		    		}
		    		row.append(product+",");
				} else if (column.equals("location")) {
					Iterator<FeatureLoc> flocs = feature.getFeatureLocsForFeatureId().iterator();
		    		while(flocs.hasNext()) {
		    			FeatureLoc floc = flocs.next();
		    			int min = floc.getFmin();
		        		int max = floc.getFmax();
		        		if(floc.getStrand() == 1) {
		        			row.append((min + "-" + max)+",");
		        		} else {
		        			row.append("complement( " + min + "-" + max + " ),");
		        		}
		    		}
				} else if (column.equals("chromosome")) {
					Iterator<FeatureLoc> flocs = feature.getFeatureLocsForFeatureId().iterator();
		    		while(flocs.hasNext()) {
		    			FeatureLoc floc = flocs.next();
		    			int min = floc.getFmin();
		        		int max = floc.getFmax();
		        		row.append(floc.getFeatureBySrcFeatureId().getUniqueName() + ",");
		    		}
				}
			}
			row.deleteCharAt(row.length()-1);
			whole = whole.append(row.toString() + "\n");
		}
		try {
			out.write(whole.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	return null;
    }
    
    private String getValue(String column, Feature feature) {
		if(column.equals("organism")) {
			return feature.getOrganism().getCommonName();
		} else if (column.equals("type")) {
			return feature.getCvTerm().getName();
		} else if (column.equals("name")) {
			return feature.getUniqueName();	
		} else if (column.equals("pname")) {
			return feature.getName();
		} else if (column.equals("product")) {
			String product = "";
    		Iterator<FeatureRelationship> iter = feature.getFeatureRelationshipsForObjectId().iterator();
    		while(iter.hasNext()) {
    			FeatureRelationship fr = iter.next();
    			Iterator<FeatureRelationship> frs = fr.getFeatureBySubjectId().getFeatureRelationshipsForObjectId().iterator();
    			while(frs.hasNext()) {
    				FeatureRelationship frel = frs.next();
    				//logger.info("Type = " + frel.getFeatureBySubjectId().getCvTerm().getName());
    				if(frel.getFeatureBySubjectId().getCvTerm().getName().equals("polypeptide")) {
    					Feature f = frel.getFeatureBySubjectId();
    					//logger.info("Polypeptide is " + f.getUniqueName());
    					Iterator<FeatureCvTerm> fcts = f.getFeatureCvTerms().iterator();
    					while(fcts.hasNext()) {
    						FeatureCvTerm fct = fcts.next();
    						if(fct.getCvTerm().getCv().getName().equals("genedb_products")) {
    							product = fct.getCvTerm().getName();
    						}
    					}
    				}
    			}
    		}
    		return product;
		} else if (column.equals("location")) {
			Iterator<FeatureLoc> flocs = feature.getFeatureLocsForFeatureId().iterator();
    		while(flocs.hasNext()) {
    			FeatureLoc floc = flocs.next();
    			int min = floc.getFmin();
        		int max = floc.getFmax();
        		if(floc.getStrand() == 1) {
        			return(min + "-" + max);
        		} else {
        			return("complement( " + min + "-" + max + " )");
        		}

    		}
		} else if (column.equals("chromosome")) {
			Iterator<FeatureLoc> flocs = feature.getFeatureLocsForFeatureId().iterator();
    		while(flocs.hasNext()) {
    			FeatureLoc floc = flocs.next();
    			int min = floc.getFmin();
        		int max = floc.getFmax();
        		return floc.getFeatureBySrcFeatureId().getUniqueName();
    		}
    		
		}
		return null;
	}
    
	@Override
    protected ModelAndView onSubmit(HttpServletRequest request, 
    		HttpServletResponse response, Object command, 
    		BindException be) throws Exception {
    	
        DownloadBean db = (DownloadBean) command;
       
        int historyItem = db.getHistoryItem()-1;
        
        OutputFormat format = db.getOutputFormat();
        
        if(!(format==null)) {
        	String file = "";
        	String columns[];
        	HistoryManager historyManager = historyManagerFactory.getHistoryManager(request.getSession());
    		List<HistoryItem> historyItems = historyManager.getHistoryItems();
    		HistoryItem hItem = historyItems.get(historyItem);
    		List<String> ids = hItem.getIds();
    		
        	switch (format) {
        		case EXCEL:
        				file = request.getSession().getId() + ".xls";
        				columns = request.getParameter("columns").split(",");
        				createExcel(ids,file,columns);
        				break;
        		case CSV:
        				file = request.getSession().getId() + ".txt"; 
        				columns = request.getParameter("columns").split(",");
        				createCsv(ids,file,columns);
        				break;
        	}
        	
        	Map model = new HashMap(2);
        	model.put("file", file);
        	model.put("format", format);
        	return new ModelAndView("download/download",model);
        	
        } else {
        
	        File tmpDir = new File(getServletContext().getRealPath("/includes/scripts/extjs"));
	        BufferedWriter out = null;
			try {
				out = new BufferedWriter(new FileWriter(tmpDir + "/download.js"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			
			out.write(" Ext.onReady(function(){ \n" +
						" var ds = new Ext.data.Store({ \n" +
				        " proxy: new Ext.data.HttpProxy({ \n" +
				        " url: 'http://localhost:8080/genedb-web/Ext?history=" + historyItem + "'\n" +
				        " }), \n" +
				        " reader: new Ext.data.JsonReader({ \n" +
				        " root: 'features', \n" +
				        " totalProperty: 'total', \n" +
				        " id: 'id' \n" +
				        " }, [ \n" +
				        " {name: 'organism', mapping: 'organism'}, \n" +
				        " {name: 'type', mapping: 'type'}, \n" +
				        " {name: 'name', mapping: 'name'}, \n" +
				        " {name: 'chromosome', mapping: 'chromosome'}, \n" +
				        " {name: 'location', mapping: 'location'}, \n" +
				        " {name: 'product', mapping: 'product'}, \n" +
				        " {name: 'pname', mapping: 'pname'} \n" +
				        " ]), \n" +
				        " remoteSort: true \n" +
	    				" });\n\n");
	
			out.write("var cm = new Ext.grid.ColumnModel([{ \n" +
	                  " id: 'organism', \n" + 
	                  " header: \"Organism\", \n" +
	                  " dataIndex: 'organism', \n" +
	                  " width: 150, \n" +
	                  " css: 'white-space:normal;' \n" +
	        		  " },{ \n" +
	        		  " id: 'type', \n" +
	                  " header: \"Type\", \n" +
	                  " dataIndex: 'type', \n" +
	                  " hidden: true, \n" +
	                  " width: 150, \n" +
	                  " },{ \n" +
	                  " id: 'chromosome', \n" +
	                  " header: \"Chromosome\", \n" +
	                  " dataIndex: 'chromosome', \n" +
	                  " hidden: true, \n" +
	                  " width: 150, \n" +
	                  " },{ \n" +
	                  " id: 'location', \n" +
	                  " header: \"Location\", \n" +
	                  " dataIndex: 'location', \n" +
	                  " hidden: true, \n" +
	                  " width: 150, \n" +
	                  " },{ \n" +
	                  " id: 'product', \n" +
	                  " header: \"Product\", \n" +
	                  " dataIndex: 'product', \n" +
	                  " width: 150 \n" +
	                  " },{ \n" +
	                  " id: 'pname', \n" +
	                  " header: \"PrimaryName\", \n" +
	                  " dataIndex: 'pname', \n" +
	                  " width: 150 \n" +
	        		  " },{ \n" +
	        		  " id: 'name', \n" +
	                  " header: \"name\", \n" +
	                  " dataIndex: 'name', \n" +
	                  " width: 150 " + "}]);\n\n");
			
			out.write("var grid = new Ext.grid.Grid('topic-grid', { \n" +
	                  " ds: ds, \n" +
	                  " cm: cm, \n" +
	                  " selModel: new Ext.grid.RowSelectionModel({singleSelect:true}), \n" +
	                  " enableColLock:false, \n" +
	                  " loadMask: true \n" +
	    			  " });\n\n");
			
			out.write("var rz = new Ext.Resizable('topic-grid', { \n" +
					  " wrap:true, \n" +
					  " minHeight:300, \n" +
					  " pinned:true, \n" +
					  " handles: 's' \n" +
	    			  " }); \n" +
	    			  " \n rz.on('resize', grid.autoSize, grid); \n" +
	    			  " grid.render(); \n" +
	    			  " var gridFoot = grid.getView().getFooterPanel(true);\n\n");
			
			out.write("var paging = new Ext.PagingToolbar(gridFoot, ds, { \n" +
					  " pageSize: 25, \n" +
					  " displayInfo: true, \n"+
					  " displayMsg: 'Displaying topics {0} - {1} of {2}', \n" +
					  " emptyMsg: \"No results to display\" \n" +
	    			  " });\n\n");
			
			out.write("paging.add('-', { \n" +
					  " pressed: false, \n" +
					  " enableToggle:true, \n" +
					  " text: 'Excel', \n" +
					  " cls: 'x-btn-text-icon details', \n" +
					  " toggleHandler: toggleExcel });\n\n");
			int h = historyItem + 1;
			out.write("function toggleExcel(btn, pressed){ \n" +
					  "if (pressed) { \n" +
					  "  var count = cm.getColumnCount();\n" +
					  "  var hidden = new Array();\n" +
					  "  var i=0;\n" +
					  "  for(var index=0; index<count; index++) {\n" +
					  "  	var id = cm.getColumnId(index);\n" +
					  "		if(!(cm.isHidden(index))) {\n" +
					  "			hidden[i] = id;\n" +
					  "			i++;\n" +
					  "		}\n" +
					  "	 }\n"+
					  " var cols = hidden.join(',')\n"+
					  " window.location=\"http://localhost:8080/genedb-web/DownloadFeatures?historyItem=" + h + "&outputFormat=EXCEL&columns=\" + cols + \"\";\n" +
					  //"	 ds.load({params:{start:0, limit:25,excel:true,columns:hidden.join(',')}}); \n" +
					  "} \n" +
					  "}\n\n");
			
			out.write("paging.add('-', { \n" +
					  " pressed: false, \n" +
					  " enableToggle:true, \n" +
					  " text: 'CSV', \n" +
					  " cls: 'x-btn-text-icon details', \n" +
					  " toggleHandler: toggleTab });\n\n");
			
			out.write("function toggleTab(btn, pressed){ \n" +
					  "if (pressed) { \n" +
					  "  var count = cm.getColumnCount();\n" +
					  "  var hidden = new Array();\n" +
					  "  var i=0;\n" +
					  "  for(var index=0; index<count; index++) {\n" +
					  "  	var id = cm.getColumnId(index);\n" +
					  "		if(!(cm.isHidden(index))) {\n" +
					  "			hidden[i] = id;\n" +
					  "			i++;\n" +
					  "		}\n" +
					  "	 }\n"+
					  " var cols = hidden.join(',')\n"+
					  " window.location=\"http://localhost:8080/genedb-web/DownloadFeatures?historyItem=" + h + "&outputFormat=CSV&columns=\" + cols + \"\";\n" +
					  //"	 ds.load({params:{start:0, limit:25,excel:true,columns:hidden.join(',')}}); \n" +
					  "} \n" +
					  "}\n\n");
			
			out.write("ds.load({params:{start:0, limit:25}});\n");
			out.write("});\n");
			
			out.close();
			
	        /*Writer w = response.getWriter();
	        w.write("destination="+db.getOutputDestination().name()+"\n");
	        w.write("format="+db.getOutputDestination().name()+"\n");
	        w.write("history="+db.getHistoryItem()+"\n");
	        w.write("version="+db.getVersion()+"\n");
	        w.write("options="+db.getOutputOption()+"\n");
	        w.close();
	        return null;*/
	//       
	//        // Problem - report FIXME
	//      
			Map<String, Object> model = new HashMap<String, Object>(2);
			model.put("historyItem", historyItem);
			model.put("version", db.getVersion());
			return new ModelAndView("search/download2",model);
        }
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

	public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
		this.historyManagerFactory = historyManagerFactory;
	}

	
}

