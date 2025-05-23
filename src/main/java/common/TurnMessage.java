// src/common/TurnMessage.java
package common;

import java.io.Serializable;

/**
 * TurnMessage is sent from the server to the client
 * to indicate whether it's this client's turn to make a move.
 */
public class TurnMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean yourTurn; // True if it's the client's turn

    /**
     * Constructs a TurnMessage indicating turn ownership.
     * @param yourTurn true if it's the client's turn, false otherwise
     */
    public TurnMessage(boolean yourTurn) {
        this.yourTurn = yourTurn;
    }

    /**
     * Returns whether it is currently this client's turn.
     * @return true if it's your turn, false if it's the opponent's
     */
    public boolean isYourTurn() {
        return yourTurn;
    }
}
