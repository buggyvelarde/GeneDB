package org.genedb.web.mvc.controller;

import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.SmallMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class WebUtils {
	
	private static SequenceDao sequenceDao;
	
    public void setSequenceDao(SequenceDao sequenceDao) {
		WebUtils.sequenceDao = sequenceDao;
	}

	public static boolean extractTaxonOrOrganism(HttpServletRequest request, boolean required, boolean onlyOne, List<String> answers) {
	boolean problem = false;
	String[] ids = request.getParameterValues("taxId");
	String[] names = request.getParameterValues("org");
	List<String> idsAndNames = new ArrayList<String>();
	if (ids != null) idsAndNames.addAll(Arrays.asList(ids));
	if (names != null) idsAndNames.addAll(Arrays.asList(names));
	
	int length = idsAndNames.size();
	if (required && length==0) {
	    buildErrorMsg(request, "No taxon id (or organism name) supplied when expected");
	    problem = true;
	}
	if (onlyOne && length>1) {
	    buildErrorMsg(request, "Only expected 1 taxon id (or organism name)");
	    problem = true;
	}
			
			
	if (problem) {
	    return false;
	}
	//		if (errMsg == null) {
	//			request.setAttribute(errMsg, errMsg);
	//		}
	answers.addAll(idsAndNames);
	// TODO check required and onlyone
	// TODO store error message if necessary
	// TODO check if ids for which we have data - need new flag
	return true;
    }

    public static void buildErrorMsg(HttpServletRequest request, String msg) {
	List<String> stored = (List<String>) request.getAttribute(WebConstants.ERROR_MSG);
	if (stored == null) {
	    stored = new ArrayList<String>();
	}
	stored.add(msg);
	request.setAttribute(WebConstants.ERROR_MSG, stored);
    }
    
    public static String buildGViewerXMLFiles(List<Feature> displayFeatures,File tmpDir){
    	List<Feature> topLevels = sequenceDao.getTopLevelFeatures();
		OutputStream out = null;
		try {
			out = new FileOutputStream(tmpDir + "/sbase.xml");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

        Element genome = new Element("genome");
        
        int i = 0;
        int length = 0;
        for (Feature feature : topLevels) {
        	String chromosomeName = feature.getUniqueName();
            String chromosomeNumber = chromosomeName.substring(chromosomeName.length()-1);
        	i++;
			Element chromosome = new Element("chromosome");
			Element end = new Element("end");
			chromosome.setAttribute("index", Integer.toString(i));
			chromosome.setAttribute("number", chromosomeNumber);
			chromosome.setAttribute("length", Integer.toString(feature.getSeqLen()));
			if (feature.getSeqLen() > length) {
				length = feature.getSeqLen();
			}
			end.setText(Integer.toString(feature.getSeqLen()));

			Element band = new Element("band");
			band.setAttribute("index", "1");
			band.setAttribute("name", "1");
			Element start = new Element("start");
			start.setText("0");
			Element stain = new Element("stain");
			stain.setText("gneg");
			band.addContent(start);
			band.addContent(end);
			band.addContent(stain);
			chromosome.addContent(band);
			genome.addContent(chromosome);
		}
        
        Document doc = new Document(genome);
        XMLOutputter xmlout = new XMLOutputter();
        xmlout.setFormat(Format.getPrettyFormat());
        try {
			xmlout.output(doc, out);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        try {
			out = new FileOutputStream(tmpDir + "/003.xml");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        genome = new Element("genome");
        
        for (Feature feature : displayFeatures) {
			Element XMLfeature = new Element("feature");
			Element chromosome = new Element("chromosome");
			Element chrstart = new Element("start");
			Element chrend = new Element("end");
			Element type = new Element("type");
			Element color = new Element("colour");
			Element label = new Element("label");
			Element link = new Element("link");
			
			Collection<FeatureLoc> temp = feature.getFeatureLocsForFeatureId();
			for (FeatureLoc fl : temp) {
				String name = fl.getFeatureBySrcFeatureId().getUniqueName();
				String number = name.substring(name.length()-1);
				chromosome.setText(number);
				chrstart.setText(fl.getFmin().toString());
				chrend.setText(fl.getFmax().toString());
			}
			
			type.setText("gene");
			color.setText("ox79cc3d");
			label.setText(feature.getUniqueName());
			link.setText("http://holly.internal.sanger.ac.uk:8080/genedb-web/NameFeature?lookup="+feature.getUniqueName());
			
			XMLfeature.addContent(chromosome);
			XMLfeature.addContent(chrstart);
			XMLfeature.addContent(chrend);
			XMLfeature.addContent(type);
			XMLfeature.addContent(color);
			XMLfeature.addContent(label);
			XMLfeature.addContent(link);
			genome.addContent(XMLfeature);
		}
        
        doc = new Document(genome);
        xmlout = new XMLOutputter();
        xmlout.setFormat(Format.getPrettyFormat());
        try {
			xmlout.output(doc, out);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        
        return Integer.toString(length);
    }
    
    public static int featureListSize(String cvTermName, String cvName){
    	int size = 0;
    	List<Feature> temp = sequenceDao.getFeaturesByCvTermNameAndCvName(cvTermName,cvName);
    	if (temp != null){
    		return temp.size();
    	}
    	return size;
    }
    
//    public static double getCharge(SymbolList aaSymList){
//    	Map chargeFor;
//    	double charge = 0.0;
//        
//        
//        try {
//            Alphabet aa = ProteinTools.getAlphabet();
//            SymbolTokenization aaToken = aa.getTokenization("token");
//
//            chargeFor = new SmallMap();
//
//            // A,C,F,G,I,L,M,N,P,Q,S,T,U,V,W,X,Y are all zero
//            chargeFor.put(aaToken.parseToken("B"), new Double(-0.5));
//            chargeFor.put(aaToken.parseToken("D"), new Double(-1));
//            chargeFor.put(aaToken.parseToken("E"), new Double(-1));
//            chargeFor.put(aaToken.parseToken("H"), new Double(0.5));
//            chargeFor.put(aaToken.parseToken("K"), new Double(1));
//            chargeFor.put(aaToken.parseToken("R"), new Double(1));
//            chargeFor.put(aaToken.parseToken("Z"), new Double(-0.5));
//        }
//        catch(IllegalSymbolException e){
//            e.printStackTrace();
//            throw new RuntimeException("Unexpected biojava error - illegal symbol");
//        }
//        catch(BioException e){
//            e.printStackTrace();
//            throw new RuntimeException("Unexpected biojava error - general");
//        }
//        
//        Map counts = residueCount(aaSymList,chargeFor);
//        // iterate thru' all counts computing the partial contribution to charge
//        Iterator countI = counts.keySet().iterator();
//
//        while (countI.hasNext()) {
//            Symbol sym = (Symbol) countI.next();
//            Double chargeValue = (Double) chargeFor.get(sym);
//
//            double symbolsCharge = chargeValue.doubleValue();
//            int count = ((Integer) counts.get(sym)).intValue();
//
//            charge += (symbolsCharge * count);
//        }
//        return charge;
//    }
//
//
//    private static Map residueCount(SymbolList aaSymList,Map chargeFor) {
//        // iterate thru' aaSymList collating number of relevant residues
//        Iterator residues = aaSymList.iterator();
//
//        Map symbolCounts = new SmallMap();
//
//        while (residues.hasNext()) {
//            Symbol sym =  (Symbol) residues.next();
//
//            if (chargeFor.containsKey(sym)) {
//                Integer currCount = (Integer) symbolCounts.get(sym);
//
//                if (currCount != null) {
//                    currCount = currCount + 1;
//                } else {
//                    symbolCounts.put(sym, new Integer(1));
//                }
//            }
//        }
//        return symbolCounts;
//    }
}
