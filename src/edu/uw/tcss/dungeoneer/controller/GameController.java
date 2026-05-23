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
 * @author Daniella Birungi
 * @version Iteration 3
 */
public class GameController {


    /** The game model holding all state. */
    private GameModel myModel;

    /** The view used to display information to the player. */
    private final GameView myView;

    /** Whether cheat mode is active (shows entire dungeon). */
    private boolean myCheatMode;

    /** Whether the game loop is currently running. */
    private boolean myRunning;

    /**
     * Constructs a GameController with the given view.
     * The model is not set here — it is created when startNewGame()
     * is called or assigned when loadGame() is called.
     *
     * @param theView the view this controller will send output to
     */
    public GameController(final GameView theView) {
        myView = theView;
        myCheatMode = false;
        myRunning = false;
    }

    /**
     * Starts a new game with the given hero name, class, and difficulty.
     * Steps:
     * 1. Build the dungeon using DungeonBuilder
     * 2. Create the hero using HeroFactory
     * 3. Create GameModel with dungeon, hero, difficulty
     * 4. Register the view as a listener on the model
     * 5. Place hero at entrance and trigger room entry effects
     *
     * @param theName       the hero's name entered by the player
     * @param theHeroType   the hero class: "Warrior", "Priestess", "Thief"
     * @param theDifficulty the selected difficulty level
     */
    public void startNewGame(final String theName,
                             final String theHeroType,
                             final Difficulty theDifficulty) {

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

        // Step 5: Apply room entry effects at starting room (entrance)
        onEnterRoom(dungeon.getCurrentRoom());

        // Show the starting room
        myView.displayRoom(dungeon.getCurrentRoom());
        myView.displayHeroStats(hero);
    }

    /**
     * Saves the current game to the given file path.
     * Shows a message to the player confirming success or failure.
     *
     * @param thePath the file path to save to
     */
    public void saveGame(final String thePath) {
        if (myModel == null) {
            myView.displayMessage("No game in progress to save.");
            return;
        }

        final boolean success =
                SaveLoadManager.saveGame(myModel, thePath);

        if (success) {
            myView.displayMessage("Game saved successfully.");
        } else {
            myView.displayMessage(
                    "Save failed. Check console for details.");
        }
    }

    /**
     * Loads a game from the given file path.
     * Replaces the current model with the loaded one and
     * re-registers the view as a listener.
     *
     * @param thePath the file path to load from
     */
    public void loadGame(final String thePath) {
        final GameModel loaded = SaveLoadManager.loadGame(thePath);

        if (loaded == null) {
            myView.displayMessage(
                    "Load failed. Save file not found or corrupted.");
            return;
        }

        // Replace the current model with the loaded one
        myModel = loaded;

        // Re-register the view since listeners are not serialized
        myModel.addPropertyChangeListener(myView);

        myRunning = true;

        myView.displayMessage("Game loaded successfully.");
        myView.displayDungeon(myModel.getDungeon());
        myView.displayRoom(myModel.getDungeon().getCurrentRoom());
        myView.displayHeroStats(myModel.getHero());
    }

    /**
     * Handles the player choosing to move in a direction.
     * Steps:
     * 1. Attempt to move in the dungeon
     * 2. If move succeeded, apply room entry effects
     * 3. Check win/lose conditions
     * 4. Update the view
     *
     * @param theDir the direction to move (NORTH, SOUTH, EAST, WEST)
     */
    public void handleMove(final Direction theDir) {
        final Dungeon dungeon = myModel.getDungeon();
        final boolean moved = dungeon.moveHero(theDir);

        if (!moved) {
            // No door in that direction
            myView.displayMessage(
                    "There is no door to the " + theDir + ".");
            return;
        }

        // Hero moved — apply room effects
        final Room currentRoom = dungeon.getCurrentRoom();
        onEnterRoom(currentRoom);

        // Check if the game has ended
        if (checkWinLose()) {
            return;
        }

        // Update the view with the new room
        myView.displayRoom(currentRoom);

        // In cheat mode show the entire dungeon
        if (myCheatMode) {
            myView.displayDungeon(dungeon);
        }
    }


