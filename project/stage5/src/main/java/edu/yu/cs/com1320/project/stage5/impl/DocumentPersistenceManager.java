package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import java.lang.reflect.Type;
import java.lang.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.reflect.TypeToken;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File baseDir;
    //private String fileName;

    public DocumentPersistenceManager() {}

    public DocumentPersistenceManager(File baseDir){
        this.baseDir = baseDir;
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        DocumentJsonSerializer<Document> documentJsonSerializer = new DocumentJsonSerializer<>();
        JsonElement element = documentJsonSerializer.serialize(val, DocumentImpl.class, null);
        String filePath;
        if(baseDir != null) {
            filePath = baseDir.getPath() + "\\";// + uri.getPath();
        }
        else {
            filePath = System.getProperty("user.dir") + "\\";
        }
        int deleteFrom = uri.toString().indexOf("://");
        if(deleteFrom == -1) {
            deleteFrom = uri.toString().indexOf(":\\\\");
        }
        String uriString = uri.toString().substring(deleteFrom + 3);
        filePath = filePath + uriString/*.replace("edu.yu.cs", "edu\\yu\\cs")*/.replace("/", File.separator).replace("\\", File.separator);
        filePath = filePath.replace("/", File.separator).replace("\\", File.separator);

        File file = new File(filePath);
        System.out.println("file path - " + filePath);
        file.getParentFile().mkdirs();
        System.out.println(file.getName() + " - file name");
        if(element == null) {
            file.delete();
            return;
        }
        FileWriter fileWriter = new FileWriter(file + ".json");
        fileWriter.write(element.getAsJsonObject().toString());
        fileWriter.close();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        String base;
        if(baseDir != null) {
            base = baseDir.getPath() + "\\"/* + uri.getPath()*/;
        }
        else {
            base = System.getProperty("user.dir") + "\\";
        }
        int deleteFrom = uri.toString().indexOf("://");
        if(deleteFrom == -1) {
            deleteFrom = uri.toString().indexOf(":\\\\");
        }
        String uriString = uri.toString().substring(deleteFrom + 3);
        String filePath = base + uriString/*.replace("edu.yu.cs", "edu\\yu\\cs")*/.replace("/", File.separator).replace("\\", File.separator) + ".json";
        //System.out.println("file path new way: " + filePath);
        //System.out.println("old way: " + uri.toString().replace("http://", base).replace("edu.yu.cs", "edu/yu/cs").replace("/", File.separator).replace("\\", File.separator) + ".json");
        filePath = filePath.replace("/", File.separator).replace("\\", File.separator);
        System.out.println("desrialize file path - " + filePath);
        File file = new File(filePath);
        if(!file.exists() || !file.canRead()) {
            System.out.println("can't find file");
            file.delete();
            return null;
        }
        FileReader reader = new FileReader(filePath);
        JsonElement element = JsonParser.parseReader(reader);
        reader.close();
        deleteFilesAndEmptyDirectories(file);
        DocumentJsonDeserializer<Document> documentJsonDeserializer = new DocumentJsonDeserializer<>();
        return documentJsonDeserializer.deserialize(element, DocumentImpl.class, null);
    }

    private void deleteFilesAndEmptyDirectories(File file) {
        System.out.println("delete files called");
        //System.out.println("file path: " + file.getPath());
        if (!file.exists() || !file.canRead()) {
            System.out.println("file doesn't exist0");
        }
        if(file.getParentFile() != null && file.getParentFile().isDirectory() && Objects.requireNonNull(file.getParentFile().listFiles()).length < 2) {
            System.out.println("got here9");
            File parent = file.getParentFile();
            boolean deleted = file.delete();
            if(deleted) {
                System.out.println("file deleted");
                deleteFilesAndEmptyDirectories(parent);
            }
            else {
                System.out.println("file didn't exist");
            }
        }
        else {
            System.out.println("got here1");
            file.delete();
        }
    }

    /*public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println(System.getProperties().get("user.dir"));
        System.out.println(System.getProperties().getProperty("user.dir\n"));
        DocumentImpl textDocument = new DocumentImpl(new URI("http://edu.yu.cs/com1320/txt"), "This is text content. Lots of it.", "This is text content. Lots of it.".hashCode());
        DocumentPersistenceManager dpm = new DocumentPersistenceManager(new File("C:\\Users\\meirj\\MyGit\\JacobsJordan\\DataStructures\\project\\stage5"));
        dpm.serialize(textDocument.getKey(), textDocument);

        //DocumentImpl textDocument = new DocumentImpl(new URI("http://edu.yu.cs/com1320/txt"), "This is text content. Lots of it.", "This is text content. Lots of it.".hashCode());
        //DocumentPersistenceManager dpm = new DocumentPersistenceManager(new File("C:\\Users\\meirj\\MyGit\\JacobsJordan\\DataStructures\\project\\stage5"));
        DocumentImpl document = (DocumentImpl) dpm.deserialize(textDocument.getKey());
    }*/
}

class DocumentJsonSerializer<Document> implements JsonSerializer<Document> {

    @Override
    public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext) {
        if(document == null) {
            return null;
        }
        Gson gson = new Gson();
        JsonObject object = new JsonObject();
        URI docURI = ((DocumentImpl) document).getKey();
        object.addProperty("URI/Key", docURI.toString());
        String textOfDoc = ((DocumentImpl) document).getDocumentAsTxt();
        object.addProperty("Document Text", textOfDoc);
        int docTextHashCode = ((DocumentImpl) document).getDocumentTextHashCode();
        object.addProperty("Hashcode of Document text", docTextHashCode);
        Map<String, Integer> wordMap = ((DocumentImpl) document).getWordMap();
        if(wordMap != null) {
            System.out.println("WORDMAP: " + wordMap.keySet().toString());
            Type gsonType = new TypeToken<HashMap<String, Integer>>() {
            }.getType();
            String jsonStringOfMap = gson.toJson(wordMap, gsonType);
            object.addProperty("wordMap", jsonStringOfMap);
        }

        return object;
    }
}

class DocumentJsonDeserializer<Document> implements JsonDeserializer<Document> {

    private Map<String, Integer> map = new HashMap<>();

    @Override
    public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Gson gson = new Gson();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        URI docURI = null;
        try {
            docURI = new URI(jsonObject.get("URI/Key").getAsString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String textOfDoc = jsonObject.get("Document Text").getAsString();
        int docTextHashCode = jsonObject.get("Hashcode of Document text").getAsInt();
        Map<String, Integer> wordMap = null;
        if(jsonObject.get("wordMap") == null) {
            return (Document) new DocumentImpl(docURI, textOfDoc, docTextHashCode, wordMap);
        }
        String string = jsonObject.get("wordMap").getAsString();
        Type gsonType = new TypeToken<HashMap<String, Integer>>() {
        }.getType();
        HashMap<String, Integer> map = gson.fromJson(string, gsonType);
        return (Document) new DocumentImpl(docURI, textOfDoc, docTextHashCode, map);
    }
}