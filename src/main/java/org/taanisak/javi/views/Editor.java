package org.taanisak.javi.views;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.taanisak.javi.models.FileBuffer;
import org.taanisak.javi.models.Position;

import java.io.IOException;
import java.util.HashMap;

public class Editor {
    private static final int TAB_SIZE = 4;
    private final Terminal terminal;
    private final int width;
    private final int height;
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
        terminal.putString("Commands");
        terminal.disableSGR(SGR.BOLD);
        nextLine();
        terminal.putString("'CTRL + e' - Edit Selected file");
        nextLine();
        terminal.putString("'CTRL + q' - Quit");
        nextLine();
        terminal.putString("'CTRL + s' - Save file");
        nextLine();
        terminal.putString("'ESC' - To see the welcome screen");
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
        show();
        editorScreen.setCursorPosition(new TerminalPosition(currentPosition.col, currentPosition.row));
        editorScreen.refresh();
    }

    private void show() throws IOException {
        int column = 0;
        int row = 0;
        terminal.clearScreen();
        editorScreen.clear();
        String content = buffer.content.report(0, buffer.contentLength);
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                column++;
                row = 0;
                continue;
            }
            editorScreen.setCharacter(row, column, new TextCharacter(
                    content.charAt(i),
                    TextColor.ANSI.DEFAULT,
                    TextColor.ANSI.DEFAULT));
            row++;
        }
        editorScreen.setCursorPosition(currentPosition.getAsTerminalPosition());
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
            buffer.numberOfLines++;
            currentPosition.col++;
            currentPosition.setRow(0);
            show();
        } else if (keyStroke.getKeyType().equals(KeyType.Backspace)) {
            buffer.delete();
            currentPosition.row--;
            show();
        } else if (keyStroke.getKeyType().equals(KeyType.Tab)) {
            for (int i = 0; i < TAB_SIZE; i++) {
                buffer.insert(' ');
            }
            currentPosition.row += TAB_SIZE;
            show();
        } else {
            buffer.insert(keyStroke.getCharacter());
            currentPosition.row++;
            show();
        }
    }

    private void moveUp() throws IOException {
        currentPosition.setColumn(currentPosition.col > 0 ? currentPosition.col-1 : currentPosition.col);
        editorScreen.setCursorPosition(currentPosition.getAsTerminalPosition());
        editorScreen.refresh();
    }

    public void moveDown() throws IOException {
        currentPosition.setColumn(currentPosition.col < buffer.numberOfLines ? currentPosition.col+1 : currentPosition.col);
        editorScreen.setCursorPosition(currentPosition.getAsTerminalPosition());
        editorScreen.refresh();
    }

    public void moveLeft() throws IOException {
        currentPosition.setRow(currentPosition.row > 0 ? currentPosition.row-1 : currentPosition.row);
        editorScreen.setCursorPosition(currentPosition.getAsTerminalPosition());
        editorScreen.refresh();
    }

    public void moveRight() throws IOException {
        currentPosition.setRow(currentPosition.row < buffer.lineNumberVsLength.get(buffer.currentLineNumber) ? currentPosition.row+1 : currentPosition.row);
        editorScreen.setCursorPosition(currentPosition.getAsTerminalPosition());
        editorScreen.refresh();
    }
}
