package common;

import java.io.Serializable;

/**
 * Her iki oyuncunun da yeniden oynamayı kabul edip etmediğini bildirir.
 */
public class RematchStatusMessage implements Serializable {
    private final boolean bothAgreed;

    public RematchStatusMessage(boolean bothAgreed) {
        this.bothAgreed = bothAgreed;
    }

    public boolean isBothAgreed() {
        return bothAgreed;
    }
}
