package com.github.mk23.jmxproxy.core.tests;

import com.github.mk23.jmxproxy.core.Attribute;
import com.github.mk23.jmxproxy.core.History;

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

    private Object[] getAttributeValues(Attribute[] attributes) {
        ArrayList<Object> al = new ArrayList<Object>(attributes.length);
        for (Attribute a : attributes) {
            al.add(a.getAttributeValue());
        }
        return al.toArray(new Object[al.size()]);
    }

    @Before
    public void printTestName() {
        System.out.println(" -> " + name.getMethodName());
    }

    @Test
    public void check0Single() throws Exception {
        History history = new History(3);
        assertNull(history.getAttribute());
    }
    @Test
    public void check0ArrayFull() throws Exception {
        History history = new History(3);
        assertTrue(history.getAttributes().length == 0);
    }
    @Test
    public void check0ArrayZero() throws Exception {
        History history = new History(3);
        assertTrue(history.getAttributes(0).length == 0);
    }
    @Test
    public void check0ArrayTen() throws Exception {
        History history = new History(3);
        assertTrue(history.getAttributes(10).length == 0);
    }
    @Test
    public void check0ArrayTwo() throws Exception {
        History history = new History(3);
        assertTrue(history.getAttributes(2).length == 0);
    }
    @Test
    public void check0ArrayOne() throws Exception {
        History history = new History(3);
        assertTrue(history.getAttributes(1).length == 0);
    }

    @Test
    public void check1Single() throws Exception {
        History history = new History(3);
        String target = new String("1");

        history.addAttributeValue(new String("1"));
        assertEquals(history.getAttribute().getAttributeValue(), target);
    }
    @Test
    public void check1ArrayFull() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("1") };

        history.addAttributeValue(new String("1"));
        assertArrayEquals(getAttributeValues(history.getAttributes()), target);
    }
    @Test
    public void check1ArrayZero() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("1") };

        history.addAttributeValue(new String("1"));
        assertArrayEquals(getAttributeValues(history.getAttributes(0)), target);
    }
    @Test
    public void check1ArrayTen() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("1") };

        history.addAttributeValue(new String("1"));
        assertArrayEquals(getAttributeValues(history.getAttributes(10)), target);
    }
    @Test
    public void check1ArrayTwo() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("1") };

        history.addAttributeValue(new String("1"));
        assertArrayEquals(getAttributeValues(history.getAttributes(2)), target);
    }
    @Test
    public void check1ArrayOne() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("1") };

        history.addAttributeValue(new String("1"));
        assertArrayEquals(getAttributeValues(history.getAttributes(1)), target);
    }

    @Test
    public void check2Single() throws Exception {
        History history = new History(3);
        String target = new String("2");

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        assertEquals(history.getAttribute().getAttributeValue(), target);
    }
    @Test
    public void check2ArrayFull() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("2"), new String("1") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        assertArrayEquals(getAttributeValues(history.getAttributes()), target);
    }
    @Test
    public void check2ArrayZero() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("2"), new String("1") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        assertArrayEquals(getAttributeValues(history.getAttributes(0)), target);
    }
    @Test
    public void check2ArrayTen() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("2"), new String("1") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        assertArrayEquals(getAttributeValues(history.getAttributes(10)), target);
    }
    @Test
    public void check2ArrayTwo() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("2"), new String("1") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        assertArrayEquals(getAttributeValues(history.getAttributes(2)), target);
    }
    @Test
    public void check2ArrayOne() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("2") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        assertArrayEquals(getAttributeValues(history.getAttributes(1)), target);
    }

    @Test
    public void check3Single() throws Exception {
        History history = new History(3);
        String target = new String("3");

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        assertEquals(history.getAttribute().getAttributeValue(), target);
    }
    @Test
    public void check3ArrayFull() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("3"), new String("2"), new String("1") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        assertArrayEquals(getAttributeValues(history.getAttributes()), target);
    }
    @Test
    public void check3ArrayZero() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("3"), new String("2"), new String("1") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        assertArrayEquals(getAttributeValues(history.getAttributes(0)), target);
    }
    @Test
    public void check3ArrayTen() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("3"), new String("2"), new String("1") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        assertArrayEquals(getAttributeValues(history.getAttributes(10)), target);
    }
    @Test
    public void check3ArrayTwo() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("3"), new String("2") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        assertArrayEquals(getAttributeValues(history.getAttributes(2)), target);
    }
    @Test
    public void check3ArrayOne() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("3") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        assertArrayEquals(getAttributeValues(history.getAttributes(1)), target);
    }

    @Test
    public void check4Single() throws Exception {
        History history = new History(3);
        String target = new String("4");

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        history.addAttributeValue(new String("4"));
        assertEquals(history.getAttribute().getAttributeValue(), target);
    }
    @Test
    public void check4ArrayFull() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("4"), new String("3"), new String("2") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        history.addAttributeValue(new String("4"));
        assertArrayEquals(getAttributeValues(history.getAttributes()), target);
    }
    @Test
    public void check4ArrayZero() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("4"), new String("3"), new String("2") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        history.addAttributeValue(new String("4"));
        assertArrayEquals(getAttributeValues(history.getAttributes(0)), target);
    }
    @Test
    public void check4ArrayTen() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("4"), new String("3"), new String("2") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        history.addAttributeValue(new String("4"));
        assertArrayEquals(getAttributeValues(history.getAttributes(10)), target);
    }
    @Test
    public void check4ArrayTwo() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("4"), new String("3") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        history.addAttributeValue(new String("4"));
        assertArrayEquals(getAttributeValues(history.getAttributes(2)), target);
    }
    @Test
    public void check4ArrayOne() throws Exception {
        History history = new History(3);
        String[] target = new String[] { new String("4") };

        history.addAttributeValue(new String("1"));
        history.addAttributeValue(new String("2"));
        history.addAttributeValue(new String("3"));
        history.addAttributeValue(new String("4"));
        assertArrayEquals(getAttributeValues(history.getAttributes(1)), target);
    }
}
