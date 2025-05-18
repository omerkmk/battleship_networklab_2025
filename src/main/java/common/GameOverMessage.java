// src/common/GameOverMessage.java
package common;

import java.io.Serializable;

public class GameOverMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int winner;
    public GameOverMessage(int winner) {
        this.winner = winner;
    }
    public int getWinner() { return winner; }
}
