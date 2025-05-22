package common;

import java.io.Serializable;

/**
 * Oyuncunun yeniden oynamak istediğini belirtir.
 */
public class RematchRequest implements Serializable {
    private final int playerId;

    public RematchRequest(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }
}
