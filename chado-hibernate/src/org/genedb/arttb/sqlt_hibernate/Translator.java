package org.genedb.arttb.sqlt_hibernate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.genedb.arttb.sqlt_hibernate.rdbms.Database;
import org.xml.sax.SAXException;

public class Translator {

	private String DIGESTOR_MAPPING = "resources/digester-rules.xml";
	private String inputFile;
	private String outputDir;
	
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public void process() throws IOException, SAXException {
		
		File input = new File( inputFile );
		File rules = new File( DIGESTOR_MAPPING );

		Digester digester = DigesterLoader.createDigester( rules.toURL() );

		Database database = (Database)digester.parse( input );
		
		File sqlt = new File("ant-build/data/gmod-0.003-rc1/sqlt/sqlt.xml");
		PrintWriter pw = new PrintWriter(new FileWriter(sqlt));
		//File hibernate = new File("ant-build/data/gmod-0.003-rc1/hibernate/combined.hbm.xml");
		File hibernate = new File("ant-build/data/gmod-0.003-rc1/hibernate");
		//PrintWriter hw = new PrintWriter(new FileWriter(hibernate));
		
		database.outputItem(OutputStyle.SQLT_XML, pw, null);
		database.outputItem(OutputStyle.HIBERNATE, null, hibernate);
		
	}
	
	
	public static void main(String[] args) throws IOException, SAXException {
		Translator t = new Translator();
		t.setInputFile(args[0]);
		t.setOutputDir(args[1]);
		t.process();
		System.out.println("Translation done");
	}
	
}
