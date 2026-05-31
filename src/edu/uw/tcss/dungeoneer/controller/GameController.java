package edu.uw.tcss.dungeoneer.controller;

import edu.uw.tcss.dungeoneer.model.*;
import edu.uw.tcss.dungeoneer.view.GameView;

/**
 * GameController is the Controller in the MVC pattern.
 * It receives input from the player (via GameView), translates
 * that input into model operations, then tells the View to update.
 * GameController NEVER holds game state directly. All state lives
 * in GameModel. GameController only reads from and writes to the model.
 *
 * Fix applied in this version:
 *   handleUseHealingPotion() now correctly applies the healed HP amount
 *   to the hero's hit points when used outside of combat. Previously
 *   Hero.useHealingPotion() returned the heal amount but never applied
 *   it, and the controller also failed to call setHitPoints(), meaning
 *   potions used in navigation mode had no actual effect on the hero's HP.
 *
 * @author Daniella Birungi
 * @author Abdullah Temori 
 * @version Iteration 4 (bugfix)
 */
public class GameController {

    /** The game model holding all state. */
    private GameModel myModel;

    /** The view used to display information to the player. */
    private final GameView myView;

    /** Whether cheat mode is active (shows entire dungeon after each move). */
    private boolean myCheatMode;

    /** Whether a game is currently in progress. */
    private boolean myRunning;

    /**
     * Constructs a GameController wired to the given view.
     * The model is not created here — it is built when startNewGame()
     * is called or assigned when loadGame() restores a saved session.
     *
     * @param theView the view this controller will send output to; must not be null
     */
    public GameController(final GameView theView) {
        myView = theView;
        myCheatMode = false;
        myRunning = false;
    }

    /**
     * Starts a new game with the given hero name, class, and difficulty.
     *
     * Steps performed in order:
     *   1. Build the dungeon using DungeonBuilder with the chosen difficulty.
     *   2. Create the hero using HeroFactory with the chosen class and name.
     *   3. Create a new GameModel containing the dungeon, hero, and difficulty.
     *   4. Register the view as a PropertyChangeListener on the model so it
     *      updates automatically whenever model state changes.
     *   5. Apply room-entry effects at the entrance (item pickup, pit check).
     *   6. Display the starting room and hero stats.
     *
     * @param theName       the hero's name entered by the player; must not be blank
     * @param theHeroType   the hero class: "Warrior", "Priestess", or "Thief"
     * @param theDifficulty the selected difficulty level; must not be null
     */
    public void startNewGame(final String theName,
                             final String theHeroType,
                             final Difficulty theDifficulty) {
        // reset cheat mode for the new game
        myCheatMode = false;
        myView.displayMessage("cheat Mode reset for New game");
        // Step 1: Build the dungeon
        final Dungeon dungeon = new DungeonBuilder()
                .setDifficulty(theDifficulty)
                .build();

        // Step 2: Create the hero
        final HeroFactory heroFactory = new HeroFactory();
        final Hero hero = heroFactory.createHero(theHeroType, theName);

        // Step 3: Create the model
        myModel = new GameModel(dungeon, hero, theDifficulty);

        // Step 4: Register view as listener so it updates on state changes
        myModel.addPropertyChangeListener(myView);

        myRunning = true;

        // Step 5: Apply room entry effects at the entrance
        onEnterRoom(dungeon.getCurrentRoom());

        // Step 6: Show the starting room and hero stats
        myView.displayRoom(dungeon.getCurrentRoom());
        myView.displayHeroStats(hero);
    }

    /**
     * Saves the current game state to the given file path using serialization.
     * Displays a success or failure message to the player via the view.
     * Does nothing if no game is currently in progress.
     *
     * @param thePath the file path to write the save file to; must not be null
     */
    public void saveGame(final String thePath) {
        if (myModel == null) {
            myView.displayMessage("No game in progress to save.");
            return;
        }

        final boolean success = SaveLoadManager.saveGame(myModel, thePath);

        if (success) {
            myView.displayMessage("Game saved successfully.");
        } else {
            myView.displayMessage("Save failed. Check console for details.");
        }
    }

    /**
     * Loads a previously saved game from the given file path.
     * Replaces the current model with the loaded one and re-registers the
     * view as a listener (listeners are not serialized).
     * Displays an error message if the file is missing or corrupted.
     *
     * @param thePath the file path to read the save file from; must not be null
     */
    public void loadGame(final String thePath) {
        final GameModel loaded = SaveLoadManager.loadGame(thePath);

        if (loaded == null) {
            myView.displayMessage(
                    "Load failed. Save file not found or corrupted.");
            return;
        }

        myModel = loaded;

        // Re-register the view since listeners are transient and not saved
        myModel.addPropertyChangeListener(myView);

        myRunning = true;

        myView.displayMessage("Game loaded successfully.");
        myView.displayRoom(myModel.getDungeon().getCurrentRoom());
        myView.displayHeroStats(myModel.getHero());
    }

