package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

public class DocumentStoreImplTest1 {

    @Test
    public void putAndGet() {
        BTreeImpl<String, Object> ht = new BTreeImpl<>();
        String meir = "meir";
        String jacobs = "jacobs";
        ht.put(meir, jacobs);
        Object result = ht.get(meir);
        assertEquals(jacobs, result);

        ht.put(meir, "blub");
        result = ht.get(meir);
        assertEquals("blub", result);

        /*ht.put((Integer) 3, "upshurin");
        Object j = new int[6];
        ht.put(j, "5, 6, 7");
        result = ht.get(j);
        assertEquals("5, 6, 7", result);*/

        ht.put("a", "b");
        ht.put("c", "d");
        ht.put("e", "f");
        ht.put("g", "h");
        ht.put("i", "j");
        ht.put("k", "l");
        ht.put("m", "n");

        assertEquals("b", ht.get("a"));
        assertEquals("d", ht.get("c"));
        assertEquals("f", ht.get("e"));
        assertEquals("h", ht.get("g"));
        assertEquals("j", ht.get("i"));
        assertEquals("l", ht.get("k"));
        assertEquals("n", ht.get("m"));

    }

    @Test
    public void putAndGet2() {
        BTreeImpl<Integer, Object> ht = new BTreeImpl<>();
        PDDocument pdfDoc = new PDDocument();
        PDPage firstPage = new PDPage();
        pdfDoc.addPage(firstPage);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PDPageContentStream cs = new PDPageContentStream(pdfDoc, firstPage);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.showText("I like my home. The house itself isn’t very impressive: it’s pretty standard, maybe a little small in comparison to the rest of the block, but that doesn’t matter to me; I’m not the materialistic type.");
            cs.newLine();
            cs.endText();
            cs.close();
            pdfDoc.save(outputStream);
            pdfDoc.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        outputStream.toByteArray();
        ht.put(123, pdfDoc);
    }

    @Test
    public void theBigOne() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();

