package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a reflector in the enigma.
 *  @author Karl Meissner and CS61B Staff
 */
class Reflector extends FixedRotor {

    /** A non-moving rotor named NAME whose permutation at the 0 setting
     * is PERM. */
    Reflector(String name, Permutation perm) {
        super(name, perm);
    }

    /** All refectors reflect. */
    @Override
    boolean reflecting() {
        return true;
    }

    /** Set rotor position to the chacter with index POSN in alphabet. */
    @Override
    void set(int posn) {
        if (posn != 0) {
            throw error("reflector has only one position");
        }
    }

    /** Set rotor position to CPSON. */
    @Override
    void set(char cpson) {
        if (alphabet().toInt(cpson) != 0) {
            throw error("reflector has only one position");
        }
    }
}
