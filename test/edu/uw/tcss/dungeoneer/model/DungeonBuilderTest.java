package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DungeonBuilder.
 * Covers grid sizes per Difficulty, exactly one entrance and one
 * exit, four distinct pillars in non-special rooms, and that every
 * built dungeon is traversable.
 *
 * @author Tarik Atasoy
 * @version Iteration 1
 */
class DungeonBuilderTest {

    /**
     * Builds a dungeon with a fixed seed so the test stays
     * deterministic.
     *
     * @param theDifficulty the difficulty to build
     * @param theSeed       the random seed
     * @return a built dungeon
     */
    private static Dungeon build(final Difficulty theDifficulty,
                                 final long theSeed) {
        return new DungeonBuilder()
                .difficulty(theDifficulty)
                .random(new Random(theSeed))
                .build();
    }

    /**
     * Tests that EASY produces a 5x5 dungeon.
     */
    @Test
    void testEasySize() {
        final Dungeon d = build(Difficulty.EASY, 1L);
        assertEquals(5, d.getRows());
        assertEquals(5, d.getCols());
    }

    /**
     * Tests that MEDIUM produces a 7x7 dungeon.
     */
    @Test
    void testMediumSize() {
        final Dungeon d = build(Difficulty.MEDIUM, 1L);
        assertEquals(7, d.getRows());
        assertEquals(7, d.getCols());
    }

    /**
     * Tests that HARD produces a 10x10 dungeon.
     */
    @Test
    void testHardSize() {
        final Dungeon d = build(Difficulty.HARD, 1L);
        assertEquals(10, d.getRows());
        assertEquals(10, d.getCols());
    }

    /**
     * Tests that a built dungeon has exactly one entrance and
     * exactly one exit.
     */
    @Test
    void testExactlyOneEntranceAndExit() {
        final Dungeon d = build(Difficulty.MEDIUM, 42L);

        int entrances = 0;
        int exits = 0;
        for (int r = 0; r < d.getRows(); r++) {
            for (int c = 0; c < d.getCols(); c++) {
                final Room room = d.getRoom(r, c);
                if (room.hasEntrance()) {
                    entrances++;
                }
                if (room.hasExit()) {
                    exits++;
                }
            }
        }
        assertEquals(1, entrances, "Should have exactly one entrance");
        assertEquals(1, exits, "Should have exactly one exit");
    }

    /**
     * Tests that the entrance and exit rooms have no other content.
     */
    @Test
    void testEntranceAndExitRoomsAreEmpty() {
        final Dungeon d = build(Difficulty.MEDIUM, 42L);

        for (int r = 0; r < d.getRows(); r++) {
            for (int c = 0; c < d.getCols(); c++) {
                final Room room = d.getRoom(r, c);
                if (!room.hasEntrance() && !room.hasExit()) {
                    continue;
                }
                assertNull(room.getHealingPotion());
                assertNull(room.getVisionPotion());
                assertNull(room.getBomb());
                assertNull(room.getPillar());
                assertFalse(room.hasPit());
                assertFalse(room.hasMonster());
            }
        }
    }

    /**
     * Tests that the hero is placed on the entrance after build().
     */
    @Test
    void testHeroStartsOnEntrance() {
        final Dungeon d = build(Difficulty.EASY, 7L);
        final Room start = d.getHeroRoom();
        assertNotNull(start);
        assertTrue(start.hasEntrance(),
                "Hero should start on the entrance");
    }

    /**
     * Tests that the four pillars are placed in four distinct rooms
     * and that none of those rooms are the entrance or exit.
     */
    @Test
    void testFourDistinctPillarsPlaced() {
        final Dungeon d = build(Difficulty.MEDIUM, 99L);

        final Set<Pillar> seen = new HashSet<>();
        int pillarRooms = 0;
        for (int r = 0; r < d.getRows(); r++) {
            for (int c = 0; c < d.getCols(); c++) {
                final Room room = d.getRoom(r, c);
                final Pillar p = room.getPillar();
                if (p == null) {
                    continue;
                }
                pillarRooms++;
                seen.add(p);
                assertFalse(room.hasEntrance(),
                        "Pillar should not be in the entrance room");
                assertFalse(room.hasExit(),
                        "Pillar should not be in the exit room");
            }
        }
        assertEquals(4, pillarRooms, "Should have exactly 4 pillar rooms");
        assertEquals(4, seen.size(), "All four pillars should be different");
    }

    /**
     * Tests that every built dungeon is traversable. Runs on each
     * difficulty with a few different seeds.
     */
    @Test
    void testBuiltDungeonsAreTraversable() {
        for (final Difficulty diff : Difficulty.values()) {
            for (long seed = 0; seed < 5; seed++) {
                final Dungeon d = build(diff, seed);
                assertTrue(d.isTraversable(),
                        "Dungeon should be traversable for "
                                + diff + " seed=" + seed);
            }
        }
    }

    /**
     * Tests that an item chance of 0.0 produces no pits or items
     * outside the entrance, exit, and pillar rooms.
     */
    @Test
    void testZeroItemChanceLeavesRoomsBare() {
        final Dungeon d = new DungeonBuilder()
                .difficulty(Difficulty.EASY)
                .random(new Random(123L))
                .itemChance(0.0)
                .build();

        for (int r = 0; r < d.getRows(); r++) {
            for (int c = 0; c < d.getCols(); c++) {
                final Room room = d.getRoom(r, c);
                if (room.hasEntrance() || room.hasExit()
                        || room.getPillar() != null) {
                    continue;
                }
                assertFalse(room.hasPit());
                assertNull(room.getHealingPotion());
                assertNull(room.getVisionPotion());
                assertNull(room.getBomb());
            }
        }
    }

    /**
     * Tests that itemChance rejects values outside [0.0, 1.0].
     */
    @Test
    void testItemChanceRejectsBadValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new DungeonBuilder().itemChance(-0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new DungeonBuilder().itemChance(1.1));
    }
}
