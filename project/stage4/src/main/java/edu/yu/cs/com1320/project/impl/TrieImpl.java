package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {

    private static final int upperAlphaNumeric = 91; // extended ASCII
    private Node root;
    private Comparator<Value> comparator;
    private Set<Value> latestDeleteAllSet;
    private Set<Value> latestPrefixSet;
    private Set<Value> latestDeleteAllWithPrefix;
    private Set<String> prefixWords;
    private boolean deleted;

    public static class Node<Value> {
        protected Set<Value> val;
        protected Node[] links = new Node[upperAlphaNumeric];

        public Node() {
            this.val = new HashSet<>();
        }
    }

    public TrieImpl() {
        root = new Node<Value>();
    }

    @Override
    public void put(String key, Value val) {
        if(val == null || key == null) {
            return;
        }
        else {
            this.root = put(this.root, key.toUpperCase(), val, 0);
        }
    }

    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        if(key == null || comparator == null) {
            //throw new IllegalArgumentException("Cannot have null key or null comparator");
            return new ArrayList<Value>();
        }
        Node x = this.get(this.root, key.toUpperCase(), 0);
        if (x == null) {
            return new ArrayList<Value>();
        }
        List<Value> list = new ArrayList<>();
        list.addAll(x.val);
        Collections.sort(list, comparator);
        return list;
    }

    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if(prefix == null || comparator == null) {
            //throw new IllegalArgumentException("Cannot have null prefix or null comparator");
            return new ArrayList<Value>();
        }
        this.latestPrefixSet = new HashSet<>();
        List<Value> list = new ArrayList<>();
        Set<Value> set = prefixSortingSet(this.root, prefix.toUpperCase(), 0);
        if(set == null || set.isEmpty()) {
            return list;
        }
        list.addAll(set);
        latestPrefixSet.clear();
        Collections.sort(list, comparator);
        return list;
    }

    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        if(prefix == null) {
            //throw new IllegalArgumentException("Cannot have null prefix");
            return new HashSet<Value>();
        }
        this.latestDeleteAllWithPrefix = new HashSet<>();
        deleteAllWithPrefixHelper(this.root, prefix.toUpperCase(), 0);
        Set<Value> set = latestDeleteAllWithPrefix;
        //latestDeleteAllWithPrefix.clear();
        return set;
    }

    @Override
    public Set<Value> deleteAll(String key) {
        if(key == null) {
            //throw new IllegalArgumentException("Cannot have null key");
            return new HashSet<Value>();
        }
        this.latestDeleteAllSet = new HashSet<>();
        deleteAll(this.root, key.toUpperCase(), 0);
        Set<Value> temp = latestDeleteAllSet;
        //latestDeleteAllSet.clear();
        return temp;
    }

    @Override
    public Value delete(String key, Value val) {
        if(key == null || val == null) {
            //throw new IllegalArgumentException("Cannot have null key or null value");
            return null;
        }
        Node x = delete(this.root, key.toUpperCase(), val,0);
        return (x == null) ? null : val;
    }

    private Node put(Node x, String key, Value val, int d)
    {
        //create a new node
        if (x == null)
        {
            x = new Node();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length())
        {
            x.val.add(val);
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    private Node get(Node x, String key, int d)
    {
        //link was null - return null, indicating a miss
        if (x == null)
        {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length())
        {
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        return this.get(x.links[c], key, d + 1);
    }

    private Node deleteAll(Node x, String key, int d)
    {
        //Set<Value> set = new HashSet<>();
        if (x == null)
        {
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length())
        {
            latestDeleteAllSet.addAll(x.val);
            x.val.clear();
        }
        //continue down the trie to the target node
        else
        {
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1);
        }
        //this node has a val – do nothing, return the node
        if (!x.val.isEmpty())
        {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty
        for (int c = 0; c < upperAlphaNumeric; c++)
        {
            if (x.links[c] != null)
            {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

    private Node delete(Node x, String key, Value val, int d) {
        if(x == null) {
            deleted = false;
            return null;
        }
        if(d == key.length()) {
            deleted = x.val.remove(val);
        }
        else {
            char c = key.charAt(d);
            x.links[c] = this.delete(x.links[c], key, val, d + 1);
        }
        if(!x.val.isEmpty()) {
            return x;
        }
        for (int c = 0; c < upperAlphaNumeric; c++) {
            if (x.links[c] != null) {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

    private Set<Value> prefixSortingSet(Node x, String prefix, int d) {
        latestPrefixSet = new HashSet<Value>();
        if(x == null) {
            return null;
        }
        if(d == prefix.length()) {
            return addPrefixMatches(x);
        }
        else {
            char c = prefix.charAt(d);
            return prefixSortingSet(x.links[c], prefix, d + 1);
        }
    }

    private Set<Value> addPrefixMatches(Node x) {
        if(x == null) {
            throw new IllegalStateException("MEIR YOU DID SOMETHING WRONG");
        }
        if(x.val != null && !x.val.isEmpty()) {
            latestPrefixSet.addAll(x.val);
        }
        for(int c = 0; c < upperAlphaNumeric; c++) {
            if(x.links[c] != null) {
                addPrefixMatches(x.links[c]);
            }
        }
        return latestPrefixSet;
    }

    private Node deleteAllWithPrefixHelper(Node x, String prefix, int d) {
        if(x == null) {
            return null;
        }
        if(d == prefix.length()) {
            return deletePrefixSubtree(x);
        }
        else {
            char c = prefix.charAt(d);
            return this.deleteAllWithPrefixHelper(x.links[c], prefix, d + 1);
        }
    }

    private Node deletePrefixSubtree(Node x) {
        if(x == null) {
            return null;
        }
        if(x.val != null && !x.val.isEmpty()) {
            latestDeleteAllWithPrefix.addAll(x.val);
            x.val.clear();
        }
        for(int c = 0; c < upperAlphaNumeric; c++) {
            if(x.links[c] != null) {
                deletePrefixSubtree(x.links[c]);
            }
        }
        x = null;
        return null;
    }

}