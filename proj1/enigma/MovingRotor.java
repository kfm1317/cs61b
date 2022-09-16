package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Karl Meissner and CS61B Staff
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }

    /** Moving rotors all can rotate. */
    @Override
    boolean rotates() {
        return true;
    }

    /** Check if a rotor is set to a character that is one of its notches. */
    @Override
    boolean atNotch() {
        for (int i = 0; i < _notches.length(); ++i) {
            if (_notches.charAt(i) == alphabet().toChar(setting())) {
                return true;
            }
        }
        return false;
    }

    /** Increment rotor setting by one. */
    @Override
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    /** String indicating where Rotor's notches are located. */
    protected String _notches;

}
