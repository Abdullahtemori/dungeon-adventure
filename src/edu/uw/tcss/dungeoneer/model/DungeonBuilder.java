package edu.uw.tcss.dungeoneer.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Builds a randomly generated Dungeon. Callers pick a difficulty
 * (which sets the grid size) and then call build(). A custom grid
 * size can also be supplied with setSize, which overrides the
 * difficulty-derived dimensions.
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
 *   5. Populate rooms with monsters drawn from the MonsterFactory.
 *      Pillar rooms, the exit, and rooms next to the exit always
 *      receive a strong monster (Ogre). The entrance never gets
 *      a monster. The spawn rate for normal rooms is per-difficulty
 *      and may be overridden via monsterChance.
 *   6. Place the hero on the entrance.
 *
 * After population the layout is verified with a BFS traversability
 * check. If the check fails the dungeon is discarded and rebuilt.
 *
 * @author Tarik Atasoy
 * @version Iteration 4
 */
public class DungeonBuilder {

    /** Default chance per item type for placement in a normal room. */
    private static final double DEFAULT_ITEM_CHANCE = 0.10;

    /** Minimum damage a pit can deal. */
    private static final int PIT_MIN_DAMAGE = 1;

    /** Maximum damage a pit can deal. */
    private static final int PIT_MAX_DAMAGE = 20;

    /** Sentinel meaning "no custom size set; fall back to difficulty". */
    private static final int SIZE_UNSET = -1;

    /** Default monster spawn chance for normal rooms, per difficulty. */
    private static final Map<Difficulty, Double> DEFAULT_MONSTER_CHANCE;
    static {
        DEFAULT_MONSTER_CHANCE = new EnumMap<>(Difficulty.class);
        DEFAULT_MONSTER_CHANCE.put(Difficulty.EASY, 0.15);
        DEFAULT_MONSTER_CHANCE.put(Difficulty.MEDIUM, 0.25);
        DEFAULT_MONSTER_CHANCE.put(Difficulty.HARD, 0.35);
    }

    /** Difficulty for the next dungeon. */
    private Difficulty myDifficulty = Difficulty.MEDIUM;

    /** Custom row count, or SIZE_UNSET to use the difficulty value. */
    private int myCustomRows = SIZE_UNSET;

    /** Custom column count, or SIZE_UNSET to use the difficulty value. */
    private int myCustomCols = SIZE_UNSET;

    /** Probability for each item type in a normal room. */
    private double myItemChance = DEFAULT_ITEM_CHANCE;

    /**
     * Monster spawn chance override for normal rooms, or null to
     * fall back to the per-difficulty default.
     */
    private Double myMonsterChance;

    /**
     * Supplier for normal-room monsters. Defaults to the shared
     * MonsterFactory's createRandom() on first use; tests can inject
     * a stub to avoid hitting the database.
     */
    private Supplier<Monster> myRandomMonsterSupplier;

    /**
     * Supplier for tougher monsters used in pillar, exit, and exit
     * adjacent rooms. Defaults to MonsterFactory.createByName("Ogre").
     */
    private Supplier<Monster> myStrongMonsterSupplier;

    /**
     * Lazily constructed MonsterFactory shared by the default
     * suppliers. Built on first build() so unit tests that never
     * call build() avoid the SQLite load.
     */
    private MonsterFactory myMonsterFactory;

    /** Random source. Can be swapped out so tests stay deterministic. */
    private Random myRandom = new Random();

    /**
     * Creates a new builder with the default difficulty, item
     * chance, and random source. All other settings can be adjusted
     * through the fluent setter methods before calling build().
     */
    public DungeonBuilder() {
        // Field defaults already cover initialization.
    }

    /**
     * Sets the difficulty (and therefore grid size) for the next
     * build. If setSize has also been called, the custom size wins.
     *
     * @param theDifficulty the difficulty to use
     * @return this builder, for chaining
     */
    public DungeonBuilder setDifficulty(final Difficulty theDifficulty) {
        if (theDifficulty == null) {
            throw new IllegalArgumentException(
                    "Difficulty must not be null.");
        }
        myDifficulty = theDifficulty;
        return this;
    }

