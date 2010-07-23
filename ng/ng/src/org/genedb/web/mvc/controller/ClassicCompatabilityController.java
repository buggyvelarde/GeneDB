package org.genedb.web.mvc.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


@Controller
@RequestMapping("/genedb")
public class ClassicCompatabilityController {

    private static final String WWW = "redirect:http://www.genedb.org/";

    private static final String CLASSIC = "redirect:http://old.genedb.org/";

    private static final String CLASSIC_FULL = CLASSIC + "genedb/";

    private static Set<String> classicOnly = Sets.newHashSet();

    // The organismprop have proved too flakey so hardcode here for now
    private static Map<String, String> classicMap = Maps.newHashMap();

    static {
        classicOnly.add("annulata");
        classicOnly.add("asp");
        classicOnly.add("cdubliniensis");
        classicOnly.add("cerevisiae");
        classicOnly.add("cjejuni");
        classicOnly.add("dicty");
        classicOnly.add("ehistolytica");
        classicOnly.add("ehuxleyi");
        classicOnly.add("glossina");
        classicOnly.add("llongipalpis");
        classicOnly.add("pberghei");
        classicOnly.add("pert");
        classicOnly.add("pombe");

        classicMap.put("etenella", "Etenella");
        classicMap.put("ncaninum", "Ncaninum");
        classicMap.put("pchabaudi", "Pchabaudi");
        classicMap.put("malaria", "Pfalciparum");
        classicMap.put("pknowlesi", "Pknowlesi");
        classicMap.put("tbrucei427", "Tbruceibrucei427");
        classicMap.put("lbraziliensis", "Lbraziliensis");
        classicMap.put("linfantum", "Linfantum");
        classicMap.put("leish", "Lmajor");
        classicMap.put("tryp", "Tbruceibrucei927");
        classicMap.put("tcruzi", "Tcruzi");
        classicMap.put("tcongolense", "Tcongolense");
        classicMap.put("tgambiense", "Tbruceigambiense");
        classicMap.put("tvivax", "Tvivax");
        classicMap.put("smansoni", "Smansoni");
        classicMap.put("bronchi", "Bbronchiseptica");
        classicMap.put("bfragilis", "Bfragilis_NCTC9343");
        classicMap.put("parapert", "Bparapertussis");
        classicMap.put("bpseudomallei", "Bpseudomallei");
        classicMap.put("cabortus", "Cabortus");
        classicMap.put("diphtheria", "Cdiphtheriae");
        classicMap.put("ecarot", "Ecarotovora");
        classicMap.put("rleguminosarum", "Rleguminosarum");
        classicMap.put("saureusMRSA", "Saureus_MRSA252");
        classicMap.put("saureusMSSA", "Saureus_MSSA4776");
        classicMap.put("scoelicolor", "Scoelicolor");
        classicMap.put("spyogenes", "Spyogenes");
        classicMap.put("styphi", "Styphi");
    }

    private Logger logger = Logger.getLogger(ClassicCompatabilityController.class);

    // Homepage
    @RequestMapping(method=RequestMethod.GET, value={"/{nickname}", "/{nickname}/", "/{nickname}/index.jsp"})
    public String viewHomepage(@PathVariable("nickname") String nickname) {
        if (classicOnly.contains(nickname)) {
            return CLASSIC_FULL + nickname + "/";
        }
        return WWW + "Homepage/" + classicMap.get(nickname);
    }


    // Blast page
    @RequestMapping(method=RequestMethod.GET, value="/{nickname}/blast.jsp")
    public String viewBlastPage(@PathVariable("nickname") String nickname) {
        if (classicOnly.contains(nickname)) {
            return CLASSIC + "genedb/" + nickname + "/blast.jsp";
        }
        return WWW + "Homepage/" + classicMap.get(nickname);
    }


    // Canonical gene
    @RequestMapping(method=RequestMethod.GET, value="/Search")
    public String viewGenePage(
            @RequestParam("name") String name,
            @RequestParam("organism") String nickname) {

        if (classicOnly.contains(nickname)) {
            return CLASSIC_FULL + "Search?name=" + name + "&organism=" + nickname;
        }
        return WWW + "gene/" + name;
    }

    // Menu bar search
    @RequestMapping(method=RequestMethod.GET, value="/Dispatcher")
    public String decodeDispatcher(
            @RequestParam("name") String name,
            @RequestParam("organism") String nickname) {

        if (classicOnly.contains(nickname)) {
            return CLASSIC_FULL + "Search?name=" + name + "&organism=" + nickname;
        }
        return WWW + "gene/" + name;
    }

}
