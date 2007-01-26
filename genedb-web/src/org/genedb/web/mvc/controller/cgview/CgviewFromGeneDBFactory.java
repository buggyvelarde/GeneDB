package org.genedb.web.mvc.controller.cgview;

import static ca.ualberta.stothard.cgview.CgviewConstants.BASES;
import static ca.ualberta.stothard.cgview.CgviewConstants.CENTISOMES;
import static ca.ualberta.stothard.cgview.CgviewConstants.DECORATION_CLOCKWISE_ARROW;
import static ca.ualberta.stothard.cgview.CgviewConstants.DECORATION_COUNTERCLOCKWISE_ARROW;
import static ca.ualberta.stothard.cgview.CgviewConstants.DECORATION_HIDDEN;
import static ca.ualberta.stothard.cgview.CgviewConstants.DECORATION_STANDARD;
import static ca.ualberta.stothard.cgview.CgviewConstants.DIRECT_STRAND;
import static ca.ualberta.stothard.cgview.CgviewConstants.INNER_LABELS_AUTO;
import static ca.ualberta.stothard.cgview.CgviewConstants.INNER_LABELS_NO_SHOW;
import static ca.ualberta.stothard.cgview.CgviewConstants.INNER_LABELS_SHOW;
import static ca.ualberta.stothard.cgview.CgviewConstants.LABEL;
import static ca.ualberta.stothard.cgview.CgviewConstants.LABEL_FORCE;
import static ca.ualberta.stothard.cgview.CgviewConstants.LABEL_NONE;
import static ca.ualberta.stothard.cgview.CgviewConstants.LABEL_ZOOMED;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_DRAW_ZOOMED;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_ITEM_ALIGN_CENTER;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_ITEM_ALIGN_LEFT;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_ITEM_ALIGN_RIGHT;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_LOWER_CENTER;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_LOWER_LEFT;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_LOWER_RIGHT;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_MIDDLE_CENTER;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_MIDDLE_LEFT;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_MIDDLE_LEFT_OF_CENTER;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_MIDDLE_RIGHT;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_MIDDLE_RIGHT_OF_CENTER;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_NO_DRAW_ZOOMED;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_UPPER_CENTER;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_UPPER_LEFT;
import static ca.ualberta.stothard.cgview.CgviewConstants.LEGEND_UPPER_RIGHT;
import static ca.ualberta.stothard.cgview.CgviewConstants.POSITIONS_AUTO;
import static ca.ualberta.stothard.cgview.CgviewConstants.POSITIONS_NO_SHOW;
import static ca.ualberta.stothard.cgview.CgviewConstants.POSITIONS_SHOW;
import static ca.ualberta.stothard.cgview.CgviewConstants.REVERSE_STRAND;
import static ca.ualberta.stothard.cgview.CgviewConstants.SWATCH_NO_SHOW;
import static ca.ualberta.stothard.cgview.CgviewConstants.SWATCH_SHOW;

import org.genedb.db.dao.SequenceDao;
import org.genedb.web.mvc.controller.ArtemisColours;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
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

public class CgviewFromGeneDBFactory extends DefaultHandler {

    private static final String contextPath = "/genedb-web/";
    
    private final static Map<String, Integer> LABEL_TYPES = new HashMap<String, Integer>();
    private final static Map<String, Integer> GLOBAL_LABEL_TYPES = new HashMap<String, Integer>();
    private final static Map<String, Integer> DECORATIONS = new HashMap<String, Integer>();
    private final static Map<String, Integer> RULER_UNITS = new HashMap<String, Integer>();
    private final static Map<String, Integer> USE_INNER_LABELS = new HashMap<String, Integer>();
    private final static Map<String, Integer> GIVE_FEATURE_POSITIONS = new HashMap<String, Integer>();
    private final static Map<String, Float> FEATURE_THICKNESSES = new HashMap<String, Float>();
    private final static Map<String, Float> FEATURESLOT_SPACINGS = new HashMap<String, Float>();
    private final static Map<String, Float> BACKBONE_THICKNESSES = new HashMap<String, Float>();
    private final static Map<String, Double> ARROWHEAD_LENGTHS = new HashMap<String, Double>();
    private final static Map<String, Double> MINIMUM_FEATURE_LENGTHS = new HashMap<String, Double>();
    private final static Map<String, Double> ORIGINS = new HashMap<String, Double>();
    private final static Map<String, Float> TICK_THICKNESSES = new HashMap<String, Float>();
    private final static Map<String, Float> TICK_LENGTHS = new HashMap<String, Float>();
    private final static Map<String, Float> LABEL_LINE_THICKNESSES = new HashMap<String, Float>();
    private final static Map<String, Double> LABEL_LINE_LENGTHS = new HashMap<String, Double>();
    private final static Map<String, Integer> LABEL_PLACEMENT_QUALITIES = new HashMap<String, Integer>();
    private final static Map<String, Boolean> BOOLEANS = new HashMap<String, Boolean>();
    private final static Map<String, Integer> SWATCH_TYPES = new HashMap<String, Integer>();
    private final static Map<String, Integer> LEGEND_POSITIONS = new HashMap<String, Integer>();
    private final static Map<String, Integer> LEGEND_ALIGNMENTS = new HashMap<String, Integer>();
    private final static Map<String, Integer> LEGEND_SHOW_ZOOM = new HashMap<String, Integer>();

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
    private Locator locator;
    private Stack context = new Stack();

