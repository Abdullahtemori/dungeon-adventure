package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Room.
 * Covers doors, entrance/exit/pit flags, item and pillar fields,
 * the monster reference, isEmpty, and pickUpItems.
 *
 * @author Tarik Atasoy
 * @version Iteration 1
 */
class RoomTest {

    /** A fresh room used by most tests. */
    private Room myRoom;

    /**
     * Creates a fresh room before each test.
     */
    @BeforeEach
    void setUp() {
        myRoom = new Room(2, 3);
    }

    /**
     * Tests that the row and column passed to the constructor are
     * stored correctly.
     */
    @Test
    void testRowAndColAreStored() {
        assertEquals(2, myRoom.getRow(), "Row should match constructor arg");
        assertEquals(3, myRoom.getCol(), "Col should match constructor arg");
    }

    /**
     * Tests that a brand new room has no doors on any side.
     */
    @Test
    void testNewRoomHasNoDoors() {
        for (final Direction d : Direction.values()) {
            assertFalse(myRoom.hasDoor(d),
                    "New room should have no door in direction " + d);
        }
    }

    /**
     * Tests that setDoor turns a door on and off independently for
     * each direction.
     */
    @Test
    void testSetDoorOpensAndCloses() {
        myRoom.setDoor(Direction.NORTH, true);
        assertTrue(myRoom.hasDoor(Direction.NORTH));
        assertFalse(myRoom.hasDoor(Direction.SOUTH));
        assertFalse(myRoom.hasDoor(Direction.EAST));
        assertFalse(myRoom.hasDoor(Direction.WEST));

        myRoom.setDoor(Direction.NORTH, false);
        assertFalse(myRoom.hasDoor(Direction.NORTH));
    }

    /**
     * Tests that the entrance flag can be turned on and off.
     */
    @Test
    void testEntranceFlag() {
        assertFalse(myRoom.hasEntrance());
        myRoom.setEntrance(true);
        assertTrue(myRoom.hasEntrance());
        myRoom.setEntrance(false);
        assertFalse(myRoom.hasEntrance());
    }

    /**
     * Tests that the exit flag can be turned on and off.
     */
    @Test
    void testExitFlag() {
        assertFalse(myRoom.hasExit());
        myRoom.setExit(true);
        assertTrue(myRoom.hasExit());
        myRoom.setExit(false);
        assertFalse(myRoom.hasExit());
    }

    /**
     * Tests that adding and clearing a pit updates both the flag
     * and the damage value.
     */
    @Test
    void testPitSetAndClear() {
        assertFalse(myRoom.hasPit());
        assertEquals(0, myRoom.getPitDamage());

        myRoom.setPit(12);
        assertTrue(myRoom.hasPit());
        assertEquals(12, myRoom.getPitDamage());

        myRoom.clearPit();
        assertFalse(myRoom.hasPit());
        assertEquals(0, myRoom.getPitDamage());
    }

    /**
     * Tests that monster placement and removal work as expected.
     */
    @Test
    void testMonsterPlacement() {
        assertFalse(myRoom.hasMonster());
        assertNull(myRoom.getMonster());

        final Monster ogre = new Ogre();
        myRoom.setMonster(ogre);
        assertTrue(myRoom.hasMonster());
        assertSame(ogre, myRoom.getMonster());

        myRoom.setMonster(null);
        assertFalse(myRoom.hasMonster());
    }

    /**
     * Tests that each item field can be set and retrieved.
     */
    @Test
    void testItemFieldsSetAndGet() {
        final HealingPotion hp = new HealingPotion();
        final VisionPotion vp = new VisionPotion();
        final Bomb bomb = new Bomb();

        myRoom.setHealingPotion(hp);
        myRoom.setVisionPotion(vp);
        myRoom.setBomb(bomb);
        myRoom.setPillar(Pillar.ABSTRACTION);

        assertSame(hp, myRoom.getHealingPotion());
        assertSame(vp, myRoom.getVisionPotion());
        assertSame(bomb, myRoom.getBomb());
        assertEquals(Pillar.ABSTRACTION, myRoom.getPillar());
    }

    /**
     * Tests that a brand new room is considered empty.
     */
    @Test
    void testNewRoomIsEmpty() {
        assertTrue(myRoom.isEmpty(), "Brand new room should be empty");
    }

