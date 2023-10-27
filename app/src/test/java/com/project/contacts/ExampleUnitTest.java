package com.project.contacts;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void testAreNormalSearchResultsCorrect() {
        int[] res = new Searcher(getContactsList()).search("a");
        assertArrayEquals(new int[]{0, 1}, res);
    }

    @Test
    public void testAreEmptySearchResultsCorrect() {
        int[] res = new Searcher(getContactsList()).search("c");
        assertArrayEquals(null, res);
    }

    public ArrayList<Contact> getContactsList() {
        ArrayList<Contact> arr = new ArrayList<>();
        Contact c = new Contact("aaa", null, null, null);
        arr.add(c);
        c = new Contact("abc", null, null, null);
        arr.add(c);
        c = new Contact("bbb", null, null, null);
        arr.add(c);
        return arr;
    }
}