package org.genedb.web.mvc.controller;

import org.genedb.web.mvc.model.BerkeleyMapFactory;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;


@Controller
@RequestMapping("/Image")
public class ImageController {

    private static final Logger logger = Logger.getLogger(ImageController.class);

    private BerkeleyMapFactory bmf;

    @RequestMapping(method = RequestMethod.GET , value="/{key}")
    public void sendBackImage(
            @PathVariable(value="key") String key,
            ServletResponse response) {


        byte[] data = bmf.getImageMap().get(key);
        if (data == null) {
            logger.error(String.format("Can't find image data for '%s'", key));
            return;
        }
        String suffix = StringUtils.getFilenameExtension(key);

        ServletOutputStream out;
        try {
            response.setContentType("image/"+suffix);
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
