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


import org.genedb.querying.core.QueryException;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.web.mvc.controller.HistoryController;
import org.genedb.web.mvc.controller.HistoryManagerFactory;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Hashtable;
import java.util.List;
import java.io.BufferedInputStream;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;



/**
 * List Download
 * 
 * @author Giles Velarde (gv1)
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 * 
 */
@Controller
@RequestMapping("/Download")
public class DownloadController {

    private Logger logger = Logger.getLogger(this.getClass());
    
    private final boolean deleteFiles = true;
    
    private DownloadProcessUtil util;
    public void setUtil(DownloadProcessUtil util) {
    	this.util = util;
    }
    
    private HistoryManagerFactory historyManagerFactory;
    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
        this.historyManagerFactory = historyManagerFactory;
    }
    
    @RequestMapping(method=RequestMethod.GET, value="/{historyItem}")
    public ModelAndView displayForm(
            @PathVariable("historyItem") int historyItem) {
        logger.error("displayForm called");
        ModelAndView mav = new ModelAndView("history/historyForm", "historyItem", historyItem);
        return mav;
    }
    
	final private static int maxResultsInWebRequest = 1000;
    
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
            @RequestParam("email") String email,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, QueryException  {
    	
		String downloadLinkUrl = request.getScheme() + "://" + request.getServerName() + "/Download/batch";
		
		logger.info("URL:: " + downloadLinkUrl);
		
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
        
        String description = HistoryController.getFormattedParameterMap(hItem);
        
        String historyItemName = hItem.getName();
        List<String> uniqueNames = hItem.getIds();
        
        if (blankField.equals("blank")) {
        	blankField = "";
        }
        
        File downloadTmpFolder = util.gettDownloadTmpFolder();
        String fileName = historyItemName + "." + util.getTime() + "." + outputFormat.name().toLowerCase();
        
        if (outputDestination == OutputDestination.TO_EMAIL) {
	        if (email != null && email.length() > 0) {
	    		
	    		saveDownloadDetailsToJsonFile(
	    				fileName, 
	    				downloadTmpFolder, 
	    				outputFormat, 
	    				outputOptions, 
	    				outputDestination, 
	    				sequenceType, 
	    				includeHeader, 
	    				fieldSeparator, 
	    				blankField, 
	    				fieldInternalSeparator, 
	    				prime3, 
	    				prime5, 
	    				email, 
	    				uniqueNames, 
	    				historyItemName, 
	    				description, 
	    				downloadLinkUrl);
	    		
	    		response.getWriter().append("The results will be mailed back to " + email + " once processed.");
	    	
	    		return null;
	    		
	    	} else {
	    		
	    		response.getWriter().append("Please supply an email address.");
	    		return null;
	    		
	    	}
		}
        
        if (uniqueNames.size() > maxResultsInWebRequest) {
        	
        	
        	response.getWriter().append("The number of results exceeds the maximum the web server will download in a single request (" + maxResultsInWebRequest + ")." +
        				"\nPlease supply your email on the previous page and the results will be mailed back to you.");
        	
        	
        	return null;
        	
        }
        
        // if we got this far, it means we're going to try a download inside the web request.
        DownloadProcess process = new DownloadProcess(outputFormat,
			custFields,
			outputDestination,
			sequenceType,
			includeHeader,
			fieldSeparator,
			blankField,
			fieldInternalSeparator,
			prime3,
			prime5,
			email,
			uniqueNames,
			historyItemName,
			description,
			util,
			downloadLinkUrl);
        
        String filePath = downloadTmpFolder + "/" + fileName;
        
        if (outputFormat == OutputFormat.XLS) {
        	
        	OutputStream outStream = null;
        	File outFile = null;
        	
        	if (outputDestination == OutputDestination.TO_BROWSER) {
        		
        		 outStream = response.getOutputStream();
        		 
        		 
        	} else if (outputDestination == OutputDestination.TO_FILE)  {
        		
        		outFile = new File( filePath );
        		outStream = new FileOutputStream(outFile);
        		
        	} 
        	
        	response.setContentType("application/vnd.ms-excel");
        	process.generateXLS(outStream);
        	
        	
        	/*
        	 * As this is a binary file, whether or not a TO_FILE is chosen, a file will be downloaded by the user.
        	 */
        	if (outputDestination == OutputDestination.TO_FILE) {
        		 
        		
              	response.setHeader("Content-Disposition", "attachment; filename=" + outFile.getName());
             	OutputStream os = response.getOutputStream();
     			returnFile(outFile, os);
     			
        	 } else {
        		 
        		 response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        		 
        	 }
        	
        	
        	if ((outFile != null) && (deleteFiles)) {
 				outFile.delete();
 			}
        	
        	
        
        } else {
        	
        	Writer out = null;
        	File tabOutFile = null;
        	
        	if (outputDestination == OutputDestination.TO_BROWSER) {
        		out = response.getWriter();
        	} else if (outputDestination == OutputDestination.TO_FILE) {
        		
        		tabOutFile = new File( filePath );
        		out = new FileWriter(tabOutFile);
        		
        	} 
        	        	
        	switch (outputFormat) {
        	case CSV:
        		response.setContentType("text/plain");
        		process.generateCSV(out);
        		break;
        	case HTML:
        		response.setContentType("text/html");
        		process.generateHTML(out);
        		break;
        	case FASTA:
        		response.setContentType("text/plain");
        		process.generateFASTA(out);
        		break;
        	}
        	        	
        	
        	/*
        	 * Post formatting for different output destinations.   
        	 */
        	
        	if (outputDestination == OutputDestination.TO_FILE)  {
        		
            	response.setContentType("application/x-download");
            	response.setHeader("Content-Disposition", "attachment; filename="+tabOutFile.getName());
            	
            	OutputStream os = response.getOutputStream();
    			returnFile(tabOutFile, os);
            
    			if ((tabOutFile != null) && (deleteFiles)) {
            		tabOutFile.delete();
    			}
        		
        	}
        	
        }
        
        logger.info(fileName + " complete");
        
        return null;
    }
	
	@RequestMapping(method=RequestMethod.GET, value="/batch")
	public ModelAndView batch (HttpServletResponse response, @RequestParam("file") final String suppliedFileName) throws FileNotFoundException, IOException {
		
		final File baseFileFolder = util.gettDownloadTmpFolder();
		File suppliedFile = new File(baseFileFolder, suppliedFileName);
		
		logger.info(String.format("Trying to return %s link for file %s.", suppliedFileName, suppliedFile.getAbsolutePath()));
		
		if ((! suppliedFile.isFile()) || (suppliedFile.isDirectory() || (! suppliedFile.getName().endsWith("zip")))) {
			logger.error("Could not find file");
			response.getWriter().append("Could not find file " + suppliedFileName);
			return null;
		}
		
		response.setContentType("application/x-download");
    	response.setHeader("Content-Disposition", "attachment; filename="+suppliedFile.getName());
		
		returnFile(suppliedFile, response.getOutputStream());
		
		return null;
	}
	
	
    
    /**
     * Lifted from genedb classic.
     * 
     * @param file
     * @param out
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void returnFile(File file, OutputStream out) throws FileNotFoundException, IOException {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			byte[  ] buf = new byte[4 * 1024];  // 4K buffer
			int bytesRead;
			while ((bytesRead = in.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
		}
		finally {
			if (in != null) in.close(  );
		}
	}
    
    
    /**
     * Save the query details to a JSON file, for batch processing. 
     * 
     * @param scriptFileNamePrefix
     * @param downloadTmpFolder
     * @param outputFormat
     * @param outputOptions
     * @param outputDestination
     * @param sequenceType
     * @param includeHeader
     * @param fieldSeparator
     * @param blankField
     * @param fieldInternalSeparator
     * @param prime3
     * @param prime5
     * @param email
     * @param uniqueNames
     * @param historyItemName
     * @param description
     * @throws IOException
     */
    public void saveDownloadDetailsToJsonFile(
			String scriptFileNamePrefix,
			File downloadTmpFolder,
			OutputFormat outputFormat,
			List<OutputOption> outputOptions,
			OutputDestination outputDestination,
			SequenceType sequenceType,
			boolean includeHeader,
			String fieldSeparator,
			String blankField,
			String fieldInternalSeparator,
			int prime3,
			int prime5,
			String email,
			List<String> uniqueNames,
			String historyItemName,
			String description, 
			String downloadLinkUrl) throws IOException {
		
		Hashtable<String, Object> ht = new Hashtable<String, Object>();
		ht.put("custFields", outputOptions);
		ht.put("outputFormat", outputFormat);
		ht.put("outputDestination", outputDestination);
		ht.put("sequenceType", sequenceType);
		ht.put("includeHeader", includeHeader);
		ht.put("fieldSeparator", fieldSeparator);
		ht.put("blankField", blankField);
		ht.put("fieldInternalSeparator", fieldInternalSeparator);
		ht.put("prime3", prime3);
		ht.put("prime5", prime5);
		ht.put("email", email);
		ht.put("historyItemName", historyItemName);
		ht.put("description", description);
		ht.put("uniqueNames", uniqueNames);
		ht.put("url", downloadLinkUrl);
		
		String filePath = downloadTmpFolder + "/" + scriptFileNamePrefix + ".json";
		
		FileWriter out = new FileWriter(filePath);
		Gson gson = new Gson();
		gson.toJson(ht, out);
		out.close();
		
		logger.info("Wrote to JSON file: " + filePath);
		
	}

    
    
}