    /**
     * Handles the player choosing to move in a direction.
     *
     * Steps performed in order:
     *   1. Attempt to move the hero in the dungeon grid.
     *   2. If the move failed (wall), inform the player and return.
     *   3. Apply room-entry effects in the new room.
     *   4. Check win and lose conditions.
     *   5. Display the new room; in cheat mode also display the full dungeon.
     *
     * @param theDir the direction to move (must not be null)
     */
    public void handleMove(final Direction theDir) {
        final Dungeon dungeon = myModel.getDungeon();
        final boolean moved = dungeon.moveHero(theDir);

        if (!moved) {
            myView.displayMessage(
                    "There is no door to the " + theDir + ".");
            return;
        }

        final Room currentRoom = dungeon.getCurrentRoom();
        onEnterRoom(currentRoom);

        if (checkWinLose()) {
            return;
        }

        myView.displayRoom(currentRoom);

        if (myCheatMode) {
            myView.displayDungeon(dungeon);
        }
    }

    /**
     * Applies all automatic effects when the hero enters a room.
     *
     * Effects applied in order:
     *   1. All collectible items (potions, bombs, pillars) are picked up
     *      and removed from the room. The view is notified of each pickup.
     *   2. If the room has a pit, the hero loses HP and the view is notified.
     *   3. If the room contains a monster, combat begins immediately.
     *
     * @param theRoom the room the hero just entered; must not be null
     */
    public void onEnterRoom(final Room theRoom) {
        final Hero hero = myModel.getHero();

        // Snapshot inventory counts before pickup to detect changes
        final int potionsBefore  = hero.getHealingPotions();
        final int visionsBefore  = hero.getVisionPotions();
        final int bombsBefore    = hero.getBombs();
        final int pillarsBefore  = hero.getPillarsFound().size();

        // Pick up all items; items are removed from the room on pickup
        theRoom.pickUpItems(hero);

        // Notify the player of each item collected
        if (hero.getHealingPotions() > potionsBefore) {
            myView.displayMessage("You picked up a Healing Potion! ("
                    + hero.getHealingPotions() + " total)");
        }
        if (hero.getVisionPotions() > visionsBefore) {
            myView.displayMessage("You picked up a Vision Potion! ("
                    + hero.getVisionPotions() + " total)");
        }
        if (hero.getBombs() > bombsBefore) {
            myView.displayMessage("You picked up a Bomb! ("
                    + hero.getBombs() + " total)");
        }
        if (hero.getPillarsFound().size() > pillarsBefore) {
            myView.displayMessage("You found a Pillar of OO! ("
                    + hero.getPillarsFound().size() + "/4 collected)");
        }

        // Apply pit damage if present
        if (theRoom.hasPit()) {
            final int damage = theRoom.getPitDamage();
            hero.setHitPoints(hero.getHitPoints() - damage);
            myView.displayMessage("You fell into a pit! Lost " + damage
                    + " HP. (" + hero.getHitPoints() + " HP remaining)");
        }

        // Start combat if a monster is present
        if (theRoom.hasMonster()) {
            handleCombat(theRoom.getMonster());
        }
    }

    /**
     * Handles the player choosing to use a Healing Potion outside of combat.
     * Does nothing if the hero has no healing potions remaining.
     */
    public void handleUseHealingPotion() {
        final Hero hero = myModel.getHero();

        if (hero.getHealingPotions() <= 0) {
            myView.displayMessage("You have no Healing Potions left.");
            return;
        }

        final int healed = hero.useHealingPotion();
        hero.setHitPoints(hero.getHitPoints() + healed);

        myView.displayMessage(
                "You used a Healing Potion and restored "
                        + healed + " HP. ("
                        + hero.getHitPoints() + " HP remaining, "
                        + hero.getHealingPotions() + " potions left)");

        myView.displayHeroStats(hero);
    }

    /**
     * Handles the player choosing to use a Vision Potion outside of combat.
     * Reveals the contents of up to 8 rooms surrounding the hero's current
     * position. Does nothing if the hero has no vision potions remaining.
     */
    public void handleUseVisionPotion() {
        final Hero hero = myModel.getHero();

        if (hero.getVisionPotions() <= 0) {
            myView.displayMessage("You have no Vision Potions left.");
            return;
        }

        final boolean used = hero.useVisionPotion();

        if (used) {
            myView.displayMessage(
                    "You used a Vision Potion. Surrounding rooms revealed! ("
                            + hero.getVisionPotions() + " potions left)");
            myView.displayVision(
                    myModel.getDungeon().getSurroundingRooms());
        }
    }

