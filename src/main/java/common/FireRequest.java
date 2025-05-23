// src/common/FireRequest.java
package common;

import model.Position;
import java.io.Serializable;

/**
 * FireRequest is sent from the client to the server
 * to indicate a shot fired at a specific position on the board.
 */
public class FireRequest implements Serializable {
    private static final long serialVersionUID = 1L; // Version ID for serialization

    private final Position position; // The position to fire at

    /**
     * Constructs a FireRequest with the given position.
     * @param position the position the player wants to fire at
     */
    public FireRequest(Position position) {
        this.position = position;
    }

    /**
     * Returns the targeted position of this fire request.
     * @return Position object representing the target coordinates
     */
    public Position getPosition() {
        return position;
    }
}
