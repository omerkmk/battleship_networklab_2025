package common;

import java.io.Serializable;

/**
 * RematchRequest is sent from the client to the server
 * to indicate that the player wants to play another game.
 */
public class RematchRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int playerId; // ID of the player requesting a rematch

    /**
     * Constructs a RematchRequest with the given player ID.
     * @param playerId the ID of the player (0 or 1) who wants a rematch
     */
    public RematchRequest(int playerId) {
        this.playerId = playerId;
    }

    /**
     * Returns the ID of the player requesting the rematch.
     * @return player ID as an integer
     */
    public int getPlayerId() {
        return playerId;
    }
}
