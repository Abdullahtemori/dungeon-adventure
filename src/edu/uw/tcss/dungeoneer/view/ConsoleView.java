package edu.uw.tcss.dungeoneer.view;

import edu.uw.tcss.dungeoneer.model.*;

import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * ConsoleView is the console-based View in the MVC pattern.
 * It implements GameView and displays all game information
 * as text output to System.out.
 * ConsoleView also implements PropertyChangeListener (via GameView)
 * so GameModel can notify it automatically when state changes.
 * When propertyChange() is called, ConsoleView reads the event
 * and decides what to display based on the property name.
 *
 * @author Daniella Birungi
 * @version Iteration 3
 */
public class ConsoleView implements GameView {

    /** Separator line used between sections of output. */
    private static final String SEPARATOR =
            "========================================";

    /** Shorter separator for minor sections. */
    private static final String THIN_LINE =
            "----------------------------------------";


    /**
     * Constructs a ConsoleView.
     * No fields needed — all output goes directly to System.out.
     */
    public ConsoleView() {

    }

    /**
     * Displays the contents of a single room as a 3x3 text grid
     * followed by a description of what the room contains.
     * @param theRoom the room to display
     */
    @Override
    public void displayRoom(final Room theRoom) {
        System.out.println(THIN_LINE);
        System.out.println("  CURRENT ROOM:");
        System.out.println(theRoom.toString());

        // Describe room contents in plain text
        if (theRoom.hasEntrance()) {
            System.out.println("  [Entrance — where you started]");
        }
        if (theRoom.hasExit()) {
            System.out.println("  [Exit — escape is near!]");
        }
        if (theRoom.hasPit()) {
            System.out.println("  [Pit — " + theRoom.getPitDamage()
                    + " damage if you fall in]");
        }
        if (theRoom.getHealingPotion() != null) {
            System.out.println("  [Healing Potion]");
        }
        if (theRoom.getVisionPotion() != null) {
            System.out.println("  [Vision Potion]");
        }
        if (theRoom.getBomb() != null) {
            System.out.println("  [Bomb]");
        }
        if (theRoom.getPillar() != null) {
            System.out.println("  [Pillar of "
                    + theRoom.getPillar().name() + "]");
        }
        if (theRoom.hasMonster()) {
            System.out.println("  [Monster: "
                    + theRoom.getMonster().getName() + " ("
                    + theRoom.getMonster().getHitPoints() + " HP)]");
        }
        System.out.println(THIN_LINE);
    }

    /**
     * Displays the entire dungeon as a grid of room representations.
     * Each room is shown using its toString() 3-character-wide symbol.
     *
     * @param theDungeon the dungeon to display
     */
    @Override
    public void displayDungeon(final Dungeon theDungeon) {
        System.out.println(SEPARATOR);
        System.out.println("  DUNGEON MAP:");
        System.out.println(theDungeon.toString());
        System.out.println(SEPARATOR);
    }

    /**
     * Displays the surrounding rooms revealed by a Vision Potion.
     * Shows each room's toString() representation with its position.
     *
     * @param theRooms the list of surrounding rooms to display
     */
    @Override
    public void displayVision(final List<Room> theRooms) {
        System.out.println(THIN_LINE);
        System.out.println("  VISION POTION — Surrounding rooms:");

        if (theRooms == null || theRooms.isEmpty()) {
            System.out.println("  No surrounding rooms visible.");
        } else {
            for (final Room room : theRooms) {
                System.out.println(room.toString());
                System.out.println();
            }
        }
        System.out.println(THIN_LINE);
    }

    /**
     * Displays the current combat state between the hero and a monster.
     * Shows both characters' HP and available actions.
     *
     * @param theHero    the hero in combat
     * @param theMonster the monster in combat
     */
    @Override
    public void displayCombat(final Hero theHero,
                              final Monster theMonster) {
        System.out.println(SEPARATOR);
        System.out.println("  *** COMBAT ***");
        System.out.println(THIN_LINE);

        // Hero status
        System.out.println("  Hero: " + theHero.getName()
                + " | HP: " + theHero.getHitPoints());

        // Monster status
        System.out.println("  Enemy: " + theMonster.getName()
                + " | HP: " + theMonster.getHitPoints());

        System.out.println(THIN_LINE);

        // Show available combat actions
        System.out.println("  Actions:");
        System.out.println("    [A] Regular Attack");
        System.out.println("    [S] Special Skill");

        // Only show potion option if hero has some
        if (theHero.getHealingPotions() > 0) {
            System.out.println("    [H] Use Healing Potion ("
                    + theHero.getHealingPotions() + " left)");
        } else {
            System.out.println("    [H] Use Healing Potion (none left)");
        }

        // Only show bomb option if hero has some
        if (theHero.getBombs() > 0) {
            System.out.println("    [B] Use Bomb ("
                    + theHero.getBombs() + " left)");
        } else {
            System.out.println("    [B] Use Bomb (none left)");
        }

        System.out.println(SEPARATOR);
    }

    /**
     * Displays a single message to the player.
     * Used for all event feedback during gameplay.
     *
     * @param theMsg the message to display
     */
    @Override
    public void displayMessage(final String theMsg) {
        System.out.println("  > " + theMsg);
    }

    /**
     * Displays a summary of the hero's current stats and inventory.
     * Called after any event that changes hero state.
     *
     * @param theHero the hero whose stats to display
     */
    @Override
    public void displayHeroStats(final Hero theHero) {
        System.out.println(THIN_LINE);
        System.out.println("  HERO STATUS — " + theHero.getName());
        System.out.println("  HP:              " + theHero.getHitPoints());
        System.out.println("  Healing Potions: "
                + theHero.getHealingPotions());
        System.out.println("  Vision Potions:  "
                + theHero.getVisionPotions());
        System.out.println("  Bombs:           " + theHero.getBombs());
        System.out.println("  Pillars Found:   "
                + theHero.getPillarsFound().size() + "/4 "
                + formatPillars(theHero));
        System.out.println(THIN_LINE);
    }