    /**
     * Constructs a new CgviewFactory object.
     */
    public CgviewFromGeneDBFactory() {
        super();


        LABEL_TYPES.put("true", new Integer(LABEL));
        LABEL_TYPES.put("false", new Integer(LABEL_NONE));  //default for feature and featureRange
        LABEL_TYPES.put("force", new Integer(LABEL_FORCE));

        GLOBAL_LABEL_TYPES.put("true", new Integer(LABEL));  //default for Cgview
        GLOBAL_LABEL_TYPES.put("false", new Integer(LABEL_NONE));
        GLOBAL_LABEL_TYPES.put("auto", new Integer(LABEL_ZOOMED));

        DECORATIONS.put("arc", new Integer(DECORATION_STANDARD));  //default for feature and featureRange
        DECORATIONS.put("hidden", new Integer(DECORATION_HIDDEN));
        DECORATIONS.put("counterclockwise-arrow", new Integer(DECORATION_COUNTERCLOCKWISE_ARROW));
        DECORATIONS.put("clockwise-arrow", new Integer(DECORATION_CLOCKWISE_ARROW));

        RULER_UNITS.put("bases", new Integer(BASES));   //default for rulerUnits
        RULER_UNITS.put("centisomes", new Integer(CENTISOMES));

        USE_INNER_LABELS.put("true", new Integer(INNER_LABELS_SHOW));   //should have true, false, auto
        USE_INNER_LABELS.put("false", new Integer(INNER_LABELS_NO_SHOW));
        USE_INNER_LABELS.put("auto", new Integer(INNER_LABELS_AUTO));

        GIVE_FEATURE_POSITIONS.put("true", new Integer(POSITIONS_SHOW));
        GIVE_FEATURE_POSITIONS.put("false", new Integer(POSITIONS_NO_SHOW));  //default
        GIVE_FEATURE_POSITIONS.put("auto", new Integer(POSITIONS_AUTO));

        FEATURE_THICKNESSES.put("xxx-small", new Float(1.0f));
        FEATURE_THICKNESSES.put("xx-small", new Float(2.0f));
        FEATURE_THICKNESSES.put("x-small", new Float(4.0f));
        FEATURE_THICKNESSES.put("small", new Float(6.0f));
        FEATURE_THICKNESSES.put("medium", new Float(8.0f)); //default for featureThickness
        FEATURE_THICKNESSES.put("large", new Float(10.0f));
        FEATURE_THICKNESSES.put("x-large", new Float(12.0f));
        FEATURE_THICKNESSES.put("xx-large", new Float(14.0f));
        FEATURE_THICKNESSES.put("xxx-large", new Float(16.0f));

        FEATURESLOT_SPACINGS.put("xxx-small", new Float(0.0f));
        FEATURESLOT_SPACINGS.put("xx-small", new Float(1.0f));
        FEATURESLOT_SPACINGS.put("x-small", new Float(2.0f));
        FEATURESLOT_SPACINGS.put("small", new Float(3.0f));
        FEATURESLOT_SPACINGS.put("medium", new Float(4.0f)); //default for featureSlotSpacing
        FEATURESLOT_SPACINGS.put("large", new Float(5.0f));
        FEATURESLOT_SPACINGS.put("x-large", new Float(6.0f));
        FEATURESLOT_SPACINGS.put("xx-large", new Float(7.0f));
        FEATURESLOT_SPACINGS.put("xxx-large", new Float(8.0f));

        BACKBONE_THICKNESSES.put("xxx-small", new Float(1.0f));
        BACKBONE_THICKNESSES.put("xx-small", new Float(2.0f));
        BACKBONE_THICKNESSES.put("x-small", new Float(3.0f));
        BACKBONE_THICKNESSES.put("small", new Float(4.0f));
        BACKBONE_THICKNESSES.put("medium", new Float(5.0f)); //default for backboneThickness
        BACKBONE_THICKNESSES.put("large", new Float(6.0f));
        BACKBONE_THICKNESSES.put("x-large", new Float(7.0f));
        BACKBONE_THICKNESSES.put("xx-large", new Float(8.0f));
        BACKBONE_THICKNESSES.put("xxx-large", new Float(9.0f));

        ARROWHEAD_LENGTHS.put("xxx-small", new Double(1.0d));
        ARROWHEAD_LENGTHS.put("xx-small", new Double(2.0d));
        ARROWHEAD_LENGTHS.put("x-small", new Double(3.0d));
        ARROWHEAD_LENGTHS.put("small", new Double(4.0d));
        ARROWHEAD_LENGTHS.put("medium", new Double(5.0d)); //default for arrowheadLength
        ARROWHEAD_LENGTHS.put("large", new Double(6.0d));
        ARROWHEAD_LENGTHS.put("x-large", new Double(7.0d));
        ARROWHEAD_LENGTHS.put("xx-large", new Double(8.0d));
        ARROWHEAD_LENGTHS.put("xxx-large", new Double(9.0d));

        TICK_LENGTHS.put("xxx-small", new Float(3.0f));
        TICK_LENGTHS.put("xx-small", new Float(4.0f));
        TICK_LENGTHS.put("x-small", new Float(5.0f));
        TICK_LENGTHS.put("small", new Float(6.0f));
        TICK_LENGTHS.put("medium", new Float(7.0f)); //default for ticks
        TICK_LENGTHS.put("large", new Float(8.0f));
        TICK_LENGTHS.put("x-large", new Float(9.0f));
        TICK_LENGTHS.put("xx-large", new Float(10.0f));
        TICK_LENGTHS.put("xxx-large", new Float(11.0f));

        MINIMUM_FEATURE_LENGTHS.put("xxx-small", new Double(0.02d)); //default for minimumFeatureLength
        MINIMUM_FEATURE_LENGTHS.put("xx-small", new Double(0.05d));
        MINIMUM_FEATURE_LENGTHS.put("x-small", new Double(0.1d));
        MINIMUM_FEATURE_LENGTHS.put("small", new Double(0.5d));
        MINIMUM_FEATURE_LENGTHS.put("medium", new Double(1.0d));
        MINIMUM_FEATURE_LENGTHS.put("large", new Double(1.5d));
        MINIMUM_FEATURE_LENGTHS.put("x-large", new Double(2.0d));
        MINIMUM_FEATURE_LENGTHS.put("xx-large", new Double(2.5d));
        MINIMUM_FEATURE_LENGTHS.put("xxx-large", new Double(3.0d));

        ORIGINS.put("1", new Double(60.0d));
        ORIGINS.put("2", new Double(30.0d));
        ORIGINS.put("3", new Double(0.0d));
        ORIGINS.put("4", new Double(-30.0d));
        ORIGINS.put("5", new Double(-60.0d));
        ORIGINS.put("6", new Double(-90.0d));
        ORIGINS.put("7", new Double(-120.0d));
        ORIGINS.put("8", new Double(-150.0d));
        ORIGINS.put("9", new Double(-180.0d));
        ORIGINS.put("10", new Double(-210.0d));
        ORIGINS.put("11", new Double(-240.0d));
        ORIGINS.put("12", new Double(90.0d));  //default for origin

        TICK_THICKNESSES.put("xxx-small", new Float(0.02f));
        TICK_THICKNESSES.put("xx-small", new Float(0.5f));
        TICK_THICKNESSES.put("x-small", new Float(1.0f));
        TICK_THICKNESSES.put("small", new Float(1.5f));
        TICK_THICKNESSES.put("medium", new Float(2.0f)); //default for tickThickness
        TICK_THICKNESSES.put("large", new Float(2.5f));
        TICK_THICKNESSES.put("x-large", new Float(3.0f));
        TICK_THICKNESSES.put("xx-large", new Float(3.5f));
        TICK_THICKNESSES.put("xxx-large", new Float(4.0f));

        LABEL_LINE_THICKNESSES.put("xxx-small", new Float(0.02f));
        LABEL_LINE_THICKNESSES.put("xx-small", new Float(0.25f));
        LABEL_LINE_THICKNESSES.put("x-small", new Float(0.50f));
        LABEL_LINE_THICKNESSES.put("small", new Float(0.75f));
        LABEL_LINE_THICKNESSES.put("medium", new Float(1.0f)); //default for labelLineThickness
        LABEL_LINE_THICKNESSES.put("large", new Float(1.25f));
        LABEL_LINE_THICKNESSES.put("x-large", new Float(1.5f));
        LABEL_LINE_THICKNESSES.put("xx-large", new Float(1.75f));
        LABEL_LINE_THICKNESSES.put("xxx-large", new Float(2.0f));

        LABEL_LINE_LENGTHS.put("xxx-small", new Double(10.0d));
        LABEL_LINE_LENGTHS.put("xx-small", new Double(20.0d));
        LABEL_LINE_LENGTHS.put("x-small", new Double(30.0d));
        LABEL_LINE_LENGTHS.put("small", new Double(40.0d));
        LABEL_LINE_LENGTHS.put("medium", new Double(50.0d)); //default for labelLineLength
        LABEL_LINE_LENGTHS.put("large", new Double(60.0d));
        LABEL_LINE_LENGTHS.put("x-large", new Double(70.0d));
        LABEL_LINE_LENGTHS.put("xx-large", new Double(80.0d));
        LABEL_LINE_LENGTHS.put("xxx-large", new Double(90.0d));

        LABEL_PLACEMENT_QUALITIES.put("good", new Integer(5));
        LABEL_PLACEMENT_QUALITIES.put("better", new Integer(8));  //default for labelPlacementQuality
        LABEL_PLACEMENT_QUALITIES.put("best", new Integer(10));

        BOOLEANS.put("true", new Boolean(true)); //default for showShading //default for moveInnerLabelsToOuter
        BOOLEANS.put("false", new Boolean(false)); //default for allowLabelClash //default for showWarning

        LEGEND_POSITIONS.put("upper-left", new Integer(LEGEND_UPPER_LEFT));
        LEGEND_POSITIONS.put("upper-center", new Integer(LEGEND_UPPER_CENTER));
        LEGEND_POSITIONS.put("upper-right", new Integer(LEGEND_UPPER_RIGHT));
        LEGEND_POSITIONS.put("middle-left", new Integer(LEGEND_MIDDLE_LEFT));
        LEGEND_POSITIONS.put("middle-left-of-center", new Integer(LEGEND_MIDDLE_LEFT_OF_CENTER));
        LEGEND_POSITIONS.put("middle-center", new Integer(LEGEND_MIDDLE_CENTER));
        LEGEND_POSITIONS.put("middle-right-of-center", new Integer(LEGEND_MIDDLE_RIGHT_OF_CENTER));
        LEGEND_POSITIONS.put("middle-right", new Integer(LEGEND_MIDDLE_RIGHT));	//default for legend
        LEGEND_POSITIONS.put("lower-left", new Integer(LEGEND_LOWER_LEFT));
        LEGEND_POSITIONS.put("lower-center", new Integer(LEGEND_LOWER_CENTER));
        LEGEND_POSITIONS.put("lower-right", new Integer(LEGEND_LOWER_RIGHT));

        LEGEND_SHOW_ZOOM.put("true", new Integer(LEGEND_DRAW_ZOOMED));  //default for legend
        LEGEND_SHOW_ZOOM.put("false", new Integer(LEGEND_NO_DRAW_ZOOMED));

        LEGEND_ALIGNMENTS.put("left", new Integer(LEGEND_ITEM_ALIGN_LEFT)); //default for legend //default for legendItem
        LEGEND_ALIGNMENTS.put("center", new Integer(LEGEND_ITEM_ALIGN_CENTER));
        LEGEND_ALIGNMENTS.put("right", new Integer(LEGEND_ITEM_ALIGN_RIGHT));

        SWATCH_TYPES.put("true", new Integer(SWATCH_SHOW));
        SWATCH_TYPES.put("false", new Integer(SWATCH_NO_SHOW)); //default for legendItem

    }

    
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
    
