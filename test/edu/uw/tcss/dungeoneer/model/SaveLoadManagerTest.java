package edu.uw.tcss.dungeoneer.model;

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
        final HeroFactory factory = new HeroFactory();
        final Hero theHero = factory.createHero("Warrior", "TestHero");
        myModel = new GameModel(null, theHero, Difficulty.MEDIUM);
    }

    /**
     * Deletes the test save file after each test so files
     * do not accumulate between test runs.
     */
    @AfterEach
    void tearDown() {
        SaveLoadManager.deleteSave(TEST_SAVE_PATH);
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
     * Tests that passing a null model returns false without crashing.
     */
    @Test
    void testSaveGameNullModelReturnsFalse() {
        final boolean result = SaveLoadManager.saveGame(
                null, TEST_SAVE_PATH);
        assertFalse(result,
                "saveGame with null model should return false");
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
     * Tests that gameOver = false is preserved through save/load.
     */
    @Test
    void testLoadGamePreservesGameOverFalse() {
        // gameOver is false by default so verify it stays false after load
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded =
                SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded, "Loaded model should not be null");
        assertFalse(loaded.isGameOver(),
                "Loaded model should preserve gameOver = false");
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
     * Tests that the hero name survives the save/load round-trip.
     */
    @Test
    void testLoadGamePreservesHeroName() {
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded =
                SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded, "Loaded model should not be null");
        assertNotNull(loaded.getHero(), "Hero should not be null");
        assertEquals("TestHero", loaded.getHero().getName(),
                "Hero name should be preserved after load");
    }

    /**
     * Tests that PropertyChangeSupport is rebuilt after loading.
     */
    @Test
    void testLoadGameRebuildsPcs() {
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded =
                SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded, "Loaded model should not be null");
        assertDoesNotThrow(
                () -> loaded.addPropertyChangeListener(evt -> { }),
                "addPropertyChangeListener should work after loading"
        );
    }

    /**
     * Tests that a loaded model can still fire property change events.
     */
    @Test
    void testLoadedModelCanFirePropertyChangeEvents() {
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded =
                SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded, "Loaded model should not be null");

        final boolean[] fired = {false};
        loaded.addPropertyChangeListener(evt -> {
            if (GameModel.PROP_GAME_OVER.equals(evt.getPropertyName())) {
                fired[0] = true;
            }
        });

        loaded.setGameOver(true);

        assertTrue(fired[0],
                "Loaded model should be able to fire property change events");
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

    /**
     * Tests that deleteSave removes the file and returns true.
     */
    @Test
    void testDeleteSaveRemovesFile() {
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        assertTrue(SaveLoadManager.saveExists(TEST_SAVE_PATH),
                "File should exist before deleting");

        final boolean deleted = SaveLoadManager.deleteSave(TEST_SAVE_PATH);

        assertTrue(deleted,
                "deleteSave should return true on success");
        assertFalse(SaveLoadManager.saveExists(TEST_SAVE_PATH),
                "File should not exist after deletion");
    }

    /**
     * Tests that deleteSave on a non-existent file returns false.
     */
    @Test
    void testDeleteSaveReturnsFalseForMissingFile() {
        final boolean deleted =
                SaveLoadManager.deleteSave("nonexistent.sav");
        assertFalse(deleted,
                "deleteSave on missing file should return false");
    }
}