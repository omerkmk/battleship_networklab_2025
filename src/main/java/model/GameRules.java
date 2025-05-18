// src/model/GameRules.java
package model;

public class GameRules {
    /** Tahta kenar uzunluğu (NxN ızgara) */
    public static final int GRID_SIZE = 10;

    /** Yerleştirilecek gemilerin uzunlukları */
    public static final int[] SHIP_SIZES = { 5, 4, 3, 3, 2 };

    /** Toplam gemi sayısını döner */
    public static int numShips() {
        return SHIP_SIZES.length;
    }
}
