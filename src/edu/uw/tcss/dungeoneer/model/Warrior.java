package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Warrior hero — high HP and damage output, with a powerful Crushing Blow
 * special skill that has a 40 % chance to deal massive damage in one swing.
 *
 * Stats:
 *   HP: 125 | Speed: 4 | Hit Chance: 80% | Damage: 35–60 | Block: 20%
 *
 * Special Skill — Crushing Blow:
 *   40% chance to deal 75–175 damage; otherwise the attempt fails.
 *
 * @author Person 1, Abdullah Temori
 * @version Iteration 4
 */
public class Warrior extends Hero implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /** Minimum damage dealt by a successful Crushing Blow. */
    private static final int CRUSH_MIN = 75;

    /** Maximum damage dealt by a successful Crushing Blow. */
    private static final int CRUSH_MAX = 175;

    /** Probability that Crushing Blow succeeds (0.0–1.0). */
    private static final double CRUSH_CHANCE = 0.4;

    /**
     * Constructs a Warrior with the given player-chosen name and default stats.
     *
     * @param theName the warrior's display name; must not be null or empty
     */
    public Warrior(final String theName) {
        super(theName, 125, 35, 60, 4, 0.8, 0.2);
    }

    /**
     * Crushing Blow special skill.
     * Has a 40 % chance to deal 75–175 damage to the opponent in a single
     * devastating hit. If the attempt fails, a SPECIAL_FAIL event is
     * returned instead and no damage is dealt.
     *
     * @param theOpponent the target of the Crushing Blow; must not be null
     * @return an unmodifiable single-element list containing either a
     *         SPECIAL_SUCCESS event (hit landed) or a SPECIAL_FAIL event
     *         (attempt failed)
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

    /**
     * Returns a string representation of this Warrior.
     *
     * @return formatted string prefixed with "Warrior"
     */
    @Override
    public String toString() {
        return "Warrior | " + super.toString();
    }
}
