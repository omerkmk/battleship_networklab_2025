// src/common/GameOverMessage.java
package common;

import java.io.Serializable;

/**
 * GameOverMessage is sent from the server to both clients
 * when the game ends, indicating which player has won.
 */
public class GameOverMessage implements Serializable {
    private static final long serialVersionUID = 1L; // Ensures consistent serialization

    private final int winner; // ID of the winning player (0 or 1)

    /**
     * Constructs a GameOverMessage indicating the winning player.
     * @param winner the ID of the winner (typically 0 or 1)
     */
    public GameOverMessage(int winner) {
        this.winner = winner;
    }

    /**
     * Returns the ID of the winning player.
     * @return int value representing the winner's player ID
     */
    public int getWinner() {
        return winner;
    }
}
