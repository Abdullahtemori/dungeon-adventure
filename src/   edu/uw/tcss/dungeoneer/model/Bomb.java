package edu.uw.tcss.dungeoneer.model;

import java.util.Random;

/**
 * Bomb deals massive damage to a single monster when used in combat.
 * Damages range between 75 and 150 HP. The bomb is consumed
 * on use regardless of if it hits.
 *
 * @author Daniella Birungi
 * @version 1.0
 */
public class Bomb implements Item{

    /**
     * Minimum damage a bomb can deal.
     */
    public static final int MIN_DAMAGE = 75;

    /**
     * Maximum damage a bomb can deal.
     */
    public static final int MAX_DAMAGE = 150;

    /**
     * The display character for this item on dungeon map.
     */
    public static final char DISPLAY_CHARACTER = 'B';

    /**
     * The amount of damage this bomb deals.
     */
    private final int myDamage;

    /**
     * Constructs a Bomb with a randomly generated damaga between
     * MIN_DAMAGE and MAX_DAMAGE inclusive.
     */
    public Bomb() {
        final Random rand = new Random();
        myDamage = rand.nextInt(MAX_DAMAGE - MIN_DAMAGE + 1) + MIN_DAMAGE;
    }

    /**
     * Constructs a Bomb with a specific damage value.
     * Used for testing purposes.
     *
     *  @param theDamage the amount of damge this bomb deals
     */

    public Bomb(final int theDamage) {
        myDamage = theDamage;
    }

    /**
     * The damage this bomb deals.
     *
     * @return the heal amount
     */
    public int getDamage() {
        return myDamage;
    }

    /**
     * The display character for bomb.
     *
     * @return 'B' for bomb
     */
    @Override
    public char getDisplayCharacter() {
        return DISPLAY_CHARACTER;
    }

    /**
     * A description for this item.
     *
     * @return descrption of the bomb
     */
    @Override
    public String getDescription() {
        return " Bomb: Deals " + myDamage + " damage to a single monster in combat.";
    }

    /**
     * A string representation of this Bomb.
     *
     * @return a string describing this bomb
     */
    @Override
    public String toString() {
        return "Bomb [myDamage=" + myDamage + "]";
    }

}