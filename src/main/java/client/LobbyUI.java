package client;

import common.MatchFoundMessage;
import javax.swing.*;
import java.awt.*;

public class LobbyUI extends JFrame {
    private final String host;
    private final int port;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;

    public LobbyUI(String host, String portStr) {
        super("Battleship – Rakip Bekleniyor");
        this.host = host;
        this.port = Integer.parseInt(portStr);

        // 1) Modern görünüm için Nimbus L&F uygula
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Nimbus yoksa varsayılan L&F kullanılacak
        }

        // 2) Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(30, 30, 30));

        // 3) Başlık
        JLabel titleLabel = new JLabel("Rakip aranıyor...");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 4) Status bilgi
        statusLabel = new JLabel(host + ":" + port, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        mainPanel.add(statusLabel, BorderLayout.CENTER);

        // 5) İlerleme göstergesi
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        progressBar.setBackground(new Color(50, 50, 50));
        progressBar.setForeground(new Color(70, 130, 180));
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        // 6) Pencere ayarları
        setContentPane(mainPanel);
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        // 7) Matchmaking başlat
        startMatchmaking();
    }

    private void startMatchmaking() {
        new Thread(() -> {
            try {
                GameClient client = new GameClient(host, port, this);
                client.start();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                        "Sunucuya bağlanılamadı:\n" + ex.getMessage(),
                        "Bağlantı Hatası",
                        JOptionPane.ERROR_MESSAGE)
                );
            }
        }, "Matchmaking-Thread").start();
    }

    public void onMatchFound(int playerId, GameClient client) {
        SwingUtilities.invokeLater(() -> {
            // Geçiş için kısa animasyon verebiliriz
            progressBar.setIndeterminate(false);
            progressBar.setValue(progressBar.getMaximum());

            dispose();  // Lobi kapat
            client.setUI(new GameUI(client, playerId));
        });
    }
}