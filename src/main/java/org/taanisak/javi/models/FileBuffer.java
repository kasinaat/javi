package org.taanisak.javi.models;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class FileBuffer {
    public Path savePath = null;
    public Rope content;
    public int cursorPosition = 0;
    public int contentLength = 0;
    public int numberOfLines = 0;

    public int currentLineNumber = 1;

    public HashMap<Integer, Integer> lineNumberVsLength = new HashMap<>();
    public FileBuffer(Path path) throws IOException {
        content = new Rope();
        loadContents(path);
    }

    public FileBuffer() {
        content = new Rope();
    }

    public void save() throws IOException {
        saveAs(savePath);
    }
    public void insert(char c) {
        content.insert(String.valueOf(c), cursorPosition);
        cursorPosition++;
        contentLength++;
    }

    public void delete() {
        content.delete(cursorPosition, cursorPosition + 1);
        cursorPosition--;
        contentLength--;
    }
    private void saveAs(Path path) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(path.toFile()), 32768);
        String result = content.report(0, contentLength);
        out.write(result);
        out.close();
    }

    private void loadContents(Path path) throws IOException {
        this.savePath = path;
        File file = path.toFile();
        if (file.exists() && !file.isDirectory()) {
            for (String line : Files.readAllLines(path, Charset.defaultCharset())) {
                content.insert(line, cursorPosition);
                cursorPosition += line.length();
                contentLength += line.length();
                content.insert("\n", cursorPosition++);
                numberOfLines++;
                lineNumberVsLength.put(currentLineNumber,contentLength - lineNumberVsLength.getOrDefault(currentLineNumber, 0) );
            }
        }
    }
}
