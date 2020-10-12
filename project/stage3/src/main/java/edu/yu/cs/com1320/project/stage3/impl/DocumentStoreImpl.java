package edu.yu.cs.com1320.project.stage3.impl;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.function.Function;


public class DocumentStoreImpl implements DocumentStore {
    HashTableImpl<Object, Object> hashTable = new HashTableImpl<>();
    private StackImpl<Undoable> commandStack;
    private TrieImpl<Document> trie;

    public DocumentStoreImpl() {
        commandStack = new StackImpl<>();
        trie = new TrieImpl<>();
    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if(format != DocumentFormat.TXT && format != DocumentFormat.PDF){ throw new IllegalArgumentException("ERROR: Document format is neither TXT nor PDF"); }
        if(uri == null) { throw new IllegalArgumentException("ERROR: URI is null"); }
        if(input == null) {
            DocumentImpl document = (DocumentImpl) hashTable.get(uri);
            Function<URI, Boolean> undo = code -> {
                hashTable.put(code, document);
                triePut(document, document.getKeys());
                return true;
            };
            Boolean deleted = deleteFromTrie(document);
            Integer hcOfDeleted = (Integer) hashTable.put(uri, null);
            GenericCommand command = new GenericCommand(uri, undo);
            commandStack.push(command);
            return hcOfDeleted;
        }
        DocumentImpl doc = returnDocument(input, uri, format);
        Set<String> keys = doc.getKeys();
        triePut(doc, keys);
        Object previousDoc = null;
        if(hashTable.get(uri) != null) { previousDoc = hashTable.get(uri);} // saving previous value for undo
        Object number = hashTable.put(uri, doc);
        Function<URI, Boolean> undo = returnUndoFunction(number, doc, previousDoc);
        GenericCommand command = new GenericCommand(uri, undo);
        commandStack.push(command);
        if(number == null) { return 0;}
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
        if(hashTable.get(uri) == null) {
            Function<URI, Boolean> undo = code -> {return false;};
            GenericCommand command = new GenericCommand(uri, undo);
            commandStack.push(command);
            return false;
        }
        else {
            putDocument(null, uri, DocumentFormat.TXT);
            return true;
        }
    }

