package enigma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author PNH, MS
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        if (pawls < 1 || pawls >= numRotors) {
            throw error("wrong number of rotors");
        }
        _alphabet = alpha;
        _allRotors = allRotors;
        _pawls = pawls;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _rotors.size();
    }

    void resetRotors() {
        _rotors = new ArrayList<>();
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {

        if (k > numRotors() - 1) {
            throw error("invalid index for rotors");
        }
        return _rotors.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (!rotorsUnique(rotors)) {
            throw error("rotors cannot be repeated.");
        }
        for (int i = 0; i < rotors.length; i++) {
            for (Rotor r : _allRotors) {
                String a = r.name();
                String b = rotors[i];
                if (a.equals(b)) {
                    _rotors.add(r);
                }
            }
        }
        if (!(_rotors.get(0) instanceof Reflector)) {
            throw error("first rotor must be a reflector");
        }

        if (_rotors.size() < rotors.length) {
            throw error("at least one rotor is misnamed,"
                    + " or you have not provided enough rotors.");
        }
    }

    boolean rotorsUnique(String[] rotors) {
        Set<String> s = new HashSet<String>(Arrays.asList(rotors));
        return (s.size() == rotors.length);
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        assert (setting.length() == (_rotors.size() - 1));

        for (int i = 0; i < setting.length(); i++) {
            if (!_alphabet.myContains(setting.charAt(i))) {
                throw error("Invalid settings");
            }
            _rotors.get(i + 1).set(setting.charAt(i));
        }
    }


    /** Attempted solution for adding the ring setting. Incomplete.
     * @param setting is a settings line.*/
    void addRingSetting(String setting) {
        assert (setting.length() == (_rotors.size() - 1));
        for (int i = 0; i < setting.length(); i++) {
            if (!_alphabet.myContains(setting.charAt(i))) {
                throw error("Invalid settings");
            }
        }
    }



    /** Return the current plugboard's permutation. */

    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();

        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }

        c = plugboard().permute(c);

        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);

        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        int n = numRotors() - 1;

        for (int q = 1; q < n; q++) {
            if (_rotors.get(q + 1).atNotch()
                  &&  _rotors.get(q + 1).rotates()
                    && _rotors.get(q).rotates()) {

                Rotor q1 = _rotors.get(q + 1);
                Rotor q2 = _rotors.get(q);

                if (!q1.isRotated()) {
                    _rotors.get(q + 1).set((q1.setting() + 1));
                    _rotors.get(q + 1).toggleRotated(true);
                }
                if (!q2.isRotated()) {
                    _rotors.get(q).set((q2.setting() + 1));
                    _rotors.get(q).toggleRotated(true);
                }
            }

        }
        if (!_rotors.get(n).isRotated()) {
            Rotor r = _rotors.get(n);
            r.set((r.setting() + 1));
        }

        for (Rotor r : _rotors) {
            r.toggleRotated(false);
        }

    }


    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        int i = numRotors() - 1;
        for (; i > 0; i--) {
            c = _rotors.get(i).convertForward(c);
        }

        c = _rotors.get(0).convertForward(c);
        i += 1;
        for (; i < numRotors(); i++) {
            c = _rotors.get(i).convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == ' ') {
                continue;
            }
            int c1 = _rotors.get(0).permutation().alphabet().toInt(c);
            c1 = convert(c1);
            c = _rotors.get(0).permutation().alphabet().toChar(c1);
            result += c;
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Available Rotors. */
    private Collection<Rotor> _allRotors;

    /** Inserted Rotors. */
    private ArrayList<Rotor> _rotors = new ArrayList<>();

    /** number of Pawls. */
    private int _pawls;

    /** Plugboard of this machine. */
    private Permutation _plugboard;




}
