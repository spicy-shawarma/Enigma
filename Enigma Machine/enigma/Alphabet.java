package enigma;

import static enigma.EnigmaException.error;
import static org.junit.Assert.assertTrue;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author PNH, MS
 */
class Alphabet {

    /** Characters of this alphabet. */
    private String _chars;


    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _chars = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if CH is in this alphabet. */

    boolean myContains(char ch) {

        for (int i = 0; i < _chars.length(); i++) {
            if (_chars.charAt(i) == ch) {
                return true;
            }
        }
        return false;

    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {

        assertTrue(((0 <= index) && (index < size())));
        return _chars.charAt(index);

    }


    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        if (!(myContains(ch))) {
            throw error("invalid message for alphabet.");
        }
        return _chars.indexOf(ch);
    }
}
