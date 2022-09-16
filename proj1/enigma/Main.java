package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.Collection;
import java.util.HashSet;

/** GSI suggested this import. */
import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Karl Meissner and CS61B Staff
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
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
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
        _alphabet = new Alphabet(_config.next());
        Machine m = readConfig();
        StringBuilder total = new StringBuilder();
        char sample = _alphabet.toChar(0);
        StringBuilder plugPerm = new StringBuilder().append(sample);
        String pluggy = plugPerm.toString();
        String next = _input.nextLine();
        Scanner sNext = new Scanner(next);
        _input = _input.useDelimiter("^[//s]");
        while (_input.hasNextLine()) {
            if (sNext.next().equals("*")) {
                String[] inserts = new String[m.numRotors()];
                for (int i = 0; i < inserts.length; ++i) {
                    inserts[i] = sNext.next();
                }
                if (!sNext.hasNext()) {
                    throw error("Bad rotor name/wheel settings wrong");
                }
                String rotorSet = sNext.next();
                for (int i = 0; i < rotorSet.length(); ++i) {
                    if (!_alphabet.contains(rotorSet.charAt(i))) {
                        throw error("Bad character in wheel settings");
                    }
                }
                if (rotorSet.length() != m.numRotors() - 1) {
                    throw error("Wheel settings wrong");
                }
                StringBuilder pBoard = new StringBuilder();
                while (sNext.hasNext()) {
                    pBoard.append(sNext.next());
                }
                String mPlug = pBoard.toString();
                m.insertRotors(inserts);
                setUp(m, rotorSet);
                if (mPlug.contains("(")) {
                    pluggy = mPlug;
                }
            }
            m.setPlugboard(new Permutation(pluggy, _alphabet));
            next = _input.nextLine();
            sNext = new Scanner(next);
            if (next.isEmpty()) {
                _output.println();
                next = _input.nextLine();
                sNext = new Scanner(next);
            } else if (next.charAt(0) != '*') {
                total.append(m.convert(next));
                printMessageLine(total.toString());
                total = new StringBuilder();
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            Collection<Rotor> fullSet = new HashSet<Rotor>();
            while (_config.hasNext()) {
                fullSet.add(readRotor());
            }
            if (fullSet == null) {
                throw error("No Rotors Given");
            }
            return new Machine(_alphabet, numRotors, numPawls, fullSet);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            String subsq = _config.next();
            char type = subsq.charAt(0);
            String notches = " ";
            if (subsq.length() > 1) {
                notches = subsq.substring(1);
            }
            StringBuilder cycles = new StringBuilder();
            boolean hNext = _config.hasNext();
            while (hNext && _config.hasNext("([(][^\\()*\\s]+[)][\\s]*+)+")) {
                cycles.append(_config.next());
            }
            String nCycles = cycles.toString();
            Permutation perm = new Permutation(nCycles, _alphabet);
            Permutation bad = new Permutation("", _alphabet);
            Rotor r = new Rotor("name", bad);
            if (type == 'R') {
                r = new Reflector(name, perm);
            } else if (type == 'N') {
                r = new FixedRotor(name, perm);
            } else if (type == 'M' && !notches.equals(" ")) {
                r = new MovingRotor(name, perm, notches);
            }
            if (r.permutation() == bad) {
                throw error("bad rotor description");
            }
            return r;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        M.setRotors(settings);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < msg.length(); i += 5) {
            message.append(msg.substring(i, Math.min(msg.length(), i + 5)));
            message.append(" ");
        }
        String mess = message.toString();
        mess = mess.substring(0, mess.length() - 1);
        System.out.println(mess.toString());
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}
