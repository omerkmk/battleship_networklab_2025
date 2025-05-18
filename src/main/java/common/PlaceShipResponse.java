// src/common/PlaceShipResponse.java
package common;

import java.io.Serializable;

public class PlaceShipResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean success;
    public PlaceShipResponse(boolean success) {
        this.success = success;
    }
    public boolean isSuccess() { return success; }
}
