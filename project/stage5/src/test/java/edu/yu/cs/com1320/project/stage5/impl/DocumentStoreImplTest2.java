package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;

import static org.junit.Assert.*;

public class DocumentStoreImplTest2 {

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

    @Before
    public void init() throws Exception {
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

        this.uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        this.txt3 = "Text for doc3. A plain old String.";
        this.pdfTxt3 = "PDF content for doc3: PDF format was opened in 2008.";
        this.pdfData3 = Utils.textToPdfData(this.pdfTxt3);

        this.uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
        this.txt4 = "Text for doc4. A plain old String.";
        this.pdfTxt4 = "PDF content for doc4: PDF format was opened in 2008.";
        this.pdfData4 = Utils.textToPdfData(this.pdfTxt4);
    }

    @After
    public void deleteAllDocs() {
        String base = System.getProperty("user.dir");
        base += File.separator + "edu.yu.cs\\com1320\\project";
        new File(base+"\\doc1.json").delete();
        new File(base+"\\doc2.json").delete();
        new File(base+"\\doc3.json").delete();
        new File(base+"\\doc4.json").delete();
    }

    @Test
    public void putDocument() {
        StackImpl<Undoable> tempStack = new StackImpl();
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        System.out.println("put 2");

        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        System.out.println("put 3");
        store.putDocument(new ByteArrayInputStream(this.pdfData3),this.uri3, DocumentStore.DocumentFormat.PDF);
        System.out.println("put 4");
        store.putDocument(new ByteArrayInputStream(this.pdfData4),this.uri4, DocumentStore.DocumentFormat.PDF);
        store.setMaxDocumentCount(3);
        assertNull(store.getDocument(uri1));
        /*int count = 0;
        /*while(store.commandStack.peek() != null) {
            GenericCommand command = (GenericCommand) store.commandStack.pop();
            System.out.println(count + ": " + command.getTarget());
            count++;
            tempStack.push(command);
        }
        while(tempStack.peek() != null) {
            store.commandStack.push(tempStack.pop());
        }
        System.out.println("document count: " + store.currentDocumentCount);
        System.out.println("1");

        store.setMaxDocumentCount(3);
        count = 0;
        /*while(store.commandStack.peek() != null) {
            GenericCommand command = (GenericCommand) store.commandStack.pop();
            System.out.println(count + ": " + command.getTarget());
            count++;
            tempStack.push(command);
        }
        while(tempStack.peek() != null) {
            store.commandStack.push(tempStack.pop());
        }
        System.out.println("document count: " + store.currentDocumentCount);
        System.out.println("2");
        assertNull(store.getDocument(uri1));
        assertNull(store.getDocument(uri1));
        assertNotNull(store.getDocument(uri2));
        assertNotNull(store.getDocument(uri3));
        assertNotNull(store.getDocument(uri4));

        store.undo();
        assertNull(store.getDocument(uri2));
        count = 0;
        /*while(store.commandStack.peek() != null) {
            GenericCommand command = (GenericCommand) store.commandStack.pop();
            System.out.println(count + ": " + command.getTarget());
            count++;
            tempStack.push(command);
        }
        while(tempStack.peek() != null) {
            store.commandStack.push(tempStack.pop());
        }
        System.out.println("document count: " + store.currentDocumentCount);
        /*System.out.println("3");
        store.putDocument(new ByteArrayInputStream(this.pdfData4),this.uri4, DocumentStore.DocumentFormat.PDF);
        count = 0;
        /*while(store.commandStack.peek() != null) {
            GenericCommand command = (GenericCommand) store.commandStack.pop();
            System.out.println(count + ": " + command.getTarget());
            count++;
            tempStack.push(command);
        }
        while(tempStack.peek() != null) {
            store.commandStack.push(tempStack.pop());
        }
        System.out.println("document count: " + store.currentDocumentCount);*/
        /*System.out.println("3.5");
        store.deleteDocument(uri4);*/

        /*count = 0;
        while(store.commandStack.peek() != null) {
            GenericCommand command = (GenericCommand) store.commandStack.pop();
            System.out.println(count + ": " + command.getTarget());
            count++;
            tempStack.push(command);
        }
        while(tempStack.peek() != null) {
            store.commandStack.push(tempStack.pop());
        }
        System.out.println("document count: " + store.currentDocumentCount);*/
        /*System.out.println("4");
        store.undo(uri3);
        count = 0;*/
        /*while(store.commandStack.peek() != null) {
            GenericCommand command = (GenericCommand) store.commandStack.pop();
            System.out.println(count + ": " + command.getTarget());
            count++;
            tempStack.push(command);
        }
        while(tempStack.peek() != null) {
            store.commandStack.push(tempStack.pop());
        }
        System.out.println("document count: " + store.currentDocumentCount);
        System.out.println("5");
        count = 0;
        /*while(store.commandStack.peek() != null) {
            GenericCommand command = (GenericCommand) store.commandStack.pop();
            System.out.println(count + ": " + command.getTarget());
            count++;
            tempStack.push(command);
        }
        while(tempStack.peek() != null) {
            store.commandStack.push(tempStack.pop());
        }
        System.out.println("document count: " + store.currentDocumentCount);*/
        /*System.out.println(store.getDocumentAsTxt(uri3));
        assertNotNull(store.getDocumentAsTxt(uri2));
        assertNull(store.getDocumentAsTxt(uri1));
        assertNull(store.getDocumentAsTxt(uri4));
        assertNull(store.getDocumentAsTxt(uri3));*/
    }

