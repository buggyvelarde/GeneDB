package org.genedb.web.mvc.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.StringTokenizer;


@Controller
@RequestMapping("/DbLinkRedirect")
public class DbLinkRedirectController {

    private static final Logger logger = Logger.getLogger(DbLinkRedirectController.class);

    private static final String QMW_PREFIX = "http://www.chem.qmul.ac.uk/iubmb/enzyme/EC";

    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm() {
        logger.error("Required parameters missing");
        return "redirect:/Homepage";
    }

    @RequestMapping(method = RequestMethod.GET , value="/{db}/{acc}")
    public String processForm(
            @PathVariable(value="db") String dbName,
            @PathVariable(value="acc") String acc) {

        if (dbName.equalsIgnoreCase("EC")) {
            StringBuilder url = new StringBuilder();
            url.append("redirect:");
            url.append(QMW_PREFIX);

            // eg id=1.8.1.4, uri=EC1/8/1/4.html
            StringTokenizer tok = new StringTokenizer(acc, ".");
            boolean dash = false;
            int count = 0;
            while (tok.hasMoreTokens()) {
                String t = tok.nextToken();
                if ("-".equals(t)) {
                    dash = true;
                    url.append("/");
                } else {
                    url.append(t);
                    if (count < 3) {
                        url.append("/");
                    }
                }
                count++;
            }
            if (!dash) {
                url.append(".html");
            }
            return url.toString();
        }




        logger.error("No identifiable db");
        return "redirect:/Homepage";

    }

}
