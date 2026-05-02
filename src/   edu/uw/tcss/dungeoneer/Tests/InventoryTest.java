package edu.uw.tcss.dungeoneer.test;

import edu.uw.tcss.dungeoneer.model.Bomb;
import edu.uw.tcss.dungeoneer.model.HealingPotion;
import edu.uw.tcss.dungeoneer.model.Pillar;
import edu.uw.tcss.dungeoneer.model.VisionPotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Item and Inventory system.
 * Test cover item creation, display characters, descriptions,
 * damage/heal ranges and inventory behavviour.
 *
 * @author Daniella Birungi
 * @version 1.0
 */

class InventoryTest {
    /**
     * A healing potion instance for testing.
     */
    private HealingPotion myHealingPotion;

    /**
     * A vision potion instance for testing.
     */
    private VisionPotion myVisionPotion;

    /**
     * A bomb instance for testing.
     */
    private Bomb myBomb;

    //--------------------------------------------------
    // SETUP
    //--------------------------------------------------

    /**
     * Sets up fresh item instances before each test.
     */
    @BeforeEach
    void setUp(){
        myHealingPotion = new HealingPotion();
        myVisionPotion = new VisionPotion();
        myBomb = new Bomb();
    }

    //--------------------------------------------------
    // Healing potion Tests
    //--------------------------------------------------

    /**
     * Tests that HealingPotion heal amount is with in the valid range
     */
    @Test
    void testHealingPotionHealAmountinRange(){
        final int healAmount = myHealingPotion.getHealAmount();
        assertTrue(healAmount >= 5 and healAmount <= 15,
                "Heal Amount should be between 5 and 15 but was: "
                        + healAmount);
    }

    /**
     * Tests that HealingPotion has the correct display Character
     */
    @Test
    void testHealingPotionDisplayCharcter(){

    }
}