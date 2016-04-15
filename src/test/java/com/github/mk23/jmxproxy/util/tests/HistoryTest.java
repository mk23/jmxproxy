package com.github.mk23.jmxproxy.util.tests;

import com.github.mk23.jmxproxy.util.History;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HistoryTest {
    @Rule public TestName name = new TestName();

    @Before
    public void printTestName() {
        System.out.println(" -> " + name.getMethodName());
    }

    @Test
    public void check0Single() throws Exception {
        History<String> history = new History<String>(3);
        assertNull(history.getLast());
    }
    @Test
    public void check0ArrayFull() throws Exception {
        History<String> history = new History<String>(3);
        assertTrue(history.get().size() == 0);
    }
    @Test
    public void check0ArrayZero() throws Exception {
        History<String> history = new History<String>(3);
        assertTrue(history.get(0).size() == 0);
    }
    @Test
    public void check0ArrayTen() throws Exception {
        History<String> history = new History<String>(3);
        assertTrue(history.get(10).size() == 0);
    }
    @Test
    public void check0ArrayTwo() throws Exception {
        History<String> history = new History<String>(3);
        assertTrue(history.get(2).size() == 0);
    }
    @Test
    public void check0ArrayOne() throws Exception {
        History<String> history = new History<String>(3);
        assertTrue(history.get(1).size() == 0);
    }

    @Test
    public void check1Single() throws Exception {
        History<String> history = new History<String>(3);
        String target = new String("1");

        history.add(new String("1"));
        assertEquals(target, history.getLast());
    }
    @Test
    public void check1ArrayFull() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("1") };

        history.add(new String("1"));
        assertArrayEquals(target, history.get().toArray());
    }
    @Test
    public void check1ArrayZero() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("1") };

        history.add(new String("1"));
        assertArrayEquals(target, history.get(0).toArray());
    }
    @Test
    public void check1ArrayTen() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("1") };

        history.add(new String("1"));
        assertArrayEquals(target, history.get(10).toArray());
    }
    @Test
    public void check1ArrayTwo() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("1") };

        history.add(new String("1"));
        assertArrayEquals(target, history.get(2).toArray());
    }
    @Test
    public void check1ArrayOne() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("1") };

        history.add(new String("1"));
        assertArrayEquals(target, history.get(1).toArray());
    }

    @Test
    public void check2Single() throws Exception {
        History<String> history = new History<String>(3);
        String target = new String("2");

        history.add(new String("1"));
        history.add(new String("2"));
        assertEquals(target, history.getLast());
    }
    @Test
    public void check2ArrayFull() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("2"), new String("1") };

        history.add(new String("1"));
        history.add(new String("2"));
        assertArrayEquals(target, history.get().toArray());
    }
    @Test
    public void check2ArrayZero() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("2"), new String("1") };

        history.add(new String("1"));
        history.add(new String("2"));
        assertArrayEquals(target, history.get(0).toArray());
    }
    @Test
    public void check2ArrayTen() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("2"), new String("1") };

        history.add(new String("1"));
        history.add(new String("2"));
        assertArrayEquals(target, history.get(10).toArray());
    }
    @Test
    public void check2ArrayTwo() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("2"), new String("1") };

        history.add(new String("1"));
        history.add(new String("2"));
        assertArrayEquals(target, history.get(2).toArray());
    }
    @Test
    public void check2ArrayOne() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("2") };

        history.add(new String("1"));
        history.add(new String("2"));
        assertArrayEquals(target, history.get(1).toArray());
    }

    @Test
    public void check3Single() throws Exception {
        History<String> history = new History<String>(3);
        String target = new String("3");

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        assertEquals(target, history.getLast());
    }
    @Test
    public void check3ArrayFull() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("3"), new String("2"), new String("1") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        assertArrayEquals(target, history.get().toArray());
    }
    @Test
    public void check3ArrayZero() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("3"), new String("2"), new String("1") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        assertArrayEquals(target, history.get(0).toArray());
    }
    @Test
    public void check3ArrayTen() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("3"), new String("2"), new String("1") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        assertArrayEquals(target, history.get(10).toArray());
    }
    @Test
    public void check3ArrayTwo() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("3"), new String("2") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        assertArrayEquals(target, history.get(2).toArray());
    }
    @Test
    public void check3ArrayOne() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("3") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        assertArrayEquals(target, history.get(1).toArray());
    }

    @Test
    public void check4Single() throws Exception {
        History<String> history = new History<String>(3);
        String target = new String("4");

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        history.add(new String("4"));
        assertEquals(target, history.getLast());
    }
    @Test
    public void check4ArrayFull() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("4"), new String("3"), new String("2") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        history.add(new String("4"));
        assertArrayEquals(target, history.get().toArray());
    }
    @Test
    public void check4ArrayZero() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("4"), new String("3"), new String("2") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        history.add(new String("4"));
        assertArrayEquals(target, history.get(0).toArray());
    }
    @Test
    public void check4ArrayTen() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("4"), new String("3"), new String("2") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        history.add(new String("4"));
        assertArrayEquals(target, history.get(10).toArray());
    }
    @Test
    public void check4ArrayTwo() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("4"), new String("3") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        history.add(new String("4"));
        assertArrayEquals(target, history.get(2).toArray());
    }
    @Test
    public void check4ArrayOne() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("4") };

        history.add(new String("1"));
        history.add(new String("2"));
        history.add(new String("3"));
        history.add(new String("4"));
        assertArrayEquals(target, history.get(1).toArray());
    }

    @Test
    public void check100Single() throws Exception {
        History<String> history = new History<String>(3);
        String target = new String("99");

        for (int i = 0; i < 100; i++) {
            history.add(new String(Integer.toString(i)));
        }
        assertEquals(target, history.getLast());
    }
    @Test
    public void check100ArrayFull() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("99"), new String("98"), new String("97"), };

        for (int i = 0; i < 100; i++) {
            history.add(new String(Integer.toString(i)));
        }
        assertArrayEquals(target, history.get().toArray());
    }
    @Test
    public void check100ArrayZero() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("99"), new String("98"), new String("97"), };

        for (int i = 0; i < 100; i++) {
            history.add(new String(Integer.toString(i)));
        }
        assertArrayEquals(target, history.get(0).toArray());
    }
    @Test
    public void check100ArrayTen() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("99"), new String("98"), new String("97"), };

        for (int i = 0; i < 100; i++) {
            history.add(new String(Integer.toString(i)));
        }
        assertArrayEquals(target, history.get(10).toArray());
    }
    @Test
    public void check100ArrayTwo() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("99"), new String("98") };

        for (int i = 0; i < 100; i++) {
            history.add(new String(Integer.toString(i)));
        }
        assertArrayEquals(target, history.get(2).toArray());
    }
    @Test
    public void check100ArrayOne() throws Exception {
        History<String> history = new History<String>(3);
        String[] target = new String[] { new String("99") };

        for (int i = 0; i < 100; i++) {
            history.add(new String(Integer.toString(i)));
        }

        assertArrayEquals(target, history.get(1).toArray());
    }
}
