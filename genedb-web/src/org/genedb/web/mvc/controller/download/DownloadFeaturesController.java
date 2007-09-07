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


import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.TaxonNode;
import org.genedb.web.mvc.controller.HistoryItem;
import org.genedb.web.mvc.controller.HistoryManager;
import org.genedb.web.mvc.controller.HistoryManagerFactory;
import org.genedb.web.mvc.controller.TaxonNodeBindingFormController;

import org.gmod.schema.sequence.Feature;
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


	@Override
    protected ModelAndView onSubmit(HttpServletRequest request, 
    		HttpServletResponse response, Object command, 
    		BindException be) throws Exception {
    	
        DownloadBean db = (DownloadBean) command;
       
        int historyItem = db.getHistoryItem()-1;
        
        logger.info("history item in downloadBean " + historyItem);
        
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
                  " header: \"Type\", \n" +
                  " dataIndex: 'type', \n" +
                  " hidden: true, \n" +
                  " width: 150, \n" +
                  " },{ \n" +
                  " header: \"Chromosome\", \n" +
                  " dataIndex: 'chromosome', \n" +
                  " hidden: true, \n" +
                  " width: 150, \n" +
                  " },{ \n" +
                  " header: \"Location\", \n" +
                  " dataIndex: 'location', \n" +
                  " hidden: true, \n" +
                  " width: 150, \n" +
                  " },{ \n" +
                  " header: \"Product\", \n" +
                  " dataIndex: 'product', \n" +
                  " width: 150 \n" +
                  " },{ \n" +
                  " header: \"PrimaryName\", \n" +
                  " dataIndex: 'pname', \n" +
                  " width: 150 \n" +
        		  " },{ \n" +
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

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

	public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
		this.historyManagerFactory = historyManagerFactory;
	}

	
}

