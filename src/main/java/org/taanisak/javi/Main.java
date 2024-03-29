package org.taanisak.javi;

import org.taanisak.javi.models.FileBuffer;
import org.taanisak.javi.views.Editor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {
        FileBuffer fileBuffer;
        if (args.length > 0) {
            Path path = Paths.get(args[0]);
            fileBuffer = new FileBuffer(path);
        } else {
            fileBuffer = new FileBuffer();
        }
        Editor editor = new Editor(fileBuffer);
        editor.start();
    }
}
