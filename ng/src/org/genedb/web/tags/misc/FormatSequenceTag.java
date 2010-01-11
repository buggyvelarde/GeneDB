package org.genedb.web.tags.misc;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class FormatSequenceTag extends SimpleTagSupport {
    private String sequence;
    public void setSequence(String sequence) {
        this.sequence = sequence.toUpperCase();
    }

    private boolean includeSpaces = true;
    public void setIncludeSpaces(boolean includeSpaces) {
        this.includeSpaces = includeSpaces;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();

        for (int i = 0; i < sequence.length(); i += 60) {
            out.print("<div class=\"sequence\">");
            int endOfRow = i + 60;
            if (endOfRow > sequence.length()) {
                endOfRow = sequence.length();
            }
            for (int j = i; j < endOfRow; j += 10) {
                int endOfBlock = j + 10;
                if (endOfBlock > endOfRow) {
                    endOfBlock = endOfRow;
                }
                String block = sequence.substring(j, endOfBlock);
                out.print(block);
                if (includeSpaces) {
                    out.print("&nbsp;");
                }
            }
            out.print("</div>");
        }
    }

}
