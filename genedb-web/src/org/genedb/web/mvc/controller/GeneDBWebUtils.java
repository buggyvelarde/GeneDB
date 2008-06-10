package org.genedb.web.mvc.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.proteomics.IsoelectricPointCalc;
import org.biojava.bio.proteomics.MassCalc;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;
import org.biojava.utils.SmallMap;
import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.SequenceDao;
import org.genedb.web.utils.Grep;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.feature.Gene;
import org.gmod.schema.sequence.feature.MRNA;
import org.gmod.schema.sequence.feature.Polypeptide;
import org.gmod.schema.sequence.feature.Transcript;
import org.gmod.schema.utils.CountedName;
import org.gmod.schema.utils.GeneNameOrganism;
import org.gmod.schema.utils.PeptideProperties;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author art
 *
 */
public class GeneDBWebUtils {

    private static final Logger logger = Logger.getLogger(GeneDBWebUtils.class);
    private static SequenceDao sequenceDao;
    private static CvDao cvDao;
    private static Grep grep;

    public void setGrep(Grep grep) {
        this.grep = grep;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        GeneDBWebUtils.sequenceDao = sequenceDao;
    }

    public static Map<String, Object> prepareFeature(Feature feature, Map<String, Object> model) {
        if (feature instanceof Gene)
            return prepareGene((Gene) feature, model);
        else if (feature instanceof Transcript)
            return prepareTranscript((Transcript) feature, model);
        else if (feature instanceof Polypeptide)
            return prepareTranscript(((Polypeptide) feature).getTranscript(), model);
        else {
            logger.error(String.format("Cannot build model for feature '%s' of type '%s'", feature.getUniqueName(), feature.getClass()));
            return model;
        }
    }

    /**
     * Populate a model object with the details of the specified gene.
     *
     * @param uniqueName the uniqueName of the gene
     * @param model the Map object to populate
     * @return a reference to <code>model</code>
     * @throws IOException
     */
    public static Map<String, Object> prepareGene(String uniqueName, Map<String, Object> model)
            throws IOException {
        model = prepareArtemisHistory(uniqueName, model);
        Gene gene = (Gene) sequenceDao.getFeatureByUniqueName(uniqueName, "gene");
        return prepareGene(gene, model);
    }

    public static Map<String, Object> prepareGene(Gene gene, Map<String, Object> model) {
        model.put("gene", gene);

        Collection<Transcript> transcripts = gene.getTranscripts();
        if (transcripts.isEmpty()) {
            logger.error(String.format("Gene '%s' has no transcripts", gene.getUniqueName()));
            return model;
        }

        Transcript firstTranscript = null;
        for (Transcript transcript : transcripts)
            if (firstTranscript == null || transcript.getFeatureId() < firstTranscript.getFeatureId())
                firstTranscript = transcript;

        return prepareTranscript(firstTranscript, model);
    }

    public static Map<String, Object> prepareTranscript(String uniqueName, Map<String, Object> model)
            throws IOException {
        model = prepareArtemisHistory(uniqueName, model);
        Transcript transcript = (Transcript) sequenceDao.getFeatureByUniqueName(uniqueName, Transcript.class);
        return prepareTranscript(transcript, model);
    }

    public static Map<String,Object> prepareTranscript(Transcript transcript, Map<String,Object> model) {

        if (!model.containsKey("gene"))
            model.put("gene", transcript.getGene());

        model.put("transcript", transcript);

        if (transcript instanceof MRNA) {
            MRNA codingTranscript = (MRNA) transcript;
            Polypeptide polypeptide = codingTranscript.getProtein();

            model.put("polypeptide", polypeptide);
            model.put("polyprop", calculatePepstats(polypeptide));
            
            List<CountedName> controlledCuration = cvDao.getCountedNamesByCvNameAndFeature("CC_genedb_controlledcuration", polypeptide);
            List<CountedName> biologicalProcess = cvDao.getCountedNamesByCvNameAndFeature("biological_process", polypeptide);
            List<CountedName> cellularComponent = cvDao.getCountedNamesByCvNameAndFeature("cellular_component", polypeptide);
            List<CountedName> molecularFunction = cvDao.getCountedNamesByCvNameAndFeature("molecular_function", polypeptide);
            model.put("CC", controlledCuration);
            model.put("BP", biologicalProcess);
            model.put("CellularC", cellularComponent);
            model.put("MF", molecularFunction);
        }
        return model;
    }

