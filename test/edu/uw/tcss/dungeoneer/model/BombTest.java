package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Bomb.
 * Tests cover damage range and description.
 *
 * @author Daniella Birungi
 * @version 1.0
 */
class BombTest {

    /**
     * A Bomb instance for testing.
     */
    private Bomb myBomb;

    /**
     * Sets up fresh item instances before each test.
     */
    @BeforeEach
    void setUp() {
        myBomb = new Bomb();

    }

    /**
     * Tests that Bomb damage is within valid range.
     */
    @Test
    void testBombDamageInRange() {
        final int damage = myBomb.getDamage();
        assertTrue(damage >= 75 && damage <= 150,
                "Bomb damage should be between 75 and 150 but was: "
                        + damage);
    }

    /**
     * Tests that Bomb has the correct display character.
     */
    @Test
    void testBombDisplayChar() {
        assertEquals('B', myBomb.getDisplayCharacter(),
                "Bomb display char should be 'B'");
    }

    /**
     * Tests that Bomb description is not null or empty.
     */
    @Test
    void testBombDescriptionNotEmpty() {
        assertNotNull(myBomb.getDescription(),
                "Description should not be null");
        assertFalse(myBomb.getDescription().isEmpty(),
                "Description should not be empty");
    }

    /**
     * Tests that a Bomb created with a specific damage value
     * returns the correct damage.
     */
    @Test
    void testBombSpecificDamage() {
        final Bomb bomb = new Bomb(100);
        assertEquals(100, bomb.getDamage(),
                "Bomb should deal exactly 100 damage");
    }

    /**
     * Tests that Bomb toString contains relevant info.
     */
    @Test
    void testBombToString() {
        assertNotNull(myBomb.toString(),
                "toString should not be null");
        assertTrue(myBomb.toString().contains("Bomb"),
                "toString should contain class name");
    }

}