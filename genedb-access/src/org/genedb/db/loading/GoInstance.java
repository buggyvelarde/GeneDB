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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * This class represents a specific GO entry 
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */
public class GoInstance {

    private List<String> qualifiers = new ArrayList<String>(0);
    private String id;
    private String ref;
    private String withFrom;
    private GoEvidenceCode evidence;
    private String subtype;
    private String name;
    private String date;
    private String attribution;
    private boolean fromEMBL = false;

    protected static final Log logger = LogFactory.getLog(GoInstance.class);
    
    private static String today;
    
    private static GoQualifierDictionary qualifierDictionary = new GoQualifierDictionary(); 

    
    static {

        DateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
        today = dFormat.format(new Date());

    }

    /**
     * Get the value of date.
     *
     * @return value of date.
     */
    public String getDate() {
        if ( date == null) {
            return today;
        }
        return date;
    }

    /**
     * Set the value of date.
     *
     * @param v Value to assign to date.
     */
    public void setDate(String v) {
        if ( v != null && v.length() == 8) {
            this.date = v;
        } else {
            throw new IllegalArgumentException("Date is null or longer than 8 characters ("+v+")");
        }
    }

    /**
     * Get the value of subtype.
     *
     * @return value of subtype.
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Set the value of subtype.
     *
     * @param subtype Value to assign to subtype.
     */
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return StringUtilities.emptyOrValue(name);
    }

    /**
     * Get the value of qualifier.
     *
     * @return Value of qualifier.
     */
    public List<String> getQualifierList() {
        if (qualifiers==null) {
            return Collections.unmodifiableList(new ArrayList<String>(0));
        }
        return qualifiers;
    }

    /**
     * Set the value of qualifier.
     *
     * @param v Value to assign to qualifier.
     */
    public void addQualifier(String q) {
    	if (q.indexOf("|")==-1) {
    		qualifiers.add(q);
    	} else {
    		String[] quals = q.split("\\|");
    		for (int i = 0; i < quals.length; i++) {
    				String qual = quals[i];
    				qualifiers.add(qual);
			}
    	}
    }

    private boolean checkQualifier(String qualifier) {
    		if (qualifierDictionary.isQualifierGOValid(qualifier)) {
    			return true;
    		}
    		if (qualifierDictionary.isQualifierPSUValid(qualifier)) {
    			return true;
    		}
    		return false;
    }
    
    public String getQualifierDisplay(boolean officialOnly, String def, String seperator, boolean replaceUnderscore) {
        StringBuffer ret = new StringBuffer();
        List<String> allQualifier = getQualifierList();
        if (allQualifier == null || allQualifier.size()==0) {
            return def;
        }
        List<String> filteredList = null;
        if (!officialOnly) {
            filteredList = allQualifier;
        } else {
            filteredList = new ArrayList<String>();
            for (Iterator it = allQualifier.iterator(); it.hasNext();) {
                String qualifier = (String) it.next();
                if (qualifierDictionary.isQualifierGOValid(qualifier)) {
                    filteredList.add(qualifier);
                }
            }
        }
        if (filteredList==null || filteredList.size()==0) {
            return def;
        }
        for (Iterator it = filteredList.iterator(); it.hasNext();) {
        	Object q = it.next();
            String qualifier = (String) q;
            if (ret.length() != 0) {
                ret.append(seperator);
            }
            if (replaceUnderscore) {
            	ret.append(qualifier.replaceAll("_", " "));
            } else {
            	ret.append(qualifier);
            }
        }
        return ret.toString();
    }

    public void setWithFrom(String withFrom, GoEvidenceCode evidence) { 
    	switch (evidence) {
    	case IC:
    		this.withFrom = withFrom;
    		break;
    	case IGI:
    	case IPI:
    	case IEA:
    	case ISS:
    		this.withFrom = withFrom;
    		break;
    	case IDA:
    	case IEP:
    	case IMP:
    	case NAS:
    	case ND:
    	case NR:
    	case RCA:
    	case TAS:
    		logger.warn("Attempting to set with/from for evidence code of '"+evidence.getDescription()+"'");
    		break;
    	}
    }
    
    public void setWithFrom(String withFrom) {
    	this.withFrom = withFrom;
    }
    
    /**
     * Get the accesion number of the Classification.
     *
     * @return The accession number
     */
    public String getId() {
        return StringUtilities.emptyOrValue(id);
    }

    /**
     * Set the accession number of the entry
     *
     * @param v The accesion number
     */
    public void setId(String id) {
	if (id.length() !=7) {
	    throw new IllegalArgumentException("GO id doesn't look right '"+id+"'");
	}
        this.id = id;
    }

    /**
     * Get the value of ref.
     *
     * @return Value of ref.
     */
    public String getRef() {
        return StringUtilities.emptyOrValue(ref);
    }

    /**
     * Set the value of ref.
     *
     * @param v Value to assign to ref.
     */
    public void setRef(String v) {
        this.ref = v;
    }


    /**
     * Get the value of evidence.
     *
     * @return Value of evidence.
     */
    public GoEvidenceCode getEvidence() {
        return evidence;
    }

    /**
     * Set the value of evidence.
     *
     * @param v Value to assign to evidence.
     */
    public void setEvidence(GoEvidenceCode evidence) {
	this.evidence = evidence;
    }

    /**
     * Get the value of aspect.
     *
     * @return Value of aspect.
     */
    public String getAspect() {
        return getSubtype().substring(0, 1).toUpperCase();
    }


    /**
     * Get the value of fromAssoc.
     *
     * @return Value of fromAssoc.
     */
    public boolean isFromEMBL() {
        return fromEMBL;
    }

    /**
     * Set the value of fromAssoc.
     *
     * @param v Value to assign to fromAssoc.
     */
    public void setFromEMBL(boolean v) {
        this.fromEMBL = v;
    }

	/**
	 * @return
	 */
	public String getQualifierString() {
        return qualifierDictionary.getQualifierString(qualifiers);
	}

	/**
	 * 
	 */
	public void validate() {
	    for (Iterator it = qualifiers.iterator(); it.hasNext();) {
		String qualifier = (String) it.next();
		if (!checkQualifier(qualifier)) {
		    System.err.println("WARN: Ignoring invalid GO qualifier '"+qualifier+"' in "+id);
		    it.remove();
		}
	    }
	}

    /**
     * @return
     */
    public boolean isWith() {
        if (GoEvidenceCode.IC.equals(getEvidence())) {
            return false;
        }
        return true;
    }

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}

	public String getWithFrom() {
	    return this.withFrom;
	}

}
