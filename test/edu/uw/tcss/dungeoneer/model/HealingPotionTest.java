package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the HealingPotion.
 * Tests cover descriptions and heal ranges.
 *
 * @author Daniella Birungi
 * @version 1.0
 */

class HealingPotionTest {

    /**
     * A HealingPotion instance for testing.
     */
    private HealingPotion myHealingPotion;

    /**
     * Sets up fresh item instances before each test.
     */
    @BeforeEach
    void setUp() {
        myHealingPotion = new HealingPotion();

    }

    /**
     * Tests that HealingPotion heal amount is within valid range.
     */
    @Test
    void testHealingPotionHealAmountInRange() {
        final int healAmount = myHealingPotion.getHealAmount();
        assertTrue(healAmount >= 5 && healAmount <= 15,
                "Heal amount should be between 5 and 15 but was: "
                        + healAmount);
    }

    /**
     * Tests that HealingPotion has the correct display character.
     */
    @Test
    void testHealingPotionDisplayChar() {
        assertEquals('H', myHealingPotion.getDisplayCharacter(),
                "HealingPotion display char should be 'H'");
    }

    /**
     * Tests that HealingPotion description is not null or empty.
     */
    @Test
    void testHealingPotionDescriptionNotEmpty() {
        assertNotNull(myHealingPotion.getDescription(),
                "Description should not be null");
        assertFalse(myHealingPotion.getDescription().isEmpty(),
                "Description should not be empty");
    }

    /**
     * Tests that a HealingPotion created with a specific amount
     * returns the correct heal amount.
     */
    @Test
    void testHealingPotionSpecificAmount() {
        final HealingPotion potion = new HealingPotion(10);
        assertEquals(10, potion.getHealAmount(),
                "Potion should heal exactly 10 HP");
    }

    /**
     * Tests that HealingPotion toString contains relevant info.
     */
    @Test
    void testHealingPotionToString() {
        assertNotNull(myHealingPotion.toString(),
                "toString should not be null");
        assertTrue(myHealingPotion.toString().contains("HealingPotion"),
                "toString should contain class name");
    }
}