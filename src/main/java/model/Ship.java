// src/model/Ship.java
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Ship represents a battleship defined by its start and end positions.
 * The ship must be aligned either horizontally or vertically.
 */
public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Position start; // Starting position of the ship
    private final Position end;   // Ending position of the ship

    /**
     * Constructs a ship with a start and end position.
     * The ship must be either horizontal or vertical.
     *
     * @param start the starting cell of the ship
     * @param end the ending cell of the ship
     * @throws IllegalArgumentException if the ship is diagonal
     */
    public Ship(Position start, Position end) {
        if (start.getRow() != end.getRow() && start.getCol() != end.getCol()) {
            throw new IllegalArgumentException("Ship must be horizontal or vertical.");
        }
        this.start = start;
        this.end = end;
    }

    /** Returns the start position of the ship */
    public Position getStart() {
        return start;
    }

    /** Returns the end position of the ship */
    public Position getEnd() {
        return end;
    }

    /**
     * Returns a list of all positions occupied by this ship.
     *
     * @return list of Position objects between start and end (inclusive)
     */
    public List<Position> getPositions() {
        List<Position> posList = new ArrayList<>();

        int dr = Integer.signum(end.getRow() - start.getRow()); // direction row
        int dc = Integer.signum(end.getCol() - start.getCol()); // direction col

        int length = Math.max(
            Math.abs(end.getRow() - start.getRow()),
            Math.abs(end.getCol() - start.getCol())
        ) + 1;

        int r = start.getRow(), c = start.getCol();
        for (int i = 0; i < length; i++) {
            posList.add(new Position(r, c));
            r += dr;
            c += dc;
        }

        return posList;
    }

    /**
     * Returns the length of the ship in cells.
     */
    public int length() {
        return getPositions().size();
    }

    /**
     * Factory method: Creates a horizontal ship starting at a position.
     *
     * @param start  starting position
     * @param length number of cells the ship will occupy
     * @return Ship instance
     */
    public static Ship fromHorizontal(Position start, int length) {
        Position end = new Position(start.getRow(), start.getCol() + length - 1);
        return new Ship(start, end);
    }

    /**
     * Factory method: Creates a vertical ship starting at a position.
     *
     * @param start  starting position
     * @param length number of cells the ship will occupy
     * @return Ship instance
     */
    public static Ship fromVertical(Position start, int length) {
        Position end = new Position(start.getRow() + length - 1, start.getCol());
        return new Ship(start, end);
    }
}
