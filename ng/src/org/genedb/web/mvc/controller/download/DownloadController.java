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
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.web.mvc.controller.HistoryManagerFactory;
import org.genedb.web.mvc.model.BerkeleyMapFactory;
import org.genedb.web.mvc.model.TranscriptDTO;

import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;



/**
 * List Download
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 * @author Giles Velarde (gv1)
 */
@Controller
@RequestMapping("/Download")
public class DownloadController {

    private Logger logger = Logger.getLogger(this.getClass());

    private SequenceDao sequenceDao;
    private HistoryManagerFactory historyManagerFactory;
    
    private BerkeleyMapFactory bmf;
    
    public void setBmf(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }

    @RequestMapping(method=RequestMethod.GET, value="/{historyItem}")
    public ModelAndView displayForm(
            @PathVariable("historyItem") int historyItem) {
        logger.error("displayForm called");
        ModelAndView mav = new ModelAndView("history/historyForm", "historyItem", historyItem);
        return mav;
    }


    @RequestMapping(method=RequestMethod.POST, value="/{historyItem}")
    public ModelAndView onSubmit(
            @PathVariable("historyItem") int historyItem,
            @RequestParam("cust_format") OutputFormat outputFormat,
            @RequestParam("cust_field") String[] custFields,
            @RequestParam("output_dest") OutputDestination outputDestination,
            @RequestParam(value="sequenceType", required=false) SequenceType sequenceType,
            @RequestParam("cust_header") boolean includeHeader,
            @RequestParam("field_sep") String fieldSeparator,
            @RequestParam("field_blank") String blankField,
            @RequestParam("field_intsep") String fieldInternalSeparator,
            @RequestParam("prime3") int prime3,
            @RequestParam("prime5") int prime5,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
    	
    	
    	
        HistoryManager historyManager = historyManagerFactory.getHistoryManager(request.getSession());
        List<HistoryItem> historyItems = historyManager.getHistoryItems();
        if (historyItem > historyItems.size()) {
            response.sendError(511);
            return null;
        }

        List<OutputOption> outputOptions = Lists.newArrayList();
        for (String custField : custFields) {
            outputOptions.add(OutputOption.valueOf(custField));
        }

        HistoryItem hItem = historyItems.get(historyItem-1);
        List<String> uniqueNames = hItem.getIds();
        List<Integer> featureIds = convertUniquenamesToFeatureIds(uniqueNames);
        
        fieldSeparator = determineFieldSeparator(fieldSeparator, outputFormat);
        
        if (blankField.equals("blank")) {
        	blankField = "";
        }
        
        List<TranscriptDTO> transcriptDTOs = new ArrayList<TranscriptDTO>();
        
        for (int id : featureIds) {
        	TranscriptDTO dto = bmf.getDtoMap().get(id);
        	transcriptDTOs.add(dto);
        }

        switch (outputFormat) {
        case EXCEL:
        	
        	OutputStream outStream = response.getOutputStream();
        	response.setContentType("application/vnd.ms-excel");
        	response.setHeader("Content-Disposition", "attachment; filename=results.xls");
        	
        	FormatExcel excelFormatter = new FormatExcel();
        	
        	excelFormatter.setFieldInternalSeparator(fieldInternalSeparator);
        	excelFormatter.setOutputOptions(outputOptions);
        	
        	excelFormatter.setOutputStream(outStream);
        	
        	excelFormatter.format(transcriptDTOs.iterator());
            
            break;

        case CSV:
        case TAB:
        {
        	Writer out = response.getWriter();
        	
        	if (fieldSeparator == "default") {
            	fieldSeparator = "\t";
            }
        	
            prepareResponse(response, "text/plain", true);
            response.setContentType("text/plain");
            
            FormatCSV csvFormatter = new FormatCSV();
            
            csvFormatter.setBlankField(blankField);
            csvFormatter.setHeader(includeHeader);
            csvFormatter.setFieldInternalSeparator(fieldInternalSeparator);
            csvFormatter.setFieldSeparator(fieldSeparator);
            csvFormatter.setOutputOptions(outputOptions);
            csvFormatter.setWriter(out);
            
            csvFormatter.format(transcriptDTOs.iterator());
            
            break;
        }

        case HTML:
        {
        	Writer out = response.getWriter();
        	
            prepareResponse(response, "text/html", true);
            
            FormatHTML htmlFormatter = new FormatHTML();
            htmlFormatter.setBlankField(blankField);
            htmlFormatter.setHeader(includeHeader);
            htmlFormatter.setFieldInternalSeparator(fieldInternalSeparator);
            htmlFormatter.setOutputOptions(outputOptions);
            htmlFormatter.setWriter(out);
            
            htmlFormatter.format(transcriptDTOs.iterator());

            break;
        }

        case FASTA:
        	
        	Writer out = response.getWriter();
        	
            prepareResponse(response, "text/plain", true);
            
            FormatFASTA fastaFormatter = new FormatFASTA();
            

            fastaFormatter.setBlankField(blankField);
            fastaFormatter.setHeader(includeHeader);
            fastaFormatter.setFieldInternalSeparator(fieldInternalSeparator);
            fastaFormatter.setFieldSeparator(fieldSeparator);
            fastaFormatter.setOutputOptions(outputOptions);
            fastaFormatter.setWriter(out);
            
            fastaFormatter.setPrime3(prime3);
            fastaFormatter.setPrime5(prime5);
            fastaFormatter.setSequenceType(sequenceType);
            fastaFormatter.setSequenceDao(sequenceDao);
            
            fastaFormatter.format(transcriptDTOs.iterator());
            
            break;
            
        }
        return null;
    }
    
    private String determineFieldSeparator(String fieldSeparator, OutputFormat outputFormat) {
    	
    	if (fieldSeparator.equals("default")) {
    		switch (outputFormat) {
	        case CSV:
	        case TAB:
	            	fieldSeparator = "\t";
	        	break;
	        case FASTA:
        		fieldSeparator = "|";
        		break;
	        }
    	}
    	else if (fieldSeparator.equals("tab")) {
        	fieldSeparator = "\t";
        }
        
        return fieldSeparator;
    }

    private void prepareResponse(HttpServletResponse response, String type, boolean toPage) {
        if (toPage) {
            response.setContentType(type);
        } else {
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", "attachment; filename=results.txt");
        }
    }

    


    private List<Integer> convertUniquenamesToFeatureIds(List<String> uniqueNames) {
        List<Integer> ret = Lists.newArrayList();
        for (String name : uniqueNames) {
            logger.error("Trying to lookup '"+name+"' as a Transcript");
            Feature f = sequenceDao.getFeatureByUniqueName(name, Feature.class);
            logger.error("The value is '"+f+"'");
            ret.add(f.getFeatureId());
        }

        return ret;
    }
    

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
        this.historyManagerFactory = historyManagerFactory;
    }
    
}

