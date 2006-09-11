package org.genedb.web.tags.db;

import static org.genedb.db.loading.EmblQualifiers.QUAL_SYS_ID;
import static org.genedb.db.loading.EmblQualifiers.QUAL_TEMP_SYS_ID;

import org.gmod.schema.sequence.FeatureSynonym;
import org.gmod.schema.sequence.Synonym;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class SynonymTag extends SimpleTagSupport {
	
	private Collection<FeatureSynonym> collection;
	private String name;
    private String var;
    private String tmpSysId;


	public void setTmpSysId(String tmpSysId) {
        this.tmpSysId = tmpSysId;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setCollection(Collection<FeatureSynonym> collection) {
        this.collection = collection;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
	public void doTag() throws JspException, IOException {
        List<FeatureSynonym> filtered = new ArrayList<FeatureSynonym>();
        System.err.println("The size of collection is '"+collection.size()+"'");
        for (FeatureSynonym featSynonym : collection) {
            Synonym synonym = featSynonym.getSynonym();
            if (name.equals(synonym.getCvTerm().getName())) {
                filtered.add(featSynonym);
            } else {
                if (name.equals(QUAL_SYS_ID) && QUAL_TEMP_SYS_ID.equals(synonym.getCvTerm().getName())) {
                    filtered.add(featSynonym);
                    if (tmpSysId != null) {
                        getJspContext().setAttribute(tmpSysId, Boolean.TRUE);
                    }
                }
            }
        }
        System.err.println("The size of synonym filtered is '"+filtered.size()+"' for '"+name+"'");
        if (filtered.size()>0) {
            JspWriter out = getJspContext().getOut();
            getJspContext().setAttribute(var, filtered);
            getJspBody().invoke(out);
            getJspContext().removeAttribute(var);
        }
	}
	
//		PageContext pc = (PageContext) getJspContext();
//		HttpServletRequest req = (HttpServletRequest) pc.getRequest();
//		String contextPath = req.getContextPath();

}
