package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Integration tests for the Item and Dungeon interaction system.
 * These tests verify that multiple classes work correctly together:
 * - TestRoom correctly transfers items to Hero via pickUpItems()
 * - Hero inventory counts update correctly after pickup
 * - Hero item use methods work correctly after items are collected
 * - Items are removed from the room after pickup
 * - Edge cases like empty rooms and zero inventory are handled safely
 *
 * @author Daniella Birungi
 * @version Iteration 2
 */
class ItemIntegrationTest {


    /**
     * The hero used in all tests.
     * Created fresh before each test to prevent state leaking
     * between tests (e.g. leftover potions from a previous test).
     */
    private Hero myHero;

    /**
     * The room used in all tests.
     * Created fresh before each test so each test starts with
     * an empty room that we then fill as needed.
     */
    private Room myRoom;

    /**
     * Sets up a fresh Warrior hero and empty room before each test.
     * Using Warrior specifically because it has well-defined stats
     * that make HP calculations predictable in tests.
     */
    @BeforeEach
    void setUp() {
        final HeroFactory factory = new HeroFactory();
        myHero = factory.createHero("Warrior", "TestHero");
        myRoom = new Room(0,0);
    }


    /**
     * Tests that a hero's healing potion count increases by 1
     * when entering a room that contains a healing potion.
     */
    @Test
    void testHeroPicksUpHealingPotion() {
        // Place a healing potion in the room
        myRoom.setHealingPotion(new HealingPotion(10));

        // Hero enters the room and picks up items
        myRoom.pickUpItems(myHero);

        // Hero should now have exactly 1 healing potion
        assertEquals(1, myHero.getHealingPotions(),
                "Hero should have 1 healing potion after pickup");
    }

    /**
     * Tests that the room's healing potion is null after pickup.
     */
    @Test
    void testHealingPotionRemovedFromRoomAfterPickup() {
        myRoom.setHealingPotion(new HealingPotion(10));
        myRoom.pickUpItems(myHero);

        // Room should no longer have the potion
        assertNull(myRoom.getHealingPotion(),
                "Healing potion should be null after hero picks it up");
    }

    /**
     * Tests that visiting an empty room does not give the hero
     * any healing potions.
     */
    @Test
    void testEmptyRoomDoesNotAddHealingPotion() {
        // Room has no items — pickUpItems should do nothing
        myRoom.pickUpItems(myHero);

        assertEquals(0, myHero.getHealingPotions(),
                "Empty room should not give hero a healing potion");
    }

    /**
     * Tests that picking up multiple healing potions across
     * multiple rooms accumulates the count correctly.
     */
    @Test
    void testMultipleHealingPotionPickupsAccumulate() {
        // Visit three rooms each with a healing potion
        myRoom.setHealingPotion(new HealingPotion(10));
        myRoom.pickUpItems(myHero);

        final Room room2 = new Room(0,1);
        room2.setHealingPotion(new HealingPotion(8));
        room2.pickUpItems(myHero);

        final Room room3 = new Room(0,2);
        room3.setHealingPotion(new HealingPotion(5));
        room3.pickUpItems(myHero);

        assertEquals(3, myHero.getHealingPotions(),
                "Hero should have 3 healing potions after "
                        + "visiting 3 rooms with potions");
    }


    /**
     * Tests that using a healing potion decrements the count by 1.
     */
    @Test
    void testUsingHealingPotionDecrementsCount() {
        myRoom.setHealingPotion(new HealingPotion(10));
        myRoom.pickUpItems(myHero);

        // Use the potion
        myHero.useHealingPotion();

        assertEquals(0, myHero.getHealingPotions(),
                "Healing potion count should be 0 after using it");
    }

    /**
     * Tests that using a healing potion when inventory is empty
     * returns 0 and does not crash.
     */
    @Test
    void testUsingHealingPotionWhenEmptyReturnsZero() {
        // Hero has no potions — using one should return 0
        final int healed = myHero.useHealingPotion();

        assertEquals(0, healed,
                "Using a healing potion with empty inventory "
                        + "should return 0");
    }


