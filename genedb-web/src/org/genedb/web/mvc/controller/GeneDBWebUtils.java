package org.genedb.web.mvc.controller;

import org.genedb.db.dao.SequenceDao;
import org.genedb.web.gui.ContextMap;
import org.genedb.web.gui.ImageInfo;
import org.genedb.web.gui.LocationColourPair;
import org.genedb.web.gui.MinimalSymbolList;
import org.genedb.web.gui.RNASummary;
import org.genedb.web.gui.TransientSequence;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
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

public class GeneDBWebUtils {
	
	private static SequenceDao sequenceDao;
	
    public void setSequenceDao(SequenceDao sequenceDao) {
		GeneDBWebUtils.sequenceDao = sequenceDao;
	}

    public static boolean extractTaxonNodesFromRequest(HttpServletRequest request, List<String> answers, boolean required, boolean onlyOne) {
        boolean problem = false;

        String[] names = request.getParameterValues("org");
        
        List<String> idsAndNames = new ArrayList<String>();
        if (names != null) {
            for (String entry : names) {
                String[] entries = entry.split(":");
                idsAndNames.addAll(Arrays.asList(entries));
            }
        }

        String msg = GeneDBWebUtils.validateTaxons(idsAndNames);
        
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
    
    public static ImageInfo drawContextMap(Feature gene, String prefix) {

    	ImageInfo info = null;
    	FeatureLoc location = gene.getFeatureLocsForFeatureId().iterator().next();

    	StrandedFeature.Strand f = StrandedFeature.NEGATIVE;
    	if( location.getStrand() == 1) {
    		f = StrandedFeature.POSITIVE;
    	}
    	
    	LocationColourPair lcp = getExonLocations(gene);
    	RNASummary target = new RNASummary(gene.getDisplayName(),gene.getUniqueName(), lcp.location,
    			"CDS",f,gene.getOrganism().getCommonName(),"", lcp.colour);
    	
    	List<RNASummary> rnas = getNeighbours(gene,target);
   
    	int bottomIndex = 0;
        int topIndex = rnas.size()-1;
        
        int min = ((RNASummary)rnas.get(bottomIndex)).getLocation().getMin();
        int max = ((RNASummary)rnas.get(topIndex)).getLocation().getMax();


        Sequence seq = null;

        try {
            FiniteAlphabet peptide =
                (FiniteAlphabet) AlphabetManager.alphabetForName("DNA");
            //SymbolTokenization tkizer = peptide.getTokenization("token");

//            String aaString = StringUtils.repeat("A", rna.getContigLength());
            SymbolList sl = new MinimalSymbolList(peptide, getContigLength(gene));


            seq = new TransientSequence(sl, "URN", target.getId(), null);
            try {
				seq.getAnnotation().setProperty("organism", target.getOrganism());
			} catch (IllegalArgumentException e) {
				// Should never happen
				e.printStackTrace();
			} catch (ChangeVetoException e) {
				// Should never happen
				e.printStackTrace();
			}


            // Need to add features to sequence...
            for (int i= bottomIndex; i <= topIndex; i++) {
                RNASummary rs = (RNASummary) rnas.get(i);
                
                StrandedFeature.Template ft = new StrandedFeature.Template();
                ft.type = "CDS";
                ft.location = rs.getLocation();
                System.err.println(rs.getId()+"\t"+rs.getLocation().getMin()+"..."+rs.getLocation().getMax());
                ft.strand = rs.getStrand();
                Annotation an = new SimpleAnnotation();
                try {
					an.setProperty("Tooltip", rs.getDescription());
					an.setProperty("name", rs.getName());
	    			an.setProperty("systematic_id", rs.getId());
	    			an.setProperty("colour", rs.getColour());
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (ChangeVetoException e) {
					e.printStackTrace();
				}
    			
    			ft.annotation = an;
                try {
					seq.createFeature(ft);
				} catch (ChangeVetoException e) {
					e.printStackTrace();
				}
            }
            
            File file = new File("/software/pathogen/projects/genedb/tomcat_workshop/tomcat/webapps/"+prefix+"includes/images/cmap/" + gene.getUniqueName() + ".gif");
            //System.err.println("Writing image to '"+file.getAbsolutePath()+"'");

            OutputStream out = null;
			try {
				out = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            ContextMap contextMap = new ContextMap();
            ImageInfo ii = new ImageInfo();
            
            //info = contextMap.drawMap(seq, ii, min,max,target.getId(),false,out);
            info = contextMap.drawMap(seq, ii, min,max,target.getId(),false,out);
        }
        catch (BioException exp) {
        	exp.printStackTrace();
        }
        System.err.println("### The imagrmap just before return is '"+info.contextMapData+"'");
        return info;
    }	
    
    private static LocationColourPair getExonLocations(Feature gene) {
    	LocationColourPair ret = new LocationColourPair();
    	ret.colour = 5;
    	Collection<Location> locs = new ArrayList<Location>();
	    	
		Feature mRNA = gene.getFeatureRelationshipsForObjectId().iterator().next().getFeatureBySubjectId();
		Iterator i = mRNA.getFeatureRelationshipsForObjectId().iterator();
		while(i.hasNext()) {
			FeatureRelationship fr = (FeatureRelationship) i.next();
			if(fr.getCvTerm().getName().equals("part_of")) {
				Iterator it = fr.getFeatureBySubjectId().getFeatureLocsForFeatureId().iterator();
				FeatureLoc loc = (FeatureLoc)it.next();
				int min = loc.getFmin();
				int max = loc.getFmax();
				Location location = new RangeLocation(min,max);
				locs.add(location);
			}
			if(fr.getCvTerm().getName().equals("derives_from")) {
				Feature polypeptide = fr.getFeatureBySubjectId();
				for (FeatureProp fp: polypeptide.getFeatureProps()) {
					if (fp.getCvTerm().getName().equals("colour")) {
						//try {
						ret.colour = Integer.parseInt(fp.getValue());
						break;
						//}
					}
				}
			}	
		}
    	
		switch (locs.size()) {
		case 0:
			throw new RuntimeException("Data problem - no exons found for '"+gene.getUniqueName()+"'");
		case 1:
			Location loc = (Location)locs.iterator().next();
			ret.location = loc;
		default:
			ret.location = LocationTools.union(locs);
		}
    	return ret;
	}

	private static int getContigLength(Feature gene) {
    	FeatureLoc loc = gene.getFeatureLocsForFeatureId().iterator().next();
    	int len = loc.getFeatureBySrcFeatureId().getSeqLen();
		return len;
	}

	private static List<RNASummary> getNeighbours(Feature gene, RNASummary target) {

    	List<RNASummary> rnas = new ArrayList<RNASummary>();
    	FeatureLoc location = gene.getFeatureLocsForFeatureId().iterator().next();
    	int min = location.getFmin();
    	int newMin = min - 13000;
    	if (newMin < 0) newMin = 0;
    	int max = location.getFmax();
    	System.err.println("min and max are " + min + " " + newMin);
    	Feature parent = location.getFeatureBySrcFeatureId();
    	List<Feature> features = sequenceDao.getFeaturesByLocation(newMin, min - 1, "gene", 
    			gene.getOrganism().getCommonName(), parent);
    	for (Feature feature : features) {
    		
    		StrandedFeature.Strand t = null;
    		FeatureLoc loc = feature.getFeatureLocsForFeatureId().iterator().next();
    		if(loc.getStrand() == 1) {
        		t = StrandedFeature.POSITIVE;
        	} else {
        		t = StrandedFeature.NEGATIVE;
        	}
    		
    		LocationColourPair lcp = getExonLocations(feature);
    		RNASummary temp = new RNASummary(feature.getDisplayName(), feature.getUniqueName(), 
    				lcp.location, "CDS", t, feature.getOrganism().getCommonName(), "", lcp.colour);
    		rnas.add(temp);
		}
    	rnas.add(target);
    	int newMax = max + 13000;
    	features = sequenceDao.getFeaturesByLocation(max + 1, newMax, "gene" , 
    			gene.getOrganism().getCommonName(), parent);
    	for (Feature feature : features) {
    		

    		FeatureLoc loc2 = feature.getFeatureLocsForFeatureId().iterator().next();
    		StrandedFeature.Strand t = StrandedFeature.NEGATIVE;
    		if(loc2.getStrand() == 1) {
        		t = StrandedFeature.POSITIVE;
        	}
    		
    		LocationColourPair lcp = getExonLocations(feature);
    		RNASummary temp = new RNASummary(feature.getDisplayName(), feature.getUniqueName(), lcp.location,
        			"CDS", t, feature.getOrganism().getCommonName(), "", lcp.colour);
    		rnas.add(temp);
		}
    	return rnas;
	}
	
	
	private static String validateTaxons(List<String> idsAndNames) {
        // TODO 
        return null;
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
			link.setText("http://developer.genedb.org/new/NameFeature?lookup="+feature.getUniqueName());
			
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
    
    // TODO Remove me once charge is stored by mining code
    public static double getCharge(SymbolList aaSymList){
    	Map chargeFor;
    	double charge = 0.0;
        
        
        try {
            Alphabet aa = ProteinTools.getAlphabet();
            SymbolTokenization aaToken = aa.getTokenization("token");

            chargeFor = new SmallMap();

            // A,C,F,G,I,L,M,N,P,Q,S,T,U,V,W,X,Y are all zero
            chargeFor.put(aaToken.parseToken("B"), new Double(-0.5));
            chargeFor.put(aaToken.parseToken("D"), new Double(-1));
            chargeFor.put(aaToken.parseToken("E"), new Double(-1));
            chargeFor.put(aaToken.parseToken("H"), new Double(0.5));
            chargeFor.put(aaToken.parseToken("K"), new Double(1));
            chargeFor.put(aaToken.parseToken("R"), new Double(1));
            chargeFor.put(aaToken.parseToken("Z"), new Double(-0.5));
        }
        catch(IllegalSymbolException e){
            e.printStackTrace();
            throw new RuntimeException("Unexpected biojava error - illegal symbol");
        }
        catch(BioException e){
            e.printStackTrace();
            throw new RuntimeException("Unexpected biojava error - general");
        }
        
        Map counts = residueCount(aaSymList,chargeFor);
        // iterate thru' all counts computing the partial contribution to charge
        Iterator countI = counts.keySet().iterator();

        while (countI.hasNext()) {
            Symbol sym = (Symbol) countI.next();
            Double chargeValue = (Double) chargeFor.get(sym);

            double symbolsCharge = chargeValue.doubleValue();
            int count = ((Integer) counts.get(sym)).intValue();

            charge += (symbolsCharge * count);
        }
        return charge;
    }


    private static Map residueCount(SymbolList aaSymList,Map chargeFor) {
        // iterate thru' aaSymList collating number of relevant residues
        Iterator residues = aaSymList.iterator();

        Map symbolCounts = new SmallMap();

        while (residues.hasNext()) {
            Symbol sym =  (Symbol) residues.next();

            if (chargeFor.containsKey(sym)) {
                Integer currCount = (Integer) symbolCounts.get(sym);

                if (currCount != null) {
                    currCount = currCount + 1;
                } else {
                    symbolCounts.put(sym, new Integer(1));
                }
            }
        }
        return symbolCounts;
    }
}