    /**
     * Applies all effects when the hero enters a room.
     * This method handles:
     * - Automatic item pickup (potions, bombs, pillars)
     * - Pit damage
     * - Monster encounters (starts combat)
     *
     * @param theRoom the room the hero just entered
     */
    public void onEnterRoom(final Room theRoom) {
        final Hero hero = myModel.getHero();

        // --- Step 1: Pick up all items in the room ---
        // pickUpItems handles null checks internally for each slot
        // It also removes items from the room so they can't be
        // collected again on a future visit
        final int potionsBefore = hero.getHealingPotions();
        final int visionsBefore = hero.getVisionPotions();
        final int bombsBefore = hero.getBombs();
        final int pillarsBefore = hero.getPillarsFound().size();

        theRoom.pickUpItems(hero);

        // Report what was picked up
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
            myView.displayMessage(
                    "You found a Pillar of OO! ("
                            + hero.getPillarsFound().size()
                            + "/4 collected)");
        }

        // --- Step 2: Apply pit damage ---
        if (theRoom.hasPit()) {
            final int damage = theRoom.getPitDamage();
            hero.setHitPoints(hero.getHitPoints() - damage);
            myView.displayMessage(
                    "You fell into a pit! Lost " + damage + " HP. ("
                            + hero.getHitPoints() + " HP remaining)");
        }

