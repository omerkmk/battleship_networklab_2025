// src/model/GameState.java
package model;

import java.io.Serializable;

/**
 * Oyun akışını ve her iki oyuncunun tahtalarını yöneten sınıf.
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Board[] boards;
    private int[] shipsPlaced;
    private int currentPlayer;
    private boolean gameOver;
    private int winner;

    public GameState() {
        boards = new Board[]{new Board(), new Board()};
        shipsPlaced = new int[]{0, 0};
        currentPlayer = 0;
        gameOver = false;
        winner = -1;
    }

    /**
     * Belirli bir oyuncunun tahtasını ve sayaçını sıfırlar. Randomize öncesi
     * UI’den çağrılır.
     */
    public void clearBoard(int player) {
        boards[player] = new Board();
        shipsPlaced[player] = 0;
    }

    /**
     * player için gemi yerleştirmeyi dener. Başarılıysa sayaç artar.
     */
    public boolean placeShip(int player, Ship ship) {
        if (shipsPlaced[player] >= GameRules.numShips()) {
            return false;
        }
        if (boards[player].placeShip(ship)) {
            shipsPlaced[player]++;
            return true;
        }
        return false;
    }

    /**
     * currentPlayer dışındaki oyuncuya ateş eder. HIT ise sıra aynı kalır, MISS
     * ise sıra diğerine geçer. Eğer tüm gemiler batmışsa oyunu sonlandırır.
     */
    public Board.Cell fire(Position pos) {
        int opponent = 1 - currentPlayer;
        Board.Cell result = boards[opponent].fire(pos);

        if (result == Board.Cell.MISS) {
            currentPlayer = opponent;
        }

        if (boards[opponent].allSunk()) {
            gameOver = true;
            winner = currentPlayer;
        }

        return result;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getWinner() {
        return winner;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * UI’nin doğrudan sıra bilgisini set etmek için (handleTurnMessage ile)
     */
    public void setCurrentPlayer(int player) {
        this.currentPlayer = player;
    }

    public int getShipsPlaced(int player) {
        return shipsPlaced[player];
    }

    public Board getBoard(int player) {
        return boards[player];
    }

    /**
     * Tüm oyun durumunu baştan başlatır.
     */
    public void reset() {
        boards = new Board[]{new Board(), new Board()};
        shipsPlaced = new int[]{0, 0};
        currentPlayer = 0;
        gameOver = false;
        winner = -1;
    }

}
