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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The loading process is driven by config files, one per organism. See an annotated config example for details.
 * 
 * The config file parses an XML file which can set mining propeprties, which files to load etc
 * 
 * @author Adrian Tivey (art) 
 */
public class RunnerConfig {

    protected final Log logger = LogFactory.getLog(this.getClass());

    private String organismCommonName;
    
    private List<String> fileNames = new ArrayList<String>(0);

    private List<Synthetic> synthetics = new ArrayList<Synthetic>();

    private String nomenclatureHandlerName = null;

    public List<String> getFileNames() {
	return this.fileNames;
    }

    public String getNomenclatureHandlerName() {
	return nomenclatureHandlerName ;
    }

    public String getOrganismCommonName() {
        return this.organismCommonName;
    }

    public List<Synthetic> getSynthetics() {
        return this.synthetics;
    }

    public void setOrganismCommonName(String commonOrganismName) {
	this.organismCommonName = commonOrganismName;
    }

    public void setNomenclatureHandlerName(String nomenclatureHandlerName) {
        this.nomenclatureHandlerName = nomenclatureHandlerName;
    }


}
