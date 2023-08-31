package enigma;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author PNH, MS
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycleSetUp(cycles);

    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    void addCycle(String cycle) {
        String[] update = new String[_cycles.length + 1];
        for (int i = 0; i < update.length; i++) {
            if (i == (update.length - 1)) {
                update[i] = cycleSetUp(cycle)[0];
            } else {
                update[i] = _cycles[i];
            }
        }
        _cycles = update;
    }

    /** Return the cycles of this permutation.
     * @param p is this permutation.*/
    public String[] getCycles(Permutation p) {
        return p._cycles;
    }

    /** Set up the cycles into an Array of type String.
     *  @param cycle is the string given to the constructor.
     *  @return a cycle set up as an array of strings.*/
    private String[] cycleSetUp(String cycle) {

        cycle = cycle.replaceAll("\\)", "");
        cycle = cycle.replaceAll("\\(", "");
        cycle = cycle.trim();

        return cycle.split((" "));
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
        char ch = alphabet().toChar(wrap(p));

        char result = 0;
        for (String cycle : _cycles) {
            for (int j = 0; j < cycle.length(); j++) {
                if (cycle.charAt(j) == ch) {

                    int m = (j + 1) % cycle.length();

                    result = cycle.charAt(m);
                    return _alphabet.toInt(result);
                }
            }
        }
        return wrap(p);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char ch = alphabet().toChar(wrap(c));

        char result = '0';
        for (String cycle : _cycles) {
            for (int j = 0; j < cycle.length(); j++) {
                if (cycle.charAt(j) == ch) {

                    int m = ((j - 1) % cycle.length());

                    while (m < 0) {
                        m += cycle.length();
                    }

                    result = cycle.charAt(m);
                    int s = _alphabet.toInt(result);
                    return s;
                }
            }
        }
        return c;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int a = _alphabet.toInt(p);
        int b = permute(a);
        char result = _alphabet.toChar(b);
        return result;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int a = _alphabet.toInt(c);
        int b = invert(a);
        char result = _alphabet.toChar(b);
        return result;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Cycles of this permutation. */
    private String[] _cycles;

}
