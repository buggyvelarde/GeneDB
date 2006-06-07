package org.genedb.web.tags.bool;

import org.genedb.query.BasicQueryI;
import org.genedb.query.NumberedQueryI;
import org.genedb.query.QueryI;
import org.genedb.query.bool.BooleanQuery;
import org.genedb.query.bool.BooleanQueryNode;
import org.genedb.web.mvc.controller.QueryForm;
import org.genedb.web.mvc.controller.WebConstants;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class BooleanQuestionTag extends SimpleTagSupport {

	private static String[] CELL_COLOURS = {"#FFFFFF", "#CCCCCC", "#999999", "#666666", "#333333"}; 
	private static int NUM_CELL_COLS = 5;
	
	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void doTag() throws JspException, IOException {
		JspWriter out = getJspContext().getOut();
		out.println("<form name=\""+id+"\" method=\"POST\">\n"+TABLE_START);
		Map map = (Map) getJspContext().getAttribute(WebConstants.MODEL_MAP, PageContext.REQUEST_SCOPE);
		QueryForm qf = (QueryForm) map.get(WebConstants.QUERY_FORM);
		NumberedQueryI nq = qf.getNumberedQueryI();
		out.print("<p>Question: " + nq.getSimpleDescription()+"</p>");
		out.println("<input type=\"hidden\" name=\"taxId\" value=\""+map.get(WebConstants.TAX_ID)+"\" />");
		StringWriter buf = new StringWriter();
		showBoolOrSingleQuery(nq, buf, 0);
		out.print(buf.toString());
		out.println("</table>\n");
		
		showSimpleOrAdvancedOption(out, qf);
		
		out.println("<p /><input type=\"submit\" name=\"runquery\" value=\"Run\" />\n</form><p>&nbsp;</p>");
	}
	
	private void showSimpleOrAdvancedOption(JspWriter out, QueryForm qf) throws IOException {
		out.print("<p><input type=\"submit\" name=\"advanced\" value=\"Switch form to ");
		if (qf.isAdvanced()) {
			out.print(" simple");
		} else {
			out.print(" advanced");
		}
		out.println(" form\" /></p>");
	}

	private void showBoolOrSingleQuery(NumberedQueryI nq, StringWriter buf, int depth) throws IOException {
		if (nq instanceof BooleanQuery) {
			showBooleanQuery((BooleanQuery) nq, buf, depth);
		} else {
			showQuestion(nq, buf);
		}	
	}
	
	private static final String TABLE_START = "<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\">";
	//private static final String BORDER_STYLE = "double red 3px";
	private static final String BORDER_STYLE = "solid black 2px";

	
	private void showBooleanQuery(BooleanQuery q, StringWriter buf, int depth) throws IOException {
			depth++;
			
			String colourCell = "bgcolor=\""+CELL_COLOURS[depth % NUM_CELL_COLS]+"\">";
			String colouredCell = "<td "+colourCell;
			String prevColouredCell = "<td bgcolor=\""+CELL_COLOURS[(depth-1) % NUM_CELL_COLS]+"\">";
			
			buf.append("\n"+TABLE_START+"\n<tr>");
			buf.append("<td style=\"border-left:"+BORDER_STYLE+"\" "+colourCell);
			buf.append("&nbsp;</td>");
			buf.append("<td style=\"border-right:"+BORDER_STYLE+"\" "+colourCell);

			showBoolOrSingleQuery(q.getFirstQuery(), buf, depth);
			buf.append("</td>"+prevColouredCell+"&nbsp;</td></tr>\n<tr><td style=\"border-left:"+BORDER_STYLE+"; border-right:"+BORDER_STYLE+"\" colspan=\"2\" bgcolor=\"");
			buf.append(CELL_COLOURS[depth % NUM_CELL_COLS]);
			buf.append("\">");
			
			StringWriter buf2 = new StringWriter();
			showBoolOrSingleQuery(q.getSecondQuery(), buf2, depth);
			
			buf.append(q.getOp().name());
			buf.append("<input type=\"hidden\" name=\"node."+q.getIndex()+"\" value=\"bool."+q.getOp()+"."+q.getFirstQuery().getIndex()+"."+q.getSecondQuery().getIndex()+"\"></input>");
			buf.append("</td>"+prevColouredCell);
			showIcons(buf, q.getIndex());
			buf.append("</tr>\n<tr>");
			buf.append("<td style=\"border-left:"+BORDER_STYLE+"\" "+colourCell);
			buf.append("&nbsp;</td>");
			buf.append("<td style=\"border-right:"+BORDER_STYLE+"\" "+colourCell);

			buf.append(buf2.toString());
			buf.append("</td>"+prevColouredCell+"&nbsp;</td></tr>\n</table>");
	}
	
	// TODO Change to two sets of buttons : union this query, union this group etc

	private void showQuestion(NumberedQueryI nq, StringWriter buf) throws IOException {
		//QueryBroken q = (QueryBroken) query
		int index = nq.getIndex();
		if (nq!=null) {
			buf.append(nq.getSimpleDescription());
			buf.append("<input type=\"hidden\" name=\"node."+index+"\" value=\""+nq.getName()+"\" />");
		} else {
			showQuestionList(buf, index);
		}
		showIcons(buf, index);
	}

	private void showQuestionList(StringWriter buf, int index) {
		buf.append("<select onchange=\"document.forms['"+id+"'].submit()\" name=\"node."+index+"\">");
		buf.append("<option value=\"none\">Choose...</option>");
		buf.append("<option value=\"chromosome\">Genes on chromosome</option>");
		buf.append("<option value=\"tm\">Genes with TM</option>");
		buf.append("<option value=\"go\">Genes on go acc</option>");
		buf.append("</select>");
	}
	
	private void showIcons(StringWriter buf, int localIndex) throws IOException {
		PageContext pc = (PageContext) getJspContext();
		HttpServletRequest req = (HttpServletRequest) pc.getRequest();
		String contextPath = req.getContextPath();
		// Remove set from icon name for text versions
		buf.append("<input type=\"IMAGE\" name=\"bop.union."+localIndex+"\" src=\""+contextPath+"/includes/images/unionset.png\" alt=\"UNION\" title=\"UNION\"/>");
		buf.append("<input type=\"IMAGE\" name=\"bop.intersect."+localIndex+"\" src=\""+contextPath+"/includes/images/intersectset.png\" alt=\"INTERSECT\" title=\"INTERSECT\"/>");
		buf.append("<input type=\"IMAGE\" name=\"bop.subtract."+localIndex+"\" src=\""+contextPath+"/includes/images/subtractset.png\" alt=\"SUBTRACT\" title=\"SUBTRACT\"/>");
	}

}
