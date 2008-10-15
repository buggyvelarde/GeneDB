package org.genedb.db.loading;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FastaFileTest {

    @Test
    public void test1_size() throws IOException {
        FastaFile test1 = new FastaFile(new File("test/data/test1.fasta"));
        int count = 0;
        for (FastaRecord record: test1) {
            assertNotNull(record);
            count++;
        }
        assertEquals(4, count);
    }

    @Test
    public void test1_ids() throws IOException {
        List<FastaRecord> records = new ArrayList<FastaRecord>();
        for(FastaRecord record: new FastaFile(new File("test/data/test1.fasta"))) {
            records.add(record);
        }

        assertEquals("contig1", records.get(0).getId());
        assertEquals("contig2", records.get(1).getId());
        assertEquals("contig3", records.get(2).getId());
        assertEquals("contig4", records.get(3).getId());
    }

    @Test
    public void test1_seqs() throws IOException {
        List<FastaRecord> records = new ArrayList<FastaRecord>();
        for(FastaRecord record: new FastaFile(new File("test/data/test1.fasta"))) {
            records.add(record);
        }

        assertEquals("gattaca", records.get(0).getSequence());
        assertEquals("gaaacatgtaatttaactgatggtattggagtagcaccgcccaattgtagctgctacact" +
                     "gttgcacttcttatatgacccagtatagctgtgatattcactcctataggttccatcata" +
                     "attgaccagagcca", records.get(1).getSequence());
        assertEquals("", records.get(2).getSequence());
        assertEquals("gattaca", records.get(3).getSequence());
    }

    @Test
    public void iterateTwice() throws IOException {
        FastaFile test1 = new FastaFile(new File("test/data/test1.fasta"));

        List<FastaRecord> records = new ArrayList<FastaRecord>();
        for(FastaRecord record: test1) {
            records.add(record);
        }

        int i=0;
        for(FastaRecord record: test1) {
            assertEquals(records.get(i++), record);
        }

    }
}