        // --- Step 3: Trigger combat if monster present ---
        if (theRoom.hasMonster()) {
            handleCombat(theRoom.getMonster());
        }
    }

    /**
     * Handles the player choosing to use a Healing Potion.
     * Can be called from navigation mode (outside combat).
     * During combat, this is called as a combat action choice.
     */
    public void handleUseHealingPotion() {
        final Hero hero = myModel.getHero();

        // Check if the hero has any potions
        if (hero.getHealingPotions() <= 0) {
            myView.displayMessage(
                    "You have no Healing Potions left.");
            return;
        }

        // Use the potion — returns amount healed
        final int healed = hero.useHealingPotion();

        myView.displayMessage(
                "You used a Healing Potion and restored "
                        + healed + " HP. ("
                        + hero.getHitPoints() + " HP remaining, "
                        + hero.getHealingPotions() + " potions left)");

        myView.displayHeroStats(hero);
    }

    /**
     * Handles the player choosing to use a Vision Potion.
     * Reveals the contents of the 8 surrounding rooms.
     * Vision Potions can only be used in navigation mode,
     * not during combat.
     */
    public void handleUseVisionPotion() {
        final Hero hero = myModel.getHero();

        // Check if the hero has any vision potions
        if (hero.getVisionPotions() <= 0) {
            myView.displayMessage(
                    "You have no Vision Potions left.");
            return;
        }

        // Use the potion — returns true if successful
        final boolean used = hero.useVisionPotion();

        if (used) {
            // Get and display surrounding rooms
            myView.displayMessage(
                    "You used a Vision Potion. Surrounding rooms revealed! ("
                            + hero.getVisionPotions() + " potions left)");

            myView.displayVision(
                    myModel.getDungeon().getSurroundingRooms());
        }
    }

    /**
     * Handles the player choosing to use a Bomb in combat.
     * Deals 75-150 damage to the target monster.
     *
     * @param theTarget the monster to bomb
     */
    public void handleUseBomb(final Monster theTarget) {
        final Hero hero = myModel.getHero();

        // Check if the hero has any bombs
        if (hero.getBombs() <= 0) {
            myView.displayMessage(
                    "You have no Bombs left.");
            return;
        }

        // Use the bomb — returns damage dealt
        final int damage = hero.useBomb(theTarget);

        myView.displayMessage(
                "BOOM! You threw a Bomb dealing "
                        + damage + " damage to "
                        + theTarget.getName() + "! ("
                        + hero.getBombs() + " bombs left, "
                        + theTarget.getHitPoints() + " HP remaining)");

        myView.displayCombat(hero, theTarget);
    }

    /**
     * Handles combat between the hero and a monster.
     * Called automatically when the hero enters a room with a monster.
     * After combat ends the monster is removed from the room
     * if the hero won, or the game ends if the hero lost.
     *
     * @param theMonster the monster to fight
     */
    public void handleCombat(final Monster theMonster) {
        myView.displayMessage(
                "A " + theMonster.getName()
                        + " blocks your path! Combat begins!");

        // Store combat in model so CombatPanel can access it
        myModel.startCombat(theMonster);

        final Hero hero = myModel.getHero();
        final Combat combat = new Combat(hero, theMonster);

        // Turn-based game loop: runs until the combat instance flags it is over
        while (!combat.isOver()) {
            myView.displayCombat(hero, theMonster);

            // 1. Ask the view/user to pick an action (Attack, Potion, Skill, Bomb)
            final HeroAction chosenAction = myView.promptHeroAction();

            // 2. Process the round using your Combat engine
            final java.util.List<CombatEvent> roundEvents =
                    combat.executeHeroAction(chosenAction);

            // 3. Send the list of what happened back to the view to show text/animations
            for (final CombatEvent event : roundEvents) {
                myView.displayCombatEvent(event);
            }
        }

        // Check the conclusion of combat
        if (combat.heroWon()) {
            myView.displayMessage(
                    "You defeated the " + theMonster.getName() + "!");

            // Remove the monster from the room
            myModel.getDungeon().getCurrentRoom().setMonster(null);
            myView.displayHeroStats(hero);
        } else {
            // Hero died — end the game
            myView.displayMessage(
                    "You were defeated by the "
                            + theMonster.getName() + "...");
            myModel.setGameOver(true);
        }
    }

    /**
     * Processes a single hero combat action during an active combat encounter.
     * Called by CombatPanel when the player clicks an action button.
     *
     * @param theAction the action the player chose (ATTACK, SPECIAL_SKILL, etc.)
     */
    public void handleCombatAction(final HeroAction theAction) {
        // Guard —> do nothing if no active combat in the model
        if (myModel == null || myModel.getActiveCombat() == null) {
            myView.displayMessage("No active combat.");
            return;
        }

        final Combat combat = myModel.getActiveCombat();
        final Hero hero = myModel.getHero();

        // Execute one round with the chosen action
        final java.util.List<CombatEvent> events =
                combat.executeHeroAction(theAction);

        // Send each event to the view for display
        for (final CombatEvent event : events) {
            myView.displayCombatEvent(event);
        }

        // Check if combat ended after this round
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
     * Checks whether the game has ended after a room entry.
     * Win condition: Hero has all 4 pillars AND is at the exit.
     * Lose condition: Hero HP is 0 or below.
     *
     * @return true if the game has ended, false if still in progress
     */
    public boolean checkWinLose() {
        final Hero hero = myModel.getHero();
        final Room currentRoom =
                myModel.getDungeon().getCurrentRoom();

        // Check lose condition first (hero could die from pit)
        if (!hero.isAlive()) {
            myView.displayMessage(
                    "You have been defeated. Game over.");
            myView.displayDungeon(myModel.getDungeon());
            myModel.setGameOver(true);
            myModel.setPlayerWon(false);
            myRunning = false;
            return true;
        }

        // Check win condition: all 4 pillars AND at exit
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
     * When active, the entire dungeon is displayed after each move.
     * This is a hidden feature for testing per the project spec.
     */
    public void toggleCheatMode() {
        myCheatMode = !myCheatMode;
        myView.displayMessage(
                "Cheat mode " + (myCheatMode ? "ON" : "OFF"));

        if (myCheatMode && myModel != null) {
            myView.displayDungeon(myModel.getDungeon());
        }
    }

    /**
     * Returns whether cheat mode is currently active.
     *
     * @return true if cheat mode is on
     */
    public boolean isCheatMode() {
        return myCheatMode;
    }

    /**
     * Returns whether the game loop is currently running.
     *
     * @return true if a game is in progress
     */
    public boolean isRunning() {
        return myRunning;
    }

    /**
     * Returns the current game model.
     * Used by tests to inspect state after controller actions.
     *
     * @return the current GameModel, or null if no game started
     */
    public GameModel getModel() {
        return myModel;
    }
}
