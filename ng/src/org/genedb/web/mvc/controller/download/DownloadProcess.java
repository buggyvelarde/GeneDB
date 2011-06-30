package org.genedb.web.mvc.controller.download;

import java.io.File;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.tmpquery.GeneDetail;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;


public class DownloadProcess {
	
	private static Logger logger = Logger.getLogger(DownloadProcess.class);
	
	OutputFormat outputFormat = OutputFormat.CSV;
	//String[] custFields = new String[0];
	OutputDestination outputDestination = OutputDestination.TO_EMAIL;
	SequenceType sequenceType = SequenceType.UNSPLICED_DNA;
	boolean includeHeader = true;
	String fieldSeparator = "\t";
	String blankField = "--";
	String fieldInternalSeparator;
	int prime3;
	int prime5;
	String email;
	
	List<OutputOption> outputOptions = new ArrayList<OutputOption>();
	List<GeneDetail> results;
	List<String> uniqueNames;
	String historyItemName;
	String description;
	
	String url;
	
	//private BerkeleyMapFactory bmf;
	private SequenceDao sequenceDao;
	
    private DownloadProcessUtil util;
    
    
    
    interface IDownloadProcess { 
    	
    	@Option(shortName="f", longName="outputFormat", description="The format for generating output.")
    	OutputFormat getOutputFormat();
        void setOutputFormat(OutputFormat outputFormat);
        
        @Option(shortName="c", longName="custFields", description="The fields to be exported.")
    	List<String> getCustFields();
        void setCustFields(List<String> custFields);
        
        @Option(shortName="d", longName="outputDestination", description="Whether to send it to the browser, the file or to an email.")
    	OutputDestination getOutputDestination();
        void setOutputDestination(OutputDestination outputDestination);
        
        @Option(shortName="t", longName="sequenceType", description="The kind of sequence.")
    	SequenceType getSequenceType();
        void setSequenceType(SequenceType sequenceType);
        
        @Option(shortName="h", longName="includeHeader", description="Whether or not to include a header line.")
    	boolean getIncludeHeader();
        void setIncludeHeader(SequenceType includeHeader);
        
        @Option(shortName="s", longName="fieldSeparator", description="The field separator.")
    	String getFieldSeparator();
        void setFieldSeparator(String fieldSeparator);
        
        @Option(shortName="b", longName="blankField", description="The blank field value.")
    	String getBlankField();
        void setBlankField(String blankField);
        
        @Option(shortName="i", longName="fieldInternalSeparator", description="The internal field separator.")
    	String getFieldInternalSeparator();
        void setFieldInternalSeparator(String fieldInternalSeparator);
        
        @Option(shortName="3", longName="prime3", description="The prime3 value.")
    	int getPrime3();
        void setPrime3(int prime3);
        
        @Option(shortName="5", longName="prime5", description="The prime5 value.")
    	int getPrime5();
        void setPrime5(int prime3);
        
        @Option(shortName="e", longName="email", description="The email address.")
    	String getEmail();
        void setEmail(String email);
        
        @Option(shortName="u", longName="uniqueNames", description="The unique names to be exported.")
    	List<String> getUniqueNames();
        void setUniqueNames(List<String> uniqueNames);
        
        @Option(shortName="n", longName="historyItemName", description="The name of the history item.")
    	String getHistoryItemName();
        void setHistoryItemName(String historyItemName);
        
        @Option(shortName="ds", longName="description", description="The HTML description of the query.")
    	String getDescription();
        void setDescription(String description);
        
        @Option(shortName="r", longName="url", description="The base url of the site.")
    	String getUrl();
        void setUrl(String url);
        
    }
	
