package edu.uw.tcss.dungeoneer.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * A single room inside the dungeon. A room knows its grid position,
 * which of its four walls have doors, whether it is the entrance or
 * exit, whether it holds a pit, and any items or a monster currently
 * inside it.
 *
 * The item fields and the pickUpItems method match what Hero's
 * inventory methods expect.
 *
 * @author Tarik Atasoy
 * @version Iteration 1
 */
public class Room {

    /** Row index of this room in the dungeon grid. */
    private final int myRow;

    /** Column index of this room in the dungeon grid. */
    private final int myCol;

    /** Doors keyed by direction. true = open door, false = wall. */
    private final Map<Direction, Boolean> myDoors;

    /** True if this room is the dungeon entrance. */
    private boolean myHasEntrance;

    /** True if this room is the dungeon exit. */
    private boolean myHasExit;

    /** True if this room contains a pit. */
    private boolean myHasPit;

    /** Damage dealt by the pit in this room (0 if no pit). */
    private int myPitDamage;

    /** Healing potion in the room, or null. */
    private HealingPotion myHealingPotion;

    /** Vision potion in the room, or null. */
    private VisionPotion myVisionPotion;

    /** Bomb in the room, or null. */
    private Bomb myBomb;

    /** Pillar in the room, or null. */
    private Pillar myPillar;

    /** Monster in the room, or null. */
    private Monster myMonster;

    /**
     * Constructs an empty room at the given grid position with no
     * doors, no items, no pit, and no monster.
     *
     * @param theRow the row index of this room
     * @param theCol the column index of this room
     */
    public Room(final int theRow, final int theCol) {
        myRow = theRow;
        myCol = theCol;
        myDoors = new EnumMap<>(Direction.class);
        for (final Direction d : Direction.values()) {
            myDoors.put(d, false);
        }
    }

    /** @return this room's row index */
    public int getRow() {
        return myRow;
    }

    /** @return this room's column index */
    public int getCol() {
        return myCol;
    }

    /**
     * Checks whether a door exists in the given direction.
     *
     * @param theDirection the direction to check
     * @return true if there is a door in that direction
     */
    public boolean hasDoor(final Direction theDirection) {
        return myDoors.get(theDirection);
    }

    /**
     * Opens or closes the door in the given direction.
     *
     * @param theDirection the direction of the door
     * @param theOpen      true to open, false to close
     */
    public void setDoor(final Direction theDirection, final boolean theOpen) {
        myDoors.put(theDirection, theOpen);
    }

    /** @return true if this room is the entrance */
    public boolean hasEntrance() {
        return myHasEntrance;
    }

    /**
     * Marks (or unmarks) this room as the entrance.
     *
     * @param theHasEntrance true to mark as entrance
     */
    public void setEntrance(final boolean theHasEntrance) {
        myHasEntrance = theHasEntrance;
    }

    /** @return true if this room is the exit */
    public boolean hasExit() {
        return myHasExit;
    }

    /**
     * Marks (or unmarks) this room as the exit.
     *
     * @param theHasExit true to mark as exit
     */
    public void setExit(final boolean theHasExit) {
        myHasExit = theHasExit;
    }

    /** @return true if this room has a pit */
    public boolean hasPit() {
        return myHasPit;
    }

    /** @return the damage dealt by this room's pit (0 if none) */
    public int getPitDamage() {
        return myPitDamage;
    }

    /**
     * Adds a pit to this room with the given damage value.
     *
     * @param theDamage the damage dealt by the pit
     */
    public void setPit(final int theDamage) {
        myHasPit = true;
        myPitDamage = theDamage;
    }

    /** Removes the pit from this room. */
    public void clearPit() {
        myHasPit = false;
        myPitDamage = 0;
    }

    /** @return true if this room contains a monster */
    public boolean hasMonster() {
        return myMonster != null;
    }

    /** @return the monster in this room, or null */
    public Monster getMonster() {
        return myMonster;
    }

    /**
     * Places the given monster in this room (or clears it if null).
     *
     * @param theMonster the monster to place, or null
     */
    public void setMonster(final Monster theMonster) {
        myMonster = theMonster;
    }

    /** @return the healing potion in this room, or null */
    public HealingPotion getHealingPotion() {
        return myHealingPotion;
    }

    /**
     * Places a healing potion in this room.
     *
     * @param thePotion the potion to place, or null to clear
     */
    public void setHealingPotion(final HealingPotion thePotion) {
        myHealingPotion = thePotion;
    }

    /** @return the vision potion in this room, or null */
    public VisionPotion getVisionPotion() {
        return myVisionPotion;
    }

