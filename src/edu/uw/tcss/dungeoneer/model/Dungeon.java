package edu.uw.tcss.dungeoneer.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * A 2D grid of Room objects that makes up the dungeon. The dungeon
 * also remembers the hero's current position and offers helper
 * methods for movement and traversability checks.
 *
 * This class only stores the layout. Random generation of the maze,
 * items, pillars, entrance, and exit lives in DungeonBuilder.
 *
 * @author Tarik Atasoy
 * @version Iteration 1
 */
public class Dungeon {

    /** The grid of rooms. */
    private final Room[][] myRooms;

    /** Number of rows in the grid. */
    private final int myRows;

    /** Number of columns in the grid. */
    private final int myCols;

    /** Hero's current row, or -1 if not yet placed. */
    private int myHeroRow;

    /** Hero's current column, or -1 if not yet placed. */
    private int myHeroCol;

    /**
     * Builds an empty dungeon of the given size. Each cell gets a
     * fresh Room with no doors and no contents.
     *
     * @param theRows number of rows (must be positive)
     * @param theCols number of columns (must be positive)
     */
    public Dungeon(final int theRows, final int theCols) {
        if (theRows <= 0 || theCols <= 0) {
            throw new IllegalArgumentException(
                    "Dungeon size must be positive.");
        }
        myRows = theRows;
        myCols = theCols;
        myRooms = new Room[theRows][theCols];
        for (int r = 0; r < theRows; r++) {
            for (int c = 0; c < theCols; c++) {
                myRooms[r][c] = new Room(r, c);
            }
        }
        myHeroRow = -1;
        myHeroCol = -1;
    }

    /** @return number of rows */
    public int getRows() {
        return myRows;
    }

    /** @return number of columns */
    public int getCols() {
        return myCols;
    }

    /**
     * Returns the room at the given coordinates.
     *
     * @param theRow row index
     * @param theCol column index
     * @return the Room at that position
     */
    public Room getRoom(final int theRow, final int theCol) {
        if (!inBounds(theRow, theCol)) {
            throw new IndexOutOfBoundsException(
                    "Room position out of bounds: " + theRow + "," + theCol);
        }
        return myRooms[theRow][theCol];
    }

    /** @return the hero's current row, or -1 if not placed */
    public int getHeroRow() {
        return myHeroRow;
    }

    /** @return the hero's current column, or -1 if not placed */
    public int getHeroCol() {
        return myHeroCol;
    }

    /** @return the room the hero is currently in, or null if not placed */
    public Room getHeroRoom() {
        if (myHeroRow < 0 || myHeroCol < 0) {
            return null;
        }
        return myRooms[myHeroRow][myHeroCol];
    }

    /**
     * Places the hero at the given coordinates. Used by the builder
     * to drop the hero on the entrance.
     *
     * @param theRow the row to place the hero on
     * @param theCol the column to place the hero on
     */
    public void setHeroPosition(final int theRow, final int theCol) {
        if (!inBounds(theRow, theCol)) {
            throw new IndexOutOfBoundsException(
                    "Hero position out of bounds: " + theRow + "," + theCol);
        }
        myHeroRow = theRow;
        myHeroCol = theCol;
    }

    /**
     * Tries to move the hero one step in the given direction. The
     * move only succeeds if the current room has a door on that
     * side and the target room is inside the grid.
     *
     * @param theDirection the direction to move in
     * @return true if the hero moved, false otherwise
     */
    public boolean moveHero(final Direction theDirection) {
        final Room current = getHeroRoom();
        if (current == null || !current.hasDoor(theDirection)) {
            return false;
        }
        final int newRow = myHeroRow + theDirection.getRowOffset();
        final int newCol = myHeroCol + theDirection.getColOffset();
        if (!inBounds(newRow, newCol)) {
            return false;
        }
        myHeroRow = newRow;
        myHeroCol = newCol;
        return true;
    }

    /**
     * Checks whether the dungeon can be traversed from the entrance
     * room to the exit room by walking only through open doors.
     * Uses breadth-first search.
     *
     * @return true if a path exists from the entrance to the exit
     */
    public boolean isTraversable() {
        final Room entrance = findRoom(true);
        final Room exit = findRoom(false);
        if (entrance == null || exit == null) {
            return false;
        }

        final Deque<Room> queue = new ArrayDeque<>();
        final Set<Room> visited = new HashSet<>();
        queue.add(entrance);
        visited.add(entrance);

        while (!queue.isEmpty()) {
            final Room curr = queue.poll();
            if (curr == exit) {
                return true;
            }
            for (final Direction d : Direction.values()) {
                if (!curr.hasDoor(d)) {
                    continue;
                }
                final int nr = curr.getRow() + d.getRowOffset();
                final int nc = curr.getCol() + d.getColOffset();
                if (!inBounds(nr, nc)) {
                    continue;
                }
                final Room next = myRooms[nr][nc];
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return false;
    }

    /**
     * Returns a multi-line string showing every room in the dungeon
     * stacked into one map. Handy for debugging and for the hidden
     * "show full dungeon" menu option.
     *
     * @return a textual map of the entire dungeon
     */
    @Override
    public String toString() {
        final String nl = System.lineSeparator();
        final StringBuilder sb = new StringBuilder();
        for (int r = 0; r < myRows; r++) {
            // Each row is three text-rows tall.
            final String[] lines = {"", "", ""};
            for (int c = 0; c < myCols; c++) {
                final String[] roomLines = myRooms[r][c].toString().split("\\R");
                for (int i = 0; i < 3; i++) {
                    lines[i] += roomLines[i];
                }
            }
            for (int i = 0; i < 3; i++) {
                sb.append(lines[i]).append(nl);
            }
        }
        return sb.toString();
    }

    /**
     * Returns true if the given coordinates are inside the grid.
     *
     * @param theRow row index
     * @param theCol column index
     * @return true if in bounds
     */
    private boolean inBounds(final int theRow, final int theCol) {
        return theRow >= 0 && theRow < myRows
                && theCol >= 0 && theCol < myCols;
    }

    /**
     * Finds the unique entrance or exit room.
     *
     * @param theFindEntrance true to look for the entrance, false for the exit
     * @return the matching room, or null if none
     */
    private Room findRoom(final boolean theFindEntrance) {
        for (int r = 0; r < myRows; r++) {
            for (int c = 0; c < myCols; c++) {
                final Room room = myRooms[r][c];
                if (theFindEntrance ? room.hasEntrance() : room.hasExit()) {
                    return room;
                }
            }
        }
        return null;
    }
}
