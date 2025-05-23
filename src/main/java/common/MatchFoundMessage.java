package common;

import java.io.Serializable;

/**
 * MatchFoundMessage is sent from the server to the client
 * to indicate that a match has been found and the game is about to start.
 */
public class MatchFoundMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The player ID assigned to this client (0 or 1) */
    private final int playerId;

    /**
     * Constructs a new MatchFoundMessage with the given player ID.
     * @param playerId the player number (0 or 1) assigned by the server
     */
    public MatchFoundMessage(int playerId) {
        this.playerId = playerId;
    }

    /**
     * Returns the player ID assigned to this client.
     * @return integer value representing the player's index (0 or 1)
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Returns a string representation of this message.
     * @return a string including the player ID
     */
    @Override
    public String toString() {
        return "MatchFoundMessage{playerId=" + playerId + '}';
    }
}
