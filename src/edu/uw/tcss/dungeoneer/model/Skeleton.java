package edu.uw.tcss.dungeoneer.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Skeleton monster — balanced stats with a moderate heal chance.
 * A mid-tier threat: faster and more accurate than an Ogre, but
 * less durable than one.
 * Stats:
 *   HP: 100 | Speed: 3 | Hit Chance: 80% | Damage: 30–50
 *   Heal Chance: 30% | Heal: 30–50
 *
 * @author Person 1, Abdullah Temori
 * @version Iteration 4
 */
public class Skeleton extends Monster implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a Skeleton with default stats.
     * No parameters are required because the Skeleton's stats are fixed
     * by the game design and set here via the Monster constructor.
     */
    public Skeleton() {
        super("Skeleton", 100, 30, 50, 3, 0.8, 0.3, 30, 50);
    }

    /**
     * Returns a string representation of this Skeleton.
     *
     * @return formatted string prefixed with "Skeleton"
     */
    @Override
    public String toString() {
        return "Skeleton | " + super.toString();
    }
}
