package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the VisionPotion.
 * Tests cover vision presence(non-nullity) and description.
 *
 * @author Daniella Birungi
 * @version 1.0
 */
class VisionPotionTest {

    /**
     * A VisionPotion instance for testing.
     */
    private VisionPotion myVisionPotion;

    /**
     * Sets up fresh item instances before each test.
     */
    @BeforeEach
    void setUp() {
        myVisionPotion = new VisionPotion();

    }

    /**
     * Tests that VisionPotion has the correct display character.
     */
    @Test
    void testVisionPotionDisplayChar() {
        assertEquals('V', myVisionPotion.getDisplayCharacter(),
                "VisionPotion display char should be 'V'");
    }

    /**
     * Tests that VisionPotion description is not null or empty.
     */
    @Test
    void testVisionPotionDescriptionNotEmpty() {
        assertNotNull(myVisionPotion.getDescription(),
                "Description should not be null");
        assertFalse(myVisionPotion.getDescription().isEmpty(),
                "Description should not be empty");
    }

    /**
     * Tests that VisionPotion toString is not null.
     */
    @Test
    void testVisionPotionToString() {
        assertNotNull(myVisionPotion.toString(),
                "toString should not be null");
    }
}