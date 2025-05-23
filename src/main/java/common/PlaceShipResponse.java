// src/common/PlaceShipResponse.java
package common;

import java.io.Serializable;

/**
 * PlaceShipResponse is sent from the server to the client
 * as a response to a PlaceShipRequest.
 * Indicates whether the ship was placed successfully.
 */
public class PlaceShipResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success; // True if the ship placement was valid

    /**
     * Constructs a new response indicating success or failure.
     * @param success true if the ship was placed successfully, false otherwise
     */
    public PlaceShipResponse(boolean success) {
        this.success = success;
    }

    /**
     * Returns whether the ship placement was successful.
     * @return true if placement is valid, false if invalid
     */
    public boolean isSuccess() {
        return success;
    }
}
