package org.taanisak.javi.views;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.taanisak.javi.models.FileBuffer;
import org.taanisak.javi.models.Position;

import java.io.IOException;
import java.util.ArrayList;

public class Editor {
    private final Terminal terminal;
    private final int width;
    private final int height;
    private final Position origin = new Position(0, 0);
    private final Position currentPosition = new Position(0, 0);
    private final FileBuffer buffer;

    private final ArrayList<Integer> modifiedLines;

    public Editor(FileBuffer buffer) throws IOException {
        this.buffer = buffer;
        terminal = new DefaultTerminalFactory().createTerminal();
        terminal.setCursorVisible(true);
        modifiedLines = new ArrayList<>();
        height = terminal.getTerminalSize().getRows();
        width = terminal.getTerminalSize().getColumns();
    }

    public void start() throws IOException {
        terminal.clearScreen();
        terminal.flush();
        terminal.setBackgroundColor(new TextColor.RGB(0, 0, 0));
        terminal.setForegroundColor(new TextColor.RGB(255, 255, 255));
        show();
        Position logical = new Position(buffer.getCursorRow(), buffer.getCursorColumn());
        Position visualCursor = viewPosition(logical);
        currentPosition.row = visualCursor.row;
        currentPosition.col = visualCursor.col;
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

    private Position viewPosition(Position logicalPosition) {
        Position visualPosition = new Position(0, 0);

        int startCol = 0;
        int startRow = 0;
        for (int i = startRow; i < logicalPosition.row; i++) {
            if (buffer.getLine(i).length() != 0) {
                if (i == startRow) {
                    visualPosition.row += (int) Math.ceil((double) (buffer.getLine(i).length() - startCol) / (double) width);
                } else {
                    visualPosition.row += (int) Math.ceil((double) buffer.getLine(i).length() / (double) width);
                }
            } else {
                visualPosition.row += 1;
            }
        }

        if (logicalPosition.row == startRow && startCol > 0) {
            visualPosition.row += ((logicalPosition.col - startCol) / width);
        } else {
            visualPosition.row += logicalPosition.col / width;
        }

        if (visualPosition.row >= height) {
            return null;
        }

        visualPosition.col = logicalPosition.col % width;

        visualPosition.row += origin.row;
        visualPosition.col += origin.col;
        return visualPosition;
    }

    private void show() throws IOException {
        terminal.clearScreen();
        if(buffer.isEmpty()) return;
        for (int i = 0; i < buffer.getNumberOfLines(); i++) {
            String line = buffer.getLine(i);
            for (int j = 0; j < line.length(); j++) {
                terminal.putCharacter(line.charAt(j));
            }
        }
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
            if (!modifiedLines.contains(buffer.getCursorRow())) {
                modifiedLines.add(buffer.getCursorRow());
            }
            buffer.insert(keyStroke.getCharacter());
            show();
        }
    }
    private void moveUp() throws IOException {
        //TODO need to check available dimensions
        buffer.setCursor(buffer.getCursorRow() - 1, buffer.getCursorColumn());
        Position backup = new Position(buffer.getCursorRow(), buffer.getCursorColumn());
        show();
        buffer.setCursor(backup.row, backup.col);

    }

    public void moveDown() throws IOException {
        buffer.setCursor(buffer.getCursorRow() + 1, buffer.getCursorColumn());
        Position backup = new Position(buffer.getCursorRow(), buffer.getCursorColumn());
        show();
        buffer.setCursor(backup.row, backup.col);
    }

    public void moveLeft() throws IOException {
        buffer.setCursor(buffer.getCursorRow(), buffer.getCursorColumn() - 1);
        Position backup = new Position(buffer.getCursorRow(), buffer.getCursorColumn());
        show();
        buffer.setCursor(backup.row, backup.col);
    }

    public void moveRight() throws IOException {
        buffer.setCursor(buffer.getCursorRow(), buffer.getCursorColumn() + 1);
        Position backup = new Position(buffer.getCursorRow(), buffer.getCursorColumn());
        show();
        buffer.setCursor(backup.row, backup.col);
    }
}
