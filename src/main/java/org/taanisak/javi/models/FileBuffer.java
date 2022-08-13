package org.taanisak.javi.models;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileBuffer extends Buffer { //Receiver class
    private Path savePath = null;

    public FileBuffer(Path path) throws IOException {
        super();
        open(path);
    }

    public FileBuffer() {

    }

    public void save() throws IOException {
        saveAs(savePath);
    }

    private void saveAs(Path path) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(path.toFile()), 32768);
        for (StringBuffer line : getLines()) {
            out.write(line.toString());
        }
        out.close();
    }

    private void open(Path path) throws IOException {
        this.savePath = path;
        File file = path.toFile();
        ArrayList<StringBuffer> lineList = super.getLines();
        if (file.exists() && !file.isDirectory()) {
            for (String line : Files.readAllLines(path, Charset.defaultCharset())) {
                StringBuffer sb = new StringBuffer(line);
                sb.append('\n');
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
}