    @Override
    public void undo() throws IllegalStateException {
        if(commandStack.peek() == null) {
            throw new IllegalStateException("ERROR: GenericCommand Stack is empty");
        }

        Undoable command = commandStack.pop();
        command.undo();
    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        if(commandStack.peek() == null) {
            throw new IllegalStateException("ERROR: Stack is empty");
        }
        StackImpl<Undoable> tempStack = new StackImpl<>();
        while(commandStack.peek() != null) {
            if(commandStack.peek() instanceof GenericCommand) {
                GenericCommand command = (GenericCommand) commandStack.pop();
                if(command.getTarget().equals(uri)) {
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
            else {
                CommandSet command = (CommandSet) commandStack.pop();
                if(command.containsTarget(uri)) {
                    command.undo(uri);
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
    }

    @Override
    public List<String> search(String keyword) {
        if(keyword == null || keyword.equals("")) {
            return new ArrayList<String>();
        }
        keyword = wordShuffle(keyword);
        List<Document> list = trie.getAllSorted(keyword, new NormalValuesComp(keyword));
        List<String> stringList = new ArrayList<>();
        for(Object doc : list) {
            DocumentImpl document = (DocumentImpl) doc;
            stringList.add(document.getDocumentAsTxt());
        }
        return stringList;
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {
        if(keyword == null || keyword.equals("")) {
            return new ArrayList<byte[]>();
        }
        keyword = wordShuffle(keyword);
        List<Document> list = trie.getAllSorted(keyword, new NormalValuesComp(keyword));
        List<byte[]> byteArrayList = new ArrayList<>();
        for(Object doc : list) {
            DocumentImpl document = (DocumentImpl) doc;
            byteArrayList.add(document.getDocumentAsPdf());
        }
        return byteArrayList;
    }

    @Override
    public List<String> searchByPrefix(String prefix) {
        if(prefix == null || prefix.equals("")) {
            return new ArrayList<String>();
        }
        prefix = wordShuffle(prefix);
        PrefixValuesComp comparator = new PrefixValuesComp(prefix);
        List<Document>  list = trie.getAllWithPrefixSorted(prefix, comparator);
        List<String> stringList = new ArrayList<>();
        for(Object doc : list) {
            DocumentImpl document = (DocumentImpl) doc;
            stringList.add(document.getDocumentAsTxt());
        }
        return stringList;
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String prefix) {
        if(prefix == null || prefix.equals("")) {
            return new ArrayList<byte[]>();
        }
        prefix = wordShuffle(prefix);
        PrefixValuesComp comparator = new PrefixValuesComp(prefix);
        List<Document> list = trie.getAllWithPrefixSorted(prefix, comparator);
        List<byte[]> byteArrayList = new ArrayList<>();
        for(Object doc : list) {
            DocumentImpl document = (DocumentImpl) doc;
            byteArrayList.add(document.getDocumentAsPdf());
        }
        return byteArrayList;
    }

    @Override
    public Set<URI> deleteAll(String key) {
        if(key == null || key.equals("")) {
            return new HashSet<URI>();
        }
        key = wordShuffle(key);
        CommandSet commandSet = new CommandSet();
        Set<URI> uriSet = new HashSet<>();
        Set<Document> set = trie.deleteAll(key); // remove all docs from the trie and return set of all docs deleted
        if(set.isEmpty()) {
            commandStack.push(commandSet);
            return uriSet;
        }
        for(Document doc : set) { // for each doc deleted from the trie...
            DocumentImpl finalDoc = (DocumentImpl) doc;
            hashTable.put(finalDoc.getKey(), null); // remove doc from the hashTable
            Function<URI, Boolean> undo = code -> { // undo is to put the doc back in the hashTable and the trie
                hashTable.put(code, finalDoc);
                triePut(finalDoc, finalDoc.getKeys()); // puts doc back in trie in all the places it was deleted from (the values of all the words contained in the doc)
                return true;
            };
            URI docURI = doc.getKey();
            GenericCommand command = new GenericCommand(docURI, undo);
            commandSet.addCommand(command);
            uriSet.add(docURI);
        }
        commandStack.push(commandSet);
        return uriSet;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String prefix) {
        if(prefix == null || prefix.equals("")) {
            return new HashSet<URI>();
        }
        prefix = wordShuffle(prefix);
        CommandSet commandSet = new CommandSet();
        Set<URI> uriSet = new HashSet<>();
        Set<Document> set = trie.deleteAllWithPrefix(prefix); // remove all docs from the trie and return set of all docs deleted
        if(set.isEmpty()) {
            commandStack.push(commandSet);
            return uriSet;
        }
        for(Document doc : set) { // for each doc deleted from the trie...
            DocumentImpl finalDoc = (DocumentImpl) doc;
            hashTable.put(finalDoc.getKey(), null); // remove doc from the hashTable
            Function<URI, Boolean> undo = code -> { // undo is to put the doc back in the hashTable and the trie
                hashTable.put(code, finalDoc);
                triePut(finalDoc, finalDoc.getKeys()); // puts doc back in trie in all the places it was deleted from (the values of all the words contained in the doc)
                return true;
            };
            URI docURI = doc.getKey();
            GenericCommand command = new GenericCommand(docURI, undo);
            commandSet.addCommand(command);
            uriSet.add(docURI);
        }
        commandStack.push(commandSet);
        return uriSet;
    }

    protected Document getDocument(URI uri) {
        if(uri == null) {
            return null;
        }
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
            doc = new DocumentImpl(uri, txt, txt.hashCode());
        }
        else { // if it's a PDF
            try {
                byte[] byteArray = getByteArray(input);
                String string = makePDFFromByteArray(byteArray);
                doc = new DocumentImpl(uri, string, string.hashCode(), byteArray);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return doc;
    }

    private Function<URI, Boolean> returnUndoFunction(Object number, Document doc, Object finalPreviousDoc) {
        Function<URI, Boolean> undo = null;
        if(number == null || (number instanceof Integer && ((Integer) number) == 0)) {
            Function<URI, Boolean> undo1 = code -> {
                //deleteDocument(code);
                //commandStack.pop();
                hashTable.put(code, null);
                deleteFromTrie((DocumentImpl) doc);
                return true;
            };
            return undo1;
        }
        if(number instanceof Integer && (Integer) number == doc.hashCode()) {
            undo = code -> {
                return false;
            };
        }
        else { // if putDocument() overwrote a previous value for the associated key
            undo = code -> {
                hashTable.put(code, finalPreviousDoc);
                DocumentImpl finalfinalPreviousDoc = (DocumentImpl) finalPreviousDoc;
                triePut(finalfinalPreviousDoc, finalfinalPreviousDoc.getKeys());
                return true;
            };
        }
        return undo;
    }

    private void triePut(DocumentImpl document, Set<String> keys) {
        //HashMap<String, Integer> hashMap = document.getHashMap();
        for(String key : keys) {
            trie.put(key, document);
        }
    }

    private boolean deleteFromTrie(DocumentImpl document) {
        if(document == null || document.getKeys() == null || document.getKeys().isEmpty()) {
            return false;
        }
        for(String key : document.getKeys()) {
            trie.delete(key, document);
        }
        return true;
    }

    private String wordShuffle(String word) {
        for(int i = 0; i < word.length(); i++) {
            int num = (int) word.charAt(i);
            if((num < 48) || (num >= 58 && num <= 64) || (num >= 91 && num <= 96) || (num >= 123)) {
                word = word.replace(word.substring(i, i+1), "");
            }
        }
        return word;
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
