package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author PNH, MS
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }


    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }


    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    char chSetting() {
        return alphabet().toChar(setting());
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = posn % alphabet().size();
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        int r = _permutation.alphabet().toInt(cposn);
        _setting = r % alphabet().size();
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {

        int adjusted = permutation().wrap(p + this._setting);
        int result = _permutation.permute(adjusted);
        result = _permutation.wrap(result - this._setting);

        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        return result;
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {

        int adjusted = permutation().wrap(e + this._setting);
        int result = _permutation.invert(adjusted);
        result = _permutation.wrap(result - this._setting);

        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }

        return result;
    }

    /** Returns the positions of the notches, as a string giving the letters
     *  on the ring at which they occur. */
    char[] notches() {
        return null;
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }


    boolean isRotated() {
        return _rotated;
    }

    public void toggleRotated(boolean a) {
        _rotated = a;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** Setting of this rotor.*/
    private int _setting;

    /** to prevent rotating twice.*/
    private boolean _rotated = false;

}
