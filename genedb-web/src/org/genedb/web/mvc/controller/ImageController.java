package org.genedb.web.mvc.controller;

import org.genedb.web.mvc.model.BerkeleyMapFactory;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;


@Controller
@RequestMapping("/Image")
public class ImageController {

    private static final Logger logger = Logger.getLogger(ImageController.class);

    private BerkeleyMapFactory bmf;

    @RequestMapping(method = RequestMethod.GET , params= "key")
    public void sendBackImage(
            @RequestParam(value="key") String key,
            ServletResponse response) {


        byte[] data = bmf.getImageMap().get(key);
        if (data == null) {
            logger.error(String.format("Can't find image data for '%s'", key));
            return;
        }

        ServletOutputStream out;
        try {
            out = response.getOutputStream();
            out.write(data);
        } catch (IOException exp) {
            logger.error(String.format("Problem returning image data for '%s'", key), exp);
            return;
        }

    }

    public void setBerkeleyMapFactory(BerkeleyMapFactory bmf) {
        this.bmf = bmf;
    }

}
