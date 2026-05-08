package edu.uw.tcss.dungeoneer.model;


import edu.uw.tcss.dungeoneer.model.Difficulty;
import edu.uw.tcss.dungeoneer.model.GameModel;
import edu.uw.tcss.dungeoneer.model.Hero;
//import edu.uw.tcss.dungeoneer.model.HeroFactory;
import edu.uw.tcss.dungeoneer.model.SaveLoadManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SaveLoadManager.
 * Tests cover saving, loading, file existence checks,
 * and handling of corrupted or missing files.
 *
 * @author Daniella Birungi
 * @version Iteration 2
 */
class SaveLoadManagerTest {

    /** Path used for test save files. */
    private static final String TEST_SAVE_PATH = "test_save.sav";

    /** A GameModel used in save/load tests. */
    private GameModel myModel;

    /**
     * Creates a fresh GameModel before each test.
     */
    @BeforeEach
    void setUp() {
//        final HeroFactory factory = new HeroFactory();
//        final Hero hero = factory.createHero("Warrior", "TestHero");
       // myModel = new GameModel(null, myHero, Difficulty.MEDIUM);
    }

    /**
     * Deletes the test save file after each test so files
     * do not accumulate between test runs.
     */
    @AfterEach
    void tearDown() {
        final File file = new File(TEST_SAVE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }


    /**
     * Tests that saveGame returns true on success.
     */
    @Test
    void testSaveGameReturnsTrue() {
        assertTrue(SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH),
                "saveGame should return true on success");
    }

    /**
     * Tests that the save file actually exists after saving.
     */
    @Test
    void testSaveGameCreatesFile() {
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        assertTrue(new File(TEST_SAVE_PATH).exists(),
                "Save file should exist after saving");
    }


    /**
     * Tests that a saved model can be loaded back successfully.
     */
    @Test
    void testLoadGameReturnsModel() {
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded =
                SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded,
                "Loaded model should not be null");
    }

    /**
     * Tests that the loaded model has the correct difficulty.
     */
    @Test
    void testLoadGamePreservesDifficulty() {
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded =
                SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded);
        assertEquals(Difficulty.MEDIUM, loaded.getDifficulty(),
                "Loaded model should have NORMAL difficulty");
    }

    /**
     * Tests that the loaded model preserves gameOver state.
     */
    @Test
    void testLoadGamePreservesGameOverState() {
        myModel.setGameOver(true);
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded =
                SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded);
        assertTrue(loaded.isGameOver(),
                "Loaded model should preserve gameOver = true");
    }

    /**
     * Tests that the loaded model preserves playerWon state.
     */
    @Test
    void testLoadGamePreservesPlayerWonState() {
        myModel.setPlayerWon(true);
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded =
                SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded);
        assertTrue(loaded.isPlayerWon(),
                "Loaded model should preserve playerWon = true");
    }

    /**
     * Tests that loading a non-existent file returns null.
     */
    @Test
    void testLoadGameReturnsNullIfFileNotFound() {
        final GameModel loaded =
                SaveLoadManager.loadGame("nonexistent_file.sav");
        assertNull(loaded,
                "Loading a missing file should return null");
    }


    /**
     * Tests that saveExists returns false before saving.
     */
    @Test
    void testSaveExistsReturnsFalseBeforeSave() {
        assertFalse(SaveLoadManager.saveExists(TEST_SAVE_PATH),
                "saveExists should be false before saving");
    }

    /**
     * Tests that saveExists returns true after saving.
     */
    @Test
    void testSaveExistsReturnsTrueAfterSave() {
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        assertTrue(SaveLoadManager.saveExists(TEST_SAVE_PATH),
                "saveExists should be true after saving");
    }
}