package org.genedb.web.mvc.controller;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeManager;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 *
 *
 * @author Adrian Tivey
 */
@Controller
@RequestMapping("/gmodrest/v1/")
public class GmodRestControllerV1 {

    private static final String DEFAULT_VIEW = "xml:";

    private Logger logger = Logger.getLogger(GmodRestControllerV1.class);

    private TaxonNodeManager tnm;


    @RequestMapping(method=RequestMethod.GET, value={"/organisms", "/organisms.*"})
    public String listOrganisms(ModelMap modelMap) {

        OrganismResultSet ors = new OrganismResultSet();
        TaxonNode root = tnm.getTaxonNodeForLabel("Root");
        for (TaxonNode node : root.getAllChildren()) {
            if (node.isOrganism()) {
                Organism o = new Organism(node.getLabel(), node.getLabel(), node.getTaxonId());
                ors.getOrganisms().add(o);
            }
        }
        //model.put("version", 1);
        logger.error("The size of modelmap before is '"+modelMap.size()+"'");
        modelMap.addAttribute("resultset", ors);
        logger.error("The size of modelmap after is '"+modelMap.size()+"'");
        return DEFAULT_VIEW;
    }

    @RequestMapping(method=RequestMethod.GET, value="/fulltext/gene/{searchTerm}")
    public ModelAndView limitedFullTextSearch(HttpServletRequest request,HttpServletResponse response,
            @PathVariable("searchTerm") String searchTerm) {
        logger.error("fulltext called with '"+searchTerm+"'");
        return notImplemented();
    }

    private ModelAndView notImplemented() {
        return null;
    }

    @RequestMapping(method=RequestMethod.GET, value="/location/chromosome/{contigName}")
    public ModelAndView byLocationSearch(HttpServletRequest request,HttpServletResponse response,
            @PathVariable("contigName") String contigName) {
        logger.error("location called with '"+contigName+"'");
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(method=RequestMethod.GET, value="/ontology/gene/{ontologyId}")
    public ModelAndView ontologySearch(HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("ontologyId") String ontologyId) {
        boolean not = ontologyId.startsWith("!");
        logger.error("ontology called with '"+ontologyId+"'");
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(method=RequestMethod.GET, value="/orthologs/gene/{geneIdentifier}")
    public ModelAndView orthologSearch(HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("geneIdentifier") String geneIdentifier) {
        logger.error("orthology called with '"+geneIdentifier+"'");
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(method=RequestMethod.GET, value="/orthologs/organism/{organismFrom}/to/{organismList}")
    public ModelAndView organismOrthologSearch(HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("organismFrom") String organismFrom,
            @PathVariable("organismList") String organismList) {
        logger.error("organism orthology called for '"+organismFrom+"' and '"+organismList+"'");
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(method=RequestMethod.GET, value="/fetch/{geneIdentifier}")
    public ModelAndView fetchGene(HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("geneIdentifier") String id) {
        logger.error("genefetch called with '"+id+"'");
        return new ModelAndView("redirect:/");
    }

    public void setTaxonNodeManager(TaxonNodeManager tnm) {
        this.tnm = tnm;
    }

}

@XStreamAlias("organism")
class Organism {
    private String genus;
    private String organism;
    private String taxonId;

    public Organism(String genus, String organism, String taxonId) {
        this.genus = genus;
        this.organism = organism;
        this.taxonId = taxonId;
    }

    public String getGenus() {
        return genus;
    }

    public String getOrganism() {
        return organism;
    }

    public String getTaxonomy_id() {
        return taxonId;
    }
}

class OrganismResultSet extends BaseResultSet {

    @XStreamImplicit(itemFieldName="organism")
    private List<Organism> organisms = Lists.newArrayList();

    public List<Organism> getOrganisms() {
        return organisms;
    }

}

@XStreamAlias("resultset")
class BaseResultSet {
    public String version = "1";
}

