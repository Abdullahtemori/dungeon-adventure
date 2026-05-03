package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Dungeon.
 * Covers grid construction, hero position, movement through doors,
 * blocked movement, and isTraversable.
 *
 * @author Tarik Atasoy
 * @version Iteration 1
 */
class DungeonTest {

    /** A small 3x3 dungeon used by most tests. */
    private Dungeon myDungeon;

    /**
     * Creates a fresh 3x3 empty dungeon before each test.
     */
    @BeforeEach
    void setUp() {
        myDungeon = new Dungeon(3, 3);
    }

    /**
     * Tests that the constructor stores the grid size and creates
     * a Room object at every position.
     */
    @Test
    void testConstructorFillsGrid() {
        assertEquals(3, myDungeon.getRows());
        assertEquals(3, myDungeon.getCols());

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                final Room room = myDungeon.getRoom(r, c);
                assertNotNull(room);
                assertEquals(r, room.getRow());
                assertEquals(c, room.getCol());
            }
        }
    }

    /**
     * Tests that a non-positive grid size is rejected.
     */
    @Test
    void testConstructorRejectsBadSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new Dungeon(0, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new Dungeon(5, -1));
    }

    /**
     * Tests that getRoom rejects out-of-bounds coordinates.
     */
    @Test
    void testGetRoomOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> myDungeon.getRoom(-1, 0));
        assertThrows(IndexOutOfBoundsException.class,
                () -> myDungeon.getRoom(0, 3));
    }

    /**
     * Tests that hero position starts unset and can be placed.
     */
    @Test
    void testHeroPosition() {
        assertEquals(-1, myDungeon.getHeroRow());
        assertEquals(-1, myDungeon.getHeroCol());
        assertNull(myDungeon.getHeroRoom());

        myDungeon.setHeroPosition(1, 2);
        assertEquals(1, myDungeon.getHeroRow());
        assertEquals(2, myDungeon.getHeroCol());
        assertSame(myDungeon.getRoom(1, 2), myDungeon.getHeroRoom());
    }

    /**
     * Tests that the hero can move through an open door to the
     * adjacent room.
     */
    @Test
    void testMoveHeroThroughDoor() {
        myDungeon.getRoom(0, 0).setDoor(Direction.EAST, true);
        myDungeon.getRoom(0, 1).setDoor(Direction.WEST, true);
        myDungeon.setHeroPosition(0, 0);

        assertTrue(myDungeon.moveHero(Direction.EAST));
        assertEquals(0, myDungeon.getHeroRow());
        assertEquals(1, myDungeon.getHeroCol());
    }

    /**
     * Tests that movement is blocked when there is no door.
     */
    @Test
    void testMoveHeroBlockedWhenNoDoor() {
        myDungeon.setHeroPosition(0, 0);

        assertFalse(myDungeon.moveHero(Direction.EAST));
        assertEquals(0, myDungeon.getHeroRow());
        assertEquals(0, myDungeon.getHeroCol());
    }

    /**
     * Tests that movement off the edge of the grid is blocked even
     * if a door is somehow open.
     */
    @Test
    void testMoveHeroBlockedAtEdge() {
        myDungeon.getRoom(0, 0).setDoor(Direction.NORTH, true);
        myDungeon.setHeroPosition(0, 0);

        assertFalse(myDungeon.moveHero(Direction.NORTH));
        assertEquals(0, myDungeon.getHeroRow());
        assertEquals(0, myDungeon.getHeroCol());
    }

    /**
     * Tests that moveHero returns false when the hero has not been
     * placed yet.
     */
    @Test
    void testMoveHeroBeforePlacement() {
        assertFalse(myDungeon.moveHero(Direction.EAST));
    }

    /**
     * Tests isTraversable for a hand-built dungeon where every
     * room is connected with open doors in a straight line.
     */
    @Test
    void testIsTraversableTrue() {
        myDungeon.getRoom(0, 0).setEntrance(true);
        myDungeon.getRoom(2, 2).setExit(true);

        // Path: (0,0) -> (0,1) -> (0,2) -> (1,2) -> (2,2)
        connect(myDungeon, 0, 0, Direction.EAST);
        connect(myDungeon, 0, 1, Direction.EAST);
        connect(myDungeon, 0, 2, Direction.SOUTH);
        connect(myDungeon, 1, 2, Direction.SOUTH);

        assertTrue(myDungeon.isTraversable());
    }

    /**
     * Tests isTraversable returns false when entrance and exit are
     * not connected by any chain of doors.
     */
    @Test
    void testIsTraversableFalseWhenDisconnected() {
        myDungeon.getRoom(0, 0).setEntrance(true);
        myDungeon.getRoom(2, 2).setExit(true);

        // No doors carved -> no path possible.
        assertFalse(myDungeon.isTraversable());
    }

    /**
     * Tests isTraversable returns false when the entrance or exit
     * is missing.
     */
    @Test
    void testIsTraversableFalseWhenMissingEndpoints() {
        // Only an entrance, no exit.
        myDungeon.getRoom(0, 0).setEntrance(true);
        assertFalse(myDungeon.isTraversable());
    }

    /**
     * Tests that toString contains lines for every grid row.
     */
    @Test
    void testToStringNotEmpty() {
        final String text = myDungeon.toString();
        assertNotNull(text);
        // Each grid row produces 3 text rows, plus trailing newlines.
        final String[] lines = text.split("\\R");
        assertEquals(myDungeon.getRows() * 3, lines.length);
    }

    /**
     * Helper that opens the door from (theRow, theCol) in the given
     * direction and the matching door on the other side so both
     * rooms agree.
     *
     * @param theDungeon   the dungeon to modify
     * @param theRow       starting row
     * @param theCol       starting column
     * @param theDirection direction to open
     */
    private static void connect(final Dungeon theDungeon,
                                final int theRow, final int theCol,
                                final Direction theDirection) {
        final Room from = theDungeon.getRoom(theRow, theCol);
        final Room to = theDungeon.getRoom(
                theRow + theDirection.getRowOffset(),
                theCol + theDirection.getColOffset());
        from.setDoor(theDirection, true);
        to.setDoor(theDirection.opposite(), true);
    }
}
