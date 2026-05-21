package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thief hero — fastest attacker with a high block chance and a
 * Surprise Attack special skill that can land two hits in one turn.
 *
 * Stats:
 *   HP: 75 | Speed: 6 | Hit Chance: 80% | Damage: 20–40 | Block: 40%
 *
 * Special Skill — Surprise Attack:
 *   40% → success: two normal attacks this round
 *   20% → caught: no attack at all (SPECIAL_CAUGHT event)
 *   40% → fallback: one normal attack
 *
 * @author Person 1, Abdullah Temori
 * @version Iteration 4
 */
public class Thief extends Hero implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /** Probability that the surprise attack fully succeeds (double hit). */
    private static final double SUCCESS_CHANCE = 0.4;

    /** Probability that the thief is caught (no attack this round). */
    private static final double CAUGHT_CHANCE = 0.2;

    /**
     * Constructs a Thief with the given player-chosen name and default stats.
     *
     * @param theName the thief's display name; must not be null or empty
     */
    public Thief(final String theName) {
        super(theName, 75, 20, 40, 6, 0.8, 0.4);
    }

    /**
     * Surprise Attack special skill.
     * Rolls once to determine one of three outcomes:
     * <ul>
     *   <li>40 % — success: the thief lands two normal attacks.</li>
     *   <li>20 % — caught: the thief is detected and attacks not at all;
     *       a SPECIAL_CAUGHT event is returned.</li>
     *   <li>40 % — fallback: one normal attack lands.</li>
     * </ul>
     * All generated attack events (ATTACK_HIT or ATTACK_MISS) are
     * included in the returned list so the view can render each swing.
     *
     * @param theOpponent the target of the surprise attack; must not be null
     * @return an unmodifiable list of CombatEvents produced this turn;
     *         contains one SPECIAL_CAUGHT event if the thief was caught,
     *         or one or two attack events otherwise
     */
    @Override
    public List<CombatEvent> specialSkill(final DungeonCharacter theOpponent) {
        final List<CombatEvent> events = new ArrayList<>();
        final double roll = Math.random();

        if (roll < SUCCESS_CHANCE) {
            // Surprise Attack succeeded: thief gets two attacks this round.
            events.add(attack(theOpponent));
            events.add(attack(theOpponent));
        } else if (roll < SUCCESS_CHANCE + CAUGHT_CHANCE) {
            // Thief was caught — no attack at all.
            events.add(new CombatEvent(CombatEvent.Type.SPECIAL_CAUGHT,
                    getName(), theOpponent.getName(), 0));
        } else {
            // Fallback — one normal attack.
            events.add(attack(theOpponent));
        }
        return Collections.unmodifiableList(events);
    }

    /**
     * Returns a string representation of this Thief.
     *
     * @return formatted string prefixed with "Thief"
     */
    @Override
    public String toString() {
        return "Thief | " + super.toString();
    }
}
