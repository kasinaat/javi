package org.taanisak.javi.models;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileBuffer extends Buffer { //Receiver class
    public Path savePath = null;

    @Override
    public String toString() {
        return savePath.toString();
    }

    public FileBuffer( Path path) throws IOException {
        open(path);
    }

    public FileBuffer() {
        super();
    }

    public void save() throws IOException {
        saveAs(savePath);
    }

    private void saveAs(Path path) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(path.toFile()), 32768);
        for (StringBuffer line : getLines()) {
            out.write(line.toString());
            out.write(System.lineSeparator());
        }
        out.close();
    }

    private void open(Path path) throws IOException {
        this.savePath = path;
        File file = path.toFile();
        ArrayList<StringBuffer> lineList = super.getLines();
        if (file.exists() && !file.isDirectory()) {
            lineList.clear();
            for (String line : Files.readAllLines(path, Charset.defaultCharset())) {
                StringBuffer sb = new StringBuffer(line);
                lineList.add(sb);
            }
            int row = lineList.size();
            int col = lineList.get(lineList.size() - 1).length();
            setCursor(row, col);
        }
    }

    @Override
    public void insert(char c) {
        super.insert(c);
    }

    public boolean hasLine(int lineNumber) {
        return lineNumber >= 0 || lineNumber <= getNumberOfLines();
    }
}
