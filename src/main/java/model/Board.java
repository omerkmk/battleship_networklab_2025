// src/model/Board.java
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Board represents a player's game board.
 * It manages ships, fire actions, and hit/miss tracking.
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Cell enum defines the state of each cell on the board.
     */
    public enum Cell {
        EMPTY, // No ship and not fired upon
        SHIP,  // A ship occupies this cell
        HIT,   // A ship was hit in this cell
        MISS   // A shot missed in this cell
    }

    private final Cell[][] grid;      // 2D array representing the game board
    private final List<Ship> ships;   // List of ships placed on this board

    /**
     * Constructs a new Board with all cells initialized as EMPTY.
     */
    public Board() {
        grid = new Cell[GameRules.GRID_SIZE][GameRules.GRID_SIZE];
        ships = new ArrayList<>();

        for (int r = 0; r < GameRules.GRID_SIZE; r++) {
            for (int c = 0; c < GameRules.GRID_SIZE; c++) {
                grid[r][c] = Cell.EMPTY;
            }
        }
    }

    /**
     * Attempts to place a ship on the board.
     * Verifies the positions are in bounds and do not overlap other ships.
     *
     * @param ship the Ship object to place
     * @return true if placement is successful, false otherwise
     */
    public boolean placeShip(Ship ship) {
        for (Position p : ship.getPositions()) {
            int r = p.getRow(), c = p.getCol();
            if (r < 0 || r >= GameRules.GRID_SIZE
             || c < 0 || c >= GameRules.GRID_SIZE
             || grid[r][c] != Cell.EMPTY) {
                return false; // Invalid position or overlapping
            }
        }

        for (Position p : ship.getPositions()) {
            grid[p.getRow()][p.getCol()] = Cell.SHIP;
        }
        ships.add(ship);
        return true;
    }

    /**
     * Fires at a position on the board.
     * Updates the cell to HIT or MISS accordingly.
     *
     * @param p the position to fire at
     * @return the result of the shot (HIT, MISS, or previous result)
     */
    public Cell fire(Position p) {
        int r = p.getRow(), c = p.getCol();

        if (r < 0 || r >= GameRules.GRID_SIZE
         || c < 0 || c >= GameRules.GRID_SIZE) {
            return Cell.MISS; // Out of bounds is treated as miss
        }

        Cell current = grid[r][c];

        if (current == Cell.HIT || current == Cell.MISS) {
            return current; // Already fired here
        }

        if (current == Cell.SHIP) {
            grid[r][c] = Cell.HIT;
            return Cell.HIT;
        } else {
            grid[r][c] = Cell.MISS;
            return Cell.MISS;
        }
    }

    /**
     * Returns the current state of a cell at the given position.
     *
     * @param p the position to query
     * @return the cell status
     */
    public Cell getCell(Position p) {
        return grid[p.getRow()][p.getCol()];
    }

    /**
     * Manually sets the state of a cell.
     * Mainly used for visual updates or syncing board state.
     *
     * @param p the position to update
     * @param v the new cell value
     */
    public void setCell(Position p, Cell v) {
        grid[p.getRow()][p.getCol()] = v;
    }

    /**
     * Checks whether all ships on the board have been sunk.
     *
     * @return true if every ship cell is HIT, false otherwise
     */
    public boolean allSunk() {
        for (Ship s : ships) {
            for (Position p : s.getPositions()) {
                if (grid[p.getRow()][p.getCol()] != Cell.HIT) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the list of placed ships.
     * Used for revealing ship positions in the UI.
     *
     * @return list of ships on this board
     */
    public List<Ship> getShips() {
        return ships;
    }
}
