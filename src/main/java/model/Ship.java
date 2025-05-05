package model;

import java.util.ArrayList;
import java.util.List;

public class Ship {
    private final List<Position> positions; // Gemiye ait tüm hücreler
    private final List<Position> hits;      // İsabet alan hücreler

    public Ship(Position start, int length, boolean isHorizontal) {
        positions = new ArrayList<>();
        hits = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            int row = start.getRow() + (isHorizontal ? 0 : i);
            int col = start.getCol() + (isHorizontal ? i : 0);
            positions.add(new Position(row, col));
        }
    }

    public List<Position> getPositions() {
        return positions;
    }

    public boolean occupies(Position pos) {
        return positions.contains(pos);
    }

    public void hit(Position pos) {
        if (occupies(pos) && !hits.contains(pos)) {
            hits.add(pos);
        }
    }

    public boolean isSunk() {
        return hits.containsAll(positions);
    }

    @Override
    public String toString() {
        return "Ship{" + "positions=" + positions + ", hits=" + hits + '}';
    }
}
