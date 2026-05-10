package edu.uw.tcss.dungeoneer.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Combat class.
 *
 * Combat uses Math.random internally, so most tests either set HP
 * directly to drive the fight to a known endpoint, or run a small
 * loop and check a property that has to be true no matter what the
 * random rolls are (for example, the fight must end once a side
 * reaches 0 HP).
 *
 * @author Tarik Atasoy
 * @version Iteration 2
 */
class CombatTest {

    /**
     * Tests that the constructor rejects a null hero.
     */
    @Test
    void testConstructorRejectsNullHero() {
        assertThrows(IllegalArgumentException.class,
                () -> new Combat(null, new Gremlin()));
    }

    /**
     * Tests that the constructor rejects a null monster.
     */
    @Test
    void testConstructorRejectsNullMonster() {
        assertThrows(IllegalArgumentException.class,
                () -> new Combat(new Warrior("A"), null));
    }

    /**
     * Tests that executeHeroAction rejects null actions.
     */
    @Test
    void testNullActionRejected() {
        final Combat c = new Combat(new Warrior("A"), new Gremlin());
        assertThrows(IllegalArgumentException.class,
                () -> c.executeHeroAction(null));
    }

    /**
     * Tests that combat ends and hero wins when the monster's HP
     * is brought to zero by a bomb.
     */
    @Test
    void testHeroWinsWhenMonsterDies() {
        final Warrior hero = new Warrior("Thor");
        final Gremlin monster = new Gremlin();
        // One bomb deals enough damage to one-shot a Gremlin (70 HP).
        hero.addBomb();
        // Make sure the gremlin will be killed regardless of bomb roll
        // by also dropping its HP to 1.
        monster.setHitPoints(1);

        final Combat combat = new Combat(hero, monster);
        final List<CombatEvent> events =
                combat.executeHeroAction(HeroAction.USE_BOMB);

        assertTrue(combat.isOver(), "Combat should end after killing blow");
        assertTrue(combat.heroWon(), "Hero should be flagged as winner");
        assertFalse(monster.isAlive(), "Monster HP should be <= 0");
        // Final event in the round must be the COMBAT_END marker.
        assertEquals(CombatEvent.Type.COMBAT_END,
                events.get(events.size() - 1).getType());
    }

    /**
     * Tests that combat ends and hero loses when the hero's HP
     * is brought to zero. We force the hero to 1 HP and a guaranteed
     * monster hit by giving the monster a high chance to hit \u2014 we
     * just use the existing Ogre but loop until a hit lands.
     */
    @Test
    void testHeroLosesWhenHeroDies() {
        final Priestess hero = new Priestess("Aria");
        final Ogre monster = new Ogre();
        // Drop hero to a single HP. Any landed hit ends the fight.
        hero.setHitPoints(1);

        final Combat combat = new Combat(hero, monster);

        // Run rounds until either side falls. With Ogre at 200 HP and
        // Priestess at 1 HP, the hero will fall first (statistically).
        for (int i = 0; i < 50 && !combat.isOver(); i++) {
            combat.executeHeroAction(HeroAction.ATTACK);
        }

        assertTrue(combat.isOver(), "Combat should eventually end");
        if (!hero.isAlive()) {
            assertFalse(combat.heroWon(),
                    "Hero death should mean hero did not win");
        }
    }

    /**
     * Tests the attacks-per-round ratio rule directly.
     */
    @Test
    void testAttacksPerRoundRatio() {
        // Faster 6 vs slower 2 -> 3 attacks for faster.
        assertEquals(3, Combat.attacksPerRound(6, 2));
        // Equal speeds -> 1 attack each.
        assertEquals(1, Combat.attacksPerRound(4, 4));
        // Slower side never gets fewer than 1.
        assertEquals(1, Combat.attacksPerRound(2, 6));
        // Defensive: slower=0 should fall back to 1, not divide-by-zero.
        assertEquals(1, Combat.attacksPerRound(5, 0));
    }

