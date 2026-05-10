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
 * @version Iteration 2
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
                .setDifficulty(theDifficulty)
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
                .setDifficulty(Difficulty.EASY)
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

    /**
     * Tests that setDifficulty and setSize both return the same
     * builder instance so calls can be chained.
     */
    @Test
    void testBuilderMethodsAreChainable() {
        final DungeonBuilder b = new DungeonBuilder();
        assertSame(b, b.setDifficulty(Difficulty.EASY));
        assertSame(b, b.setSize(5, 5));
        assertSame(b, b.itemChance(0.1));
        assertSame(b, b.random(new Random(1L)));
    }

    /**
     * Tests that setSize overrides the dimensions implied by the
     * chosen difficulty.
     */
    @Test
    void testSetSizeOverridesDifficulty() {
        final Dungeon d = new DungeonBuilder()
                .setDifficulty(Difficulty.HARD)
                .setSize(4, 6)
                .random(new Random(11L))
                .build();
        assertEquals(4, d.getRows());
        assertEquals(6, d.getCols());
    }

    /**
     * Tests that setSize rejects non-positive dimensions.
     */
    @Test
    void testSetSizeRejectsBadValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new DungeonBuilder().setSize(0, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new DungeonBuilder().setSize(5, -1));
    }

    /**
     * Tests that setDifficulty rejects null.
     */
    @Test
    void testSetDifficultyRejectsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new DungeonBuilder().setDifficulty(null));
    }

    /**
     * Tests that doors are symmetric: if room A has a door on side
     * X, the neighbour on that side has a door on the opposite side.
     */
    @Test
    void testDoorsAreSymmetric() {
        final Dungeon d = build(Difficulty.MEDIUM, 17L);
        for (int r = 0; r < d.getRows(); r++) {
            for (int c = 0; c < d.getCols(); c++) {
                final Room room = d.getRoom(r, c);
                for (final Direction dir : Direction.values()) {
                    if (!room.hasDoor(dir)) {
                        continue;
                    }
                    final int nr = r + dir.getRowOffset();
                    final int nc = c + dir.getColOffset();
                    assertTrue(nr >= 0 && nr < d.getRows()
                                    && nc >= 0 && nc < d.getCols(),
                            "Door at (" + r + "," + c + ") "
                                    + dir + " points off the grid");
                    assertTrue(d.getRoom(nr, nc).hasDoor(dir.opposite()),
                            "Neighbour of (" + r + "," + c + ") "
                                    + dir + " is missing the return door");
                }
            }
        }
    }

    /**
     * Tests that an item chance of 1.0 fills every non-critical room
     * with a pit, a healing potion, and a vision potion.
     */
    @Test
    void testFullItemChanceFillsAllNormalRooms() {
        final Dungeon d = new DungeonBuilder()
                .setDifficulty(Difficulty.EASY)
                .random(new Random(5L))
                .itemChance(1.0)
                .build();
        for (int r = 0; r < d.getRows(); r++) {
            for (int c = 0; c < d.getCols(); c++) {
                final Room room = d.getRoom(r, c);
                if (room.hasEntrance() || room.hasExit()
                        || room.getPillar() != null) {
                    continue;
                }
                assertTrue(room.hasPit());
                assertNotNull(room.getHealingPotion());
                assertNotNull(room.getVisionPotion());
            }
        }
    }

    /**
     * Stress test: builds many dungeons across all difficulties and
     * checks that every one is traversable. Verifies the regenerate
     * loop and the spanning-tree carving stay correct under varied
     * seeds.
     */
    @Test
    void testRegenerationProducesTraversableDungeons() {
        for (final Difficulty diff : Difficulty.values()) {
            for (long seed = 0; seed < 25; seed++) {
                final Dungeon d = build(diff, seed);
                assertTrue(d.isTraversable(),
                        "Non-traversable dungeon for "
                                + diff + " seed=" + seed);
            }
        }
    }
}
