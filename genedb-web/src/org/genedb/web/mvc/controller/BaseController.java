package org.genedb.web.mvc.controller;

import org.genedb.db.taxon.TaxonNodeListFormatter;
import org.genedb.db.taxon.TaxonNodeManager;
import org.genedb.util.Pair;

import org.apache.log4j.Logger;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.Set;

import com.google.common.collect.Sets;

public class BaseController {

    protected Logger logger = Logger.getLogger(BaseController.class);

    private TaxonNodeListFormatter taxonNodeListFormatter;

    private TaxonNodeManager taxonNodeManager;

    private Set<String> validExtensions = Sets.newHashSet();

    public Pair<String, String> parseExtension(String argument) {
        if (argument != null) {
            int extensionStart = argument.lastIndexOf('.');
            if (extensionStart > 0 && extensionStart < argument.length()) {
                String firstPart = argument.substring(0, extensionStart);
                String extension = argument.substring(extensionStart+1);
                logger.warn("The extension is '"+extension+"'");
                if (validExtensions.contains(extension)) {
                    return new Pair<String, String>(firstPart, extension);
                }
            }
            return new Pair<String, String>(argument, "");
        }
        return null;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.getFormatterRegistry().addFormatterByType(taxonNodeListFormatter);
    }



    public void setTaxonNodeListFormatter(TaxonNodeListFormatter taxonNodeListFormatter) {
        this.taxonNodeListFormatter = taxonNodeListFormatter;
    }

    public void setValidExtensions(Set<String> validExtensions) {
        this.validExtensions = validExtensions;
    }

    public void setTaxonNodeManager(TaxonNodeManager taxonNodeManager) {
        this.taxonNodeManager = taxonNodeManager;
    }

    protected TaxonNodeManager getTaxonNodeManager() {
        return taxonNodeManager;
    }

}
