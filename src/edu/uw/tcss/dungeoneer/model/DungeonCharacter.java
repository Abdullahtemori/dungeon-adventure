package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;

/**
 * Abstract base class for all dungeon characters (heroes and monsters).
 * Contains shared fields and behaviors such as attacking and HP management.
 *
 * @author Person 1
 * @version Iteration 1
 */
public abstract class DungeonCharacter implements Serializable {
    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /** The character's name. */
    private String myName;

    /** The character's current hit points. */
    private int myHitPoints;

    /** Minimum damage this character can deal. */
    private int myMinDamage;

    /** Maximum damage this character can deal. */
    private int myMaxDamage;

    /** Attack speed (higher = faster). */
    private int myAttackSpeed;

    /** Probability of landing an attack (0.0 to 1.0). */
    private double myChanceToHit;

    /**
     * Constructs a DungeonCharacter with the given stats.
     *
     * @param theName       character name
     * @param theHP         starting hit points
     * @param theMinDmg     minimum damage per attack
     * @param theMaxDmg     maximum damage per attack
     * @param theSpeed      attack speed
     * @param theChanceToHit probability to hit (0.0–1.0)
     */
    protected DungeonCharacter(final String theName, final int theHP,
                                final int theMinDmg, final int theMaxDmg,
                                final int theSpeed, final double theChanceToHit) {
        myName = theName;
        myHitPoints = theHP;
        myMinDamage = theMinDmg;
        myMaxDamage = theMaxDmg;
        myAttackSpeed = theSpeed;
        myChanceToHit = theChanceToHit;
    }

    /**
     * Attacks an opponent. If the attack hits (based on chance to hit),
     * random damage in [minDamage, maxDamage] is applied to the opponent.
     * Returns a CombatEvent describing what happened so the view can
     * render it; the model itself does no I/O.
     *
     * Note: this method does not check the opponent's block chance.
     * Block resolution is handled by the Combat orchestrator before
     * calling this method, since only Heroes block.
     *
     * @param theOpponent the character being attacked
     * @return a CombatEvent (ATTACK_HIT or ATTACK_MISS) describing the result
     */
    public CombatEvent attack(final DungeonCharacter theOpponent) {
        if (Math.random() < myChanceToHit) {
            final int damage = myMinDamage
                    + (int) (Math.random() * (myMaxDamage - myMinDamage + 1));
            theOpponent.setHitPoints(theOpponent.getHitPoints() - damage);
            return new CombatEvent(CombatEvent.Type.ATTACK_HIT,
                    myName, theOpponent.getName(), damage);
        }
        return new CombatEvent(CombatEvent.Type.ATTACK_MISS,
                myName, theOpponent.getName(), 0);
    }

    /** @return the character's name */
    public String getName() { return myName; }

    /** @return current hit points */
    public int getHitPoints() { return myHitPoints; }

    /** @param theHP new hit points value */
    public void setHitPoints(final int theHP) { myHitPoints = theHP; }

    /** @return minimum damage */
    public int getMinDamage() { return myMinDamage; }

    /** @return maximum damage */
    public int getMaxDamage() { return myMaxDamage; }

    /** @return attack speed */
    public int getAttackSpeed() { return myAttackSpeed; }

    /** @return chance to hit */
    public double getChanceToHit() { return myChanceToHit; }

    /** @return true if character has more than 0 HP */
    public boolean isAlive() { return myHitPoints > 0; }

    /**
     * Returns a string representation of this character.
     *
     * @return formatted character info
     */
    @Override
    public String toString() {
        return "Name: " + myName + " | HP: " + myHitPoints;
    }
}
