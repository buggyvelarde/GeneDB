package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.QueryException;
import org.genedb.querying.tmpquery.GeneSummary;
import org.genedb.web.mvc.model.ResultsCacheFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


@Controller
@RequestMapping("/ResultsNavigator")
public class ResultsNavigatorController {

    @Autowired
    private ResultsCacheFactory resultsCacheFactory;

    @RequestMapping(method = RequestMethod.GET )
    public ModelAndView navigate(
            @RequestParam(value="index") int index,
            @RequestParam(value="key") String key) {

        ModelAndView ret = new ModelAndView();

        List<GeneSummary> results = resultsCacheFactory.getResultsCacheMap().get(key);
        if (results == null && results.size() == 0) {
            return new ModelAndView("/Homepage");
        }

        if (index == -256) {
            ret.addObject("key", key);
            ret.setViewName("/Results");
            return ret;
        }

        int checkedIndex = index;

        if (index < 0) {
            checkedIndex = 0;
        }

        if (index >= results.size()) {
            checkedIndex = results.size() - 1;
        }

        ret.addObject("index", checkedIndex);
        ret.addObject("key", key);
        ret.addObject("resultsLength", results.size());
        String uniqueName = results.get(index).getSystematicId();
        ret.setViewName("redirect:/NamedFeature?name="+uniqueName);
        return ret;
    }

}