    /**
     * Tests that any single piece of content makes the room
     * non-empty.
     */
    @Test
    void testRoomIsNotEmptyWithContent() {
        myRoom.setHealingPotion(new HealingPotion());
        assertFalse(myRoom.isEmpty());

        myRoom = new Room(0, 0);
        myRoom.setPit(5);
        assertFalse(myRoom.isEmpty());

        myRoom = new Room(0, 0);
        myRoom.setEntrance(true);
        assertFalse(myRoom.isEmpty());

        myRoom = new Room(0, 0);
        myRoom.setExit(true);
        assertFalse(myRoom.isEmpty());

        myRoom = new Room(0, 0);
        myRoom.setMonster(new Gremlin());
        assertFalse(myRoom.isEmpty());
    }

    /**
     * Tests that pickUpItems transfers a healing potion from the
     * room to the hero and clears the room slot.
     */
    @Test
    void testPickUpItemsTransfersHealingPotion() {
        final Hero hero = new Warrior("Tester");
        myRoom.setHealingPotion(new HealingPotion());

        myRoom.pickUpItems(hero);

        assertNull(myRoom.getHealingPotion(),
                "Healing potion should be removed from the room");
        assertEquals(1, hero.getHealingPotions(),
                "Hero should have one more healing potion");
    }

    /**
     * Tests that pickUpItems transfers a vision potion and a bomb
     * to the hero and clears both slots.
     */
    @Test
    void testPickUpItemsTransfersVisionPotionAndBomb() {
        final Hero hero = new Warrior("Tester");
        myRoom.setVisionPotion(new VisionPotion());
        myRoom.setBomb(new Bomb());

        myRoom.pickUpItems(hero);

        assertNull(myRoom.getVisionPotion());
        assertNull(myRoom.getBomb());
        assertEquals(1, hero.getVisionPotions());
        assertEquals(1, hero.getBombs());
    }

    /**
     * Tests that pickUpItems adds a pillar to the hero's set and
     * clears the pillar from the room.
     */
    @Test
    void testPickUpItemsTransfersPillar() {
        final Hero hero = new Warrior("Tester");
        myRoom.setPillar(Pillar.INHERITANCE);

        myRoom.pickUpItems(hero);

        assertNull(myRoom.getPillar(),
                "Pillar should be removed from the room");
        assertTrue(hero.getPillarsFound().contains(Pillar.INHERITANCE),
                "Hero should now have the inheritance pillar");
    }

    /**
     * Tests that calling pickUpItems on an empty room does nothing.
     */
    @Test
    void testPickUpItemsOnEmptyRoomDoesNothing() {
        final Hero hero = new Warrior("Tester");
        myRoom.pickUpItems(hero);

        assertEquals(0, hero.getHealingPotions());
        assertEquals(0, hero.getVisionPotions());
        assertEquals(0, hero.getBombs());
        assertTrue(hero.getPillarsFound().isEmpty());
    }

    /**
     * Tests that pickUpItems is idempotent: calling it twice does
     * not give the hero a second copy of the same item.
     */
    @Test
    void testPickUpItemsIsIdempotent() {
        final Hero hero = new Warrior("Tester");
        myRoom.setHealingPotion(new HealingPotion());

        myRoom.pickUpItems(hero);
        myRoom.pickUpItems(hero);

        assertEquals(1, hero.getHealingPotions(),
                "Second pickUp should not add another potion");
    }

    /**
     * Tests that toString returns a 3-line drawing using the
     * expected wall and door characters.
     */
    @Test
    void testToStringFormat() {
        myRoom.setDoor(Direction.NORTH, true);
        myRoom.setDoor(Direction.EAST, true);

        final String[] lines = myRoom.toString().split("\\R");
        assertEquals(3, lines.length, "toString should produce 3 lines");
        assertEquals(3, lines[0].length(), "Each line should be 3 chars wide");
        assertEquals('-', lines[0].charAt(1), "North door should be '-'");
        assertEquals('|', lines[1].charAt(2), "East door should be '|'");
        assertEquals('*', lines[1].charAt(0), "West wall should be '*'");
        assertEquals('*', lines[2].charAt(1), "South wall should be '*'");
    }
}
