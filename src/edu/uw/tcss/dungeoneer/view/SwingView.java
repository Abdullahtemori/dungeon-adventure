package edu.uw.tcss.dungeoneer.view;

import edu.uw.tcss.dungeoneer.controller.*;
import edu.uw.tcss.dungeoneer.model.*;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * SwingView is the graphical view in the MVC pattern.
 * It implements GameView and displays all game information using Java
 * Swing Components inside a JFrame.
 *
 * SwingView also implements PropertyChangeListener (via GameView) so
 * GameModel can notify it automatically when state changes. When propertyChange()
 * is called, SwingView updates the relevant panels without the controller needing
 * to know anything about how the display is structured.
 *
 * Fixes applied in this version:
 *   1. attachListeners() is now called inside setController() so that
 *      navigation button clicks are properly wired to the controller.
 *   2. myLogArea.setFocusable(false) is set inside setController() so
 *      that keyboard controls (WASD / arrow keys) are not stolen by
 *      the log text area when the player interacts with the window.
 *   3. myMapArea is now initialized in the constructor to prevent a
 *      NullPointerException when displayDungeon() is called at game
 *      over or when cheat mode is toggled on.
 *
 * @author daniellabirungi
 * @author Abdullah Temori
 * @version Iteration 5 (bugfix)
 */
public class SwingView implements GameView {

    /** Minimum window width in pixels. */
    private static final int MIN_WIDTH = 800;

    /** Minimum window height in pixels. */
    private static final int MIN_HEIGHT = 600;

    /** Font size used for hero status labels. */
    private static final int STATUS_FONT_SIZE = 14;

    /** Font size used for the message log and map area. */
    private static final int LOG_FONT_SIZE = 13;

    /** Number of rows visible in the message log. */
    private static final int LOG_ROWS = 20;

    /** Number of columns in the message log. */
    private static final int LOG_COLS = 60;

    /** Background color for the main window. */
    private static final Color BG_COLOR = new Color(30, 30, 40);

    /** Text color for labels and text areas. */
    private static final Color TEXT_COLOR = new Color(220, 220, 200);

    /** Color for panel borders. */
    private static final Color PANEL_BORDER = new Color(80, 80, 100);

    /**
     * Dedicated scrollable text area used to display the full dungeon
     * map when cheat mode is active or the game ends.
     * Initialized in the constructor to prevent NullPointerException.
     */
    private JTextArea myMapArea;

    /** The main application window. */
    private final JFrame myFrame;

    /** Scrollable read-only area showing game events and messages. */
    private final JTextArea myLogArea;

    /** Label showing the hero's name and class. */
    private final JLabel myHeroNameLabel;

    /** Label showing the hero's current hit points. */
    private final JLabel myHpLabel;

    /** Label showing the number of healing potions in inventory. */
    private final JLabel myPotionLabel;

    /** Label showing the number of vision potions in inventory. */
    private final JLabel myVisionLabel;

    /** Label showing the number of bombs in inventory. */
    private final JLabel myBombLabel;

    /** Label showing which pillars of OO have been collected. */
    private final JLabel myPillarLabel;

    /** Label showing the current difficulty level. */
    private final JLabel myDifficultyLabel;

    /** Navigation button — move North. */
    private final JButton myNorthBtn;

    /** Navigation button — move South. */
    private final JButton mySouthBtn;

    /** Navigation button — move East. */
    private final JButton myEastBtn;

    /** Navigation button — move West. */
    private final JButton myWestBtn;

    /** Button to use a healing potion outside of combat. */
    private final JButton myUsePotionBtn;

    /** Button to use a vision potion outside of combat. */
    private final JButton myUseVisionBtn;

    /**
     * Reference to the GameController.
     * Set via setController() after both the view and controller are created.
     * All button and keyboard listeners use this reference to forward
     * player input to the controller.
     */
    private GameController myController;