    public Cgview createCgviewFromEmbossReport(String fileName) {
        //org.genedb.db.hibernate.Feature chromosome = featureDAO.findByUniqueName(id);
        //Cgview ret = new Cgview(chromosome.getSeqlen());


        //FeatureSlot forward = new FeatureSlot(ret, DIRECT_STRAND);
        //FeatureSlot reverse = new FeatureSlot(ret, REVERSE_STRAND);
        


        EmbossTableParser etp = new EmbossTableParser();
        List<CutSite> sites = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            sites = etp.parse(br);
        }
        catch (IOException exp) {
            throw new RuntimeException("Couldn't read, or parse results");
        }
        
        Cgview ret = new Cgview(etp.getLength());
        ret.setHeight(600);
        FeatureSlot cutSlot = new FeatureSlot(ret, DIRECT_STRAND);
        
        int i = 0;
        for (CutSite cutSite : sites) {
            createFeature(cutSlot, cutSite.getStart(), cutSite.getEnd(), i);
            i++;
        }
        return ret;
    }

    private void createFeature(FeatureSlot cutSlot, int coord1, int coord2, int counter) {
        Feature f1 = new Feature(cutSlot);
        //f1.setShowLabel(LABEL_FORCE);
        f1.setLabel("Restriction Zone "+counter);
        f1.setProportionOfThickness(0.5f);
        if (counter % 2 == 0) {
            f1.setColor(ArtemisColours.getByName("red"));
        } else {
            f1.setColor(ArtemisColours.getByName("blue"));
            f1.setRadiusAdjustment(0.5f);
        }
        f1.setMouseover("alert(\'hello\')");
        f1.setHyperlink(contextPath+"SimpleReport/ByRegion/report=list&of=html&org=wibble&feat=&min=coord1&max=coord2&field=sysId&field=molWeight");
        FeatureRange fr = new FeatureRange(f1, coord1, coord2);
    }

