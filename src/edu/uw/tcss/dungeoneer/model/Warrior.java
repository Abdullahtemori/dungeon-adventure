package edu.uw.tcss.dungeoneer.model;

/**
 * Warrior hero — high HP and damage, special skill is Crushing Blow.
 *
 * Stats:
 *   HP: 125 | Speed: 4 | Hit Chance: 80% | Damage: 35–60 | Block: 20%
 *
 * @author Person 1
 * @version Iteration 1
 */
public class Warrior extends Hero {

    /** Minimum damage for Crushing Blow. */
    private static final int CRUSH_MIN = 75;

    /** Maximum damage for Crushing Blow. */
    private static final int CRUSH_MAX = 175;

    /** Chance Crushing Blow succeeds. */
    private static final double CRUSH_CHANCE = 0.4;

    /**
     * Constructs a Warrior with the given name and default stats.
     *
     * @param theName the warrior's name
     */
    public Warrior(final String theName) {
        super(theName, 125, 35, 60, 4, 0.8, 0.2);
    }

    /**
     * Crushing Blow — 40% chance to deal 75–175 damage.
     *
     * @param theOpponent the target
     */
    @Override
    public void specialSkill(final DungeonCharacter theOpponent) {
        if (Math.random() < CRUSH_CHANCE) {
            int damage = CRUSH_MIN + (int) (Math.random() * (CRUSH_MAX - CRUSH_MIN + 1));
            theOpponent.setHitPoints(theOpponent.getHitPoints() - damage);
            System.out.println(getName() + " lands a Crushing Blow for " + damage + " damage!");
        } else {
            System.out.println(getName() + "'s Crushing Blow missed!");
        }
    }

    @Override
    public String toString() {
        return "Warrior | " + super.toString();
    }
}
