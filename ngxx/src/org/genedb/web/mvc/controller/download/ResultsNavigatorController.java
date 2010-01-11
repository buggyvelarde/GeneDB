package org.genedb.web.mvc.controller.download;

import org.apache.log4j.Logger;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.web.mvc.controller.WebConstants;
import org.genedb.web.mvc.model.ResultsCacheFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/ResultsNavigator")
public class ResultsNavigatorController {
    private static final Logger logger = Logger.getLogger(ResultsNavigatorController.class);

    //@Autowired
    private ResultsCacheFactory resultsCacheFactory;

    public void setResultsCacheFactory(ResultsCacheFactory resultsCacheFactory) {
        this.resultsCacheFactory = resultsCacheFactory;
    }

    @RequestMapping(method = RequestMethod.GET, value="/{key}" )
    public ModelAndView navigate(
            HttpSession session,
            @RequestParam(value="index") int index,
            @PathVariable(value="key") String key//,
            //@RequestParam(value="taxonNodeName", required=false) String taxonNodeName
            ) {

        if (!key.startsWith(session.getId())){
            logger.warn("The key doesn't match the session - redirecting to error.");
            session.setAttribute(WebConstants.FLASH_MSG, "Your session has timed out - Please repeat your search.");
            return new ModelAndView("redirect:/QueryList");
        }

        ModelAndView ret = new ModelAndView();

        List<GeneSummary> results = resultsCacheFactory.getResultsCacheMap().get(key).results;
        if (results == null && results.size() == 0) {
            return new ModelAndView("/Homepage");
        }

        if (index == -256) {
            ret.setViewName("redirect:/Results/"+key);
            return ret;
        }

        int checkedIndex = index - 1;

        if (index < 0) {
            checkedIndex = 0;
        }

        if (index >= results.size()) {
            checkedIndex = results.size() - 1;
        }

        ret.addObject("index", checkedIndex+1);
        ret.addObject("key", key);
        ret.addObject("resultsLength", results.size());
//        if (taxonNodeName != null) {
//            ret.addObject("taxonNodeName", taxonNodeName);
//        }
        String uniqueName = results.get(checkedIndex).getSystematicId();
        ret.setViewName("redirect:/gene/"+uniqueName);
        return ret;
    }

}
