package org.genedb.web.mvc.controller.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.genedb.querying.core.QueryException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:testContext-query.xml", "classpath:Download.xml"})
public class DownloadTest {
	
	

	@Autowired
	private DownloadProcessUtil util;
	
	OutputFormat outputFormat = OutputFormat.FASTA;
	String[] custFields = new String[] {};
	OutputDestination outputDestination = OutputDestination.TO_FILE;
	boolean includeHeader = true;
	String fieldSeparator = "|";
	String blankField = " -- ";
	String fieldInternalSeparator = ",";
	int prime3 = 0;
	int prime5 = 0;
	String email = "gv1@sanger.ac.uk";
	List<String> uniqueNames = Arrays.asList(new String[] {"MAL8P1.300"});
	String historyItemName = "testing";
	String description = " testing 123 ";
	
	@Test
	public void test1() throws QueryException, IOException {
		
		SequenceType sequenceType = SequenceType.UNSPLICED_DNA;
		String sequence = "ATGAAGATTAATATATTGAAGAAAGGCAAAAAATTTTATATTACAAACAATCATTTTAATTATGATATTAAACGAAATTTTACAATATTTCAAAACTCATTTATAAAAACAAACGATATAGTTTATAGAAAAAACATTGATATTGTTTGTGCAAAAGATTTATTCTTTTATACAATTCTAAATGTAGATAGATATAAGTATTTTCTACCATATGTAACGGTAAGCATAAATATATTCATATTTTTAAAACGTATGCGGATGTTGTTTATGTGTGTACATATATTTGTTCATATAAATTGTTACATTTGTTCATATAAATTGTTACATTTGTTCATGTAAATTGTTATATTTGTTCATATAAATTGTTACATTTGTTCATGTAAATTGTTATATTTGTTCATGTAAATTGTTACATTTGTTCATGTAAATTGTTATATTTGTTCATGTAAATTGTTACATTTGTTCATGTAAATTGTTACATTTATTCATGTAAATTGTTACATTTGTTCATGTAAATTGTTACATTTATTCATGTAAATTGTTACATTTGTTCATGTAAATTGTTATATTTGTTCATGTAAAATTTTATATTTGTTCATATATTTTTGCACATTTATTTTTTAAAAATTATGAGCAATTGTATTGTATATTTGTAGGATAGCAAGATAACAGAAAAAAACAAAGAATATTTTAAAGCCAATTTACAAATTGAGAATATTTTCTTTAAAGAAAAATATGACTCTTTAATTCAATTCATTTACCCAACAACAATTACGGTAATAAGACTTTAAAGAGGAAATAAATGAATGAGAAGAAATATAAGTATATTTCGTATATAAATGTGTAAATATATACATACATATATATATATATATATATATATATATATATATATATAAATACTTAGACATTGTTGCATTTTTTTATTTTATTTTATTTTATTTTATTTTATTTAGGTATCTAGCGAAGATACAAATATTTTTCATCACTTGGTATGTAATAATAATAAAGAGACAACTAATTAAAAATATAAACAGAAAAATTAATATATATACTTATATATATTTTAAATAAGGCACATATCGTGTGTCCATTATAATATATATATATATATATATATATTGTTCTTTTTATAGATAACCGAGTGGATAATTAAAGAAAAAAAGAATTGCATAAACATTGATTTTTATATAAATTTTAGGGTAACAAAAATGTTGTATATATATTATATATGTATATATGTGTGTGTGTAAATATACTATATTTTATGTATCTGTGAATTTCTCAAAATTTTAATAAGTTTACATTATTCATAAAGTATTCACTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTCTGCTTAGTTGAAAAATAAAATATATCAAAATTTTATGAATTTATACATAAAAGAGTTAGGGAAAAAAATTTTATATTCCTTTATAAATGAATCAAAATCAAACAGTTATAAAAACACGGACGTTTTACACTTGATAAAATAG";
		
		File file = new File("/tmp/test1.fasta");
		runProcess(file, sequenceType);
		checkSequence(file, sequence);
		
	}
	
	@Test
	public void test2() throws QueryException, IOException {
		SequenceType sequenceType = SequenceType.PROTEIN;
		String sequence = "MKINILKKGKKFYITNNHFNYDIKRNFTIFQNSFIKTNDIVYRKNIDIVCAKDLFFYTILNVDRYKYFLPYVTVSINIFIFLKRMRMLFMCVHIFVHINCYICSYKLLHLFM#IVIFVHINCYICSCKLLYLFM#IVTFVHVNCYICSCKLLHLFM#IVTFIHVNCYICSCKLLHLFM#IVTFVHVNCYICSCKILYLFIYFCTFIF#KL*AIVLYICRIAR#QKKTKNILKPIYKLRIFSLKKNMTL#FNSFTQQQLR##DFKEEINE*EEI#VYFVYKCVNIYIHIYIYIYIYIYIYKYLDIVAFFYFILFYFILFRYLAKIQIFFITWYVIIIKRQLIKNINRKINIYTYIYFK#GTYRVSIIIYIYIYIYCSFYR#PSG#LKKKRIA#TLIFI#ILG#QKCCIYIIYVYMCVCKYTIFYVSVNFSKF##VYIIHKVFTFFFFFFFFFFSA+LKNKIYQNFMNLYIKELGKKILYSFINESKSNSYKNTDVLHLIK";
		File file = new File("/tmp/test2.fasta");
		runProcess(file, sequenceType);
		checkSequence(file, sequence);
	}
	
	
	private void runProcess(File file, SequenceType sequenceType) throws QueryException, IOException {
		
		DownloadProcess process = new DownloadProcess(
				outputFormat, 
				custFields, 
				outputDestination, 
				sequenceType, 
				includeHeader, 
				fieldSeparator, 
				blankField, 
				fieldInternalSeparator, 
				0, 
				0, 
				email, 
				uniqueNames, 
				historyItemName, 
				description, 
				util);
		
		
		process.generateFASTA(file);
		
	}
	
	private void checkSequence(File file, String sequence) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String s;
		StringBuffer sb = new StringBuffer();
		while((s = reader.readLine()) != null) {
			
			if (! s.startsWith(">")) {
				
				if (s.contains("*")) {
					s = s.replace("*", "");
				}
				
				sb.append(s.trim());
			}
			
		}
		
		reader.close();
		
		System.out.println(sb.toString());
		
		Assert.assertEquals(sb.toString().toUpperCase(), sequence);
	}
	
}
