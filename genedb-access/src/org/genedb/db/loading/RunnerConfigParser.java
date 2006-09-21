/*
 * Copyright (c) 2006 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The loading process is driven by config files, one per organism. See an annotated config example for details.
 * 
 * The config file parses an XML file which can set mining propeprties, which files to load etc
 * 
 * @author Adrian Tivey (art) 
 */
public class RunnerConfigParser {

    private static final String DEFAULT_DATA_PREFIX = "/nfs/pathdb/prod/data/input";
    //private static final String DEFAULT_DATA_PREFIX = "/Users/art/Documents/Data/chadoloading";

    private static final Set<String> VALID_OPTION_KEYS = new HashSet<String>();
    {
        VALID_OPTION_KEYS.addAll(Arrays.asList( new String[] {}));   
    }
    
    protected final Log logger = LogFactory.getLog(this.getClass());

    private String organismCommonName;

    private String configFilePath;

    private XPathEvaluator xPathEval;
    


    /**
     * Recurse through a directory tree, adding any files with the given extension to 
     * the list of filenames to process.
     * 
     * @param directory the directory to start from
     * @param extension the file extension to match on
     */
    private void addFilesFromDirectory(final File directory, final String extension, final RunnerConfig rc) {
	String[] names = directory.list(new FilenameFilter() {
	    public boolean accept(File file, String name) {
		if (file.isDirectory() && !file.equals(directory)) {
		    RunnerConfigParser.this.addFilesFromDirectory(file, extension, rc);
		} else {
		    if (name.endsWith("."+extension)) {
			return true;
		    }
		}
		return false;
	    }
	});
	for (String name : names) {
	    rc.getFileNames().add(directory.getAbsolutePath()+File.separatorChar+name);
	}
    }

    public void afterPropertiesSet() {
	if (this.configFilePath == null) {
	    this.configFilePath = DEFAULT_DATA_PREFIX + "/" + this.organismCommonName + "/" + this.organismCommonName + ".config.xml";
	}
    }


    @SuppressWarnings({ "unchecked", "cast" })
    private List<Element> elementListFromXPath(String pattern) {
	try {
	    return (List<Element>) this.xPathEval.evaluate(pattern);
	} catch (XPathException exp) {
	    System.err.println(exp);
	}
	catch (NullPointerException exp) {
	    System.err.println("NPE- Is the config file valid?");
	    System.exit(-1);
	}
	return Collections.EMPTY_LIST;
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

    public RunnerConfig getConfig() {
	RunnerConfig ret = new RunnerConfig();
	ret.setOrganismCommonName(organismCommonName);
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
	
	List<Element> nomenclatureOptions = this.elementListFromXPath("code/nomenclature-handler-options/*");
	if (nomenclatureOptions.size() > 0) {
	    Map<String, String> map = new HashMap<String, String>(0);
	    for (Element element : nomenclatureOptions) {
		map.put(element.getAttribute("key").getValue(), element.getAttribute("value").getValue());
	    }
	    ret.setNomenclatureOptions(map);
	}
    
    List<Element> propertyOptions = this.elementListFromXPath("code/options/*");
    if (propertyOptions.size() > 0) {
        Map<String, String> options = new HashMap<String, String>(0);
        for (Element element : propertyOptions) {
            String key = element.getAttribute("key").getValue();
            if (VALID_OPTION_KEYS.contains(key)) {
                options.put(key, element.getAttribute("value").getValue());  
            }
            logger.fatal("Found unrecognized option key '"+key+"'");
        }
        ret.setGeneralOptions(options);
    }
	
	List<Element> files = this.elementListFromXPath("inputs/file");
	for (Element element : files) {
	    String fileName = element.getAttribute("name").getValue();
	    if (fileName.startsWith("/")) {
		ret.getFileNames().add(fileName);
	    } else {
		System.err.println("fileName is '"+fileName+"'");
		File tmp = new File(configFile.getParentFile(), fileName);
		ret.getFileNames().add(tmp.getAbsolutePath());
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
        
	    String extension = element.getAttribute("extension").getValue();
	    this.addFilesFromDirectory(directory, extension, ret);
	}

	List<Element> synthetics = this.elementListFromXPath("inputs/synthetic");
	int syntheticCount = 0;
	for (Element element : synthetics) {
	    syntheticCount++;
	    Synthetic synthetic = new Synthetic();
	    ret.getSynthetics().add(synthetic);
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
	return ret;
//	System.exit(0);

    }
    
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


    public String getOrganismCommonName() {
        return this.organismCommonName;
    }



}
