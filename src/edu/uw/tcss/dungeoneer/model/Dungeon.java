package edu.uw.tcss.dungeoneer.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A 2D grid of Room objects that makes up the dungeon. The dungeon
 * also remembers the hero's current position and offers helper
 * methods for movement and traversability checks.
 * This class only stores the layout. Random generation of the maze,
 * items, pillars, entrance, and exit lives in DungeonBuilder.
 *
 * @author Tarik Atasoy
 * @version Iteration 4
 */
public class Dungeon implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Number of text rows used to draw a single room in the map.
     */
    private static final int ROOM_TEXT_HEIGHT = 3;

    /**
     * The grid of rooms.
     */
    private final Room[][] myRooms;

    /**
     * Number of rows in the grid.
     */
    private final int myRows;

    /**
     * Number of columns in the grid.
     */
    private final int myCols;

    /**
     * Hero's current row, or -1 if not yet placed.
     */
    private int myHeroRow;

    /**
     * Hero's current column, or -1 if not yet placed.
     */
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

    /**
     * Returns the number of rows in the dungeon grid.
     *
     * @return the row count of this dungeon
     */
    public int getRows() {
        return myRows;
    }

    /**
     * Returns the number of columns in the dungeon grid.
     *
     * @return the column count of this dungeon
     */
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

    /**
     * Returns the hero's current row index.
     *
     * @return the hero's row, or -1 if the hero has not been placed
     */
    public int getHeroRow() {
        return myHeroRow;
    }

    /**
     * Returns the hero's current column index.
     *
     * @return the hero's column, or -1 if the hero has not been placed
     */
    public int getHeroCol() {
        return myHeroCol;
    }

    /**
     * Returns the room at the hero's current position, or null if
     * the hero has not been placed yet. Primary accessor used by
     * movement, combat, and rendering code.
     *
     * @return the room the hero is currently in, or null
     */
    public Room getCurrentRoom() {
        if (myHeroRow < 0 || myHeroCol < 0) {
            return null;
        }
        return myRooms[myHeroRow][myHeroCol];
    }

    /**
     * Legacy alias for {@link #getCurrentRoom()}. Kept so older
     * controller code that calls getHeroRoom() keeps working; new
     * callers should prefer getCurrentRoom().
     *
     * @return the room the hero is currently in, or null if not placed
     */
    @Deprecated
    public Room getHeroRoom() {
        return getCurrentRoom();
    }

    /**
     * Returns the rooms immediately surrounding the hero on the
     * 3x3 patch centered on the current position (orthogonal and
     * diagonal neighbours). Cells outside the grid are skipped, so
     * a corner position yields 3 rooms, an edge position yields 5,
     * and an interior position yields 8.
     *
     * @return list of in-bounds neighbouring rooms (never null)
     */
    public List<Room> getSurroundingRooms() {
        final List<Room> surrounding = new ArrayList<>();
        final int[][] offsets = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        for (final int[] off : offsets) {
            final int targetRow = myHeroRow + off[0];
            final int targetCol = myHeroCol + off[1];
            if (inBounds(targetRow, targetCol)) {
                surrounding.add(myRooms[targetRow][targetCol]);
            }
        }
        return surrounding;
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
     * move only succeeds if the hero has been placed, the current
     * room has a door on that side, and the target room is inside
     * the grid.
     *
     * @param theDirection the direction to move in
     * @return true if the hero moved, false otherwise
     */
    public boolean moveHero(final Direction theDirection) {
        if (theDirection == null) {
            throw new IllegalArgumentException(
                    "Direction must not be null.");
        }
        final Room current = getCurrentRoom();
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
            // Each grid row is drawn ROOM_TEXT_HEIGHT text-rows tall.
            final String[] lines = new String[ROOM_TEXT_HEIGHT];
            for (int i = 0; i < ROOM_TEXT_HEIGHT; i++) {
                lines[i] = "";
            }
            for (int c = 0; c < myCols; c++) {
                final String[] roomLines =
                        myRooms[r][c].toString().split("\\R");
                for (int i = 0; i < ROOM_TEXT_HEIGHT; i++) {
                    lines[i] += roomLines[i];
                }
            }
            for (int i = 0; i < ROOM_TEXT_HEIGHT; i++) {
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