	/**
	 * @param args
	 * @throws ArgumentValidationException 
	 * @throws ArgumentValidationException 
	 * @throws MessagingException 
	 * @throws IOException 
	 * @throws QueryException 
	 * @throws QueryException 
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public static void main(String[] args) throws ArgumentValidationException, QueryException, IOException, MessagingException  {
		
		PropertyConfigurator.configure("resources/classpath/log4j.download.properties");
		
		ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] {"classpath:Load.xml", "classpath:Download.xml"});
		
		try {
			
			IDownloadProcess cliArgs = CliFactory.parseArguments(IDownloadProcess.class, args);
			DownloadProcess process = new DownloadProcess(cliArgs, ctx.getBean("downloadProcessUtil", DownloadProcessUtil.class));
			
			
			logger.debug(cliArgs.getOutputFormat());
			logger.debug(cliArgs.getCustFields());
			logger.debug(cliArgs.getOutputDestination());
			logger.debug(cliArgs.getSequenceType());
			logger.debug(cliArgs.getIncludeHeader());
			logger.debug(cliArgs.getFieldSeparator());
			logger.debug(cliArgs.getBlankField());
			logger.debug(cliArgs.getFieldInternalSeparator());
			logger.debug(cliArgs.getPrime3());
			logger.debug(cliArgs.getPrime5());
			logger.debug(cliArgs.getEmail());
			logger.debug(cliArgs.getUniqueNames());
			logger.debug(cliArgs.getHistoryItemName());
			logger.debug(cliArgs.getDescription());
			
			logger.debug(cliArgs.getUrl());
			
			
			if (process.outputDestination == OutputDestination.TO_BROWSER) {
				logger.debug("Cannot send to browser from command line.");
				System.exit(1);
			}
			
			if (process.outputDestination == OutputDestination.TO_EMAIL && process.email == null) {
				logger.debug("You must supply an email address.");
				System.exit(1);
			}
			
			process.run();
			
		} catch (ArgumentValidationException e) {
			logger.debug(e.getMessage());
			System.exit(1);
		}
		
		
	}
	
	public void run() throws QueryException, IOException, MessagingException {
		
		String fileName = historyItemName + "." + util.getTime() + "." + outputFormat.name().toLowerCase();
        String filePath = util.gettDownloadTmpFolder() + "/" + fileName;
        
        File file = new File(filePath);
		
		switch (outputFormat) {
		case CSV:
			generateCSV(file);
			break;
		case HTML:
			generateHTML(file);
			break;
		case FASTA:
			generateFASTA(file);
			break;
		case XLS:
			generateXLS(file);
			break;
		}
		
		System.gc();
		
		File zipFile = util.zip(file);
		
		if (outputDestination == OutputDestination.TO_EMAIL) {
			util.sendEmail(email, historyItemName, "Please find your " + outputFormat.name() + " results." + description, zipFile, url);
		}
		
		// don't need to keep the unzipped version
		file.delete();
		
	}
	
	
	
	/**
	 * Constructor for use in command line request.
	 * 
	 * @param args
	 */
    public DownloadProcess(IDownloadProcess args, DownloadProcessUtil util) {
		this.outputFormat = args.getOutputFormat();
		
		for (String custField : args.getCustFields()) {
            outputOptions.add(OutputOption.valueOf(custField));
        }
		
		this.outputDestination = args.getOutputDestination();
		this.sequenceType = args.getSequenceType();
		this.includeHeader = args.getIncludeHeader();
		this.fieldSeparator = determineFieldSeparator(args.getFieldSeparator(), outputFormat);
		this.blankField = args.getBlankField();
		this.fieldInternalSeparator = args.getFieldInternalSeparator();
		this.prime3 = args.getPrime3();
		this.prime5 = args.getPrime5();
		this.email = args.getEmail();
		this.uniqueNames = args.getUniqueNames();
		this.historyItemName = args.getHistoryItemName();
		this.description = args.getDescription();
		this.url = args.getUrl();
		
		this.util = util;
		
		sequenceDao = util.getSequenceDao();
	}
    
