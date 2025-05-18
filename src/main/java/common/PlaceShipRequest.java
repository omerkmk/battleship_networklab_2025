// src/common/PlaceShipRequest.java
package common;

import model.Ship;
import java.io.Serializable;

public class PlaceShipRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int player;
    private final Ship ship;
    public PlaceShipRequest(int player, Ship ship) {
        this.player = player;
        this.ship   = ship;
    }
    public int getPlayer() { return player; }
    public Ship getShip()  { return ship;   }
}
