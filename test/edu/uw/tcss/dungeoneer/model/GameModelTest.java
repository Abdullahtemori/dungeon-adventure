package edu.uw.tcss.dungeoneer.model;

import edu.uw.tcss.dungeoneer.model.Difficulty;
import edu.uw.tcss.dungeoneer.model.GameModel;
import edu.uw.tcss.dungeoneer.model.Hero;
//import edu.uw.tcss.dungeoneer.model.HeroFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameModel.
 * Tests cover default state, setters, property change firing,
 * getters, and toString output.
 *
 * @author Daniella Birungi
 * @version Iteration 2
 */
class GameModelTest {

    /** The GameModel instance used in each test. */
    private GameModel myModel;

    /** A hero created by HeroFactory for use in tests. */
    private Hero myHero;

    /**
     * Creates a fresh GameModel before each test so tests
     * do not interfere with each other.
     */
    @BeforeEach
    void setUp() {
       //final HeroFactory factory = new HeroFactory();
       // myHero = factory.createHero("Warrior", "TestHero");

        // Pass null for dungeon — dungeon tests belong in DungeonTest
        myModel = new GameModel(null, myHero, Difficulty.MEDIUM);
    }


    /**
     * Tests that the game is not over at the start.
     */
    @Test
    void testGameOverDefaultFalse() {
        assertFalse(myModel.isGameOver(),
                "Game should not be over at the start");
    }

    /**
     * Tests that the player has not won at the start.
     */
    @Test
    void testPlayerWonDefaultFalse() {
        assertFalse(myModel.isPlayerWon(),
                "Player should not have won at the start");
    }

    /**
     * Tests that the hero is returned correctly.
     */
    @Test
    void testGetHeroReturnsCorrectHero() {
        assertEquals(myHero, myModel.getHero(),
                "Model should return the same hero it was given");
    }

    /**
     * Tests that difficulty is returned correctly.
     */
    @Test
    void testGetDifficultyReturnsCorrectDifficulty() {
        assertEquals(Difficulty.MEDIUM, myModel.getDifficulty(),
                "Difficulty should be MEDIUM");
    }

    /**
     * Tests that dungeon is null when passed as null.
     */
    @Test
    void testGetDungeonNullWhenNotSet() {
        assertNull(myModel.getDungeon(),
                "Dungeon should be null when not provided");
    }


    /**
     * Tests that setGameOver changes the flag to true.
     */
    @Test
    void testSetGameOverTrue() {
        myModel.setGameOver(true);
        assertTrue(myModel.isGameOver(),
                "Game should be over after setGameOver(true)");
    }

    /**
     * Tests that setGameOver can be set back to false.
     */
    @Test
    void testSetGameOverFalse() {
        myModel.setGameOver(true);
        myModel.setGameOver(false);
        assertFalse(myModel.isGameOver(),
                "Game should not be over after setGameOver(false)");
    }

    /**
     * Tests that setPlayerWon changes the flag to true.
     */
    @Test
    void testSetPlayerWonTrue() {
        myModel.setPlayerWon(true);
        assertTrue(myModel.isPlayerWon(),
                "Player should have won after setPlayerWon(true)");
    }

    /**
     * Tests that setPlayerWon can be set back to false.
     */
    @Test
    void testSetPlayerWonFalse() {
        myModel.setPlayerWon(true);
        myModel.setPlayerWon(false);
        assertFalse(myModel.isPlayerWon(),
                "Player should not have won after setPlayerWon(false)");
    }


    /**
     * Tests that a registered listener is notified when
     * gameOver changes. Uses a boolean array to capture the
     * event since lambda cannot modify a plain boolean variable.
     */
    @Test
    void testPropertyChangeListenerNotifiedOnGameOver() {
        // Use array so lambda can modify it
        final boolean[] eventFired = {false};

        // Register a listener that sets the flag when notified
        myModel.addPropertyChangeListener(evt -> {
            if ("gameOver".equals(evt.getPropertyName())) {
                eventFired[0] = true;
            }
        });

        // Trigger the change
        myModel.setGameOver(true);

        assertTrue(eventFired[0],
                "Listener should be notified when gameOver changes");
    }

    /**
     * Tests that a registered listener is notified when
     * playerWon changes.
     */
    @Test
    void testPropertyChangeListenerNotifiedOnPlayerWon() {
        final boolean[] eventFired = {false};

        myModel.addPropertyChangeListener(evt -> {
            if ("playerWon".equals(evt.getPropertyName())) {
                eventFired[0] = true;
            }
        });

        myModel.setPlayerWon(true);

        assertTrue(eventFired[0],
                "Listener should be notified when playerWon changes");
    }

    /**
     * Tests that a removed listener is no longer notified.
     */
    @Test
    void testRemovedListenerNotNotified() {
        final boolean[] eventFired = {false};

        final java.beans.PropertyChangeListener listener = evt ->
                eventFired[0] = true;

        myModel.addPropertyChangeListener(listener);
        myModel.removePropertyChangeListener(listener);

        // Trigger a change — listener should NOT fire
        myModel.setGameOver(true);

        assertFalse(eventFired[0],
                "Removed listener should not be notified");
    }


    /**
     * Tests that toString is not null or empty.
     */
    @Test
    void testToStringNotNull() {
        assertNotNull(myModel.toString(),
                "toString should not return null");
        assertFalse(myModel.toString().isEmpty(),
                "toString should not be empty");
    }

    /**
     * Tests that toString contains the difficulty.
     */
    @Test
    void testToStringContainsDifficulty() {
        assertTrue(myModel.toString().contains("MEDIUM"),
                "toString should contain the difficulty level");
    }
}