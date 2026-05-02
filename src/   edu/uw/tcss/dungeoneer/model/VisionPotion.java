package edu.uw.tcss.dungeoneer.model;

/**
 * VisionPotion reveals the contents of the eight rooms surrounding the
 * hero's current position.
 *
 * @author daniella Birungi
 * @version 1.0
 */
public class VisionPotion implements Item{

    /**
     * Display character for this item on the dungeon map.
     */
    private static final char DISPLAY_CHARACTER = 'V';

    /**
     * Constructs a VisionPotion
     */
    public VisionPotion() {

    }

    /**
     * The display character for this item.
     *
     * @return 'V' for VisionPotion
     */
    @Override
    public char getDisplayCharacter() {
        return DISPLAY_CHARACTER;
    }

    /**
     * A description of this item
     *
     * @return description of the vision potion
     */
    @Override
    public String getDescription() {
        return "Vision Potion: Reveals the contents of all surrounding rooms";
    }

    /**
     *  A string representation of thois vision potion
     *
     * @return striung describing this potion
     */
    @Override
    public String toString() {
        return "Vision Potion{}";
    }
}