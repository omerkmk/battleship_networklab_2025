package common;

import java.io.Serializable;

/**
 * RematchStatusMessage is sent from the server to both clients
 * indicating whether both players have agreed to start a rematch.
 */
public class RematchStatusMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean bothAgreed; // True if both players accepted rematch

    /**
     * Constructs a RematchStatusMessage with the agreement status.
     * @param bothAgreed true if both players want a rematch, false otherwise
     */
    public RematchStatusMessage(boolean bothAgreed) {
        this.bothAgreed = bothAgreed;
    }

    /**
     * Returns whether both players have agreed to play again.
     * @return true if both players agreed to rematch
     */
    public boolean isBothAgreed() {
        return bothAgreed;
    }
}
