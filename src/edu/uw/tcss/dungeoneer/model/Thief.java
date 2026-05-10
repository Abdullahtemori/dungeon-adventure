package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thief hero — fast attacker with a surprise attack special skill.
 *
 * Stats:
 *   HP: 75 | Speed: 6 | Hit Chance: 80% | Damage: 20–40 | Block: 40%
 *
 * Special Skill — Surprise Attack:
 *   40% → success: two attacks this round
 *   20% → caught: no attack at all
 *   40% → normal single attack
 *
 * @author Person 1
 * @version Iteration 1
 */
public class Thief extends Hero implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /** Chance surprise attack fully succeeds. */
    private static final double SUCCESS_CHANCE = 0.4;

    /** Chance thief gets caught (no attack). */
    private static final double CAUGHT_CHANCE = 0.2;

    /**
     * Constructs a Thief with the given name and default stats.
     *
     * @param theName the thief's name
     */
    public Thief(final String theName) {
        super(theName, 75, 20, 40, 6, 0.8, 0.4);
    }

    /**
     * Surprise Attack special skill.
     * 40% chance of double attack, 20% caught (no attack), 40% normal attack.
     * The events from any landed attacks are bundled and returned so
     * the view can show all of them in order.
     *
     * @param theOpponent the target
     * @return list of events produced by this skill (may be empty if caught)
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
            // Thief was caught, no attack at all.
            events.add(new CombatEvent(CombatEvent.Type.SPECIAL_CAUGHT,
                    getName(), theOpponent.getName(), 0));
        } else {
            // Normal attack fallback.
            events.add(attack(theOpponent));
        }
        return Collections.unmodifiableList(events);
    }

    @Override
    public String toString() {
        return "Thief | " + super.toString();
    }
}
