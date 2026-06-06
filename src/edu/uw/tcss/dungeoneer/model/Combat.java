package edu.uw.tcss.dungeoneer.model;
 
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/**
 * Runs a single turn-based fight between a Hero and a Monster.
 *
 * <p>The class itself does not print anything. Each call to
 * {@link #executeHeroAction(HeroAction)} returns a list of
 * {@link CombatEvent} objects that the view (console or Swing) can
 * turn into text or animations. This keeps combat logic separate
 * from the UI.</p>
 *
 * <p>How a round works:</p>
 * <ol>
 *   <li>The hero picks one action: attack, special skill, drink a
 *       potion, or throw a bomb. If the hero attacks and is faster
 *       than the monster, the hero swings multiple times.</li>
 *   <li>If the monster took damage and is still alive, it gets a
 *       chance to heal itself.</li>
 *   <li>The monster swings back. Faster monsters get more swings.
 *       The hero may block each incoming swing.</li>
 *   <li>As soon as either side hits 0 HP the fight is over.</li>
 * </ol>
 *
 * @author Tarik Atasoy
 * @author Abdullah Temori 
 * @version Iteration 6
 */
public class Combat implements Serializable {
 
    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;
 
    /**
     * Minimum number of attacks either side can make in one round.
     * Used as the floor value in {@link #attacksPerRound(int, int)}.
     */
    private static final int MIN_ATTACKS = 1;
 
    /**
     * Amount placed in a {@link CombatEvent.Type#COMBAT_END} event
     * when the hero wins.
     */
    private static final int WIN_FLAG = 1;
 
    /**
     * Amount placed in a {@link CombatEvent.Type#COMBAT_END} event
     * when the hero loses.
     */
    private static final int LOSS_FLAG = 0;
 
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
     * @param theHero    the hero (must be non-null and alive)
     * @param theMonster the monster (must be non-null and alive)
     * @throws IllegalArgumentException if either argument is {@code null}
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
 
    /**
     * Returns the hero in this fight.
     *
     * @return the hero
     */
    public Hero getHero() {
        return myHero;
    }
 
    /**
     * Returns the monster in this fight.
     *
     * @return the monster
     */
    public Monster getMonster() {
        return myMonster;
    }
 
    /**
     * Returns {@code true} if this combat has ended.
     *
     * @return {@code true} when the fight is over
     */
    public boolean isOver() {
        return myCombatOver;
    }
 
    /**
     * Returns {@code true} if the hero won.
     * Only meaningful when {@link #isOver()} returns {@code true}.
     *
     * @return {@code true} if the hero was the last one standing
     */
    public boolean heroWon() {
        return myHeroWon;
    }
 
    /**
     * Returns the full event log accumulated so far. The returned
     * list is unmodifiable so callers cannot tamper with combat state.
     *
     * @return cumulative, unmodifiable event log
     */
    public List<CombatEvent> getLog() {
        return Collections.unmodifiableList(myLog);
    }
 
    /**
     * Calculates the number of attacks the faster character gets when
     * their speed is {@code theFaster} and the slower one is
     * {@code theSlower}. The slower side always gets exactly one attack.
     * The faster side gets at least one and at most
     * {@code floor(theFaster / theSlower)}.
     *
     * @param theFaster attack speed of the faster character
     * @param theSlower attack speed of the slower character
     * @return number of attacks for the faster character (&ge; 1)
     */
    static int attacksPerRound(final int theFaster, final int theSlower) {
        if (theSlower <= 0) {
            return MIN_ATTACKS;
        }
        final int attacks = theFaster / theSlower;
        return Math.max(MIN_ATTACKS, attacks);
    }
 
    /**
     * Returns how many attacks the hero gets when choosing the
     * {@link HeroAction#ATTACK} action this round.
     *
     * @return attack count &ge; 1
     */
    public int getHeroAttacksThisRound() {
        final int heroAttacks = attacksPerRound(
                myHero.getAttackSpeed(), myMonster.getAttackSpeed());
        final int monsterAttacks = attacksPerRound(
                myMonster.getAttackSpeed(), myHero.getAttackSpeed());
        return Math.max(heroAttacks, monsterAttacks);
    }
 
