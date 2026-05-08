package edu.uw.tcss.dungeoneer.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * GameModel is the central data container for the Dungeon Adventure game.
 * It holds all game state including the dungeon layout, the hero, the
 * selected difficulty, and win/lose flags.
 *
 * GameModel is part of the Model layer in the MVC pattern. It does not
 * contain game logic — that belongs in GameController. It notifies
 * registered listeners (the View) whenever state changes using the
 * Observer pattern via PropertyChangeSupport.
 *
 * GameModel implements Serializable so the entire game state can be
 * saved to a file and loaded back later.
 *
 * @author Daniella Birungi
 * @version Iteration 2
 */
public class GameModel implements Serializable {
    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The dungeon grid containing all rooms, items and monsters.
     * This is the entire map of the current game.
     */
    private Dungeon myDungeon;

    /**
     * The hero the player is controlling.
     * Holds HP, inventory, pillars found, and all hero stats.
     */
    private Hero myHero;

    /**
     * The difficulty level chosen at the start of the game.
     * Affects dungeon size, monster strength, and item spawn rates.
     */
    private Difficulty myDifficulty;

    /**
     * Flag that becomes true when the game ends (win or lose).
     * GameController checks this after every action.
     */
    private boolean myGameOver;

    /**
     * Flag that becomes true when the player wins the game.
     * Only meaningful when MyGameOver is also true.
     */
    private boolean myPlayerWon;

    /**
     * PropertyChangeSupport handles notifying the View when model
     * state changes. It is marked as transient because it cannot
     * be serialized. It will be rebuilt when the game is loaded.
     */
    private transient PropertyChangeSupport myPcs;

    /**
     * Constructs a GameModel with the given dungeon, hero, and difficulty.
     * Initializes game flags to their default starting values.
     *
     * @param theDungeon the generated dungeon for this game session
     * @param theHero the hero the player selected
     * @param theDifficulty the difficulty level chosen by the player
     */
    public GameModel(final Dungeon theDungeon,
                     final Hero theHero,
                     final Difficulty theDifficulty) {
        myDungeon = theDungeon;
        myHero = theHero;
        myDifficulty = theDifficulty;

        //Game always starts with these flags as false
        myGameOver = false;
        myPlayerWon = false;

        //Initialize the observer support system
        myPcs = new PropertyChangeSupport(this);
    }

    /**
     * Returns the dungeon for this game session.
     *
     * @return the current dungeon
     */
    public Dungeon getDungeon() {
        return myDungeon;
    }

    /**
     * Returns the hero the player is controlling.
     *
     * @return the current hero
     */
    public Hero getHero() {
        return myHero;
    }

    /**
     * Returns the difficulty level for this game session.
     *
     * @return the selected difficulty
     */
    public Difficulty getDifficulty() {
        return myDifficulty;
    }
    /**
     * Returns whether the game has ended.
     *
     * @return true if the game is over, false if still in progress
     */
    public boolean isGameOver() {
        return myGameOver;
    }

    /**
     * Returns whether the player has won.
     * Should only be checked when isGameOver() returns true.
     *
     * @return true if the player won, false if they lost
     */
    public boolean isPlayerWon() {
        return myPlayerWon;
    }

    /**
     * Sets the game over flag and notifies all registered listeners.
     * Called by GameController when the hero dies or wins.
     *
     * @param theVal true to mark the game as over
     */
    public void setGameOver(final boolean theVal) {
        // Save the old value so the event carries both old and new
        final boolean oldVal = myGameOver;
        myGameOver = theVal;

        // Notify the View that gameOver has changed
        // The View can then show a game over or victory screen
        myPcs.firePropertyChange("gameOver", oldVal, theVal);
    }

    /**
     * Sets the player won flag and notifies all registered listeners.
     * Called by GameController when the player collects all four pillars
     * and reaches the exit room.
     *
     * @param theVal true to mark the player as having won
     */
    public void setPlayerWon(final boolean theVal) {
        final boolean oldVal = myPlayerWon;
        myPlayerWon = theVal;

        // Notify the View so it can display the victory screen
        myPcs.firePropertyChange("playerWon", oldVal, theVal);
    }

    /**
     * Updates the dungeon reference.
     * Used when starting a new game without creating a new GameModel.
     *
     * @param theDungeon the new dungeon to set
     */
    public void setDungeon(final Dungeon theDungeon) {
        final Dungeon oldVal = myDungeon;
        myDungeon = theDungeon;
        myPcs.firePropertyChange("dungeon", oldVal, theDungeon);
    }

    /**
     * Updates the hero reference.
     * Used when the player selects a new hero.
     *
     * @param theHero the new hero to set
     */
    public void setHero(final Hero theHero) {
        final Hero oldVal = myHero;
        myHero = theHero;
        myPcs.firePropertyChange("hero", oldVal, theHero);
    }

    /**
     * Registers a listener to receive property change notifications.
     * The View (ConsoleView or SwingView) calls this on startup so
     * it can update the display whenever the model changes.
     *
     * @param theListener the listener to register
     */
    public void addPropertyChangeListener(
            final PropertyChangeListener theListener) {
        // Rebuild myPcs if it was lost during deserialization
        if (myPcs == null) {
            myPcs = new PropertyChangeSupport(this);
        }
        myPcs.addPropertyChangeListener(theListener);
    }

    /**
     * Removes a previously registered listener.
     * Called when the View is being torn down or replaced.
     *
     * @param theListener the listener to remove
     */
    public void removePropertyChangeListener(
            final PropertyChangeListener theListener) {
        if (myPcs != null) {
            myPcs.removePropertyChangeListener(theListener);
        }
    }

    /**
     * Restores the PropertyChangeSupport after deserialization.
     * Because myPcs is transient it is not saved to the file.
     * Java calls this method automatically after loading a save file
     * so that listeners can be registered again.
     *
     * @return this GameModel with myPcs rebuilt
     */
    private Object readResolve() {
        myPcs = new PropertyChangeSupport(this);
        return this;
    }

    /**
     * Returns a string summary of the current game state.
     *
     * @return formatted game state string
     */
    @Override
    public String toString() {
        return "GameModel{"
                + "difficulty=" + myDifficulty
                + ", gameOver=" + myGameOver
                + ", playerWon=" + myPlayerWon
                + ", hero=" + (myHero != null ? myHero.getName() : "none")
                + "}";
    }


}
