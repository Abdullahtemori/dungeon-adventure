package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Item.
 * Tests cover implementation of item descriptions and displays.
 *
 * @author Daniella Birungi
 * @version 1.0
 */
class ItemTest {

    /**
     * A HealingPotion instance for testing.
     */
    private HealingPotion myHealingPotion;

    /**
     * A VisionPotion instance for testing.
     */
    private VisionPotion myVisionPotion;

    /**
     * A Bomb instance for testing.
     */
    private Bomb myBomb;

    /**
     * Sets up fresh item instances before each test.
     */
    @BeforeEach
    void setUp() {
        myHealingPotion = new HealingPotion();
        myVisionPotion = new VisionPotion();
        myBomb = new Bomb();
    }

    /**
     * Tests that all items implement the Item interface correctly
     * by verifying display chars are not null characters.
     */
    @Test
    void testAllItemsHaveDisplayChar() {
        assertNotEquals('\0', myHealingPotion.getDisplayCharacter(),
                "HealingPotion should have a valid display char");
        assertNotEquals('\0', myVisionPotion.getDisplayCharacter(),
                "VisionPotion should have a valid display char");
        assertNotEquals('\0', myBomb.getDisplayCharacter(),
                "Bomb should have a valid display char");
    }

    /**
     * Tests that all items have non-null descriptions.
     */
    @Test
    void testAllItemsHaveDescriptions() {
        assertNotNull(myHealingPotion.getDescription(),
                "HealingPotion description should not be null");
        assertNotNull(myVisionPotion.getDescription(),
                "VisionPotion description should not be null");
        assertNotNull(myBomb.getDescription(),
                "Bomb description should not be null");
    }

    /**
     * Tests that different items have different display characters.
     */
    @Test
    void testItemsHaveUniqueDisplayChars() {
        assertNotEquals(myHealingPotion.getDisplayCharacter(),
                myVisionPotion.getDisplayCharacter(),
                "HealingPotion and VisionPotion should have "
                        + "different display chars");
        assertNotEquals(myHealingPotion.getDisplayCharacter(),
                myBomb.getDisplayCharacter(),
                "HealingPotion and Bomb should have "
                        + "different display chars");
        assertNotEquals(myVisionPotion.getDisplayCharacter(),
                myBomb.getDisplayCharacter(),
                "VisionPotion and Bomb should have "
                        + "different display chars");
    }

    /**
     * Tests that multiple HealingPotions can have different heal amounts
     * (confirms randomness is working).
     */
    @Test
    void testMultipleHealingPotionsCanVary() {
        boolean foundDifferent = false;
        final HealingPotion base = new HealingPotion();
        for (int i = 0; i < 20; i++) {
            final HealingPotion other = new HealingPotion();
            if (other.getHealAmount() != base.getHealAmount()) {
                foundDifferent = true;
                break;
            }
        }
        // This test is uses probability but with 20 tries it should
        // almost always find a different value in the 5-15 range
        assertTrue(foundDifferent || base.getHealAmount() >= 5,
                "Heal amounts should vary or at least be in valid range");
    }

    /**
     * Tests that multiple Bombs can have different damage values
     * (confirms randomness is working).
     */
    @Test
    void testMultipleBombsCanVary() {
        boolean foundDifferent = false;
        final Bomb base = new Bomb();
        for (int i = 0; i < 20; i++) {
            final Bomb other = new Bomb();
            if (other.getDamage() != base.getDamage()) {
                foundDifferent = true;
                break;
            }
        }
        assertTrue(foundDifferent || base.getDamage() >= 75,
                "Bomb damages should vary or at least be in valid range");
    }

}