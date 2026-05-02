package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;

/**
 * This is the item interface representing any collectible item in the dungeon.
 * All the items must provide a display character and description
 *
 * @author Daniella Birungi
 * @version 1.0
 */
public interface Item extends Serializable {

    /**
     * The Character used to display this item on the dungeon map.
     *
     * @return the display character for this item
     */
    char getDisplayCharacter();

    /**
     * A description of this item
     *
     * @return the item description
     */
    String getDescription();
}