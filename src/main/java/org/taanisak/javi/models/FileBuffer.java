package org.taanisak.javi.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileBuffer extends Buffer{
    private Path savePath=null;

    public FileBuffer(Path path) throws IOException {
        open(path);
    }
    public void save() throws IOException {
        saveAs(savePath);
    }
    private void saveAs(Path path) throws IOException{
        Files.write(path, getLines(), StandardCharsets.UTF_8);
    }

    private void open (Path path) throws IOException {
        this.savePath = path;
        File file = path.toFile();
        ArrayList<StringBuffer> lineList = getLines();
        if (file.exists() && !file.isDirectory()) {
            for (String line : Files.readAllLines(path,Charset.defaultCharset())) {
                StringBuffer sb = new StringBuffer(line);
                lineList.add(sb);
            }
            if (lineList.size() > 0)
                lineList.remove(0);
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
