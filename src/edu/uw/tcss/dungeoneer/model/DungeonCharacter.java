package edu.uw.tcss.dungeoneer.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Abstract base class for all dungeon characters (heroes and monsters).
 * Contains shared fields and behaviours such as attacking and HP management.
 * All concrete character types (Hero subclasses and Monster subclasses)
 * extend this class and inherit its attack logic and stat accessors.
 *
 * @author Abdullah Temori
 * @author Daniella Birungi
 * @version Iteration 4
 */
public abstract class DungeonCharacter implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The character's name.
     */
    private final String myName;

    /**
     * The character's current hit points.
     */
    private int myHitPoints;

    /**
     * Minimum damage this character can deal per attack.
     */
    private final int myMinDamage;

    /**
     * Maximum damage this character can deal per attack.
     */
    private final int myMaxDamage;

    /**
     * Attack speed (higher value means faster).
     */
    private final int myAttackSpeed;

    /**
     * Probability of landing an attack (0.0 to 1.0).
     */
    private final double myChanceToHit;

    /**
     * Constructs a DungeonCharacter with the given stats.
     *
     * @param theName        the character's display name
     * @param theHP          starting hit points
     * @param theMinDmg      minimum damage per attack
     * @param theMaxDmg      maximum damage per attack
     * @param theSpeed       attack speed
     * @param theChanceToHit probability to hit per swing (0.0–1.0)
     */
    protected DungeonCharacter(final String theName, final int theHP,
                               final int theMinDmg, final int theMaxDmg,
                               final int theSpeed,
                               final double theChanceToHit) {
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
     * Note: this method does not check the opponent's block chance.
     * Block resolution is handled by the Combat orchestrator before
     * calling this method, since only Heroes can block.
     *
     * @param theOpponent the character being attacked; must not be null
     * @return a CombatEvent of type ATTACK_HIT if the attack landed,
     * or ATTACK_MISS if it did not
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

    /**
     * Returns the character's display name.
     *
     * @return the name of this character
     */
    public String getName() {
        return myName;
    }

    /**
     * Returns the character's current hit points.
     *
     * @return current HP (always >= 0)
     */
    public int getHitPoints() {
        return myHitPoints;
    }

    /**
     * Sets the character's current hit points.
     * Values below zero are clamped to zero so HP never goes negative.
     *
     * @param theHP the new hit points value
     */
    public void setHitPoints(final int theHP) {
        myHitPoints = Math.max(0, theHP);
    }

    /**
     * Returns the minimum damage this character deals per attack.
     *
     * @return minimum damage value
     */
    public int getMinDamage() {
        return myMinDamage;
    }

    /**
     * Returns the maximum damage this character deals per attack.
     *
     * @return maximum damage value
     */
    public int getMaxDamage() {
        return myMaxDamage;
    }

    /**
     * Returns the character's attack speed.
     * Higher values allow more swings per combat round relative to slower
     * opponents; see Combat.attacksPerRound() for the formula.
     *
     * @return attack speed (positive integer)
     */
    public int getAttackSpeed() {
        return myAttackSpeed;
    }

    /**
     * Returns the probability that this character's attack lands.
     *
     * @return chance to hit, in the range 0.0 (never) to 1.0 (always)
     */
    public double getChanceToHit() {
        return myChanceToHit;
    }

    /**
     * Returns whether this character is still alive.
     *
     * @return true if current HP is greater than zero, false otherwise
     */
    public boolean isAlive() {
        return myHitPoints > 0;
    }

    /**
     * Returns a string representation of this character's name and HP.
     *
     * @return formatted character info string
     */
    @Override
    public String toString() {
        return "Name: " + myName + " | HP: " + myHitPoints;
    }
}
