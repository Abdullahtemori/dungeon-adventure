package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for the Dungeon Adventure game.
 * These tests simulate gameplay scenarios without a GUI or
 * controller They operate directly on GameModel and Hero to
 * verify that the model layer is correct end to end.
 *
 * @author Daniella Birungi
 * @version Iteration 4
 */
class IntegrationTest {

    /** Save file path used for save/load integration tests. */
    private static final String TEST_SAVE_PATH = "integration_test_save.sav";

    /** The hero used in all tests. */
    private Hero myHero;

    /** The game model used in all tests. */
    private GameModel myModel;

    /**
     * Creates a fresh Warrior hero and GameModel before each test.
     * Dungeon is null. These tests focus on model state and hero
     * behavior, not dungeon generation.
     */
    @BeforeEach
    void setUp() {
        final HeroFactory factory = new HeroFactory();
        myHero = factory.createHero("Warrior", "IntegrationHero");
        myModel = new GameModel(null, myHero, Difficulty.MEDIUM);
    }

    /**
     * Cleans up any test save files after each test.
     */
    @AfterEach
    void tearDown() {
        SaveLoadManager.deleteSave(TEST_SAVE_PATH);
    }


    /**
     * Simulates the full win flow:
     * 1. Hero collects all 4 pillars
     * 2. Controller sets playerWon and gameOver
     * 3. Verifies model state reflects win
     */
    @Test
    void testFullWinScenarioAllPillarsCollected() {
        // Step 1: Collect all 4 pillars
        myHero.addPillar(Pillar.ABSTRACTION);
        myHero.addPillar(Pillar.ENCAPSULATION);
        myHero.addPillar(Pillar.INHERITANCE);
        myHero.addPillar(Pillar.POLYMORPHISM);

        // Step 2: Verify all pillars collected
        assertEquals(4, myHero.getPillarsFound().size(),
                "Hero should have all 4 pillars");

        // Step 3: Simulate controller setting win flags
        myModel.setPlayerWon(true);
        myModel.setGameOver(true);

        // Step 4: Verify model state
        assertTrue(myModel.isPlayerWon(), "playerWon should be true");
        assertTrue(myModel.isGameOver(),  "gameOver should be true");
    }

    /**
     * Tests that collecting only 3 pillars does NOT trigger a win.
     */
    @Test
    void testThreePillarsDoesNotTriggerWin() {
        myHero.addPillar(Pillar.ABSTRACTION);
        myHero.addPillar(Pillar.ENCAPSULATION);
        myHero.addPillar(Pillar.INHERITANCE);
        // POLYMORPHISM not collected

        final boolean hasAll =
                myHero.getPillarsFound().size() == Pillar.values().length;

        assertFalse(hasAll,
                "Win condition should not be met with only 3 pillars");
    }

    /**
     * Tests that all 4 Pillar enum values exist.
     */
    @Test
    void testExactlyFourPillarEnumValues() {
        assertEquals(4, Pillar.values().length,
                "There must be exactly 4 Pillar enum values");
    }

    /**
     * Simulates the full lose flow:
     * 1. Hero takes lethal damage
     * 2. Controller detects hero is dead and sets gameOver
     * 3. Verifies model state reflects loss
     */
    @Test
    void testFullLoseScenarioHeroDies() {
        // Step 1: Kill the hero with massive damage
        myHero.setHitPoints(0);
        assertFalse(myHero.isAlive(),
                "Hero should be dead after lethal damage");

        // Step 2: Simulate controller setting lose flags
        myModel.setGameOver(true);

        // Step 3: playerWon should remain false
        assertTrue(myModel.isGameOver(),   "gameOver should be true");
        assertFalse(myModel.isPlayerWon(), "playerWon should be false on loss");
    }

    /**
     * Tests that hero HP cannot go below zero.
     */
    @Test
    void testHeroHpFloorsAtZero() {
        myHero.setHitPoints(0);
        assertTrue(myHero.getHitPoints() >= 0,
                "Hero HP should not go below zero");
    }

