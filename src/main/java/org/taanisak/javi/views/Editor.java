package org.taanisak.javi.views;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.taanisak.javi.models.FileBuffer;
import org.taanisak.javi.models.OperationType;
import org.taanisak.javi.models.Position;

import java.io.IOException;
import java.util.EnumSet;

public class Editor {
    private static final int TAB_SIZE = 4;
    private final Terminal terminal;
    private final int width;
    private final int height;
    private final Position origin = new Position(0, 0);

    private final Position currentPosition = new Position(0, 0);
    private final FileBuffer buffer;
    Screen editorScreen;
    private boolean isEditMode;

    public Editor(FileBuffer buffer) throws IOException {
        this.buffer = buffer;
        terminal = new DefaultTerminalFactory().createTerminal();
        terminal.setCursorVisible(true);
        height = terminal.getTerminalSize().getRows();
        width = terminal.getTerminalSize().getColumns();
    }

    public void start() throws IOException {
        displayWelcomeScreen();
        waitForCommands();
    }

    private void waitForCommands() throws IOException {
        while (true) {
            KeyStroke keyStroke = terminal.pollInput();
            if (keyStroke != null) {
                if (keyStroke.isCtrlDown() && keyStroke.getCharacter() == 'e') {
                    initEditMode();
                } else if (keyStroke.getKeyType().equals(KeyType.Escape)) {
                    exitEditMode();
                } else if (keyStroke.isCtrlDown() && keyStroke.getCharacter() == 's') {
                    saveFile();
                } else if (keyStroke.isCtrlDown() && keyStroke.getCharacter() == 'q') {
                    closeEditor();
                    break;
                } else if (isEditMode) {
                    editBuffer(keyStroke);
                }

            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private void closeEditor() throws IOException {
        editorScreen.close();
        terminal.close();
    }

    private void saveFile() throws IOException {
        buffer.save();
    }

    private void exitEditMode() throws IOException {
        isEditMode = false;
        displayWelcomeScreen();
    }

    private void displayWelcomeScreen() throws IOException {
        terminal.clearScreen();
        terminal.setCursorPosition(0, 0);
        terminal.enableSGR(SGR.BOLD);
        terminal.putString("JAVI - Your Text Editor");
        terminal.disableSGR(SGR.BOLD);
        nextLine();
        terminal.putString(String.format("Current File : %s", buffer.savePath));
        nextLine();
        terminal.enableSGR(SGR.BOLD);
        terminal.putString(String.format("Commands"));
        terminal.disableSGR(SGR.BOLD);
        nextLine();
        terminal.putString(String.format("'CTRL + e' - Edit Selected file"));
        nextLine();
        terminal.putString(String.format("'CTRL + q' - Quit"));
        nextLine();
        terminal.putString(String.format("'CTRL + s' - Save file"));
        nextLine();
        terminal.putString(String.format("'ESC' - To see the welcome screen"));
        terminal.flush();
    }

    private void nextLine() throws IOException {
        terminal.setCursorPosition(0, terminal.getCursorPosition().getRow() + 1);
    }

    public void initEditMode() throws IOException {
        this.isEditMode = true;
        editorScreen = new TerminalScreen(terminal);
        editorScreen.startScreen();
        terminal.clearScreen();

        render();
        editorScreen.setCursorPosition(new TerminalPosition(currentPosition.col, currentPosition.row));
        editorScreen.refresh();
    }

    private void render() throws IOException {
        editorScreen.clear();
        for (int i = 0; i < buffer.getNumberOfLines(); i++) {
            String line = buffer.getLine(i);
//            for (int j = startColumn; j < line.length(); j++) {
//                if (line.charAt(j) == '\n') continue;
            TextGraphics textGraphics = editorScreen.newTextGraphics();
            textGraphics.putString(0, i, line);
//            }
        }
        editorScreen.refresh();
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
            updateBufferAndScreen(OperationType.NEWLINE);
        } else if (keyStroke.getKeyType().equals(KeyType.Backspace)) {
            buffer.delete();
            updateBufferAndScreen(OperationType.DELETE);
        } else if (keyStroke.getKeyType().equals(KeyType.Tab)) {
            for (int i = 0; i < TAB_SIZE; i++) {
                buffer.insert(' ');
            }
            updateBufferAndScreen(OperationType.INSERT);
        } else {
            buffer.insert(keyStroke.getCharacter());
            updateBufferAndScreen(OperationType.INSERT);
        }
    }

    private void updateBufferAndScreen(OperationType operationType) throws IOException {
        buffer.setCursor(buffer.getCursorRow(), buffer.getCursorColumn());
        editorScreen.setCursorPosition(new TerminalPosition(buffer.getCursorColumn(), buffer.getCursorRow()));
        if(operationType.equals(OperationType.INSERT))
            render();
        else
            render();

    }

    private void moveUp() throws IOException {
        if (buffer.hasLine(buffer.getCursorRow() - 1)) {
            buffer.setCursor(buffer.getCursorRow() - 1, buffer.getCursorColumn());
//            show();
            int prevLineLength = buffer.getLine(buffer.getCursorRow()).length();
            int currentLineLength = buffer.getLine(buffer.getCursorRow()).length();
            if (prevLineLength < currentLineLength) {
                editorScreen.setCursorPosition(new TerminalPosition(prevLineLength, buffer.getCursorRow()));

            } else {
                editorScreen.setCursorPosition(new TerminalPosition(buffer.getCursorColumn(), buffer.getCursorRow()));
            }
            editorScreen.refresh();
        }
    }

    public void moveDown() throws IOException {
        if (buffer.hasLine(buffer.getCursorRow() + 1)) {
            buffer.setCursor(buffer.getCursorRow() + 1, buffer.getCursorColumn());
//            show();
            int nextLineLength = buffer.getLine(buffer.getCursorRow()).length();
            int currentLineLength = buffer.getLine(buffer.getCursorRow()).length();
            if (nextLineLength > currentLineLength) {
                editorScreen.setCursorPosition(new TerminalPosition(nextLineLength, buffer.getCursorRow()));
            } else {
                editorScreen.setCursorPosition(new TerminalPosition(buffer.getCursorColumn(), buffer.getCursorRow()));
            }
            editorScreen.refresh();
        }
    }

    public void moveLeft() throws IOException {
        if (buffer.getCursorRow() == 0 && buffer.getCursorColumn() == 0) return;

        if (buffer.getCursorColumn() == 0 && buffer.hasLine(buffer.getCursorRow() - 1)) {
            buffer.setCursor(buffer.getCursorRow() - 1, buffer.getLine(buffer.getCursorRow() - 1).length());
        } else {
            buffer.setCursor(buffer.getCursorRow(), buffer.getCursorColumn() - 1);
        }
//        show();
        editorScreen.setCursorPosition(new TerminalPosition(buffer.getCursorColumn(), buffer.getCursorRow()));
        editorScreen.refresh();
    }

    public void moveRight() throws IOException {
        int numberOfRows = buffer.getNumberOfLines();
        if (buffer.getCursorRow() == numberOfRows && buffer.getCursorColumn() == buffer.getLine(numberOfRows).length())
            return;

        if (buffer.getCursorColumn() == buffer.getLine(buffer.getCursorRow()).length()) {
            buffer.setCursor(buffer.getCursorRow() + 1, 0);
        } else {
            buffer.setCursor(buffer.getCursorRow(), buffer.getCursorColumn() + 1);
        }
//        show();
        editorScreen.setCursorPosition(new TerminalPosition(buffer.getCursorColumn(), buffer.getCursorRow()));
        editorScreen.refresh();
    }
}
