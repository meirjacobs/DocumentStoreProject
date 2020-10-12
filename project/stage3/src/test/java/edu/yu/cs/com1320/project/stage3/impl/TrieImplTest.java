package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;

public class TrieImplTest<Value> {
    private String a;
    private String b;
    private String c;
    private String d;
    private DocumentImpl document;
    private DocumentImpl document2;
    private DocumentImpl document3;
    private DocumentImpl document4;
    private DocumentImpl document5;
    private TrieImpl<Value> trie;

    @Test
    public void put() {

        TrieImpl trieImpl=new TrieImpl();
        Set<Value> set=new HashSet<>();
        set.add((Value) "hello");
        trieImpl.put("Hello", set);
        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        System.out.println(trieImpl.getAllSorted("Hello", comparator));
        assertEquals("test if put comes back",set,trieImpl.getAllSorted("hello",comparator).get(0));
    }

    @Before
    public void setUp() throws Exception {
        this.trie = new TrieImpl<>();
        this.a = "a";
        this.b = "b";
        this.c = "c";
        this.d = "d";
        String e = "e";
        String f = "f";
        String g = "g";
        String h = "h";
        String i = "i";
        String j = "j";
        String k = "k";
        String l = "l";
        URI uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        String txt1 = "This is the text of doc1, in plain text. No fancy file format - just plain old String A a A a";
        URI uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        String txt2 = "Text for doc2. A plain old String. toomba toocke Too";
        URI uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        String txt3 = "Text for doc2. A plain old String 2. A. toot  Toogle";
        URI uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
        String txt4 = "Text for doc2. A plain old String 3. A. a. ToObra Toomuch Toople Tooerd";
        URI uri5 = new URI("http://edu.yu.cs/com1320/project/doc5");
        String txt5 = "this text has a word with the prefixa: abracadabra";
        this.document = new DocumentImpl(uri1, txt1, txt1.hashCode());
        this.document2 = new DocumentImpl(uri2, txt2, txt2.hashCode());
        this.document3 = new DocumentImpl(uri3, txt3, txt3.hashCode());
        this.document4 = new DocumentImpl(uri4, txt4, txt4.hashCode());
        this.document5 = new DocumentImpl(uri5, txt5, txt5.hashCode());
    }

    @Test
    public void deleteAll() {
        trie.put(a, (Value) document);
        trie.put(a, (Value) document2);
        trie.put(a, (Value) document3);
        trie.put(a, (Value) document4);
        trie.put(a, (Value) document5);
        Set<Value> set = trie.deleteAll(a);
        System.out.println("\ndocument ref: " + document.toString());
        System.out.println("\ndocument2 ref: " + document2.toString());
        System.out.println("\ndocument3 ref: " + document3.toString());
        System.out.println("\ndocument4 ref: " + document4.toString());
        System.out.println("\ndocument5 ref: " + document5.toString());
        assertEquals(5, set.size());
        assertEquals(new ArrayList<Value>(), trie.getAllSorted((a), (Comparator<Value>) new NormalValuesComp(a)));

    }

    @Test
    public void delete() {
        trie.put(a, (Value) document);
        trie.put(a, (Value) document2);
        trie.put(a, (Value) document3);
        trie.put(a, (Value) document4);
        trie.put(a, (Value) document5);

        assertEquals(5, trie.getAllSorted((a), (Comparator<Value>) new NormalValuesComp(a)).size());
        trie.delete(a, (Value) document4);
        assertEquals(4, trie.getAllSorted((a), (Comparator<Value>) new NormalValuesComp(a)).size());
    }

    @Test
    public void put2() {
        trie.put(a, (Value) document);
        trie.put(b, (Value) document2);
        trie.put(c, (Value) document3);
        trie.put(d, (Value) document4);

        List<Value> list = trie.getAllSorted(a, (Comparator<Value>) new NormalValuesComp(a));
        assertEquals(1, list.size());
        assertTrue(list.contains(document));
        assertEquals(document, (DocumentImpl) list.get(0));
        assertEquals(document, list.get(0));

        trie.put(a, (Value) document4);
        list = trie.getAllSorted(a, (Comparator<Value>) new NormalValuesComp(a));
        assertEquals(2, list.size());
        assertTrue(list.contains(document));
        assertTrue(list.contains(document4));
    }

