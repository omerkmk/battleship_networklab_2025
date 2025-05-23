// src/common/PlaceShipRequest.java
package common;

import model.Ship;
import java.io.Serializable;

/**
 * PlaceShipRequest is sent from the client to the server
 * to request placement of a ship on the game board.
 */
public class PlaceShipRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int player;   // ID of the player placing the ship
    private final Ship ship;    // Ship to be placed on the board

    /**
     * Constructs a new PlaceShipRequest with player ID and ship object.
     * @param player the ID of the player (0 or 1)
     * @param ship the ship to place on the board
     */
    public PlaceShipRequest(int player, Ship ship) {
        this.player = player;
        this.ship   = ship;
    }

    /**
     * Returns the ID of the player making the request.
     * @return player ID (integer)
     */
    public int getPlayer() {
        return player;
    }

    /**
     * Returns the ship to be placed.
     * @return Ship object containing position and size
     */
    public Ship getShip() {
        return ship;
    }
}
