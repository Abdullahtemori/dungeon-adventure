package edu.uw.tcss.dungeoneer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Priestess hero — lower damage but can heal herself.
 *
 * Stats:
 *   HP: 75 | Speed: 5 | Hit Chance: 70% | Damage: 25–45 | Block: 30%
 *
 * @author Person 1
 * @version Iteration 1
 */
public class Priestess extends Hero implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    private static final long serialVersionUID = 1L;

    /** Minimum HP healed by special skill. */
    private static final int HEAL_MIN = 20;

    /** Maximum HP healed by special skill. */
    private static final int HEAL_MAX = 50;

    /**
     * Constructs a Priestess with the given name and default stats.
     *
     * @param theName the priestess's name
     */
    public Priestess(final String theName) {
        super(theName, 75, 25, 45, 5, 0.7, 0.3);
    }

    /**
     * Heal, restores 20-50 HP to self. The opponent argument is
     * ignored; kept for signature compatibility with the abstract
     * specialSkill in Hero.
     *
     * @param theOpponent not used for healing (heals self)
     * @return list with one SPECIAL_HEAL event
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

    @Override
    public String toString() {
        return "Priestess | " + super.toString();
    }
}
