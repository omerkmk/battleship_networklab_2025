// src/common/ReadyRequest.java
package common;

import java.io.Serializable;

/**
 * İstemci, tüm gemilerini yerleştirdikten sonra
 * savaşa geçmek üzere hazır olduğunu bildirir.
 */
public class ReadyRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int player;
    public ReadyRequest(int player) { this.player = player; }
    public int getPlayer() { return player; }
}
