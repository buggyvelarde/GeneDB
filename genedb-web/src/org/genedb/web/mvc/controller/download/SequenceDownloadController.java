package org.genedb.web.mvc.controller.download;

import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/SequenceDownload")
public class SequenceDownloadController {

    @Autowired
    private SequenceDao sequenceDao;

    @RequestMapping(method = RequestMethod.GET)
    public String setUpForm() {
        return "err/forminput";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(@RequestParam("topLevelFeature") String tlf,
            @RequestParam("downloadType") String downloadType,
            @RequestParam("featureType") String featureType,
            NativeWebRequest nwr, Writer writer) {

        PrintWriter out;
        if (writer instanceof PrintWriter) {
            out = (PrintWriter) writer;
        } else {
            out = new PrintWriter(writer);
        }

        HttpServletResponse hsr = (HttpServletResponse) nwr.getNativeResponse();
        hsr.setContentType("text/plain");

        Feature chromosome = sequenceDao.getFeatureByUniqueName(tlf, featureType);
        if (chromosome == null) {
            out.println("Unable to retrieve feature for '" + tlf + "'");
            return null;
        }
        for (FeatureLoc fl : chromosome.getFeatureLocsForSrcFeatureId()) {
            Object ob = fl.getFeature();
            if (ob instanceof Feature) {
                Feature f = (Feature) ob;
                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("systematic_id", f.getUniqueName());
                boolean strand = true;
                if (fl.getStrand() < 0) {
                    strand = false;
                }
                writeEmblEntry(out, f.getType().getName(), strand, fl.getFmin(), fl.getFmax(),
                    attributes);
            }
        }

        return null;
    }

    private static int FEATURE_PREFIX_WIDTH = 22;
    private static int MAX_FEATURE_WIDTH = 18;
    private static final String FEATURE_TABLE_PREFIX = String.format("%-" + FEATURE_PREFIX_WIDTH
            + "s", "FT");

    private void writeEmblEntry(PrintWriter out, String featureType, boolean forwardStrand,
            int min, int max, Map<String, String> qualifiers) {

        if (featureType.length() > MAX_FEATURE_WIDTH) {
            featureType = featureType.substring(0, MAX_FEATURE_WIDTH);
        }

        out.format("FT %-" + (FEATURE_PREFIX_WIDTH - 3) + "s", featureType);
        if (!forwardStrand) {
            out.print("complement(");
        }

        out.print(min + ".." + max);

        if (!forwardStrand) {
            out.print(")");
        }
        out.println();

        for (Map.Entry<String, String> qualifier : qualifiers.entrySet()) {
            out.println(FEATURE_TABLE_PREFIX + "/" + qualifier.getKey() + "=\""
                    + qualifier.getValue() + "\"");
        }

    }

}
