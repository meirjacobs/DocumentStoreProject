package edu.yu.cs.com1320.project.stage2.impl;
import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;
import java.util.Scanner;
import java.util.function.Function;


public class DocumentStoreImpl implements DocumentStore {
    HashTableImpl<Object, Object> hashTable = new HashTableImpl<>();
    private StackImpl commandStack;

    public DocumentStoreImpl() {
        commandStack = new StackImpl();
    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if(format != DocumentFormat.TXT && format != DocumentFormat.PDF){ throw new IllegalArgumentException("ERROR: Document format is neither TXT nor PDF"); }
        if(uri == null) { throw new IllegalArgumentException("ERROR: URI is null"); }
        if(input == null) { return (Integer) hashTable.put(uri, null); }
        DocumentImpl doc = returnDocument(input, uri, format);
        Object previousDoc = null;
        if(hashTable.get(uri) != null) { previousDoc = hashTable.get(uri);} // saving previous value for undo
        Object number = hashTable.put(uri, doc);
        Function<URI, Boolean> undo = returnUndoFunction(number, doc, previousDoc);
        Command command = new Command(uri, undo);
        commandStack.push((Object) command);
        if(number == null) { return 0;}
        return number.hashCode();
    }

    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if(hashTable.get(uri) == null) {
            return null;
        }
        edu.yu.cs.com1320.project.stage2.impl.DocumentImpl document = (edu.yu.cs.com1320.project.stage2.impl.DocumentImpl) hashTable.get(uri);
        return document.getDocumentAsPdf();
    }

    @Override
    public String getDocumentAsTxt(URI uri) {
        if(hashTable.get(uri) == null) {
            return null;
        }
        edu.yu.cs.com1320.project.stage2.impl.DocumentImpl document = (edu.yu.cs.com1320.project.stage2.impl.DocumentImpl) hashTable.get(uri);
        return document.getDocumentAsTxt().trim();
    }

    @Override
    public boolean deleteDocument(URI uri) {
        if(hashTable.get(uri) == null) {
            Function<URI, Boolean> undo = code -> {return false;};
            Command command = new Command(uri, undo);
            commandStack.push((Object) command);
            return false;
        }
        else {
            DocumentImpl document = (DocumentImpl) hashTable.get(uri);
            Function<URI, Boolean> undo = code -> {
                hashTable.put(code, document);
                return true;
            };
            putDocument(null, uri, DocumentFormat.TXT);
            Command command = new Command(uri, undo);
            commandStack.push((Object) command);
            return true;
        }
    }

    @Override
    public void undo() throws IllegalStateException {
        if(commandStack.peek() == null) {
            throw new IllegalStateException("ERROR: Command Stack is empty");
        }

        Command command = (Command) commandStack.pop();
        command.undo();
    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        if(commandStack.peek() == null) {
            throw new IllegalStateException("ERROR: Command Stack is empty");
        }
        StackImpl tempStack = new StackImpl();
        while(commandStack.peek() != null) {
            Command command = (Command) commandStack.pop();
            if(command.getUri().equals(uri)) {
                command.undo();
                while(tempStack.peek() != null) {
                    commandStack.push(tempStack.pop());
                }
                break;
            }
            else {
                tempStack.push(command);
            }
        }
    }

    protected Document getDocument(URI uri) {
        return (Document) hashTable.get(uri);
    }

    private String inputAsTxtForTXT(InputStream input) {
        Scanner sc = new Scanner(input).useDelimiter("\\A");
        return sc.hasNext() ? sc.next() : ""; // If scanner has a next, then add it to string, if not return empty string
    }

    private byte[] getByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int bytesRead;
        byte[] data = new byte[1024];
        while ((bytesRead = input.read(data, 0, data.length)) != -1) { buffer.write(data, 0, bytesRead); }
        buffer.flush();
        return buffer.toByteArray();
    }

    private String makePDFFromByteArray(byte[] byteArray) throws IOException {
        PDDocument document = PDDocument.load(byteArray);
        PDFTextStripper stripper = new PDFTextStripper();
        String string = stripper.getText(document).trim();
        document.close();
        return string;
    }

    private DocumentImpl returnDocument(InputStream input, URI uri, DocumentFormat format) {
        DocumentImpl doc = null;
        if(format == DocumentFormat.TXT) { // if it's a TXT
            String txt = inputAsTxtForTXT(input);
            doc = new edu.yu.cs.com1320.project.stage2.impl.DocumentImpl(uri, txt, txt.hashCode());
        }
        else { // if it's a PDF
            try {
                byte[] byteArray = getByteArray(input);
                String string = makePDFFromByteArray(byteArray);
                doc = new edu.yu.cs.com1320.project.stage2.impl.DocumentImpl(uri, string, string.hashCode(), byteArray);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return doc;
    }

    private Function<URI, Boolean> returnUndoFunction(Object number, DocumentImpl doc, Object finalPreviousDoc) {
        Function<URI, Boolean> undo = null;
        if(number == null || (number instanceof Integer && (Integer) number == 0)) {
            undo = code -> {
                deleteDocument(code);
                commandStack.pop();
                return true;
            };
        }
        if(number instanceof Integer && (Integer) number == doc.hashCode()) {
            undo = code -> {
                return false;
            };
        }
        else { // if putDocument() overwrote a previous value for the associated key
            undo = code -> {
                hashTable.put(code, finalPreviousDoc);
                return true;
            };
        }
        return undo;
    }
}
