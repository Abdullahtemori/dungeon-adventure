package edu.uw.tcss.dungeoneer;

import edu.uw.tcss.dungeoneer.controller.GameController;
import edu.uw.tcss.dungeoneer.model.Difficulty;
import edu.uw.tcss.dungeoneer.model.Direction;
import edu.uw.tcss.dungeoneer.model.SaveLoadManager;
import edu.uw.tcss.dungeoneer.view.AudioManager;
import edu.uw.tcss.dungeoneer.view.ConsoleView;
import edu.uw.tcss.dungeoneer.view.GameView;
import edu.uw.tcss.dungeoneer.view.SwingView;

import javax.swing.SwingUtilities;
import java.util.Scanner;

/**
 * Single entry point for the Dungeon Adventure game.
 * Launches either the Swing GUI (default) or the console version
 * depending on a command line flag. The Swing flow is fully driven
 * by the menu inside SwingView, while the console flow uses a
 * simple text menu and a minimal navigation loop that runs until
 * the player wins, loses, or quits.
 * Usage:
 * java edu.uw.tcss.dungeoneer.DungeonAdventure              -> Swing mode
 * java edu.uw.tcss.dungeoneer.DungeonAdventure --swing      -> Swing mode
 * java edu.uw.tcss.dungeoneer.DungeonAdventure --console    -> Console mode
 *
 * @author Tarik Atasoy
 * @version Iteration 4
 */
public final class DungeonAdventure {

    /**
     * CLI flag that selects the text-based view.
     */
    private static final String FLAG_CONSOLE = "--console";

    /**
     * Prevents instantiation of this launcher class.
     */
    private DungeonAdventure() {
        // no instances
    }

    /**
     * Program entry point. Picks the view based on the first argument
     * and hands off to the matching launch routine.
     *
     * @param theArgs command line arguments; first arg may be a view flag
     */
    static void main(final String[] theArgs) {
        final boolean consoleMode =
                theArgs != null && theArgs.length > 0
                        && FLAG_CONSOLE.equalsIgnoreCase(theArgs[0]);

        if (consoleMode) {
            launchConsole();
        } else {
            launchSwing();
        }
    }

    /**
     * Builds the Swing view and controller, wires them together, and
     * shows the window on the Event Dispatch Thread. The window's File
     * menu provides New Game, Load Game, and Exit, so no extra welcome
     * loop is needed here.
     */
    private static void launchSwing() {
        SwingUtilities.invokeLater(() -> {
            final SwingView view = new SwingView();
            final GameController controller = new GameController(view);
            AudioManager.getInstance().playMusic(
                    AudioManager.MUSIC_MENU);
            view.setController(controller);
            view.show();
        });
    }

    /**
     * Runs the text-based welcome screen and dispatches to a new game,
     * a loaded game, or exit. After a game starts, a minimal navigation
     * loop reads single-letter commands until the game ends or the
     * player quits.
     */
    private static void launchConsole() {
        final ConsoleView view = new ConsoleView();
        final GameController controller = new GameController(view);
        final Scanner in = new Scanner(System.in);

        printWelcomeBanner();

        boolean exitRequested = false;
        while (!exitRequested) {
            final int choice = readWelcomeChoice(in);

            switch (choice) {
                case 1:
                    if (startNewGameFromConsole(in, controller, view)) {
                        runConsoleGameLoop(in, controller, view);
                    }
                    break;
                case 2:
                    controller.loadGame(SaveLoadManager.DEFAULT_SAVE_PATH);
                    if (controller.isRunning()) {
                        runConsoleGameLoop(in, controller, view);
                    }
                    break;
                case 3:
                    exitRequested = true;
                    break;
                default:
                    view.displayMessage("Invalid selection. Try again.");
                    break;
            }
        }

        System.out.println("Thanks for playing Dungeon Adventure!");
    }

    /**
     * Prints the static welcome banner shown above the main menu.
     */
    private static void printWelcomeBanner() {
        System.out.println();
        System.out.println("  ====================================");
        System.out.println("       DUNGEON ADVENTURE");
        System.out.println("       TCSS 360 - Spring 2026");
        System.out.println("  ====================================");
        System.out.println();
    }