    /**
     * Constructs the SwingView by building and wiring all Swing components.
     * The window is not made visible here; call show() after setController()
     * to ensure all listeners are attached before the player can interact.
     *
     * Fix: myMapArea is now initialized here to prevent a NullPointerException
     * in displayDungeon() which can be called at any time after game start.
     */
    public SwingView() {
        // Create main window
        myFrame = new JFrame("Dungeon Adventure");
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        myFrame.getContentPane().setBackground(BG_COLOR);

        // Create log area (read-only, scrollable)
        myLogArea = new JTextArea(LOG_ROWS, LOG_COLS);
        myLogArea.setEditable(false);
        myLogArea.setLineWrap(true);
        myLogArea.setWrapStyleWord(true);
        myLogArea.setBackground(new Color(20, 20, 30));
        myLogArea.setForeground(TEXT_COLOR);
        myLogArea.setFont(new Font("Monospaced", Font.PLAIN, LOG_FONT_SIZE));
        myLogArea.setCaretPosition(0);

        // FIX 1: Initialize myMapArea here so displayDungeon() never
        // throws a NullPointerException regardless of when it is called.
        myMapArea = new JTextArea();
        myMapArea.setEditable(false);
        myMapArea.setBackground(new Color(20, 20, 30));
        myMapArea.setForeground(TEXT_COLOR);
        myMapArea.setFont(new Font("Monospaced", Font.PLAIN, LOG_FONT_SIZE));

        // Create hero status labels
        myHeroNameLabel   = makeLabel("Hero: ---");
        myHpLabel         = makeLabel("HP: ---");
        myPotionLabel     = makeLabel("Potions: 0");
        myVisionLabel     = makeLabel("Vision: 0");
        myBombLabel       = makeLabel("Bombs: 0");
        myPillarLabel     = makeLabel("Pillars: [_][_][_][_]");
        myDifficultyLabel = makeLabel("Difficulty: ---");

        // Create navigation buttons (disabled by default until a game starts)
        myNorthBtn     = makeNavButton("North [W/↑]");
        mySouthBtn     = makeNavButton("South [S/↓]");
        myEastBtn      = makeNavButton("East [D/→]");
        myWestBtn      = makeNavButton("West [A/←]");
        myUsePotionBtn = makeNavButton("Use Potion [H]");
        myUseVisionBtn = makeNavButton("Use Vision [V]");

        // Assemble the layout
        myFrame.setJMenuBar(buildMenuBar());
        myFrame.setLayout(new BorderLayout(8, 8));
        myFrame.add(buildLogPanel(),    BorderLayout.CENTER);
        myFrame.add(buildStatusPanel(), BorderLayout.WEST);
        myFrame.add(buildNavPanel(),    BorderLayout.EAST);
        myFrame.add(buildTitleBar(),    BorderLayout.NORTH);

        myFrame.pack();
        myFrame.setLocationRelativeTo(null);
    }

