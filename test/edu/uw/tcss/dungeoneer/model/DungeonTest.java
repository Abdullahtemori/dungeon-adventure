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
 * @version Iteration 3
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

    /**
     * Tests that moveHero succeeds northward when a door is present.
     */
    @Test
    void testMoveHeroNorth() {
        connect(myDungeon, 1, 1, Direction.NORTH);
        myDungeon.setHeroPosition(1, 1);
        assertTrue(myDungeon.moveHero(Direction.NORTH));
        assertEquals(0, myDungeon.getHeroRow());
        assertEquals(1, myDungeon.getHeroCol());
    }

    /**
     * Tests that moveHero succeeds southward when a door is present.
     */
    @Test
    void testMoveHeroSouth() {
        connect(myDungeon, 1, 1, Direction.SOUTH);
        myDungeon.setHeroPosition(1, 1);
        assertTrue(myDungeon.moveHero(Direction.SOUTH));
        assertEquals(2, myDungeon.getHeroRow());
        assertEquals(1, myDungeon.getHeroCol());
    }

    /**
     * Tests that moveHero succeeds eastward when a door is present.
     */
    @Test
    void testMoveHeroEast() {
        connect(myDungeon, 1, 1, Direction.EAST);
        myDungeon.setHeroPosition(1, 1);
        assertTrue(myDungeon.moveHero(Direction.EAST));
        assertEquals(1, myDungeon.getHeroRow());
        assertEquals(2, myDungeon.getHeroCol());
    }

    /**
     * Tests that moveHero succeeds westward when a door is present.
     */
    @Test
    void testMoveHeroWest() {
        connect(myDungeon, 1, 1, Direction.WEST);
        myDungeon.setHeroPosition(1, 1);
        assertTrue(myDungeon.moveHero(Direction.WEST));
        assertEquals(1, myDungeon.getHeroRow());
        assertEquals(0, myDungeon.getHeroCol());
    }

    /**
     * Tests that the hero cannot move past the north edge regardless
     * of whether a door is set.
     */
    @Test
    void testMoveHeroBlockedAtNorthEdge() {
        myDungeon.getRoom(0, 1).setDoor(Direction.NORTH, true);
        myDungeon.setHeroPosition(0, 1);
        assertFalse(myDungeon.moveHero(Direction.NORTH));
        assertEquals(0, myDungeon.getHeroRow());
        assertEquals(1, myDungeon.getHeroCol());
    }

    /**
     * Tests that the hero cannot move past the south edge.
     */
    @Test
    void testMoveHeroBlockedAtSouthEdge() {
        myDungeon.getRoom(2, 1).setDoor(Direction.SOUTH, true);
        myDungeon.setHeroPosition(2, 1);
        assertFalse(myDungeon.moveHero(Direction.SOUTH));
        assertEquals(2, myDungeon.getHeroRow());
        assertEquals(1, myDungeon.getHeroCol());
    }

    /**
     * Tests that the hero cannot move past the east edge.
     */
    @Test
    void testMoveHeroBlockedAtEastEdge() {
        myDungeon.getRoom(1, 2).setDoor(Direction.EAST, true);
        myDungeon.setHeroPosition(1, 2);
        assertFalse(myDungeon.moveHero(Direction.EAST));
        assertEquals(1, myDungeon.getHeroRow());
        assertEquals(2, myDungeon.getHeroCol());
    }

    /**
     * Tests that the hero cannot move past the west edge.
     */
    @Test
    void testMoveHeroBlockedAtWestEdge() {
        myDungeon.getRoom(1, 0).setDoor(Direction.WEST, true);
        myDungeon.setHeroPosition(1, 0);
        assertFalse(myDungeon.moveHero(Direction.WEST));
        assertEquals(1, myDungeon.getHeroRow());
        assertEquals(0, myDungeon.getHeroCol());
    }

    /**
     * Tests that moveHero rejects a null direction.
     */
    @Test
    void testMoveHeroRejectsNullDirection() {
        myDungeon.setHeroPosition(1, 1);
        assertThrows(IllegalArgumentException.class,
                () -> myDungeon.moveHero(null));
    }

    /**
     * Tests that getCurrentRoom returns null before the hero is
     * placed.
     */
    @Test
    void testGetCurrentRoomNullBeforePlacement() {
        assertNull(myDungeon.getCurrentRoom());
    }

    /**
     * Tests that getCurrentRoom returns the room at the hero's
     * position and stays in sync across several moves.
     */
    @Test
    void testGetCurrentRoomTracksHeroPosition() {
        connect(myDungeon, 0, 0, Direction.EAST);
        connect(myDungeon, 0, 1, Direction.SOUTH);
        myDungeon.setHeroPosition(0, 0);

        assertSame(myDungeon.getRoom(0, 0), myDungeon.getCurrentRoom());
        assertTrue(myDungeon.moveHero(Direction.EAST));
        assertSame(myDungeon.getRoom(0, 1), myDungeon.getCurrentRoom());
        assertTrue(myDungeon.moveHero(Direction.SOUTH));
        assertSame(myDungeon.getRoom(1, 1), myDungeon.getCurrentRoom());
    }

    /**
     * Tests that the legacy getHeroRoom() accessor returns the same
     * room as getCurrentRoom().
     */
    @Test
    void testGetHeroRoomMatchesGetCurrentRoom() {
        myDungeon.setHeroPosition(2, 0);
        assertSame(myDungeon.getCurrentRoom(), myDungeon.getHeroRoom());
    }

    /**
     * Tests that the hero cannot move through a wall (no door).
     */
    @Test
    void testHeroCannotMoveThroughWall() {
        myDungeon.setHeroPosition(1, 1);
        for (final Direction dir : Direction.values()) {
            assertFalse(myDungeon.moveHero(dir),
                    "Hero should not pass through wall in " + dir);
        }
        assertEquals(1, myDungeon.getHeroRow());
        assertEquals(1, myDungeon.getHeroCol());
    }

    /**
     * Tests that the hero's position is updated correctly after a
     * valid move through an open door.
     */
    @Test
    void testHeroPositionUpdatesAfterValidMove() {
        connect(myDungeon, 0, 0, Direction.EAST);
        myDungeon.setHeroPosition(0, 0);

        assertEquals(0, myDungeon.getHeroRow());
        assertEquals(0, myDungeon.getHeroCol());

        assertTrue(myDungeon.moveHero(Direction.EAST));
        assertEquals(0, myDungeon.getHeroRow());
        assertEquals(1, myDungeon.getHeroCol());
        assertSame(myDungeon.getRoom(0, 1), myDungeon.getCurrentRoom());
    }

    /**
     * Tests that getSurroundingRooms returns 3 rooms when the hero
     * is in a corner.
     */
    @Test
    void testGetSurroundingRoomsAtCornerReturns3() {
        myDungeon.setHeroPosition(0, 0);
        assertEquals(3, myDungeon.getSurroundingRooms().size(),
                "Corner position should expose 3 neighbours");
    }

    /**
     * Tests that getSurroundingRooms returns 5 rooms when the hero
     * is on a non-corner edge.
     */
    @Test
    void testGetSurroundingRoomsAtEdgeReturns5() {
        myDungeon.setHeroPosition(0, 1);
        assertEquals(5, myDungeon.getSurroundingRooms().size(),
                "Edge position should expose 5 neighbours");
    }

    /**
     * Tests that getSurroundingRooms returns 8 rooms when the hero
     * is at an interior position.
     */
    @Test
    void testGetSurroundingRoomsAtInteriorReturns8() {
        myDungeon.setHeroPosition(1, 1);
        assertEquals(8, myDungeon.getSurroundingRooms().size(),
                "Interior position should expose 8 neighbours");
    }

    /**
     * Tests that setHeroPosition rejects coordinates outside the
     * grid.
     */
    @Test
    void testSetHeroPositionRejectsOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> myDungeon.setHeroPosition(-1, 0));
        assertThrows(IndexOutOfBoundsException.class,
                () -> myDungeon.setHeroPosition(0, 3));
    }

    /**
     * Tests that getSurroundingRooms returns the actual neighbouring
     * rooms (not just the right count) for an interior position.
     */
    @Test
    void testGetSurroundingRoomsContainsCorrectNeighbours() {
        myDungeon.setHeroPosition(1, 1);
        final java.util.List<Room> neighbours =
                myDungeon.getSurroundingRooms();

        assertTrue(neighbours.contains(myDungeon.getRoom(0, 1)),
                "North neighbour should be included");
        assertTrue(neighbours.contains(myDungeon.getRoom(2, 1)),
                "South neighbour should be included");
        assertTrue(neighbours.contains(myDungeon.getRoom(1, 0)),
                "West neighbour should be included");
        assertTrue(neighbours.contains(myDungeon.getRoom(1, 2)),
                "East neighbour should be included");
        assertTrue(neighbours.contains(myDungeon.getRoom(0, 0)),
                "Diagonal neighbour should be included");
        assertFalse(neighbours.contains(myDungeon.getRoom(1, 1)),
                "The hero's own room should not be included");
    }
}
