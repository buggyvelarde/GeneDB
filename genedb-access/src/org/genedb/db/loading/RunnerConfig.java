package org.genedb.db.loading;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xom.DocumentWrapper;
import net.sf.saxon.xpath.XPathEvaluator;
import net.sf.saxon.xpath.XPathException;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunnerConfig {

    //  private static final String DEFAULT_DATA_PREFIX = "/nfs/pathdb/prod/data/input";
    private static final String DEFAULT_DATA_PREFIX = "/Users/art/Documents/Data/chadoloading";

    protected final Log logger = LogFactory.getLog(this.getClass());

    private String organismCommonName;

    private String configFilePath;

    private XPathEvaluator xPathEval;
    
    private List<String> fileNames = new ArrayList<String>(0);

    private void addFilesFromDirectory(File directory, final String extension) {
	String[] names = directory.list(new FilenameFilter() {
	    public boolean accept(File file, String name) {
		if (file.isDirectory()) {
		    RunnerConfig.this.addFilesFromDirectory(file, extension);
		} else {
		    if (name.endsWith("."+extension)) {
			return true;
		    }
		}
		return false;
	    }
	});
	for (String name : names) {
	    this.fileNames.add(name);
	}
    }

    public void afterPropertiesSet() {
	if (this.configFilePath == null) {
	    this.configFilePath = DEFAULT_DATA_PREFIX + "/" + this.organismCommonName + "/" + this.organismCommonName + ".config.xml";
	}
	this.readConfig();
    }


    @SuppressWarnings({ "unchecked", "cast" })
    private List<Element> elementListFromXPath(String pattern) {
	try {
	    return (List<Element>) this.xPathEval.evaluate(pattern);
	} catch (XPathException exp) {
	    System.err.println(exp);
	}
	return Collections.EMPTY_LIST;
    }

    public List<String> gatherFileNames() {
	return this.fileNames;
    }

    
    public void initXPathEvaluator(Document doc) throws XPathException {
	// Give DOM a Saxon wrapper
	Configuration conf = new Configuration();
	DocumentWrapper configDocWrapper = new DocumentWrapper(doc, "ConfigFile", conf);
	this.xPathEval = new XPathEvaluator(configDocWrapper);
	AxisIterator children = configDocWrapper.getDocumentRoot().iterateAxis(Axis.DESCENDANT);
	NodeInfo loaderConfigNodeInfo = (NodeInfo) children.next();

	this.xPathEval.setContextNode(loaderConfigNodeInfo);
    }

    private void readConfig() {
	// Load into DOM
	File configFile = null;
	try {
	    configFile = new File(this.configFilePath);
	    if (!configFile.exists()) {
		this.logger.fatal("Config file doesn't exist at '"+this.configFilePath+"'");
		System.exit(-1);
	    }
	    Document doc = new Builder().build(configFile);
	    this.initXPathEvaluator(doc);
	}
	catch (IOException exp) {
	    exp.printStackTrace();
	} catch (XPathException exp) {
	    // TODO Auto-generated catch block
	    exp.printStackTrace();
	} catch (ValidityException exp) {
	    // TODO Auto-generated catch block
	    exp.printStackTrace();
	} catch (ParsingException exp) {
	    // TODO Auto-generated catch block
	    exp.printStackTrace();
	}
	
	// Process code
	List<Element> nomenclatureHandlers = this.elementListFromXPath("code/nomenclature-handler");
	if (nomenclatureHandlers.size() > 1) {
	    this.logger.fatal("More than one value specified for nomenclature handler name '"+nomenclatureHandlers.size()+"'");
	    throw new RuntimeException("More than one value specified for nomenclature handler name");
	}
	if (nomenclatureHandlers.size() == 1) {
	    this.nomenclatureHandlerName = nomenclatureHandlers.get(0).getValue();
	}  
	
	
	List<Element> files = this.elementListFromXPath("inputs/file");
	for (Element element : files) {
	    String fileName = element.getAttribute("name").getValue();
	    if (fileName.startsWith("/")) {
		this.fileNames.add(fileName);
	    } else {
		System.err.println("fileName is '"+fileName+"'");
		File tmp = new File(configFile.getParentFile(), fileName);
		this.fileNames.add(tmp.getAbsolutePath());
	    }
	}

	List<Element> dirs = this.elementListFromXPath("inputs/directory");
	for (Element element : dirs) {
	    String dirName = element.getAttribute("name").getValue();
	    File directory;
	    if (dirName.startsWith("/")) {
		directory = new File(dirName);
	    } else {
		directory = new File(configFile.getParentFile(), dirName);
	    }
	    String extension = "embl";
	    this.addFilesFromDirectory(directory, extension);
	}

	List<Element> synthetics = this.elementListFromXPath("inputs/synthetic");
	int syntheticCount = 0;
	for (Element element : synthetics) {
	    syntheticCount++;
	    Synthetic synthetic = new Synthetic();
	    this.synthetics.add(synthetic);
	    synthetic.setName(element.getAttributeValue("name"));
	    synthetic.setSoType(element.getAttributeValue("sotype"));
	    synthetic.setProperties(findPropertySubElements(element));
	    
	    List<Element> parts = this.elementListFromXPath("inputs/synthetic["+syntheticCount+"]/primary/*");
	    System.err.println("Parts list is '"+parts+"'");
	    for (Element part : parts) {
		if (part.getLocalName().equals("file-entry")) {
		    FilePart fp = new FilePart();
		    String fileName = part.getAttributeValue("name");
		    if (fileName.startsWith("/")) {
			fp.setName(fileName);
		    } else {
			File tmp = new File(configFile.getParentFile(), fileName);
			fp.setName(tmp.getAbsolutePath());
		    }
		    int size = Integer.parseInt(part.getAttributeValue("length"));
		    fp.setSize(size);
		    fp.setReparent(Boolean.parseBoolean(part.getAttributeValue("reparent")));
		    synthetic.addPart(fp);
		}
		if (part.getLocalName().equals("feature")) {
		    FeaturePart fp = new FeaturePart();
		    fp.setName(part.getAttributeValue("name"));
		    int size = Integer.parseInt(part.getAttributeValue("length"));
		    fp.setSize(size);
		    fp.setSoType(part.getAttributeValue("sotype"));
		    Short strand = +1;
		    String strandAsString = part.getAttributeValue("strand");
		    if ("-".equals(strandAsString)) {
			strand = -1;
		    }
		    fp.setStrand(strand);
		    fp.setProperties(findPropertySubElements(part));
		    synthetic.addPart(fp);
		}
	    }
	}

//	System.exit(0);

    }

    private List<Synthetic> synthetics = new ArrayList<Synthetic>();

    private String nomenclatureHandlerName = null;
    
    private Map<String,String> findPropertySubElements(Element element) {
	Map<String,String> props = new HashMap<String,String>();
	Elements properties = element.getChildElements();
	for (int i=0; i < properties.size(); i++) {
	    Element property = properties.get(i);
	    props.put(property.getAttributeValue("key"),
		    property.getAttributeValue("value"));
	}
	return props;
    }

    public void setConfigFilePath(String configFilePath) {
	this.configFilePath = configFilePath;
    }

    public void setOrganismCommonName(String commonOrganismName) {
	this.organismCommonName = commonOrganismName;
    }

    public List<Synthetic> getSynthetics() {
        return this.synthetics;
    }

    public String getOrganismCommonName() {
        return this.organismCommonName;
    }

    public String getNomenclatureHandlerName() {
	return nomenclatureHandlerName ;
    }


}
