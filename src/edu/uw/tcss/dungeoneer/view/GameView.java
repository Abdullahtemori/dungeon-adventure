package edu.uw.tcss.dungeoneer.view;

import edu.uw.tcss.dungeoneer.model.Dungeon;
import edu.uw.tcss.dungeoneer.model.Hero;
import edu.uw.tcss.dungeoneer.model.Monster;
import edu.uw.tcss.dungeoneer.model.Room;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * GameView defines the contract for all views in the Dungeon Adventure game.
 * It is part of the View layer in the MVC pattern.
 * Any class that wants to display game information to the player must
 * implement this interface. This allows GameController to work with
 * any view implementation (console, Swing GUI, etc.) without knowing
 * which one is being used.
 * GameView extends PropertyChangeListener so that GameModel can notify
 * the view automatically whenever game state changes. The view must
 * implement propertyChange() to react to those notifications.
 *
 * @author Daniella Birungi
 * @version Iteration 3
 */
public interface GameView extends PropertyChangeListener {

    /**
     * Displays the contents and layout of a single room.
     * Called after the hero moves into a new room.
     *
     * @param theRoom the room to display
     */
    void displayRoom(Room theRoom);

    /**
     * Displays the entire dungeon map.
     * Called in cheat mode, at game over, and at victory.
     *
     * @param theDungeon the dungeon to display
     */
    void displayDungeon(Dungeon theDungeon);

    /**
     * Displays the contents of the rooms surrounding the hero.
     * Called when the hero uses a Vision Potion.
     *
     * @param theRooms the list of surrounding rooms to display
     */
    void displayVision(List<Room> theRooms);

    /**
     * Displays the current combat state between the hero and a monster.
     * Called at the start of combat and after each round.
     *
     * @param theHero    the hero engaged in combat
     * @param theMonster the monster engaged in combat
     */
    void displayCombat(Hero theHero, Monster theMonster);

    /**
     * Displays a message to the player.
     * Used for event feedback such as item pickup, damage taken,
     * doors blocked, and combat results.
     *
     * @param theMsg the message to display
     */
    void displayMessage(String theMsg);

    /**
     * Displays the hero's current stats.
     * Called after item use, damage, healing, or hero changes.
     *
     * @param theHero the hero whose stats to display
     */
    void displayHeroStats(Hero theHero);

    /**
     * Prompts the player to select a combat action.
     * Blocks execution until a valid selection is made.
     *
     * @return the selected HeroAction enum variant
     */
    edu.uw.tcss.dungeoneer.model.HeroAction promptHeroAction();

    /**
     * Translates a combat model event into visual or textual feedback.
     *
     * @param theEvent the event detailing actions, damage, or item use
     */
    void displayCombatEvent(edu.uw.tcss.dungeoneer.model.CombatEvent theEvent);

    /**
     * Notifies the view whether cheat mode is currently active so it can
     * show or hide any cheat-only display (such as the full dungeon map).
     * Views that have no cheat display may ignore this call.
     *
     * @param theCheatOn true if cheat mode is now on, false otherwise
     */
    default void setCheatMode(final boolean theCheatOn) {
        // Optional: views without a cheat display do nothing.
    }

}