    /**
     * Overrides the difficulty-derived grid size with an explicit
     * row and column count. Useful for custom builds and tests.
     *
     * @param theRows the number of rows (must be positive)
     * @param theCols the number of columns (must be positive)
     * @return this builder, for chaining
     */
    public DungeonBuilder setSize(final int theRows, final int theCols) {
        if (theRows <= 0 || theCols <= 0) {
            throw new IllegalArgumentException(
                    "Dungeon size must be positive.");
        }
        myCustomRows = theRows;
        myCustomCols = theCols;
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
     * Overrides the per-difficulty default monster spawn chance for
     * normal rooms. Pillar rooms, the exit, and exit-adjacent rooms
     * always get a monster regardless of this value.
     *
     * @param theChance probability between 0.0 and 1.0
     * @return this builder, for chaining
     */
    public DungeonBuilder monsterChance(final double theChance) {
        if (theChance < 0.0 || theChance > 1.0) {
            throw new IllegalArgumentException(
                    "Monster chance must be between 0.0 and 1.0.");
        }
        myMonsterChance = theChance;
        return this;
    }

    /**
     * Injects the supplier used for normal-room monsters. Intended
     * for tests that need deterministic placement without touching
     * the SQLite database.
     *
     * @param theSupplier the supplier to use
     * @return this builder, for chaining
     */
    public DungeonBuilder monsterSupplier(final Supplier<Monster> theSupplier) {
        myRandomMonsterSupplier = theSupplier;
        return this;
    }

    /**
     * Injects the supplier used for strong (guardian) monsters in
     * pillar rooms, the exit, and exit-adjacent rooms.
     *
     * @param theSupplier the supplier to use
     * @return this builder, for chaining
     */
    public DungeonBuilder strongMonsterSupplier(
            final Supplier<Monster> theSupplier) {
        myStrongMonsterSupplier = theSupplier;
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
            final int rows = myCustomRows == SIZE_UNSET
                    ? myDifficulty.getRows() : myCustomRows;
            final int cols = myCustomCols == SIZE_UNSET
                    ? myDifficulty.getCols() : myCustomCols;
            final Dungeon dungeon = new Dungeon(rows, cols);

            carveMaze(dungeon);
            placeEntranceAndExit(dungeon);
            placePillars(dungeon);
            placeRandomContent(dungeon);
            placeMonsters(dungeon);

            if (dungeon.isTraversable()) {
                return dungeon;
            }
        }
    }

    /**
     * Populates the given dungeon with monsters. Rules:
     *   - The entrance room never gets a monster.
     *   - Every pillar room gets a strong (Ogre) guardian.
     *   - The exit room and every room orthogonally adjacent to the
     *     exit get a strong guardian.
     *   - All other rooms have a monster placed with probability
     *     equal to the effective monster chance (per difficulty or
     *     the explicit override). Those use MonsterFactory.createRandom().
     *
     * Public so external callers (e.g. a legacy MonsterPlacer
     * wrapper) can repopulate an existing dungeon without going
     * through a full build().
     *
     * @param theDungeon the dungeon to populate
     */
    public void placeMonsters(final Dungeon theDungeon) {
        ensureMonsterSuppliers();
        final int rows = theDungeon.getRows();
        final int cols = theDungeon.getCols();
        final int exitRow = rows - 1;
        final int exitCol = cols - 1;
        final double chance = effectiveMonsterChance();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final Room room = theDungeon.getRoom(r, c);
                if (room.hasEntrance()) {
                    room.setMonster(null);
                    continue;
                }
                if (room.hasExit() || room.getPillar() != null
                        || isOrthogonallyAdjacent(r, c, exitRow, exitCol)) {
                    room.setMonster(myStrongMonsterSupplier.get());
                    continue;
                }
                if (myRandom.nextDouble() < chance) {
                    room.setMonster(myRandomMonsterSupplier.get());
                } else {
                    room.setMonster(null);
                }
            }
        }
    }

    /**
     * Resolves the effective monster spawn chance for the current
     * configuration. Explicit override wins over the difficulty
     * default.
     *
     * @return chance in [0.0, 1.0]
     */
    private double effectiveMonsterChance() {
        if (myMonsterChance != null) {
            return myMonsterChance;
        }
        return DEFAULT_MONSTER_CHANCE.getOrDefault(myDifficulty, 0.25);
    }

    /**
     * Initializes the default monster suppliers on first use. The
     * MonsterFactory is built lazily so the SQLite database is only
     * touched when a real dungeon is being built.
     */
    private void ensureMonsterSuppliers() {
        if (myRandomMonsterSupplier != null
                && myStrongMonsterSupplier != null) {
            return;
        }
        if (myMonsterFactory == null) {
            myMonsterFactory = new MonsterFactory();
        }
        if (myRandomMonsterSupplier == null) {
            myRandomMonsterSupplier = myMonsterFactory::createRandom;
        }
        if (myStrongMonsterSupplier == null) {
            myStrongMonsterSupplier = () -> myMonsterFactory.createByName("Ogre");
        }
    }

    /**
     * Returns true if (theRow, theCol) is orthogonally adjacent to
     * (theTargetRow, theTargetCol) (i.e. shares an edge, not the
     * same cell).
     *
     * @param theRow        source row
     * @param theCol        source column
     * @param theTargetRow  target row
     * @param theTargetCol  target column
     * @return true if the two cells are orthogonal neighbours
     */
    private static boolean isOrthogonallyAdjacent(final int theRow,
                                                  final int theCol,
                                                  final int theTargetRow,
                                                  final int theTargetCol) {
        final int dr = Math.abs(theRow - theTargetRow);
        final int dc = Math.abs(theCol - theTargetCol);
        return dr + dc == 1;
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
