package edu.uw.tcss.dungeoneer.model;

/**
 * The four cardinal directions used for doors and movement
 * inside the dungeon.
 *
 * @author Tarik Atasoy
 * @version Iteration 1
 */
public enum Direction {

    /** North (decreases the row index). */
    NORTH(-1, 0),

    /** South (increases the row index). */
    SOUTH(1, 0),

    /** East (increases the column index). */
    EAST(0, 1),

    /** West (decreases the column index). */
    WEST(0, -1);

    /** Row offset applied when moving in this direction. */
    private final int myRowOffset;

    /** Column offset applied when moving in this direction. */
    private final int myColOffset;

    /**
     * Creates a Direction with the given grid offsets.
     *
     * @param theRowOffset the row delta for this direction
     * @param theColOffset the column delta for this direction
     */
    Direction(final int theRowOffset, final int theColOffset) {
        myRowOffset = theRowOffset;
        myColOffset = theColOffset;
    }

    /** @return the row offset for this direction */
    public int getRowOffset() {
        return myRowOffset;
    }

    /** @return the column offset for this direction */
    public int getColOffset() {
        return myColOffset;
    }

    /**
     * Returns the direction opposite to this one. Used when carving
     * a door between two adjacent rooms so both sides agree.
     *
     * @return the opposite direction
     */
    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }
}
