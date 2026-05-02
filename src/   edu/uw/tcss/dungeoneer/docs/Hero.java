package edu.uw.tcss.dungeoneer.model;

/**
 * Abstract class representing a Hero in the dungeon.
 * Heroes can block attacks and have a unique special skill.
 *
 * @author Person 1
 * @version Iteration 1
 */
public abstract class Hero extends DungeonCharacter {

    /** Probability that this hero blocks an incoming attack (0.0–1.0). */
    private double myChanceToBlock;

    /**
     * Constructs a Hero with all required stats.
     *
     * @param theName         hero's name
     * @param theHP           starting hit points
     * @param theMinDmg       minimum damage
     * @param theMaxDmg       maximum damage
     * @param theSpeed        attack speed
     * @param theChanceToHit  chance to hit (0.0–1.0)
     * @param theChanceToBlock chance to block (0.0–1.0)
     */
    protected Hero(final String theName, final int theHP,
                   final int theMinDmg, final int theMaxDmg,
                   final int theSpeed, final double theChanceToHit,
                   final double theChanceToBlock) {
        super(theName, theHP, theMinDmg, theMaxDmg, theSpeed, theChanceToHit);
        myChanceToBlock = theChanceToBlock;
    }

    /**
     * Each hero has a unique special skill.
     *
     * @param theOpponent the target of the special skill
     */
    public abstract void specialSkill(final DungeonCharacter theOpponent);

    /**
     * Attempts to block an incoming attack.
     *
     * @return true if the block succeeds
     */
    public boolean block() {
        return Math.random() < myChanceToBlock;
    }

    /** @return the chance to block value */
    public double getChanceToBlock() { return myChanceToBlock; }

    /**
     * Returns hero info as a string.
     *
     * @return formatted hero info
     */
    @Override
    public String toString() {
        return super.toString() + " | Block Chance: " + myChanceToBlock;
    }
}
