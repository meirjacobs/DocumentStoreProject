package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class MinHeapImplTest {

    //variables to hold possible values for doc1
    private URI uri1;
    private String txt1;
    private byte[] pdfData1;
    private String pdfTxt1;
    private DocumentImpl doc1;

    //variables to hold possible values for doc2
    private URI uri2;
    private String txt2;
    private byte[] pdfData2;
    private String pdfTxt2;
    private DocumentImpl doc2;

    private URI uri3;
    private String txt3;
    private byte[] pdfData3;
    private String pdfTxt3;

    private URI uri4;
    private String txt4;
    private byte[] pdfData4;
    private String pdfTxt4;

    private DocumentImpl document1;
    private DocumentImpl document2;
    private DocumentImpl document3;
    private DocumentImpl document4;

    @Before
    public void init() throws Exception {
        //init possible values for doc1
        this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        this.txt1 = "This is the text of doc1, in plain text. No fancy file format - just plain old String";
        this.pdfTxt1 = "This is some PDF text for doc1, hat tip to Adobe.";
        this.pdfData1 = Utils.textToPdfData(this.pdfTxt1);
        document1 = new DocumentImpl(uri1, txt1, txt1.hashCode(), pdfData1);

        //init possible values for doc2
        this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txt2 = "Text for doc2. A plain old String.";
        this.pdfTxt2 = "PDF content for doc2: PDF format was opened in 2008.";
        this.pdfData2 = Utils.textToPdfData(this.pdfTxt2);
        document2 = new DocumentImpl(uri2, txt2, txt2.hashCode(), pdfData2);

        this.uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        this.txt3 = "Text for doc3. A plain old String.";
        this.pdfTxt3 = "PDF content for doc4: PDF format was opened in 2008.";
        this.pdfData3 = Utils.textToPdfData(this.pdfTxt3);
        document3 = new DocumentImpl(uri3, txt3, txt3.hashCode(), pdfData3);

        this.uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
        this.txt4 = "Text for doc4. A plain old String.";
        this.pdfTxt4 = "PDF content for doc4: PDF format was opened in 2008.";
        this.pdfData4 = Utils.textToPdfData(this.pdfTxt4);
        document4 = new DocumentImpl(uri1, txt4, txt4.hashCode(), pdfData4);
    }

    @Test
    public void reHeapify() {
        MinHeapImpl heap = new MinHeapImpl<>();
        document1.setLastUseTime(System.nanoTime());
        document2.setLastUseTime(System.nanoTime());
        document3.setLastUseTime(System.nanoTime());
        document4.setLastUseTime(System.nanoTime());

        heap.insert(document1);
        heap.insert(document2);
        heap.insert(document3);
        heap.insert(document4);

        assertEquals(1, heap.getArrayIndex(document1));
        assertEquals(2, heap.getArrayIndex(document2));
        document1.setLastUseTime(System.nanoTime());
        heap.reHeapify(document1);
        assertEquals(4, heap.getArrayIndex(document1));
        assertEquals(1, heap.getArrayIndex(document2));
        document1.setLastUseTime(System.nanoTime());
        heap.reHeapify(document1);
        assertEquals(4, heap.getArrayIndex(document1));
        assertEquals(1, heap.getArrayIndex(document2));
    }

    @Test
    public void getArrayIndex() {
    }

    @Test
    public void doubleArraySize() {
        MinHeapImpl heap = new MinHeapImpl<>();
        heap.insert(1);
        heap.insert(11);
        heap.insert(12);
        heap.insert(13);
        heap.insert(14);
        heap.insert(15);
        heap.insert(16);
        heap.insert(17);
        heap.insert(18);
        heap.insert(19);
        heap.insert(10);
        heap.insert(20);
        assertEquals(12, heap.getArrayIndex(20));
    }

    @Test
    public void swap() {
        MinHeapImpl heap = new MinHeapImpl<>();
        heap.insert(2);
        heap.insert(1);
        assertEquals(1, heap.getArrayIndex(1));
        heap.swap(heap.getArrayIndex(1), heap.getArrayIndex(2));
        assertEquals(1, heap.getArrayIndex(2));
        assertEquals(2, heap.getArrayIndex(1));


    }

    @Test
    public void insert() {
        MinHeapImpl heap = new MinHeapImpl<>();
        heap.insert(document1);
        assertEquals(1, heap.getArrayIndex(document1));
        assertEquals(document1, heap.removeMin());
        assertEquals(-1, heap.getArrayIndex(document1));

        heap.insert(document1);
        int i = 1 + 1;
        heap.insert(document2);
        i = 1 + 1;
        heap.insert(document3);
        i = 1 + 1;
        heap.insert(document4);

        System.out.println("doc1 index: " + heap.getArrayIndex(document1));
        System.out.println("doc2 index: " + heap.getArrayIndex(document2));
        System.out.println("doc3 index: " + heap.getArrayIndex(document3));
        System.out.println("doc4 index: " + heap.getArrayIndex(document4));
        System.out.println("doc1 last use time " + document1.getLastUseTime());
        System.out.println("doc2 last use time " + document2.getLastUseTime());
        System.out.println("doc3 last use time " + document3.getLastUseTime());
        System.out.println("doc4 last use time " + document4.getLastUseTime());


        heap.removeMin();
        //System.out.println("\ndoc1 index: " + heap.getArrayIndex(document1));
        System.out.println("\ndoc2 index: " + heap.getArrayIndex(document2));
        System.out.println("doc3 index: " + heap.getArrayIndex(document3));
        System.out.println("doc4 index: " + heap.getArrayIndex(document4));
        //System.out.println("doc1 last use time " + document1.getLastUseTime());
        System.out.println("doc2 last use time " + document2.getLastUseTime());
        System.out.println("doc3 last use time " + document3.getLastUseTime());
        System.out.println("doc4 last use time " + document4.getLastUseTime());

        heap.insert(document1);

        System.out.println("\ndoc1 index: " + heap.getArrayIndex(document1));
        System.out.println("doc2 index: " + heap.getArrayIndex(document2));
        System.out.println("doc3 index: " + heap.getArrayIndex(document3));
        System.out.println("doc4 index: " + heap.getArrayIndex(document4));
        System.out.println("doc1 last use time " + document1.getLastUseTime());
        System.out.println("doc2 last use time " + document2.getLastUseTime());
        System.out.println("doc3 last use time " + document3.getLastUseTime());
        System.out.println("doc4 last use time " + document4.getLastUseTime());

    }

    @Test
    public void insert2() {
        MinHeapImpl heap = new MinHeapImpl<>();
        heap.insert((Integer) 3);
        heap.insert((Integer) 1);
        heap.insert((Integer) 4);
        heap.insert((Integer) 2);

        System.out.println("\n1 index: " + heap.getArrayIndex(1));
        System.out.println("2 index: " + heap.getArrayIndex(2));
        System.out.println("3 index: " + heap.getArrayIndex(3));
        System.out.println("4 index: " + heap.getArrayIndex(4));

        assertEquals(1, heap.removeMin());
        System.out.println("\n2 index: " + heap.getArrayIndex(2));
        System.out.println("3 index: " + heap.getArrayIndex(3));
        System.out.println("4 index: " + heap.getArrayIndex(4));

        assertEquals(1, heap.getArrayIndex(2));

        heap.insert((Integer) 1);

        System.out.println("\n1 index: " + heap.getArrayIndex(1));
        System.out.println("2 index: " + heap.getArrayIndex(2));
        System.out.println("3 index: " + heap.getArrayIndex(3));
        System.out.println("4 index: " + heap.getArrayIndex(4));
    }

    @Test
    public void removeMin() {
        MinHeapImpl heap = new MinHeapImpl<>();
        heap.insert(11);
        heap.insert(111);
        heap.insert(12);
        heap.insert(13);
        heap.insert(14);
        heap.insert(15);
        heap.insert(16);
        heap.insert(17);
        heap.insert(18);
        heap.insert(19);
        heap.insert(1);
        heap.insert(20);
        heap.removeMin();
        assertEquals(-1, heap.getArrayIndex(1));
    }
}