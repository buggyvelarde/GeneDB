package org.genedb.web.applications.motifsearch;

import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;

public class FastaLoader {

    BufferedReader in;


    public FastaLoader(BufferedReader in) {
        if (!in.markSupported()) {
            throw new RuntimeException("FastaLoader requires a BufferedReader which supports marks");
        }
        this.in = in;
    }


    public void skip(int numRecords) {
        // TODO Lines or records
    }


    FastaEntry readEntry() {
        String header;
        try {
            header = in.readLine();
            if (!header.startsWith(">")) {
                throw new RuntimeException(String.format("Expected a FASTA header but got '%s'", header));
            }
            FastaEntry entry = new FastaEntry(header);

            boolean inSequence = true;
            StringBuilder sequence = new StringBuilder();
            while (inSequence) {
                in.mark(200);
                String line = in.readLine();
                if (line.startsWith(">")) {
                    inSequence = false;
                    in.reset();
                } else {
                    line = StringUtils.deleteAny(header, " "); // TODO Check
                    sequence.append(line);
                }
            }
            entry.setSequence(sequence.toString());
            return entry;
        } catch (IOException exp) {
            throw new RuntimeException(exp);
        }
    }

}
