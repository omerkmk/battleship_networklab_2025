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
import model.Ship;

/**
 * Sadece savaş fazını yöneten estetik, koyu temalı UI (güncellendi gemi vurgusu ve rematch özelliği).
 */
public class BattleUI extends JFrame {

    private static final int G = GameRules.GRID_SIZE;
    // Güncellenmiş renk paleti
    private static final Color WATER       = Color.decode("#0D1B2A");
    private static final Color EMPTY_CELL  = Color.decode("#BDC3C7");
    private static final Color SHIP_COLOR  = Color.decode("#C0392B");
    private static final Color BORDER_COL  = Color.decode("#2C3E50");
    private static final Color HIT_COLOR   = Color.decode("#D72631");
    private static final Color MISS_COLOR  = Color.decode("#57C7F4");
    private static final Color SUNK_COLOR  = Color.decode("#6C2DC7");

    private final GameClient client;
    private final int myPlayer;
    private final GameState state;

    private final BoardPanel myPanel;
    private final BoardPanel oppPanel;
    private final JLabel infoLabel = new JLabel();
    private final JButton rematchButton = new JButton("↻ Rematch");

    public BattleUI(GameClient client, int myPlayer, GameState state, boolean yourTurn) {
        super("Battle – Player " + (myPlayer + 1));
        this.client = client;
        this.myPlayer = myPlayer;
        this.state = state;

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(WATER);
        setLayout(new BorderLayout(10, 10));

        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        myPanel  = new BoardPanel(myPlayer);
        oppPanel = new BoardPanel(1 - myPlayer);
        JPanel center = new JPanel(new GridLayout(1, 2, 20, 20));
        center.setOpaque(false);
        center.add(myPanel);
        center.add(oppPanel);
        add(center, BorderLayout.CENTER);

        // Rematch button init
        rematchButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rematchButton.setVisible(false);
        rematchButton.setEnabled(false);
        rematchButton.addActionListener(e -> {
            client.sendRematchRequest();
            rematchButton.setEnabled(false);
            rematchButton.setText("Bekleniyor...");
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(rematchButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        pack(); setResizable(false); setLocationRelativeTo(null); setVisible(true);

        state.setCurrentPlayer(yourTurn ? myPlayer : 1 - myPlayer);
        refreshView();
    }

    private void refreshView() {
        myPanel.revealShips(state.getBoard(myPlayer).getShips());
        myPanel.setButtonsEnabled(false);

        for (int r = 0; r < G; r++) for (int c = 0; c < G; c++) {
            Position p = new Position(r, c);
            Cell cell = state.getBoard(1 - myPlayer).getCell(p);
            if (cell != Cell.EMPTY) oppPanel.markShot(p, cell);
        }

        boolean yourTurn = state.getCurrentPlayer() == myPlayer;
        oppPanel.setButtonsEnabled(yourTurn);
        infoLabel.setText(yourTurn ? "YOUR TURN" : "OPPONENT'S TURN");
    }

    public void handleFireResponse(FireResponse resp) {
        SwingUtilities.invokeLater(() -> {
            Position p = resp.getPosition();
            Cell result = resp.getResult();
            boolean iAttacked = state.getCurrentPlayer() == myPlayer;
            int defender = iAttacked ? 1 - myPlayer : myPlayer;

            state.getBoard(defender).setCell(p, result);
            if (iAttacked) oppPanel.markShot(p, result);
            else myPanel.markShot(p, result);

            if (result == Cell.HIT) {
                for (Ship ship : state.getBoard(defender).getShips()) {
                    boolean sunk = ship.getPositions().stream()
                            .allMatch(pos -> state.getBoard(defender).getCell(pos) == Cell.HIT);
                    if (sunk) ship.getPositions().forEach(oppPanel::markSunk);
                }
            }
        });
    }

    public void handleTurnMessage(TurnMessage tm) {
        SwingUtilities.invokeLater(() -> {
            boolean yourTurn = tm.isYourTurn();
            state.setCurrentPlayer(yourTurn ? myPlayer : 1 - myPlayer);
            oppPanel.setButtonsEnabled(yourTurn);
            infoLabel.setText(yourTurn ? "YOUR TURN" : "OPPONENT'S TURN");
        });
    }

    public void handleGameOverMessage(GameOverMessage gom) {
        SwingUtilities.invokeLater(() -> {
            String msg = gom.getWinner() == myPlayer ? "YOU WIN!" : "YOU LOSE!";
            infoLabel.setText("GAME OVER – " + msg);

            // Rematch butonunu aktif et
            rematchButton.setVisible(true);
            rematchButton.setEnabled(true);
        });
    }

    private class BoardPanel extends JPanel {
        private final JButton[][] buttons = new JButton[G][G];
        private final int player;

        BoardPanel(int player) {
            this.player = player;
            setOpaque(false);
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COL, 2),
                player == myPlayer ? "Your Board" : "Opponent Board",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16), Color.WHITE));
            setLayout(new GridLayout(G, G, 2, 2));
            initButtons();
        }

        private void initButtons() {
            for (int r = 0; r < G; r++) for (int c = 0; c < G; c++) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(38, 38));
                b.setBackground(EMPTY_CELL);
                b.setOpaque(true);
                b.setBorder(BorderFactory.createLineBorder(BORDER_COL, 2));
                Position pos = new Position(r, c);
                b.putClientProperty("pos", pos);
                b.addActionListener(e -> {
                    if (player != myPlayer && state.getCurrentPlayer() == myPlayer) {
                        client.sendFire(pos);
                    }
                });
                buttons[r][c] = b;
                add(b);
            }
        }

        void markShot(Position p, Cell cell) {
            JButton b = buttons[p.getRow()][p.getCol()];
            if (cell == Cell.HIT) {
                b.setBackground(HIT_COLOR);
                b.setText("X");
            } else if (cell == Cell.MISS) {
                b.setBackground(MISS_COLOR);
                b.setText("O");
            }
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Segoe UI", Font.BOLD, 16));
            b.setBorder(BorderFactory.createLineBorder(BORDER_COL, 2));
            b.setEnabled(false);
        }

        void markSunk(Position p) {
            JButton b = buttons[p.getRow()][p.getCol()];
            b.setBackground(SUNK_COLOR);
            b.setText("\u2693");
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
            b.setBorder(BorderFactory.createLineBorder(BORDER_COL, 3));
            b.setEnabled(false);
        }

        void revealShips(java.util.List<Ship> ships) {
            for (Ship ship : ships) for (Position p : ship.getPositions()) {
                JButton b = buttons[p.getRow()][p.getCol()];
                b.setBackground(SHIP_COLOR);
                b.setText("\u2693");
                b.setForeground(Color.WHITE);
                b.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
                b.setBorder(BorderFactory.createLineBorder(SHIP_COLOR.darker(), 3));
                b.setEnabled(false);
            }
        }

        void setButtonsEnabled(boolean enabled) {
            for (int r = 0; r < G; r++) for (int c = 0; c < G; c++) {
                JButton b = buttons[r][c];
                if (b.getText().isEmpty()) b.setEnabled(enabled);
            }
        }
    }
}
