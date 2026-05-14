package edu.uw.tcss.dungeoneer.controller;

import edu.uw.tcss.dungeoneer.model.Dungeon;
import edu.uw.tcss.dungeoneer.model.Gremlin;
import edu.uw.tcss.dungeoneer.model.Monster;
import edu.uw.tcss.dungeoneer.model.Ogre;
import edu.uw.tcss.dungeoneer.model.Pillar;
import edu.uw.tcss.dungeoneer.model.Room;
import edu.uw.tcss.dungeoneer.model.Skeleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * MonsterPlacer distributes monsters across a generated dungeon.
 *
 * Placement rules (matching the assignment spec):
 *   - Entrance and exit rooms never get a monster.
 *   - Rooms that hold a Pillar always get a strong monster (Ogre or Skeleton).
 *   - The exit-adjacent rooms also get tougher monsters.
 *   - Remaining rooms each have a 25% chance of a random monster.
 *
 * This is a stateless utility class; all methods are static.
 *
 * @author Generated scaffold — fill in team member name
 * @version Iteration 2
 */
public final class MonsterPlacer {

    /** Chance a plain room gets a monster. */
    private static final double MONSTER_CHANCE = 0.25;

    private static final Random RANDOM = new Random();

    /** Utility class — no instances. */
    private MonsterPlacer() { }

    /**
     * Walks every room in the dungeon and places monsters according
     * to the spec rules described in the class Javadoc.
     *
     * @param theDungeon the dungeon to populate with monsters
     */
    public static void placeMonsters(final Dungeon theDungeon) {
        final int rows = theDungeon.getRows();
        final int cols = theDungeon.getCols();

        // Locate the exit cell (bottom-right by DungeonBuilder convention)
        final int exitRow = rows - 1;
        final int exitCol = cols - 1;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final Room room = theDungeon.getRoom(r, c);

                // Entrance and exit stay clear
                if (room.hasEntrance() || room.hasExit()) {
                    continue;
                }

                // Pillar rooms always get a tough guardian
                if (room.getPillar() != null) {
                    room.setMonster(toughMonster());
                    continue;
                }

                // Rooms adjacent to the exit get a tough guardian too
                if (isAdjacentTo(r, c, exitRow, exitCol)) {
                    room.setMonster(toughMonster());
                    continue;
                }

                // All other rooms: random chance of any monster
                if (RANDOM.nextDouble() < MONSTER_CHANCE) {
                    room.setMonster(randomMonster());
                }
            }
        }
    }

    /**
     * Returns a random strong monster (Ogre or Skeleton).
     * Used for pillar rooms and exit-adjacent rooms.
     *
     * @return a new tough Monster instance
     */
    private static Monster toughMonster() {
        return RANDOM.nextBoolean() ? new Ogre() : new Skeleton();
    }

    /**
     * Returns a randomly selected monster of any type.
     *
     * @return a new Monster instance
     */
    private static Monster randomMonster() {
        final int roll = RANDOM.nextInt(3);
        return switch (roll) {
            case 0 -> new Ogre();
            case 1 -> new Gremlin();
            default -> new Skeleton();
        };
    }

    /**
     * Returns true if (theRow, theCol) is orthogonally or diagonally
     * adjacent to (theTargetRow, theTargetCol).
     */
    private static boolean isAdjacentTo(final int theRow, final int theCol,
                                         final int theTargetRow,
                                         final int theTargetCol) {
        return Math.abs(theRow - theTargetRow) <= 1
                && Math.abs(theCol - theTargetCol) <= 1
                && !(theRow == theTargetRow && theCol == theTargetCol);
    }
}
