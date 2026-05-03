package edu.uw.tcss.dungeoneer.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Builds a randomly generated Dungeon. Callers pick a difficulty
 * (which sets the grid size) and then call build().
 *
 * What build() does, in order:
 *   1. Carve a maze of doors with recursive backtracking. The door
 *      graph is a spanning tree, so every room is reachable from
 *      every other room.
 *   2. Mark the entrance and exit in opposite corners.
 *   3. Drop the four Pillars of OO into distinct rooms that are
 *      not the entrance or exit.
 *   4. Sprinkle pits, healing potions, vision potions, and bombs
 *      into the remaining rooms with a small probability each.
 *   5. Place the hero on the entrance.
 *
 * Monsters, save/load, and the SQLite monster table are not part of
 * this iteration.
 *
 * @author Tarik Atasoy
 * @version Iteration 1
 */
public class DungeonBuilder {

    /** Default chance per item type for placement in a normal room. */
    private static final double DEFAULT_ITEM_CHANCE = 0.10;

    /** Minimum damage a pit can deal. */
    private static final int PIT_MIN_DAMAGE = 1;

    /** Maximum damage a pit can deal. */
    private static final int PIT_MAX_DAMAGE = 20;

    /** Difficulty for the next dungeon. */
    private Difficulty myDifficulty = Difficulty.MEDIUM;

    /** Probability for each item type in a normal room. */
    private double myItemChance = DEFAULT_ITEM_CHANCE;

    /** Random source. Can be swapped out so tests stay deterministic. */
    private Random myRandom = new Random();

    /** Default constructor. */
    public DungeonBuilder() {
        // Field defaults already cover initialization.
    }

    /**
     * Sets the difficulty (and therefore grid size) for the next build.
     *
     * @param theDifficulty the difficulty to use
     * @return this builder, for chaining
     */
    public DungeonBuilder difficulty(final Difficulty theDifficulty) {
        myDifficulty = theDifficulty;
        return this;
    }

    /**
     * Overrides the per-item placement probability.
     *
     * @param theChance probability between 0.0 and 1.0
     * @return this builder, for chaining
     */
    public DungeonBuilder itemChance(final double theChance) {
        if (theChance < 0.0 || theChance > 1.0) {
            throw new IllegalArgumentException(
                    "Item chance must be between 0.0 and 1.0.");
        }
        myItemChance = theChance;
        return this;
    }

    /**
     * Replaces the random source. Mainly useful when a test wants
     * to seed it for repeatable output.
     *
     * @param theRandom the random source to use
     * @return this builder, for chaining
     */
    public DungeonBuilder random(final Random theRandom) {
        myRandom = theRandom;
        return this;
    }

    /**
     * Builds and returns a new randomly generated dungeon. The maze
     * carving step already produces a connected layout, but we still
     * verify with isTraversable() and rebuild if a future change
     * ever breaks that property.
     *
     * @return a fully populated, traversable dungeon
     */
    public Dungeon build() {
        while (true) {
            final int rows = myDifficulty.getRows();
            final int cols = myDifficulty.getCols();
            final Dungeon dungeon = new Dungeon(rows, cols);

            carveMaze(dungeon);
            placeEntranceAndExit(dungeon);
            placePillars(dungeon);
            placeRandomContent(dungeon);

            if (dungeon.isTraversable()) {
                return dungeon;
            }
        }
    }

