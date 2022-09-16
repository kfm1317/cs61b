package enigma;

import org.junit.Test;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Alphabet class.
 * @author Karl Meissner
 */

public class AlphabetTest {

    /* ***** TESTING UTILITIES ***** */

    Alphabet al = new Alphabet("NDESOP");
    Alphabet standard = new Alphabet();

    /* ***** TESTS ***** */

    @Test(expected = EnigmaException.class)
    public void alphabetConstructorTest() {
        assertEquals("Invalid Alphabet", new Alphabet(""));
        assertEquals("Duplicate Alphabet Values", new Alphabet("ABCDC"));
        assertEquals("Invalid Alphabet", new Alphabet(" "));
    }

    @Test
    public void alphabetSizeTests() {
        assertEquals(6, al.size());
        assertEquals(26, standard.size());
    }

    @Test
    public void alphabetContainsTest() {
        assertTrue(al.contains('S'));
        assertFalse(al.contains('T'));
        assertTrue(standard.contains('X'));
        assertFalse(standard.contains('#'));
    }

    @Test
    public void alphabetToCharTests() {
        assertEquals('E', al.toChar(2));
        assertEquals('Y', standard.toChar(24));
    }

    @Test(expected = EnigmaException.class)
    public void toCharBoundaryTests() {
        assertEquals("Index Out of Bounds", al.toChar(20));
        assertEquals("Index Out of Bounds", standard.toChar(-1));
    }

    @Test
    public void alphabetToIntTests() {
        assertEquals(4, al.toInt('O'));
        assertEquals(12, standard.toInt('M'));
    }

    @Test(expected = EnigmaException.class)
    public void toIntContainerTests() {
        assertEquals("Character not in Alphabet", al.toInt('@'));
        assertEquals("Character not in Alphabet", standard.toInt('$'));
    }
}
