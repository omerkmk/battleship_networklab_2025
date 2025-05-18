// src/common/TurnMessage.java
package common;

import java.io.Serializable;

public class TurnMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean yourTurn;
    public TurnMessage(boolean yourTurn) {
        this.yourTurn = yourTurn;
    }
    public boolean isYourTurn() { return yourTurn; }
}
