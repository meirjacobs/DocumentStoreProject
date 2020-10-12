package edu.yu.cs.com1320.project.stage4.impl;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.function.Function;


public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, DocumentImpl> hashTable = new HashTableImpl<>();
    private StackImpl<Undoable> commandStack;
    private TrieImpl<Document> trie;
    private MinHeapImpl<Document> heap;
    private int maxDocumentCount;
    private int maxDocumentBytes;
    private int currentDocumentCount;
    private int currentDocumentBytes;

    public DocumentStoreImpl() {
        commandStack = new StackImpl<>();
        trie = new TrieImpl<>();
        heap = new MinHeapImpl<>();
        maxDocumentCount = Integer.MAX_VALUE;
        maxDocumentBytes = Integer.MAX_VALUE;
        currentDocumentCount = 0;
        currentDocumentBytes = 0;
    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if(format != DocumentFormat.TXT && format != DocumentFormat.PDF){ throw new IllegalArgumentException("ERROR: Document format is neither TXT nor PDF"); }
        if(uri == null) { throw new IllegalArgumentException("ERROR: URI is null"); }
        if(input == null) {
            return deleteHelper(uri, hashTable.get(uri));
        }
        DocumentImpl doc = returnDocument(input, uri, format); // don't worry, the time is updated in this method
        DocumentImpl previousDoc = null; // initializing previousDoc
        if(hashTable.get(uri) != null) { previousDoc = hashTable.get(uri);} // saving previous value for undo and for potential space issues
        if(previousDoc != null && previousDoc.equals(doc)) { // previous doc == new doc (i.e. duplicate put)
            return duplicate(previousDoc);
        }
        if (previousDoc == null) {
            dealWithSpaceIssues(doc);
        } else {
            dealWithSpaceIssues2(doc, previousDoc);
        }

        triePut(doc, doc.getKeys()); // putting in trie
        heap.insert(doc); // putting in heap
        DocumentImpl number = hashTable.put(uri, doc); // putting in hashTable
        Function<URI, Boolean> undo = returnUndoFunction(number, doc, previousDoc);
        commandStack.push(new GenericCommand(uri, undo));
        currentDocumentCount++;
        currentDocumentBytes += doc.totalBytes();
        if(number == null) { return 0;}
        return number.getDocumentTextHashCode();
    }

    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if(hashTable.get(uri) == null) {
            return null;
        }
        DocumentImpl document = hashTable.get(uri);
        document.setLastUseTime(System.nanoTime());
        heap.reHeapify(document);
        return document.getDocumentAsPdf();
    }

    @Override
    public String getDocumentAsTxt(URI uri) {
        if(hashTable.get(uri) == null) {
            return null;
        }
        DocumentImpl document = hashTable.get(uri);
        document.setLastUseTime(System.nanoTime());
        heap.reHeapify(document);
        return document.getDocumentAsTxt().trim();
    }

    @Override
    public boolean deleteDocument(URI uri) {
        if(hashTable.get(uri) == null) {
            pushEmptyCommand(uri);
            return false;
        }
        else {
            deleteHelper(uri, hashTable.get(uri));
            return true;
        }
    }

    @Override
    public void undo() throws IllegalStateException {
        if(commandStack.peek() == null) {
            throw new IllegalStateException("ERROR: GenericCommand Stack is empty");
        }
        Undoable command = commandStack.pop();

        // updating lastUseTime of undone doc(s)
        if(command instanceof GenericCommand) {
            GenericCommand gc = (GenericCommand) command;
            URI uri = (URI) gc.getTarget();
            DocumentImpl doc = hashTable.get(uri);
            doc.setLastUseTime(System.nanoTime());
            heap.reHeapify(doc);
        }
        else {
            CommandSet commandSet = (CommandSet) command;
            Iterator<GenericCommand> iterator = commandSet.iterator();
            long time = System.nanoTime();
            while(iterator.hasNext()) {
                URI uri = (URI) iterator.next().getTarget();
                DocumentImpl document = hashTable.get(uri);
                if(document != null) {
                    document.setLastUseTime(time);
                    heap.reHeapify(document);
                }
            }
        }

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
        long time = System.nanoTime();
        for(Object doc : list) {
            DocumentImpl document = (DocumentImpl) doc;
            stringList.add(document.getDocumentAsTxt());
            document.setLastUseTime(time);
            heap.reHeapify(document);
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
        long time = System.nanoTime();
        for(Object doc : list) {
            DocumentImpl document = (DocumentImpl) doc;
            byteArrayList.add(document.getDocumentAsPdf());
            document.setLastUseTime(time);
            heap.reHeapify(document);
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
        long time = System.nanoTime();
        for(Object doc : list) {
            DocumentImpl document = (DocumentImpl) doc;
            stringList.add(document.getDocumentAsTxt());
            document.setLastUseTime(time);
            heap.reHeapify(document);
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
        long time = System.nanoTime();
        for(Object doc : list) {
            DocumentImpl document = (DocumentImpl) doc;
            byteArrayList.add(document.getDocumentAsPdf());
            document.setLastUseTime(time);
            heap.reHeapify(document);
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
            deleteFromHeap(finalDoc);
            Function<URI, Boolean> undo = deleteAllUndoFunction(finalDoc);
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
            deleteFromHeap(finalDoc);
            Function<URI, Boolean> undo = deleteAllUndoFunction(finalDoc);
            URI docURI = doc.getKey();
            GenericCommand command = new GenericCommand(docURI, undo);
            commandSet.addCommand(command);
            uriSet.add(docURI);
        }
        commandStack.push(commandSet);
        return uriSet;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        if(limit < 0) {
            return;
        }
        this.maxDocumentCount = limit;
        if(currentDocumentCount > maxDocumentCount) {
            makeDocSpaceForNewDoc();
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        if(limit < 0) {
            return;
        }
        this.maxDocumentBytes = limit;
        if(currentDocumentBytes > maxDocumentBytes) {
            makeByteSpaceForNewDoc(currentDocumentBytes - maxDocumentBytes);
        }
    }

    protected Document getDocument(URI uri) {
        if(uri == null) {
            return null;
        }
        return hashTable.get(uri);
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
        if(doc != null) {
            doc.setLastUseTime(System.nanoTime()); // setting last used time of doc
        }
        return doc;
    }

    private Function<URI, Boolean> returnUndoFunction(Object number, DocumentImpl doc, Object finalPreviousDoc) {
        Function<URI, Boolean> undo;
        if(number == null || (number instanceof Integer && ((Integer) number) == 0)) { // normal put, no previous doc
            Function<URI, Boolean> undo1;
            undo1 = code -> {
                hashTable.put(doc.getKey(), null);
                deleteFromTrie(doc);
                deleteFromHeap(doc);
                currentDocumentCount--;
                currentDocumentBytes = currentDocumentBytes - doc.totalBytes();
                return true;
            };
            return undo1;
        }
        else { // if putDocument() overwrote a previous value for the associated key
            undo = code -> {
                dealWithSpaceIssues2((DocumentImpl) finalPreviousDoc, doc);
                hashTable.put(code, (DocumentImpl) finalPreviousDoc); // put previous in hashTable
                DocumentImpl finalfinalPreviousDoc = (DocumentImpl) finalPreviousDoc;
                triePut(finalfinalPreviousDoc, finalfinalPreviousDoc.getKeys()); // put previous in trie
                finalfinalPreviousDoc.setLastUseTime(System.nanoTime());
                heap.insert(finalfinalPreviousDoc); // put previous in heap
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

    private void dealWithSpaceIssues(DocumentImpl doc) {
        if(currentDocumentCount + 1 > maxDocumentCount) {
            currentDocumentCount++;
            makeDocSpaceForNewDoc();
            currentDocumentCount--;
        }
        int docTotalBytes = doc.totalBytes();
        if((currentDocumentBytes + docTotalBytes) > maxDocumentBytes) {
            makeByteSpaceForNewDoc(currentDocumentBytes + docTotalBytes - maxDocumentBytes);
        }
    }

    private void dealWithSpaceIssues2(DocumentImpl doc, DocumentImpl previousDoc) {
        if(previousDoc == null) {
            dealWithSpaceIssues(doc);
            return;
        }
        int docTotalBytes = doc.totalBytes();
        int prevDocTotalBytes = previousDoc.totalBytes();
        int addedBytes = docTotalBytes - prevDocTotalBytes;
        if(currentDocumentBytes + addedBytes > maxDocumentBytes) {
            makeByteSpaceForNewDoc(currentDocumentBytes + addedBytes - maxDocumentBytes);
        }
    }

    private void makeDocSpaceForNewDoc() {
        while(currentDocumentCount > maxDocumentCount) {
            DocumentImpl removedDoc = (DocumentImpl) heap.removeMin();
            hashTable.put(removedDoc.getKey(), null);
            deleteFromTrie(removedDoc);
            currentDocumentCount--;
            currentDocumentBytes = currentDocumentBytes - removedDoc.totalBytes();
            deleteFromStack(removedDoc);
        }
    }

    private void makeByteSpaceForNewDoc(int bytesOver) {
        int bytesRemoved = 0;
        while(bytesRemoved < bytesOver) {
            DocumentImpl removedDoc = (DocumentImpl) heap.removeMin();
            hashTable.put(removedDoc.getKey(), null);
            deleteFromTrie(removedDoc);
            currentDocumentCount--;
            bytesRemoved += removedDoc.totalBytes();
            currentDocumentBytes = currentDocumentBytes - removedDoc.totalBytes();
            deleteFromStack(removedDoc);
        }
    }

    private int deleteHelper(URI uri, DocumentImpl document) {
        if(document == null) {
            pushEmptyCommand(uri);
            return 0;
        }
        Function<URI, Boolean> undo = code -> {
            dealWithSpaceIssues(document);
            document.setLastUseTime(System.nanoTime());
            hashTable.put(code, document);
            triePut(document, document.getKeys());
            heap.insert(document);
            currentDocumentCount++;
            currentDocumentBytes += document.totalBytes();
            return true;
        };
        deleteFromTrie(document);
        deleteFromHeap(document);
        Integer hcOfDeleted = hashTable.put(uri, null).hashCode();
        GenericCommand command = new GenericCommand(uri, undo);
        commandStack.push(command);
        currentDocumentCount--;
        currentDocumentBytes -= document.totalBytes();
        return hcOfDeleted;
    }

    private void deleteFromHeap(DocumentImpl document) {
        if(document == null) {
            return;
        }
        document.setLastUseTime(Long.MIN_VALUE);
        heap.reHeapify(document);
        heap.removeMin();
    }

    private void deleteFromStack(DocumentImpl document) {
        URI target = document.getKey();
        StackImpl<Undoable> tempStack = new StackImpl<>();
        while(commandStack.peek() != null) {
            Undoable command = commandStack.pop();
            if(command instanceof GenericCommand) {
                GenericCommand gc = (GenericCommand) command;
                if(gc.getTarget().equals(target)) {
                    continue;
                }
                tempStack.push(command);
            }
            else {
                CommandSet cs = (CommandSet) command;
                if(cs.containsTarget(target)) {
                    Iterator iterator = cs.iterator();
                    while(iterator.hasNext()) {
                        GenericCommand genericCommand = (GenericCommand) iterator.next();
                        if(genericCommand.getTarget().equals(target)) {
                            iterator.remove();
                        }
                    }
                }
                tempStack.push(command);
            }
        }
        while(tempStack.peek() != null) {
            commandStack.push(tempStack.pop());
        }
    }

    private Function<URI, Boolean> deleteAllUndoFunction(DocumentImpl finalDoc) {
        Function<URI, Boolean> undo;
        undo = code -> { // undo is to put the doc back in the hashTable, trie, and heap
            dealWithSpaceIssues(finalDoc);
            finalDoc.setLastUseTime(System.nanoTime());
            hashTable.put(code, finalDoc);
            triePut(finalDoc, finalDoc.getKeys()); // puts doc back in trie in all the places it was deleted from (the values of all the words contained in the doc)
            heap.insert(finalDoc);
            return true;
        };
        return undo;
    }

    private int duplicate(DocumentImpl doc) {
        Function<URI, Boolean> undo;
        doc.setLastUseTime(System.nanoTime());
        heap.reHeapify(doc);
        undo = code -> {
            doc.setLastUseTime(System.nanoTime());
            heap.reHeapify(doc);
            return false;
        };
        GenericCommand command = new GenericCommand(doc.getKey(), undo);
        commandStack.push(command);
        return doc.getDocumentTextHashCode();
    }

    private void pushEmptyCommand(URI uri) {
        Function<URI, Boolean> undo = code -> false;
        GenericCommand command = new GenericCommand(uri, undo);
        commandStack.push(command);
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
