package edu.uw.tcss.dungeoneer.model;
 
/**
 * The four actions a hero may choose on their turn during combat.
 *
 * <p>The controller passes one of these to
 * {@link Combat#executeHeroAction(HeroAction)} each round so the
 * model never has to inspect raw button strings coming from the
 * GUI.</p>
 *
 * @author Tarik Atasoy
 * @author Abdullah Temori
 * @version Iteration 6
 */
public enum HeroAction {
 
    /**
     * Standard attack against the current monster.
     * The number of swings depends on the relative attack speeds of
     * the hero and monster; see
     * {@link Combat#getHeroAttacksThisRound()}.
     */
    ATTACK,
 
    /**
     * Hero-specific special skill (Crushing Blow, Heal, Surprise
     * Attack). Each {@link Hero} subclass defines the effect via
     * {@code Hero#specialSkill(Monster)}.
     */
    SPECIAL_SKILL,
 
    /**
     * Drink a healing potion from the hero's inventory.
     * Produces {@link CombatEvent.Type#POTION_USED} on success or
     * {@link CombatEvent.Type#ITEM_UNAVAILABLE} if none remain.
     */
    USE_HEALING_POTION,
 
    /**
     * Throw a bomb at the current monster.
     * Produces {@link CombatEvent.Type#BOMB_USED} on success or
     * {@link CombatEvent.Type#ITEM_UNAVAILABLE} if none remain.
     */
    USE_BOMB
}
