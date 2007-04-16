package org.genedb.web.tags.db;

import java.io.IOException;

import java.util.List;
import javax.servlet.jsp.JspWriter;
import org.genedb.db.loading.TaxonNode;
import org.genedb.web.menu.CompositeMenu;
import org.genedb.web.menu.Menu;
import org.genedb.web.menu.SimpleMenu;


public class DisplayPhylogeny extends AbstractHomepageTag{
	
	int top = 200;
	int left = 154;
	
	private void buildMenu(TaxonNode node, CompositeMenu comSrc) {
		List<TaxonNode> childs = node.getChildren();
		for (TaxonNode child : childs) {
			if(child.isLeaf()) {
				SimpleMenu sm = new SimpleMenu(Integer.toString(Menu.counter++),child.getLabel(),null);
				comSrc.add(sm);
			} else {
				CompositeMenu parentMenu = new CompositeMenu(Integer.toString(Menu.counter++),child.getLabel(),null,false);
				comSrc.add(parentMenu);
				buildMenu(child,parentMenu);
			}
		}
	}
	
	@Override
	protected void display(TaxonNode node, JspWriter out, int indent) throws IOException {
		Menu.left = this.left;
		Menu.top = this.top;
		List<TaxonNode> nodes = node.getChildren();
		StringBuffer sb = new StringBuffer();
		StringBuffer top = new StringBuffer();
        int j=1;
        sb.append("<div id=\"start\" onmouseover=\"showfirst();\" onmouseout=\"hidefirst();\" style=\"position: relative; border: 1px solid black; width: 154px; background: rgb(153, 153, 255);\"><a href=\"null\" style=\"text-decoration: none; color: white;\">Start</a>");
        for (TaxonNode tnode : nodes) {
        	CompositeMenu menu = new CompositeMenu(Integer.toString(Menu.counter++),tnode.getLabel(),null,true);
			top.append(Menu.counter-1);
			top.append(",");
			menu.setLevelCoord(Integer.toString(j));
			j++;
			buildMenu(tnode,menu);
			sb.append(menu.render(j-1));
		}
		top.deleteCharAt(top.length()-1);
		//System.out.println(sb.toString());
		sb.append("<input type=\"hidden\" id=\"itemsLength\" value=\"" + Menu.counter + "\"/>");
		sb.append("<input type=\"hidden\" id=\"topItems\" value=\"" + top.toString() + "\"/>");
		sb.append("<script type=\"text/javascript\" src=\"/genedb-web/includes/scripts/phylogeny.js\"/></script>");
		Menu.counter = 0;
		out.print(sb.toString());
	}
}