    /**
     * Called automatically by GameModel whenever game state changes.
     * ConsoleView reads the property name and displays the appropriate
     * message or screen update.
     *
     * @param theEvt the property change event fired by GameModel
     */
    @Override
    public void propertyChange(final PropertyChangeEvent theEvt) {
        final String propName = theEvt.getPropertyName();

        switch (propName) {

            case GameModel.PROP_GAME_OVER:
                // Game ended — check if player won or lost
                final boolean gameOver = (boolean) theEvt.getNewValue();
                if (gameOver) {
                    System.out.println(SEPARATOR);
                    System.out.println("  GAME OVER");
                    System.out.println(SEPARATOR);
                }
                break;

            case GameModel.PROP_PLAYER_WON:
                // Player won — show victory message
                final boolean playerWon = (boolean) theEvt.getNewValue();
                if (playerWon) {
                    System.out.println(SEPARATOR);
                    System.out.println("  *** YOU WIN! ***");
                    System.out.println(
                            "  You collected all 4 Pillars of OO "
                                    + "and escaped the dungeon!");
                    System.out.println(SEPARATOR);
                }
                break;

            case GameModel.PROP_DUNGEON:
                // Dungeon was updated — display new dungeon
                final Dungeon dungeon =
                        (Dungeon) theEvt.getNewValue();
                if (dungeon != null) {
                    displayDungeon(dungeon);
                }
                break;

            case GameModel.PROP_HERO:
                // Hero was updated — display new hero stats
                final Hero hero = (Hero) theEvt.getNewValue();
                if (hero != null) {
                    displayHeroStats(hero);
                }
                break;

            case GameModel.PROP_COMBAT:
                // Combat started or ended
                final Object newVal = theEvt.getNewValue();
                if (newVal == null) {
                    // Combat ended
                    System.out.println(THIN_LINE);
                    System.out.println("  Combat has ended.");
                    System.out.println(THIN_LINE);
                }
                break;

            default:
                // Unknown property — ignore silently
                break;
        }
    }

    /**
     * Prompts the player in the console to choose a valid combat option.
     * Keeps looping until a valid command is input.
     *
     * @return the corresponding HeroAction matching the selection
     */
    @Override
    public HeroAction promptHeroAction() {
        final java.util.Scanner console = new java.util.Scanner(System.in);

        while (true) {
            System.out.print("  Select action [A, S, H, B]: ");
            final String choice = console.nextLine().trim().toUpperCase();

            switch (choice) {
                case "A":
                    return HeroAction.ATTACK;
                case "S":
                    return HeroAction.SPECIAL_SKILL;
                case "H":
                    return HeroAction.USE_HEALING_POTION;
                case "B":
                    return HeroAction.USE_BOMB;
                default:
                    System.out.println("  > Invalid option. Please try again.");
            }
        }
    }

    /**
     * Interprets a CombatEvent item and builds a clean message
     * output to System.out.
     *
     * @param theEvent the event detailing actions, damage, or item use
     */
    @Override
    public void displayCombatEvent(final CombatEvent theEvent) {
        if (theEvent == null) {
            return;
        }

        final String actor = theEvent.getActor();
        final String target = theEvent.getTarget();
        final int amount = theEvent.getAmount();

        switch (theEvent.getType()) {
            case ATTACK_HIT:
                System.out.println("  > " + actor + " attacks " + target + " for " + amount + " damage!");
                break;

            case ATTACK_BLOCKED:
                System.out.println("  > " + target + " blocked " + actor + "'s incoming attack!");
                break;

            case SPECIAL_SUCCESS:
                System.out.println("  > " + actor + " unleashes a special ability on " + target + " dealing " + amount + " damage!");
                break;

            case POTION_USED:
                System.out.println("  > " + actor + " consumes a healing potion, recovering " + amount + " HP!");
                break;

            case BOMB_USED:
                System.out.println("  > BOOM! " + actor + " detonates a bomb on " + target + " for " + amount + " damage!");
                break;

            case ITEM_UNAVAILABLE:
                System.out.println("  > " + actor + " tried to use an item, but the inventory slot was empty!");
                break;

            case COMBAT_END:
                // amount == 1 represents hero victory inside Combat.java finishCombat logic
                if (amount == 1) {
                    System.out.println("\n  *** VICTORY — " + actor + " stands triumphant! ***");
                } else {
                    System.out.println("\n  *** DEFEAT — " + actor + " has fallen in combat... ***");
                }
                break;

            default:
                System.out.println("  > " + actor + " performed an action against " + target);
                break;
        }
    }


    /**
     * Formats the hero's collected pillars as a compact string.
     * Shows which pillars have been collected using their first letter.
     * For example: [A] [E] [_] [_] means Abstraction and Encapsulation
     * have been collected but Inheritance and Polymorphism have not.
     *
     * @param theHero the hero to read pillar data from
     * @return formatted pillar string
     */
    private String formatPillars(final Hero theHero) {
        final StringBuilder sb = new StringBuilder();
        for (final Pillar p : Pillar.values()) {
            if (theHero.getPillarsFound().contains(p)) {
                sb.append("[")
                        .append(p.getDisplayCharacter())
                        .append("] ");
            } else {
                sb.append("[_] ");
            }
        }
        return sb.toString().trim();
    }
}