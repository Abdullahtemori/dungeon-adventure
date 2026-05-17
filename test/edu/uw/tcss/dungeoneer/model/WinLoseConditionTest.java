package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for win and lose conditions.
 * WHY these tests: The win/lose logic involves multiple classes
 * working together (Hero inventory, GameModel flags, PropertyChange
 * events). Unit tests alone cannot catch bugs that occur at the
 * boundary between these classes.
 *
 * @author Daniella Birungi
 * @version Iteration 3
 */
class WinLoseConditionTest {

    /** The game model used in all tests. */
    private GameModel myModel;

    /** The hero used in all tests. */
    private Hero myHero;

    /**
     * Creates a fresh Warrior hero and GameModel before each test.
     */
    @BeforeEach
    void setUp() {
        final HeroFactory factory = new HeroFactory();
        myHero = factory.createHero("Warrior", "TestHero");

        // Null dungeon — we test game state flags, not dungeon logic
        myModel = new GameModel(null, myHero, Difficulty.MEDIUM);
    }

    /**
     * Tests that isGameOver returns false at game start.
     */
    @Test
    void testGameOverDefaultFalse() {
        assertFalse(myModel.isGameOver(),
                "Game should not be over at the start");
    }

    /**
     * Tests that isPlayerWon returns false at game start.
     */
    @Test
    void testPlayerWonDefaultFalse() {
        assertFalse(myModel.isPlayerWon(),
                "Player should not have won at the start");
    }

    /**
     * Tests that setGameOver(true) changes isGameOver to true.
     */
    @Test
    void testSetGameOverTrueChangesFlag() {
        myModel.setGameOver(true);

        assertTrue(myModel.isGameOver(),
                "isGameOver should be true after setGameOver(true)");
    }

    /**
     * Tests that setPlayerWon(true) changes isPlayerWon to true.
     */
    @Test
    void testSetPlayerWonTrueChangesFlag() {
        myModel.setPlayerWon(true);

        assertTrue(myModel.isPlayerWon(),
                "isPlayerWon should be true after setPlayerWon(true)");
    }

    /**
     * Tests that setGameOver can be toggled back to false.
     */
    @Test
    void testSetGameOverCanBeResetToFalse() {
        myModel.setGameOver(true);
        myModel.setGameOver(false);

        assertFalse(myModel.isGameOver(),
                "isGameOver should be false after setGameOver(false)");
    }


    /**
     * Tests that a hero with fewer than 4 pillars does not
     * satisfy the win condition.
     */
    @Test
    void testHeroWithThreePillarsDoesNotWin() {
        // Collect only 3 of the 4 pillars
        myHero.addPillar(Pillar.ABSTRACTION);
        myHero.addPillar(Pillar.ENCAPSULATION);
        myHero.addPillar(Pillar.INHERITANCE);

        // Win requires all 4 — this should not be satisfied
        final boolean hasAllPillars =
                myHero.getPillarsFound().size() == Pillar.values().length;

        assertFalse(hasAllPillars,
                "Hero with 3 pillars should not satisfy win condition");
    }

    /**
     * Tests that a hero with all 4 pillars satisfies the pillar
     * part of the win condition.
     */
    @Test
    void testHeroWithAllFourPillarsSatisfiesCondition() {
        myHero.addPillar(Pillar.ABSTRACTION);
        myHero.addPillar(Pillar.ENCAPSULATION);
        myHero.addPillar(Pillar.INHERITANCE);
        myHero.addPillar(Pillar.POLYMORPHISM);

        final boolean hasAllPillars =
                myHero.getPillarsFound().size() == Pillar.values().length;

        assertTrue(hasAllPillars,
                "Hero with all 4 pillars should satisfy pillar condition");
    }

    /**
     * Tests that collecting the same pillar twice does not
     * count as two separate pillars.
     */
    @Test
    void testDuplicatePillarNotCountedTwice() {
        myHero.addPillar(Pillar.ABSTRACTION);
        myHero.addPillar(Pillar.ABSTRACTION);
        myHero.addPillar(Pillar.ABSTRACTION);

        assertEquals(1, myHero.getPillarsFound().size(),
                "Adding the same pillar multiple times should only count once");
    }

    /**
     * Tests that all four pillar enum values exist.
     */
    @Test
    void testFourPillarEnumValuesExist() {
        assertEquals(4, Pillar.values().length,
                "There must be exactly 4 Pillar enum values");
    }


    /**
     * Tests that a hero with full HP is alive.
     */
    @Test
    void testHeroWithFullHpIsAlive() {
        assertTrue(myHero.isAlive(),
                "Hero with full HP should be alive");
    }

    /**
     * Tests that a hero who takes lethal damage is no longer alive.
     */
    @Test
    void testHeroIsDeadAfterLethalDamage() {
        // Hero takes lethal damage and is no longer alive
        myHero.setHitPoints(0);

        assertFalse(myHero.isAlive(),
                "Hero should not be alive after lethal damage");
    }

