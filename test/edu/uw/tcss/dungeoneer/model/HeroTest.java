package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Hero subclasses: Warrior, Priestess, Thief.
 *
 * @author Person 1
 * @version Iteration 1
 */
public class HeroTest {

    // ── Warrior Tests ──────────────────────────────────────────

    @Test
    public void testWarriorStartingHP() {
        Warrior w = new Warrior("TestWarrior");
        assertEquals(125, w.getHitPoints(), "Warrior should start with 125 HP");
    }

    @Test
    public void testWarriorName() {
        Warrior w = new Warrior("Thor");
        assertEquals("Thor", w.getName());
    }

    @Test
    public void testWarriorAttackSpeed() {
        Warrior w = new Warrior("Thor");
        assertEquals(4, w.getAttackSpeed());
    }

    @Test
    public void testWarriorChanceToHit() {
        Warrior w = new Warrior("Thor");
        assertEquals(0.8, w.getChanceToHit(), 0.001);
    }

    @Test
    public void testWarriorChanceToBlock() {
        Warrior w = new Warrior("Thor");
        assertEquals(0.2, w.getChanceToBlock(), 0.001);
    }

    @Test
    public void testWarriorIsAlive() {
        Warrior w = new Warrior("Thor");
        assertTrue(w.isAlive());
    }

    @Test
    public void testWarriorDiesAtZeroHP() {
        Warrior w = new Warrior("Thor");
        w.setHitPoints(0);
        assertFalse(w.isAlive());
    }

    @Test
    public void testWarriorCrushingBlowDoesNotExceedMaxDamage() {
        // Run many times — opponent HP should never go below HP - 175
        Warrior w = new Warrior("Thor");
        Skeleton dummy = new Skeleton();
        int startHP = dummy.getHitPoints();
        for (int i = 0; i < 100; i++) {
            dummy.setHitPoints(startHP);
            w.specialSkill(dummy);
            int dmgDealt = startHP - dummy.getHitPoints();
            assertTrue(dmgDealt <= 175, "Crushing Blow max is 175");
        }
    }

    // ── Priestess Tests ────────────────────────────────────────

    @Test
    public void testPriestessStartingHP() {
        Priestess p = new Priestess("Aria");
        assertEquals(75, p.getHitPoints());
    }

    @Test
    public void testPriestessHealIncreasesHP() {
        Priestess p = new Priestess("Aria");
        p.setHitPoints(40);
        int before = p.getHitPoints();
        p.specialSkill(null); // heals self, no opponent needed
        assertTrue(p.getHitPoints() > before, "Heal should increase HP");
    }

    @Test
    public void testPriestessHealMax() {
        Priestess p = new Priestess("Aria");
        p.setHitPoints(1);
        for (int i = 0; i < 100; i++) {
            p.setHitPoints(1);
            p.specialSkill(null);
            assertTrue(p.getHitPoints() <= 51, "Max heal is 50, so HP should be <= 51");
        }
    }

    @Test
    public void testPriestessChanceToBlock() {
        Priestess p = new Priestess("Aria");
        assertEquals(0.3, p.getChanceToBlock(), 0.001);
    }

    // ── Thief Tests ────────────────────────────────────────────

    @Test
    public void testThiefStartingHP() {
        Thief t = new Thief("Shadow");
        assertEquals(75, t.getHitPoints());
    }

    @Test
    public void testThiefAttackSpeed() {
        Thief t = new Thief("Shadow");
        assertEquals(6, t.getAttackSpeed());
    }

    @Test
    public void testThiefChanceToBlock() {
        Thief t = new Thief("Shadow");
        assertEquals(0.4, t.getChanceToBlock(), 0.001);
    }

    @Test
    public void testThiefSpecialSkillDoesNotCrash() {
        Thief t = new Thief("Shadow");
        Ogre o = new Ogre();
        // Should run without throwing any exception
        assertDoesNotThrow(() -> t.specialSkill(o));
    }

    @Test
    public void testThiefDamageRange() {
        Thief t = new Thief("Shadow");
        assertTrue(t.getMinDamage() == 20 && t.getMaxDamage() == 40);
    }
}