    /**
     * Tests that a hero's vision potion count increases by 1
     * when entering a room that contains a vision potion.
     */
    @Test
    void testHeroPicksUpVisionPotion() {
        myRoom.setVisionPotion(new VisionPotion());
        myRoom.pickUpItems(myHero);

        assertEquals(1, myHero.getVisionPotions(),
                "Hero should have 1 vision potion after pickup");
    }

    /**
     * Tests that the room's vision potion is null after pickup.
     */
    @Test
    void testVisionPotionRemovedFromRoomAfterPickup() {
        myRoom.setVisionPotion(new VisionPotion());
        myRoom.pickUpItems(myHero);

        assertNull(myRoom.getVisionPotion(),
                "Vision potion should be null after hero picks it up");
    }

    /**
     * Tests that using a vision potion returns true and
     * decrements the count.
     */
    @Test
    void testUsingVisionPotionReturnsTrueAndDecrementsCount() {
        myRoom.setVisionPotion(new VisionPotion());
        myRoom.pickUpItems(myHero);

        final boolean used = myHero.useVisionPotion();

        assertTrue(used,
                "useVisionPotion should return true when used");
        assertEquals(0, myHero.getVisionPotions(),
                "Vision potion count should be 0 after use");
    }

    /**
     * Tests that using a vision potion when inventory is empty
     * returns false and does not crash.
     */
    @Test
    void testUsingVisionPotionWhenEmptyReturnsFalse() {
        final boolean used = myHero.useVisionPotion();

        assertFalse(used,
                "useVisionPotion should return false "
                        + "when inventory is empty");
    }


    /**
     * Tests that a hero's bomb count increases by 1
     * when entering a room that contains a bomb.
     */
    @Test
    void testHeroPicksUpBomb() {
        myRoom.setBomb(new Bomb(100));
        myRoom.pickUpItems(myHero);

        assertEquals(1, myHero.getBombs(),
                "Hero should have 1 bomb after pickup");
    }

    /**
     * Tests that the room's bomb is null after pickup.
     */
    @Test
    void testBombRemovedFromRoomAfterPickup() {
        myRoom.setBomb(new Bomb(100));
        myRoom.pickUpItems(myHero);

        assertNull(myRoom.getBomb(),
                "Bomb should be null after hero picks it up");
    }

    /**
     * Tests that using a bomb deals damage between 75 and 150
     * to a monster and decrements the bomb count.
     */
    @Test
    void testUsingBombDealsDamageToMonster() {
        myRoom.setBomb(new Bomb());
        myRoom.pickUpItems(myHero);

        // Create a monster with high HP so one bomb won't kill it
        final Monster ogre = new Ogre();
        final int hpBefore = ogre.getHitPoints();

        // Use the bomb against the ogre
        final int damage = myHero.useBomb(ogre);

        // Damage must be in valid bomb range
        assertTrue(damage >= 75 && damage <= 150,
                "Bomb damage should be between 75 and 150 but was: "
                        + damage);

        // Monster's HP must have decreased by exactly that amount
        // (assuming the ogre had enough HP to survive)
        assertEquals(hpBefore - damage, ogre.getHitPoints(),
                "Ogre HP should decrease by the bomb damage amount");
    }

    /**
     * Tests that using a bomb decrements the bomb count by 1.
     */
    @Test
    void testUsingBombDecrementsCount() {
        myRoom.setBomb(new Bomb(100));
        myRoom.pickUpItems(myHero);

        final Monster ogre = new Ogre();
        myHero.useBomb(ogre);

        assertEquals(0, myHero.getBombs(),
                "Bomb count should be 0 after using it");
    }

    /**
     * Tests that using a bomb when inventory is empty returns 0
     * and does not crash.
     */
    @Test
    void testUsingBombWhenEmptyReturnsZero() {
        final Monster ogre = new Ogre();
        final int damage = myHero.useBomb(ogre);

        assertEquals(0, damage,
                "useBomb should return 0 when inventory is empty");
    }


