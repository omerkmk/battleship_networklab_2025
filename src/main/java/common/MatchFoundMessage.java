package common;

import java.io.Serializable;

/**
 * Sunucudan istemciye “eşleşme bulundu, oyun başlıyor” bildirimi.
 */
public class MatchFoundMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Bu istemcinin oyuncu numarası (0 veya 1) */
    private final int playerId;

    public MatchFoundMessage(int playerId) {
        this.playerId = playerId;
    }

    /** İstemcinin oyuncu numarasını döner */
    public int getPlayerId() {
        return playerId;
    }

    @Override
    public String toString() {
        return "MatchFoundMessage{playerId=" + playerId + '}';
    }
}
