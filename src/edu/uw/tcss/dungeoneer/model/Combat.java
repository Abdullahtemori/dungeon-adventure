package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs a single turn-based fight between a Hero and a Monster.
 *
 * The class itself does not print anything. Each call to
 * executeHeroAction returns a list of CombatEvent objects that the
 * view (console or Swing) can turn into text or animations. This
 * keeps combat logic separate from the UI.
 *
 * How a round works:
 *   1. The hero picks one action: attack, special skill, drink a
 *      potion, or throw a bomb. If the hero attacks and is faster
 *      than the monster, the hero swings multiple times.
 *   2. If the monster took damage and is still alive, it gets a
 *      chance to heal itself.
 *   3. The monster swings back. Faster monsters get more swings.
 *      The hero may block each incoming swing.
 *   4. As soon as either side hits 0 HP the fight is over.
 *
 * @author Tarik Atasoy
 
 * @version Iteration 2
 */
public class Combat implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /** The hero participating in the encounter. */
    private final Hero myHero;

    /** The monster participating in the encounter. */
    private final Monster myMonster;

    /**
     * Cumulative log of every event that has happened in this fight.
     * Useful for save/load and post-mortem display.
     */
    private final List<CombatEvent> myLog;

    /** Latched once a COMBAT_END event has been produced. */
    private boolean myCombatOver;

    /** True only if the hero was the side still standing at COMBAT_END. */
    private boolean myHeroWon;

    /**
     * Constructs a Combat between the given hero and monster.
     *
     * @param theHero    the hero (must be alive)
     * @param theMonster the monster (must be alive)
     */
    public Combat(final Hero theHero, final Monster theMonster) {
        if (theHero == null || theMonster == null) {
            throw new IllegalArgumentException(
                    "Combat requires a non-null hero and monster");
        }
        myHero = theHero;
        myMonster = theMonster;
        myLog = new ArrayList<>();
        myCombatOver = false;
        myHeroWon = false;
    }

    /** @return the hero in this fight */
    public Hero getHero() {
        return myHero;
    }

    /** @return the monster in this fight */
    public Monster getMonster() {
        return myMonster;
    }

    /** @return true if this combat has ended */
    public boolean isOver() {
        return myCombatOver;
    }

    /** @return true if the hero won (only meaningful when isOver) */
    public boolean heroWon() {
        return myHeroWon;
    }

    /**
     * Returns the full event log accumulated so far. The returned
     * list is unmodifiable so callers cannot tamper with combat state.
     *
     * @return cumulative event log
     */
    public List<CombatEvent> getLog() {
        return Collections.unmodifiableList(myLog);
    }

    /**
     * Number of attacks the faster character gets when their speed is
     * theFaster and the slower one is theSlower. The slower side always
     * gets exactly one attack. The faster side gets at least one and at
     * most floor(faster / slower).
     *
     * @param theFaster attack speed of the faster character
     * @param theSlower attack speed of the slower character
     * @return number of attacks for the faster character (>= 1)
     */
    static int attacksPerRound(final int theFaster, final int theSlower) {
        if (theSlower <= 0) {
            return 1;
        }
        final int n = theFaster / theSlower;
        return Math.max(1, n);
    }

    /**
     * Returns how many attacks the hero gets when choosing the ATTACK
     * action this round.
     *
     * @return attack count >= 1
     */
    public int getHeroAttacksThisRound() {
        // The hero should never end up with fewer swings than the monster.
        final int n = attacksPerRound(myHero.getAttackSpeed(),
                myMonster.getAttackSpeed());
        final int monsterN = attacksPerRound(myMonster.getAttackSpeed(),
                myHero.getAttackSpeed());
        return Math.max(n, monsterN);
    }

    /**
     * Returns how many attacks the monster gets on its retaliation step
     * this round.
     *
     * @return attack count >= 1
     */
    public int getMonsterAttacksThisRound() {
        return attacksPerRound(myMonster.getAttackSpeed(),
                myHero.getAttackSpeed());
    }

    /**
     * Executes one full round: the hero's chosen action, then the
     * monster's retaliation (if the monster is still alive). Returns
     * the events generated this round only. The cumulative log can be
     * fetched separately via getLog().
     *
     * Calling this after combat is over is a no-op and returns an
     * empty list.
     *
     * @param theAction the hero's chosen action this round
     * @return events produced this round (never null)
     */
    public List<CombatEvent> executeHeroAction(final HeroAction theAction) {
        final List<CombatEvent> roundEvents = new ArrayList<>();

        if (myCombatOver) {
            return roundEvents;
        }
        if (theAction == null) {
            throw new IllegalArgumentException("HeroAction cannot be null");
        }

        // Hero phase
        boolean monsterTookDamage = false;

        switch (theAction) {
            case ATTACK:
                monsterTookDamage = doHeroAttacks(roundEvents);
                break;
            case SPECIAL_SKILL:
                final List<CombatEvent> skillEvents =
                        myHero.specialSkill(myMonster);
                if (skillEvents != null) {
                    roundEvents.addAll(skillEvents);
                }
                monsterTookDamage = anyDamageDealt(skillEvents);
                break;
            case USE_HEALING_POTION:
                roundEvents.add(useHealingPotion());
                break;
            case USE_BOMB:
                final CombatEvent bombEvent = useBomb();
                roundEvents.add(bombEvent);
                if (bombEvent.getType() == CombatEvent.Type.BOMB_USED
                        && bombEvent.getAmount() > 0) {
                    monsterTookDamage = true;
                }
                break;
            default:
                // Defensive: should never happen since enum is exhaustive.
                throw new IllegalStateException(
                        "Unhandled hero action: " + theAction);
        }

        // Monster heal step (only if it took damage this turn)
        if (monsterTookDamage && myMonster.isAlive()) {
            final CombatEvent healEvent = myMonster.heal();
            if (healEvent != null) {
                roundEvents.add(healEvent);
            }
        }

        // Check for hero victory
        if (!myMonster.isAlive()) {
            finishCombat(true, roundEvents);
            myLog.addAll(roundEvents);
            return roundEvents;
        }

        // Monster phase
        doMonsterAttacks(roundEvents);

        // Check for hero defeat
        if (!myHero.isAlive()) {
            finishCombat(false, roundEvents);
        }

        myLog.addAll(roundEvents);
        return roundEvents;
    }

    /**
     * Runs the hero's attack swings for a regular ATTACK action.
     *
     * @param theEvents list to append events to
     * @return true if any attack landed (so monster heal check should run)
     */
    private boolean doHeroAttacks(final List<CombatEvent> theEvents) {
        boolean tookDamage = false;
        final int swings = getHeroAttacksThisRound();
        for (int i = 0; i < swings; i++) {
            if (!myMonster.isAlive()) {
                break;
            }
            final CombatEvent e = myHero.attack(myMonster);
            theEvents.add(e);
            if (e.getType() == CombatEvent.Type.ATTACK_HIT) {
                tookDamage = true;
            }
        }
        return tookDamage;
    }

    /**
     * Runs the monster's retaliation swings, applying the hero's block
     * roll to each individual incoming attack.
     *
     * @param theEvents list to append events to
     */
    private void doMonsterAttacks(final List<CombatEvent> theEvents) {
        final int swings = getMonsterAttacksThisRound();
        for (int i = 0; i < swings; i++) {
            if (!myHero.isAlive()) {
                break;
            }
            if (myHero.block()) {
                theEvents.add(new CombatEvent(CombatEvent.Type.ATTACK_BLOCKED,
                        myMonster.getName(), myHero.getName(), 0));
                continue;
            }
            theEvents.add(myMonster.attack(myHero));
        }
    }

    /**
     * Wraps Hero.useHealingPotion so the model can convert the int
     * return value into a CombatEvent and actually apply the heal.
     * Hero.useHealingPotion only returns the amount (it does not
     * modify HP) so we set the new HP here.
     *
     * @return a POTION_USED event or ITEM_UNAVAILABLE event
     */
    private CombatEvent useHealingPotion() {
        final int amount = myHero.useHealingPotion();
        if (amount <= 0) {
            return new CombatEvent(CombatEvent.Type.ITEM_UNAVAILABLE,
                    myHero.getName(), myHero.getName(), 0);
        }
        myHero.setHitPoints(myHero.getHitPoints() + amount);
        return new CombatEvent(CombatEvent.Type.POTION_USED,
                myHero.getName(), myHero.getName(), amount);
    }

    /**
     * Wraps Hero.useBomb. Hero.useBomb already deducts the bomb and
     * applies damage to the monster, so we only need to translate
     * the int return value into a CombatEvent.
     *
     * @return a BOMB_USED event or ITEM_UNAVAILABLE event
     */
    private CombatEvent useBomb() {
        final int damage = myHero.useBomb(myMonster);
        if (damage <= 0) {
            return new CombatEvent(CombatEvent.Type.ITEM_UNAVAILABLE,
                    myHero.getName(), myMonster.getName(), 0);
        }
        return new CombatEvent(CombatEvent.Type.BOMB_USED,
                myHero.getName(), myMonster.getName(), damage);
    }

    /**
     * Marks combat as over and appends a COMBAT_END event.
     *
     * @param theHeroWon true if the hero won
     * @param theEvents  list to append the COMBAT_END event to
     */
    private void finishCombat(final boolean theHeroWon,
                              final List<CombatEvent> theEvents) {
        myCombatOver = true;
        myHeroWon = theHeroWon;
        theEvents.add(new CombatEvent(CombatEvent.Type.COMBAT_END,
                myHero.getName(), myMonster.getName(),
                theHeroWon ? 1 : 0));
    }

    /**
     * Helper for SPECIAL_SKILL: did any of the events represent damage
     * dealt to the monster? Used to decide whether the monster gets a
     * post-damage heal check.
     *
     * @param theEvents events returned by the special skill
     * @return true if any event landed damage on a target
     */
    private static boolean anyDamageDealt(final List<CombatEvent> theEvents) {
        if (theEvents == null) {
            return false;
        }
        for (final CombatEvent e : theEvents) {
            final CombatEvent.Type t = e.getType();
            if ((t == CombatEvent.Type.ATTACK_HIT
                    || t == CombatEvent.Type.SPECIAL_SUCCESS)
                    && e.getAmount() > 0) {
                return true;
            }
        }
        return false;
    }
}
