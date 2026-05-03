package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Monster subclasses: Ogre, Gremlin, Skeleton.
 *
 * @author Person 1
 * @version Iteration 1
 */
public class MonsterTest {

    // ── Ogre Tests ─────────────────────────────────────────────

    @Test
    public void testOgreStartingHP() {
        Ogre o = new Ogre();
        assertEquals(200, o.getHitPoints());
    }

    @Test
    public void testOgreName() {
        Ogre o = new Ogre();
        assertEquals("Ogre", o.getName());
    }

    @Test
    public void testOgreAttackSpeed() {
        Ogre o = new Ogre();
        assertEquals(2, o.getAttackSpeed());
    }

    @Test
    public void testOgreChanceToHeal() {
        Ogre o = new Ogre();
        assertEquals(0.1, o.getChanceToHeal(), 0.001);
    }

    @Test
    public void testOgreHealDoesNotOccurWhenDead() {
        Ogre o = new Ogre();
        o.setHitPoints(0);
        o.heal(); // should do nothing
        assertEquals(0, o.getHitPoints(), "Dead monster should not heal");
    }

    @Test
    public void testOgreIsAlive() {
        Ogre o = new Ogre();
        assertTrue(o.isAlive());
    }

    @Test
    public void testOgreDiesAtZeroHP() {
        Ogre o = new Ogre();
        o.setHitPoints(0);
        assertFalse(o.isAlive());
    }

    // ── Gremlin Tests ──────────────────────────────────────────

    @Test
    public void testGremlinStartingHP() {
        Gremlin g = new Gremlin();
        assertEquals(70, g.getHitPoints());
    }

    @Test
    public void testGremlinAttackSpeed() {
        Gremlin g = new Gremlin();
        assertEquals(5, g.getAttackSpeed());
    }

    @Test
    public void testGremlinChanceToHeal() {
        Gremlin g = new Gremlin();
        assertEquals(0.4, g.getChanceToHeal(), 0.001);
    }

    @Test
    public void testGremlinHealIncreasesHP() {
        // Force heal to trigger by running many times
        Gremlin g = new Gremlin();
        g.setHitPoints(10);
        boolean healed = false;
        for (int i = 0; i < 50; i++) {
            int before = g.getHitPoints();
            g.heal();
            if (g.getHitPoints() > before) {
                healed = true;
                break;
            }
        }
        assertTrue(healed, "Gremlin should heal at least once in 50 tries (40% chance)");
    }

    @Test
    public void testGremlinDamageRange() {
        Gremlin g = new Gremlin();
        assertTrue(g.getMinDamage() == 15 && g.getMaxDamage() == 30);
    }

    // ── Skeleton Tests ─────────────────────────────────────────

    @Test
    public void testSkeletonStartingHP() {
        Skeleton s = new Skeleton();
        assertEquals(100, s.getHitPoints());
    }

    @Test
    public void testSkeletonAttackSpeed() {
        Skeleton s = new Skeleton();
        assertEquals(3, s.getAttackSpeed());
    }

    @Test
    public void testSkeletonChanceToHeal() {
        Skeleton s = new Skeleton();
        assertEquals(0.3, s.getChanceToHeal(), 0.001);
    }

    @Test
    public void testSkeletonHealRange() {
        Skeleton s = new Skeleton();
        assertEquals(30, s.getMinHeal());
        assertEquals(50, s.getMaxHeal());
    }

    @Test
    public void testSkeletonIsAliveAfterDamage() {
        Skeleton s = new Skeleton();
        s.setHitPoints(s.getHitPoints() - 50);
        assertTrue(s.isAlive(), "Skeleton should still be alive with 50 HP left");
    }
}