        File file = File.createTempFile("coolfile", ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("oftheblock");
        writer.close();

        InputStream targetStream = new FileInputStream(file);
        Scanner scanner = new Scanner(file);
        String text = scanner.nextLine();
        URI uri = new URI(text);
        int tip0 = store.putDocument(targetStream, uri, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yes = store.getDocumentAsTxt(uri);
        assertEquals("oftheblock", yes);
        System.out.println(yes);


        File file1 = File.createTempFile("coolfile1", ".txt");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(file1));
        writer1.write("oftheblock1");
        writer1.close();

        InputStream targetStream1 = new FileInputStream(file1);
        Scanner scanner1 = new Scanner(file1);
        String text1 = scanner1.nextLine();
        URI uri1 = new URI(text1);
        store.putDocument(targetStream1, uri1, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yes1 = store.getDocumentAsTxt(uri1);
        assertEquals("oftheblock1", yes1);
        System.out.println(yes1);


        File filetwo = File.createTempFile("coolfiletwo", ".txt");
        BufferedWriter writertwo = new BufferedWriter(new FileWriter(filetwo));
        writertwo.write("oftheblocktwo");
        writertwo.close();

        InputStream targetStreamtwo = new FileInputStream(filetwo);
        Scanner scannertwo = new Scanner(filetwo);
        String texttwo = scannertwo.nextLine();
        URI uritwo = new URI(texttwo);
        store.putDocument(targetStreamtwo, uritwo, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yestwo = store.getDocumentAsTxt(uritwo);
        assertEquals("oftheblocktwo", yestwo);
        System.out.println(yestwo);


        File filethree = File.createTempFile("coolfilethree", ".txt");
        BufferedWriter writerthree = new BufferedWriter(new FileWriter(filethree));
        writerthree.write("oftheblockthree");
        writerthree.close();

        InputStream targetStreamthree = new FileInputStream(filethree);
        Scanner scannerthree = new Scanner(filethree);
        String textthree = scannerthree.nextLine();
        URI urithree = new URI(textthree);
        store.putDocument(targetStreamthree, urithree, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yesthree = store.getDocumentAsTxt(urithree);
        assertEquals("oftheblockthree", yesthree);
        System.out.println(yesthree);

        File filefour = File.createTempFile("coolfilefour", ".txt");
        BufferedWriter writerfour = new BufferedWriter(new FileWriter(filefour));
        writerfour.write("oftheblockfour");
        writerfour.close();

        InputStream targetStreamfour = new FileInputStream(filefour);
        Scanner scannerfour = new Scanner(filefour);
        String textfour = scannerfour.nextLine();
        URI urifour = new URI(textfour);
        store.putDocument(targetStreamfour, urifour, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yesfour = store.getDocumentAsTxt(urifour);
        assertEquals("oftheblockfour", yesfour);
        System.out.println(yesfour);


        File filefive = File.createTempFile("coolfilefive", ".txt");
        BufferedWriter writerfive = new BufferedWriter(new FileWriter(filefive));
        writerfive.write("oftheblockfive");
        writerfive.close();

        InputStream targetStreamfive = new FileInputStream(filefive);
        Scanner scannerfive = new Scanner(filefive);
        String textfive = scannerfive.nextLine();
        URI urifive = new URI(textfive);
        store.putDocument(targetStreamfive, urifive, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yesfive = store.getDocumentAsTxt(urifive);
        assertEquals("oftheblockfive", yesfive);
        System.out.println(yesfive);


        File filesix = File.createTempFile("coolfilesix", ".txt");
        BufferedWriter writersix = new BufferedWriter(new FileWriter(filesix));
        writersix.write("oftheblocksix");
        writersix.close();

        InputStream targetStreamsix = new FileInputStream(filesix);
        Scanner scannersix = new Scanner(filesix);
        String textsix = scannersix.nextLine();
        URI urisix = new URI(textsix);
        store.putDocument(targetStreamsix, urisix, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yessix = store.getDocumentAsTxt(urisix);
        assertEquals("oftheblocksix", yessix);
        System.out.println(yessix);


        File fileseven = File.createTempFile("coolfileseven", ".txt");
        BufferedWriter writerseven = new BufferedWriter(new FileWriter(fileseven));
        writerseven.write("oftheblockseven");
        writerseven.close();

        InputStream targetStreamseven = new FileInputStream(fileseven);
        Scanner scannerseven = new Scanner(fileseven);
        String textseven = scannerseven.nextLine();
        URI uriseven = new URI(textseven);
        store.putDocument(targetStreamseven, uriseven, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yesseven = store.getDocumentAsTxt(uriseven);
        assertEquals("oftheblockseven", yesseven);
        System.out.println(yesseven);


        File fileeight = File.createTempFile("coolfileeight", ".txt");
        BufferedWriter writereight = new BufferedWriter(new FileWriter(fileeight));
        writereight.write("oftheblockeight");
        writereight.close();

        InputStream targetStreameight = new FileInputStream(fileeight);
        Scanner scannereight = new Scanner(fileeight);
        String texteight = scannereight.nextLine();
        URI urieight = new URI(texteight);
        store.putDocument(targetStreameight, urieight, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yeseight = store.getDocumentAsTxt(urieight);
        assertEquals("oftheblockeight", yeseight);
        System.out.println(yeseight);


        File filenine = File.createTempFile("coolfilenine", ".txt");
        BufferedWriter writernine = new BufferedWriter(new FileWriter(filenine));
        writernine.write("oftheblocknine");
        writernine.close();

        InputStream targetStreamnine = new FileInputStream(filenine);
        Scanner scannernine = new Scanner(filenine);
        String textnine = scannernine.nextLine();
        URI urinine = new URI(textnine);
        store.putDocument(targetStreamnine, urinine, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yesnine = store.getDocumentAsTxt(urinine);
        assertEquals("oftheblocknine", yesnine);
        System.out.println(yesnine);


        File fileten = File.createTempFile("coolfileten", ".txt");
        BufferedWriter writerten = new BufferedWriter(new FileWriter(fileten));
        writerten.write("oftheblockten");
        writerten.close();

        InputStream targetStreamten = new FileInputStream(fileten);
        Scanner scannerten = new Scanner(fileten);
        String textten = scannerten.nextLine();
        URI uriten = new URI(textten);
        store.putDocument(targetStreamten, uriten, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yesten = store.getDocumentAsTxt(uriten);
        assertEquals("oftheblockten", yesten);
        System.out.println(yesten);


        File fileeleven = File.createTempFile("coolfileeleven", ".txt");
        BufferedWriter writereleven = new BufferedWriter(new FileWriter(fileeleven));
        writereleven.write("oftheblockeleven");
        writereleven.close();

        InputStream targetStreameleven = new FileInputStream(fileeleven);
        Scanner scannereleven = new Scanner(fileeleven);
        String texteleven = scannereleven.nextLine();
        URI urieleven = new URI(texteleven);
        store.putDocument(targetStreameleven, urieleven, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yeseleven = store.getDocumentAsTxt(urieleven);
        assertEquals("oftheblockeleven", yeseleven);
        System.out.println(yeseleven);


        File filetwelve = File.createTempFile("coolfile", ".txt");
        BufferedWriter writertwelve = new BufferedWriter(new FileWriter(filetwelve));
        writertwelve.write("oftheblock");
        writertwelve.close();

        InputStream targetStreamtwelve = new FileInputStream(filetwelve);
        Scanner scannertwelve = new Scanner(filetwelve);
        String texttwelve = scannertwelve.nextLine();
        URI uritwelve = new URI(texttwelve);
        int tip = store.putDocument(targetStreamtwelve, uritwelve, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yestwelve = store.getDocumentAsTxt(uritwelve);
        assertEquals("oftheblock", yestwelve);
        System.out.println(yestwelve);
        System.out.println(tip);
        System.out.println(tip0);

        assertEquals("oftheblockeleven", store.getDocumentAsTxt(urieleven));
        assertTrue(store.deleteDocument(urieleven));
        assertFalse(store.deleteDocument(urieleven));

        InputStream targetStreamnull = null;
        //Scanner scannereleven = new Scanner(fileeleven);
        //String texteleven = scannereleven.nextLine();
        //URI urieleven = new URI(texteleven);
        int hip = store.putDocument(targetStreamnull, urieleven, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        assertEquals(0, hip);
        URI tyi = new URI("tyi");
        hip = store.putDocument(targetStreamnull, tyi, DocumentStore.DocumentFormat.TXT);
        assertEquals(0, hip);
        //DocumentImpl doc = (DocumentImpl) store.hashTable.get(urieight);
        int hip2 = store.getDocumentAsTxt(urieight).hashCode();
        hip = store.putDocument(targetStreamnull, urieight, DocumentStore.DocumentFormat.TXT);
        URI nulluri = new URI("kjfhdjfhsdliu");

        assertNull(store.getDocumentAsPdf(nulluri));

        //adding pdf documents
        PDDocument pdfDoc = new PDDocument();
        PDPage firstPage = new PDPage();
        pdfDoc.addPage(firstPage);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PDPageContentStream cs = new PDPageContentStream(pdfDoc, firstPage);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.showText("flubberbig");
            cs.newLine();
            cs.endText();
            cs.close();
            pdfDoc.save(outputStream);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        /*ByteArrayInputStream in = new ByteArrayInputStream(outputStream.toByteArray());
        PDFTextStripper stripper = new PDFTextStripper();
        String docText = stripper.getText(pdfDoc);
        URI flubberuri = new URI(docText.trim());
        pdfDoc.close();
        store.putDocument(in, flubberuri, DocumentStore.DocumentFormat.PDF);
        String flubberstring = store.getDocumentAsTxt(flubberuri);
        assertEquals("flubberbig", flubberstring);*/

        System.out.println("get as pdf: " + Arrays.toString(store.getDocumentAsPdf(urifive)));

        InputStream inputStream = new FileInputStream("C:\\Users\\meirj\\birthdaysong.pdf");

        List list = store.searchByPrefix("op");
        assertEquals(0, list.size());


    }

    @Test
    public void remove() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();

        File file = File.createTempFile("coolfile", ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("oftheblock");
        writer.close();

        InputStream targetStream = new FileInputStream(file);
        Scanner scanner = new Scanner(file);
        String text = scanner.nextLine();
        URI uri = new URI(text);
        int tip0 = store.putDocument(targetStream, uri, DocumentStore.DocumentFormat.TXT);
        //System.out.println(Arrays.toString(store.getDocumentAsPdf(uri)));
        String yes = store.getDocumentAsTxt(uri);
        assertEquals("oftheblock", yes);

        assertNotNull(store.getDocumentAsTxt(uri));
        assertTrue(store.deleteDocument(uri));
        assertFalse(store.deleteDocument(uri));
        assertNull(store.getDocumentAsTxt(uri));

    }

    @Test
    public void noduplicatekeys() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();

        File file = File.createTempFile("coolfile", ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("oftheblock");
        writer.close();

        InputStream targetStream = new FileInputStream(file);
        Scanner scanner = new Scanner(file);
        String text = scanner.nextLine();
        URI uri = new URI(text);
        int tip0 = store.putDocument(targetStream, uri, DocumentStore.DocumentFormat.TXT);

        File file1 = File.createTempFile("coolfile", ".txt");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(file1));
        writer1.write("oftheblock");
        writer1.close();

        InputStream targetStream1 = new FileInputStream(file);
        Scanner scanner1 = new Scanner(file);
        if (scanner.hasNextLine()) {
            String text1 = scanner.nextLine();
        }
        URI uri1 = new URI(text);
        int up = store.putDocument(null, uri, DocumentStore.DocumentFormat.TXT);
        int up2 = store.putDocument(null, uri, DocumentStore.DocumentFormat.TXT);

        assertNull(store.getDocumentAsTxt(uri));
        assertEquals(0, up2);
        assertNotEquals(up, up2);

    }

    @Test
    public void pdfs() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();

        File file = File.createTempFile("coolfile", ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write("oftheblock");
        writer.close();

        InputStream targetStream = new FileInputStream(file);
        Scanner scanner = new Scanner(file);
        String text = scanner.nextLine();


        File file1 = File.createTempFile("coolfile1", ".pdf");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(file1));
        writer1.write("meirjacobs");
        writer1.close();

        InputStream targetStream1 = new FileInputStream(file1);
        Scanner scanner1 = new Scanner(file1);
        String text1 = scanner1.nextLine();
        URI uri1 = new URI(text1);
        store.putDocument(targetStream1, uri1, DocumentStore.DocumentFormat.TXT);
    }

    @Test
    public void newinput() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        File file1 = File.createTempFile("coolfile1", ".pdf");
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(file1));
        writer1.write("meirjacobs");
        writer1.close();

        InputStream targetStream1 = new FileInputStream(file1);
        Scanner scanner1 = new Scanner(file1);
        String text1 = scanner1.nextLine();
        URI uri1 = new URI(text1);
        int zero = store.putDocument(targetStream1, uri1, DocumentStore.DocumentFormat.TXT);

        assertEquals(0, zero);
    }

}