	/**
	 * Constructor for use in web request.
	 * 
	 * @param outputFormat
	 * @param custFields
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
	 * @throws QueryException
	 */
	public DownloadProcess( 
			OutputFormat outputFormat,
			String[] custFields,
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
			DownloadProcessUtil util,
			String url
			) throws QueryException {
		
		this.outputFormat = outputFormat;
		
		for (String custField : custFields) {
            outputOptions.add(OutputOption.valueOf(custField));
        }
		
		this.outputDestination = outputDestination;
		this.sequenceType = sequenceType;
		this.includeHeader = includeHeader;
		
		this.fieldSeparator = determineFieldSeparator(fieldSeparator, outputFormat);
		
		this.blankField = blankField;
		this.fieldInternalSeparator = fieldInternalSeparator;
		this.prime3 = prime3;
		this.prime5 = prime5;
		this.email = email;
		this.uniqueNames = uniqueNames;
		this.historyItemName = historyItemName;
		this.description = description;
		
		this.util = util;
		
		sequenceDao = util.getSequenceDao();
		
		this.url = url;
	}
	
	
	public void generateXLS(File file) throws IOException, QueryException {
		OutputStream stream = new FileOutputStream(file);
		generateXLS(stream);
	}
	
	public void generateXLS(OutputStream stream) throws IOException, QueryException {
		
		results = util.getResults(uniqueNames);
		
		FormatExcel excelFormatter = new FormatExcel();
    	
    	excelFormatter.setFieldInternalSeparator(fieldInternalSeparator);
    	excelFormatter.setOutputOptions(outputOptions);
    	
    	excelFormatter.setOutputStream(stream);
    	
    	excelFormatter.setSequenceDao(sequenceDao);
    	excelFormatter.setTransactionTemplate(util.getTransactionTemplate());
    	excelFormatter.format(results);
	}
	
	public void generateCSV(File file) throws IOException, QueryException {
		Writer writer = new FileWriter(file);
		generateCSV(writer);
		writer.close();
	}
	
	public void generateCSV(Writer writer) throws IOException, QueryException {
		
		results = util.getResults(uniqueNames);
		
		FormatCSV csvFormatter = new FormatCSV();
        
        csvFormatter.setBlankField(blankField);
        csvFormatter.setHeader(includeHeader);
        csvFormatter.setFieldInternalSeparator(fieldInternalSeparator);
        csvFormatter.setFieldSeparator(fieldSeparator);
        csvFormatter.setOutputOptions(outputOptions);
        csvFormatter.setWriter(writer);
        
        csvFormatter.setSequenceDao(sequenceDao);
        csvFormatter.setTransactionTemplate(util.getTransactionTemplate());
        csvFormatter.format(results);
        
	}
	
	public void generateHTML(File file) throws IOException, QueryException {
		Writer writer = new FileWriter(file);
		generateHTML(writer);
		writer.close();
	}
	
	public void generateHTML(Writer writer) throws IOException, QueryException {
		
		results = util.getResults(uniqueNames);
		
		FormatHTML htmlFormatter = new FormatHTML();
        
        htmlFormatter.setBlankField(blankField);
        htmlFormatter.setHeader(includeHeader);
        htmlFormatter.setFieldInternalSeparator(fieldInternalSeparator);
        htmlFormatter.setOutputOptions(outputOptions);
        htmlFormatter.setWriter(writer);
        
        htmlFormatter.setSequenceDao(sequenceDao);
        htmlFormatter.setTransactionTemplate(util.getTransactionTemplate());
        htmlFormatter.format(results);
	}
	
	public void generateFASTA(File file) throws IOException, QueryException {
		Writer writer = new FileWriter(file);
		generateFASTA(writer);
		writer.close();
	}
	
	public void generateFASTA(Writer writer) throws IOException, QueryException {
		
		results = util.getResults(uniqueNames);
		
		FormatFASTA fastaFormatter = new FormatFASTA();
        
        fastaFormatter.setBlankField(blankField);
        fastaFormatter.setHeader(includeHeader);
        fastaFormatter.setFieldInternalSeparator(fieldInternalSeparator);
        fastaFormatter.setFieldSeparator(fieldSeparator);
        fastaFormatter.setOutputOptions(outputOptions);
        fastaFormatter.setWriter(writer);
        
        fastaFormatter.setPrime3(prime3);
        fastaFormatter.setPrime5(prime5);
        fastaFormatter.setSequenceType(sequenceType);
        
        fastaFormatter.setSequenceDao(sequenceDao);
        fastaFormatter.setTransactionTemplate(util.getTransactionTemplate());
        fastaFormatter.format(results);
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
