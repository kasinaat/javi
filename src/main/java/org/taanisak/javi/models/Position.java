package org.taanisak.javi.models;

import com.googlecode.lanterna.TerminalPosition;

public class Position {
    public int col;
    public int row;

    public Position() {
        row = 0;
        col = 0;
    }

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public TerminalPosition getAsTerminalPosition() {
        return new TerminalPosition(row, col);
    }

    public void setColumn(int i) {
        this.col = i;
    }

    public void setRow(int i) {
        this.row = i;
    }
}