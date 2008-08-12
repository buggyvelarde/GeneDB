package org.genedb.web.tags.db;

import org.gmod.schema.utils.CountedName;

import org.displaytag.decorator.TableDecorator;

public class TableWrapper extends TableDecorator {
    
    public String getdisplayTerm(){
        CountedName product = (CountedName) getCurrentRowObject();
        String name = product.getName();
        return "<a href=\"./FeatureByCvTermNameAndCvName?name=" + name + "&cvName=genedb_products" + "\">" + name + "</a>";
    }

}
