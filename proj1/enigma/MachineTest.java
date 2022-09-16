package enigma;

import org.junit.Test;

import org.junit.Assert;

import java.util.Collection;

import java.util.HashSet;

import static enigma.EnigmaException.*;

/** The suite of all JUnit tests for the Machine class.
 * @author Karl Meissner
 */

public class MachineTest {

    /* ***** TESTING UTILITIES ***** */

    /** Build a sample machine. */
    public Machine build() {
        Alphabet standard = new Alphabet();

        String perm1 = "(AE)(BN)(CK)(DQ)(FU)(GY)(HW)(IJ)(LO)(MP)(RX)(SZ)(TV)";
        String perm2 = "(ALBEVFCYODJWUGNMQTZSKPR) (HIX)";
        String perm3 = "(ABDHPEJT) (CFLVMZOYQIRWUKXSG) (N)";
        String perm4 = "(AEPLIYWCOXMRFZBSTGJQNH) (DV) (KU)";
        String perm5 = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";

        Permutation p1 = new Permutation(perm1, standard);
        Permutation p2 = new Permutation(perm2, standard);
        Permutation p3 = new Permutation(perm3, standard);
        Permutation p4 = new Permutation(perm4, standard);
        Permutation p5 = new Permutation(perm5, standard);

        Rotor loren = new Reflector("Loren", p1);
        Rotor paul = new MovingRotor("Paul", p2, "");
        Rotor rachel = new MovingRotor("Rachel", p3, "V");
        Rotor karl = new MovingRotor("Karl", p4, "J");
        Rotor anthony = new MovingRotor("Anthony", p5, "Q");


        Collection<Rotor> c = new HashSet<Rotor>();
        c.add(loren);
        c.add(paul);
        c.add(rachel);
        c.add(karl);
        c.add(anthony);

        return new Machine(standard, 5, 4, c);
    }

    /** Build Machine with Challenging Specs. */
    public Machine build2() {
        Alphabet standard = new Alphabet();

        String perm1 = "(AE)(BN)(CK)(DQ)(FU)(GY)(HW)(IJ)(LO)(MP)(RX)(SZ)(TV)";
        String perm2 = "(ALBEVFCYODJWUGNMQTZSKPR) (HIX)";
        String perm3 = "(ABDHPEJT) (CFLVMZOYQIRWUKXSG) (N)";
        String perm4 = "(AEPLIYWCOXMRFZBSTGJQNH) (DV) (KU)";
        String perm5 = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";

        Permutation p1 = new Permutation(perm1, standard);
        Permutation p2 = new Permutation(perm2, standard);
        Permutation p3 = new Permutation(perm3, standard);
        Permutation p4 = new Permutation(perm4, standard);
        Permutation p5 = new Permutation(perm5, standard);

        Rotor loren = new Reflector("Loren", p1);
        Rotor paul = new FixedRotor("Paul", p2);
        Rotor rachel = new FixedRotor("Rachel", p3);
        Rotor karl = new MovingRotor("Karl", p4, "J");
        Rotor anthony = new MovingRotor("Anthony", p5, "Q");


        Collection<Rotor> c = new HashSet<Rotor>();
        c.add(loren);
        c.add(paul);
        c.add(rachel);
        c.add(karl);
        c.add(anthony);

        return new Machine(standard, 5, 2, c);
    }

    Machine m = build();
    Machine n = build2();
    String[] a = {"Loren", "Paul", "Rachel", "Karl", "Anthony"};
    Permutation plug = new Permutation("(YF) (ZH)", new Alphabet());

    /* ***** TESTS ***** */

    @Test
    public void consMachine() {
        Assert.assertEquals(5, m.numRotors());
    }

    @Test
    public void insertRotorsTest() {
        m.insertRotors(a);
        Assert.assertEquals("Loren", m._rotors[0].name());
        Assert.assertEquals("Rachel", m._rotors[2].name());
        Assert.assertEquals(true, m._rotors[3].rotates());
        Assert.assertEquals(false, m._rotors[2].atNotch());
    }

    @Test
    public void setRotorsTest() {
        m.insertRotors(a);
        m.setRotors("AXLE");
        Assert.assertEquals(0, m._rotors[0].setting());
        Assert.assertEquals(0, m._rotors[1].setting());
        Assert.assertEquals(23, m._rotors[2].setting());
        Assert.assertEquals(4, m._rotors[4].setting());
    }

    @Test
    public void convertTests() {
        m.insertRotors(a);
        m.setRotors("AXLE");
        m.setPlugboard(plug);

        Assert.assertEquals(25, m.convert(24));
        Assert.assertEquals(15, m.convert(1));
    }

    @Test
    public void stringConvertTests() {
        m.insertRotors(a);
        n.insertRotors(a);
        m.setRotors("AXLE");
        n.setRotors("BVJQ");
        m.setPlugboard(plug);
        n.setPlugboard(plug);

        Assert.assertEquals("FPFCU", m.convert("ABCDE"));
        m.setRotors("AVJQ");
        Assert.assertEquals("C", m.convert("Y"));
        Assert.assertEquals("L", n.convert("Y"));
    }
}
