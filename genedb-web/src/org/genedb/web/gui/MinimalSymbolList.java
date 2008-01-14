package org.genedb.web.gui;

import org.biojava.bio.BioError;
import org.biojava.bio.symbol.AbstractSymbolList;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

import java.io.Serializable;
import java.util.Collections;

/**
 * Basic implementation of SymbolList.  This
 * is currently backed by a normal Java array.
 *  <p>
 * SimpleSymbolList is now editable. edit() has been implemented
 * in a way that edits are relatively inefficient, but symbolAt() is
 * very efficient.
 * </p>
 * <p>
 * A new constructor SimpleSymbolList(SymbolParser,String) has
 * been added so you can now simply turn a String into a SymbolList.
 * This is mostly to provide a simple way to create a SymbolList for
 * people just trying to get their feet wet. So here is an example.
 * </p>
 * <code>
 * String seqString = "gaattc";
 * FiniteAlphabet dna = (FiniteAlphabet) AlphabetManager.alphabetForName("DNA");
 * SymbolParser parser = dna.getParser("token");
 * SymbolList mySl = new SimpleSymbolList (parser,seqString);
 * System.out.println("Look at my sequence " + mySl.seqString());
 * </code>
 * <p>
 * with the right parser you should be able to make a protein sequence
 * from the String "AspAlaValIleAsp"
 * </p>
 * <p>
 * subList() is implemented such that subLists are views of the original until
 * such time as the underlying SymbolList is edited in a way that would modify
 * the subList, at which point the subList gets its own array of Symbols and
 * does not reflect the edit to the original. When subList() is called on another
 * subList (which is a veiw SimpleSymbolList) the new SimpleSymbolList is a view
 * of the original, not the subList.
 * </p>
 *
 * @author Thomas Down
 * @author David Waring
 */

public class MinimalSymbolList extends AbstractSymbolList implements ChangeListener, Serializable {

    private Alphabet alphabet;
    private int length;
    private boolean isView;  // this is for subList which returns a view onto a SimpleSymbolList until either is edited
    private SymbolList referenceSymbolList; // the original SymbolList subLists of views become sublists of original

    protected void finalize() throws Throwable {
        super.finalize();
        alphabet.removeChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
        if (isView){
            referenceSymbolList.removeChangeListener(this, ChangeType.UNKNOWN);
        }
    }
    

    /**
     * Construct a SymbolList from a string.
     *
     * @param parser A SymbolParser for whatever your string is -- e.g. alphabet.getParser("token").
     * @param seqString A String of characters.
     *
     * @throws IllegalSymbolException if a Symbol is not in the specified alphabet.
     */

    public MinimalSymbolList(Alphabet alphabet, int length)
        throws IllegalSymbolException
    {
        this.length = length;
        this.alphabet = alphabet;
        this.isView = false;
    }

    /**
     * Get the alphabet of this SymbolList.
     */

    public Alphabet getAlphabet() {
      return alphabet;
    }

    /**
     * Get the length of this SymbolList.
     */

    public int length() {
      return length;
    }

    /**
     * Find a symbol at a specified offset in the SymbolList.
     *
     * @param pos Position in biological coordinates (1..length)
     */

    public Symbol symbolAt(int pos) {
    		return alphabet.getGapSymbol();
    }


    // Dummy implementation
    public SymbolList subList(int start, int end){
    		return null;
    }


    public void edit(Edit edit)throws IndexOutOfBoundsException, IllegalAlphabetException,ChangeVetoException {
    	// Deliberately empty
    }

    /**
    *  On preChange() we convert the SymolList to a non-veiw version,
    *  giving it its own copy of symbols
    */

    public void preChange(ChangeEvent cev) {
    	// Deliberately empty
   }

//    // we don't do anything on the postChange we don't want to reflect the changes
   public void postChange(ChangeEvent cev){
        // Deliberately empty
    }



    /**
     * Add a new Symbol to the end of this list.
     *
     * @param sym Symbol to add
     * @throws IllegalSymbolException if the Symbol is not in this list's alphabet
     */

      public void addSymbol(Symbol sym)
          throws IllegalSymbolException, ChangeVetoException
      {
          try {
              SymbolList extraSymbol = new SimpleSymbolList(getAlphabet(), Collections.nCopies(1, sym));
              edit(new Edit(length() + 1, 0, extraSymbol));
          } catch (IllegalAlphabetException ex) {
              throw new IllegalSymbolException(ex, sym, "Couldn't add symbol");
          } catch (IndexOutOfBoundsException ex) {
              throw new BioError("Assertion failure: couldn't add symbol at end of list");
          }
      }

}
