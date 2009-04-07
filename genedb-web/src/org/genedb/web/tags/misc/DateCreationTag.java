package org.genedb.web.tags.misc;

import static javax.servlet.jsp.PageContext.PAGE_SCOPE;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class DateCreationTag extends SimpleTagSupport {

    private static final Logger logger = Logger.getLogger(DateCreationTag.class);

    private int time;

    private String varName;

    @Override
    public void doTag() throws JspException, IOException {


        Date date = new Date(time);
        PageContext pc = (PageContext) getJspContext();
        logger.error("Creating a new date from a value of '"+time+"'");

        pc.setAttribute(varName, date, PAGE_SCOPE);
    }


    public int getTime() {
        return time;
    }


    public void setTime(int time) {
        this.time = time;
    }


    public String getVarName() {
        return varName;
    }


    public void setVarName(String varName) {
        this.varName = varName;
    }

}