    /**
     * Wires the controller to this view and attaches all input listeners.
     * Must be called after both the view and controller have been created,
     * and before show() is called so that input works from the first frame.
     *
     * Fix 1: attachListeners() is now called here so navigation button
     * clicks are forwarded to the controller. Previously this method
     * existed but was never invoked, making all buttons non-functional.
     *
     * Fix 2: myLogArea.setFocusable(false) is set here so the JTextArea
     * cannot steal keyboard focus from the JFrame. Without this fix,
     * clicking anywhere in the log area caused WASD and arrow key presses
     * to be swallowed by the text area and never reach the key listener
     * registered on the frame.
     *
     * @param theController the game controller to wire up; must not be null
     */
    public void setController(final GameController theController) {
        myController = theController;

        // FIX 2: Prevent the log area from stealing keyboard focus.
        // The key listener is on myFrame, so focus must stay on the frame.
        myLogArea.setFocusable(false);

        myFrame.setFocusable(true);
        myFrame.requestFocusInWindow();

        // Register WASD / arrow key navigation and hotkeys on the frame
        myFrame.addKeyListener(new java.awt.event.KeyAdapter() {
            /**
             * Dispatches key press events to the appropriate controller method.
             * WASD and arrow keys move the hero. H uses a healing potion.
             * V uses a vision potion.
             *
             * @param e the key event fired by the JFrame
             */
            @Override
            public void keyPressed(final java.awt.event.KeyEvent e) {
                switch (e.getKeyCode()) {
                    // WASD / Arrow key navigation
                    case java.awt.event.KeyEvent.VK_W:
                    case java.awt.event.KeyEvent.VK_UP:
                        myController.handleMove(Direction.NORTH);
                        break;
                    case java.awt.event.KeyEvent.VK_S:
                    case java.awt.event.KeyEvent.VK_DOWN:
                        myController.handleMove(Direction.SOUTH);
                        break;
                    case java.awt.event.KeyEvent.VK_A:
                    case java.awt.event.KeyEvent.VK_LEFT:
                        myController.handleMove(Direction.WEST);
                        break;
                    case java.awt.event.KeyEvent.VK_D:
                    case java.awt.event.KeyEvent.VK_RIGHT:
                        myController.handleMove(Direction.EAST);
                        break;

                    // Inventory hotkeys
                    case java.awt.event.KeyEvent.VK_H:
                        myController.handleUseHealingPotion();
                        break;
                    case java.awt.event.KeyEvent.VK_V:
                        myController.handleUseVisionPotion();
                        break;
                    default:
                        break;
                }
            }
        });

        // FIX 1: Wire navigation buttons to controller actions.
        // attachListeners() was defined but never called in the previous
        // version, so button clicks had no effect.
        attachListeners();
    }

    /**
     * Makes the main window visible.
     * Always call this after setController() to ensure all listeners
     * are registered before the player can interact with the UI.
     */
    public void show() {
        myFrame.setVisible(true);
    }

