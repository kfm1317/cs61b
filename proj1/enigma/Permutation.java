package enigma;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Karl Meissner and CS61B Staff
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        cycles = cycles.replaceAll("[\\s]+", " ");
        if (!cycles.equals("")) {
            if (cycles.contains(")(")) {
                cycles = cycles.replaceAll("[)][(]", ") (");
            }
            cycles = cleaner(cycles);
            Scanner s = new Scanner(cycles);
            while (s.hasNext()) {
                String next = s.next();
                boolean x = next.contains("()"), z = next.contains("(");
                boolean w = next.contains(")");
                if (x || z && next.indexOf('(') != 0) {
                    throw error("Parentheses Error");
                } else if (z && next.indexOf(')') != next.length() - 1) {
                    throw error("Parentheses Error");
                } else if (z && !w) {
                    throw error("Parentheses Error");
                }
                next = next.replaceAll("[()]", "");
                List<Character> L = new ArrayList<Character>(0);
                for (int i = 0; i < next.length(); ++i) {
                    L.add(next.charAt(i));
                }
                int szL = L.size() - 1;
                for (int j = 0; j < szL; ++j) {
                    perm.put(L.get(j), L.get(j + 1));
                    revPerm.put(L.get(szL - j), L.get(szL - j - 1));
                }
                perm.put(L.get(L.size() - 1), L.get(0));
                revPerm.put(L.get(0), L.get(L.size() - 1));
            }
        } else {
            perm.put("", "");
            revPerm.put("", "");
        }
        for (int k = 0; k < _alphabet.size(); ++k) {
            if (!perm.containsKey(_alphabet.toChar(k))) {
                perm.put(_alphabet.toChar(k), _alphabet.toChar(k));
                revPerm.put(_alphabet.toChar(k), _alphabet.toChar(k));
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char ch = (char) perm.get(_alphabet.toChar(wrap(p)));
        return _alphabet.toInt(ch);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char ch = (char) revPerm.get(_alphabet.toChar(wrap(c)));
        return _alphabet.toInt(ch);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return (char) perm.get(p);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return (char) revPerm.get(c);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < _alphabet.size(); ++i) {
            if (permute(_alphabet.toChar(i)) == _alphabet.toChar(i)) {
                return false;
            }
        }
        return true;
    }

    /** Checks and throws error for invalid whitespace, duplicate characters,
     * characters not in alphabet, or select parentheses errors
     * in cycles string CY. Otherwise, returns CY.
     */
    private String cleaner(String cy) {
        for (int n = 0; n < cy.length(); ++n) {
            char nChar = cy.charAt(n);
            boolean cSpace = cy.contains("\\s"), nOpen = nChar != '(';
            boolean nClosed = nChar != ')', nSpace = nChar != ' ';
            boolean cChar = _alphabet.contains(nChar);
            if (cSpace && !cy.contains("(") && !cy.contains(")")) {
                throw error("No Whitespace Allowed");
            } else if (nOpen && nClosed && nSpace && !cChar) {
                throw error("Cycle char not in Alphabet");
            }
            for (int m = n + 1; m < cy.length(); ++m) {
                char mChar = cy.charAt(m);
                if (mChar == nChar && nSpace) {
                    if (mChar == '(' || mChar == ')') {
                        int uCt = 0, dCt = 0, uIx = 0;
                        for (int r = n; r < m; ++r) {
                            if (cy.charAt(r) == ')') {
                                ++uCt;
                                uIx = r;
                            } else if (cy.charAt(r) == '(') {
                                ++dCt;
                            }
                        }
                        char aOpen = cy.charAt(uIx + 1);
                        boolean q = aOpen != ' ' || uIx + 2 != m;
                        if (uCt != dCt || mChar == '(' && q) {
                            throw error("Parentheses Error");
                        }
                    } else {
                        throw error("Duplicate Cycles Characters");
                    }
                }
            }
        }
        return cy;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** HashMap to map permutation values. */
    private HashMap perm = new HashMap(0);

    /** HashMap to inverse map permutation values. */
    private HashMap revPerm = new HashMap(0);
}
