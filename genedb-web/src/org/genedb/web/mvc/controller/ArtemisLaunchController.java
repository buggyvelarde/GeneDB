package org.genedb.web.mvc.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

public class ArtemisLaunchController extends AbstractController{


	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String organism = ServletRequestUtils.getStringParameter(request, "organism");
		String chromosome = ServletRequestUtils.getStringParameter(request, "chromosome");
		String s = ServletRequestUtils.getStringParameter(request, "start");
		String e = ServletRequestUtils.getStringParameter(request, "end");
		
		int start = Integer.parseInt(s);
		int end = Integer.parseInt(e) + 1500;
		
		if (start > 1500) {
			start = start - 1500;
		} else {
			start = 0;
		}
		
		
		String argument = organism + ":" + chromosome + ":" + start + ".." + end;
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("argument", argument);
		model.put("offset", start);
		//FreeMarkerView fmv = new FreeMarkerView();
		//fmv.setContentType("application/x-java-jnlp-file");
		//fmv.setUrl("artemis");
		return new ModelAndView("artemis",model);
	}

}
