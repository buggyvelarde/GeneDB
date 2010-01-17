package org.genedb.db.loading;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FastaFileTest {

    private File testFile = new File("test/data/test1.fasta");
    private FastaFile test1;

    @Before
    public void setup() throws IOException {
        test1 = new FastaFile(new FileReader(testFile));
    }

    @Test
    public void test1_size() {
        int count = 0;
        for (FastaRecord record: test1) {
            assertNotNull(record);
            count++;
        }
        assertEquals(4, count);
    }

    @Test
    public void test1_ids() {
        List<FastaRecord> records = new ArrayList<FastaRecord>();
        for(FastaRecord record: test1) {
            records.add(record);
        }

        assertEquals("contig1", records.get(0).getId());
        assertEquals("contig2", records.get(1).getId());
        assertEquals("contig3", records.get(2).getId());
        assertEquals("contig4", records.get(3).getId());
    }

    @Test
    public void test1_lineNumbers() {
        List<FastaRecord> records = new ArrayList<FastaRecord>();
        for(FastaRecord record: test1) {
            records.add(record);
        }

        assertEquals(1, records.get(0).getLineNumber());
        assertEquals(3, records.get(1).getLineNumber());
        assertEquals(7, records.get(2).getLineNumber());
        assertEquals(8, records.get(3).getLineNumber());
    }

    @Test
    public void test1_seqs() {
        List<FastaRecord> records = new ArrayList<FastaRecord>();
        for(FastaRecord record: test1) {
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
    public void iterateTwice() {
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
