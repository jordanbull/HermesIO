package com.jbull.hermes.desktop;


import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class RadixTrie {
    Node root = new Node('\0');

    public Set<Contact> getContact(String prefix) {
        return getContact(prefix.toLowerCase(), root);
    }

    private Set<Contact> getContact(String prefix, Node n) {
        if (prefix.length() == 0) {
            return n.leaves;
        }
        char c = prefix.charAt(0);
        prefix = prefix.substring(1);
        if (n.children.containsKey(c)) {
            return getContact(prefix, n.children.get(c));
        }
        return new LinkedHashSet<Contact>(0);
    }

    public void insertContact(Contact contact) {
        String fullName = contact.getDisplayName().toLowerCase();
        String[] words = fullName.split(" ");
        String number = contact.getPhoneNumber();
        insertContact(fullName, root, contact);
        if (!fullName.equals(number.toLowerCase()))
            insertContact(number, root, contact);
        if (words.length > 1)
            for (String word : words) {
                insertContact(word, root, contact);
            }
    }

    private void insertContact(String word, Node n, Contact contact) {
        if (word.length() == 0)
            return;
        char c = word.charAt(0);
        word = word.substring(1);
        if (!n.children.containsKey(c)) {
            Node child = new Node(c);
            n.children.put(c, child);
        }
        Node child = n.children.get(c);
        child.leaves.add(contact);
        insertContact(word, child, contact);
    }


    private static class Node {
        public char myChar;
        public Set<Contact> leaves = new LinkedHashSet<Contact>();
        public HashMap<Character, Node> children = new HashMap<Character, Node>();


        public Node(char c) {
            myChar = c;
        }
    }

}
