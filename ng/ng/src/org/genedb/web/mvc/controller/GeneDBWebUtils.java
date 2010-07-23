package org.genedb.web.mvc.controller;

import org.genedb.db.dao.SequenceDao;
import org.genedb.util.Pair;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;

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
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Sets;

/**
 * @author art
 *
 */
public class GeneDBWebUtils {

    public boolean extractTaxonNodesFromRequest(HttpServletRequest request,
            List<String> answers, boolean required, boolean onlyOne) {
        boolean problem = false;

        String[] names = request.getParameterValues("org");

        List<String> idsAndNames = new ArrayList<String>();
        if (names != null) {
            for (String entry : names) {
                String[] entries = entry.split(":");
                idsAndNames.addAll(Arrays.asList(entries));
            }
        }

        //String msg = validateTaxons(idsAndNames); TODO ???

        int length = idsAndNames.size();
        if (required && length == 0) {
            buildErrorMsg(request, "No taxon id (or organism name) supplied when expected");
            problem = true;
        }
        if (onlyOne && length > 1) {
            buildErrorMsg(request, "Only expected 1 taxon id (or organism name)");
            problem = true;
        }

        if (problem) {
            return false;
        }
        // if (errMsg == null) {
        // request.setAttribute(errMsg, errMsg);
        // }
        answers.addAll(idsAndNames);
        // TODO check required and onlyone
        // TODO store error message if necessary
        // TODO check if ids for which we have data - need new flag
        return true;
    }

    private void buildErrorMsg(HttpServletRequest request, String msg) {
        @SuppressWarnings("unchecked")
        List<String> stored = (List<String>) request.getAttribute(WebConstants.ERROR_MSG);
        if (stored == null) {
            stored = new ArrayList<String>();
        }
        stored.add(msg);
        request.setAttribute(WebConstants.ERROR_MSG, stored);
    }

    public String buildGViewerXMLFiles(List<Feature> displayFeatures, File tmpDir) {
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
            String chromosomeNumber = chromosomeName.substring(chromosomeName.length() - 1);
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

            Collection<FeatureLoc> temp = feature.getFeatureLocs();
            for (FeatureLoc fl : temp) {
                String name = fl.getSourceFeature().getUniqueName();
                String number = name.substring(name.length() - 1);
                chromosome.setText(number);
                chrstart.setText(fl.getFmin().toString());
                chrend.setText(fl.getFmax().toString());
            }

            type.setText("gene");
            color.setText("ox79cc3d");
            label.setText(feature.getUniqueName());
            link.setText("http://developer.genedb.org/new/NameFeature?lookup="
                    + feature.getUniqueName());

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

    public static Pair<String, String> parseExtension(String argument, Set<String> validExtensions) {
        return parseExtension(argument, validExtensions, "");
    }

    public static Pair<String, String> parseExtension(String argument, Set<String> validExtensions, String defaultExtension) {
        if (argument != null) {
            int extensionStart = argument.lastIndexOf('.');
            if (extensionStart > 0 && extensionStart < argument.length()) {
                String firstPart = argument.substring(0, extensionStart);
                String extension = argument.substring(extensionStart+1);
                //logger.warn("The extension is '"+extension+"'");
                if (validExtensions.contains(extension)) {
                    return new Pair<String, String>(firstPart, extension);
                }
            }
            return new Pair<String, String>(argument, defaultExtension);
        }
        return null;
    }



    private SequenceDao sequenceDao;

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
}
