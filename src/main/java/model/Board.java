// src/model/Board.java
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bir oyuncunun tahtasını ve üzerindeki gemileri/atışları yönetir.
 */
public class Board implements Serializable {
    // src/model/Board.java

    private static final long serialVersionUID = 1L;

    /**
     * Her hücrenin durumu
     */
    public enum Cell {
        EMPTY, // Boş
        SHIP, // Gemi var
        HIT, // Vuruldu
        MISS     // Iskaladı
    }

    private final Cell[][] grid;
    private final List<Ship> ships;

    public Board() {
        grid = new Cell[GameRules.GRID_SIZE][GameRules.GRID_SIZE];
        ships = new ArrayList<>();
        // Başlangıçta tüm hücreler boştur
        for (int r = 0; r < GameRules.GRID_SIZE; r++) {
            for (int c = 0; c < GameRules.GRID_SIZE; c++) {
                grid[r][c] = Cell.EMPTY;
            }
        }
    }

    /**
     * Gemi yerleştirmeye çalışır. Eğer gemi tahtayı aşmıyor ve mevcut gemilerle
     * çakışmıyorsa, grid üzerinde SHIP olarak işaretler.
     *
     * @param ship Yerleştirilecek gemi
     * @return Başarılıysa true, aksi halde false
     */
    public boolean placeShip(Ship ship) {
        // Önce pozisyonları kontrol et
        for (Position p : ship.getPositions()) {
            int r = p.getRow(), c = p.getCol();
            if (r < 0 || r >= GameRules.GRID_SIZE
                    || c < 0 || c >= GameRules.GRID_SIZE
                    || grid[r][c] != Cell.EMPTY) {
                return false;
            }
        }
        // Hepsi uygunsa yerleştir
        for (Position p : ship.getPositions()) {
            grid[p.getRow()][p.getCol()] = Cell.SHIP;
        }
        ships.add(ship);
        return true;
    }

    /**
     * Bir pozisyona ateş eder. Eğer orada SHIP varsa HIT, değilse MISS olarak
     * işaretler ve sonucu döner.
     *
     * @param p Atış pozisyonu
     * @return Board.Cell.HIT veya MISS veya mevcut durum (tekrar atışsa)
     */
    public Cell fire(Position p) {
        int r = p.getRow(), c = p.getCol();
        if (r < 0 || r >= GameRules.GRID_SIZE
                || c < 0 || c >= GameRules.GRID_SIZE) {
            return Cell.MISS;
        }
        Cell current = grid[r][c];
        if (current == Cell.HIT || current == Cell.MISS) {
            return current; // zaten işaretlenmiş
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
     * Verilen pozisyondaki hücrenin güncel durumunu döner
     */
    public Cell getCell(Position p) {
        return grid[p.getRow()][p.getCol()];
    }

    // src/model/Board.java
    // Hücre durumunu dışarıdan set etmek için
    public void setCell(Position p, Cell v) {
        grid[p.getRow()][p.getCol()] = v;
    }

    /**
     * Tüm gemiler battı mı kontrol eder.
     *
     * @return Eğer tüm gemi hücreleri HIT ise true
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
     * Yerleştirilmiş gemi listesini döner (UI için revealShips)
     */
    public List<Ship> getShips() {
        return ships;
    }
}
