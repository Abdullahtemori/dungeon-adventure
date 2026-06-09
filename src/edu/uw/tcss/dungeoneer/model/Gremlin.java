package edu.uw.tcss.dungeoneer.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Gremlin monster — the fastest monster, heals frequently, and deals
 * light damage. Its high heal chance (40 %) makes it deceptively
 * durable despite its low HP pool.
 * Stats:
 * HP: 70 | Speed: 5 | Hit Chance: 80% | Damage: 15–30
 * Heal Chance: 40% | Heal: 20–40
 *
 * @author Person 1, Abdullah Temori
 * @version Iteration 4
 */
public class Gremlin extends Monster implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a Gremlin with default stats.
     * No parameters are required because the Gremlin's stats are fixed
     * by the game design and set here via the Monster constructor.
     */
    public Gremlin() {
        super("Gremlin", 70, 15, 30, 5, 0.8, 0.4, 20, 40);
    }

    /**
     * Returns a string representation of this Gremlin.
     *
     * @return formatted string prefixed with "Gremlin"
     */
    @Override
    public String toString() {
        return "Gremlin | " + super.toString();
    }
}
