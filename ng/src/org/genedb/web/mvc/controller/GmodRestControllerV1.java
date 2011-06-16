//package org.genedb.web.mvc.controller;
//
//import org.genedb.db.dao.SequenceDao;
//import org.genedb.db.taxon.TaxonNode;
//import org.genedb.db.taxon.TaxonNodeManager;
//import org.genedb.querying.core.NumericQueryVisibility;
//import org.genedb.querying.core.QueryException;
//import org.genedb.querying.core.QueryFactory;
//import org.genedb.querying.tmpquery.ControlledCurationQuery;
//import org.genedb.querying.tmpquery.GeneLocationQuery;
//import org.genedb.querying.tmpquery.GenesByDbQuery;
//import org.genedb.web.mvc.model.BerkeleyMapFactory;
//import org.genedb.web.mvc.model.TranscriptDTO;
//
//import org.gmod.schema.mapped.Feature;
//
//import org.apache.log4j.Logger;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.ModelMap;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import java.util.List;
//
//import com.google.common.collect.Lists;
//import com.thoughtworks.xstream.annotations.XStreamAlias;
//import com.thoughtworks.xstream.annotations.XStreamImplicit;
//
///**
// *
// *
// * @author Adrian Tivey
// */
//@Controller
//@RequestMapping("/gmodrest/v1/")
//public class GmodRestControllerV1 {
//
//    private static final String DEFAULT_VIEW = "xml:";
//
//    private Logger logger = Logger.getLogger(GmodRestControllerV1.class);
//
//    private TaxonNodeManager tnm;
//
//    private SequenceDao sequenceDao;
//
//    private BerkeleyMapFactory bmf;
//
//    private QueryFactory<NumericQueryVisibility> queryFactory;
//
//
//    @RequestMapping(method=RequestMethod.GET, value={"/organisms", "/organisms.*"})
//    public String listOrganisms(ModelMap modelMap) {
//
//        OrganismResultSet ors = new OrganismResultSet();
//        TaxonNode root = tnm.getTaxonNodeForLabel("Root");
//        for (TaxonNode node : root.getAllChildren()) {
//            if (node.isOrganism()) {
//                Organism o = new Organism(node.getLabel(), node.getLabel(), node.getTaxonId());
//                ors.getOrganisms().add(o);
//            }
//        }
//        modelMap.addAttribute("resultset", ors);
//        return DEFAULT_VIEW;
//    }
//
//    // Not implemented - not to be
//    @RequestMapping(method=RequestMethod.GET, value="/fulltext/gene/{searchTerm}")
//    public String limitedFullTextSearch(ModelMap modelMap,
//            @PathVariable("searchTerm") String searchTerm) {
//        notImplemented(modelMap, " with full-text");
//        return DEFAULT_VIEW;
//    }
//
//    //---------------------------------------------------------------
//
//    @RequestMapping(method=RequestMethod.GET, value="/location/chromosome/{contigName}/gene")
//    public String byLocationSearch(ModelMap modelMap,
//            @PathVariable("contigName") String contigName) {
//
//        /* http://flybase.org/gmodrest/v1/location/chromosome/X/gene - Find all genes that are localized to the X chromosome of any Drosophila species.
//        http://flybase.org/gmodrest/v1/location/chromosome/X/gene/organism/7227 - Find all genes that are localized to the X chromosome of melanogaster.
//        http://flybase.org/gmodrest/v1/location/chromosome/X/fmin/40000/gene/organism/7227 - Find all genes that are localized on the X chromosome in the range of 40,000 bp to the end of X on any strand.
//        http://flybase.org/gmodrest/v1/location/chromosome/X/fmax/40000/gene/organism/7227 - Find all genes that are localized on the X chromosome in the range of the start of the X up to and including 40,000 bp on any strand.
//        http://flybase.org/gmodrest/v1/location/chromosome/X/fmin/50000/fmax/140000/strand/1/gene/organism/7227 - Find all genes that are localized on the plus strand of the X chromosome between and including 50,000 bp and 140,000 bp. */
//
//
//        logger.error("location called with '"+contigName+"'");
//
//        GeneLocationQuery q = (GeneLocationQuery) queryFactory.retrieveQuery("geneLocation", NumericQueryVisibility.PUBLIC);
//        q.setTopLevelFeatureName(contigName);
//        q.setMax(Integer.MAX_VALUE);
//        q.setMin(1);
//
//        List<String> results = null;
//        try {
//            results = q.getResults();
//        }
//        catch (QueryException exp) {
//            exp.printStackTrace();
//        }
//
//        OrthologuesResultSet ors = new OrthologuesResultSet();
//        for (String result : results) {
//            Gene gene = new Gene();
//            gene.accession = result;
//            //gene.data_provider = "Sanger Institute";
//            gene.db = "GeneDB";
//            gene.url = "http://www.genedb.org/gene/"+result;
//            ors.getGenes().add(gene);
//        }
//
//        modelMap.addAttribute("resultset", ors);
//        return DEFAULT_VIEW;
////    <?xml version="1.0" encoding="UTF-8"?>
////    <resultset>
////       <api_version>1</api_version>
////       <data_provider>FlyBase</data_provider>
////       <data_version>FB2008_10</data_version>
////       <query_time>2009-01-15 09:03:00</query_time>
////       <query_url>http://flybase.org/gmodrest/v1/location/chromosome/2L/fmin/12587000/fmax/12629000/gene/organism/7227</query_url>
////       <result>
////          after dbxref
////          <date_created>2003-03-08 00:00:00</date_created>
////          <last_modified>2005-01-15 09:03:00</last_modified>
////    </resultset>
//    }
//
//    //---------------------------------------------------------------
//
//    @RequestMapping(method=RequestMethod.GET, value="/ontology/gene/{ontologyId}")
//    public String ontologySearch(ModelMap modelMap,
//            @PathVariable("ontologyId") String ontologyId) {
//        boolean not = ontologyId.startsWith("!");
//        logger.error("ontology called with '"+ontologyId+"'");
//
//        // .../Query/controlledCuration?
//        // cvTermName=%27de+novo%27+pyrimidine+base+biosynthetic+process
//        // &taxons=Root&cv=biological_process
//
//        logger.error("curation called with '"+ontologyId+"'");
//
//        GenesByDbQuery q = (GenesByDbQuery) queryFactory.retrieveQuery("geneByDb", NumericQueryVisibility.PRIVATE);
//        q.setAccession(ontologyId);
//
//        List<String> results = null;
//        try {
//            results = q.getResults();
//        }
//        catch (QueryException exp) {
//            exp.printStackTrace();
//        }
//
//        OrthologuesResultSet ors = new OrthologuesResultSet();
//        for (String result : results) {
//            Gene gene = new Gene();
//            gene.accession = result;
//            //gene.data_provider = "Sanger Institute";
//            gene.db = "GeneDB";
//            gene.url = "http://www.genedb.org/gene/"+result;
//            ors.getGenes().add(gene);
//        }
//
//        modelMap.addAttribute("resultset", ors);
//        return DEFAULT_VIEW;
////    <?xml version="1.0" encoding="UTF-8"?>
////    <resultset>
////       <api_version>1</api_version>
////       <data_provider>FlyBase</data_provider>
////       <data_version>FB2008_10</data_version>
////       <query_time>2009-01-15 09:03:00</query_time>
////       <query_url>http://flybase.org/gmodrest/v1/ontology/gene/GO:00012345</query_url>
////       <result>
////          <dbxref>
////             <db>FlyBase</db>
////             <accession>FBgn0085432</accession>
////             <url>http://flybase.org/gmodrest/v1/fetch/FBgn0085432</url>
////          </dbxref>
////          <date_created>2003-03-08 00:00:00</date_created>
////          <last_modified>2005-01-15 09:03:00</last_modified>
////       </result>
////       <result>
////          <dbxref>
////             <db>FlyBase</db>
////             <accession>FBgn0004364</accession>
////             <url>http://flybase.org/gmodrest/v1/fetch/FBgn0004364</url>
////          </dbxref>
////          <date_created>2005-01-08 00:00:00</date_created>
////          <last_modified>2009-01-01 00:00:00</last_modified>
////       </result>
////    </resultset>
//    }
//
//    //---------------------------------------------------------------
//    // Not implemented
//    @RequestMapping(method=RequestMethod.GET, value="/orthologs/gene/{geneIdentifier}")
//    public String orthologSearch(ModelMap modelMap,
//            @PathVariable("geneIdentifier") String geneIdentifier) {
//
//        TranscriptDTO dto = fetchDTO(geneIdentifier);
//
//        List<String> orthologueNames = dto.getOrthologueNames();
//        OrthologuesResultSet ors = new OrthologuesResultSet();
//        for (String orthologueName : orthologueNames) {
//            Gene gene = new Gene();
//            gene.accession = orthologueName;
//            gene.data_provider = "Sanger Institute";
//            gene.db = "GeneDB";
//            gene.url = "http://www.genedb.org/gene/"+orthologueName;
//            ors.getGenes().add(gene);
//        }
//
//        modelMap.addAttribute("resultset", ors);
//        return DEFAULT_VIEW;
////    <?xml version="1.0" encoding="UTF-8"?>
////    <resultset>
////       <api_version>1</api_version>
////       <data_provider>FlyBase</data_provider>
////       <data_version>FB2008_10</data_version>
////       <query_time>2009-01-15 09:03:00</query_time>
////       <query_url>http://flybase.org/gmodrest/v1/orthologs/gene/FBgn0000490</query_url>
////       <result>
////          <dbxref>
////             <db>FlyBase</db>
////             <accession>FBgn0097591</accession>
////             <data_provider>FlyBase</data_provider>
////             <url>http://flybase.org/gmodrest/v1/fetch/FBgn0097591</url>
////          </dbxref>
////       </result>
////       <result>
////          <dbxref>
////              <db>ENSEMBL</db>
////              <accession>ENSBTAP00000004992</accession>
////              <data_provider>InParanoid</data_provider>
////          </dbxref>
////       </result>
////    </resultset>
//    }
//
//    //---------------------------------------------------------------
//    // Not implemented - not to be?
//    @RequestMapping(method=RequestMethod.GET, value="/orthologs/organism/{organismFrom}/to/{organismList}")
//    public String organismOrthologSearch(ModelMap modelMap,
//            @PathVariable("organismFrom") String organismFrom,
//            @PathVariable("organismList") String organismList) {
//        logger.error("organism orthology called for '"+organismFrom+"' and '"+organismList+"'");
//        notImplemented(modelMap, " by orthologs");
//        return DEFAULT_VIEW;
////    <?xml version="1.0" encoding="UTF-8"?>
////    <resultset>
////       <api_version>1</api_version>
////       <data_provider>FlyBase</data_provider>
////       <data_version>FB2008_10</data_version>
////       <query_time>2009-01-15 09:03:00</query_time>
////       <query_url>http://flybase.org/gmodrest/v1/orthologs/organism/7227/to/7240,6239</query_url>
////       <result>
////          <dbxref>
////             <db>FlyBase</db>
////             <accession>FBgn0000490</accession>
////             <taxonomy_id>7227</taxonomy_id>
////             <data_provider>FlyBase</data_provider>
////             <url>http://flybase.org/gmodrest/v1/fetch/FBgn0000490</url>
////          </dbxref>
////          <dbxref>
////             <db>FlyBase</db>
////             <accession>FBgn0015673</accession>
////             <taxonomy_id>7240</taxonomy_id>
////             <data_provider>FlyBase</data_provider>
////             <url>http://flybase.org/gmodrest/v1/fetch/FBgn0015673</url>
////          </dbxref>
////          <dbxref>
////             <db>WormBase</db>
////             <accession>WBGene00006570</accession>
////             <taxonomy_id>6239</taxonomy_id>
////             <data_provider>InParanoid</data_provider>
////             <url>http://wormbase.org/gmodrest/v1/fetch/WBGene00006570</url>
////          </dbxref>
////       </result>
////    ...
////    </resultset>
//    }
//
//    //---------------------------------------------------------------
//    // Not implemented - view not implemented
//    @RequestMapping(method=RequestMethod.GET, value="/fetch/{geneIdentifier}")
//    public String fetchGene(ModelMap modelMap,
//            @PathVariable("geneIdentifier") String id) {
//        logger.error("genefetch called with '"+id+"'");
//        TranscriptDTO dto = fetchDTO(id);
//        modelMap.addAttribute("resultset", dto);
//        return "json:";
//    }
//
//    //---------------------------------------------------------------
//
//    private void notImplemented(ModelMap map, String searchType) {
//        ErrorMsg msg = new ErrorMsg();
//        msg.type = "not implemented";
//        msg.message = "Not implemented, we don't support searches " + searchType;
//        map.addAttribute("resultset", msg);
//        return;
//    }
//
//    //---------------------------------------------------------------
//
//    public void setTaxonNodeManager(TaxonNodeManager tnm) {
//        this.tnm = tnm;
//    }
//
//    private TranscriptDTO fetchDTO(String geneIdentifier) {
//        Feature feature = sequenceDao.getFeatureByUniqueName(geneIdentifier, Feature.class);
//        if (feature == null) {
//            logger.warn(String.format("Failed to find feature '%s'", geneIdentifier));
//            return null; // FIXME
//        }
//
//        //Transcript transcript = modelBuilder.findTranscriptForFeature(feature);
//        TranscriptDTO dto = bmf.getDtoMap().get(feature.getFeatureId());
//        return dto;
//    }
//
//    public void setSequenceDao(SequenceDao sequenceDao) {
//        this.sequenceDao = sequenceDao;
//    }
//
//    public void setBmf(BerkeleyMapFactory bmf) {
//        this.bmf = bmf;
//    }
//
//    public void setQueryFactory(QueryFactory<NumericQueryVisibility> queryFactory) {
//        this.queryFactory = queryFactory;
//    }
//
//}
//
//
//
//@XStreamAlias("error")
//class ErrorMsg {
//    String type;
//    String message;
//}
//
//@XStreamAlias("organism")
//class Organism {
//    String genus;
//    String organism;
//    String taxonId;
//
//    public Organism(String genus, String organism, String taxonId) {
//        this.genus = genus;
//        this.organism = organism;
//        this.taxonId = taxonId;
//    }
//}
//
//@XStreamAlias("results")
//class OrganismResultSet extends BaseResultSet {
//
//    @XStreamImplicit(itemFieldName="organism")
//    private List<Organism> organisms = Lists.newArrayList();
//
//    public List<Organism> getOrganisms() {
//        return organisms;
//    }
//}
//
//@XStreamAlias("dbxref")
//class Gene {
//    String db;
//    String accession;
//    String taxonomy_id;
//    String data_provider;
//    String url;
//}
//
//@XStreamAlias("results")
//class OrthologuesResultSet extends BaseResultSet {
//
//    @XStreamImplicit(itemFieldName="dbxrefs")
//    private List<Gene> genes = Lists.newArrayList();
//
//    public List<Gene> getGenes() {
//        return genes;
//    }
//}
//
//@XStreamAlias("results")
//class BaseResultSet {
//    public String api_version = "1";
//}

