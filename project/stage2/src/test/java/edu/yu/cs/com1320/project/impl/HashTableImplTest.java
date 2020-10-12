package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.impl.DocumentStoreImpl;
import org.junit.Before;
import org.junit.Test;
import edu.yu.cs.com1320.project.impl.HashTableImpl;

import java.net.URI;
import java.util.Arrays;

import static org.junit.Assert.*;

public class HashTableImplTest {

    @Test
    public void arrayDoubling() {
        HashTableImpl<Object, Object> hashTable = new HashTableImpl<>();

        hashTable.put("one", 1);
        hashTable.put("two", 2);
        hashTable.put("three", 3);

        System.out.println(Arrays.toString(hashTable.array));
        int arraySlot = 1;
        for (LinkedList list : hashTable.array) {
            System.out.println("\nArray slot " + arraySlot);
            LinkedListElement current = new LinkedListElement(null, null);
            if(list == null || list.head == null) {
                arraySlot++;
                continue;
            }
            current = list.head;
            int listSlot = 1;
            while(current != null) {
                System.out.println("\n      List slot " + listSlot + " value: " + (Integer) current.value);
                current = current.next;
                listSlot++;
            }
            arraySlot++;
        }

        System.out.println("\nvalue of \"two\" = " + hashTable.get("two"));

        System.out.println("\n\n----------------------------------------------");

        hashTable.put("four", 4);
        System.out.println("successfully put");

        System.out.println(Arrays.toString(hashTable.array));
        arraySlot = 1;
        for (LinkedList list : hashTable.array) {
            System.out.println("\nArray slot " + arraySlot);
            LinkedListElement current = new LinkedListElement(null, null);
            if(list == null || list.head == null) {
                arraySlot++;
                continue;
            }
            current = list.head;
            int listSlot = 1;
            while(current != null) {
                System.out.println("\n      List slot " + listSlot + " value: " + (Integer) current.value);
                current = current.next;
                listSlot++;
            }
            arraySlot++;
        }

        System.out.println("\n\n----------------------------------------------");

        hashTable.put("five", 5);
        hashTable.put("six", 6);
        hashTable.put("seven", 7);
        hashTable.put("eight", 8);
        hashTable.put("nine", 9);
        hashTable.put("ten", 10);
        hashTable.put("eleven", 11);
        hashTable.put("twelve", 12);
        hashTable.put("thirteen", 13);
        hashTable.put("fourteen", 14);
        hashTable.put("one", 15);

        System.out.println(Arrays.toString(hashTable.array));
        arraySlot = 1;
        for (LinkedList list : hashTable.array) {
            System.out.println("\nArray slot " + arraySlot);
            LinkedListElement current = new LinkedListElement(null, null);
            if(list == null || list.head == null) {
                arraySlot++;
                continue;
            }
            current = list.head;
            int listSlot = 1;
            while(current != null) {
                System.out.println("\n      List slot " + listSlot + " value: " + (Integer) current.value);
                current = current.next;
                listSlot++;
            }
            arraySlot++;
        }

        assertEquals(15, hashTable.get("one"));
        assertEquals(2, hashTable.get("two"));



    }

    /*@Before
    public void docinit() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        //init possible values for doc1
        this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        this.txt1 = "This is the text of doc1, in plain text. No fancy file format - just plain old String";
        this.pdfTxt1 = "This is some PDF text for doc1, hat tip to Adobe.";
        this.pdfData1 = Utils.textToPdfData(this.pdfTxt1);

        //init possible values for doc2
        this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txt2 = "Text for doc2. A plain old String.";
        this.pdfTxt2 = "PDF content for doc2: PDF format was opened in 2008.";
        this.pdfData2 = Utils.textToPdfData(this.pdfTxt2);
    }*/

    /*@Test
    public void stackTest() {
        StackImpl stackImpl = new StackImpl();
        stackImpl.push();
    }*/

}