package edu.yu.cs.com1320.project.stage5.impl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class DocumentImpl implements Document {
    private InputStream doc;
    private URI uri;
    private DocumentStore.DocumentFormat format;
    private String documentText;
    private int documentHashCode;
    private byte[] pdfBytes;
    private HashMap<String, Integer> hashMap;
    private Set<String> keys;
    private long lastUseTime;
    protected DocumentStats documentStats = new DocumentStats();
    //private boolean needsToBeBroughtBackIntoMemory;

    public DocumentImpl(URI uri, String txt, int txtHash){
        this.uri = uri;
        this.documentStats.uri = uri;
        this.documentText = txt;
        this.documentHashCode = txtHash;
        this.pdfBytes = null;
        this.hashMap = new HashMap<>();
        makeHashMap();
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes){
        this(uri, txt, txtHash);
        this.pdfBytes = pdfBytes;
    }

    public DocumentImpl(URI uri, String txt, int txtHash, HashMap<String, Integer> hashMap, byte[] pdfBytes) {
        this(uri, txt, txtHash, pdfBytes);
        //this.hashMap = hashMap;
    }

    public DocumentImpl(URI docURI, String textOfDoc, int docTextHashCode, Map<String, Integer> wordMap) {
        this(docURI, textOfDoc, docTextHashCode);
        //this.hashMap = (HashMap<String, Integer>) wordMap;
    }

    @Override
    public byte[] getDocumentAsPdf() {

        PDDocument pdfDoc = new PDDocument();
        PDPage firstPage = new PDPage();
        pdfDoc.addPage(firstPage);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PDPageContentStream cs = new PDPageContentStream(pdfDoc, firstPage);
            cs.beginText();
            cs.newLine();
            cs.setFont(PDType1Font.TIMES_ROMAN, 14);
            cs.showText(this.documentText.trim());
            cs.endText();
            cs.close();
            pdfDoc.save(outputStream);
            pdfDoc.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        this.pdfBytes = outputStream.toByteArray();

        return this.pdfBytes;
    }

    @Override
    public String getDocumentAsTxt() {
        return this.documentText.trim();
    }

    @Override
    public int getDocumentTextHashCode() {
        return this.documentHashCode;
    }

    @Override
    public URI getKey() {
        return this.uri;
    }

    @Override
    public int wordCount(String word) {
        for(int i = 0; i < word.length(); i++) {
            int num = (int) word.charAt(i);
            if((num < 48) || (num >= 58 && num <= 64) || (num >= 91 && num <= 96) || (num >= 123)) {
                word = word.replace(word.substring(i, i+1), "");
            }
        }
        word = word.toUpperCase();
        if(!hashMap.containsKey(word)) {
            return 0;
        }
        return (Integer) hashMap.get(word);
    }

    @Override
    public long getLastUseTime() {
        return lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        lastUseTime = timeInNanoseconds;
        documentStats.lastUsedTime = lastUseTime;
    }

    @Override
    public Map<String, Integer> getWordMap() {
        return this.hashMap;
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        HashMap<String, Integer> correctMap = new HashMap<>();
        for(String word : wordMap.keySet()) {
            Integer integer = wordMap.get(word).intValue();
            word = word.toUpperCase();
            for(int i = 0; i < word.length(); i++) {
                int num = (int) word.charAt(i);
                if((num < 48) || (num >= 58 && num <= 64) || (num >= 91 && num <= 96) || (num >= 123)) {
                    word = word.replace(word.substring(i, i+1), "");
                }
            }
            correctMap.put(word, integer);
        }
        this.hashMap = correctMap;
    }

    private Set<String> makeHashMap() {
        Set<String> keys = new HashSet<>();
        String[] wordsArray = this.documentText.split("\\s+");
        for(String word : wordsArray) {
            word = word.toUpperCase();
            for(int i = 0; i < word.length(); i++) {
                int num = (int) word.charAt(i);
                if((num < 48) || (num >= 58 && num <= 64) || (num >= 91 && num <= 96) || (num >= 123)) {
                    word = word.replace(word.substring(i, i+1), "");
                }
            }
            if(hashMap.get(word) == null) {
                hashMap.put(word, 1);
            }
            else{
                Integer integer = (Integer) hashMap.get(word);
                hashMap.put(word, integer + 1);
            }
            keys.add(word);
        }
        this.keys = keys;
        return keys;
    }

    protected HashMap<String, Integer> getHashMap() {
        return this.hashMap;
    }

    protected Set<String> getKeys() {
        return this.keys;
    }

    protected Set<String> prefixWords(String prefix) {
        Set<String> words = new HashSet<>();
        for(String word : getKeys()) {
            if(word.length() < prefix.length()) {
                continue;
            }
            if(word.substring(0, prefix.length()).equals(prefix.toUpperCase())) {
                words.add(word);
            }
        }
        return words;
    }

    protected int totalBytes() {
        return getDocumentAsTxt().getBytes().length + getDocumentAsPdf().length;
    }

    protected DocumentStats getDocumentStats() {
        return documentStats;
    }

    /*protected void setNeedsToBeBroughtBackIntoMemory(boolean truefalse) {
        this.needsToBeBroughtBackIntoMemory = truefalse;
    }

    protected boolean getNeedsToBeBroughtBackIntoMemory() {
        return this.needsToBeBroughtBackIntoMemory;
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentImpl document = (DocumentImpl) o;
        return Objects.equals(uri, document.uri) && Objects.equals(hashCode(), document.hashCode()) &&
                Objects.equals(documentText, document.documentText);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int key = 1;

        key = prime * key + uri.hashCode();
        key = prime * key +  ((documentText == null) ? 0 : documentText.hashCode());

        return key;
    }

    @Override
    public int compareTo(Document o) {
        return (int) (this.getLastUseTime() - o.getLastUseTime());
    }
}