    /**
     * Handles turn-based combat between the hero and a monster.
     * Called automatically when the hero enters a room containing a monster.
     *
     * Each round the view prompts the player for an action, the Combat engine
     * processes it, and the resulting CombatEvents are sent back to the view
     * for display. Combat continues until one side reaches 0 HP.
     *
     * On hero victory: the monster is removed from the room.
     * On hero defeat: the game is flagged as over.
     *
     * @param theMonster the monster to fight; must not be null and must be alive
     */
    public void handleCombat(final Monster theMonster) {
        myView.displayMessage(
                "A " + theMonster.getName()
                        + " blocks your path! Combat begins!");

        myModel.startCombat(theMonster);

        final Hero hero = myModel.getHero();
        final Combat combat = new Combat(hero, theMonster);

        while (!combat.isOver()) {
            myView.displayCombat(hero, theMonster);

            final HeroAction chosenAction = myView.promptHeroAction();

            final java.util.List<CombatEvent> roundEvents =
                    combat.executeHeroAction(chosenAction);

            for (final CombatEvent event : roundEvents) {
                myView.displayCombatEvent(event);
            }
        }

        if (combat.heroWon()) {
            myView.displayMessage(
                    "You defeated the " + theMonster.getName() + "!");
            myModel.getDungeon().getCurrentRoom().setMonster(null);
            myView.displayHeroStats(hero);
        } else {
            myView.displayMessage(
                    "You were defeated by the "
                            + theMonster.getName() + "...");
            myModel.setGameOver(true);
        }
    }

    /**
     * Processes a single hero combat action during an active combat encounter.
     * Called by CombatPanel (Swing GUI) when the player clicks an action button.
     * Does nothing if there is no active combat in the model.
     *
     * @param theAction the action the player chose; must not be null
     */
    public void handleCombatAction(final HeroAction theAction) {
        if (myModel == null || myModel.getActiveCombat() == null) {
            myView.displayMessage("No active combat.");
            return;
        }

        final Combat combat = myModel.getActiveCombat();
        final Hero hero = myModel.getHero();

        final java.util.List<CombatEvent> events =
                combat.executeHeroAction(theAction);

        for (final CombatEvent event : events) {
            myView.displayCombatEvent(event);
        }

        if (combat.isOver()) {
            if (combat.heroWon()) {
                myView.displayMessage(
                        "You defeated " + combat.getMonster().getName() + "!");
                myModel.getDungeon().getCurrentRoom().setMonster(null);
                myModel.endCombat();
            } else {
                myView.displayMessage("You have been defeated...");
                myModel.setGameOver(true);
                myModel.endCombat();
            }
        }
    }

    /**
     * Checks whether the game has ended after a room entry or combat.
     *
     * Lose condition: hero HP is 0 or below.
     *   — Displays the full dungeon map, sets game-over flags, stops the loop.
     *
     * Win condition: hero has all 4 pillars AND is standing in the exit room.
     *   — Displays a congratulations message and the full dungeon map,
     *     sets the win and game-over flags, stops the loop.
     *
     * @return true if the game has ended (win or loss), false if still running
     */
    public boolean checkWinLose() {
        final Hero hero = myModel.getHero();
        final Room currentRoom = myModel.getDungeon().getCurrentRoom();

        // Lose condition: hero is dead
        if (!hero.isAlive()) {
            myView.displayMessage("You have been defeated. Game over.");
            myView.displayDungeon(myModel.getDungeon());
            myModel.setGameOver(true);
            myModel.setPlayerWon(false);
            myRunning = false;
            return true;
        }

        // Win condition: all 4 pillars collected and standing at the exit
        final boolean hasAllPillars =
                hero.getPillarsFound().size() == Pillar.values().length;
        final boolean atExit = currentRoom.hasExit();

        if (hasAllPillars && atExit) {
            myView.displayMessage(
                    "Congratulations! You collected all 4 Pillars "
                            + "and escaped the dungeon! YOU WIN!");
            myView.displayDungeon(myModel.getDungeon());
            myModel.setPlayerWon(true);
            myModel.setGameOver(true);
            myRunning = false;
            return true;
        }

        return false;
    }

    /**
     * Toggles cheat mode on or off.
     * When cheat mode is active, the entire dungeon map is displayed after
     * every move so the player can see all rooms and their contents.
     * This is a hidden testing feature required by the project specification.
     * In the console it is activated by typing XYZZY; in the Swing GUI
     * it is accessible via Help > Cheat Mode.
     */
    public void toggleCheatMode() {
        myCheatMode = !myCheatMode;
        myView.displayMessage("Cheat mode " + (myCheatMode ? "ON" : "OFF"));

        if (myCheatMode && myModel != null) {
            myView.displayDungeon(myModel.getDungeon());
        }
    }

    /**
     * Returns whether cheat mode is currently active.
     * Used by SwingView to keep the cheat mode checkbox in sync.
     *
     * @return true if cheat mode is on, false otherwise
     */
    public boolean isCheatMode() {
        return myCheatMode;
    }

    /**
     * Returns whether a game is currently in progress.
     * Used by the console game loop to decide whether to keep running.
     *
     * @return true if a game is active, false if no game has started
     *         or the game has ended
     */
    public boolean isRunning() {
        return myRunning;
    }

    /**
     * Returns the current game model.
     * Intended for use by unit tests that need to inspect model state
     * after controller actions without going through the view.
     *
     * @return the current GameModel, or null if no game has been started
     */
    public GameModel getModel() {
        return myModel;
    }
}
