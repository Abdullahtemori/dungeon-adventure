package edu.uw.tcss.dungeoneer.model;

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
public class Thief extends Hero {

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
     *
     * @param theOpponent the target
     */
    @Override
    public void specialSkill(final DungeonCharacter theOpponent) {
        double roll = Math.random();
        if (roll < SUCCESS_CHANCE) {
            System.out.println(getName() + "'s Surprise Attack succeeds! Two attacks!");
            attack(theOpponent);
            attack(theOpponent);
        } else if (roll < SUCCESS_CHANCE + CAUGHT_CHANCE) {
            System.out.println(getName() + " was caught! No attack this round.");
        } else {
            System.out.println(getName() + " performs a normal attack.");
            attack(theOpponent);
        }
    }

    @Override
    public String toString() {
        return "Thief | " + super.toString();
    }
}
