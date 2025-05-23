// src/model/GameState.java
package model;

import java.io.Serializable;

/**
 * GameState manages the overall game logic and state.
 * It tracks both players' boards, whose turn it is, and whether the game is over.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Board[] boards;         // Each player's board (index 0 and 1)
    private int[] shipsPlaced;      // Number of ships placed by each player
    private int currentPlayer;      // Whose turn it is (0 or 1)
    private boolean gameOver;       // Flag to indicate if game is over
    private int winner;             // The winning player's ID (-1 if not decided)

    /**
     * Constructs a new game state with initial settings.
     */
    public GameState() {
        boards = new Board[]{new Board(), new Board()};
        shipsPlaced = new int[]{0, 0};
        currentPlayer = 0;
        gameOver = false;
        winner = -1;
    }

    /**
     * Clears the specified player's board and resets their ship counter.
     * Called from the UI before randomized placement or reset.
     *
     * @param player the player ID (0 or 1)
     */
    public void clearBoard(int player) {
        boards[player] = new Board();
        shipsPlaced[player] = 0;
    }

    /**
     * Attempts to place a ship on the given player's board.
     * If successful, increments the ship placement counter.
     *
     * @param player the player ID (0 or 1)
     * @param ship the ship to be placed
     * @return true if the ship was successfully placed
     */
    public boolean placeShip(int player, Ship ship) {
        if (shipsPlaced[player] >= GameRules.numShips()) {
            return false; // All ships already placed
        }
        if (boards[player].placeShip(ship)) {
            shipsPlaced[player]++;
            return true;
        }
        return false;
    }

    /**
     * Fires at the opponent's board.
     * If the result is MISS, the turn switches to the opponent.
     * If all opponent ships are sunk, ends the game.
     *
     * @param pos the position to fire at
     * @return the result of the fire (HIT or MISS)
     */
    public Board.Cell fire(Position pos) {
        int opponent = 1 - currentPlayer;
        Board.Cell result = boards[opponent].fire(pos);

        if (result == Board.Cell.MISS) {
            currentPlayer = opponent; // Switch turn on miss
        }

        if (boards[opponent].allSunk()) {
            gameOver = true;
            winner = currentPlayer;
        }

        return result;
    }

    /**
     * Returns whether the game has ended.
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the ID of the player who won the game.
     */
    public int getWinner() {
        return winner;
    }

    /**
     * Returns the current player's ID (0 or 1).
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Sets the current player's turn.
     * Used in UI synchronization with server.
     */
    public void setCurrentPlayer(int player) {
        this.currentPlayer = player;
    }

    /**
     * Returns the number of ships placed by a specific player.
     */
    public int getShipsPlaced(int player) {
        return shipsPlaced[player];
    }

    /**
     * Returns the board associated with the given player.
     */
    public Board getBoard(int player) {
        return boards[player];
    }

    /**
     * Resets the entire game state to initial conditions.
     * Used for rematch or restart.
     */
    public void reset() {
        boards = new Board[]{new Board(), new Board()};
        shipsPlaced = new int[]{0, 0};
        currentPlayer = 0;
        gameOver = false;
        winner = -1;
    }
}
