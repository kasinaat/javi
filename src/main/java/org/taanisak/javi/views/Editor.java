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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Editor {
    private static final int TAB_SIZE = 4;
    private final Terminal terminal;
    private final int width;
    private final int height;
    private int currentFileCounter = 0;

    private final Position currentPosition = new Position(0, 0);
    private final List<FileBuffer> filesList;
    private FileBuffer currentFile = null;
    Screen editorScreen;
    private boolean isEditMode;
    private boolean textWrapEnabled = true;

    Set<Integer> foldedLines = null;

    public Editor(FileBuffer buffer) throws IOException {
        this.filesList = new ArrayList<>();
        this.filesList.add(buffer);
        this.currentFile = buffer;
        terminal = new DefaultTerminalFactory().createTerminal();
        terminal.setCursorVisible(true);
        height = terminal.getTerminalSize().getRows();
        width = terminal.getTerminalSize().getColumns();
        foldedLines = new HashSet<>();
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
                }
                else if(keyStroke.isCtrlDown() && keyStroke.getCharacter() == 'o') {
                    String newFileName = createOrOpenFile();
                    saveFile();
                    FileBuffer fileBuffer = new FileBuffer(Paths.get(newFileName));
                    filesList.add(fileBuffer);
                    currentFile = fileBuffer;
                    displayWelcomeScreen();
                }
                else if (keyStroke.isCtrlDown() && keyStroke.getKeyType().equals(KeyType.Tab)) {
                    saveFile();
                    if(currentFileCounter == filesList.size() - 1) {
                        currentFileCounter = 0;
                    } else {
                        currentFileCounter++;
                    }
                    currentFile = filesList.get(currentFileCounter);
                    initEditMode();
                }
                else if (isEditMode) {
                    editBuffer(keyStroke);
                }
            }
        }
    }
    private String createOrOpenFile() throws IOException {
        terminal.clearScreen();
        terminal.setCursorPosition(0, 0);
        terminal.enableSGR(SGR.BOLD);
        terminal.putString("JAVI - Your Text Editor");
        terminal.disableSGR(SGR.BOLD);
        nextLine();
        terminal.putString(String.format("Current File : %s", currentFile.savePath));
        nextLine();
        terminal.putString("Enter file name to open : ");
        terminal.flush();
        int cursorX = 2;
        int cursorY = 25;
        StringBuilder newFileName = new StringBuilder();
        terminal.setCursorPosition(new TerminalPosition(cursorY,cursorX));
        while(true) {
            KeyStroke key = terminal.pollInput();
            if(key != null) {
                if(key.getKeyType().equals(KeyType.Character)) {
                    terminal.putCharacter(key.getCharacter());
                    newFileName.append(key.getCharacter());
                    terminal.setCursorPosition(new TerminalPosition(++cursorY,cursorX));
                    terminal.flush();
                }
                if (key.getKeyType().equals(KeyType.Backspace) && newFileName.length() > 0) {
                    terminal.setCursorPosition(new TerminalPosition(--cursorY,cursorX));
                    newFileName.deleteCharAt(newFileName.length() - 1);
                    terminal.putCharacter(' ');
                    terminal.setCursorPosition(new TerminalPosition(cursorY,cursorX));
                    terminal.flush();
                }
                if (key.getKeyType().equals(KeyType.Enter)) {
                    return newFileName.toString();
                }
            }
        }
    }

    private void closeEditor() throws IOException {
        if(editorScreen != null)
            editorScreen.close();
        terminal.close();
    }

    private void saveFile() throws IOException {
        currentFile.save();
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
        nextLine();
        terminal.putString("Current File : ");
        terminal.enableSGR(SGR.BOLD);
        terminal.putString(currentFile.savePath.toString());
        nextLine();
        terminal.disableSGR(SGR.BOLD);
        terminal.putString("List of Files Open : ");
        terminal.enableSGR(SGR.BOLD);
        terminal.putString(filesList.toString());
        nextLine();
        nextLine();
        terminal.putString("Commands");
        terminal.disableSGR(SGR.BOLD);
        nextLine();
        terminal.putString("'CTRL + e' - Edit Selected file");
        nextLine();
        terminal.putString("'CTRL + q' - Quit");
        nextLine();
        terminal.putString("'CTRL + s' - Save file");
        nextLine();
        terminal.putString("'ALT + z' - Toggle soft-wrap");
        nextLine();
        terminal.putString("'CTRL + o' - To create or open a new file");
        nextLine();
        terminal.putString("'CTRL + TAB' - Cycle through open files");
        nextLine();
        terminal.putString("'ESC' - To see the welcome screen");
        terminal.flush();
    }

    private void nextLine() throws IOException {
        terminal.setCursorPosition(0, terminal.getCursorPosition().getRow() + 1);
    }

    public void initEditMode() throws IOException {
        this.isEditMode = true;
//        terminal.setTitle(String.valueOf(currentFile));
        editorScreen = new TerminalScreen(terminal);
        editorScreen.startScreen();
        terminal.clearScreen();

        render();
        editorScreen.setCursorPosition(new TerminalPosition(currentPosition.col, currentPosition.row));
        editorScreen.refresh();
    }

    private void render() throws IOException {
        editorScreen.clear();
        int cursorX = 0;
        TerminalPosition cursorPosition = new TerminalPosition(0, 0);
        for (int i = 0; i < currentFile.getNumberOfLines(); i++) {
            String line = currentFile.getLine(i);
            int cursorY = 0;
            for (int j = 0; j < line.length(); j++) {
                if(cursorY >= width && textWrapEnabled) { // line folding
                    cursorY = 0;
                    cursorX++;
                }
                editorScreen.setCharacter(cursorY++, cursorX, new TextCharacter(
                        line.charAt(j),
                        TextColor.ANSI.DEFAULT,
                        TextColor.ANSI.DEFAULT));
                if(j == currentFile.getCursorColumn() && i == currentFile.getCursorRow())
                    cursorPosition = new TerminalPosition(currentFile.getCursorColumn() % width, cursorX);
            }
            if(line.length() == 0 && i == currentFile.getCursorRow()) {
                cursorPosition = new TerminalPosition(0, cursorX);
            }
            else if (line.length() == currentFile.getCursorColumn() && i == currentFile.getCursorRow()) {
                cursorPosition = new TerminalPosition(line.length() % width, cursorX);
            }
            cursorX++;
        }
        if(textWrapEnabled)
            editorScreen.setCursorPosition(cursorPosition);
        else
            editorScreen.setCursorPosition(new TerminalPosition(currentFile.getCursorColumn(), currentFile.getCursorRow()));
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
            currentFile.insert('\n');
            updateBufferAndScreen();
        } else if (keyStroke.getKeyType().equals(KeyType.Backspace)) {
            currentFile.delete();
            updateBufferAndScreen();
        } else if (keyStroke.getKeyType().equals(KeyType.Tab)) {
            for (int i = 0; i < TAB_SIZE; i++) {
                currentFile.insert(' ');
            }
            updateBufferAndScreen();
        }
        else if (keyStroke.isAltDown() && keyStroke.getCharacter().equals('z')) {
            textWrapEnabled = !textWrapEnabled;
            render();
        }
        else if(keyStroke.getKeyType().equals(KeyType.Character)) {
            currentFile.insert(keyStroke.getCharacter());
            updateBufferAndScreen();
        }
    }

    private void updateBufferAndScreen() throws IOException {
        currentFile.setCursor(currentFile.getCursorRow(), currentFile.getCursorColumn());
        render();
    }

    private void moveUp() throws IOException {
        if (currentFile.hasLine(currentFile.getCursorRow() - 1)) {
            currentFile.setCursor(currentFile.getCursorRow() - 1, currentFile.getCursorColumn());
//            show();
//            int prevLineLength = buffer.getLine(buffer.getCursorRow()).length();
//            int currentLineLength = buffer.getLine(buffer.getCursorRow()).length();
//            if (prevLineLength < currentLineLength) {
//                editorScreen.setCursorPosition(new TerminalPosition(prevLineLength, buffer.getCursorRow()));
//
//            } else {
//                editorScreen.setCursorPosition(new TerminalPosition(buffer.getCursorColumn(), buffer.getCursorRow()));
//            }
//            editorScreen.refresh();
        }
        render();
    }

    public void moveDown() throws IOException {
        if (currentFile.hasLine(currentFile.getCursorRow() + 1)) {
            currentFile.setCursor(currentFile.getCursorRow() + 1, currentFile.getCursorColumn());
//            show();
//            int nextLineLength = buffer.getLine(buffer.getCursorRow()).length();
//            int currentLineLength = buffer.getLine(buffer.getCursorRow()).length();
//            if (nextLineLength > currentLineLength) {
//                editorScreen.setCursorPosition(new TerminalPosition(nextLineLength, buffer.getCursorRow()));
//            } else {
//                editorScreen.setCursorPosition(new TerminalPosition(buffer.getCursorColumn(), buffer.getCursorRow()));
//            }
//            editorScreen.refresh();
            render();
        }
    }

    public void moveLeft() throws IOException {
        if (currentFile.getCursorRow() == 0 && currentFile.getCursorColumn() == 0) return;

        if (currentFile.getCursorColumn() != 0) {
            currentFile.setCursor(currentFile.getCursorRow(), currentFile.getCursorColumn() - 1);
        }
        render();
    }

    public void moveRight() throws IOException {
        if (currentFile.getCursorColumn() != currentFile.getLine(currentFile.getCursorRow()).length()) {
            currentFile.setCursor(currentFile.getCursorRow(), currentFile.getCursorColumn() + 1);
        }
        render();
    }
}
