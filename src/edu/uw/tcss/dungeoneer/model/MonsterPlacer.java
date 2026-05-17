package edu.uw.tcss.dungeoneer.model;

/**
 * Legacy entry point for monster placement, kept temporarily so the
 * Main and GameController wiring does not break while the rest of
 * the team is still integrating their iteration 3 changes.
 *
 * The real logic now lives in DungeonBuilder.placeMonsters(Dungeon),
 * which is called automatically during DungeonBuilder.build() and
 * uses MonsterFactory plus a difficulty-aware spawn rate. This class
 * just forwards the call so any existing reference still works.
 *
 * Marked deprecated so future callers prefer using DungeonBuilder
 * directly; once nothing references this class it can be removed.
 *
 * @author Abdullah Temori, Tarik Atasoy
 * @version Iteration 3
 */
@Deprecated
public final class MonsterPlacer {

    /** Utility class, no instances. */
    private MonsterPlacer() { }

    /**
     * Populates the given dungeon with monsters by delegating to
     * DungeonBuilder.placeMonsters(Dungeon).
     *
     * @param theDungeon the dungeon to populate with monsters
     */
    public static void placeMonsters(final Dungeon theDungeon) {
        new DungeonBuilder().placeMonsters(theDungeon);
    }
}