    /**
     * Tests the full healing potion flow:
     * add one potion ->  use it -> count decrements.
     */
    @Test
    void testHealingPotionPickupAndUseFlow() {
        final int initialPotions = myHero.getHealingPotions();

        myHero.addHealingPotion();
        assertEquals(initialPotions + 1, myHero.getHealingPotions(),
                "Potion count should increase after pickup");

        final int healed = myHero.useHealingPotion();
        assertTrue(healed >= 5 && healed <= 15,
                "Healing amount should be 5-15");
        assertEquals(initialPotions, myHero.getHealingPotions(),
                "Potion count should return to initial after use");
    }

    /**
     * Tests the full bomb use flow:
     * add one bomb -> use it against monster -> damage is in range.
     */
    @Test
    void testBombPickupAndUseFlow() {
        myHero.addBomb();
        assertEquals(1, myHero.getBombs(),
                "Bomb count should be 1 after pickup");

        // Need a monster to bomb
        final edu.uw.tcss.dungeoneer.model.Ogre ogre =
                new edu.uw.tcss.dungeoneer.model.Ogre();
        final int hpBefore = ogre.getHitPoints();

        final int damage = myHero.useBomb(ogre);

        assertTrue(damage >= 75 && damage <= 150,
                "Bomb damage should be 75-150");
        assertEquals(hpBefore - damage, ogre.getHitPoints(),
                "Ogre HP should decrease by bomb damage");
        assertEquals(0, myHero.getBombs(),
                "Bomb count should be 0 after use");
    }

    /**
     * Tests that using a potion when inventory is empty returns 0
     * and does not crash.
     */
    @Test
    void testUsePotionWhenEmptyReturnsZero() {
        // Ensure no potions
        assertEquals(0, myHero.getHealingPotions());

        final int healed = myHero.useHealingPotion();
        assertEquals(0, healed,
                "useHealingPotion with empty inventory should return 0");
    }

    /**
     * Tests that using a vision potion when empty returns false.
     */
    @Test
    void testUseVisionPotionWhenEmptyReturnsFalse() {
        assertEquals(0, myHero.getVisionPotions());
        assertFalse(myHero.useVisionPotion(),
                "useVisionPotion with empty inventory should return false");
    }

    /**
     * Tests the full save/load round-trip preserving hero inventory.
     */
    @Test
    void testSaveLoadRoundTripPreservesHeroInventory() {
        // Set up inventory state
        myHero.addHealingPotion();
        myHero.addHealingPotion();
        myHero.addVisionPotion();
        myHero.addBomb();
        myHero.addPillar(Pillar.ABSTRACTION);
        myHero.addPillar(Pillar.ENCAPSULATION);

        // Save
        final boolean saved = SaveLoadManager.saveGame(
                myModel, TEST_SAVE_PATH);
        assertTrue(saved, "Save should succeed");

        // Load
        final GameModel loaded = SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded, "Loaded model should not be null");

