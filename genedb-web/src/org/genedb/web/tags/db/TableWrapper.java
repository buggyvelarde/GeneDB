package org.genedb.web.tags.db;

import org.displaytag.decorator.TableDecorator;
import org.gmod.schema.utils.Product;

public class TableWrapper extends TableDecorator {
	
	public String getdisplayTerm(){
		Product product = (Product) getCurrentRowObject();
		String name = product.getName();
		return "<a href=\"./FeatureByCvTermNameAndCvName?name=" + name + "&cvName=genedb_products" + "\">" + name + "</a>";
	}

}
