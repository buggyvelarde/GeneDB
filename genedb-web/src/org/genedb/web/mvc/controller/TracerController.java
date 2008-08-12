package org.genedb.web.mvc.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/Tracer")
public class TracerController {

    public String trace(@RequestParam("type") String type, 
            @RequestParam("var1") String var1, HttpSession session) {
        
        // Decode and update history
        
        // Return an image
        return "redirect:/includes/images/onepixel.gif";
    }
    
}
