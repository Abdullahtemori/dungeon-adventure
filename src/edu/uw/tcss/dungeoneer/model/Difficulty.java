package edu.uw.tcss.dungeoneer.model;

/**
 * The difficulty levels for the game. Each level fixes the size of
 * the dungeon grid.
 *
 * Later iterations may also use this enum to scale things like
 * monster strength or item rates. For now it just controls the
 * grid size used by DungeonBuilder.
 *
 * @author Tarik Atasoy
 * @version Iteration 1
 */
public enum Difficulty {

    /** Small dungeon (5 x 5). */
    EASY(5, 5),

    /** Medium dungeon (7 x 7). */
    MEDIUM(7, 7),

    /** Large dungeon (10 x 10). */
    HARD(10, 10);

    /** Number of rows in the dungeon for this difficulty. */
    private final int myRows;

    /** Number of columns in the dungeon for this difficulty. */
    private final int myCols;

    /**
     * Constructs a difficulty with the given grid dimensions.
     *
     * @param theRows the number of rows
     * @param theCols the number of columns
     */
    Difficulty(final int theRows, final int theCols) {
        myRows = theRows;
        myCols = theCols;
    }

    /** @return the number of rows for this difficulty */
    public int getRows() {
        return myRows;
    }

    /** @return the number of columns for this difficulty */
    public int getCols() {
        return myCols;
    }
}
