package org.genedb.web.tags.db;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.PhylogenyDao;
import org.gmod.schema.phylogeny.Phylonode;

public class DisplayPhylogeny extends SimpleTagSupport{
	
	private PhylogenyDao phylogenyDao; 
	private OrganismDao organismDao; 

	@Override
    public void doTag() throws JspException, IOException {
		phylogenyDao = new PhylogenyDao();
		organismDao = new OrganismDao();
		
		List<Phylonode> topNodes = this.phylogenyDao.getPhylonodeByDepthAndParent(2, null);
		JspWriter out = getJspContext().getOut();
		
		for (Phylonode topNode : topNodes) {
			drawNode(topNode,null,out);
			getNodes(2,topNode,out);
		}
		
		out.close();
	}
	
	private void getNodes(double depth,Phylonode parent,JspWriter out) {
		List<Phylonode> topNodes = this.phylogenyDao.getPhylonodeByDepthAndParent(++depth, parent);
		for (Phylonode topNode : topNodes) {
				if (this.organismDao.getOrganismByCommonName(topNode.getLabel()) != null) {
					drawNode(topNode,parent,out);
				} else {
					drawNode(topNode,parent,out);
					getNodes(depth,topNode,out);
				}
		}
		
	}

	private void drawNode(Phylonode node, Phylonode parent,JspWriter out) {
		double depth = node.getDistance();
		for (double i = 1; i<depth; i++) {
			try {
				out.print("\t");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			out.println(node.getLabel());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}


