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

import org.genedb.querying.core.NumericQueryVisibility;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryFactory;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.tmpquery.GeneDetail;
import org.genedb.querying.tmpquery.IdsToGeneDetailQuery;
import org.genedb.web.mvc.controller.HistoryController;
import org.genedb.web.mvc.controller.HistoryManagerFactory;
import org.genedb.web.mvc.model.BerkeleyMapFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.BufferedInputStream;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;



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

    private SequenceDao sequenceDao;
    private HistoryManagerFactory historyManagerFactory;
    
    private JavaMailSender mailSender;
    
    public void setMailSender(JavaMailSender mailSender) {
    	this.mailSender = mailSender;
    }
    
    private BerkeleyMapFactory bmf;
    
    public void setBmf(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }
    
    private File downloadTmpFolder;
    private boolean deleteFiles = true;
    
    @SuppressWarnings("unchecked")
	private QueryFactory queryFactory;
    
    @SuppressWarnings("unchecked")
    public void setQueryFactory(QueryFactory queryFactory) {
    	this.queryFactory = queryFactory;
    }
    
    public void setDownloadTmpFolder(String downloadTmpFolder) throws Exception {
    	this.downloadTmpFolder = new File (downloadTmpFolder);
    	
    	if (this.downloadTmpFolder.isFile()) {
    		throw new Exception("Can't use the path to a file as a folder");
    	}
    	
    	if (! this.downloadTmpFolder.isDirectory()) {
    		this.downloadTmpFolder.mkdirs();
    	}
    }
    
    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

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
        
        @SuppressWarnings("unchecked")
        IdsToGeneDetailQuery query = (IdsToGeneDetailQuery) queryFactory.retrieveQuery("idsToGeneDetail",  NumericQueryVisibility.PRIVATE);
        query.setIds(uniqueNames);
        
        @SuppressWarnings("unchecked")
        List<GeneDetail> results = query.getResults();
        
        fieldSeparator = determineFieldSeparator(fieldSeparator, outputFormat);
        
        if (blankField.equals("blank")) {
        	blankField = "";
        }
        
        String fileName = downloadTmpFolder + "/" + historyItemName + "." + outputFormat.name().toLowerCase();
        
        if (outputFormat == OutputFormat.XLS) {
        	
        	OutputStream outStream = null;
        	File outFile = null;
        	
        	if (outputDestination == OutputDestination.TO_BROWSER) {
        		
        		 outStream = response.getOutputStream();
        		 
        	} else if ((outputDestination == OutputDestination.TO_EMAIL) || (outputDestination == OutputDestination.TO_FILE) ) {
        		
        		outFile = new File( fileName );
        		outStream = new FileOutputStream(outFile);
        		
        	} 
        	
        	FormatExcel excelFormatter = new FormatExcel();
        	
        	excelFormatter.setBmf(bmf);
        	excelFormatter.setFieldInternalSeparator(fieldInternalSeparator);
        	excelFormatter.setOutputOptions(outputOptions);
        	
        	excelFormatter.setOutputStream(outStream);
        	
        	excelFormatter.setSequenceDao(sequenceDao);
        	excelFormatter.format(results);
            
        	
        	/*
        	 * Post formatting for file output destination.
        	 */
        	if (outputDestination == OutputDestination.TO_EMAIL) {
        		
        		File zipFile = zip(outFile);
        		
            	try {
					sendEmail(email, historyItemName, "Please find attached your results in the excel file." + description, zipFile);
					response.getWriter().append("Your email has been sent to " + email + ".");
				} catch (MessagingException e) {
					logger.error(e.getStackTrace().toString());
					response.getWriter().append("Could not send mail. " + e.getMessage());
					
				}
            	
            	if (deleteFiles) {
            		zipFile.delete();
            	}
            	
        	 } else if (outputDestination == OutputDestination.TO_FILE) {
        		 
        		response.setContentType("application/vnd.ms-excel");
              	response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        		
             	OutputStream os = response.getOutputStream();
     			returnFile(outFile, os);
     			
        	 } else {
        		 
        		 response.setContentType("application/vnd.ms-excel");
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
        	} else if ((outputDestination == OutputDestination.TO_EMAIL) || (outputDestination == OutputDestination.TO_FILE)) {
        		
        		tabOutFile = new File( fileName );
        		out = new FileWriter(tabOutFile);
        		
        	} 
        	
        	if (fieldSeparator == "default") {
            	fieldSeparator = "\t";
            }
        	
        	if (outputFormat == OutputFormat.CSV) {
	            
        		response.setContentType("text/plain");
	            
	            FormatCSV csvFormatter = new FormatCSV();
	            
	            csvFormatter.setBmf(bmf);
	            csvFormatter.setBlankField(blankField);
	            csvFormatter.setHeader(includeHeader);
	            csvFormatter.setFieldInternalSeparator(fieldInternalSeparator);
	            csvFormatter.setFieldSeparator(fieldSeparator);
	            csvFormatter.setOutputOptions(outputOptions);
	            csvFormatter.setWriter(out);
	            
	            csvFormatter.setSequenceDao(sequenceDao);
	            csvFormatter.format(results);
	            
        	}
        	
        	if (outputFormat == OutputFormat.HTML) {
	            
        		response.setContentType("text/html");
        		
	            FormatHTML htmlFormatter = new FormatHTML();
	            
	            htmlFormatter.setBmf(bmf);
	            htmlFormatter.setBlankField(blankField);
	            htmlFormatter.setHeader(includeHeader);
	            htmlFormatter.setFieldInternalSeparator(fieldInternalSeparator);
	            htmlFormatter.setOutputOptions(outputOptions);
	            htmlFormatter.setWriter(out);
	            
	            htmlFormatter.setSequenceDao(sequenceDao);
	            htmlFormatter.format(results);
	            
        	}
        	
        	
        	
        	if (outputFormat == OutputFormat.FASTA) {
        		
        		response.setContentType("text/plain");
        		
	            FormatFASTA fastaFormatter = new FormatFASTA();
	            
	            fastaFormatter.setBmf(bmf);
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
	            fastaFormatter.format(results);
	            
        	}
        	
        	
        	/*
        	 * Post formatting for different output destinations.   
        	 */
        	
        	if ((outputDestination == OutputDestination.TO_EMAIL) || (outputDestination == OutputDestination.TO_FILE))  {
        		
        		
            	out.close();
            	
            	
        		if (outputDestination == OutputDestination.TO_EMAIL) {
                	
        			File zipFile = zip(tabOutFile);
        			
                	try {
						sendEmail(email, historyItemName, "Please find attached your " + outputFormat.name() + " results." + description, zipFile);
						response.getWriter().append("Your email has been sent to " + email + ".");
					} catch (MessagingException e) {
						logger.error(e.getStackTrace().toString());
						response.getWriter().append("Could not send mail. " + e.getMessage());
					}
					if (deleteFiles) {
	    				zipFile.delete();
	     			}
                	
                } else if (outputDestination == OutputDestination.TO_FILE) {
                	
                	
                	response.setContentType("application/x-download");
                	response.setHeader("Content-Disposition", "attachment; filename="+tabOutFile.getName());
                	
                	OutputStream os = response.getOutputStream();
        			returnFile(tabOutFile, os);
        			
                }
        		
        		if (deleteFiles) {
    				tabOutFile.delete();
     			}
        	}
        	
        	
        	
        }
        	
        
        return null;
    }
	
	
	private File zip(File file) throws IOException {
		
		byte[] buf = new byte[1024]; 
		
		String zipFileName = file.getName() +".zip";
		
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		out.setLevel(Deflater.BEST_COMPRESSION);
    	
		FileInputStream in = new FileInputStream(file);
    	out.putNextEntry(new ZipEntry(file.getName()));
    	
    	int len; 
    	while ((len = in.read(buf)) > 0) { 
    		out.write(buf, 0, len); 
    	} 
    	
    	out.closeEntry(); 
    	in.close();
    	out.close();
    	
    	return new File(zipFileName);
    	
	}
    
    
    
    /*
     * Lifted from genedb classic.
     */
    private static void returnFile(File file, OutputStream out) throws FileNotFoundException, IOException {
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
    
    private void sendEmail(String to, final String subject, String text, File attachment) throws javax.mail.MessagingException {
    	
    	MimeMessage message = mailSender.createMimeMessage();
    	
    	MimeMessageHelper helper = new MimeMessageHelper(message, true);
    	helper.setTo(to);
    	helper.setFrom(new InternetAddress("webmaster@genedb.org"));
    	helper.setSubject("Your GeneDB query results - " + subject);
    	helper.setText(text, true);
    	
    	
    	
    	if (attachment != null) {
    		FileSystemResource file = new FileSystemResource(attachment);
        	helper.addAttachment(file.getFilename(), file);
    	}
    	
    	mailSender.send(message);
    	
    }
    
    private String determineFieldSeparator(String fieldSeparator, OutputFormat outputFormat) {
    	
    	if (fieldSeparator.equals("default")) {
    		switch (outputFormat) {
	        case CSV:
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




    
    
}