    /**
     * Carves a maze of doors using iterative DFS (recursive
     * backtracking). The resulting door graph is a spanning tree of
     * the grid, which keeps the dungeon connected.
     *
     * @param theDungeon the dungeon to carve
     */
    private void carveMaze(final Dungeon theDungeon) {
        final int rows = theDungeon.getRows();
        final int cols = theDungeon.getCols();
        final boolean[][] visited = new boolean[rows][cols];

        final Deque<int[]> stack = new ArrayDeque<>();
        final int startRow = myRandom.nextInt(rows);
        final int startCol = myRandom.nextInt(cols);
        stack.push(new int[] {startRow, startCol});
        visited[startRow][startCol] = true;

        while (!stack.isEmpty()) {
            final int[] cell = stack.peek();
            final int r = cell[0];
            final int c = cell[1];

            final List<Direction> options = new ArrayList<>();
            for (final Direction d : Direction.values()) {
                final int nr = r + d.getRowOffset();
                final int nc = c + d.getColOffset();
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                        && !visited[nr][nc]) {
                    options.add(d);
                }
            }

            if (options.isEmpty()) {
                stack.pop();
            } else {
                final Direction chosen = options.get(
                        myRandom.nextInt(options.size()));
                final int nr = r + chosen.getRowOffset();
                final int nc = c + chosen.getColOffset();

                theDungeon.getRoom(r, c).setDoor(chosen, true);
                theDungeon.getRoom(nr, nc).setDoor(chosen.opposite(), true);

                visited[nr][nc] = true;
                stack.push(new int[] {nr, nc});
            }
        }
    }

    /**
     * Marks the entrance in the top-left corner and the exit in the
     * bottom-right corner. Both rooms get any other content cleared
     * out, and the hero is placed on the entrance.
     *
     * @param theDungeon the dungeon to mark
     */
    private void placeEntranceAndExit(final Dungeon theDungeon) {
        final Room entrance = theDungeon.getRoom(0, 0);
        final Room exit = theDungeon.getRoom(
                theDungeon.getRows() - 1, theDungeon.getCols() - 1);

        entrance.setEntrance(true);
        clearRoomContents(entrance);

        exit.setExit(true);
        clearRoomContents(exit);

        theDungeon.setHeroPosition(0, 0);
    }

    /**
     * Places the four pillars in distinct random rooms that are not
     * the entrance and not the exit.
     *
     * @param theDungeon the dungeon to populate
     */
    private void placePillars(final Dungeon theDungeon) {
        final List<Room> candidates = new ArrayList<>();
        for (int r = 0; r < theDungeon.getRows(); r++) {
            for (int c = 0; c < theDungeon.getCols(); c++) {
                final Room room = theDungeon.getRoom(r, c);
                if (!room.hasEntrance() && !room.hasExit()) {
                    candidates.add(room);
                }
            }
        }
        Collections.shuffle(candidates, myRandom);

        final Pillar[] pillars = Pillar.values();
        for (int i = 0; i < pillars.length && i < candidates.size(); i++) {
            final Room target = candidates.get(i);
            clearRoomContents(target);
            target.setPillar(pillars[i]);
        }
    }

    /**
     * Walks every normal room and rolls independent probabilities
     * for a pit, a healing potion, a vision potion, and a bomb.
     *
     * @param theDungeon the dungeon to populate
     */
    private void placeRandomContent(final Dungeon theDungeon) {
        for (int r = 0; r < theDungeon.getRows(); r++) {
            for (int c = 0; c < theDungeon.getCols(); c++) {
                final Room room = theDungeon.getRoom(r, c);
                if (room.hasEntrance() || room.hasExit()
                        || room.getPillar() != null) {
                    continue;
                }
                if (myRandom.nextDouble() < myItemChance) {
                    final int dmg = PIT_MIN_DAMAGE
                            + myRandom.nextInt(PIT_MAX_DAMAGE - PIT_MIN_DAMAGE + 1);
                    room.setPit(dmg);
                }
                if (myRandom.nextDouble() < myItemChance) {
                    room.setHealingPotion(new HealingPotion());
                }
                if (myRandom.nextDouble() < myItemChance) {
                    room.setVisionPotion(new VisionPotion());
                }
                if (myRandom.nextDouble() < myItemChance) {
                    room.setBomb(new Bomb());
                }
            }
        }
    }

    /**
     * Removes every item, pit, and monster from the given room while
     * leaving its position and doors alone.
     *
     * @param theRoom the room to clear
     */
    private static void clearRoomContents(final Room theRoom) {
        theRoom.setHealingPotion(null);
        theRoom.setVisionPotion(null);
        theRoom.setBomb(null);
        theRoom.setPillar(null);
        theRoom.setMonster(null);
        theRoom.clearPit();
    }
}
