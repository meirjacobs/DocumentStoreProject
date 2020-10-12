package edu.yu.cs.com1320.project.stage5.impl;
import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Undoable;
//import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

// MAKE SURE BTREE CLASS PUTS THINGS ACCESED FROM DISC BACK INTO THE BTREE
public class DocumentStoreImpl implements DocumentStore {
    private BTreeImpl<URI, Document> bTree;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<URI> trie;
    private MinHeapImpl<DocumentStats> heap;
    private int maxDocumentCount;
    private int maxDocumentBytes;
    private int currentDocumentCount;
    private int currentDocumentBytes;
    private HashMap<URI, DocumentStats> keepTrackMap;
    private HashMap<URI, Boolean> seenBefore;

    public DocumentStoreImpl() {
        commandStack = new StackImpl<>();
        trie = new TrieImpl<>();
        heap = new MinHeapImpl<>();
        maxDocumentCount = Integer.MAX_VALUE;
        maxDocumentBytes = Integer.MAX_VALUE;
        currentDocumentCount = 0;
        currentDocumentBytes = 0;
        this.keepTrackMap = new HashMap<>();
        bTree = new BTreeImpl<>();
        String string = "";
        URI uri = null;
        try {
            uri = new URI(string);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        bTree.put(uri, null);
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager();
        bTree.setPersistenceManager(pm);
        this.seenBefore = new HashMap<>();
    }

    public DocumentStoreImpl(File baseDir) {
        commandStack = new StackImpl<>();
        trie = new TrieImpl<>();
        heap = new MinHeapImpl<>();
        maxDocumentCount = Integer.MAX_VALUE;
        maxDocumentBytes = Integer.MAX_VALUE;
        currentDocumentCount = 0;
        currentDocumentBytes = 0;
        this.keepTrackMap = new HashMap<>();
        bTree = new BTreeImpl<>();
        String string = "";
        URI uri = null;
        try {
            uri = new URI(string);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        bTree.put(uri, null);
        PersistenceManager<URI, Document> pm = new DocumentPersistenceManager(baseDir);
        bTree.setPersistenceManager(pm);
        this.seenBefore = new HashMap<>();
    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if(format != DocumentFormat.TXT && format != DocumentFormat.PDF){ throw new IllegalArgumentException("ERROR: Document format is neither TXT nor PDF"); }
        if(uri == null) { throw new IllegalArgumentException("ERROR: URI is null"); }
        if(input == null) {
            return deleteHelper(uri, bTreeGet(uri));
        }
        DocumentImpl doc = returnDocument(input, uri, format); // don't worry, the time is updated in this method
        DocumentImpl previousDoc = null; // initializing previousDoc
        if(seenBefore.containsKey(uri)) {
            if (bTreeGet(uri) != null) {
                previousDoc = bTreeGet(uri);
            } // saving previous value for undo and for potential space issues
        }
        if(previousDoc != null && previousDoc.equals(doc)) { // previous doc == new doc (i.e. duplicate put)
            return duplicate(previousDoc);
        }
        if (previousDoc == null) {
            try {
                dealWithSpaceIssues(doc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                dealWithSpaceIssues2(doc, previousDoc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        triePut(doc, doc.getKeys()); // putting in trie
        heap.insert(doc.getDocumentStats()); // putting in heap
        keepTrackMap.put(doc.getKey(), doc.getDocumentStats());
        seenBefore.put(doc.getKey(), true);
        DocumentImpl number = bTreePut(uri, doc); // putting in bTree
        Function<URI, Boolean> undo = returnUndoFunction(number, doc, previousDoc);
        commandStack.push(new GenericCommand(uri, undo));
        doc.setLastUseTime(System.nanoTime());
        heap.reHeapify(doc.getDocumentStats());
        currentDocumentCount++;
        currentDocumentBytes += doc.totalBytes();
        if(number == null) { return 0;}
        return number.getDocumentTextHashCode();
    }

    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if(bTreeGet(uri) == null) {
            return null;
        }
        DocumentImpl document = bTreeGet(uri);
        document.setLastUseTime(System.nanoTime());
        heap.reHeapify(new DocumentStats(document));
        keepTrackMap.put(document.getKey(), document.getDocumentStats());
        //seenBefore.put(document.getKey(), true);
        return document.getDocumentAsPdf();
    }

    @Override
    public String getDocumentAsTxt(URI uri) {
        System.out.println("get called");
        if(bTreeGet(uri) == null) {
            return null;
        }
        DocumentImpl document = bTreeGet(uri);
        document.setLastUseTime(System.nanoTime());
        heap.reHeapify(document.documentStats);
        keepTrackMap.put(document.getKey(), document.getDocumentStats());
        //seenBefore.put(document.getKey(), true);
        return document.getDocumentAsTxt().trim();
    }

    @Override
    public boolean deleteDocument(URI uri) {
        if(bTreeGet(uri) == null) {
            pushEmptyCommand(uri);
            return false;
        }
        else {
            deleteHelper(uri, bTreeGet(uri));
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
            DocumentImpl doc = bTreeGet(uri);
            doc.setLastUseTime(System.nanoTime());
            heap.reHeapify(doc.documentStats);
            keepTrackMap.put(doc.getKey(), doc.getDocumentStats());
            //seenBefore.put(doc.getKey(), true);
        }
        else {
            CommandSet commandSet = (CommandSet) command;
            Iterator<GenericCommand> iterator = commandSet.iterator();
            long time = System.nanoTime();
            while(iterator.hasNext()) {
                URI uri = (URI) iterator.next().getTarget();
                DocumentImpl document = bTreeGet(uri);
                if(document != null) {
                    document.setLastUseTime(time);
                    heap.reHeapify(document.documentStats);
                    keepTrackMap.put(document.getKey(), document.getDocumentStats());
                    //seenBefore.put(document.getKey(), true);
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
        List<URI> list = trie.getAllSorted(keyword, new NormalValuesComp0(keyword, bTree));
        List<String> stringList = new ArrayList<>();
        long time = System.nanoTime();
        for(URI docURI : list) {
            DocumentImpl document = bTreeGet(docURI);
            if(document == null) { continue;}
            stringList.add(document.getDocumentAsTxt());
            document.setLastUseTime(time);
            heap.reHeapify(document.documentStats);
            keepTrackMap.put(document.getKey(), document.getDocumentStats());
            //seenBefore.put(document.getKey(), true);
        }
        return stringList;
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {
        if(keyword == null || keyword.equals("")) {
            return new ArrayList<byte[]>();
        }
        keyword = wordShuffle(keyword);
        List<URI> list = trie.getAllSorted(keyword, new NormalValuesComp0(keyword, bTree));
        List<byte[]> byteArrayList = new ArrayList<>();
        long time = System.nanoTime();
        for(URI docURI : list) {
            DocumentImpl document = bTreeGet(docURI);
            if(document == null) { continue;}
            byteArrayList.add(document.getDocumentAsPdf());
            document.setLastUseTime(time);
            heap.reHeapify(document.documentStats);
            keepTrackMap.put(document.getKey(), document.getDocumentStats());
            //seenBefore.put(document.getKey(), true);
        }
        return byteArrayList;
    }

    @Override
    public List<String> searchByPrefix(String prefix) {
        if(prefix == null || prefix.equals("")) {
            return new ArrayList<String>();
        }
        prefix = wordShuffle(prefix);
        PrefixValuesComp1 comparator = new PrefixValuesComp1(prefix, bTree);
        List<URI> list = trie.getAllWithPrefixSorted(prefix, comparator);
        List<String> stringList = new ArrayList<>();
        long time = System.nanoTime();
        for(URI docURI : list) {
            DocumentImpl document = bTreeGet(docURI);
            if(document == null) { continue;}
            stringList.add(document.getDocumentAsTxt());
            document.setLastUseTime(time);
            heap.reHeapify(document.documentStats);
            keepTrackMap.put(document.getKey(), document.getDocumentStats());
            //seenBefore.put(document.getKey(), true);
        }
        return stringList;
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String prefix) {
        if(prefix == null || prefix.equals("")) {
            return new ArrayList<byte[]>();
        }
        prefix = wordShuffle(prefix);
        PrefixValuesComp1 comparator = new PrefixValuesComp1(prefix, bTree);
        List<URI> list = trie.getAllWithPrefixSorted(prefix, comparator);
        List<byte[]> byteArrayList = new ArrayList<>();
        long time = System.nanoTime();
        for(URI docURI : list) {
            DocumentImpl document = bTreeGet(docURI);
            if(document == null) { continue;}
            byteArrayList.add(document.getDocumentAsPdf());
            document.setLastUseTime(time);
            heap.reHeapify(document.documentStats);
            keepTrackMap.put(document.getKey(), document.getDocumentStats());
            //seenBefore.put(document.getKey(), true);
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
        Set<URI> set = trie.deleteAll(key); // remove all docs from the trie and return set of all docs deleted
        if(set.isEmpty()) {
            commandStack.push(commandSet);
            return uriSet;
        }
        for(URI docURI : set) { // for each doc deleted from the trie...
            DocumentImpl finalDoc = bTreeGet(docURI);
            bTreePut(docURI, null); // remove doc from the bTree
            deleteFromHeap(finalDoc);
            Function<URI, Boolean> undo = deleteAllUndoFunction(finalDoc);
            GenericCommand command = new GenericCommand(docURI, undo);
            commandSet.addCommand(command);
            //uriSet.add(docURI);
        }
        commandStack.push(commandSet);
        return set;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String prefix) {
        if(prefix == null || prefix.equals("")) {
            return new HashSet<URI>();
        }
        prefix = wordShuffle(prefix);
        CommandSet commandSet = new CommandSet();
        Set<URI> uriSet = new HashSet<>();
        Set<URI> set = trie.deleteAllWithPrefix(prefix); // remove all docs from the trie and return set of all docs deleted
        if(set.isEmpty()) {
            commandStack.push(commandSet);
            return uriSet;
        }
        for(URI docURI : set) { // for each doc deleted from the trie...
            DocumentImpl finalDoc = bTreeGet(docURI);
            bTreePut(docURI, null); // remove doc from the bTree
            deleteFromHeap(finalDoc);
            Function<URI, Boolean> undo = deleteAllUndoFunction(finalDoc);
            GenericCommand command = new GenericCommand(docURI, undo);
            commandSet.addCommand(command);
            //uriSet.add(docURI);
        }
        commandStack.push(commandSet);
        return set;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        if(limit < 0) {
            return;
        }
        this.maxDocumentCount = limit;
        if(currentDocumentCount > maxDocumentCount) {
            try {
                makeDocSpaceForNewDoc();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        if(limit < 0) {
            return;
        }
        this.maxDocumentBytes = limit;
        if(currentDocumentBytes > maxDocumentBytes) {
            try {
                makeByteSpaceForNewDoc(currentDocumentBytes - maxDocumentBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected Document getDocument(URI uri) {
        if(uri == null) {
            return null;
        }
        if(keepTrackMap.get(uri) == null) {
            return null;
        }
        return bTree.get(uri);
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
                bTreePut(doc.getKey(), null);
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
                try {
                    dealWithSpaceIssues2((DocumentImpl) finalPreviousDoc, doc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bTreePut(code, (DocumentImpl) finalPreviousDoc); // put previous in bTree
                DocumentImpl finalfinalPreviousDoc = (DocumentImpl) finalPreviousDoc;
                triePut(finalfinalPreviousDoc, finalfinalPreviousDoc.getKeys()); // put previous in trie
                finalfinalPreviousDoc.setLastUseTime(System.nanoTime());
                heap.insert(finalfinalPreviousDoc.getDocumentStats()); // put previous in heap
                keepTrackMap.put(finalfinalPreviousDoc.getKey(), finalfinalPreviousDoc.getDocumentStats());
                //seenBefore.put(finalfinalPreviousDoc.getKey(), true);
                return true;
            };
        }
        return undo;
    }

    private void triePut(DocumentImpl document, Set<String> keys) {
        //HashMap<String, Integer> hashMap = document.getHashMap();
        URI uri = document.getKey();
        for(String key : keys) {
            key = key.toUpperCase();
            for(int i = 0; i < key.length(); i++) {
                int num = (int) key.charAt(i);
                if((num < 48) || (num >= 58 && num <= 64) || (num >= 91 && num <= 96) || (num >= 123)) {
                    key = key.replace(key.substring(i, i+1), "");
                }
            }
            trie.put(key, uri);
        }
    }

    private boolean deleteFromTrie(DocumentImpl document) {
        if(document == null || document.getKeys() == null || document.getKeys().isEmpty()) {
            return false;
        }
        for(String key : document.getKeys()) {
            trie.delete(key, document.getKey());
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

    private void dealWithSpaceIssues(DocumentImpl doc) throws Exception {
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

    private void dealWithSpaceIssues2(DocumentImpl doc, DocumentImpl previousDoc) throws Exception {
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

    private void makeDocSpaceForNewDoc() throws Exception {
        while(currentDocumentCount > maxDocumentCount) {
            DocumentImpl removedDoc = bTreeGet(heap.removeMin().getURI());
            if(removedDoc == null) {
                return;
            }
            System.out.println("removed doc " + removedDoc.getKey().toString());
            keepTrackMap.remove(removedDoc.getKey());
            bTree.moveToDisk(removedDoc.getKey());
            //deleteFromTrie(removedDoc);
            currentDocumentCount--;
            currentDocumentBytes = currentDocumentBytes - removedDoc.totalBytes();
        }
    }

    private void makeByteSpaceForNewDoc(int bytesOver) throws Exception {
        int bytesRemoved = 0;
        while(bytesRemoved < bytesOver) {
            DocumentImpl removedDoc = bTreeGet(heap.removeMin().getURI());
            keepTrackMap.remove(removedDoc.getKey());
            bTree.moveToDisk(removedDoc.getKey());
            //deleteFromTrie(removedDoc);
            currentDocumentCount--;
            bytesRemoved += removedDoc.totalBytes();
            currentDocumentBytes = currentDocumentBytes - removedDoc.totalBytes();
        }
    }

    private int deleteHelper(URI uri, DocumentImpl document) {
        if(document == null || bTree.get(uri) == null) {
            pushEmptyCommand(uri);
            return 0;
        }
        Function<URI, Boolean> undo = code -> {
            try {
                dealWithSpaceIssues(document);
            } catch (Exception e) {
                e.printStackTrace();
            }
            document.setLastUseTime(System.nanoTime());
            bTreePut(code, document);
            triePut(document, document.getKeys());
            heap.insert(document.getDocumentStats());
            keepTrackMap.put(document.getKey(), document.getDocumentStats());
            //seenBefore.put(document.getKey(), true);
            currentDocumentCount++;
            currentDocumentBytes += document.totalBytes();
            return true;
        };
        deleteFromTrie(document);
        deleteFromHeap(document);
        Integer hcOfDeleted = bTreePut(uri, null).hashCode();
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
        heap.reHeapify(document.documentStats);
        heap.removeMin();
        keepTrackMap.remove(document.getKey());
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
        undo = code -> { // undo is to put the doc back in the bTree, trie, and heap
            try {
                dealWithSpaceIssues(finalDoc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finalDoc.setLastUseTime(System.nanoTime());
            bTreePut(code, finalDoc);
            triePut(finalDoc, finalDoc.getKeys()); // puts doc back in trie in all the places it was deleted from (the values of all the words contained in the doc)
            heap.insert(finalDoc.getDocumentStats());
            keepTrackMap.put(finalDoc.getKey(), finalDoc.getDocumentStats());
            //seenBefore.put(finalDoc.getKey(), true);
            return true;
        };
        return undo;
    }

    private int duplicate(DocumentImpl doc) {
        Function<URI, Boolean> undo;
        doc.setLastUseTime(System.nanoTime());
        heap.reHeapify(doc.documentStats);
        keepTrackMap.put(doc.getKey(), doc.getDocumentStats());
        //seenBefore.put(doc.getKey(), true);
        undo = code -> {
            doc.setLastUseTime(System.nanoTime());
            heap.reHeapify(doc.documentStats);
            keepTrackMap.put(doc.getKey(), doc.getDocumentStats());
            //seenBefore.put(doc.getKey(), true);
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

    private DocumentImpl bTreeGet(URI uri) {
        System.out.println("bTree get called");
        if(!seenBefore.containsKey(uri) || !seenBefore.get(uri)) {
            System.out.println("here");
            return null;
        }
        DocumentImpl document = (DocumentImpl) bTree.get(uri);
        if(document == null) {
            System.out.println("document is null");
        }
        if(document != null && (!keepTrackMap.containsKey(uri) || keepTrackMap.get(uri) == null)) {
            System.out.println("here1");
            putBackInMemory(document);
        }
        return document;
    }

    private DocumentImpl bTreePut(URI uri, DocumentImpl document) {
        DocumentImpl doc = (DocumentImpl) bTree.put(uri, document);
        if(doc == null) {
            return null;
        }
        if(!seenBefore.containsKey(doc.getKey()) || !seenBefore.get(uri)) {
            return null;
        }
        if(doc != null && (!keepTrackMap.containsKey(uri) || keepTrackMap.get(uri) == null)) {
            putBackInMemory(doc);
        }
        return doc;
    }

    private void putBackInMemory(DocumentImpl document) {
        System.out.println("pbim called");
        document.setLastUseTime(System.nanoTime());
        try {
            dealWithSpaceIssues(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        heap.insert(document.getDocumentStats());
        keepTrackMap.put(document.getKey(), document.getDocumentStats());
        //seenBefore.put(document.getKey(), true);
        triePut(document, document.getKeys());
    }

}

class NormalValuesComp0 implements Comparator<URI> {
    private String key;
    private BTreeImpl<URI, Document> bTree;

    protected NormalValuesComp0(String key, BTreeImpl<URI, Document> bTree) {
        this.key = key;
        this.bTree = bTree;
    }

    @Override
    public int compare(URI o1, URI o2) {
        return bTree.get(o2).wordCount(key) - bTree.get(o1).wordCount(key);
    }

}

class PrefixValuesComp1 implements Comparator<URI> {
    private String key;
    private BTreeImpl<URI, Document> bTree;

    protected PrefixValuesComp1(String key, BTreeImpl<URI, Document> bTree) {
        this.key = key;
        this.bTree = bTree;
    }

    @Override
    public int compare(URI o1, URI o2) {
        DocumentImpl doc1 = (DocumentImpl) bTree.get(o1);
        DocumentImpl doc2 = (DocumentImpl) bTree.get(o2);
        int doc1words = 0;
        for(String word: doc1.prefixWords(key)) {
            doc1words += doc1.wordCount(word);
        }

        int doc2words = 0;
        for(String word: doc2.prefixWords(key)) {
            doc2words += doc2.wordCount(word);
        }

        return doc2words - doc1words;
    }

}

class DocumentStats implements Comparable<DocumentStats> {
    DocumentImpl document;
    URI uri;
    long lastUsedTime;

    protected DocumentStats(DocumentImpl document) {
        this.document = document; // CAUTION: DON'T USE THIS BECAUSE IT'S CURRENTLY NOT BEING UPDATED LIKE THE TIME IS
        this.uri = document.getKey();
        this.lastUsedTime = document.getLastUseTime();
    }

    protected DocumentStats() {}

    protected URI getURI() {
        return this.uri;
    }

    @Override
    public int compareTo(DocumentStats o) {
        return (int) (this.lastUsedTime - o.lastUsedTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.uri.equals(((DocumentStats) o).getURI());
    }
}
