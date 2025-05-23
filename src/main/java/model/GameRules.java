// src/model/GameRules.java
package model;

/**
 * GameRules defines the static rules and settings for the Battleship game.
 * Includes board size and the sizes of the ships to be placed.
 */
public class GameRules {

    /** Size of the square grid (NxN) */
    public static final int GRID_SIZE = 10;

    /** Lengths of the ships to be placed on the board */
    public static final int[] SHIP_SIZES = { 5, 4, 3, 3, 2 };

    /**
     * Returns the total number of ships each player must place.
     * @return number of ships
     */
    public static int numShips() {
        return SHIP_SIZES.length;
    }
}
