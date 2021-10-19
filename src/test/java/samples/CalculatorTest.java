package samples;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import samples.Calculator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CalculatorTest {
    private Calculator calculator;

    @Before
    public void setUp() throws Exception {
        this.calculator = new Calculator();
    }

    @After
    public void tearDown() throws Exception {
        calculator = null;
    }

    @Test
    public void testAdd_positiveNumbers_shouldReturnResult() {
        assertEquals("add", 7, calculator.add(3, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdd_negativeNumbers_shouldThrowException() {
        calculator.add(-3, -4);
    }

    @Test
    public void testSubtract() {
        assertEquals("substract", 2, calculator.substract(5, 3));
    }

    @Test
    public void testMultiply() {
        assertEquals("multiply", 56, calculator.multiply(7, 8));
    }

    @Test
    @Ignore
    public void testFindMax(){
        assertEquals(4,calculator.findMax(new int[]{1,3,4,2}));
        assertEquals(-2,calculator.findMax(new int[]{-12,-3,-4,-2}));
    }

    @Test
    public void testCube(){
        System.out.println("test case cube");
        assertEquals(27,calculator.cube(3));
    }

    @Test
    public void testReverseWord(){
        System.out.println("test case reverse word");
        assertEquals("ym eman si inined ",calculator.reverseWord("my name is denini"));
    }

    @Test
    public void m5() {
        List list  = new ArrayList<String>();
        list.add("test");
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Ignore
    public void m6() {
        System.out.println("Using @Ignore , this execution is ignored");
    }
}
