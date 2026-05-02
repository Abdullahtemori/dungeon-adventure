package edu.uw.tcss.dungeoneer.model;

import java.util.Random;

/**
 * Healling potion restores a random amount of hit points to the hero when used.
 * The heal amount is between 5 and 15 HP.
 *
 * @author Daniella Birungi
 * @version 1.0
 */
public class HealingPotion implements Item{

    /**
     * Minimum healing amount.
     */
    public static final int MIN_HEALING = 5;

    /**
     * Maximum healing amount.
     */
    public static final int MAX_HEALING = 15;

    /**
     * The display character for this item on dungeon map.
     */
    public static final char DISPLAY_CHARACTER = 'HP';

    /**
     * The amount of hit points this potion will heal.
     */
    private final int myHealAmount;

    /**
     * Constructs a HealingPotion with a randomly generated heal amount between
     * MIN_HEALING and MAX_HEALING inclusive.
     *
     * @param theHealAmount the amount of HP this potion heals
     */
    public HealingPotion(final int theHealAmount) {
        myHealAmount = theHealAmount;
    }

    /**
     * The amount of hit points this potion heals.
     *
     * @return the heal amount
     */
    public int getHealAmount() {
        return myHealAmount;
    }

    /**
     * The display character for healing potion.
     *
     * @return 'HP' for HealingPotion
     */
    @Override
    public char getDisplayCharacter() {
        return DISPLAY_CHARACTER;
    }

    /**
     * A description for this item.
     *
     * @return descrption of the healing potion
     */
    @Override
    public String getDescription() {
        return " Healing Potion: Restores " + myHealAmount + "hit points.";
    }

    /**
     * A string representation of this HealingPotion.
     *
     * @return a string describing this potion
     */
    @Override
    public String toString() {
        return "HealingPotion [myHealAmount=" + myHealAmount + "]";
    }
}