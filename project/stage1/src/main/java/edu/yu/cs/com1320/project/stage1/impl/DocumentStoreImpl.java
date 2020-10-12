package edu.yu.cs.com1320.project.stage1.impl;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.Scanner;


public class DocumentStoreImpl implements DocumentStore {
    HashTableImpl<Object, Object> hashTable = new HashTableImpl<>();

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if(format != DocumentFormat.TXT && format != DocumentFormat.PDF){ throw new IllegalArgumentException("ERROR: Document format is neither TXT nor PDF"); }
        if(uri == null) { throw new IllegalArgumentException("ERROR: URI is null"); }
        if(input == null) { return (Integer) hashTable.put(uri, null); }
        String txt = null;
        Document doc = null;
        if(format == DocumentFormat.TXT) {
            txt = inputAsTxtForTXT(input);
            doc = new DocumentImpl(uri, txt, txt.hashCode());
        }
        else {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int bytesRead;
                byte[] data = new byte[1024];
                while ((bytesRead = input.read(data, 0, data.length)) != -1) { buffer.write(data, 0, bytesRead); }
                buffer.flush();
                byte[] byteArray = buffer.toByteArray();
                PDDocument document = PDDocument.load(byteArray);
                PDFTextStripper stripper = new PDFTextStripper();
                String string = stripper.getText(document).trim();
                document.close();
                doc = new DocumentImpl(uri, string, string.hashCode(), byteArray);
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        Object number = hashTable.put(uri, doc);
        if(number == null) {
            return 0;
        }
        return number.hashCode();
    }

    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if(hashTable.get(uri) == null) {
            return null;
        }
        DocumentImpl document = (DocumentImpl) hashTable.get(uri);
        return document.getDocumentAsPdf();
    }

    @Override
    public String getDocumentAsTxt(URI uri) {
        if(hashTable.get(uri) == null) {
            return null;
        }
        DocumentImpl document = (DocumentImpl) hashTable.get(uri);
        return document.getDocumentAsTxt().trim();
    }

    @Override
    public boolean deleteDocument(URI uri) {
        if(putDocument(null, uri, DocumentFormat.TXT) == 0) {
            return false;
        }
        else {
            return true;
        }
    }

    private String inputAsTxtForTXT(InputStream input) {
        Scanner sc = new Scanner(input).useDelimiter("\\A");
        return sc.hasNext() ? sc.next() : ""; // If scanner has a next, then add it to string, if not return empty string
    }

}