        // Verify hero state is preserved
        final Hero loadedHero = loaded.getHero();
        assertNotNull(loadedHero, "Loaded hero should not be null");
        assertEquals("IntegrationHero", loadedHero.getName(),
                "Hero name should be preserved");
        assertEquals(2, loadedHero.getHealingPotions(),
                "Healing potions should be preserved");
        assertEquals(1, loadedHero.getVisionPotions(),
                "Vision potions should be preserved");
        assertEquals(1, loadedHero.getBombs(),
                "Bombs should be preserved");
        assertEquals(2, loadedHero.getPillarsFound().size(),
                "Pillar count should be preserved");
        assertTrue(loadedHero.getPillarsFound().contains(Pillar.ABSTRACTION),
                "ABSTRACTION pillar should be preserved");
        assertTrue(loadedHero.getPillarsFound().contains(Pillar.ENCAPSULATION),
                "ENCAPSULATION pillar should be preserved");
    }

    /**
     * Tests that game flags are preserved through save/load.
     */
    @Test
    void testSaveLoadRoundTripPreservesGameFlags() {
        myModel.setGameOver(true);
        myModel.setPlayerWon(true);

        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded = SaveLoadManager.loadGame(TEST_SAVE_PATH);

        assertNotNull(loaded);
        assertTrue(loaded.isGameOver(),   "gameOver should be preserved");
        assertTrue(loaded.isPlayerWon(),  "playerWon should be preserved");
        assertEquals(Difficulty.MEDIUM, loaded.getDifficulty(),
                "Difficulty should be preserved");
    }

    /**
     * Tests that PropertyChangeSupport is functional after loading.
     */
    @Test
    void testLoadedModelCanFirePropertyChangeEvents() {
        SaveLoadManager.saveGame(myModel, TEST_SAVE_PATH);
        final GameModel loaded = SaveLoadManager.loadGame(TEST_SAVE_PATH);
        assertNotNull(loaded);

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
     * Tests that setGameOver fires an event with the correct property name.
     */
    @Test
    void testGameOverEventHasCorrectPropertyName() {
        final String[] firedName = {null};
        myModel.addPropertyChangeListener(evt ->
                firedName[0] = evt.getPropertyName());

        myModel.setGameOver(true);

        assertEquals(GameModel.PROP_GAME_OVER, firedName[0],
                "gameOver event should use PROP_GAME_OVER constant");
    }

    /**
     * Tests that setPlayerWon fires an event with the correct property name.
     */
    @Test
    void testPlayerWonEventHasCorrectPropertyName() {
        final String[] firedName = {null};
        myModel.addPropertyChangeListener(evt ->
                firedName[0] = evt.getPropertyName());

        myModel.setPlayerWon(true);

        assertEquals(GameModel.PROP_PLAYER_WON, firedName[0],
                "playerWon event should use PROP_PLAYER_WON constant");
    }

    /**
     * Tests that the event carries the correct old and new values.
     */
    @Test
    void testGameOverEventCarriesCorrectOldAndNewValues() {
        final Object[] oldVal = {null};
        final Object[] newVal = {null};

        myModel.addPropertyChangeListener(evt -> {
            if (GameModel.PROP_GAME_OVER.equals(evt.getPropertyName())) {
                oldVal[0] = evt.getOldValue();
                newVal[0] = evt.getNewValue();
            }
        });

        myModel.setGameOver(true);

        assertEquals(false, oldVal[0], "Old value should be false");
        assertEquals(true,  newVal[0], "New value should be true");
    }

    /**
     * Tests that no event fires when gameOver is set to the same value.
     */
    @Test
    void testNoEventWhenGameOverValueUnchanged() {
        final int[] count = {0};
        myModel.addPropertyChangeListener(evt -> {
            if (GameModel.PROP_GAME_OVER.equals(evt.getPropertyName())) {
                count[0]++;
            }
        });

        // Already false — setting to false again should not fire
        myModel.setGameOver(false);

        assertEquals(0, count[0],
                "No event should fire when value does not change");
    }

    /**
     * Tests that all three hero types are created with correct names.
     */
    @Test
    void testHeroFactoryCreatesAllThreeTypes() {
        final HeroFactory factory = new HeroFactory();

        final Hero warrior  = factory.createHero("Warrior",  "W");
        final Hero priestess = factory.createHero("Priestess", "P");
        final Hero thief    = factory.createHero("Thief",    "T");

        assertNotNull(warrior,   "Warrior should be created");
        assertNotNull(priestess, "Priestess should be created");
        assertNotNull(thief,     "Thief should be created");

        assertEquals("W", warrior.getName());
        assertEquals("P", priestess.getName());
        assertEquals("T", thief.getName());
    }

    /**
     * Tests that an invalid hero type throws IllegalArgumentException.
     */
    @Test
    void testHeroFactoryInvalidTypeThrowsException() {
        final HeroFactory factory = new HeroFactory();
        assertThrows(IllegalArgumentException.class,
                () -> factory.createHero("Dragon", "Test"),
                "Unknown hero type should throw IllegalArgumentException");
    }
}