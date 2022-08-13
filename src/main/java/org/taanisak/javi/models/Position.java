package org.taanisak.javi.models;

public class Position
{
    public int col;
    public int row;

    public Position() {
        row=0;
        col=0;
    }

    public Position(int row, int col) {
        this.row=row;
        this.col=col;
    }
}