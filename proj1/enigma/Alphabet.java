package enigma;

import static enigma.EnigmaException.*;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Karl Meissner and CS61B Staff
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */

    Alphabet(String chars) {
        str = chars;
        int len = chars.length();
        if (len == 0 || chars.contains("\\s") || chars.contains("*")) {
            throw error("Invalid Alphabet");
        }
        for (int i = 0; i < chars.length(); ++i) {
            for (int j = i + 1; j < chars.length(); ++j) {
                if (chars.charAt(j) == chars.charAt(i)) {
                    throw error("Duplicate Alphabet Values");
                }
            }
            ++length;
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of this alphabet. */
    int size() {
        return length;
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        for (int i = 0; i < length; ++i) {
            if (str.charAt(i) == ch) {
                return true;
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if (index < 0 || index >= size()) {
            throw error("Index Out of Bounds");
        }
        return str.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        for (int i = 0; i < length; ++i) {
            if (str.charAt(i) == ch) {
                return i;
            }
        }
        throw error("Character not in Alphabet");
    }

    /** String object for storing this Alphabet's string origin. */
    private String str;

    /** Length of this Alphabet. */
    private int length;



}
