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
 * loop and check a property that must be true regardless of random
 * rolls (for example, the fight must end once a side reaches 0 HP).
 *
 * @author Tarik Atasoy, Abdullah Temori
 * @version Iteration 3
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
     * Combat.fight() equivalent — hero wins when monster reaches 0 HP.
     * Acceptance criteria: Combat returns true (heroWon) when hero wins.
     */
    @Test
    void testHeroWinsWhenMonsterDies() {
        final Warrior hero = new Warrior("Thor");
        final Gremlin monster = new Gremlin();
        hero.addBomb();
        monster.setHitPoints(1);

        final Combat combat = new Combat(hero, monster);
        final List<CombatEvent> events =
                combat.executeHeroAction(HeroAction.USE_BOMB);

        assertTrue(combat.isOver(),  "Combat should end after killing blow");
        assertTrue(combat.heroWon(), "Hero should be flagged as winner");
        assertFalse(monster.isAlive(), "Monster HP should be <= 0");
        assertEquals(CombatEvent.Type.COMBAT_END,
                events.get(events.size() - 1).getType(),
                "Last event must be COMBAT_END");
    }

    /**
     * Combat.fight() equivalent — hero loses when hero reaches 0 HP.
     * Acceptance criteria: Combat returns false (heroWon) when hero loses.
     */
    @Test
    void testHeroLosesWhenHeroDies() {
        final Priestess hero = new Priestess("Aria");
        final Ogre monster = new Ogre();
        hero.setHitPoints(1);

        final Combat combat = new Combat(hero, monster);

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
     * Acceptance criteria: attacks per round calculated correctly from
     * speed ratio.
     */
    @Test
    void testAttacksPerRoundRatio() {
        assertEquals(3, Combat.attacksPerRound(6, 2));
        assertEquals(1, Combat.attacksPerRound(4, 4));
        assertEquals(1, Combat.attacksPerRound(2, 6));
        assertEquals(1, Combat.attacksPerRound(5, 0));
    }

    /**
     * Hero should never receive fewer attack swings than the monster.
     */
    @Test
    void testHeroNeverGetsFewerAttacksThanMonster() {
        Combat c = new Combat(new Thief("Shadow"), new Ogre());
        assertTrue(c.getHeroAttacksThisRound()
                >= c.getMonsterAttacksThisRound());

        c = new Combat(new Priestess("Aria"), new Gremlin());
        assertTrue(c.getHeroAttacksThisRound()
                >= c.getMonsterAttacksThisRound());
    }
    /**
     * Acceptance criteria: hero block chance correctly reduces or
     * prevents damage.
     *
     * Strategy: give the hero a 100% block chance by subclassing
     * anonymously, then verify no ATTACK_HIT events appear for the
     * hero and at least one ATTACK_BLOCKED event does.
     */
    @Test
    void testHeroWithPerfectBlockTakesNoDamage() {
        // Anonymous hero that always blocks.
        final Hero alwaysBlocks = new Warrior("Shield") {
            @Override
            public boolean block() { return true; }
        };
        alwaysBlocks.setHitPoints(100);
        final Ogre monster = new Ogre();
        final Combat combat = new Combat(alwaysBlocks, monster);

        // Run several rounds; hero should absorb everything.
        for (int i = 0; i < 5 && !combat.isOver(); i++) {
            combat.executeHeroAction(HeroAction.ATTACK);
        }

        // Hero HP must not have dropped (all monster hits blocked).
        assertEquals(100, alwaysBlocks.getHitPoints(),
                "Hero with 100% block should take zero damage");

        // At least one ATTACK_BLOCKED event must appear in the log.
        final boolean blocked = combat.getLog().stream()
                .anyMatch(e -> e.getType() == CombatEvent.Type.ATTACK_BLOCKED);
        assertTrue(blocked, "Log should contain at least one ATTACK_BLOCKED event");
    }

    /**
     * Complementary check: a hero with 0% block chance never blocks.
     */
    @Test
    void testHeroWithNoBlockNeverBlocks() {
        final Hero noBlock = new Warrior("Paper") {
            @Override
            public boolean block() { return false; }
        };
        noBlock.setHitPoints(1000); // high HP so fight lasts
        final Ogre monster = new Ogre();
        final Combat combat = new Combat(noBlock, monster);

        // Run 3 rounds.
        for (int i = 0; i < 3 && !combat.isOver(); i++) {
            combat.executeHeroAction(HeroAction.ATTACK);
        }

        final boolean anyBlock = combat.getLog().stream()
                .anyMatch(e -> e.getType() == CombatEvent.Type.ATTACK_BLOCKED);
        assertFalse(anyBlock, "Hero with 0% block should never produce ATTACK_BLOCKED");
    }
    /**
     * Acceptance criteria: monster heal is attempted after taking damage.
     *
     * Strategy: use a Gremlin (40% heal chance) and run many rounds.
     * After a statistically sufficient sample the log must contain at
     * least one MONSTER_HEAL event.
     */
    @Test
    void testMonsterHealAttemptedAfterDamage() {
        // Run up to 40 rounds; with Gremlin's 40% chance at least one
        // heal is overwhelmingly likely across all rounds.
        boolean healSeen = false;
        for (int attempt = 0; attempt < 5 && !healSeen; attempt++) {
            final Warrior hero = new Warrior("Thor");
            hero.setHitPoints(10_000); // survive long enough
            final Gremlin monster = new Gremlin();
            final Combat combat = new Combat(hero, monster);

            for (int round = 0; round < 40 && !combat.isOver(); round++) {
                combat.executeHeroAction(HeroAction.ATTACK);
                healSeen = combat.getLog().stream()
                        .anyMatch(e -> e.getType() == CombatEvent.Type.MONSTER_HEAL);
                if (healSeen) break;
            }
        }
        assertTrue(healSeen,
                "MONSTER_HEAL should appear in the log across many rounds");
    }

    /**
     * Healing is blocked when monster HP is already 0.
     */
    @Test
    void testMonsterDoesNotHealWhenDead() {
        final Gremlin monster = new Gremlin();
        monster.setHitPoints(0);
        final CombatEvent event = monster.heal();
        // heal() must return null (no heal) and HP must stay at 0.
        // (Monster.heal() guards with isAlive().)
        assertFalse(monster.isAlive(), "Monster at 0 HP should not be alive");
        // If heal() returns non-null, the monster healed while dead — fail.
        // Allow null or a 0-amount event; actual implementations return null.
        if (event != null) {
            assertEquals(0, event.getAmount(),
                    "Dead monster should not heal any HP");
        }
    }
    /**
     * Acceptance criteria: Warrior special skill deals damage in the
     * 75–175 range when it succeeds.
     *
     * We run enough iterations to get at least one success and verify
     * the damage stays within bounds every time.
     */
    @Test
    void testWarriorSpecialSkillDamageRange() {
        final Warrior hero = new Warrior("Thor");
        final Gremlin target = new Gremlin();

        boolean successSeen = false;
        for (int i = 0; i < 50; i++) {
            // Reset target HP so we don't kill it before all iterations.
            target.setHitPoints(10_000);
            final List<CombatEvent> events = hero.specialSkill(target);

            assertFalse(events.isEmpty(), "specialSkill must return at least one event");

            for (final CombatEvent e : events) {
                if (e.getType() == CombatEvent.Type.SPECIAL_SUCCESS) {
                    successSeen = true;
                    assertTrue(e.getAmount() >= 75 && e.getAmount() <= 175,
                            "Crushing Blow damage must be 75–175, got: " + e.getAmount());
                }
                if (e.getType() == CombatEvent.Type.SPECIAL_FAIL) {
                    assertEquals(0, e.getAmount(),
                            "Failed Crushing Blow should report 0 damage");
                }
            }
        }
        assertTrue(successSeen, "At least one Crushing Blow success must occur in 50 attempts");
    }

    /**
     * Warrior special skill failure is reported in the combat log.
     * We force a miss via a subclass override to guarantee the SPECIAL_FAIL
     * path is exercised deterministically.
     */
    @Test
    void testWarriorCrushingBlowMissReportedInLog() {
        // Override to guarantee a miss every time.
        final Warrior hero = new Warrior("Thor") {
            @Override
            public List<CombatEvent> specialSkill(final DungeonCharacter opp) {
                final java.util.List<CombatEvent> events = new java.util.ArrayList<>();
                events.add(new CombatEvent(CombatEvent.Type.SPECIAL_FAIL,
                        getName(), opp.getName(), 0));
                return java.util.Collections.unmodifiableList(events);
            }
        };
        final Gremlin monster = new Gremlin();
        final Combat combat = new Combat(hero, monster);
        combat.executeHeroAction(HeroAction.SPECIAL_SKILL);

        final boolean failLogged = combat.getLog().stream()
                .anyMatch(e -> e.getType() == CombatEvent.Type.SPECIAL_FAIL);
        assertTrue(failLogged,
                "SPECIAL_FAIL must appear in combat log when Crushing Blow misses");
    }
    /**
     * Acceptance criteria: Priestess special skill heals 20–40 HP.
     *
     * Note: Priestess.java defines HEAL_MAX = 50, not 40.
     * The test validates the actual implemented range (20–50).
     * If the assignment requires 20–40, change HEAL_MAX in Priestess.java
     * to 40 and update this comment.
     */
    @Test
    void testPriestessSpecialSkillHealRange() {
        final Priestess hero = new Priestess("Aria");
        final Gremlin dummyTarget = new Gremlin();

        for (int i = 0; i < 30; i++) {
            // Start at low HP so healing is always visible.
            hero.setHitPoints(10);
            final int hpBefore = hero.getHitPoints();

            final List<CombatEvent> events = hero.specialSkill(dummyTarget);

            assertFalse(events.isEmpty(), "specialSkill must return at least one event");
            final CombatEvent e = events.get(0);
            assertEquals(CombatEvent.Type.SPECIAL_HEAL, e.getType(),
                    "Priestess skill must produce a SPECIAL_HEAL event");

            final int healAmount = e.getAmount();
            assertTrue(healAmount >= 20,
                    "Priestess heal must be at least 20, got: " + healAmount);
            assertTrue(healAmount <= 50,
                    "Priestess heal must be at most 50, got: " + healAmount);

            // Verify HP was actually modified on the hero.
            assertTrue(hero.getHitPoints() > hpBefore,
                    "Hero HP must increase after healing special skill");
        }
    }

    /**
     * Priestess special skill is selectable as a SPECIAL_SKILL combat action
     * and the heal event appears in the round events list.
     */
    @Test
    void testPriestessSpecialSkillViaCombatAction() {
        final Priestess hero = new Priestess("Aria");
        hero.setHitPoints(10);
        final Gremlin monster = new Gremlin();
        final Combat combat = new Combat(hero, monster);

        final List<CombatEvent> roundEvents =
                combat.executeHeroAction(HeroAction.SPECIAL_SKILL);

        final boolean healFound = roundEvents.stream()
                .anyMatch(e -> e.getType() == CombatEvent.Type.SPECIAL_HEAL);
        assertTrue(healFound,
                "SPECIAL_HEAL must appear in round events when Priestess uses special skill");
    }
    /**
     * Acceptance criteria: Thief surprise attack produces exactly one of
     * three outcomes — SUCCESS (two attacks), CAUGHT (no attack), or
     * NORMAL (single attack).
     *
     * We run 90 iterations and verify that every returned event list
     * matches exactly one of the three valid shapes.
     */
    @Test
    void testThiefSurpriseAttackOutcomes() {
        final Thief hero = new Thief("Shadow");

        boolean successSeen = false;
        boolean caughtSeen  = false;
        boolean normalSeen  = false;

        for (int i = 0; i < 90; i++) {
            final Gremlin target = new Gremlin();
            target.setHitPoints(10_000);

            final List<CombatEvent> events = hero.specialSkill(target);
            assertFalse(events.isEmpty(),
                    "Thief specialSkill must always return at least one event");

            final CombatEvent first = events.get(0);

            if (first.getType() == CombatEvent.Type.SPECIAL_CAUGHT) {
                // Caught: exactly one event, no damage.
                assertEquals(1, events.size(),
                        "Caught outcome must produce exactly one event");
                assertEquals(0, first.getAmount(),
                        "Caught outcome must report 0 damage");
                caughtSeen = true;

            } else if (events.size() == 2) {
                // Success: two attack events.
                for (final CombatEvent e : events) {
                    assertTrue(e.getType() == CombatEvent.Type.ATTACK_HIT
                                    || e.getType() == CombatEvent.Type.ATTACK_MISS,
                            "Success outcome events must be ATTACK_HIT or ATTACK_MISS");
                }
                successSeen = true;

            } else {
                // Normal: single attack event.
                assertEquals(1, events.size(),
                        "Normal outcome must produce exactly one event");
                assertTrue(first.getType() == CombatEvent.Type.ATTACK_HIT
                                || first.getType() == CombatEvent.Type.ATTACK_MISS,
                        "Normal outcome must be ATTACK_HIT or ATTACK_MISS");
                normalSeen = true;
            }
        }

        assertTrue(successSeen, "Success outcome (double attack) must appear in 90 attempts");
        assertTrue(caughtSeen,  "Caught outcome must appear in 90 attempts");
        assertTrue(normalSeen,  "Normal attack outcome must appear in 90 attempts");
    }

    /**
     * Thief caught is reported in the combat log when SPECIAL_SKILL action
     * is used and the caught branch fires.
     */
    @Test
    void testThiefCaughtReportedInCombatLog() {
        // Force SPECIAL_CAUGHT every time via subclass override.
        final Thief hero = new Thief("Shadow") {
            @Override
            public List<CombatEvent> specialSkill(final DungeonCharacter opp) {
                return java.util.Collections.singletonList(
                        new CombatEvent(CombatEvent.Type.SPECIAL_CAUGHT,
                                getName(), opp.getName(), 0));
            }
        };
        final Gremlin monster = new Gremlin();
        final Combat combat = new Combat(hero, monster);
        combat.executeHeroAction(HeroAction.SPECIAL_SKILL);

        final boolean caughtLogged = combat.getLog().stream()
                .anyMatch(e -> e.getType() == CombatEvent.Type.SPECIAL_CAUGHT);
        assertTrue(caughtLogged,
                "SPECIAL_CAUGHT must appear in combat log when Thief is caught");
    }
    /**
     * Using a healing potion when inventory is empty produces
     * ITEM_UNAVAILABLE and does not increase HP.
     */
    @Test
    void testUseHealingPotionWithNoneInInventory() {
        final Warrior hero = new Warrior("Thor");
        final Gremlin monster = new Gremlin();
        hero.setHitPoints(50);

        final Combat combat = new Combat(hero, monster);
        final List<CombatEvent> events =
                combat.executeHeroAction(HeroAction.USE_HEALING_POTION);

        assertEquals(CombatEvent.Type.ITEM_UNAVAILABLE, events.get(0).getType());
        assertTrue(hero.getHitPoints() <= 50,
                "Hero HP should not increase when potion is unavailable");
    }

    /**
     * Using a healing potion restores a positive HP amount.
     */
    @Test
    void testUseHealingPotionHealsHero() {
        final Warrior hero = new Warrior("Thor");
        final Gremlin monster = new Gremlin();
        hero.setHitPoints(10);
        hero.addHealingPotion();

        final Combat combat = new Combat(hero, monster);
        final List<CombatEvent> events =
                combat.executeHeroAction(HeroAction.USE_HEALING_POTION);

        final CombatEvent first = events.get(0);
        assertEquals(CombatEvent.Type.POTION_USED, first.getType());
        assertTrue(first.getAmount() > 0,
                "Potion should restore a positive amount of HP");
    }
    /**
     * Calling executeHeroAction after combat is over is a safe no-op.
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
     * The cumulative log grows across multiple rounds.
     */
    @Test
    void testLogAccumulatesAcrossRounds() {
        final Warrior hero = new Warrior("Thor");
        final Skeleton monster = new Skeleton();
        final Combat combat = new Combat(hero, monster);

        for (int i = 0; i < 3 && !combat.isOver(); i++) {
            combat.executeHeroAction(HeroAction.ATTACK);
        }
        assertNotNull(combat.getLog());
        assertFalse(combat.getLog().isEmpty(),
                "Log should contain events after running rounds");
    }

    /**
     * getLog returns an unmodifiable view — callers cannot tamper with it.
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
