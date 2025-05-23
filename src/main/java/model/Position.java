// src/model/Position.java
package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Position represents the coordinates of a single cell on the game board.
 * It is defined by a row and column index.
 */
public class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int row;  // The row index (0-based)
    private final int col;  // The column index (0-based)

    /**
     * Constructs a new Position object with the given row and column.
     * @param row the row index (must be >= 0)
     * @param col the column index (must be >= 0)
     * @throws IllegalArgumentException if row or col is negative
     */
    public Position(int row, int col) {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Row and col must be non-negative.");
        }
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the row index of this position.
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column index of this position.
     */
    public int getCol() {
        return col;
    }

    /**
     * Compares this position to another object for equality.
     * @param o another object to compare
     * @return true if the other object is a Position with the same row and column
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position that = (Position) o;
        return row == that.row && col == that.col;
    }

    /**
     * Returns a hash code for this position.
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    /**
     * Returns a string representation of the position.
     * Format: (row,col)
     */
    @Override
    public String toString() {
        return String.format("(%d,%d)", row, col);
    }
}
