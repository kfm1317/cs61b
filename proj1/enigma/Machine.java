package enigma;

import java.util.Collection;

import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Karl Meissner and CS61B Staff
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _rotors = new Rotor[_numRotors];
        _allRotors = allRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        int movers = 0;
        for (int i = 0; i < _numRotors; ++i) {
            int name = rotors[i].length();
            for (Rotor avail : _allRotors) {
                String aNam = avail.name();
                if (rotors[i].contains(avail.name()) && name == aNam.length()) {
                    _rotors[i] = avail;
                    if (_rotors[i].rotates()) {
                        ++movers;
                    }
                }
            }
            Rotor curr = _rotors[i];
            boolean ref = curr.reflecting();
            if (curr == null || i > 0 && ref || i == 0 && !ref) {
                throw error("Rotor Initialization Error");

            } else if (i > 0 && !curr.rotates() && _rotors[i - 1].rotates()) {
                throw error("Rotor Initialization Error");
            }
            for (int j = 0; j < i; ++j) {
                if (curr.equals(_rotors[j])) {
                    throw error("Rotor Initialization Error");
                }
            }
        }
        if (!_rotors[numRotors() - 1].rotates() || movers != numPawls()) {
            throw error("Rotor Types Error");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 1; i < numRotors(); ++i) {
            if (!_alphabet.contains(setting.charAt(i - 1))) {
                throw error("Invalid Setting");
            }
            _rotors[i].set(setting.charAt(i - 1));
            _rotors[i]._initial = _rotors[i].setting();
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        c = _plugboard.permute(c);
        for (int k = 0; k < numRotors(); ++k) {
            _rotors[k]._initial = _rotors[k].setting();
        }
        for (int i = numRotors() - 1; i >= 0; --i) {
            Rotor curr = _rotors[i];
            if (i != 0) {
                if (i == numRotors() - 1) {
                    _rotors[i].advance();
                }
                if (curr.setting() != curr._initial) {
                    char prev = curr.alphabet().toChar(curr._initial);
                    String s = " ";
                    s = s.replace(' ', prev);
                    if (((MovingRotor) curr)._notches.contains(s)) {
                        _rotors[i - 1].advance();
                    }

                } else if (curr.atNotch() && _rotors[i - 1].rotates()) {
                    _rotors[i].advance();
                    _rotors[i - 1].advance();
                }
            }
            c = _rotors[i].convertForward(c);
        }
        for (int k = 1; k < numRotors(); ++k) {
            c = _rotors[k].convertBackward(c);
        }
        c = _plugboard.invert(c);
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        StringBuilder word = new StringBuilder();
        Scanner printer = new Scanner(msg);
        while (printer.hasNext()) {
            String s = printer.next();
            for (int i = 0; i < s.length(); ++i) {
                int ch = _alphabet.toInt(s.charAt(i));
                char conv = _alphabet.toChar(convert(ch));
                word.append(conv);
            }
        }
        return word.toString();
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors in machine. */
    private int _numRotors;

    /** Number of pawls in machine. */
    private int _pawls;

    /** Plugboard used for machine. */
    private Permutation _plugboard = new Permutation("", new Alphabet());

    /** List to hold all Rotors. */
    protected Rotor[] _rotors;

    /** Collection of all available rotors. */
    private Collection<Rotor> _allRotors;
}