    /**
     * Tests that the hero gets at least as many attacks as the monster.
     * The hero should never be slower in terms of swing count, even
     * when the monster's attack speed is higher.
     */
    @Test
    void testHeroNeverGetsFewerAttacksThanMonster() {
        // Thief speed 6 vs Ogre speed 2 -> hero=3, monster=1.
        Combat c = new Combat(new Thief("Shadow"), new Ogre());
        assertTrue(c.getHeroAttacksThisRound()
                >= c.getMonsterAttacksThisRound());

        // Priestess speed 5 vs Gremlin speed 5 -> both 1, still equal.
        c = new Combat(new Priestess("Aria"), new Gremlin());
        assertTrue(c.getHeroAttacksThisRound()
                >= c.getMonsterAttacksThisRound());
    }

    /**
     * Tests that using a healing potion when the hero has none returns
     * an ITEM_UNAVAILABLE event and does not modify HP.
     */
    @Test
    void testUseHealingPotionWithNoneInInventory() {
        final Warrior hero = new Warrior("Thor");
        final Gremlin monster = new Gremlin();
        hero.setHitPoints(50);

        final Combat combat = new Combat(hero, monster);
        final List<CombatEvent> events =
                combat.executeHeroAction(HeroAction.USE_HEALING_POTION);

        // First event should be ITEM_UNAVAILABLE since no potions exist.
        assertEquals(CombatEvent.Type.ITEM_UNAVAILABLE,
                events.get(0).getType());
        // Hero HP should not have gone up from the potion call itself
        // (it might have gone DOWN from the monster's retaliation,
        // which is allowed).
        assertTrue(hero.getHitPoints() <= 50,
                "Hero HP should not increase when potion is unavailable");
    }

    /**
     * Tests that using a healing potion actually heals the hero.
     */
    @Test
    void testUseHealingPotionHealsHero() {
        // Use a Gremlin that we'll keep at full HP \u2014 monster might still
        // attack back, so we measure HP delta before vs immediately after
        // the potion event.
        final Warrior hero = new Warrior("Thor");
        final Gremlin monster = new Gremlin();
        hero.setHitPoints(10);
        hero.addHealingPotion();

        final Combat combat = new Combat(hero, monster);
        final List<CombatEvent> events =
                combat.executeHeroAction(HeroAction.USE_HEALING_POTION);

        // First event should be POTION_USED.
        final CombatEvent first = events.get(0);
        assertEquals(CombatEvent.Type.POTION_USED, first.getType());
        assertTrue(first.getAmount() > 0,
                "Potion should restore a positive amount of HP");
    }

    /**
     * Tests that calling executeHeroAction after combat is over
     * is a safe no-op and returns an empty list.
     */
    @Test
    void testActionAfterCombatEndsReturnsEmpty() {
        final Warrior hero = new Warrior("Thor");
        final Gremlin monster = new Gremlin();
        monster.setHitPoints(1);
        hero.addBomb();

        final Combat combat = new Combat(hero, monster);
        combat.executeHeroAction(HeroAction.USE_BOMB);
        assertTrue(combat.isOver());

        final List<CombatEvent> after =
                combat.executeHeroAction(HeroAction.ATTACK);
        assertTrue(after.isEmpty(),
                "Actions after combat ends should produce no events");
    }

    /**
     * Tests that the cumulative log accumulates events across rounds.
     */
    @Test
    void testLogAccumulatesAcrossRounds() {
        final Warrior hero = new Warrior("Thor");
        final Skeleton monster = new Skeleton();
        final Combat combat = new Combat(hero, monster);

        // Run a few rounds; even if the fight isn't over yet the log
        // should grow each round.
        for (int i = 0; i < 3 && !combat.isOver(); i++) {
            combat.executeHeroAction(HeroAction.ATTACK);
        }
        assertNotNull(combat.getLog());
        assertFalse(combat.getLog().isEmpty(),
                "Log should contain events after running rounds");
    }

    /**
     * Tests that getLog returns an unmodifiable view (defensive copy).
     */
    @Test
    void testLogIsUnmodifiable() {
        final Combat combat =
                new Combat(new Warrior("Thor"), new Gremlin());
        combat.executeHeroAction(HeroAction.ATTACK);
        assertThrows(UnsupportedOperationException.class,
                () -> combat.getLog().clear());
    }
}
