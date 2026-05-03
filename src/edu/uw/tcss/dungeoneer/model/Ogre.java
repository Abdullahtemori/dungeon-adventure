package edu.uw.tcss.dungeoneer.model;

/**
 * Ogre monster — slow but hits hard. Low heal chance.
 *
 * Stats:
 *   HP: 200 | Speed: 2 | Hit Chance: 60% | Damage: 30–60
 *   Heal Chance: 10% | Heal: 30–60
 *
 * @author Person 1
 * @version Iteration 1
 */
public class Ogre extends Monster {

    /**
     * Constructs an Ogre with default stats.
     */
    public Ogre() {
        super("Ogre", 200, 30, 60, 2, 0.6, 0.1, 30, 60);
    }

    @Override
    public String toString() {
        return "Ogre | " + super.toString();
    }
}
