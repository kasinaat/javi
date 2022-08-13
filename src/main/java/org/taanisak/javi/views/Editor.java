package org.taanisak.javi.views;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.taanisak.javi.models.FileBuffer;
import org.taanisak.javi.models.Position;

import java.io.IOException;

public class Editor {
    private final Terminal terminal;
    private final int width;
    private final int height;
    private final Position origin = new Position(0, 0);
    private final Position currentPosition = new Position(0, 0);
    private final FileBuffer buffer;
    public Editor(FileBuffer buffer) throws IOException {
        this.buffer = buffer;
        terminal = new DefaultTerminalFactory().createTerminal();
        terminal.setCursorVisible(true);
        height = terminal.getTerminalSize().getRows();
        width = terminal.getTerminalSize().getColumns();
    }

    public void start() throws IOException {
        terminal.clearScreen();
        terminal.flush();
        show();
        terminal.setCursorPosition(currentPosition.col, currentPosition.row);

        while (true) {
            KeyStroke keyStroke = terminal.pollInput();
            if (keyStroke != null) {
                editBuffer(keyStroke);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private void show() throws IOException {
        terminal.clearScreen();
        for (int i = 0; i < buffer.getNumberOfLines(); i++) {
            String line = buffer.getLine(i);
            for (int j = 0; j < line.length(); j++) {
                terminal.putCharacter(line.charAt(j));
            }
        }
        terminal.setCursorPosition(buffer.getCursorColumn(), buffer.getCursorRow());
        terminal.flush();
    }

    private void editBuffer(KeyStroke keyStroke) throws IOException {
        if (keyStroke.getKeyType().equals(KeyType.ArrowLeft)) {
            moveLeft();
        } else if (keyStroke.getKeyType().equals(KeyType.ArrowRight)) {
            moveRight();
        } else if (keyStroke.getKeyType().equals(KeyType.ArrowUp)) {
            moveUp();
        } else if (keyStroke.getKeyType().equals(KeyType.ArrowDown)) {
            moveDown();
        } else if (keyStroke.getKeyType().equals(KeyType.Enter)) {
            buffer.insert('\n');
            buffer.setCursor(buffer.getCursorRow(), buffer.getCursorColumn());
            terminal.setCursorPosition(buffer.getCursorColumn(), buffer.getCursorRow());
            show();
        } else if (keyStroke.getKeyType().equals(KeyType.Backspace)) {
            buffer.delete();
            int row = buffer.getCursorRow();
            int col = buffer.getCursorColumn();
            show();
            buffer.setCursor(row, col);
        } else if (keyStroke.isCtrlDown() && keyStroke.getCharacter() == 's') {
            buffer.save();
        } else {
            buffer.insert(keyStroke.getCharacter());
            show();
        }
    }

    private void moveUp() throws IOException {
        //TODO need to check available dimensions
        buffer.setCursor(buffer.getCursorRow() - 1, buffer.getCursorColumn());
        show();
    }

    public void moveDown() throws IOException {
        buffer.setCursor(buffer.getCursorRow() + 1, buffer.getCursorColumn());
        show();
    }

    public void moveLeft() throws IOException {
        buffer.setCursor(buffer.getCursorRow(), buffer.getCursorColumn() - 1);
        show();
    }

    public void moveRight() throws IOException {
        buffer.setCursor(buffer.getCursorRow(), buffer.getCursorColumn() + 1);
        show();
    }
}
