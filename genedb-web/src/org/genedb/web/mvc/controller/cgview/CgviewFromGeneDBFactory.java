package org.genedb.web.mvc.controller.cgview;

import org.apache.log4j.Logger;
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.symbol.Location;
import org.genedb.db.dao.SequenceDao;


import uk.ac.sanger.artemis.circular.DNADraw;
import uk.ac.sanger.artemis.circular.Track;
import uk.ac.sanger.artemis.circular.Feature;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


public class CgviewFromGeneDBFactory {

    private static final Logger logger = Logger.getLogger(CgviewFromGeneDBFactory.class);
    SequenceDao sequenceDAO;

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
        return ret;
    }
    
    public DNADraw createDnaFromReportDetails(ReportDetails rd) {

        DNADraw dna = new DNADraw();
        dna.setSize(600, 600);
        dna.setName("Circular Diagram");
        int sequenceLength = rd.length;
        Hashtable<String,Object> lineAttr = new Hashtable<String,Object>();
        lineAttr.put("lsize",new Integer(1) );
        lineAttr.put("circular",new Boolean(true) );
        lineAttr.put("start",new Integer(0) );
        lineAttr.put("end",new Integer(sequenceLength) );
        dna.setLineAttributes(lineAttr);
        
        //set ticks
        int div;
        if(sequenceLength < 1000)
          div = 100;
        else if(sequenceLength < 10000)
          div = 1000;
        else if(sequenceLength < 100000)
          div = 10000;
        else
          div = 100000;
        int tickNum = sequenceLength/div;
        int tick = tickNum*(div/10);
        while((sequenceLength % tick) < (div/10)) {
            tickNum++;
            tick = tickNum*(div/10);
        }
        dna.setMinorTickInterval(tick);
        dna.setTickInterval(tick);
        dna.setBackground(Color.WHITE);
        dna.setGeneticMarker(null);
        Track forward = new Track(0.9,"",true,false,null);
        Track reverse = new Track(0.85,"",false,true,null);
        int counter = 1;
        if(rd.cutSites.size() == 1) {
          CutSite cutSite = rd.cutSites.get(0);
          dna.addFeatureToTrack(createFeature(cutSite.getEnd(), rd.length + cutSite.getStart(), 1),forward,false);
          return dna;
        } 
        Iterator<CutSite> it = rd.cutSites.iterator();
        CutSite cutSite = it.next();
        int firstCutPos = cutSite.getStart();
        int lastCutPos = cutSite.getEnd();
        while (it.hasNext()) {
            cutSite = it.next();
            if(counter % 2 == 0) {
                dna.addFeatureToTrack(createFeature(lastCutPos, cutSite.getStart(), counter),reverse,false);
            } else {
                dna.addFeatureToTrack(createFeature(lastCutPos, cutSite.getStart(), counter),forward,false);
            }
            lastCutPos = cutSite.getEnd();
            counter++;
        }
        if(counter % 2 == 0) {
            dna.addFeatureToTrack(createFeature(lastCutPos, rd.length + firstCutPos, counter),reverse,false);
        } else {
            dna.addFeatureToTrack(createFeature(lastCutPos, rd.length + firstCutPos, counter),forward,false);
        }
        return dna;
    }

    private Feature createFeature(int coord1, int coord2, int counter) {
        int colour;
        if (counter % 2 == 0) {
            colour = 2;
        } else {
            colour = 5;
        }
        Feature f1 = new Feature(String.valueOf(counter),coord1,coord2,colour);
        return f1;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Location> parseEmblForMiscFeatures(String addFileName) {
        Map<String,Location> ret = new HashMap<String,Location>();
        BufferedReader br = null;
        try {
              br = new BufferedReader(new FileReader(addFileName));
            }
            catch (FileNotFoundException ex) {
              ex.printStackTrace();
              System.exit(-1);
            }
            SequenceIterator sequences = SeqIOTools.readEmbl(br);

            while(sequences.hasNext()){
              try {
                Sequence seq = sequences.nextSequence();
                Iterator<org.biojava.bio.seq.Feature> features = seq.features();
                int counter = 0;
                while(features.hasNext()) {
                    org.biojava.bio.seq.Feature feature = (org.biojava.bio.seq.Feature) features.next();
                    if(feature.getType().equals("misc_feature")) {
                        Annotation an = feature.getAnnotation();
                        String label = (String)an.getProperty("systematic_id");
                        if (label == "") {
                            label = "misc-feature-" + counter;
                        } else {
                            label = "misc-feature-" + label;
                        }
                        counter++;
                        Location loc = feature.getLocation();
                        ret.put(label, loc);
                    }
                }
              }
              catch (BioException ex) {
                ex.printStackTrace();
              }catch (NoSuchElementException ex) {
                ex.printStackTrace();
              }
            }
        return ret;
    }
}

    class ReportDetails {
        int length;
        List<CutSite> cutSites;
    }
    