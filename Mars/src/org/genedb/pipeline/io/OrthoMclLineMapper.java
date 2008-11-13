package org.genedb.pipeline.io;

import org.genedb.pipeline.domain.OrthoMCL;

import org.springframework.batch.item.file.mapping.LineMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//ORTHOMCL0(2 genes,1 taxa):     PB20.0(P.berghei.tmp) PB24.0(P.berghei.tmp)
public class OrthoMclLineMapper implements LineMapper<OrthoMCL> {

    private Pattern pattern = Pattern.compile("(\\S+)\\((\\d+) genes,\\d+ taxa\\):([\\s\\S]+)");
    private Pattern bracketPair = Pattern.compile("\\(\\S+\\)");

    @Override
    public OrthoMCL mapLine(String line, int lineNumber) throws Exception {

        Matcher matcher = pattern.matcher(line);

        if (!matcher.lookingAt()) {
            throw new IllegalArgumentException(String.format("The line ('%d') isn't in the expected OrthoMCL format: '%s'", lineNumber, line));
        }

        String clusterId = matcher.group(1);
        int numberOfMembers = Integer.parseInt(matcher.group(2));

        // content is everything after the colon
        String content = matcher.group(3);
        Matcher matchBrackets = bracketPair.matcher(content);
        // Now remove the bracketed filenames
        String ids = matchBrackets.replaceAll("").trim();

        String[] orthologueIds = ids.split("\\s+");

        if (numberOfMembers != orthologueIds.length) {
            throw new RuntimeException(String.format("Didn't get expected number '%d' of ids (got '%d') at line '%d' in '%s'",
                    numberOfMembers, orthologueIds.length, lineNumber, ids));
        }

        OrthoMCL ret = new OrthoMCL(clusterId, orthologueIds);
        return ret;
    }

}
