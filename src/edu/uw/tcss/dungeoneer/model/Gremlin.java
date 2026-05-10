package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;

/**
 * Gremlin monster — fast and heals frequently.
 *
 * Stats:
 *   HP: 70 | Speed: 5 | Hit Chance: 80% | Damage: 15–30
 *   Heal Chance: 40% | Heal: 20–40
 *
 * @author Person 1
 * @version Iteration 1
 */
public class Gremlin extends Monster implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a Gremlin with default stats.
     */
    public Gremlin() {
        super("Gremlin", 70, 15, 30, 5, 0.8, 0.4, 20, 40);
    }

    @Override
    public String toString() {
        return "Gremlin | " + super.toString();
    }
}
