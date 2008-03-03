package org.genedb.web.mvc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class JograLaunchController extends AbstractController {

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String cmd = ServletRequestUtils.getStringParameter(request, "cmd");
		String gene = ServletRequestUtils.getStringParameter(request, "gene");
		
		List<String> args = new ArrayList<String>();
		args.add(gene);
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("command", cmd);
		model.put("args", args);
		return new ModelAndView("jogra", model);
	}

}
