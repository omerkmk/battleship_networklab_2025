// src/common/FireResponse.java
package common;

import model.Position;
import model.Board.Cell;
import java.io.Serializable;

public class FireResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Position position;
    private final Cell result;
    public FireResponse(Position position, Cell result) {
        this.position = position;
        this.result   = result;
    }
    public Position getPosition() { return position; }
    public Cell getResult()       { return result;   }
}
