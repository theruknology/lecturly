package org.example.lecturly;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for saving and loading notebooks from local storage
 */
public class NotebookStorageService {
    private static final String NOTEBOOKS_DIR = "notebooks";
    private static final String NOTEBOOKS_INDEX_FILE = "notebooks_index.json";
    private Gson gson;
    private Path notebooksPath;

    public NotebookStorageService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        
        // Create notebooks directory in user's home or app directory
        String userHome = System.getProperty("user.home");
        this.notebooksPath = Paths.get(userHome, ".lecturly", NOTEBOOKS_DIR);
        
        try {
            Files.createDirectories(notebooksPath);
        } catch (IOException e) {
            System.err.println("Failed to create notebooks directory: " + e.getMessage());
        }
    }

    /**
     * Save a notebook to disk
     */
    public void saveNotebook(Notebook notebook) throws IOException {
        Path notebookFile = notebooksPath.resolve(notebook.getId() + ".json");
        String json = gson.toJson(notebook);
        Files.writeString(notebookFile, json);
        updateIndex(notebook);
    }

    /**
     * Load a notebook by ID
     */
    public Notebook loadNotebook(String id) throws IOException {
        Path notebookFile = notebooksPath.resolve(id + ".json");
        if (!Files.exists(notebookFile)) {
            throw new IOException("Notebook not found: " + id);
        }
        String json = Files.readString(notebookFile);
        return gson.fromJson(json, Notebook.class);
    }

    /**
     * Load all notebooks
     */
    public List<Notebook> loadAllNotebooks() {
        List<Notebook> notebooks = new ArrayList<>();
        try {
            // Ensure directory exists
            if (!Files.exists(notebooksPath)) {
                Files.createDirectories(notebooksPath);
            }
            
            // Try to load from index first
            Path indexFile = notebooksPath.getParent().resolve(NOTEBOOKS_INDEX_FILE);
            if (Files.exists(indexFile)) {
                try {
                    String indexJson = Files.readString(indexFile);
                    JsonArray indexArray = JsonParser.parseString(indexJson).getAsJsonArray();
                    for (var element : indexArray) {
                        String id = element.getAsJsonObject().get("id").getAsString();
                        try {
                            notebooks.add(loadNotebook(id));
                        } catch (IOException e) {
                            System.err.println("Failed to load notebook " + id + ": " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to read index file: " + e.getMessage());
                }
            }
            
            // Fallback: scan directory for all .json files (skip index file)
            if (Files.exists(notebooksPath)) {
                try {
                    Files.list(notebooksPath)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                String json = Files.readString(path);
                                Notebook notebook = gson.fromJson(json, Notebook.class);
                                // Avoid duplicates
                                if (notebooks.stream().noneMatch(n -> n.getId().equals(notebook.getId()))) {
                                    notebooks.add(notebook);
                                }
                            } catch (IOException e) {
                                System.err.println("Failed to load notebook from " + path + ": " + e.getMessage());
                            }
                        });
                } catch (IOException e) {
                    System.err.println("Failed to list notebooks directory: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load notebooks: " + e.getMessage());
        }
        return notebooks.stream()
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .collect(Collectors.toList());
    }
    
    /**
     * Initialize with dummy notebooks if none exist
     */
    public void initializeDummyNotebooks() {
        try {
            List<Notebook> existing = loadAllNotebooks();
            if (existing.isEmpty()) {
                // Create 3 dummy notebooks
                Notebook notebook1 = new Notebook("Introduction to Machine Learning");
                notebook1.setNotes("# Machine Learning Basics\n\nMachine learning is a subset of artificial intelligence that enables systems to learn and improve from experience without being explicitly programmed.\n\n## Key Concepts\n\n- **Supervised Learning**: Learning with labeled data\n- **Unsupervised Learning**: Finding patterns in unlabeled data\n- **Reinforcement Learning**: Learning through interaction with environment");
                saveNotebook(notebook1);
                
                Notebook notebook2 = new Notebook("Data Structures and Algorithms");
                notebook2.setNotes("# Data Structures\n\n## Arrays\nArrays are contiguous memory locations storing elements of the same type.\n\n## Linked Lists\nLinked lists are dynamic data structures where elements are connected via pointers.\n\n## Trees\nTrees are hierarchical data structures with nodes and edges.");
                saveNotebook(notebook2);
                
                Notebook notebook3 = new Notebook("Web Development Notes");
                notebook3.setNotes("# Web Development\n\n## Frontend\n- HTML: Structure\n- CSS: Styling\n- JavaScript: Interactivity\n\n## Backend\n- Server-side programming\n- Database management\n- API development");
                saveNotebook(notebook3);
                
                System.out.println("Created 3 dummy notebooks");
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize dummy notebooks: " + e.getMessage());
        }
    }

    /**
     * Delete a notebook
     */
    public void deleteNotebook(String id) throws IOException {
        Path notebookFile = notebooksPath.resolve(id + ".json");
        if (Files.exists(notebookFile)) {
            Files.delete(notebookFile);
        }
        updateIndexAfterDelete(id);
    }

    /**
     * Update the index file with notebook metadata
     */
    private void updateIndex(Notebook notebook) throws IOException {
        Path indexFile = notebooksPath.getParent().resolve(NOTEBOOKS_INDEX_FILE);
        JsonArray indexArray;
        
        if (Files.exists(indexFile)) {
            String indexJson = Files.readString(indexFile);
            indexArray = JsonParser.parseString(indexJson).getAsJsonArray();
            
            // Remove existing entry if present
            for (int i = indexArray.size() - 1; i >= 0; i--) {
                JsonObject element = indexArray.get(i).getAsJsonObject();
                if (element.get("id").getAsString().equals(notebook.getId())) {
                    indexArray.remove(i);
                    break;
                }
            }
        } else {
            indexArray = new JsonArray();
        }
        
        // Add new entry
        JsonObject entry = new JsonObject();
        entry.addProperty("id", notebook.getId());
        entry.addProperty("name", notebook.getName());
        entry.addProperty("updatedAt", notebook.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        indexArray.add(entry);
        
        Files.writeString(indexFile, gson.toJson(indexArray));
    }

    /**
     * Remove entry from index after deletion
     */
    private void updateIndexAfterDelete(String id) throws IOException {
        Path indexFile = notebooksPath.getParent().resolve(NOTEBOOKS_INDEX_FILE);
        if (!Files.exists(indexFile)) {
            return;
        }
        
        String indexJson = Files.readString(indexFile);
        JsonArray indexArray = JsonParser.parseString(indexJson).getAsJsonArray();
        
        // Remove entry with matching id
        for (int i = indexArray.size() - 1; i >= 0; i--) {
            JsonObject element = indexArray.get(i).getAsJsonObject();
            if (element.get("id").getAsString().equals(id)) {
                indexArray.remove(i);
                break;
            }
        }
        
        Files.writeString(indexFile, gson.toJson(indexArray));
    }

    /**
     * Custom adapter for LocalDateTime serialization
     */
    private static class LocalDateTimeAdapter implements com.google.gson.JsonSerializer<LocalDateTime>,
            com.google.gson.JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public com.google.gson.JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(formatter.format(src));
        }

        @Override
        public LocalDateTime deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
}

