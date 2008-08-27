package org.genedb.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.seq.io.CrossProductTokenization;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolListViews;
import org.biojava.bio.symbol.TranslationTable;

/**
 * For translating DNA sequences into proteins. Each instance corresponds
 * to a particular genetic code.
 *
 * @author rh11
 *
 */
class Translator {

    private static final TranslationTable transcriptionTable = RNATools.transcriptionTable();
    private static final SymbolTokenization dnaTokenization;
    static {
        try {
            dnaTokenization = DNATools.getDNA().getTokenization("token");
        } catch (BioException e) {
            throw new IllegalStateException("BioJava appears to be broken", e);
        }
    }

    private TranslationTable translationTable;
    private StartCodonTable startCodonTable;

    /**
     * Get the Translator corresponding to the specified genetic code,
     * as defined by NCBI.
     *
     * @see http://www.ncbi.nlm.nih.gov/Taxonomy/Utils/wprintgc.cgi
     * @param geneticCodeId the ID number of the genetic code
     * @return
     */
    public static Translator getTranslator(int geneticCodeId) {
        return new Translator(geneticCodeId);
    }

    private Translator(int translationTableId) {
        this.translationTable = RNATools.getGeneticCode(translationTableId);
        this.startCodonTable = StartCodonTable.getTable(translationTableId);
    }

    public String translate(String dnaSequence, int phase) throws TranslationException {
        return translate(dnaSequence, phase, false);
    }

    public String translate(String dnaSequence, int phase, boolean stopCodonTranslatedAsSelenocysteine)
        throws TranslationException {

        try {
            SymbolList dna = new SimpleSymbolList(dnaTokenization, dnaSequence);
            SymbolList rna = SymbolListViews.translate(dna, transcriptionTable);
            rna = rna.subList(phase + 1, phase + 3 * ((rna.length() - phase) / 3));

            SymbolList rnaWindowed = SymbolListViews.windowedSymbolList(rna, 3);
            SymbolList protein = SymbolListViews.translate(rnaWindowed, translationTable);
            String naiveTranslation = protein.seqString();

            if (startCodonTable.contains(rnaWindowed.symbolAt(1))) {
                naiveTranslation = "M" + naiveTranslation.substring(1);
            }

            if (stopCodonTranslatedAsSelenocysteine) {
                if (naiveTranslation.endsWith("*")) {
                    return naiveTranslation.substring(0, naiveTranslation.length() - 1).replaceAll("\\*", "U") + '*';
                }
                else {
                    return naiveTranslation.replaceAll("\\*", "U");
                }
            }

            return naiveTranslation;
        }
        catch (BioException e) {
            throw new TranslationException ("Failed to translate cds", e);
        }
    }
}

/**
 * Represents the start codons used by a particular genetic code,
 * as a set of BioJava Symbols in the alphabet <code>RNA x RNA x RNA</code>.
 *
 * @author rh11
 */
class StartCodonTable {
    public static StartCodonTable getTable(int geneticCodeId) {
        if (geneticCodeId < 0 || geneticCodeId >= tables.length || tables[geneticCodeId] == null)
            throw new IllegalArgumentException(String.format("No such genetic code (%d)", geneticCodeId));

        return tables[geneticCodeId];
    }

    private static final char[] bases = new char[] {'U', 'C', 'A', 'G'};
    private static final SymbolTokenization codonTokenization;
    static {
        final Alphabet rnaAlphabet = AlphabetManager.alphabetForName("RNA");
        final Alphabet codonAlphabet = AlphabetManager.alphabetForName("(RNA x RNA x RNA)");
        try {
            final List<SymbolTokenization> threeTokens = Collections.nCopies(3, rnaAlphabet.getTokenization("token"));
            codonTokenization = new CrossProductTokenization(codonAlphabet, threeTokens);
        }
        catch (BioException e) {
            throw new RuntimeException("BioJava appears to be broken", e);
        }
    }

    private Set<Symbol> symbols = new HashSet<Symbol> ();

    private static final StartCodonTable[] tables = new StartCodonTable[] {
        /*  0 */ null,
        /*  1 */ new StartCodonTable("---M---------------M---------------M----------------------------"),
        /*  2 */ new StartCodonTable("--------------------------------MMMM---------------M------------"),
        /*  3 */ new StartCodonTable("----------------------------------MM----------------------------"),
        /*  4 */ new StartCodonTable("--MM---------------M------------MMMM---------------M------------"),
        /*  5 */ new StartCodonTable("---M----------------------------MMMM---------------M------------"),
        /*  6 */ new StartCodonTable("-----------------------------------M----------------------------"),
        /*  7 */ null,
        /*  8 */ null,
        /*  9 */ new StartCodonTable("-----------------------------------M---------------M------------"),
        /* 10 */ new StartCodonTable("-----------------------------------M----------------------------"),
        /* 11 */ new StartCodonTable("---M---------------M------------MMMM---------------M------------"),
        /* 12 */ new StartCodonTable("-------------------M---------------M----------------------------"),
        /* 13 */ new StartCodonTable("---M------------------------------MM---------------M------------"),
        /* 14 */ new StartCodonTable("-----------------------------------M----------------------------"),
        /* 15 */ new StartCodonTable("-----------------------------------M----------------------------"),
        /* 16 */ new StartCodonTable("-----------------------------------M----------------------------"),
        /* 17 */ null,
        /* 18 */ null,
        /* 19 */ null,
        /* 20 */ null,
        /* 21 */ new StartCodonTable("-----------------------------------M---------------M------------"),
        /* 22 */ new StartCodonTable("-----------------------------------M----------------------------"),
        /* 23 */ new StartCodonTable("--------------------------------M--M---------------M------------"),
    };

    /**
     * Create a new StartCodonTableImpl from a string representing the 'starts'
     * line in the NCBI format.
     *
     * @param starts
     */
    private StartCodonTable(String starts) {
        char[] startsChars = starts.toCharArray();
        int i = 0;
        for (char base1 : bases) {
            for (char base2 : bases) {
                for (char base3 : bases) {
                    if (startsChars[i++] == 'M') {
                        final String codonString = String.format("(%c %c %c)", base1, base2, base3);
                        try {
                            final Symbol codonSymbol = codonTokenization.parseToken(codonString);
                            symbols.add(codonSymbol);
                        } catch (IllegalSymbolException exception) {
                            throw new RuntimeException(
                                    String.format(
                                        "BioJava failed to recognise codon '%s'. This should never happen.",
                                        codonString),
                                    exception);
                        }
                    }
                }
            }
        }
    }

    /**
     * Does this set contain the specified symbol?
     *
     * @param symbol
     * @return
     */
    public boolean contains(Symbol symbol) {
        return symbols.contains(symbol);
    }
}
