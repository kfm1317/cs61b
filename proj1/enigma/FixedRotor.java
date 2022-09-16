package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotor that has no ratchet and does not advance.
 *  @author Karl Meissner and CS61B Staff
 */
class FixedRotor extends Rotor {

    /**
     * A non-moving rotor named NAME whose permutation at the 0 setting
     * is given by PERM.
     */
    FixedRotor(String name, Permutation perm) {
        super(name, perm);
    }
}
