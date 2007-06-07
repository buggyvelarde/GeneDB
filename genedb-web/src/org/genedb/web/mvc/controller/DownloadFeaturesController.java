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

package org.genedb.web.mvc.controller;


import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.TaxonNode;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Looks up a feature by uniquename, and possibly synonyms
 * 
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class DownloadFeaturesController extends SimpleFormController {

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
        
        Writer w = response.getWriter();
        w.write("destination="+db.getOutputDestination().name()+"<br>");
        w.write("format="+db.getOutputDestination().name()+"<br>");
        w.write("options="+db.getOutputOption()+"<br>");
        w.close();
        return null;
//        String sequenceOrFeature = db.getMethod();
//        
//        if ("Sequence".equalsIgnoreCase(sequenceOrFeature)) {
//        	return sequenceDownload(db, response);
//        }
//        if ("Annotation".equalsIgnoreCase(sequenceOrFeature)) {
//        	return annotationDownload(db, response);
//        }
//        
//        // Problem - report FIXME
//        
//		return new ModelAndView(getFormView());
    }

	private ModelAndView annotationDownload(DownloadBean db, HttpServletResponse response) throws IOException {
		Writer out = response.getWriter();
		out.write("Downloading annotation");
		return null;
	}

	private ModelAndView sequenceDownload(DownloadBean db, HttpServletResponse response) throws IOException {
		Writer out = response.getWriter();
		out.write("Downloading sequence");
		return null;
	}

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

	public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
		this.historyManagerFactory = historyManagerFactory;
	}

	
}

