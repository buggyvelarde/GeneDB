/*
 * Copyright (c) 2002 Genome Research Limited.
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

import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;


/**
 * This class stores a list of GO and PSU valid values for the GO qualifier column
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */
public class GoQualifierDictionary {

    private Set<String> officialQualifiers;
    private Set<String> psuQualifiers = new HashSet<String>();

    private void initOfficialQualifiers() {
	officialQualifiers = new HashSet<String>();
	officialQualifiers.add("NOT");
	officialQualifiers.add("contributes_to");
	officialQualifiers.add("colocalizes_with");
    }

    private static final String GO_QUALIFIER_FILENAME = "/nfs/disk222/yeastpub/analysis/yeast/pombe/CONTIGS/Docs/PSU_GO_qualifiers.txt";

    private void initPsuQualifiers() {
	FileReader r = null;
	try {
	    r = new FileReader(GO_QUALIFIER_FILENAME);
	    GoQualifierParser gqp = new GoQualifierParser(r);
	    gqp.go();
	} catch (FileNotFoundException e) {
	    System.err.println("ERROR: Can't load Val's qualifier file: '"+GO_QUALIFIER_FILENAME+"'");
	}
    }


    GoQualifierDictionary() {
	initOfficialQualifiers();
	initPsuQualifiers();
    }


    /**
     * @return
     */
    public boolean isQualifierPSUValid(String qualifier) {
	if (qualifier == null) {
	    return false;
	}
	if ( !(psuQualifiers.contains(qualifier) || officialQualifiers.contains(qualifier))) {
	    return false;
	}
	return true;
    }

    public boolean isQualifierGOValid(String qualifier) {
	// TODO contributes_to only valid for function, colocalizes_with for component
	if (qualifier == null) {
	    return false;
	}
	if ( !officialQualifiers.contains(qualifier)) {
	    return false;
	}
	return true;
    }

    /**
     * @return
     */
    public String getQualifierString(List<String> qualifier) {
	if (qualifier == null || qualifier.size()==0) {
	    return StringUtilities.EMPTY_STRING;
	}
	return StringUtils.collectionToDelimitedString(qualifier, "|");
    }



    public class GoQualifierParser extends DefaultHandler implements ErrorHandler {

	private Reader reader;

	public GoQualifierParser(Reader r) {
	    this.reader = r;
	}

	private StringBuilder accumulator = new StringBuilder();

	// When the parser encounters plain text (not XML elements), it calls
	// this method, which accumulates them in a string buffer.
	// Note that this method may be called multiple times, even with no
	// intervening elements.
	@Override
    public void characters(char[] buffer, int start, int length) {
	    accumulator.append(buffer, start, length);
	}


	@Override
    public void warning(SAXParseException exception) {
	    System.err.println("WARN: GOParser line " + exception.getLineNumber() + ": " +
		    exception.getMessage());
	}


	@Override
    public void error(SAXParseException exception) {
	    System.err.println("ERROR: GOParser line " + exception.getLineNumber() + ": " +
		    exception.getMessage());
	}

	// Report a non-recoverable error and exit
	@Override
    public void fatalError(SAXParseException exception) throws SAXException {
	    System.err.println("FATAL: GOParser line " + exception.getLineNumber() + ": " +
		    exception.getMessage());
	    throw(exception);
	}

	public void go() {

	    try {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		// Create the builder and parse the file
		factory.newSAXParser().parse(new InputSource(reader), this);
	    } catch (SAXException se) {
		System.err.println(se.getMessage());
		System.exit(1);
	    } catch (Throwable t) {
		System.err.println(t);
		t.printStackTrace();
	    }
	}


	// At the beginning of each new element, erase any accumulated text.
	@Override
    public void startElement(String namespaceURL, String localName,
		String qname, Attributes attributes) {
	    accumulator.setLength(0);
	}


	// Take special action when we reach the end of selected elements.
	@Override
    public void endElement(String namespaceURL, String localName, String qname) {
	    if ( localName.trim().equals("name")) {
		String field = accumulator.toString().trim();
		psuQualifiers.add( field );
	    }
	}
    }

}
