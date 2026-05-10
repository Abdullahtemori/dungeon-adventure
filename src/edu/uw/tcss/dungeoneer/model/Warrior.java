package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Warrior hero — high HP and damage, special skill is Crushing Blow.
 *
 * Stats:
 *   HP: 125 | Speed: 4 | Hit Chance: 80% | Damage: 35–60 | Block: 20%
 *
 * @author Person 1
 * @version Iteration 1
 */
public class Warrior extends Hero implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /** Minimum damage for Crushing Blow. */
    private static final int CRUSH_MIN = 75;

    /** Maximum damage for Crushing Blow. */
    private static final int CRUSH_MAX = 175;

    /** Chance Crushing Blow succeeds. */
    private static final double CRUSH_CHANCE = 0.4;

    /**
     * Constructs a Warrior with the given name and default stats.
     *
     * @param theName the warrior's name
     */
    public Warrior(final String theName) {
        super(theName, 125, 35, 60, 4, 0.8, 0.2);
    }

    /**
     * Crushing Blow — 40% chance to deal 75–175 damage.
     * Returns a single-element list with a SPECIAL_SUCCESS or
     * SPECIAL_FAIL event describing the outcome.
     *
     * @param theOpponent the target
     * @return list of events produced by this skill
     */
    @Override
    public List<CombatEvent> specialSkill(final DungeonCharacter theOpponent) {
        final List<CombatEvent> events = new ArrayList<>(1);
        if (Math.random() < CRUSH_CHANCE) {
            final int damage = CRUSH_MIN
                    + (int) (Math.random() * (CRUSH_MAX - CRUSH_MIN + 1));
            theOpponent.setHitPoints(theOpponent.getHitPoints() - damage);
            events.add(new CombatEvent(CombatEvent.Type.SPECIAL_SUCCESS,
                    getName(), theOpponent.getName(), damage));
        } else {
            events.add(new CombatEvent(CombatEvent.Type.SPECIAL_FAIL,
                    getName(), theOpponent.getName(), 0));
        }
        return Collections.unmodifiableList(events);
    }

    @Override
    public String toString() {
        return "Warrior | " + super.toString();
    }
}
