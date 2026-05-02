package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Pillar.
 * Tests cover that all the four pillars display correctly.
 *
 * @author Daniella Birungi
 * @version 1.0
 */
class PillarTest {
    /**
     * Tests that Abstraction pillar has display char 'A'.
     */
    @Test
    void testAbstractionDisplayChar() {
        assertEquals('A', Pillar.ABSTRACTION.getDisplayCharacter(),
                "ABSTRACTION display char should be 'A'");
    }

    /**
     * Tests that Encapsulation pillar has display char 'E'.
     */
    @Test
    void testEncapsulationDisplayChar() {
        assertEquals('E', Pillar.ENCAPSULATION.getDisplayCharacter(),
                "ENCAPSULATION display char should be 'E'");
    }

    /**
     * Tests that Inheritance pillar has display char 'I'.
     */
    @Test
    void testInheritanceDisplayChar() {
        assertEquals('I', Pillar.INHERITANCE.getDisplayCharacter(),
                "INHERITANCE display char should be 'I'");
    }

    /**
     * Tests that Polymorphism pillar has display char 'P'.
     */
    @Test
    void testPolymorphismDisplayChar() {
        assertEquals('P', Pillar.POLYMORPHISM.getDisplayCharacter(),
                "POLYMORPHISM display char should be 'P'");
    }

    /**
     * Tests that there are exactly 4 Pillar values.
     */
    @Test
    void testFourPillarsExist() {
        assertEquals(4, Pillar.values().length,
                "There should be exactly 4 pillars");
    }

}