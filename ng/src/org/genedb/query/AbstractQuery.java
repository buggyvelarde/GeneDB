package org.genedb.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Basic, abstract implementation of Query, designed to be extended.
 * 
 * @author art
 */
public abstract class AbstractQuery implements QueryI {

    private String name;
    private List<Param> params;
    private boolean inHistory;
    private String type;
//  private StorageMethod storageMethod;
    private Detailer summaryDetailer;
    private Detailer fineDetailer;
    private String simpleDescription;
    private String help;

    public String getSimpleDescription() {
    return this.simpleDescription;
    }

    protected void setSimpleDescription(String simpleDescription) {
    this.simpleDescription = simpleDescription;
    }

    protected String getHelp() {
    return this.help;
    }

    protected void setHelp(String help) {
    this.help = help;
    }

    protected void setName(String name) {
    this.name = name;
    }

    public String getName() {
    return name;
    }

    public String getResultType() {
    return type;
    }

    public boolean isStoredInHistory() {
    return inHistory;
    }

    public String getQueryAsString() {
    // TODO Deal with query context perhaps
    StringBuffer ret = new StringBuffer();
    ret.append(name);
    //ret.append(queryContext);
    ret.append("{");
    for (Param param : params) {
        ret.append("{");
        ret.append(param.getName());
        ret.append("=");
        ret.append(param.getValue());
        ret.append("}");            
    }
    ret.append("}");

    return ret.toString();
    }

    public void setFineDetailer(Detailer fineDetailer) {
    this.fineDetailer = fineDetailer;
    }

    public void setSummaryDetailer(Detailer summaryDetailer) {
    this.summaryDetailer = summaryDetailer;
    }

    protected Detailer getFineDetailer() {
    return fineDetailer;
    }

    protected Detailer getSummaryDetailer() {
    return summaryDetailer;
    }

    List<Param> EMPTY_PARAMS_LIST = Collections.unmodifiableList(new ArrayList<Param>());

    public List<Param> getParameters() {
    if (this.params == null) {
        return EMPTY_PARAMS_LIST;
    }
    return this.params;
    }

    //public abstract void writeSpringBean(PrintWriter pw);
    public boolean isComplete() {
    for (Param param : params) {
        if (!param.isSet()) {
        return false;
        }
    }
    return true;
    }

}