    /**
     * Appends the room's ASCII representation and a plain-text description
     * of its contents to the game log. Also updates the navigation buttons
     * to reflect which doors are open in the new room.
     *
     * @param theRoom the room the hero just entered; must not be null
     */
    @Override
    public void displayRoom(final Room theRoom) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(40)).append("\n");
        sb.append("  CURRENT ROOM:\n");
        sb.append(theRoom.toString()).append("\n");

        if (theRoom.hasEntrance()) {
            sb.append("  [Entrance]\n");
        }
        if (theRoom.hasExit()) {
            sb.append("  [EXIT — escape is near!]\n");
        }
        if (theRoom.hasPit()) {
            sb.append("  [Pit — ").append(theRoom.getPitDamage())
              .append(" damage!]\n");
        }
        if (theRoom.getHealingPotion() != null) {
            sb.append("  [Healing Potion]\n");
        }
        if (theRoom.getVisionPotion() != null) {
            sb.append("  [Vision Potion]\n");
        }
        if (theRoom.getBomb() != null) {
            sb.append("  [Bomb]\n");
        }
        if (theRoom.getPillar() != null) {
            sb.append("  [Pillar of ")
              .append(theRoom.getPillar().name()).append("]\n");
        }
        if (theRoom.hasMonster()) {
            sb.append("  [Monster: ")
              .append(theRoom.getMonster().getName())
              .append(" (").append(theRoom.getMonster().getHitPoints())
              .append(" HP)]\n");
        }

        appendLog(sb.toString());

        // Reflect which doors exist so the player knows valid move directions
        updateNavButtons(theRoom);
    }

    /**
     * Writes the full dungeon map to myMapArea.
     * Only visible when cheat mode is active or the game has ended.
     * The map is rendered in the dedicated map text area rather than
     * the main game log to keep the log readable during normal play.
     *
     * @param theDungeon the dungeon whose map should be rendered; must not be null
     */
    @Override
    public void displayDungeon(final Dungeon theDungeon) {
        SwingUtilities.invokeLater(() ->
                myMapArea.setText(theDungeon.toString()));
    }

    /**
     * Appends the contents of all surrounding rooms to the game log.
     * Called when the hero uses a Vision Potion. Up to 8 rooms may be
     * shown depending on the hero's position in the grid.
     *
     * @param theRooms the list of surrounding rooms to display; may be empty
     */
    @Override
    public void displayVision(final List<Room> theRooms) {
        appendLog("\n  VISION POTION — Surrounding rooms:");
        if (theRooms == null || theRooms.isEmpty()) {
            appendLog("  (No surrounding rooms visible.)");
        } else {
            for (final Room r : theRooms) {
                appendLog(r.toString());
            }
        }
    }

    /**
     * Appends a formatted combat status block to the game log showing
     * both the hero's and the monster's current HP. Called at the start
     * of each combat round before the player picks an action.
     *
     * @param theHero    the hero engaged in combat; must not be null
     * @param theMonster the monster engaged in combat; must not be null
     */
    @Override
    public void displayCombat(final Hero theHero, final Monster theMonster) {
        final String block = "\n"
                + "-".repeat(40) + "\n"
                + "  *** COMBAT ***\n"
                + "  " + theHero.getName()
                + "  HP: " + theHero.getHitPoints() + "\n"
                + "  " + theMonster.getName()
                + "  HP: " + theMonster.getHitPoints() + "\n"
                + "-".repeat(40);
        appendLog(block);
    }

    /**
     * Appends a single informational message to the game log.
     * Used for event feedback such as item pickup, pit damage,
     * blocked movement, and combat results.
     *
     * @param theMsg the message to display; must not be null
     */
    @Override
    public void displayMessage(final String theMsg) {
        appendLog("  > " + theMsg);
    }

    /**
     * Updates all hero status labels in the left-hand status panel.
     * Always dispatched on the Event Dispatch Thread so Swing components
     * are only modified from the correct thread.
     *
     * @param theHero the hero whose stats should be displayed; must not be null
     */
    @Override
    public void displayHeroStats(final Hero theHero) {
        SwingUtilities.invokeLater(() -> {
            myHeroNameLabel.setText("Hero: " + theHero.getName());
            myHpLabel.setText("HP: " + theHero.getHitPoints());
            myPotionLabel.setText("Potions: " + theHero.getHealingPotions());
            myVisionLabel.setText("Vision: " + theHero.getVisionPotions());
            myBombLabel.setText("Bombs: " + theHero.getBombs());
            myPillarLabel.setText("Pillars: " + formatPillars(theHero));

            // Enable item buttons only when the hero actually has the item
            myUsePotionBtn.setEnabled(theHero.getHealingPotions() > 0);
            myUseVisionBtn.setEnabled(theHero.getVisionPotions() > 0);
        });
    }

    /**
     * Shows a modal JOptionPane dialog asking the player to choose a
     * combat action for this round. Blocks execution until the player
     * makes a selection or closes the dialog (which defaults to ATTACK).
     *
     * @return the HeroAction corresponding to the player's selection;
     *         never null — defaults to ATTACK if dialog is dismissed
     */
    @Override
    public HeroAction promptHeroAction() {
        final String[] options = {
            "Attack", "Special Skill", "Healing Potion", "Bomb"
        };
        final int choice = JOptionPane.showOptionDialog(
                myFrame,
                "Choose your combat action:",
                "Combat",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        return switch (choice) {
            case 1  -> HeroAction.SPECIAL_SKILL;
            case 2  -> HeroAction.USE_HEALING_POTION;
            case 3  -> HeroAction.USE_BOMB;
            default -> HeroAction.ATTACK;
        };
    }

    /**
     * Converts a CombatEvent into a human-readable message and appends
     * it to the game log. Uses the event's built-in toString() method
     * for formatting consistency across all event types.
     *
     * @param theEvent the combat event to display; silently ignored if null
     */
    @Override
    public void displayCombatEvent(final CombatEvent theEvent) {
        if (theEvent != null) {
            appendLog("  > " + theEvent.toString());
        }
    }

    /**
     * Called automatically by GameModel whenever a tracked property changes.
     * Routes each property change to the appropriate view update so the
     * controller does not need to manually refresh the UI after every action.
     *
     * Handled properties:
     *   PROP_GAME_OVER  — appends a GAME OVER banner and disables nav buttons
     *   PROP_PLAYER_WON — appends a YOU WIN banner and shows a dialog
     *   PROP_HERO       — refreshes the hero status panel
     *   PROP_DUNGEON    — intentionally ignored (map only shown on demand)
     *   PROP_COMBAT     — logs a "combat ended" message when combat clears
     *
     * @param theEvt the property change event fired by GameModel; must not be null
     */
    @Override
    public void propertyChange(final PropertyChangeEvent theEvt) {
        final String prop = theEvt.getPropertyName();

        switch (prop) {

            case GameModel.PROP_GAME_OVER:
                final boolean over = (boolean) theEvt.getNewValue();
                if (over) {
                    appendLog("\n" + "=".repeat(40));
                    appendLog("  GAME OVER");
                    appendLog("=".repeat(40));
                    disableNavButtons();
                }
                break;

            case GameModel.PROP_PLAYER_WON:
                final boolean won = (boolean) theEvt.getNewValue();
                if (won) {
                    appendLog("\n" + "=".repeat(40));
                    appendLog("  *** YOU WIN! ***");
                    appendLog("  All 4 Pillars collected — you escaped!");
                    appendLog("=".repeat(40));
                    JOptionPane.showMessageDialog(myFrame,
                            "YOU WIN!\nAll 4 Pillars of OO collected!",
                            "Victory!", JOptionPane.INFORMATION_MESSAGE);
                }
                break;

            case GameModel.PROP_HERO:
                final Hero hero = (Hero) theEvt.getNewValue();
                if (hero != null) {
                    displayHeroStats(hero);
                }
                break;

            case GameModel.PROP_DUNGEON:
                // Dungeon replaced — map is only shown on demand via
                // displayDungeon(); no auto-display during normal play.
                break;

            case GameModel.PROP_COMBAT:
                final Object newCombat = theEvt.getNewValue();
                if (newCombat == null) {
                    appendLog("  Combat has ended.");
                }
                break;

            default:
                // Unknown property — ignore silently
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Private helper methods
    // -------------------------------------------------------------------------

    /**
     * Builds and returns the menu bar with File and Help menus.
     *
     * File menu items: New Game, Save Game, Load Game, Exit.
     * Help menu items: Instructions, About, Mute Audio, Cheat Mode.
     *
     * @return the fully assembled JMenuBar
     */
    private JMenuBar buildMenuBar() {
        final JMenuBar bar = new JMenuBar();
        bar.setBackground(new Color(45, 45, 55));

        // --- File menu ---
        final JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(TEXT_COLOR);

        final JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> promptNewGame());

        final JMenuItem saveItem = new JMenuItem("Save Game");
        saveItem.addActionListener(e -> {
            if (myController != null) {
                myController.saveGame(SaveLoadManager.DEFAULT_SAVE_PATH);
            }
        });

        final JMenuItem loadItem = new JMenuItem("Load Game");
        loadItem.addActionListener(e -> {
            if (myController != null) {
                myController.loadGame(SaveLoadManager.DEFAULT_SAVE_PATH);
            }
        });

        final JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newGameItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // --- Help menu ---
        final JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(TEXT_COLOR);

        final JMenuItem instructItem = new JMenuItem("Instructions");
        instructItem.addActionListener(e -> showInstructions());

        final JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(myFrame,
                "Dungeon Adventure\nTCSS 360 — Spring 2026\n"
                        + "Team: Abdullah Temori, Daniella Birungi, Tarik Atasoy",
                "About", JOptionPane.INFORMATION_MESSAGE));

        final JCheckBoxMenuItem audioItem =
                new JCheckBoxMenuItem("Mute Audio");
        audioItem.setSelected(false);
        audioItem.addActionListener(e -> {
            AudioManager.getInstance().toggleMute();
            audioItem.setSelected(AudioManager.getInstance().isMuted());
        });

        final JCheckBoxMenuItem cheatItem =
                new JCheckBoxMenuItem("Cheat Mode ON");
        cheatItem.setSelected(false);
        cheatItem.addActionListener(e -> {
            if (myController != null) {
                myController.toggleCheatMode();
                cheatItem.setSelected(myController.isCheatMode());
            }
        });

        helpMenu.add(instructItem);
        helpMenu.add(aboutItem);
        helpMenu.addSeparator();
        helpMenu.add(audioItem);
        helpMenu.addSeparator();
        helpMenu.add(cheatItem);

        bar.add(fileMenu);
        bar.add(helpMenu);
        return bar;
    }

    /**
     * Builds and returns the gold title bar panel shown at the top of the window.
     *
     * @return the configured title JPanel
     */
    private JPanel buildTitleBar() {
        final JPanel panel = new JPanel();
        panel.setBackground(new Color(20, 70, 20));
        final JLabel title = new JLabel("⚔  Dungeon Adventure  ⚔");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(255, 215, 0));
        panel.add(title);
        return panel;
    }

    /**
     * Builds and returns the scrollable game log panel placed in the center
     * of the window. All gameplay messages are appended here in real time.
     *
     * @return the log panel wrapped in a titled border
     */
    private JPanel buildLogPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PANEL_BORDER),
                "Game Log",
                0, 0,
                new Font("SansSerif", Font.BOLD, 12),
                TEXT_COLOR));

        final JScrollPane scroll = new JScrollPane(myLogArea);
        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds and returns the hero status panel placed on the left side
     * of the window. Contains all stat labels stacked vertically.
     *
     * @return the status panel with a preferred width of 220 px
     */
    private JPanel buildStatusPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(35, 35, 50));
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PANEL_BORDER),
                "Hero Status",
                0, 0,
                new Font("SansSerif", Font.BOLD, 12),
                TEXT_COLOR));

        panel.add(Box.createVerticalStrut(10));
        panel.add(myHeroNameLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(myHpLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(myPotionLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(myVisionLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(myBombLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(myPillarLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(myDifficultyLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Builds and returns the navigation panel placed on the right side
     * of the window. Buttons are arranged in a compass layout with
     * item-use buttons below.
     *
     * @return the navigation panel with a preferred width of 160 px
     */
    private JPanel buildNavPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(35, 35, 50));
        panel.setPreferredSize(new Dimension(160, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PANEL_BORDER),
                "Navigation",
                0, 0,
                new Font("SansSerif", Font.BOLD, 12),
                TEXT_COLOR));

        // 3x3 compass grid: North top-center, West/East middle, South bottom
        final JPanel compass = new JPanel(new GridLayout(3, 3, 2, 2));
        compass.setBackground(new Color(35, 35, 50));
        compass.add(new JLabel());   // top-left  (empty)
        compass.add(myNorthBtn);
        compass.add(new JLabel());   // top-right (empty)
        compass.add(myWestBtn);
        compass.add(new JLabel());   // center    (empty)
        compass.add(myEastBtn);
        compass.add(new JLabel());   // bot-left  (empty)
        compass.add(mySouthBtn);
        compass.add(new JLabel());   // bot-right (empty)

        panel.add(Box.createVerticalStrut(10));
        panel.add(compass);
        panel.add(Box.createVerticalStrut(12));
        panel.add(myUsePotionBtn);
        panel.add(Box.createVerticalStrut(6));
        panel.add(myUseVisionBtn);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Attaches ActionListeners to all navigation and item buttons.
     * Each button forwards its event to the appropriate GameController method.
     * Movement buttons also trigger a move sound effect via AudioManager.
     *
     * Fix: This method is now called from setController() so that button
     * clicks are actually wired up. In the previous version this method
     * existed but was never called, making all buttons non-functional.
     */
    private void attachListeners() {
        myNorthBtn.addActionListener(e -> {
            AudioManager.getInstance().playSFX(AudioManager.SFX_MOVE);
            myController.handleMove(Direction.NORTH);
        });
        mySouthBtn.addActionListener(e -> {
            AudioManager.getInstance().playSFX(AudioManager.SFX_MOVE);
            myController.handleMove(Direction.SOUTH);
        });
        myEastBtn.addActionListener(e -> {
            AudioManager.getInstance().playSFX(AudioManager.SFX_MOVE);
            myController.handleMove(Direction.EAST);
        });
        myWestBtn.addActionListener(e -> {
            AudioManager.getInstance().playSFX(AudioManager.SFX_MOVE);
            myController.handleMove(Direction.WEST);
        });
        myUsePotionBtn.addActionListener(e ->
                myController.handleUseHealingPotion());
        myUseVisionBtn.addActionListener(e ->
                myController.handleUseVisionPotion());
    }

    /**
     * Enables or disables each navigation button based on whether a door
     * exists in that direction in the given room. Walls show as disabled
     * buttons so the player cannot attempt an impossible move.
     * Dispatched on the Event Dispatch Thread for Swing thread safety.
     *
     * @param theRoom the room the hero is currently occupying; must not be null
     */
    private void updateNavButtons(final Room theRoom) {
        SwingUtilities.invokeLater(() -> {
            myNorthBtn.setEnabled(theRoom.hasDoor(Direction.NORTH));
            mySouthBtn.setEnabled(theRoom.hasDoor(Direction.SOUTH));
            myEastBtn.setEnabled(theRoom.hasDoor(Direction.EAST));
            myWestBtn.setEnabled(theRoom.hasDoor(Direction.WEST));
        });
    }

    /**
     * Disables all navigation and item-use buttons.
     * Called when the game ends (win or loss) to prevent the player from
     * continuing to move or use items after the outcome is decided.
     * Dispatched on the Event Dispatch Thread for Swing thread safety.
     */
    private void disableNavButtons() {
        SwingUtilities.invokeLater(() -> {
            myNorthBtn.setEnabled(false);
            mySouthBtn.setEnabled(false);
            myEastBtn.setEnabled(false);
            myWestBtn.setEnabled(false);
            myUsePotionBtn.setEnabled(false);
            myUseVisionBtn.setEnabled(false);
        });
    }

    /**
     * Shows a sequence of JOptionPane dialogs to collect the hero's name,
     * class, and difficulty from the player, then starts a new game via
     * the controller. Clears the game log before starting so previous
     * session output does not carry over.
     *
     * Returns early without starting a game if the player cancels any dialog.
     */
    private void promptNewGame() {
        // Step 1: Hero name
        final String name = JOptionPane.showInputDialog(myFrame,
                "Enter your hero's name:", "New Game",
                JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        // Step 2: Hero class
        final String[] classes = {"Warrior", "Priestess", "Thief"};
        final String heroType = (String) JOptionPane.showInputDialog(myFrame,
                "Choose your hero class:\n"
                        + "  Warrior   — Crushing Blow (high damage)\n"
                        + "  Priestess — Heal (restore HP in combat)\n"
                        + "  Thief     — Surprise Attack (bonus turns)",
                "Hero Class", JOptionPane.QUESTION_MESSAGE,
                null, classes, classes[0]);
        if (heroType == null) {
            return;
        }

        // Step 3: Difficulty
        final String[] diffs = {"EASY", "MEDIUM", "HARD"};
        final String diffStr = (String) JOptionPane.showInputDialog(myFrame,
                "Choose difficulty:\n"
                        + "  EASY   — 5x5 dungeon\n"
                        + "  MEDIUM — 7x7 dungeon\n"
                        + "  HARD   — 10x10 dungeon, tougher monsters",
                "Difficulty", JOptionPane.QUESTION_MESSAGE,
                null, diffs, diffs[1]);
        if (diffStr == null) {
            return;
        }

        // Clear previous session log and start the new game
        myLogArea.setText("");
        appendLog("Starting new game...");
        myDifficultyLabel.setText("Difficulty: " + diffStr);

        final Difficulty difficulty = Difficulty.valueOf(diffStr);
        myController.startNewGame(name.trim(), heroType, difficulty);

        AudioManager.getInstance().playMusic(AudioManager.MUSIC_DUNGEON);
    }

    /**
     * Shows a JOptionPane dialog with the full game instructions.
     * Covers navigation, items, combat, menus, and cheat mode.
     */
    private void showInstructions() {
        final String text =
                "HOW TO PLAY DUNGEON ADVENTURE\n\n"
                        + "GOAL: Collect all 4 Pillars of OO and reach the Exit.\n\n"
                        + "PILLARS: A=Abstraction  E=Encapsulation\n"
                        + "         I=Inheritance  P=Polymorphism\n\n"
                        + "KEYBOARD SHORTCUTS:\n"
                        + "  Move North: W or Up Arrow\n"
                        + "  Move South: S or Down Arrow\n"
                        + "  Move West:  A or Left Arrow\n"
                        + "  Move East:  D or Right Arrow\n"
                        + "  Use Healing Potion: H\n"
                        + "  Use Vision Potion:  V\n\n"
                        + "NAVIGATION:\n"
                        + "  Click North [W], South [S], West [A], East [D].\n"
                        + "  Disabled buttons mean a wall blocks that direction.\n\n"
                        + "ITEMS (picked up automatically on room entry):\n"
                        + "  H = Healing Potion  - press H or click Use Potion [H]\n"
                        + "  V = Vision Potion   - press V or click Use Vision [V]\n"
                        + "  B = Bomb            — use during combat for heavy damage\n\n"
                        + "COMBAT:\n"
                        + "  Entering a room with a monster starts combat.\n"
                        + "  Each round choose: Attack, Special Skill, Potion, or Bomb.\n\n"
                        + "MENUS:\n"
                        + "  File > New Game / Save Game / Load Game / Exit\n"
                        + "  Help > Instructions / About / Mute Audio / Cheat Mode\n\n"
                        + "CHEAT MODE: Reveals the entire dungeon map (Help menu).";
        JOptionPane.showMessageDialog(myFrame, text,
                "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Creates and returns a styled JLabel for use in the status panel.
     * All status labels share the same font, color, and alignment so
     * this factory method avoids duplicating those settings.
     *
     * @param theText the initial text to display on the label
     * @return a configured JLabel ready to add to the status panel
     */
    private JLabel makeLabel(final String theText) {
        final JLabel label = new JLabel("  " + theText);
        label.setFont(new Font("SansSerif", Font.PLAIN, STATUS_FONT_SIZE));
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Creates and returns a styled JButton for use in the navigation panel.
     * All nav buttons share the same appearance and start disabled; they
     * are enabled by updateNavButtons() once a game is in progress.
     *
     * @param theText the label text to display on the button
     * @return a configured JButton ready to add to the nav panel
     */
    private JButton makeNavButton(final String theText) {
        final JButton btn = new JButton(theText);
        btn.setBackground(new Color(60, 60, 90));
        btn.setForeground(TEXT_COLOR);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setEnabled(false);
        btn.setMaximumSize(new Dimension(150, 35));
        btn.setAlignmentX(JButton.CENTER_ALIGNMENT);
        return btn;
    }

    /**
     * Appends a line of text to the game log and scrolls to the bottom
     * so the latest message is always visible. Always dispatched on the
     * Event Dispatch Thread for Swing thread safety.
     *
     * @param theMsg the text to append; a newline is added automatically
     */
    private void appendLog(final String theMsg) {
        SwingUtilities.invokeLater(() -> {
            myLogArea.append(theMsg + "\n");
            myLogArea.setCaretPosition(
                    myLogArea.getDocument().getLength());
        });
    }

    /**
     * Formats the hero's collected pillars as a compact bracket string.
     * Collected pillars show their letter (A, E, I, P); missing ones
     * show an underscore. Example: "[A][_][I][_]".
     *
     * @param theHero the hero to read pillar data from; must not be null
     * @return the formatted pillar string
     */
    private String formatPillars(final Hero theHero) {
        final StringBuilder sb = new StringBuilder();
        for (final Pillar p : Pillar.values()) {
            sb.append(theHero.getPillarsFound().contains(p)
                    ? "[" + p.getDisplayCharacter() + "]"
                    : "[_]");
        }
        return sb.toString();
    }
}