//    /**
//     * Adds FeatureSlot, Feature, and FeatureRange objects described in an XML file to an existing Cgview object. Any
//     * Legend and LegendItem objects in the XML are ignored, as are attributes in the cgview element.
//     *
//     * @param cgview   the Cgview object to modify.
//     * @param filename the XML file to supply the additional map content.
//     * @throws SAXException
//     * @throws IOException
//     */
//    public void addToCgviewFromFile(Cgview cgview, String filename) throws SAXException, IOException {
//
//        ignoreCgviewTag = true;
//        ignoreLegendTag = true;
//        ignoreLegendItemTag = true;
//
//        currentCgview = cgview;
//
//        XMLReader xr = new org.apache.xerces.parsers.SAXParser();
//        xr.setContentHandler(this);
//
//        ErrorHandler handler = new ErrorHandler() {
//            public void warning(SAXParseException e) throws SAXException {
//                System.err.println("[warning] " + e.getMessage());
//            }
//
//            public void error(SAXParseException e) throws SAXException {
//                System.err.println("[error] " + e.getMessage());
//            }
//
//            public void fatalError(SAXParseException e) throws SAXException {
//                System.err.println("[fatal error] " + e.getMessage());
//                throw e;
//            }
//        };
//
//        xr.setErrorHandler(handler);
//        FileReader r = new FileReader(filename);
//        xr.parse(new InputSource(r));
//        ignoreCgviewTag = false;
//        ignoreLegendTag = false;
//        ignoreLegendItemTag = false;
//    }


    /**
     * Handles the featureSlot element and its attributes.
     *
     * @throws SAXException
     */
    //required attributes: strand.
    //optional attributes featureThickness, showShading:
    private void handleFeatureSlot() throws SAXException {

        for (int p = context.size() - 1; p >= 0; p--) {
            ElementDetails elem = (ElementDetails) context.elementAt(p);
            if (elem.name.equalsIgnoreCase("featureSlot")) {
                if (currentFeatureSlot != null) {
                    //an error because already in a FeatureSlot tag
                    String error = "featureSlot element encountered inside of another featureSlot element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (currentCgview == null) {
                    //an error because no currentCgview
                    String error = "featureSlot element encountered outside of a cgview element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (elem.attributes.getValue("strand") == null) {
                    //an error because no strand given
                    String error = "featureSlot element is missing 'strand' attribute";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else {
                    if ((elem.attributes.getValue("strand")).equalsIgnoreCase("direct")) {
                        currentFeatureSlot = new FeatureSlot(currentCgview, DIRECT_STRAND);
                    } else if ((elem.attributes.getValue("strand")).equalsIgnoreCase("reverse")) {
                        currentFeatureSlot = new FeatureSlot(currentCgview, REVERSE_STRAND);
                    } else {
                        //an error because strand could not be understood
                        String error = "value for 'strand' attribute in featureSlot element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }
                }
                //optional tags
                //featureThickness
                if (elem.attributes.getValue("featureThickness") != null) {
                    if (FEATURE_THICKNESSES.get(((elem.attributes.getValue("featureThickness"))).toLowerCase()) != null) {
                        currentFeatureSlot.setFeatureThickness((FEATURE_THICKNESSES.get(((elem.attributes.getValue("featureThickness"))).toLowerCase())).floatValue());

                    } else {
			try {
			    float s = Float.parseFloat(elem.attributes.getValue("featureThickness"));
			    currentFeatureSlot.setFeatureThickness(s);
			}
			catch (Exception e) {

			    String error = "value for 'featureThickness' attribute in cgview element not understood";
			    if (locator != null) {
				error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
			    }
			    //throw new SAXException (error);
			    System.err.println("[warning] " + error);
			}
                    }
                }
                //showShading
                if (elem.attributes.getValue("showShading") != null) {
                    if (BOOLEANS.get(((elem.attributes.getValue("showShading"))).toLowerCase()) != null) {
                        currentFeatureSlot.setShowShading((BOOLEANS.get(((elem.attributes.getValue("showShading"))).toLowerCase())).booleanValue());
                    } else {
                        String error = "value for 'showShading' attribute in featureSlot element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }
            }
        }
    }

    /**
     * Handles the feature element and its attributes.
     *
     * @throws SAXException
     */
    //required attributes:
    //optional attributes: color, opacity, proportionOfThickness, radiusAdjustment, decoration, showLabel, font, label, showShading, hyperlink, mouseover 
    private void handleFeature() throws SAXException {

        for (int p = context.size() - 1; p >= 0; p--) {
            ElementDetails elem = (ElementDetails) context.elementAt(p);
            if (elem.name.equalsIgnoreCase("feature")) {
                if (currentFeature != null) {
                    //an error because already in a Feature tag
                    String error = "feature element encountered inside of another feature element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (currentCgview == null) {
                    //an error because no currentCgview
                    String error = "feature element encountered outside of a cgview element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (currentFeatureSlot == null) {
                    //an error because no currentFeatureSlot
                    String error = "feature element encountered outside of a featureSlot element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else {
                    currentFeature = new Feature(currentFeatureSlot);
                }

                //optional tags
                //color
                if (elem.attributes.getValue("color") != null) {
                    if (ArtemisColours.getByName(((elem.attributes.getValue("color"))).toLowerCase()) != null) {
                        currentFeature.setColor(ArtemisColours.getByName(((elem.attributes.getValue("color"))).toLowerCase()));
                    } else {
                        m = colorDescriptionPattern.matcher(elem.attributes.getValue("color"));
                        if (m.find()) {
                            try {

                                int r = Integer.parseInt(m.group(1));
                                int g = Integer.parseInt(m.group(2));
                                int b = Integer.parseInt(m.group(3));

                                currentFeature.setColor(new Color(r, g, b));

                            } catch (Exception e) {
                                String error = "value for 'color' attribute in feature element not understood";
                                if (locator != null) {
                                    error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                                }
                                //throw new SAXException (error);
                                System.err.println("[warning] " + error);
                            }
                        } else {
                            String error = "value for 'color' attribute in feature element not understood";
                            if (locator != null) {
                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                            }
                            //throw new SAXException (error);
                            System.err.println("[warning] " + error);
                        }
                    }
                }
                //opacity
                if (elem.attributes.getValue("opacity") != null) {
                    float opacity;
                    try {
                        opacity = Float.parseFloat(elem.attributes.getValue("opacity"));
                    } catch (NumberFormatException nfe) {
                        String error = "value for 'opacity' attribute in feature element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (opacity > 1.0f) {
                        String error = "value for 'opacity' attribute in feature element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (opacity < 0.0f) {
                        String error = "value for 'opacity' attribute in feature element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    currentFeature.setOpacity(opacity);
                }
                //proportionOfThickness
                if (elem.attributes.getValue("proportionOfThickness") != null) {
                    float thickness;
                    try {
                        thickness = Float.parseFloat(elem.attributes.getValue("proportionOfThickness"));
                    } catch (NumberFormatException nfe) {
                        String error = "value for 'proportionOfThickness' attribute in feature element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (thickness > 1.0f) {
                        String error = "value for 'proportionOfThickness' attribute in feature element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (thickness < 0.0f) {
                        String error = "value for 'proportionOfThickness' attribute in feature element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    currentFeature.setProportionOfThickness(thickness);
                }
                //radiusAdjustment
                if (elem.attributes.getValue("radiusAdjustment") != null) {
                    float height;
                    try {
                        height = Float.parseFloat(elem.attributes.getValue("radiusAdjustment"));
                    } catch (NumberFormatException nfe) {
                        String error = "value for 'radiusAdjustment' attribute in feature element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (height > 1.0f) {
                        String error = "value for 'radiusAdjustment' attribute in feature element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (height < 0.0f) {
                        String error = "value for 'radiusAdjustment' attribute in feature element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    currentFeature.setRadiusAdjustment(height);
                }
                //decoration
                if (elem.attributes.getValue("decoration") != null) {

                    if (DECORATIONS.get(((elem.attributes.getValue("decoration"))).toLowerCase()) != null) {
                        currentFeature.setDecoration((DECORATIONS.get(((elem.attributes.getValue("decoration"))).toLowerCase())).intValue());
                    } else {
                        String error = "value for 'decoration' attribute in feature element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }
                //showLabel
                if (elem.attributes.getValue("showLabel") != null) {
                    if (LABEL_TYPES.get(((elem.attributes.getValue("showLabel"))).toLowerCase()) != null) {
                        currentFeature.setShowLabel((LABEL_TYPES.get(((elem.attributes.getValue("showLabel"))).toLowerCase())).intValue());
                    } else {
                        String error = "value for 'showLabel' attribute in feature element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }
                //font
                if (elem.attributes.getValue("font") != null) {

                    m = fontDescriptionPattern.matcher(elem.attributes.getValue("font"));
                    if (m.find()) {
                        try {

                            String name = m.group(1);
                            String style = m.group(2);
                            int size = Integer.parseInt(m.group(3));
                            int intStyle = Font.PLAIN;

                            if (style.equalsIgnoreCase("bold")) {
                                intStyle = Font.BOLD;
                            } else if ((style.equalsIgnoreCase("italic")) || (style.equalsIgnoreCase("italics"))) {
                                intStyle = Font.ITALIC;
                            } else if ((style.equalsIgnoreCase("bold-italic")) || (style.equalsIgnoreCase("italic-bold"))) {
                                intStyle = Font.ITALIC + Font.BOLD;
                            }
                            currentFeature.setFont(new Font(name, intStyle, size));

                        } catch (Exception e) {
                            String error = "value for 'font' attribute in feature element not understood";
                            if (locator != null) {
                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                            }
                            //throw new SAXException (error);
                            System.err.println("[warning] " + error);
                        }
                    } else {
                        String error = "value for 'font' attribute in feature element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }

                }
                //label
                if (elem.attributes.getValue("label") != null) {
                    currentFeature.setLabel(elem.attributes.getValue("label"));
                }
                //showShading
                if (elem.attributes.getValue("showShading") != null) {
                    if (BOOLEANS.get(((elem.attributes.getValue("showShading"))).toLowerCase()) != null) {
                        currentFeature.setShowShading((BOOLEANS.get(((elem.attributes.getValue("showShading"))).toLowerCase())).booleanValue());
                    } else {
                        String error = "value for 'showShading' attribute in feature element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }
                //hyperlink
                if (elem.attributes.getValue("hyperlink") != null) {
                    currentFeature.setHyperlink(elem.attributes.getValue("hyperlink"));
                }
                //mouseover
                if (elem.attributes.getValue("mouseover") != null) {
                    currentFeature.setMouseover(elem.attributes.getValue("mouseover"));
                }

		if (this.labelFontSize != -1) {
		    if (currentFeature.getFont() != null) {
			currentFeature.setFont(new Font(currentFeature.getFont().getName(), currentFeature.getFont().getStyle(), this.labelFontSize));
		    }
		    else {
			currentFeature.setFont(new Font(currentCgview.getLabelFont().getName(), currentCgview.getLabelFont().getStyle(), this.labelFontSize));
		    }
		}

            }
        }
    }

    /**
     * Handles the featureRange element and its attributes.
     *
     * @throws SAXException
     */
    //required attributes: start, stop
    //optional attributes: color, opacity, proportionOfThickness, radiusAdjustment, decoration, showLabel, font, label, showShading, hyperlink, mouseover
    private void handleFeatureRange() throws SAXException {

        for (int p = context.size() - 1; p >= 0; p--) {
            ElementDetails elem = (ElementDetails) context.elementAt(p);
            if (elem.name.equalsIgnoreCase("featureRange")) {
                if (currentFeatureRange != null) {
                    //an error because already in a FeatureRange
                    String error = "featureRange element encountered inside of another featureRange element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (currentFeature == null) {
                    //an error because no current Feature
                    String error = "featureRange element encountered outside of a feature element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (currentCgview == null) {
                    //an error because no currentCgview
                    String error = "featureRange element encountered outside of a cgview element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (currentFeatureSlot == null) {
                    //an error because no currentFeatureSlot
                    String error = "featureRange element encountered outside of a featureSlot element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (elem.attributes.getValue("start") == null) {
                    //an error because no length
                    String error = "featureRange element is missing 'start' attribute";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (elem.attributes.getValue("stop") == null) {
                    //an error because no length
                    String error = "featureRange element is missing 'stop' attribute";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else {
                    int start;
                    try {
                        start = Integer.parseInt(elem.attributes.getValue("start"));
                    } catch (NumberFormatException nfe) {
                        String error = "value for 'start' attribute in featureRange element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (start > cgviewLength) {
                        String error = "value for 'start' attribute in featureRange element must be less than or equal to the length of the plasmid";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (start < 1) {
                        String error = "value for 'start' attribute in featureRange element must be greater than or equal to 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    int stop;
                    try {
                        stop = Integer.parseInt(elem.attributes.getValue("stop"));
                    } catch (NumberFormatException nfe) {
                        String error = "value for 'stop' attribute in featureRange element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (stop > cgviewLength) {
                        String error = "value for 'stop' attribute in featureRange element must be less than or equal to the length of the plasmid";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (stop < 1) {
                        String error = "value for 'stop' attribute in featureRange element must be greater than or equal to 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    currentFeatureRange = new FeatureRange(currentFeature, start, stop);
                }
                //optional tags
                //color
                if (elem.attributes.getValue("color") != null) {
                    if (ArtemisColours.getByName(((elem.attributes.getValue("color"))).toLowerCase()) != null) {
                        currentFeatureRange.setColor(ArtemisColours.getByName(((elem.attributes.getValue("color"))).toLowerCase()));
                    } else {
                        m = colorDescriptionPattern.matcher(elem.attributes.getValue("color"));
                        if (m.find()) {
                            try {

                                int r = Integer.parseInt(m.group(1));
                                int g = Integer.parseInt(m.group(2));
                                int b = Integer.parseInt(m.group(3));

                                currentFeatureRange.setColor(new Color(r, g, b));

                            } catch (Exception e) {
                                String error = "value for 'color' attribute in featureRange element not understood";
                                if (locator != null) {
                                    error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                                }
                                //throw new SAXException (error);
                                System.err.println("[warning] " + error);
                            }
                        } else {
                            String error = "value for 'color' attribute in featureRange element not understood";
                            if (locator != null) {
                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                            }
                            //throw new SAXException (error);
                            System.err.println("[warning] " + error);
                        }
                    }
                }

                //opacity
                if (elem.attributes.getValue("opacity") != null) {
                    float opacity;
                    try {
                        opacity = Float.parseFloat(elem.attributes.getValue("opacity"));
                    } catch (NumberFormatException nfe) {
                        String error = "value for 'opacity' attribute in featureRange element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (opacity > 1.0f) {
                        String error = "value for 'opacity' attribute in featureRange element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (opacity < 0.0f) {
                        String error = "value for 'opacity' attribute in featureRange element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    currentFeatureRange.setOpacity(opacity);
                }
                //proportionOfThickness
                if (elem.attributes.getValue("proportionOfThickness") != null) {
                    float thickness;
                    try {
                        thickness = Float.parseFloat(elem.attributes.getValue("proportionOfThickness"));
                    } catch (NumberFormatException nfe) {
                        String error = "value for 'proportionOfThickness' attribute in featureRange element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (thickness > 1.0f) {
                        String error = "value for 'proportionOfThickness' attribute in featureRange element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (thickness < 0.0f) {
                        String error = "value for 'proportionOfThickness' attribute in featureRange element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    currentFeatureRange.setProportionOfThickness(thickness);
                }
                //radiusAdjustment
                if (elem.attributes.getValue("radiusAdjustment") != null) {
                    float height;
                    try {
                        height = Float.parseFloat(elem.attributes.getValue("radiusAdjustment"));
                    } catch (NumberFormatException nfe) {
                        String error = "value for 'radiusAdjustment' attribute in featureRange element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (height > 1.0f) {
                        String error = "value for 'radiusAdjustment' attribute in featureRange element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (height < 0.0f) {
                        String error = "value for 'radiusAdjustment' attribute in featureRange element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    currentFeatureRange.setRadiusAdjustment(height);
                }
                //decoration
                if (elem.attributes.getValue("decoration") != null) {

                    if (DECORATIONS.get(((elem.attributes.getValue("decoration"))).toLowerCase()) != null) {
                        currentFeatureRange.setDecoration((DECORATIONS.get(((elem.attributes.getValue("decoration"))).toLowerCase())).intValue());
                    } else {
                        String error = "value for 'decoration' attribute in featureRange element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }
                //showLabel
                if (elem.attributes.getValue("showLabel") != null) {
                    if (LABEL_TYPES.get(((elem.attributes.getValue("showLabel"))).toLowerCase()) != null) {
                        currentFeatureRange.setShowLabel((LABEL_TYPES.get(((elem.attributes.getValue("showLabel"))).toLowerCase())).intValue());
                    } else {
                        String error = "value for 'showLabel' attribute in featureRange element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }
                //font
                if (elem.attributes.getValue("font") != null) {

                    m = fontDescriptionPattern.matcher(elem.attributes.getValue("font"));
                    if (m.find()) {
                        try {

                            String name = m.group(1);
                            String style = m.group(2);
                            int size = Integer.parseInt(m.group(3));
                            int intStyle = Font.PLAIN;

                            if (style.equalsIgnoreCase("bold")) {
                                intStyle = Font.BOLD;
                            } else if ((style.equalsIgnoreCase("italic")) || (style.equalsIgnoreCase("italics"))) {
                                intStyle = Font.ITALIC;
                            } else if ((style.equalsIgnoreCase("bold-italic")) || (style.equalsIgnoreCase("italic-bold"))) {
                                intStyle = Font.ITALIC + Font.BOLD;
                            }
                            currentFeatureRange.setFont(new Font(name, intStyle, size));

                        } catch (Exception e) {
                            String error = "value for 'font' attribute in featureRange element not understood";
                            if (locator != null) {
                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                            }
                            //throw new SAXException (error);
                            System.err.println("[warning] " + error);
                        }
                    } else {
                        String error = "value for 'font' attribute in featureRange element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }

                }
                //label
                if (elem.attributes.getValue("label") != null) {
                    currentFeatureRange.setLabel(elem.attributes.getValue("label"));
                }
                //showShading
                if (elem.attributes.getValue("showShading") != null) {
                    if (BOOLEANS.get(((elem.attributes.getValue("showShading"))).toLowerCase()) != null) {
                        currentFeatureRange.setShowShading((BOOLEANS.get(((elem.attributes.getValue("showShading"))).toLowerCase())).booleanValue());
                    } else {
                        String error = "value for 'showShading' attribute in featureRange element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }
                //hyperlink
                if (elem.attributes.getValue("hyperlink") != null) {
                    currentFeatureRange.setHyperlink(elem.attributes.getValue("hyperlink"));
                }
                //mouseover
                if (elem.attributes.getValue("mouseover") != null) {
                    currentFeatureRange.setMouseover(elem.attributes.getValue("mouseover"));
                }

		if (this.labelFontSize != -1) {
		    if (currentFeatureRange.getFont() != null) {
			currentFeatureRange.setFont(new Font(currentFeatureRange.getFont().getName(), currentFeatureRange.getFont().getStyle(), this.labelFontSize));
		    }
		    else if (currentFeature.getFont() != null) {
			currentFeatureRange.setFont(new Font(currentFeature.getFont().getName(), currentFeature.getFont().getStyle(), this.labelFontSize));
		    }
		    else {
			currentFeatureRange.setFont(new Font(currentCgview.getLabelFont().getName(), currentCgview.getLabelFont().getStyle(), this.labelFontSize));
		    }
		}
            }
        }
    }

    /**
     * Handles the legend element and its attributes.
     *
     * @throws SAXException
     */
    //required attributes: none.
    //optional attributes: font, fontColor, position, drawWhenZoomed, textAlignment, backgroundColor, backgroundOpacity, allowLabelClash.
    private void handleLegend() throws SAXException {

        for (int p = context.size() - 1; p >= 0; p--) {
            ElementDetails elem = (ElementDetails) context.elementAt(p);
            if (elem.name.equalsIgnoreCase("legend")) {
                if (currentLegend != null) {
                    //an error because already in a legend tag
                    String error = "legend element encountered inside of another legend element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                } else if (currentCgview == null) {
                    //an error because no currentCgview
                    String error = "legend element encountered outside of a cgview element";
                    if (locator != null) {
                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                    }
                    throw new SAXException(error);
                }

                currentLegend = new Legend(currentCgview);

                //optional tags
                //fontColor
                if (elem.attributes.getValue("fontColor") != null) {
                    if (ArtemisColours.getByName(((elem.attributes.getValue("fontColor"))).toLowerCase()) != null) {
                        currentLegend.setFontColor(ArtemisColours.getByName(((elem.attributes.getValue("fontColor"))).toLowerCase()));
                    } else {
                        m = colorDescriptionPattern.matcher(elem.attributes.getValue("fontColor"));
                        if (m.find()) {
                            try {

                                int r = Integer.parseInt(m.group(1));
                                int g = Integer.parseInt(m.group(2));
                                int b = Integer.parseInt(m.group(3));

                                currentLegend.setFontColor(new Color(r, g, b));

                            } catch (Exception e) {
                                String error = "value for 'fontColor' attribute in legend element not understood";
                                if (locator != null) {
                                    error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                                }
                                //throw new SAXException (error);
                                System.err.println("[warning] " + error);
                            }
                        } else {
                            String error = "value for 'fontColor' attribute in legend element not understood";
                            if (locator != null) {
                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                            }
                            //throw new SAXException (error);
                            System.err.println("[warning] " + error);
                        }
                    }
                }

                //font
                if (elem.attributes.getValue("font") != null) {

                    m = fontDescriptionPattern.matcher(elem.attributes.getValue("font"));
                    if (m.find()) {
                        try {

                            String name = m.group(1);
                            String style = m.group(2);
                            int size = Integer.parseInt(m.group(3));
                            int intStyle = Font.PLAIN;

                            if (style.equalsIgnoreCase("bold")) {
                                intStyle = Font.BOLD;
                            } else if ((style.equalsIgnoreCase("italic")) || (style.equalsIgnoreCase("italics"))) {
                                intStyle = Font.ITALIC;
                            } else if ((style.equalsIgnoreCase("bold-italic")) || (style.equalsIgnoreCase("italic-bold"))) {
                                intStyle = Font.ITALIC + Font.BOLD;
                            }
                            currentLegend.setFont(new Font(name, intStyle, size));

                        } catch (Exception e) {
                            String error = "value for 'font' attribute in legend element not understood";
                            if (locator != null) {
                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                            }
                            //throw new SAXException (error);
                            System.err.println("[warning] " + error);
                        }
                    } else {
                        String error = "value for 'font' attribute in legend element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }

                }

                //position
                if (elem.attributes.getValue("position") != null) {
                    if (LEGEND_POSITIONS.get(((elem.attributes.getValue("position"))).toLowerCase()) != null) {
                        currentLegend.setPosition((LEGEND_POSITIONS.get(((elem.attributes.getValue("position"))).toLowerCase())).intValue());
                    } else {
                        String error = "value for 'position' attribute in legend element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }

                //textAlignment
                if (elem.attributes.getValue("textAlignment") != null) {
                    if (LEGEND_ALIGNMENTS.get(((elem.attributes.getValue("textAlignment"))).toLowerCase()) != null) {
                        currentLegend.setAlignment((LEGEND_ALIGNMENTS.get(((elem.attributes.getValue("textAlignment"))).toLowerCase())).intValue());
                    } else {
                        String error = "value for 'textAlignment' attribute in legend element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }

                //drawWhenZoomed
                if (elem.attributes.getValue("drawWhenZoomed") != null) {
                    if (LEGEND_SHOW_ZOOM.get(((elem.attributes.getValue("drawWhenZoomed"))).toLowerCase()) != null) {
                        currentLegend.setPosition((LEGEND_POSITIONS.get(((elem.attributes.getValue("drawWhenZoomed"))).toLowerCase())).intValue());
                    } else {
                        String error = "value for 'drawWhenZoomed' attribute in legend element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }

                //backgroundOpacity
                if (elem.attributes.getValue("backgroundOpacity") != null) {
                    float opacity;
                    try {
                        opacity = Float.parseFloat(elem.attributes.getValue("backgroundOpacity"));
                    } catch (NumberFormatException nfe) {
                        String error = "value for 'backgroundOpacity' attribute in legend element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (opacity > 1.0f) {
                        String error = "value for 'backgroundOpacity' attribute in legend element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    if (opacity < 0.0f) {
                        String error = "value for 'backgroundOpacity' attribute in legend element must be between 0 and 1";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        throw new SAXException(error);
                    }

                    currentLegend.setBackgroundOpacity(opacity);
                }

                //backgroundColor
                if (elem.attributes.getValue("backgroundColor") != null) {
                    if (ArtemisColours.getByName(((elem.attributes.getValue("backgroundColor"))).toLowerCase()) != null) {
                        currentLegend.setBackgroundColor(ArtemisColours.getByName(((elem.attributes.getValue("backgroundColor"))).toLowerCase()));
                    } else {
                        m = colorDescriptionPattern.matcher(elem.attributes.getValue("backgroundColor"));
                        if (m.find()) {
                            try {

                                int r = Integer.parseInt(m.group(1));
                                int g = Integer.parseInt(m.group(2));
                                int b = Integer.parseInt(m.group(3));

                                currentLegend.setBackgroundColor(new Color(r, g, b));

                            } catch (Exception e) {
                                String error = "value for 'backgroundColor' attribute in legend element not understood";
                                if (locator != null) {
                                    error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                                }
                                //throw new SAXException (error);
                                System.err.println("[warning] " + error);
                            }
                        } else {
                            String error = "value for 'backgroundColor' attribute in legend element not understood";
                            if (locator != null) {
                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                            }
                            //throw new SAXException (error);
                            System.err.println("[warning] " + error);
                        }
                    }
                }

                //allowLabelClash
                if (elem.attributes.getValue("allowLabelClash") != null) {
                    if (BOOLEANS.get(((elem.attributes.getValue("allowLabelClash"))).toLowerCase()) != null) {
                        currentLegend.setAllowLabelClash((BOOLEANS.get(((elem.attributes.getValue("allowLabelClash"))).toLowerCase())).booleanValue());
                    } else {
                        String error = "value for 'allowLabelClash' attribute in legend element not understood";
                        if (locator != null) {
                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
                        }
                        //throw new SAXException (error);
                        System.err.println("[warning] " + error);
                    }
                }

		if (this.legendFontSize != -1) {
		    if (currentLegend.getFont() != null) {
			currentLegend.setFont(new Font(currentLegend.getFont().getName(), currentLegend.getFont().getStyle(), this.legendFontSize));
		    }
		    else {
			currentLegend.setFont(new Font(currentCgview.getLegendFont().getName(), currentCgview.getLegendFont().getStyle(), this.legendFontSize));
		    }
		}

            }
        }
    }

//    /**
//     * Handles the legendItem element and its attributes.
//     *
//     * @throws SAXException
//     */
//    //required attributes: text.
//    //optional attributes: fontColor, swatchOpacity, drawSwatch, swatchColor, font, textAlignment.
//    private void handleLegendItem() throws SAXException {
//
//        for (int p = context.size() - 1; p >= 0; p--) {
//            ElementDetails elem = (ElementDetails) context.elementAt(p);
//            if (elem.name.equalsIgnoreCase("legendItem")) {
//                if (currentLegend == null) {
//                    //an error because no current legend
//                    String error = "legendItem element encountered inside of legend element";
//                    if (locator != null) {
//                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                    }
//                    throw new SAXException(error);
//                } else if (currentCgview == null) {
//                    //an error because no currentCgview
//                    String error = "legendItem element encountered outside of a cgview element";
//                    if (locator != null) {
//                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                    }
//                    throw new SAXException(error);
//                } else if (currentLegendItem != null) {
//                    //an error because already inside legendItem tag
//                    String error = "legendItem element encountered inside of another legendItem element";
//                    if (locator != null) {
//                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                    }
//                    throw new SAXException(error);
//                } else if (elem.attributes.getValue("text") == null) {
//                    //an error because no length
//                    String error = "legendItem element is missing 'text' attribute";
//                    if (locator != null) {
//                        error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                    }
//                    throw new SAXException(error);
//                }
//
//                currentLegendItem = new LegendItem(currentLegend);
//
//                currentLegendItem.setLabel(elem.attributes.getValue("text"));
//
//                //optional tags
//                //swatchOpacity
//                if (elem.attributes.getValue("swatchOpacity") != null) {
//                    float opacity;
//                    try {
//                        opacity = Float.parseFloat(elem.attributes.getValue("swatchOpacity"));
//                    } catch (NumberFormatException nfe) {
//                        String error = "value for 'swatchOpacity' attribute in legendItem element not understood";
//                        if (locator != null) {
//                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                        }
//                        throw new SAXException(error);
//                    }
//
//                    if (opacity > 1.0f) {
//                        String error = "value for 'swatchOpacity' attribute in legendItem element must be between 0 and 1";
//                        if (locator != null) {
//                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                        }
//                        throw new SAXException(error);
//                    }
//
//                    if (opacity < 0.0f) {
//                        String error = "value for 'swatchOpacity' attribute in legendItem element must be between 0 and 1";
//                        if (locator != null) {
//                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                        }
//                        throw new SAXException(error);
//                    }
//
//                    currentLegendItem.setSwatchOpacity(opacity);
//                }
//                //fontColor
//                if (elem.attributes.getValue("fontColor") != null) {
//                    if (ArtemisColours.getByName(((elem.attributes.getValue("fontColor"))).toLowerCase()) != null) {
//                        currentLegendItem.setFontColor((Color) ArtemisColours.getByName(((elem.attributes.getValue("fontColor"))).toLowerCase()));
//                    } else {
//                        m = colorDescriptionPattern.matcher(elem.attributes.getValue("fontColor"));
//                        if (m.find()) {
//                            try {
//
//                                int r = Integer.parseInt(m.group(1));
//                                int g = Integer.parseInt(m.group(2));
//                                int b = Integer.parseInt(m.group(3));
//
//                                currentLegendItem.setFontColor(new Color(r, g, b));
//
//                            } catch (Exception e) {
//                                String error = "value for 'fontColor' attribute in legendItem element not understood";
//                                if (locator != null) {
//                                    error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                                }
//                                //throw new SAXException (error);
//                                System.err.println("[warning] " + error);
//                            }
//                        } else {
//                            String error = "value for 'fontColor' attribute in legendItem element not understood";
//                            if (locator != null) {
//                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                            }
//                            //throw new SAXException (error);
//                            System.err.println("[warning] " + error);
//                        }
//                    }
//                }
//
//                //swatchColor
//                if (elem.attributes.getValue("swatchColor") != null) {
//                    if (ArtemisColours.getByName(((elem.attributes.getValue("swatchColor"))).toLowerCase()) != null) {
//                        currentLegendItem.setSwatchColor((Color) ArtemisColours.getByName(((elem.attributes.getValue("swatchColor"))).toLowerCase()));
//                    } else {
//                        m = colorDescriptionPattern.matcher(elem.attributes.getValue("swatchColor"));
//                        if (m.find()) {
//                            try {
//
//                                int r = Integer.parseInt(m.group(1));
//                                int g = Integer.parseInt(m.group(2));
//                                int b = Integer.parseInt(m.group(3));
//
//                                currentLegendItem.setSwatchColor(new Color(r, g, b));
//
//                            } catch (Exception e) {
//                                String error = "value for 'swatchColor' attribute in legendItem element not understood";
//                                if (locator != null) {
//                                    error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                                }
//                                //throw new SAXException (error);
//                                System.err.println("[warning] " + error);
//                            }
//                        } else {
//                            String error = "value for 'swatchColor' attribute in legendItem element not understood";
//                            if (locator != null) {
//                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                            }
//                            //throw new SAXException (error);
//                            System.err.println("[warning] " + error);
//                        }
//                    }
//                }
//
//                //drawSwatch
//                if (elem.attributes.getValue("drawSwatch") != null) {
//                    if (SWATCH_TYPES.get(((elem.attributes.getValue("drawSwatch"))).toLowerCase()) != null) {
//                        currentLegendItem.setDrawSwatch(((Integer) SWATCH_TYPES.get(((elem.attributes.getValue("drawSwatch"))).toLowerCase())).intValue());
//                    } else {
//                        String error = "value for 'drawSwatch' attribute in legendItem element not understood";
//                        if (locator != null) {
//                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                        }
//                        //throw new SAXException (error);
//                        System.err.println("[warning] " + error);
//                    }
//                }
//
//                //textAlignment
//                if (elem.attributes.getValue("textAlignment") != null) {
//                    if (LEGEND_ALIGNMENTS.get(((elem.attributes.getValue("textAlignment"))).toLowerCase()) != null) {
//                        currentLegendItem.setTextAlignment(((Integer) LEGEND_ALIGNMENTS.get(((elem.attributes.getValue("textAlignment"))).toLowerCase())).intValue());
//                    } else {
//                        String error = "value for 'textAlignment' attribute in legendItem element not understood";
//                        if (locator != null) {
//                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                        }
//                        //throw new SAXException (error);
//                        System.err.println("[warning] " + error);
//                    }
//                }
//
//                //font
//                if (elem.attributes.getValue("font") != null) {
//
//                    m = fontDescriptionPattern.matcher(elem.attributes.getValue("font"));
//                    if (m.find()) {
//                        try {
//
//                            String name = m.group(1);
//                            String style = m.group(2);
//                            int size = Integer.parseInt(m.group(3));
//                            int intStyle = Font.PLAIN;
//
//                            if (style.equalsIgnoreCase("bold")) {
//                                intStyle = Font.BOLD;
//                            } else if ((style.equalsIgnoreCase("italic")) || (style.equalsIgnoreCase("italics"))) {
//                                intStyle = Font.ITALIC;
//                            } else if ((style.equalsIgnoreCase("bold-italic")) || (style.equalsIgnoreCase("italic-bold"))) {
//                                intStyle = Font.ITALIC + Font.BOLD;
//                            }
//                            currentLegendItem.setFont(new Font(name, intStyle, size));
//
//                        } catch (Exception e) {
//                            String error = "value for 'font' attribute in legendItem element not understood";
//                            if (locator != null) {
//                                error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                            }
//                            //throw new SAXException (error);
//                            System.err.println("[warning] " + error);
//                        }
//                    } else {
//                        String error = "value for 'font' attribute in legendItem element not understood";
//                        if (locator != null) {
//                            error = error + " in " + locator.getSystemId() + " at line " + locator.getLineNumber() + " column " + locator.getColumnNumber();
//                        }
//                        //throw new SAXException (error);
//                        System.err.println("[warning] " + error);
//                    }
//
//                }
//
//		if (this.legendFontSize != -1) {
//		    if (currentLegendItem.getFont() != null) {
//			currentLegendItem.setFont(new Font(currentLegendItem.getFont().getName(), currentLegendItem.getFont().getStyle(), this.legendFontSize));
//		    }
//		    else if (currentLegend.getFont() != null) {
//			currentLegendItem.setFont(new Font(currentLegend.getFont().getName(), currentLegend.getFont().getStyle(), this.legendFontSize));
//		    }
//		    else {
//			currentLegendItem.setFont(new Font(currentCgview.getLegendFont().getName(), currentCgview.getLegendFont().getStyle(), this.legendFontSize));
//		    }
//
//		}
//
//            }
//        }
//    }

    private class ElementDetails {

        public String name;
        public Attributes attributes;

        public ElementDetails(String name, Attributes atts) {
            this.name = name;
            this.attributes = new AttributesImpl(atts);
        }
    }
}
