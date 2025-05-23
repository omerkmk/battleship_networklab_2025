// src/common/FireResponse.java
package common;

import model.Position;
import model.Board.Cell;
import java.io.Serializable;

/**
 * FireResponse is sent from the server to the client
 * after a fire request, indicating the result of the shot.
 */
public class FireResponse implements Serializable {
    private static final long serialVersionUID = 1L; // For Java serialization consistency

    private final Position position;  // Position that was targeted
    private final Cell result;        // Result of the shot (e.g., HIT, MISS, SUNK)

    /**
     * Constructs a response with the position fired at and the outcome.
     * @param position The position the player fired at
     * @param result The result of the fire (HIT, MISS, or SUNK)
     */
    public FireResponse(Position position, Cell result) {
        this.position = position;
        this.result   = result;
    }

    /**
     * Gets the position that was targeted.
     * @return the Position object
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Gets the result of the shot at the given position.
     * @return the Cell enum value representing the result
     */
    public Cell getResult() {
        return result;
    }
}
