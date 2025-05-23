// src/common/ReadyRequest.java
package common;

import java.io.Serializable;

/**
 * ReadyRequest is sent from the client to the server
 * after all ships are placed, indicating that the player is ready to start the battle.
 */
public class ReadyRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int player; // ID of the player who is ready

    /**
     * Constructs a ReadyRequest with the specified player ID.
     * @param player the ID of the player (0 or 1)
     */
    public ReadyRequest(int player) {
        this.player = player;
    }

    /**
     * Returns the ID of the player who is ready.
     * @return the player's ID
     */
    public int getPlayer() {
        return player;
    }
}
