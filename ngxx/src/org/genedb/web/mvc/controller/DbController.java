package org.genedb.web.mvc.controller;

import org.genedb.db.dao.GeneralDao;
import org.genedb.util.Pair;

import org.gmod.schema.mapped.Db;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;



@Controller
@RequestMapping("/db")
public class DbController {

    private static final String WWW = "redirect:http://www.genedb.org/";

    private Logger logger = Logger.getLogger(DbController.class);

    private GeneralDao generalDao;


    @RequestMapping(method=RequestMethod.GET, value={"", "/"})
    public String list(ModelMap map) {

        List<String> names = generalDao.getAllDbNames();
        logger.error(StringUtils.collectionToCommaDelimitedString(names));
        List<Pair<String, String>> results1 = Lists.transform(names, new Function<String, Pair<String, String>>() {
            @Override
            public Pair<String, String> apply(String in) {
                return new Pair<String, String>(in, "http://www.genedb.org/db/"+in);
            }
        });
        logger.error(StringUtils.collectionToCommaDelimitedString(results1));
        List<Pair<String,String>> results = new ArrayList<Pair<String, String>>(results1);
        map.put("resultset", results);
        return "json:";
    }


    @RequestMapping(method=RequestMethod.GET, value="/{dbname}/")
    public String viewDB(@PathVariable("dbname") String dbname,
            ModelMap map) {

        Db db = generalDao.getDbByName(dbname);
        map.put("resultset", new DbDTO(db));

        return "json:";
    }


    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }


}

@XStreamAlias("results")
class DbDTO {
    int dbId;
    String name;
    String url;
    String urlPrefix;


    public DbDTO(Db db) {
        this.dbId = db.getDbId();
        this.name = db.getName();
        this.urlPrefix = db.getUrlPrefix();
        this.url = db.getUrl();
    }


}