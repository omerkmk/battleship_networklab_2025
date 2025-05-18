package client;

import common.FireResponse;
import common.TurnMessage;
import common.GameOverMessage;
import model.GameRules;
import model.GameState;
import model.Position;
import model.Board.Cell;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Sadece savaş fazını yöneten UI.
 */
public class BattleUI extends JFrame {

    private static final int G = GameRules.GRID_SIZE;
    private static final Color WATER = new Color(173, 216, 230);
    private static final Border CELL_BORDER = BorderFactory.createLineBorder(Color.BLUE, 1);

    private final GameClient client;
    private final int myPlayer;
    private final GameState state;

    private final BoardPanel myPanel;
    private final BoardPanel oppPanel;
    private final JLabel infoLabel = new JLabel();

    public BattleUI(GameClient client, int myPlayer, GameState state, boolean yourTurn) {
        super("Battle – Player " + (myPlayer + 1));
        this.client = client;
        this.myPlayer = myPlayer;
        this.state = state;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // Üstte bilgi etiketi
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(infoLabel, BorderLayout.NORTH);

        // Ortada iki tahta
        myPanel = new BoardPanel(myPlayer);
        oppPanel = new BoardPanel(1 - myPlayer);
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
        center.add(myPanel);
        center.add(oppPanel);
        add(center, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // İlk turu ayarla ve görünümü yenile
        state.setCurrentPlayer(yourTurn ? myPlayer : 1 - myPlayer);
        refreshView();
    }

    private void refreshView() {
        // Kendi tahtam: gemileri göster, tıklamayı kapat
        myPanel.clear();
        myPanel.revealShips(state.getBoard(myPlayer).getShips());
        myPanel.setButtonsEnabled(false);

        // Rakip tahtası: önce tüm atışları işaretle
        oppPanel.clear();
        for (int r = 0; r < G; r++) {
            for (int c = 0; c < G; c++) {
                Position p = new Position(r, c);
                Cell cell = state.getBoard(1 - myPlayer).getCell(p);
                if (cell == Cell.HIT || cell == Cell.MISS) {
                    oppPanel.markShot(p, cell);
                }
            }
        }

        // Sıra sende mi?
        boolean yourTurn = state.getCurrentPlayer() == myPlayer;
        oppPanel.setButtonsEnabled(yourTurn);
        infoLabel.setText(yourTurn ? "Your turn" : "Opponent's turn");
    }

    // -----------------------------------
    // GameClient’dan gelecek callback’ler
    // -----------------------------------
    public void handleFireResponse(FireResponse resp) {
        SwingUtilities.invokeLater(() -> {
            Position p = resp.getPosition();
            Cell r = resp.getResult();

            boolean iAttacked = (state.getCurrentPlayer() == myPlayer);

            // ① İşaretlemeyi koy
            if (iAttacked) {
                // saldıran kendi opponent paneline
                oppPanel.markShot(p, r);
            } else {
                // savunan kendi your-board’una
                myPanel.markShot(p, r);
            }

            // ② Hit/Miss mesajı
            if (iAttacked) {
                JOptionPane.showMessageDialog(
                        this,
                        r == Cell.HIT ? "You hit a ship!" : "You missed!",
                        r == Cell.HIT ? "Hit!" : "Miss!",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        r == Cell.HIT ? "Your ship was hit!" : "Opponent missed!",
                        r == Cell.HIT ? "Hit received" : "Missed by opponent",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            // **NOT:** Şimdi **state.fire(p)** çağırma ve **refreshView()** yapma!
            // Sadece server’dan gelecek TurnMessage’a kadar bekle
        });
    }

    public void handleTurnMessage(TurnMessage tm) {
        SwingUtilities.invokeLater(() -> {
            // ① Server’dan gelen “senin sıran mı” bilgisini state’e aktar
            boolean yourTurn = tm.isYourTurn();
            state.setCurrentPlayer(yourTurn ? myPlayer : 1 - myPlayer);

            // ② State’teki tüm atışları (önceden markShot’la işaretlenmiş hücreler de dahil)
            //     ve yeni turn durumunu ekrana yansıt
            refreshView();
        });
    }

    public void handleGameOverMessage(GameOverMessage gom) {
        SwingUtilities.invokeLater(() -> {
            String msg = (gom.getWinner() == myPlayer) ? "You win!" : "You lose!";
            JOptionPane.showMessageDialog(this, msg);
            System.exit(0);
        });
    }

    // -----------------------------------
    // İç sınıf: tek bir tahta (myPanel veya oppPanel)
    // -----------------------------------
    private class BoardPanel extends JPanel {

        private final JButton[][] btns = new JButton[G][G];
        private final int panelPlayer;

        BoardPanel(int panelPlayer) {
            this.panelPlayer = panelPlayer;
            setBorder(BorderFactory.createTitledBorder(
                    CELL_BORDER, "Player " + (panelPlayer + 1),
                    TitledBorder.CENTER, TitledBorder.TOP));
            setLayout(new GridLayout(G, G));

            for (int r = 0; r < G; r++) {
                for (int c = 0; c < G; c++) {
                    JButton b = new JButton();
                    b.setPreferredSize(new Dimension(35, 35));
                    b.setBackground(WATER);
                    b.setOpaque(true);
                    b.setBorder(CELL_BORDER);

                    Position p = new Position(r, c);
                    b.putClientProperty("pos", p);
                    b.addActionListener(e -> {
                        // Sadece rakip tahtasını ve eğer sıra bende ise tıkla
                        if (panelPlayer == (1 - myPlayer)
                                && state.getCurrentPlayer() == myPlayer) {
                            client.sendFire(p);
                        }
                    });

                    btns[r][c] = b;
                    add(b);
                }
            }
        }

        void clear() {
            for (int r = 0; r < G; r++) {
                for (int c = 0; c < G; c++) {
                    JButton b = btns[r][c];
                    b.setText("");
                    b.setBackground(WATER);
                    b.setBorder(CELL_BORDER);
                    b.setEnabled(true);
                }
            }
        }

        void revealShips(java.util.List<model.Ship> ships) {
            for (model.Ship s : ships) {
                for (Position p : s.getPositions()) {
                    JButton b = btns[p.getRow()][p.getCol()];
                    b.setBackground(Color.DARK_GRAY);
                    b.setEnabled(false);
                }
            }
        }

        /**
         * Hücreye vurulduysa kalıcı kırmızı, ıskaladıysa O harfiyle işaretler.
         */
        void markShot(Position p, Cell res) {
            JButton b = btns[p.getRow()][p.getCol()];
            if (res == Cell.HIT) {
                b.setBackground(Color.RED);               // vurulan gemiyi kırmızıya boyar
                b.setText("X");
                b.setFont(new Font("SansSerif", Font.BOLD, 18));
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(WATER);                   // ıskalama hâlinde su renginde bırak
                b.setText("O");
                b.setFont(new Font("SansSerif", Font.BOLD, 18));
                b.setForeground(new Color(0, 0, 128));
            }
            b.setEnabled(false);
        }

        void setButtonsEnabled(boolean enabled) {
            for (int r = 0; r < G; r++) {
                for (int c = 0; c < G; c++) {
                    if (btns[r][c].isEnabled()) {
                        btns[r][c].setEnabled(enabled);
                    }
                }
            }
        }
    }
}
