package edu.uw.tcss.dungeoneer.model;

/**
 * The four actions a hero may choose on their turn during combat.
 * The controller passes one of these to Combat.executeHeroAction
 * each round so the model never has to inspect raw button strings
 * coming from the GUI.
 *
 * @author Tarik Atasoy
 * @version Iteration 2
 */
public enum HeroAction {

    /** Standard attack against the current monster. */
    ATTACK,

    /** Hero-specific special skill (Crushing Blow, Heal, Surprise Attack). */
    SPECIAL_SKILL,

    /** Drink a healing potion from the hero's inventory. */
    USE_HEALING_POTION,

    /** Throw a bomb at the current monster. */
    USE_BOMB
}
