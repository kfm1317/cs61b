package enigma;

import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author Karl Meissner and CS61B Staff
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(AlphabetTest.class,
                PermutationTest.class,
                MovingRotorTest.class,
                MachineTest.class));
    }

}


