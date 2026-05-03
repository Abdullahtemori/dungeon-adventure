package edu.uw.tcss.dungeoneer.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class representing a Hero in the dungeon.
 * Heroes can block attacks and have a unique special skill.
 *
 * @author Person 1, Daniella Birungi
 * @version Iteration 1
 */
public abstract class Hero extends DungeonCharacter {

    /** Probability that this hero blocks an incoming attack (0.0–1.0). */
    private double myChanceToBlock;

     /** Number of healing potions in the hero's inventory. */
    private int myHealingPotions;

    /** Number of vision potions in the hero's inventory. */
    private int myVisionPotions;

    /** Number of bombs in the hero's inventory. */
    private int myBombs;

    /** Set of pillars the hero has collected. */
    private Set<Pillar> myPillarsFound;

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

        myHealingPotions = 0;
        myVisionPotions = 0;
        myBombs = 0;
        myPillarsFound = new HashSet<>();
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
     * Returns the number of healing potions in the hero's inventory.
     *
     *@return healing potion count
     */
    public int getHealingPotions() {
        return myHealingPotions;
    }

     /**
     * Returns the number of vision potions in the hero's inventory.
     *
     *@return vision potion count
     */
    public int getVisionPotions() {
        return myVisionPotions;
    }

     /**
     * Returns the number of bombs in the hero's inventory.
     *
     *@return bomb count
     */
    public int getBombs() {
        return myBombs;
    }
    
    /**
     * Returns the set of pillars the hero has collected.
     *
     *@return set of collected pillars
     */
    public Set<Pillar> getPillarsFound() {
        return myPillarsFound;
    }

    /**
     * Adds one healing potion to the hero's inventory.
     */
    public void addHealingPotion() {
        myHealingPotions++;
    }

    /**
     * Adds one vision potion to the hero's inventory.
     */
    public void addVisionPotion() {
        myVisionPotions++;
    }

    /**
     * Adds one Bomb to the hero's inventory.
     */
    public void addBomb() {
        myBombs++;
    }

    /**
     * Adds a pillar to the hero's collected pillars set.
     * Duplicate pillars are ignored automatically by the set.
     *
     * @param thePillar the pillar to add
     */
    public void addPillar(final Pillar thePillar) {
        myPillarsFound.add(thePillar);
    }

    /**
     * Uses one healing potion if available.
     * Returns the amount healed, or 0 if none is available.
     *
     * @return HP healed, or 0 if no potions available
     */
    public int usehealingPotion(){
        if (myHealingPotions <= 0){
            return 0;
        }
        myHealingPotions--;
        final HealingPotion potion = new HealingPotion();
        return potion.getHealAmount();
    }

    /**
     * Uses one vision potion if available.
     * Returns true if successful, false if none is available.
     *
     * @return true if potion was used, false if none left
     */
    public boolean useVisionPotion(){
        if (myVisionPotions <= 0){
            return false;
        }
        myVisionPotions--;
        return true;
    }

    /**
     * Uses one bomb against a target monster if available.
     * Returns the damage dealt, or 0 if none is available.
     *
     * @param theTarget the monster to bomb
     * @return damage dealt, or 0 if no bombs are left
     */
    public int useBomb(final Monster theTarget){
        if (myBombs <= 0){
            return 0;
        }
        myBombs--;
        final Bomb bomb = new Bomb();
        final int  damage = bomb.getDamage();
        theTarget.setHitPoints(theTarget.getHitPoints() - damage);
        return damage;
    }
    /**
     * Returns hero info as a string.
     *
     * @return formatted hero info
     */
    @Override
    public String toString() {
        return super.toString() 
            + " | Block Chance: " + myChanceToBlock
            + " | Healing Potions: " + myHealingPotions
            + " | Vision Potions: " + myVisionPotions
            + " | Bombs: " + myBombs
            + " | Pillars Found: " + myPillarsFound;
        
    }
}
