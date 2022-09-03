package org.taanisak.javi.models;

import java.util.ArrayList;

public class Buffer {


    private final ArrayList<StringBuffer> lines;
    private int cursorRow;
    private int cursorColumn;

    public Buffer() {
        StringBuffer line = new StringBuffer("");
        lines = new ArrayList<>();
        lines.add(line);
        cursorColumn = 0;
        cursorRow = 0;
    }

    public ArrayList<StringBuffer> getLines() {
        return lines;
    }

    public String getLine(int index) {
        if (index < 0 || index >= lines.size())
            throw new IllegalArgumentException();

        return lines.get(index).toString();
    }

    public int getCursorRow() {
        return cursorRow;
    }

    public int getCursorColumn() {
        return cursorColumn;
    }

    public int getNumberOfLines() {
        return lines.size();
    }

    public void setCursor(int row, int col) {
        if (row >= 0 && row < lines.size()) {
            if (col >= 0 && col <= lines.get(row).length()) {
                cursorRow = row;
                cursorColumn = col;
            } else {
                cursorRow = row;
                cursorColumn = lines.get(row).length();
            }
        }
    }

    public void insert(char c) {
        if (c == '\n') {
            addNewLine();
            return;
        }
        lines.get(cursorRow).insert(cursorColumn++, c);
    }

    public void delete() {
        if (cursorColumn == 0 && cursorRow > 0) {
            cursorRow--;
            cursorColumn = lines.get(cursorRow).length();
            lines.get(cursorRow).append(lines.remove(cursorRow + 1));
        } else if (cursorColumn > 0) {
            lines.get(cursorRow).deleteCharAt(--cursorColumn);
        }
    }

    public void addNewLine() {
        StringBuffer newLine = new StringBuffer(lines.get(cursorRow).substring(cursorColumn));
        lines.get(cursorRow).delete(cursorColumn, lines.get(cursorRow).length());
        lines.add(++cursorRow, newLine);
        this.cursorColumn = 0;
    }

    public boolean isEmpty() {
        return cursorRow == 0 && cursorColumn == 0;
    }
}
