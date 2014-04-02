package com.jbull.hermes.desktop;

import junit.framework.TestCase;

import java.util.Set;

public class RadixTrieTest extends TestCase {

    public void testInsertAndGet() throws Exception {
        RadixTrie trie = new RadixTrie();
        assertEquals(0, trie.getContact("").size());
        assertEquals(0, trie.getContact("a").size());

        Contact a = new Contact("", "a", null);
        Contact aa = new Contact("", "aa", null);
        trie.insertContact(a);
        trie.insertContact(aa);
        Set aResults = trie.getContact("a");
        assertEquals(2, aResults.size());
        assertTrue(aResults.contains(a));
        assertTrue(aResults.contains(aa));
        Set aaResults = trie.getContact("aa");
        assertEquals(1, aaResults.size());
        assertTrue(aaResults.contains(aa));
        assertEquals(0, trie.getContact("b").size());

        Contact b = new Contact("", "b", null);
        trie.insertContact(b);
        Set bResults = trie.getContact("b");
        assertEquals(1, bResults.size());
        assertEquals(2, trie.getContact("a").size());
        assertTrue(bResults.contains(b));

        Contact b_a = new Contact("", "b a", null);
        trie.insertContact(b_a);
        aResults = trie.getContact("a");
        bResults = trie.getContact("b");
        Set b_aResults = trie.getContact("b a");
        assertEquals(3, aResults.size());
        assertTrue(aResults.contains(b_a));
        assertEquals(2, bResults.size());
        assertTrue(bResults.contains(b_a));
        assertEquals(1, b_aResults.size());
        assertTrue(b_aResults.contains(b_a));

    }
}
