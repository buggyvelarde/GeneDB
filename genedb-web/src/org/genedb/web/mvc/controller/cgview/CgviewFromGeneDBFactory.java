package org.genedb.web.mvc.controller.cgview;

import static ca.ualberta.stothard.cgview.CgviewConstants.DIRECT_STRAND;

import org.genedb.db.dao.SequenceDao;
import org.genedb.web.mvc.controller.ArtemisColours;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.ualberta.stothard.cgview.Cgview;
import ca.ualberta.stothard.cgview.Feature;
import ca.ualberta.stothard.cgview.FeatureRange;
import ca.ualberta.stothard.cgview.FeatureSlot;
import ca.ualberta.stothard.cgview.Legend;

/**
 * This class reads an XML document and creates a Cgview object. The various elements and attributes in the file are
 * used to describe sequence features (position, type, name, color, label font, and opacity). Optional XML attributes
 * can be included, to control global map characteristics, and to add legends, a title, and footnotes.
 *
 * @author Paul Stothard
 */

public class CgviewFromGeneDBFactory {

    private static final String contextPath = "/";

    private final static int MAX_BASES = 10000000;
    private final static int MIN_BASES = 10;
    private final static double MIN_BACKBONE_RADIUS = 10.0d;
    private final static double MAX_BACKBONE_RADIUS = 12000.0d;  //default is 190.0d
    private final static int MAX_IMAGE_WIDTH = 30000; //default is 700.0d
    private final static int MIN_IMAGE_WIDTH = 100;

    private final static int MAX_IMAGE_HEIGHT = 30000; //default is 700.0d
    private final static int MIN_IMAGE_HEIGHT = 100;

    private Pattern fontDescriptionPattern = Pattern.compile("\\s*(\\S+)\\s*,\\s*(\\S+)\\s*,\\s*(\\d+)\\s*,*\\s*");
    private Pattern colorDescriptionPattern = Pattern.compile("\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,*\\s*");
    private Matcher m;

    private int cgviewLength;
//    private int imageWidth;
//    private int imageHeight;

    //set to true if combining data
//    private boolean ignoreCgviewTag = false;
//    private boolean ignoreLegendTag = false;
//    private boolean ignoreLegendItemTag = false;

    private Cgview currentCgview;
    private FeatureSlot currentFeatureSlot;
    private Feature currentFeature;
    private FeatureRange currentFeatureRange;
    private Legend currentLegend;
//    private LegendItem currentLegendItem;

    private int labelFontSize = -1;
//    private int rulerFontSize = -1;
    private int legendFontSize = -1;

//    private StringBuffer content = new StringBuffer();
    
    SequenceDao sequenceDAO;
    
    /**
     * Generates a Cgview object from a String of XML content.
     *
     * @param xml the XML content to read.
     * @return the newly created Cgview object.
     */
    public Cgview createCgviewFromString(String id) {
        //org.genedb.db.hibernate.Feature chromosome = featureDAO.findByUniqueName(id);
        //Cgview ret = new Cgview(chromosome.getSeqlen());

        Cgview ret = new Cgview(20000);
        ret.setHeight(600);
        //FeatureSlot forward = new FeatureSlot(ret, DIRECT_STRAND);
        //FeatureSlot reverse = new FeatureSlot(ret, REVERSE_STRAND);
        
        FeatureSlot cutSlot = new FeatureSlot(ret, DIRECT_STRAND);

        EmbossTableParser etp = new EmbossTableParser();
        List<CutSite> sites = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/nfs/team81/art/circular-restrict.txt"));
            sites = etp.parse(br);
        }
        catch (IOException exp) {
            throw new RuntimeException("Couldn't read, or parse results");
        }
        
        int i = 0;
        for (CutSite cutSite : sites) {
            createFeature(cutSlot, cutSite.getStart(), cutSite.getEnd(), i);
            i++;
        }
        return ret;
    }
    
    
    public ReportDetails findCutSitesFromEmbossReport(String fileName) {
        EmbossTableParser etp = new EmbossTableParser();
        ReportDetails ret = new ReportDetails();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            ret.cutSites = etp.parse(br);
            ret.length = etp.getLength();
        }
        catch (IOException exp) {
            throw new RuntimeException("Couldn't read, or parse results");
        }
        
        System.err.println("Found '"+ret.cutSites.size()+"' cutsites in a length of '"+etp.getLength()+"'");
    	return ret;
    }
    
    public Cgview createCgviewFromReportDetails(ReportDetails rd) {

        Cgview ret = new Cgview(rd.length);
        ret.setHeight(600);
        FeatureSlot cutSlot = new FeatureSlot(ret, DIRECT_STRAND);
        
        int counter = 1;
        Iterator<CutSite> it = rd.cutSites.iterator();
        CutSite cutSite = it.next();
        int firstCutPos = cutSite.getStart();
        int lastCutPos = cutSite.getEnd();
        while (it.hasNext()) {
            cutSite = it.next();
            createFeature(cutSlot, lastCutPos, cutSite.getStart(), counter);
            lastCutPos = cutSite.getEnd();
            counter++;
        }
        createFeature(cutSlot, lastCutPos, firstCutPos, counter);
        return ret;
    }

    private Feature createFeature(FeatureSlot cutSlot, int coord1, int coord2, int counter) {
        Feature f1 = new Feature(cutSlot);
        //f1.setShowLabel(3);
        f1.setLabel(counter + ":"+coord1+"->"+coord2+" ("+(coord2-coord1)+" bp)");
        f1.setProportionOfThickness(0.5f);
        if (counter % 2 == 0) {
            f1.setColor(ArtemisColours.getByName("red"));
        } else {
            f1.setColor(ArtemisColours.getByName("blue"));
            f1.setRadiusAdjustment(0.5f);
        }
        f1.setMouseover("return showMenu("+coord1+","+coord2+")");
        //f1.setMouseover("<a href=\\\"www.google.com\\\">Menu 1</a>&nbsp;&nbsp;<a href=\\\"www.sanger.ac.uk\\\">Menu 2</a>");
        f1.setLabel(".....   "+counter);
        f1.setHyperlink(""+coord1+":"+coord2);
        new FeatureRange(f1, coord1, coord2); // Don't need to store reference
        return f1;
    }
}

    class ReportDetails {
    	int length;
    	List<CutSite> cutSites;
    }
    