    /**
     * Tests that HP cannot go below zero from excessive damage.
     */
    @Test
    void testHeroHpDoesNotGoBelowZero() {
        // Force the health below zero to test the defensive bounding limits if applicable or
        // simulate taking massive damage relative to current health
        myHero.setHitPoints(myHero.getHitPoints() - 10000);

        assertTrue(myHero.getHitPoints() >= 0,
                "Hero HP should not go below zero");
    }


    /**
     * Tests that setting gameOver fires a property change event
     * with the correct property name.
     */
    @Test
    void testSetGameOverFiresCorrectPropertyName() {
        final String[] firedProperty = {null};

        myModel.addPropertyChangeListener(evt ->
                firedProperty[0] = evt.getPropertyName()
        );

        myModel.setGameOver(true);

        assertEquals(GameModel.PROP_GAME_OVER, firedProperty[0],
                "setGameOver should fire event with name 'gameOver'");
    }

    /**
     * Tests that setting playerWon fires a property change event
     * with the correct property name.
     */
    @Test
    void testSetPlayerWonFiresCorrectPropertyName() {
        final String[] firedProperty = {null};

        myModel.addPropertyChangeListener(evt ->
                firedProperty[0] = evt.getPropertyName()
        );

        myModel.setPlayerWon(true);

        assertEquals(GameModel.PROP_PLAYER_WON, firedProperty[0],
                "setPlayerWon should fire event with name 'playerWon'");
    }

    /**
     * Tests that the old value in the gameOver event is false
     * and the new value is true.
     */
    @Test
    void testSetGameOverEventCarriesCorrectValues() {
        final Object[] oldVal = {null};
        final Object[] newVal = {null};

        myModel.addPropertyChangeListener(evt -> {
            if (GameModel.PROP_GAME_OVER.equals(evt.getPropertyName())) {
                oldVal[0] = evt.getOldValue();
                newVal[0] = evt.getNewValue();
            }
        });

        myModel.setGameOver(true);

        assertEquals(false, oldVal[0],
                "Old value should be false before setGameOver(true)");
        assertEquals(true, newVal[0],
                "New value should be true after setGameOver(true)");
    }

    /**
     * Tests that no event fires when gameOver is set to the same value.
     */
    @Test
    void testNoEventFiredWhenGameOverValueUnchanged() {
        final int[] eventCount = {0};

        myModel.addPropertyChangeListener(evt -> {
            if (GameModel.PROP_GAME_OVER.equals(evt.getPropertyName())) {
                eventCount[0]++;
            }
        });

        // Set to false when it is already false — should not fire
        myModel.setGameOver(false);

        assertEquals(0, eventCount[0],
                "No event should fire when value does not change");
    }

    /**
     * Tests that a removed listener is not notified.
     */
    @Test
    void testRemovedListenerNotNotified() {
        final boolean[] notified = {false};

        final java.beans.PropertyChangeListener listener =
                evt -> notified[0] = true;

        myModel.addPropertyChangeListener(listener);
        myModel.removePropertyChangeListener(listener);

        // Trigger a change — removed listener should not fire
        myModel.setGameOver(true);

        assertFalse(notified[0],
                "Removed listener should not be notified of changes");
    }


    /**
     * Tests the full win scenario: collect all pillars then trigger
     * the win flag. Simulates what GameController.checkWinLose()
     * does when the hero reaches the exit with all pillars.
     */
    @Test
    void testFullWinScenario() {
        // Step 1: Collect all 4 pillars
        myHero.addPillar(Pillar.ABSTRACTION);
        myHero.addPillar(Pillar.ENCAPSULATION);
        myHero.addPillar(Pillar.INHERITANCE);
        myHero.addPillar(Pillar.POLYMORPHISM);

        // Step 2: Verify all pillars collected
        assertEquals(4, myHero.getPillarsFound().size(),
                "Hero should have 4 pillars");

        // Step 3: Simulate GameController setting win flags
        myModel.setPlayerWon(true);
        myModel.setGameOver(true);

        // Step 4: Verify both flags
        assertTrue(myModel.isPlayerWon(),
                "Player won should be true");
        assertTrue(myModel.isGameOver(),
                "Game over should be true");
    }

    /**
     * Tests the full lose scenario: hero takes lethal damage
     * then GameController sets the loss flags.
     */
    @Test
    void testFullLoseScenario() {
        // Step 1: Hero takes lethal damage
        myHero.setHitPoints(0);

        // Step 2: Verify hero is dead
        assertFalse(myHero.isAlive(),
                "Hero should be dead");

        // Step 3: Simulate GameController setting lose flags
        myModel.setGameOver(true);

        // Step 4: Verify flags
        assertTrue(myModel.isGameOver(),
                "Game over should be true");
        assertFalse(myModel.isPlayerWon(),
                "Player won should remain false on a loss");
    }
}

