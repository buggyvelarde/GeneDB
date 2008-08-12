package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class HtmlUtils {

    public static void generateUnorderedList(Writer out, List<String> list) {
        writeLine(out, "<ul>");
        for (String item : list) {
            writeLine(out, "<li>"+item+"</li>");
        }
        writeLine(out, "</ul>");
    }

    private static void writeLine(Writer out, String line) {
        try {
            out.write(line);
            out.write('\n');
        } catch (IOException e) {
            // FIXME Log that there was a problem
            // e.printStackTrace();
        }
    }

}