    @Test
    public void getAllSorted() {
        trie.put(a, (Value) document);
        trie.put(b, (Value) document2);
        trie.put(c, (Value) document3);
        trie.put(d, (Value) document4);

        List<Value> list = trie.getAllSorted(a, (Comparator<Value>) new NormalValuesComp(a));
        assertEquals(1, list.size());
        assertTrue(list.contains(document));
        assertEquals(document, (DocumentImpl) list.get(0));
        assertEquals(document, list.get(0));

        trie.put(a, (Value) document);
        trie.put(a, (Value) document2);
        trie.put(a, (Value) document3);
        trie.put(a, (Value) document4);
        list = trie.getAllSorted(a, (Comparator<Value>) new NormalValuesComp(a));
        assertEquals(4, list.size());
        assertTrue(document.getKeys().contains(a.toUpperCase()));
        assertTrue(document2.getKeys().contains(a.toUpperCase()));
        assertTrue(document3.getKeys().contains(a.toUpperCase()));
        assertTrue(document4.getKeys().contains(a.toUpperCase()));
        HashMap doc1 = document.getHashMap();
        System.out.println("document: " + doc1.get(a.toUpperCase()));
        HashMap doc2 = document2.getHashMap();
        System.out.println("document2: " + doc2.get(a.toUpperCase()));
        HashMap doc3 = document3.getHashMap();
        System.out.println("document3: " + doc3.get(a.toUpperCase()));
        HashMap doc4 = document4.getHashMap();
        System.out.println("document4: " + doc4.get(a.toUpperCase()));
        System.out.println("\ndocument ref: " + document.toString());
        System.out.println("\ndocument2 ref: " + document2.toString());
        System.out.println("\ndocument3 ref: " + document3.toString());
        System.out.println("\ndocument4 ref: " + document4.toString());

        System.out.println(Arrays.toString(list.toArray()));

        assertEquals(document, list.get(0));
        assertEquals(document4, list.get(1));
        assertEquals(document3, list.get(2));
        assertEquals(document2, list.get(3));
    }

    @Test
    public void getAllWithPrefixSorted() {
        trie.put(a, (Value) document);
        trie.put(a, (Value) document2);
        trie.put(a, (Value) document3);
        trie.put(a, (Value) document4);

        List<Value> list = trie.getAllWithPrefixSorted(a, (Comparator<Value>) new PrefixValuesComp("tOo"));
        assertEquals(4, list.size());
        assertTrue(document.prefixWords("tOo").isEmpty());
        assertFalse(document2.prefixWords("tOo").isEmpty());
        assertFalse(document3.prefixWords("tOo").isEmpty());
        assertFalse(document4.prefixWords("tOo").isEmpty());
        System.out.println(document.prefixWords("tOo").size());
        System.out.println(document2.prefixWords("tOo").size());
        System.out.println(document3.prefixWords("tOo").size());
        System.out.println(document4.prefixWords("tOo").size());

        System.out.println("\ndocument ref: " + document.toString());
        System.out.println("\ndocument2 ref: " + document2.toString());
        System.out.println("\ndocument3 ref: " + document3.toString());
        System.out.println("\ndocument4 ref: " + document4.toString());

        System.out.println("\n" + Arrays.toString(list.toArray()));
        assertEquals(document4, list.get(0));
        assertEquals(document2, list.get(1));
        assertEquals(document3, list.get(2));
        assertEquals(document, list.get(3));

    }

    @Test
    public void deleteAllWithPrefix() {
        trie.put(a, (Value) document);
        trie.put(a, (Value) document2);
        trie.put(a, (Value) document3);
        trie.put(a, (Value) document4);
        trie.put(a, (Value) document5);
        Set<Value> set = trie.deleteAllWithPrefix(a);
        System.out.println("\ndocument ref: " + document.toString());
        System.out.println("\ndocument2 ref: " + document2.toString());
        System.out.println("\ndocument3 ref: " + document3.toString());
        System.out.println("\ndocument4 ref: " + document4.toString());
        System.out.println("\ndocument5 ref: " + document5.toString());
        assertEquals(5, set.size());
        assertEquals(new ArrayList<Value>(), trie.getAllWithPrefixSorted((a), (Comparator<Value>) new PrefixValuesComp(a)));

    }

}

class NormalValuesComp implements Comparator<Document> {
    private String key;

    protected NormalValuesComp(String key) {
        this.key = key;
    }

    @Override
    public int compare(Document o1, Document o2) {
        return o2.wordCount(key) - o1.wordCount(key);
    }

}

class PrefixValuesComp implements Comparator<Document> {
    private String key;

    protected PrefixValuesComp(String key) {
        this.key = key;
    }

    @Override
    public int compare(Document o1, Document o2) {
        DocumentImpl doc1 = (DocumentImpl) o1;
        DocumentImpl doc2 = (DocumentImpl) o2;
        int doc1words = 0;
        for(String word: doc1.prefixWords(key)) {
            doc1words += doc1.wordCount(word);
        }

        int doc2words = 0;
        for(String word: doc2.prefixWords(key)) {
            doc2words += o2.wordCount(word);
        }

        return doc2words - doc1words;
    }

}