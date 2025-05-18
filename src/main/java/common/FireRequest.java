// src/common/FireRequest.java
package common;

import model.Position;
import java.io.Serializable;

public class FireRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Position position;
    public FireRequest(Position position) {
        this.position = position;
    }
    public Position getPosition() { return position; }
}