    @Test
    public void deleteDocument() {
    }

    @Test
    public void undo() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3),this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4),this.uri4, DocumentStore.DocumentFormat.PDF);
        store.undo(uri3);
        assertNull(store.getDocumentAsTxt(uri3));
    }

    @Test
    public void heapTest() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        System.out.println("humpity");
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        System.out.println("humpity");
        store.putDocument(new ByteArrayInputStream(this.pdfData3),this.uri3, DocumentStore.DocumentFormat.PDF);

        store.setMaxDocumentCount(2);
        assertNull(store.getDocument(uri1));
    }

    @Test
    public void duplicateDoc() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3),this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4),this.uri4, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);



        assertNotNull(store.getDocument(uri2));
        assertNotNull(store.getDocument(uri1));
        assertNotNull(store.getDocument(uri4));
        assertNotNull(store.getDocument(uri3));

        store.setMaxDocumentCount(3);
        if (store.getDocument(uri2) == null) {
            System.out.println("2 is null");
        }
        assertNull(store.getDocument(uri2));
        assertNotNull(store.getDocument(uri1));
        assertNotNull(store.getDocument(uri4));
        assertNotNull(store.getDocument(uri3));

        store.deleteDocument(uri2);
        assertNull(store.getDocument(uri2));
    }

    @Test
    public void testUndo() {
    }

    @Test
    public void deleteAll() {
    }

    @Test
    public void deleteAllWithPrefix() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3),this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4),this.uri4, DocumentStore.DocumentFormat.PDF);
        store.deleteAllWithPrefix("2008");
        store.undo(uri3);
        assertNotNull(store.getDocumentAsTxt(uri3));
        assertNotNull(store.getDocumentAsTxt(uri1));
        assertNull(store.getDocumentAsTxt(uri2));
        assertNull(store.getDocumentAsTxt(uri4));
    }

    @Test
    public void setMax() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3),this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4),this.uri4, DocumentStore.DocumentFormat.PDF);
        assertEquals(pdfTxt1, store.getDocumentAsTxt(uri1));

        store.setMaxDocumentCount(3);
        assertNotNull(store.getDocumentAsTxt(uri1));
        assertNotNull(store.getDocumentAsPdf(uri2));
        store.undo(uri2);
        assertNull(store.getDocumentAsPdf(uri2));
    }
    @Test
    public void serialize() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);

        store.setMaxDocumentCount(1);
        assertNull(store.getDocument(uri1));
        store.setMaxDocumentCount(2);
        String text = store.getDocumentAsTxt(uri1);
        assertEquals(text, this.pdfTxt1);
        byte[] bytes = store.getDocumentAsPdf(uri1);
        //assertEquals(bytes, pdfData1);
    }


    @Test
    public void testHeapAfterSearch()
    {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String uriS="uri";
        String stam="hello";
        String push="hellooooo";
        String words="hi th%ere im here cool";
        String moreWords="hi there yes i too am here hi";
        String extraWords="totally a good time";
        URI uri =URI.create(uriS);
        URI uriTwo=URI.create(stam);
        URI uriThree=uri.create(push);
        InputStream inputStream = new ByteArrayInputStream(words.getBytes());
        InputStream inputStreamTwo = new ByteArrayInputStream(moreWords.getBytes());
        InputStream inputStreamThree=new ByteArrayInputStream(extraWords.getBytes());

        documentStore.putDocument(inputStream,uri, DocumentStore.DocumentFormat.TXT);
        documentStore.putDocument(inputStreamTwo,uriTwo, DocumentStore.DocumentFormat.TXT);
        documentStore.putDocument(inputStreamThree,uriThree, DocumentStore.DocumentFormat.TXT);

        DocumentImpl doc = (DocumentImpl) documentStore.getDocument(uri);
        DocumentImpl doc2 = (DocumentImpl) documentStore.getDocument(uriTwo);
        DocumentImpl doc3 = (DocumentImpl) documentStore.getDocument(uriThree);

        documentStore.searchPDFs("hi");

        //assertEquals("test heap after search", doc3,documentStore.heap.removeMin());


    }

    @Test
    public void stage5Undo() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3),this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4),this.uri4, DocumentStore.DocumentFormat.PDF);

        store.setMaxDocumentCount(3);
        assertNull(store.getDocument(uri1));
    }

    @Test
    public void stage5Undo1() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        //store.putDocument(new ByteArrayInputStream(this.pdfData3),this.uri3, DocumentStore.DocumentFormat.PDF);
        //store.putDocument(new ByteArrayInputStream(this.pdfData4),this.uri4, DocumentStore.DocumentFormat.PDF);

        store.setMaxDocumentCount(1);

        store.undo();

        assertNull(store.getDocument(uri1));
        store.setMaxDocumentCount(2);
        System.out.println("1");
        assertNotNull(store.getDocumentAsTxt(uri1));
        store.deleteDocument(uri1);
        assertNull(store.getDocumentAsPdf(uri1));

    }

}