    /**
     * Returns how many attacks the monster gets on its retaliation step
     * this round.
     *
     * @return attack count &ge; 1
     */
    public int getMonsterAttacksThisRound() {
        return attacksPerRound(
                myMonster.getAttackSpeed(), myHero.getAttackSpeed());
    }
 
    /**
     * Executes one full round: the hero's chosen action, then the
     * monster's retaliation (if the monster is still alive). Returns
     * the events generated this round only. The cumulative log can be
     * fetched separately via {@link #getLog()}.
     *
     * <p>Calling this after combat is over is a no-op and returns an
     * empty list.</p>
     *
     * @param theAction the hero's chosen action this round (must not
     *                  be {@code null})
     * @return events produced this round (never {@code null})
     * @throws IllegalArgumentException if {@code theAction} is {@code null}
     * @throws IllegalStateException    if an unhandled action is encountered
     */
    public List<CombatEvent> executeHeroAction(final HeroAction theAction) {
        final List<CombatEvent> roundEvents = new ArrayList<>();
 
        if (myCombatOver) {
            return roundEvents;
        }
        if (theAction == null) {
            throw new IllegalArgumentException("HeroAction cannot be null");
        }
 
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
                throw new IllegalStateException(
                        "Unhandled hero action: " + theAction);
        }
 
        if (monsterTookDamage && myMonster.isAlive()) {
            final CombatEvent healEvent = myMonster.heal();
            if (healEvent != null) {
                roundEvents.add(healEvent);
            }
        }
 
        if (!myMonster.isAlive()) {
            finishCombat(true, roundEvents);
            myLog.addAll(roundEvents);
            return roundEvents;
        }
 
        doMonsterAttacks(roundEvents);
 
        if (!myHero.isAlive()) {
            finishCombat(false, roundEvents);
        }
 
        myLog.addAll(roundEvents);
        return roundEvents;
    }
 
    /**
     * Runs the hero's attack swings for a regular
     * {@link HeroAction#ATTACK} action.
     *
     * @param theEvents list to append events to
     * @return {@code true} if any attack landed so the monster heal
     *         check should run
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
                theEvents.add(new CombatEvent(
                        CombatEvent.Type.ATTACK_BLOCKED,
                        myMonster.getName(),
                        myHero.getName(),
                        0));
                continue;
            }
            theEvents.add(myMonster.attack(myHero));
        }
    }
 
    /**
     * Wraps {@link Hero#useHealingPotion()} so the model can convert
     * the {@code int} return value into a {@link CombatEvent} and apply
     * the heal. {@code Hero.useHealingPotion} only returns the amount —
     * it does not modify HP — so new HP is set here.
     *
     * @return a {@link CombatEvent.Type#POTION_USED} event on success,
     *         or {@link CombatEvent.Type#ITEM_UNAVAILABLE} if none remain
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
     * Wraps {@link Hero#useBomb(Monster)}. {@code Hero.useBomb} already
     * deducts the bomb and applies damage to the monster, so only the
     * {@code int} return value needs to be translated into a
     * {@link CombatEvent}.
     *
     * @return a {@link CombatEvent.Type#BOMB_USED} event on success,
     *         or {@link CombatEvent.Type#ITEM_UNAVAILABLE} if none remain
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
     * Marks combat as over and appends a
     * {@link CombatEvent.Type#COMBAT_END} event.
     *
     * @param theHeroWon {@code true} if the hero won
     * @param theEvents  list to append the COMBAT_END event to
     */
    private void finishCombat(final boolean theHeroWon,
                              final List<CombatEvent> theEvents) {
        myCombatOver = true;
        myHeroWon = theHeroWon;
        theEvents.add(new CombatEvent(
                CombatEvent.Type.COMBAT_END,
                myHero.getName(),
                myMonster.getName(),
                theHeroWon ? WIN_FLAG : LOSS_FLAG));
    }
 
    /**
     * Helper for {@link HeroAction#SPECIAL_SKILL}: checks whether any
     * returned event represents damage dealt to the monster. Used to
     * decide whether the monster gets a post-damage heal check.
     *
     * @param theEvents events returned by the special skill (may be
     *                  {@code null})
     * @return {@code true} if any event recorded positive damage on a
     *         target
     */
    private static boolean anyDamageDealt(
            final List<CombatEvent> theEvents) {
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
 
