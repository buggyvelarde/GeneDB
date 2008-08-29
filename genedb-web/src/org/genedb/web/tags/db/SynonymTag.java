package org.genedb.web.tags.db;

import org.gmod.schema.mapped.FeatureSynonym;
import org.gmod.schema.mapped.Synonym;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class SynonymTag extends SimpleTagSupport {

    /*
     * cf. org.genedb.db.loading.EmblQualifiers.
     *
     * I assume that the same constants are being used for
     * EMBL qualifiers and for the corresponding CV terms.
     * Someone who understands the intention (i.e. art) should
     * have a look at this at some point. -rh11
     */
    private static final String QUAL_SYS_ID        = "systematic_id";
    private static final String QUAL_TEMP_SYS_ID   = "temporary_systematic_id";

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
        for (FeatureSynonym featSynonym : collection) {
            Synonym synonym = featSynonym.getSynonym();
            if (name.equals(synonym.getType().getName())) {
                filtered.add(featSynonym);
            } else {
                if (name.equals(QUAL_SYS_ID)
                        && QUAL_TEMP_SYS_ID.equals(synonym.getType().getName())) {
                    filtered.add(featSynonym);
                    if (tmpSysId != null) {
                        getJspContext().setAttribute(tmpSysId, Boolean.TRUE);
                    }
                }
            }
        }
        if (filtered.size() > 0) {
            JspWriter out = getJspContext().getOut();
            getJspContext().setAttribute(var, filtered);
            getJspBody().invoke(out);
            getJspContext().removeAttribute(var);
        }
    }
}