    /**
     * Places a vision potion in this room.
     *
     * @param thePotion the potion to place, or null to clear
     */
    public void setVisionPotion(final VisionPotion thePotion) {
        myVisionPotion = thePotion;
    }

    /** @return the bomb in this room, or null */
    public Bomb getBomb() {
        return myBomb;
    }

    /**
     * Places a bomb in this room.
     *
     * @param theBomb the bomb to place, or null to clear
     */
    public void setBomb(final Bomb theBomb) {
        myBomb = theBomb;
    }

    /** @return the pillar in this room, or null */
    public Pillar getPillar() {
        return myPillar;
    }

    /**
     * Places a pillar in this room.
     *
     * @param thePillar the pillar to place, or null to clear
     */
    public void setPillar(final Pillar thePillar) {
        myPillar = thePillar;
    }

    /**
     * Returns true if the room is empty: no items, no pit, no
     * monster, and not flagged as the entrance or exit.
     *
     * @return true if the room has no contents at all
     */
    public boolean isEmpty() {
        return !myHasEntrance
                && !myHasExit
                && !myHasPit
                && myHealingPotion == null
                && myVisionPotion == null
                && myBomb == null
                && myPillar == null
                && myMonster == null;
    }

    /**
     * Checks the room for any collectible items and transfers them
     * to the hero's inventory. Once an item is picked up, it is
     * removed from the room so it cannot be collected again.
     *
     * Items that can be picked up:
     * HealingPotion: added to hero's healing potion count
     * VisionPotion: added to hero's vision potion count
     * Bomb: added to hero's bomb count
     * Pillar: added to hero's set of collected pillars
     *
     * @param theHero the hero who is entering and collecting items
     */
    public void pickUpItems(final Hero theHero) {

        // Check if the room contains a healing potion
        // If so, give it to the hero and remove it from the room
        if (myHealingPotion != null) {
            theHero.addHealingPotion();
            myHealingPotion = null;
        }

        // Check if the room contains a vision potion
        // If so, give it to the hero and remove it from the room
        if (myVisionPotion != null) {
            theHero.addVisionPotion();
            myVisionPotion = null;
        }

        // Check if the room contains a bomb
        // If so, give it to the hero and remove it from the room
        if (myBomb != null) {
            theHero.addBomb();
            myBomb = null;
        }

        // Check if the room contains a Pillar of OO
        // Pillars are automatically collected on room entry
        // The Set in Hero prevents duplicate pillars from being counted
        if (myPillar != null) {
            theHero.addPillar(myPillar);
            myPillar = null;
        }
    }

    /**
     * Returns the single character that represents the contents of
     * this room when drawn on the map. Entrance/exit win first, then
     * "multiple items" shows as 'M', then a pit, then whatever single
     * item the room holds.
     *
     * @return the display character for this room
     */
    private char getDisplayCharacter() {
        if (myHasEntrance) {
            return 'i';
        }
        if (myHasExit) {
            return 'O';
        }

        int itemCount = 0;
        if (myHealingPotion != null) {
            itemCount++;
        }
        if (myVisionPotion != null) {
            itemCount++;
        }
        if (myBomb != null) {
            itemCount++;
        }
        if (myPillar != null) {
            itemCount++;
        }

        if (itemCount > 1) {
            return 'M';
        }
        if (myHasPit) {
            return 'X';
        }
        if (myPillar != null) {
            return myPillar.getDisplayCharacter();
        }
        if (myHealingPotion != null) {
            return HealingPotion.DISPLAY_CHARACTER;
        }
        if (myVisionPotion != null) {
            return 'V';
        }
        if (myBomb != null) {
            return Bomb.DISPLAY_CHARACTER;
        }
        return ' ';
    }

    /**
     * Returns a 3-line, 3-character-wide ASCII drawing of this room.
     * Doors are shown as '-' (north/south) or '|' (east/west). Walls
     * are shown as '*'.
     *
     * @return the multi-line string drawing of this room
     */
    @Override
    public String toString() {
        final char north = hasDoor(Direction.NORTH) ? '-' : '*';
        final char south = hasDoor(Direction.SOUTH) ? '-' : '*';
        final char east = hasDoor(Direction.EAST) ? '|' : '*';
        final char west = hasDoor(Direction.WEST) ? '|' : '*';
        final char center = getDisplayCharacter();

        final StringBuilder sb = new StringBuilder();
        sb.append('*').append(north).append('*').append(System.lineSeparator());
        sb.append(west).append(center).append(east).append(System.lineSeparator());
        sb.append('*').append(south).append('*').append(System.lineSeparator());
        return sb.toString();
    }
}
