package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;

/**
 * Abstract class representing a Monster in the dungeon.
 * Monsters can heal themselves after taking damage.
 *
 * @author Person 1
 * @version Iteration 1
 */
public abstract class Monster extends DungeonCharacter implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /** Probability that this monster heals after being hit. */
    private double myChanceToHeal;

    /** Minimum HP restored when healing. */
    private int myMinHeal;

    /** Maximum HP restored when healing. */
    private int myMaxHeal;

    /**
     * Constructs a Monster with all required stats.
     *
     * @param theName         monster name
     * @param theHP           starting hit points
     * @param theMinDmg       minimum damage
     * @param theMaxDmg       maximum damage
     * @param theSpeed        attack speed
     * @param theChanceToHit  chance to hit (0.0–1.0)
     * @param theHealChance   chance to heal after being hit (0.0–1.0)
     * @param theMinHeal      minimum heal amount
     * @param theMaxHeal      maximum heal amount
     */
    protected Monster(final String theName, final int theHP,
                      final int theMinDmg, final int theMaxDmg,
                      final int theSpeed, final double theChanceToHit,
                      final double theHealChance,
                      final int theMinHeal, final int theMaxHeal) {
        super(theName, theHP, theMinDmg, theMaxDmg, theSpeed, theChanceToHit);
        myChanceToHeal = theHealChance;
        myMinHeal = theMinHeal;
        myMaxHeal = theMaxHeal;
    }

    /**
     * Attempts to heal after taking damage. Only triggers if the
     * monster is still alive (HP > 0). Returns a CombatEvent if the
     * heal succeeded, or null if the monster did not heal this time.
     * Returning null instead of throwing keeps the call site (Combat)
     * simple, it just adds non-null events to its log.
     *
     * @return a MONSTER_HEAL event if a heal occurred, otherwise null
     */
    public CombatEvent heal() {
        if (isAlive() && Math.random() < myChanceToHeal) {
            final int amount = myMinHeal
                    + (int) (Math.random() * (myMaxHeal - myMinHeal + 1));
            setHitPoints(getHitPoints() + amount);
            return new CombatEvent(CombatEvent.Type.MONSTER_HEAL,
                    getName(), getName(), amount);
        }
        return null;
    }

    /** @return chance to heal */
    public double getChanceToHeal() { return myChanceToHeal; }

    /** @return minimum heal amount */
    public int getMinHeal() { return myMinHeal; }

    /** @return maximum heal amount */
    public int getMaxHeal() { return myMaxHeal; }

    @Override
    public String toString() {
        return "Monster | " + super.toString();
    }
}