    /**
     * Prompts the player for a welcome-menu choice and returns the
     * numeric selection. Returns -1 if the input cannot be parsed.
     *
     * @param theIn shared scanner over System.in
     * @return 1 for New Game, 2 for Load Game, 3 for Exit, -1 otherwise
     */
    private static int readWelcomeChoice(final Scanner theIn) {
        System.out.println("  1) New Game");
        System.out.println("  2) Load Game");
        System.out.println("  3) Exit");
        System.out.print("  Select an option: ");

        final String line = theIn.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (final NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Collects hero name, hero class, and difficulty from the console,
     * then asks the controller to start a new game. Returns true if a
     * game actually started so the caller can enter the play loop.
     *
     * @param theIn         shared scanner over System.in
     * @param theController the controller that owns the new game
     * @param theView       the console view used for feedback messages
     * @return true if startNewGame was invoked, false on bad input
     */
    private static boolean startNewGameFromConsole(final Scanner theIn,
                                                   final GameController theController,
                                                   final GameView theView) {
        System.out.print("  Enter your hero name: ");
        final String name = theIn.nextLine().trim();
        if (name.isEmpty()) {
            theView.displayMessage("Hero name cannot be empty.");
            return false;
        }

        final String heroType = promptHeroClass(theIn, theView);
        if (heroType == null) {
            return false;
        }

        final Difficulty difficulty = promptDifficulty(theIn, theView);
        if (difficulty == null) {
            return false;
        }

        theController.startNewGame(name, heroType, difficulty);
        return true;
    }

    /**
     * Asks the player to pick one of the three available hero classes.
     *
     * @param theIn   shared scanner over System.in
     * @param theView console view for error feedback
     * @return canonical class name, or null on invalid input
     */
    private static String promptHeroClass(final Scanner theIn,
                                          final GameView theView) {
        System.out.println("  Choose a hero class:");
        System.out.println("    1) Warrior   - Crushing Blow");
        System.out.println("    2) Priestess - Heal");
        System.out.println("    3) Thief     - Surprise Attack");
        System.out.print("  Selection: ");

        final String choice = theIn.nextLine().trim();
        return switch (choice) {
            case "1" -> "Warrior";
            case "2" -> "Priestess";
            case "3" -> "Thief";
            default -> {
                theView.displayMessage("Invalid hero class.");
                yield null;
            }
        };
    }

    /**
     * Asks the player to pick a difficulty level.
     *
     * @param theIn   shared scanner over System.in
     * @param theView console view for error feedback
     * @return chosen Difficulty, or null on invalid input
     */
    private static Difficulty promptDifficulty(final Scanner theIn,
                                               final GameView theView) {
        System.out.println("  Choose difficulty:");
        System.out.println("    1) EASY   (5x5)");
        System.out.println("    2) MEDIUM (7x7)");
        System.out.println("    3) HARD   (10x10)");
        System.out.print("  Selection: ");

        final String choice = theIn.nextLine().trim();
        return switch (choice) {
            case "1" -> Difficulty.EASY;
            case "2" -> Difficulty.MEDIUM;
            case "3" -> Difficulty.HARD;
            default -> {
                theView.displayMessage("Invalid difficulty.");
                yield null;
            }
        };
    }

    /**
     * Hidden cheat keyword. Typing this at the navigation prompt
     * toggles the controller's cheat mode and reveals the full map.
     * Not advertised in any printed menu so the player must know it.
     */
    private static final String CHEAT_CODE = "XYZZY";

    /**
     * Full text navigation loop covering all DA-32 acceptance criteria.
     * Each turn: reprint the current room and a short status line,
     * read one command, dispatch to the controller, and let model
     * events drive the win/lose/combat output through the view.
     * <p>
     * Commands:
     * N/S/E/W  move in a direction
     * H        use a healing potion
     * V        use a vision potion
     * T        reprint hero stats
     * M        print the full map (only effective while cheat is on)
     * S        save the game to the default file
     * L        load the game from the default file
     * Q        quit the current game and return to the welcome menu
     * XYZZY    hidden cheat toggle (reveals dungeon map)
     *
     * @param theIn         shared scanner over System.in
     * @param theController controller driving the current game
     * @param theView       console view used for prompts and feedback
     */
    private static void runConsoleGameLoop(final Scanner theIn,
                                           final GameController theController,
                                           final GameView theView) {

        // Render the starting room and stats once before the first prompt
        printTurnState(theController, theView);

        while (theController.isRunning()
                && theController.getModel() != null
                && !theController.getModel().isGameOver()) {

            System.out.println();
            System.out.println("  Commands: [N][S][E][W] move  "
                    + "[H] heal  [V] vision  [T] status");
            System.out.println("            [SAVE] save  [LOAD] load  "
                    + "[M] map  [Q] quit");
            System.out.print("  > ");

            final String raw = theIn.nextLine().trim();
            if (raw.isEmpty()) {
                continue;
            }
            final String cmd = raw.toUpperCase();

            // Hidden cheat keyword is matched before normal commands
            if (CHEAT_CODE.equalsIgnoreCase(raw)) {
                theController.toggleCheatMode();
                continue;
            }

            try {
                switch (cmd) {
                    case "N":
                        theController.handleMove(Direction.NORTH);
                        printTurnState(theController, theView);
                        break;
                    case "S":
                        theController.handleMove(Direction.SOUTH);
                        printTurnState(theController, theView);
                        break;
                    case "E":
                        theController.handleMove(Direction.EAST);
                        printTurnState(theController, theView);
                        break;
                    case "W":
                        theController.handleMove(Direction.WEST);
                        printTurnState(theController, theView);
                        break;
                    case "H":
                        theController.handleUseHealingPotion();
                        break;
                    case "V":
                        theController.handleUseVisionPotion();
                        break;
                    case "T":
                        theView.displayHeroStats(
                                theController.getModel().getHero());
                        break;
                    case "M":
                        if (theController.isCheatMode()) {
                            theView.displayDungeon(
                                    theController.getModel().getDungeon());
                        } else {
                            theView.displayMessage(
                                    "Map view is unavailable.");
                        }
                        break;
                    case "SAVE":
                        theController.saveGame(
                                SaveLoadManager.DEFAULT_SAVE_PATH);
                        break;
                    case "LOAD":
                        theController.loadGame(
                                SaveLoadManager.DEFAULT_SAVE_PATH);
                        break;
                    case "Q":
                        theView.displayMessage("Returning to main menu.");
                        return;
                    default:
                        theView.displayMessage("Unknown command: " + raw);
                        break;
                }
            } catch (final RuntimeException ex) {
                // Never let a backend failure kill the game loop
                theView.displayMessage(
                        "An error occurred this turn: " + ex.getMessage());
            }
        }
    }

    /**
     * Prints the current room and the hero's status line. Called once
     * at game start and after each successful move so the player can
     * always see where they are and how they are doing.
     *
     * @param theController active controller
     * @param theView       console view to render through
     */
    private static void printTurnState(final GameController theController,
                                       final GameView theView) {
        if (theController.getModel() == null
                || theController.getModel().isGameOver()) {
            return;
        }
        theView.displayRoom(
                theController.getModel().getDungeon().getCurrentRoom());
        theView.displayHeroStats(theController.getModel().getHero());
    }
}
