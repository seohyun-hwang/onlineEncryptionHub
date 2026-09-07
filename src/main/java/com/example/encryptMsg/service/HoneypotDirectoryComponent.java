package com.example.encryptMsg.service;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class HoneypotDirectoryComponent {
    private String currentDirectory = "/home/passwordManager";

    private final Set<String> files = new HashSet<>(Set.of(
            "/home/passwordManager/readme.txt",
            "/home/passwordManager/config.json",
            "/home/passwordManager/users.txt",
            "/home/passwordManager/messages.txt"
    ));

    private final Map<String, String> fileContents = new HashMap<>(Map.of(
            "/home/passwordManager/readme.txt", "Welcome to the internal configurative directory.",
            "/home/passwordManager/config.json", "{\"db_host\": \"localhost:5555\", \"pass\": \"dkRJd239L!\"}",
            "/home/passwordManager/users.txt", "",
            "/home/passwordManager/messages.txt", ""
    ));

    public String getCurrentDirectory() { return currentDirectory; }

    public void changeDirectory(String target) {
        if (target.isEmpty() || target.equals("~")) {
            currentDirectory = "/home/passwordManager";
            return;
        }
        if (target.equals("..")) {
            if (!currentDirectory.equals("/")) {
                int lastSlash = currentDirectory.lastIndexOf("/");
                currentDirectory = (lastSlash == 0) ? "/" : currentDirectory.substring(0, lastSlash);
            }
            return;
        }
        currentDirectory = resolvePath(target);
        if (currentDirectory.length() > 1 && currentDirectory.endsWith("/")) {
            currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 1);
        }
    }

    public Set<String> getFiles() { return files; }
    public String getCurrentFileContentMap_forLLM() {
        StringBuilder sb = new StringBuilder();
        for (String fileContentsKey : files) {
            sb.append(fileContentsKey);
            sb.append(" -> ");
            sb.append(fileContents.get(fileContentsKey));
            sb.append("\n\n");
        }
        return sb.toString();
    }

    public void touchFile(String path) {
        String resolved = resolvePath(path);
        files.add(resolved);
        fileContents.putIfAbsent(resolved, "");
    }

    public void removeFile(String path) {
        String resolved = resolvePath(path);
        files.remove(resolved);
        fileContents.remove(resolved);
    }
    public String getFileContent(String path) {
        String resolved = resolvePath(path);
        // defaults to a 'file not found' error
        return fileContents.getOrDefault(resolved, "cat: " + path + ": No such file or directory");
    }
    public String resolvePath(String path) {
        if (path.startsWith("/")) return path;
        return currentDirectory.equals("/") ? "/" + path : currentDirectory + "/" + path;
    }
}