package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author PNH, MS
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine m = config(_config);
        applyFreshMachine(m);
    }

    private void applyFreshMachine(Machine m) {
        String settings = _input.nextLine();
        while (settings == "") {
            settings = _input.nextLine();
            _output.println();
        }

        setUp(m, settings);
        while (_input.hasNextLine()) {
            if (!_input.hasNext("\\*")) {
                String out = m.convert(_input.nextLine());
                if (out.equals("")) {
                    _output.println();
                    continue;
                } else {
                    _output.println(format(out));
                }
            } else {
                applyFreshMachine(m);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config.
     *  @param config is the scanner that reads the config file.
     *  @return a configurated machine.*/
    private Machine config(Scanner config) {
        alphaPawlRotorRead(config);
        ArrayList<Rotor> allRotors = new ArrayList<>();
        Machine m = new Machine(_alphabet, _numRotors, _numPawls, allRotors);
        Rotor lastAdded = null;
        int i = -1;
        while (config.hasNextLine()) {
            String newLine = config.nextLine().trim();
            while (newLine.matches("[\\s]*")) {
                if (config.hasNextLine()) {
                    newLine = config.nextLine();
                } else {
                    return m;
                }
            }
            Scanner line = new Scanner(newLine);
            if (newLine.matches(_movingRotor)) {
                i += 1;
                String name = line.next();
                String notches = line.next().substring(1);
                String perm = line.nextLine();
                Permutation p = new Permutation(perm, _alphabet);
                MovingRotor r = new MovingRotor(name, p, notches);
                lastAdded = r;
                allRotors.add(r);
            } else if (newLine.matches(_fixedRotor)) {
                i += 1;
                String name = line.next();
                line.next();
                String perm = line.nextLine();
                Permutation p = new Permutation(perm, _alphabet);
                FixedRotor r = new FixedRotor(name, p);
                lastAdded = r;
                allRotors.add(r);
            } else if (newLine.matches(_reflectingRotor)) {
                i += 1;
                String name = line.next();
                line.next();
                String perm = line.nextLine();
                Permutation p = new Permutation(perm, _alphabet);
                Reflector r = new Reflector(name, p);
                lastAdded = r;
                allRotors.add(r);
            } else if (newLine.matches(_continuedPerm)
                    && lastAdded != null) {
                allRotors.remove(i);
                Scanner sc = new Scanner(newLine);
                while (sc.hasNext()) {
                    lastAdded.permutation().addCycle(sc.next());
                }
                Rotor temp = lastAdded;
                allRotors.add(temp);
                lastAdded = null;
            } else {
                throw error("bad rotor description");
            }
        }
        m = new Machine(_alphabet, _numRotors, _numPawls, allRotors);
        return m;
    }

    /** Just because I am a contrarian.
     * @param config is the scanner that reads the config file.*/
    public void alphaPawlRotorRead(Scanner config) {
        String alphabet = config.nextLine();
        while (alphabet.equals("")) {
            alphabet = config.nextLine();
        }
        alphabet = alphabet.trim();
        if (!alphabet.matches(_alpha)) {
            throw error("invalid alphabet");
        } else {
            _alphabet = new Alphabet(alphabet);
        }
        String check = config.nextLine();
        Scanner rp = new Scanner(check);
        if (!check.matches("([\\s]*[\\d][\\s]+[\\d][\\s]*)")) {
            throw error("where are the pawls and rotors?!");
        }
        try {
            _numRotors = Integer.parseInt(rp.next());
        } catch (EnigmaException excp) {
            throw error("Rotors not well formed");
        }
        try {
            _numPawls = Integer.parseInt(rp.next());
        } catch (EnigmaException excp) {
            throw error("Pawls not well formed");
        }
        if (_numPawls > _numRotors
               || _numRotors < 1) {
            throw error("Invalid number of rotors or pawls.");
        }
    }



     /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        settings = settings.trim();
        settings.replaceAll("[\\s]*", " ");


        if (!settings.startsWith("*")) {
            throw error("invalid settings line");
        }
        if ((!checkSettings(settings))) {
            throw error("invalid settings line");
        }
        Scanner s = new Scanner(settings);
        s.next();

        String rotors = s.next();
        for (int i = 0; i < (_numRotors - 1); i++) {
            rotors += " " + s.next();
        }

        String[] rotors1 = rotors.split(" ");


        if (M.numRotors() == 0) {
            M.insertRotors(rotors1);
        } else {
            M.resetRotors();
            M.insertRotors(rotors1);
        }

        M.setRotors(s.next());

        if (!(s.hasNext("([\\s]*(([\\(].+[\\)])*\\s*)+)"))
               && s.hasNext()) {
            M.addRingSetting(s.next());
        }
        if (s.hasNext()) {
            String perms = s.next();
            while (s.hasNext()) {
                perms += " " + s.next();
            }
            Permutation p = new Permutation(perms, _alphabet);
            M.setPlugboard(p);
        } else {
            Permutation p = new Permutation("", _alphabet);
            M.setPlugboard(p);
        }
    }

    boolean checkSettings(String settings) {
        Scanner sc = new Scanner(settings);
        int c = 0;
        while (!(sc.hasNext("([\\s]*(([\\(].+[\\)])*\\s*)+)"))
                && sc.hasNext()) {
            c += 1;
            sc.next();
        }
        return (c == 2 + _numRotors || c == 3 + _numRotors);
    }



    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** formats MSG into groups of five (except that the last group may
     *  have fewer letters).
     *  @return a correctly formatted string*/
    private String format(String msg) {
        String result = "";
        result += msg.charAt(0);
        for (int i = 1; i < msg.length(); i++) {
            if (i % 5 == 0) {
                result += " ";
            }
            result += msg.charAt(i);
        }
        return result;
    }

    /** Number of rotors. */
    private int _numRotors;

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** Number of rotors. */
    private int _numPawls;

    /** REGEX for alphabet. */
    private String _alpha =
            "^[^\\s\\)\\(*]+";
    /** Regex for moving rotor. */
    private String _movingRotor =
            "^[\\w]+[\\s]+[M][\\w]+[\\s]+(([\\(].+[\\)])\\s*)+";
    /** Regex for reflecting rotor. */
    private String _reflectingRotor =
            "^[\\w]+[\\s]+[R][\\s]+(([\\(].+[\\)])\\s*)+";
    /** Regex for fixed rotor.. */
    private String _fixedRotor =
            "^[\\w]+[\\s]+[N][\\s]+(([\\(].+[\\)])\\s*)+";
    /** regex for a continued permutation. */
    private String _continuedPerm =
            "([\\s]*(([\\(].+[\\)])*\\s*)+)";

}
