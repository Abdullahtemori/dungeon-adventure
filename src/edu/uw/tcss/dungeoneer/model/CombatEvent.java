package edu.uw.tcss.dungeoneer.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * A single thing that happened during a combat round.
 *
 * <p>Combat methods return {@link CombatEvent} objects (or lists of
 * them) instead of printing to {@code System.out}, so the model layer
 * stays fully decoupled from the GUI. The view is responsible for
 * turning these events into text, animations, sounds, etc.</p>
 *
 * <p>Events are immutable.</p>
 *
 * @author Tarik Atasoy
 * @author Abdullah Temori
 * @author Daniella Birungi
 * @version Iteration 6
 */
public final class CombatEvent implements Serializable {

    /**
     * Serial Version UID required for safe serialization.
     * If the class structure changes this number should be updated.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Categories of things that can happen in a combat round.
     */
    public enum Type {
        /**
         * An attack landed and dealt damage.
         */
        ATTACK_HIT,
        /**
         * An attack missed (chance-to-hit roll failed).
         */
        ATTACK_MISS,
        /**
         * Defender blocked the incoming attack.
         */
        ATTACK_BLOCKED,
        /**
         * Monster healed itself after being hit.
         */
        MONSTER_HEAL,
        /**
         * Hero drank a healing potion.
         */
        POTION_USED,
        /**
         * Hero threw a bomb.
         */
        BOMB_USED,
        /**
         * Hero's special skill connected.
         */
        SPECIAL_SUCCESS,
        /**
         * Hero's special skill failed (e.g. Crushing Blow miss).
         */
        SPECIAL_FAIL,
        /**
         * Thief was caught while attempting Surprise Attack.
         */
        SPECIAL_CAUGHT,
        /**
         * Priestess (or similar) self-heal via special skill.
         */
        SPECIAL_HEAL,
        /**
         * Hero attempted to use a potion or bomb but had none left.
         */
        ITEM_UNAVAILABLE,
        /**
         * Combat finished. amount = 1 if hero won, 0 if hero lost.
         */
        COMBAT_END
    }

    /**
     * What kind of event this is.
     */
    private final Type myType;

    /**
     * Name of whoever caused the event (attacker, healer, etc.).
     */
    private final String myActor;

    /**
     * Name of the target of the event (may equal actor for self-heal).
     */
    private final String myTarget;

    /**
     * Numeric payload (damage dealt, HP healed, hero-won flag, etc.).
     * Meaning depends on the event type. 0 when not applicable.
     */
    private final int myAmount;

    /**
     * Constructs a CombatEvent.
     *
     * @param theType   the type of event
     * @param theActor  name of the actor that caused the event
     * @param theTarget name of the target affected by the event
     * @param theAmount numeric payload (damage, heal, etc.)
     */
    public CombatEvent(final Type theType, final String theActor,
                       final String theTarget, final int theAmount) {
        myType = theType;
        myActor = theActor;
        myTarget = theTarget;
        myAmount = theAmount;
    }

    /**
     * Returns the event type.
     *
     * @return the event type
     */
    public Type getType() {
        return myType;
    }

    /**
     * Returns the name of the actor that caused this event.
     *
     * @return name of the actor
     */
    public String getActor() {
        return myActor;
    }

    /**
     * Returns the name of the target affected by this event.
     *
     * @return name of the target
     */
    public String getTarget() {
        return myTarget;
    }

    /**
     * Returns the numeric amount associated with this event (damage
     * dealt, HP healed, or win/loss flag for
     * {@link Type#COMBAT_END}).
     *
     * @return the numeric amount (&ge; 0)
     */
    public int getAmount() {
        return myAmount;
    }

    /**
     * Builds a human-readable line for a console view. The Swing
     * view is free to ignore this and format events its own way.
     *
     * @return one-line description of this event
     */
    @Override
    public String toString() {
        return switch (myType) {
            case ATTACK_HIT -> myActor + " hits " + myTarget
                    + " for " + myAmount + " damage.";
            case ATTACK_MISS -> myActor + "'s attack on " + myTarget + " missed.";
            case ATTACK_BLOCKED -> myTarget + " blocked " + myActor + "'s attack.";
            case MONSTER_HEAL -> myActor + " heals for " + myAmount + " HP.";
            case POTION_USED -> myActor + " drinks a healing potion (+"
                    + myAmount + " HP).";
            case BOMB_USED -> myActor + " throws a bomb at " + myTarget
                    + " for " + myAmount + " damage.";
            case SPECIAL_SUCCESS -> myActor + "'s special skill hits " + myTarget
                    + " for " + myAmount + " damage.";
            case SPECIAL_FAIL -> myActor + "'s special skill failed.";
            case SPECIAL_CAUGHT -> myActor + " was caught attempting a special skill.";
            case SPECIAL_HEAL -> myActor + " heals self for " + myAmount + " HP.";
            case ITEM_UNAVAILABLE -> myActor + " has none of that item left.";
            case COMBAT_END -> myAmount == 1
                    ? myActor + " won the fight against " + myTarget + "."
                    : myTarget + " was defeated by " + myActor + ".";
            default -> myType + " " + myActor + " " + myTarget + " " + myAmount;
        };
    }
}
 
