package enigma;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author PNH, MS
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initially in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches.toCharArray();
        this.set(0);
    }



    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        this.set(permutation().wrap(this.setting() + 1));
    }


    boolean atNotch() {
        char setting = alphabet().toChar(this.setting());
        for (int i = 0; i < _notches.length; i++) {
            if (setting == _notches[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    char[] notches() {
        return _notches;
    }

    /** Char array of notches of this rotor.*/
    private char[] _notches;
}
