package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Karl Meissner and CS61B Staff
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    Alphabet standard = new Alphabet();

    Permutation tester1 = new Permutation("(ABCD)", standard);
    Permutation tester2 = new Permutation("(BE)", new Alphabet("ABCDEFG"));
    Permutation tester3 = new Permutation("(AB) (DF) (GHI)", standard);
    Permutation tester4 = new Permutation("(ABC)  (DEF)   (HIJ)", standard);
    Permutation tester5 = new Permutation("(ABCDE)", new Alphabet("ABCDE"));
    Permutation tester6 = new Permutation("(AB)(CD)", standard);

    @Test
    public void permSizeTests() {
        assertEquals(26, tester1.size());
        assertEquals(7, tester2.size());
        assertEquals(26, tester3.size());
        assertEquals(26, tester6.size());
    }

    @Test
    public void permuteTests() {
        assertEquals(14, tester1.permute(40));
        assertEquals(2, tester1.permute(1));
        assertEquals(0, tester1.permute(3));
        assertEquals(5, tester3.permute(3));
        assertEquals(6, tester3.permute(8));
        assertEquals('C', tester1.permute('B'));
        assertEquals('B', tester2.permute('E'));
        assertEquals('B', tester3.permute('A'));
        assertEquals('A', tester3.permute('B'));
        assertEquals('D', tester3.permute('F'));
        assertEquals('G', tester3.permute('I'));
        assertEquals('A', tester4.permute('C'));
        assertEquals('A', tester6.permute('B'));
    }

    @Test
    public void invertTests() {
        assertEquals(14, tester1.invert(40));
        assertEquals(1, tester1.invert(2));
        assertEquals(3, tester1.invert(0));
        assertEquals(3, tester3.invert(5));
        assertEquals(8, tester3.invert(6));
        assertEquals('B', tester1.invert('C'));
        assertEquals('E', tester2.invert('B'));
        assertEquals('A', tester3.invert('B'));
        assertEquals('B', tester3.invert('A'));
        assertEquals('F', tester3.invert('D'));
        assertEquals('I', tester3.invert('G'));
        assertEquals('C', tester4.invert('A'));
    }

    @Test
    public void derangementTests() {
        assertFalse(tester1.derangement());
        assertFalse(tester3.derangement());
        assertFalse(tester4.derangement());
        assertTrue(tester5.derangement());
    }

    @Test(expected = EnigmaException.class)
    public void constructorTests() {
        String a = "Cycle char not in Alphabet";
        String b = "Duplicate Cycles Characters";

        assertEquals("Parentheses Error", new Permutation("((ABC)", standard));
        assertEquals("Parentheses Error", new Permutation("( (ABC)", standard));
        assertEquals(a, new Permutation("($DF%)", standard));
        assertEquals("Parentheses Error", new Permutation("()", standard));
        assertEquals(b, new Permutation("(AABC)", standard));
        assertEquals("Parentheses Error", new Permutation("(A B)", standard));
        assertEquals("No Whitespace Allowed", new Permutation("A B", standard));
        assertEquals("Parentheses Error", new Permutation(")(", standard));
    }
}
