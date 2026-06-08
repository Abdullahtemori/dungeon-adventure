package edu.uw.tcss.dungeoneer.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Ogre monster — slow and hard-hitting with a low heal chance.
 * The toughest monster in the dungeon by raw HP and damage output.
 * Stats:
 * HP: 200 | Speed: 2 | Hit Chance: 60% | Damage: 30–60
 * Heal Chance: 10% | Heal: 30–60
 *
 * @author Person 1, Abdullah Temori
 * @version Iteration 4
 */
public class Ogre extends Monster implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an Ogre with default stats.
     * No parameters are required because the Ogre's stats are fixed
     * by the game design and set here via the Monster constructor.
     */
    public Ogre() {
        super("Ogre", 200, 30, 60, 2, 0.6, 0.1, 30, 60);
    }

    /**
     * Returns a string representation of this Ogre.
     *
     * @return formatted string prefixed with "Ogre"
     */
    @Override
    public String toString() {
        return "Ogre | " + super.toString();
    }
}