    private static PeptideProperties calculatePepstats(Feature polypeptide) {
        if (polypeptide.getResidues() == null) {
            logger.warn("No residues for '" + polypeptide.getUniqueName() + "'");
            return null;
        }
        SymbolList protein = null;
        PeptideProperties pp = new PeptideProperties();
        try {
            SymbolTokenization proteinTokenization = ProteinTools.getTAlphabet().getTokenization("token");
            protein = new SimpleSymbolList(proteinTokenization, new String(polypeptide.getResidues()));

             try {
                // if the seq ends with a * (termination) we need to
                // remove the *
                if (protein.symbolAt(protein.length()) == ProteinTools.ter())
                    protein = protein.subList(1, protein.length() - 1);

             } catch (IndexOutOfBoundsException exception) {
                 throw new RuntimeException(exception);
             }
        } catch (BioException e) {
            logger.error("Can't translate into a protein sequence", e);
            return pp;
        }
        IsoelectricPointCalc ipc = new IsoelectricPointCalc();
        Double cal = 0.0;
        try {
            cal = ipc.getPI(protein, false, false);
        } catch (IllegalAlphabetException e) {
            logger.error(String.format("Error computing protein isoelectric point for '%s'", protein), e);
        } catch (BioException e) {
            logger.error(String.format("Error computing protein isoelectric point for '%s'", protein), e);
        }
        DecimalFormat df = new DecimalFormat("#.##");
        pp.setIsoelectricPoint(df.format(cal));
        pp.setAminoAcids(Integer.toString(protein.length()));
        try {
            cal = 0.0;
            cal = MassCalc.getMass(protein, SymbolPropertyTable.AVG_MASS, true) / 1000;
        } catch (IllegalSymbolException e) {
            logger.error("Error computing protein mass", e);
        }
        pp.setMass(df.format(cal / 1000));
        cal = getCharge(protein);
        pp.setCharge(df.format(cal));
        return pp;
    }

    /**
     * Grep all references to the given id from a logfile, and remove usernames
     * etc and verbose info
     *
     * @param systematicId the genename
     * @param model the model returned to the view
     * @throws IOException if the log file can't be read
     */
    private static Map<String, Object> prepareArtemisHistory(String systematicId,
            Map<String, Object> model) throws IOException {
        grep.compile("ID=" + systematicId);
        List<String> out = grep.grep();
        List<String> filtered = new ArrayList<String>(out.size());
        for (String s : out) {
            s = s.trim();
            s = s.replace("uk.ac.sanger.artemis.chado.ChadoTransactionManager", "");
            s = s.replace("[AWT-EventQueue-0]", "");
            s = s.replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+\\s+\\d+", "");
            s = s.replaceAll("\\S+@\\S+", "uname");
            s = "&nbsp;&nbsp;&nbsp;" + s;
            filtered.add(s);
        }
        model.put("modified", filtered);
        return model;
    }

    public static boolean extractTaxonNodesFromRequest(HttpServletRequest request,
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

    public static void buildErrorMsg(HttpServletRequest request, String msg) {
        @SuppressWarnings("unchecked")
        List<String> stored = (List<String>) request.getAttribute(WebConstants.ERROR_MSG);
        if (stored == null) {
            stored = new ArrayList<String>();
        }
        stored.add(msg);
        request.setAttribute(WebConstants.ERROR_MSG, stored);
    }

    public static String buildGViewerXMLFiles(List<Feature> displayFeatures, File tmpDir) {
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

            Collection<FeatureLoc> temp = feature.getFeatureLocsForFeatureId();
            for (FeatureLoc fl : temp) {
                String name = fl.getFeatureBySrcFeatureId().getUniqueName();
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

    public static int featureListSize(String cvTermName, String cvName) {
        List<GeneNameOrganism> temp = sequenceDao.getGeneNameOrganismsByCvTermNameAndCvName(cvTermName, cvName);
        if (temp == null)
            return 0;
        return temp.size();
    }

    // TODO Remove me once charge is stored by mining code
    public static double getCharge(SymbolList aaSymList) {
        @SuppressWarnings("unchecked")
        Map<Symbol, Double> chargeFor = new SmallMap();
        double charge = 0.0;

        try {
            Alphabet aa = ProteinTools.getAlphabet();
            SymbolTokenization aaToken = aa.getTokenization("token");

            // A,C,F,G,I,L,M,N,P,Q,S,T,U,V,W,X,Y are all zero
            chargeFor.put(aaToken.parseToken("B"), -0.5);
            chargeFor.put(aaToken.parseToken("D"), -1.0);
            chargeFor.put(aaToken.parseToken("E"), -1.0);
            chargeFor.put(aaToken.parseToken("H"), 0.5);
            chargeFor.put(aaToken.parseToken("K"), 1.0);
            chargeFor.put(aaToken.parseToken("R"), 1.0);
            chargeFor.put(aaToken.parseToken("Z"), -0.5);
        } catch (IllegalSymbolException e) {
            logger.error(e);
            throw new RuntimeException("Unexpected biojava error - illegal symbol");
        } catch (BioException e) {
            logger.error(e);
            throw new RuntimeException("Unexpected biojava error - general");
        }

        @SuppressWarnings("unchecked")
        Map<Symbol, Integer> counts = residueCount(aaSymList, chargeFor);

        // iterate thru' all counts computing the partial contribution to charge
        for (Map.Entry<Symbol, Integer> e : counts.entrySet()) {
            Symbol sym = e.getKey();
            int count = e.getValue();
            double symbolsCharge = chargeFor.get(sym);

            charge += (symbolsCharge * count);
        }
        return charge;
    }

    @SuppressWarnings("unchecked")
    private static Map residueCount(SymbolList aaSymList, Map chargeFor) {
        // iterate thru' aaSymList collating number of relevant residues
        Iterator residues = aaSymList.iterator();

        Map symbolCounts = new SmallMap();

        while (residues.hasNext()) {
            Symbol sym = (Symbol) residues.next();

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

    public void setCvDao(CvDao cvDao) {
        GeneDBWebUtils.cvDao = cvDao;
    }
}
