package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.genedb.db.dao.OrganismDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/JBrowse")
public class JBrowseController {
	
	@Autowired
    @Qualifier("organismDao")
    OrganismDao organismDao;
	
	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView test(HttpServletRequest request, HttpServletResponse response)
	{		
		List<String> commonNames = new ArrayList<String>();
		Map <String, String> fullNames = new HashMap<String, String>();
		for (org.gmod.schema.mapped.Organism organism : organismDao.getOrganisms())
		{
			if (! organism.isPopulated())
				continue;
			String commonName = organism.getCommonName();
			commonNames.add(commonName);
			fullNames.put(commonName, organism.getGenus() + " " + organism.getSpecies());
		}
		
		Collections.sort(commonNames);
		commonNames.add(0, "Select organism");
		fullNames.put("Select organism", "Select organism");
		
		ModelAndView mav = new ModelAndView("jbrowse/listOrganisms");
		mav.addObject("commonNames", commonNames);
		mav.addObject("fullNames", fullNames);
		return mav;
	}
}
