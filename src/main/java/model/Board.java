package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Oyuncunun oyun tahtasını temsil eder.
 */
public class Board {
    public enum Cell { EMPTY, SHIP, HIT, MISS }

    private static final int SIZE = 10;
    private final Cell[][] grid = new Cell[SIZE][SIZE];
    private final List<Ship> ships = new ArrayList<>();

    public Board() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = Cell.EMPTY;
            }
        }
    }

    public boolean placeShip(Ship ship) {
        for (Position pos : ship.getPositions()) {
            int row = pos.getRow();
            int col = pos.getCol();

            if (!isValidPosition(row, col) || grid[row][col] != Cell.EMPTY) {
                return false; // Taşma ya da çakışma
            }
        }

        for (Position pos : ship.getPositions()) {
            grid[pos.getRow()][pos.getCol()] = Cell.SHIP;
        }
        ships.add(ship);
        return true;
    }

    public Cell fire(Position pos) {
        int row = pos.getRow();
        int col = pos.getCol();

        if (!isValidPosition(row, col)) {
            throw new IllegalArgumentException("Invalid position: " + pos);
        }

        if (grid[row][col] == Cell.SHIP) {
            grid[row][col] = Cell.HIT;
            for (Ship ship : ships) {
                if (ship.occupies(pos)) {
                    ship.hit(pos);
                    break;
                }
            }
            return Cell.HIT;
        } else if (grid[row][col] == Cell.EMPTY) {
            grid[row][col] = Cell.MISS;
            return Cell.MISS;
        } else {
            return grid[row][col]; // Önceden vurulmuş yer
        }
    }

    public boolean allSunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }
}