    /**
     * Tests that collecting a pillar adds it to the hero's
     * pillar set and it is removed from the room.
     */
    @Test
    void testHeroCollectsPillar() {
        myRoom.setPillar(Pillar.ABSTRACTION);
        myRoom.pickUpItems(myHero);

        assertTrue(myHero.getPillarsFound().contains(Pillar.ABSTRACTION),
                "Hero should have collected the ABSTRACTION pillar");
    }

    /**
     * Tests that the pillar is removed from the room after collection.
     */
    @Test
    void testPillarRemovedFromRoomAfterCollection() {
        myRoom.setPillar(Pillar.ABSTRACTION);
        myRoom.pickUpItems(myHero);

        assertNull(myRoom.getPillar(),
                "Pillar should be null after hero collects it");
    }

    /**
     * Tests that collecting all 4 pillars fills the set completely.
     */
    @Test
    void testCollectingAllFourPillarsFillsSet() {
        // Place each pillar in a separate room and collect them
        myRoom.setPillar(Pillar.ABSTRACTION);
        myRoom.pickUpItems(myHero);

        final Room room2 = new Room(0,2);
        room2.setPillar(Pillar.ENCAPSULATION);
        room2.pickUpItems(myHero);

        final Room room3 = new Room(0,3);
        room3.setPillar(Pillar.INHERITANCE);
        room3.pickUpItems(myHero);

        final Room room4 = new Room(0,4);
        room4.setPillar(Pillar.POLYMORPHISM);
        room4.pickUpItems(myHero);

        // Hero should have all 4 pillars
        assertEquals(4, myHero.getPillarsFound().size(),
                "Hero should have all 4 pillars after collecting them");
    }

    /**
     * Tests that collecting the same pillar twice does not
     * add it to the set a second time.
     */
    @Test
    void testDuplicatePillarNotAddedTwice() {
        // Manually add the same pillar twice
        myHero.addPillar(Pillar.ABSTRACTION);
        myHero.addPillar(Pillar.ABSTRACTION);

        assertEquals(1, myHero.getPillarsFound().size(),
                "Duplicate pillar should not be added to the set twice");
    }


    /**
     * Tests that a room containing multiple items transfers
     * all of them to the hero in one pickUpItems call.
     */
    @Test
    void testRoomWithMultipleItemsTransfersAll() {
        // Place all three item types in the same room
        myRoom.setHealingPotion(new HealingPotion(10));
        myRoom.setVisionPotion(new VisionPotion());
        myRoom.setBomb(new Bomb(100));

        // Hero enters and picks everything up
        myRoom.pickUpItems(myHero);

        // All counts should be 1
        assertAll("All items should be picked up",
                () -> assertEquals(1, myHero.getHealingPotions(),
                        "Should have 1 healing potion"),
                () -> assertEquals(1, myHero.getVisionPotions(),
                        "Should have 1 vision potion"),
                () -> assertEquals(1, myHero.getBombs(),
                        "Should have 1 bomb")
        );
    }

    /**
     * Tests that after picking up multiple items the room
     * is completely empty.
     */
    @Test
    void testRoomIsEmptyAfterPickingUpAllItems() {
        myRoom.setHealingPotion(new HealingPotion(10));
        myRoom.setVisionPotion(new VisionPotion());
        myRoom.setBomb(new Bomb(100));
        myRoom.setPillar(Pillar.ABSTRACTION);

        myRoom.pickUpItems(myHero);

        assertAll("Room should be empty after pickup",
                () -> assertNull(myRoom.getHealingPotion(),
                        "Healing potion should be null"),
                () -> assertNull(myRoom.getVisionPotion(),
                        "Vision potion should be null"),
                () -> assertNull(myRoom.getBomb(),
                        "Bomb should be null"),
                () -> assertNull(myRoom.getPillar(),
                        "Pillar should be null")
        );
    }
}