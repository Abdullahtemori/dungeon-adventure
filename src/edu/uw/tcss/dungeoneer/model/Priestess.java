package edu.uw.tcss.dungeoneer.model;

/**
 * Priestess hero — lower damage but can heal herself.
 *
 * Stats:
 *   HP: 75 | Speed: 5 | Hit Chance: 70% | Damage: 25–45 | Block: 30%
 *
 * @author Person 1
 * @version Iteration 1
 */
public class Priestess extends Hero {

    /** Minimum HP healed by special skill. */
    private static final int HEAL_MIN = 20;

    /** Maximum HP healed by special skill. */
    private static final int HEAL_MAX = 50;

    /**
     * Constructs a Priestess with the given name and default stats.
     *
     * @param theName the priestess's name
     */
    public Priestess(final String theName) {
        super(theName, 75, 25, 45, 5, 0.7, 0.3);
    }

    /**
     * Heal — restores 20–50 HP to self.
     *
     * @param theOpponent not used for healing (heals self)
     */
    @Override
    public void specialSkill(final DungeonCharacter theOpponent) {
        int heal = HEAL_MIN + (int) (Math.random() * (HEAL_MAX - HEAL_MIN + 1));
        setHitPoints(getHitPoints() + heal);
        System.out.println(getName() + " heals herself for " + heal + " HP!");
    }

    @Override
    public String toString() {
        return "Priestess | " + super.toString();
    }
}
