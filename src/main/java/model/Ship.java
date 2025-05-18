// src/model/Ship.java
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bir geminin başlangıç ve bitiş pozisyonlarıyla tanımlandığı sınıf.
 */
public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Position start;
    private final Position end;

    public Ship(Position start, Position end) {
        // Yatay veya dikey olması gerektiğini garanti edelim
        if (start.getRow() != end.getRow() && start.getCol() != end.getCol()) {
            throw new IllegalArgumentException("Ship must be horizontal or vertical.");
        }
        this.start = start;
        this.end   = end;
    }

    /** Başlangıç pozisyonu */
    public Position getStart() {
        return start;
    }

    /** Bitiş pozisyonu */
    public Position getEnd() {
        return end;
    }

    /** Gemi üzerindeki tüm hücre pozisyonlarını sıralı olarak döner */
    public List<Position> getPositions() {
        List<Position> posList = new ArrayList<>();
        int dr = Integer.signum(end.getRow() - start.getRow());
        int dc = Integer.signum(end.getCol() - start.getCol());
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

    /** Gemi uzunluğunu döner (hücre sayısı) */
    public int length() {
        return getPositions().size();
    }

    /** Yatay konumdan gemi oluşturur */
    public static Ship fromHorizontal(Position start, int length) {
        Position end = new Position(start.getRow(), start.getCol() + length - 1);
        return new Ship(start, end);
    }

    /** Dikey konumdan gemi oluşturur */
    public static Ship fromVertical(Position start, int length) {
        Position end = new Position(start.getRow() + length - 1, start.getCol());
        return new Ship(start, end);
    }
}
