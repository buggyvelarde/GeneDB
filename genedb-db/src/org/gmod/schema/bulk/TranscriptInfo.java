package org.gmod.schema.bulk;

import java.util.Collection;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

public class TranscriptInfo {

    public int transcriptFeatureId;
    public String transcriptUniqueName;
    public String transcriptName;
    public String transcriptType;
    public Collection<Synonym> transcriptSynonyms;

    public int geneFeatureId;
    public String geneUniqueName;
    public String geneName;
    public String geneType;
    public Collection<Synonym> geneSynonyms;

    public int fmin;
    public int fmax;
    public int strand;

    public int srcFeatureId;
    public String srcFeatureUniqueName;
    public int srcFeatureSeqLen;

    public SortedSet<Exon> exons = new TreeSet<Exon>();
    public Polypeptide polypeptide;

    @Override
    public String toString() {
        return String.format("'%s' (ID=%d)", transcriptUniqueName, transcriptFeatureId);
    }

    /**
     * Dump all the details in human-readable form.
     *
     * @return a string containing all the details
     */
    public String dump() {
        StringBuilder s = new StringBuilder();

        s.append(String.format("transcript: ID=%d, uniquename='%s', name='%s', type='%s'\n",
            transcriptFeatureId, transcriptUniqueName, transcriptName, transcriptType));

        if (transcriptSynonyms != null && !transcriptSynonyms.isEmpty()) {
            String transcriptSynonymsString = commasep(transcriptSynonyms);
            if (transcriptSynonymsString.length() > 0) {
                s.append(String.format("\t- synonyms: %s\n", transcriptSynonymsString));
            }
        }

        s.append(String.format("gene: ID=%d, uniquename='%s', name='%s', type='%s'\n",
            geneFeatureId, geneUniqueName, geneName, geneType));

        if (geneSynonyms != null && !geneSynonyms.isEmpty()) {
            String geneSynonymsString = commasep(geneSynonyms);
            if (geneSynonymsString.length() > 0) {
                s.append(String.format("\t- synonyms: %s\n", geneSynonymsString));
            }
        }

        s.append("exons: ");
        for (Exon exon: exons) {
            s.append("{" + exon + "}");
        }
        s.append('\n');

        s.append(String.format("location: fmin=%d, fmax=%d, strand=%d\n",
            fmin, fmax, strand));
        s.append(String.format("source: uniquename='%s', ID=%d, length=%d",
            srcFeatureUniqueName, srcFeatureId, srcFeatureSeqLen));

        s.append("polypeptide: " + polypeptide.dump());

        return s.toString();
    }

    private static String commasep(Iterable<?> os) {
        StringBuilder s = new StringBuilder();
        for(Object o: os) {
            if (s.length() > 0) {
                s.append(", ");
            }
            s.append(o);
        }
        return s.toString();
    }

    public static class Synonym {
        public String name;
        public String type;

        @Override
        public String toString() {
            return String.format("name='%s', type='%s'", name, type);
        }
    }

    public static class Exon implements Comparable<Exon> {
        public int featureId;
        public String uniqueName;
        public String type;

        public int fmin;
        public int fmax;

        @Override
        public int compareTo(Exon other) {
            return this.fmin - other.fmin;
        }

        @Override
        public String toString() {
            return String.format("%s(%d..%d)", type, fmin+1, fmax);
        }
    }

    public static class Polypeptide {
        public int featureId;
        public String uniqueName;
        public int seqLen;
        public Collection<Prop> props = new HashSet<Prop>();
        public Collection<Term> terms = new HashSet<Term>();

        public String dump() {
            StringBuilder s = new StringBuilder();

            s.append(String.format("uniquename='%s', ID=%d, seqlen=%d\n",
                uniqueName, featureId, seqLen));

            if (props != null && !props.isEmpty()) {
                s.append("\tprops: " + commasep(props) + '\n');
            }

            if (terms != null && !terms.isEmpty()) {
                s.append("\tterms: " + commasep(terms) + '\n');
            }

            return s.toString();
        }
    }

    public static class Prop {
        public String typeCV;
        public String type;
        public String value;

        @Override
        public String toString() {
            return String.format("%s:%s=%s", typeCV, type, value);
        }
    }

    public static class Term {
        public String cv;
        public String term;
        public Collection<Prop> props;

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();

            s.append(String.format("%s:%s", cv, term));
            if (props != null && !props.isEmpty()) {
                s.append( "[" + commasep(props) + "]" );
            }

            return s.toString();
        }
    }
}
