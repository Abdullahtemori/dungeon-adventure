package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;

/**
 * Skeleton monster — balanced stats with moderate heal chance.
 *
 * Stats:
 *   HP: 100 | Speed: 3 | Hit Chance: 80% | Damage: 30–50
 *   Heal Chance: 30% | Heal: 30–50
 *
 * @author Person 1
 * @version Iteration 1
 */
public class Skeleton extends Monster implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a Skeleton with default stats.
     */
    public Skeleton() {
        super("Skeleton", 100, 30, 50, 3, 0.8, 0.3, 30, 50);
    }

    @Override
    public String toString() {
        return "Skeleton | " + super.toString();
    }
}
