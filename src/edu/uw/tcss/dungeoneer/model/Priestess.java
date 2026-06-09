package edu.uw.tcss.dungeoneer.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Priestess hero — lower damage than other heroes, but can heal herself
 * during combat using her special skill.
 * Stats:
 * HP: 75 | Speed: 5 | Hit Chance: 70% | Damage: 25–45 | Block: 30%
 * Special Skill — Heal:
 * Always succeeds; restores 20–50 HP to self.
 *
 * @author Person 1, Abdullah Temori
 * @version Iteration 4
 */
public class Priestess extends Hero implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Minimum HP restored by the Heal special skill.
     */
    private static final int HEAL_MIN = 20;

    /**
     * Maximum HP restored by the Heal special skill.
     */
    private static final int HEAL_MAX = 50;

    /**
     * Constructs a Priestess with the given player-chosen name and default stats.
     *
     * @param theName the priestess's display name; must not be null or empty
     */
    public Priestess(final String theName) {
        super(theName, 75, 25, 45, 5, 0.7, 0.3);
    }

    /**
     * Heal special skill.
     * Restores 20–50 HP to the Priestess herself. This skill always
     * succeeds and ignores the opponent argument, which is accepted only
     * to satisfy the abstract method signature defined in Hero.
     *
     * @param theOpponent the current combat opponent; not used by this skill
     * @return an unmodifiable single-element list containing a
     * SPECIAL_HEAL event with the amount of HP restored
     */
    @Override
    public List<CombatEvent> specialSkill(final DungeonCharacter theOpponent) {
        final int heal = HEAL_MIN
                + (int) (Math.random() * (HEAL_MAX - HEAL_MIN + 1));
        setHitPoints(getHitPoints() + heal);
        final List<CombatEvent> events = new ArrayList<>(1);
        events.add(new CombatEvent(CombatEvent.Type.SPECIAL_HEAL,
                getName(), getName(), heal));
        return Collections.unmodifiableList(events);
    }

    /**
     * Returns a string representation of this Priestess.
     *
     * @return formatted string prefixed with "Priestess"
     */
    @Override
    public String toString() {
        return "Priestess | " + super.toString();
    }
}
