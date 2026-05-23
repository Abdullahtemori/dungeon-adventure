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
 * SwingView also implements PropertyChangeListener(via GameView) so
 * GameModel can notify it automatically when state changes. When propertyChange()
 * is called, SwingView updates the relevant panels without the controller needing
 * to know anything about how the display is structured.
 *
 * @author daniellabirungi
 * @version Iteration 4
 */
public class SwingView implements GameView {

    /** Minimum window width in pixels. */
    private static final int MIN_WIDTH = 900;

    /** Minimum window height in pixels. */
    private static final int MIN_HEIGHT = 650;

    /** Font size used for hero status labels. */
    private static final int STATUS_FONT_SIZE = 14;

    /** Font size used for the message log. */
    private static final int LOG_FONT_SIZE = 13;

    /** Number of rows visible in the message log. */
    private static final int LOG_ROWS = 20;

    /** Number of columns in the message log. */
    private static final int LOG_COLS = 60;

    /** Background color for the main window. */
    private static final Color BG_COLOR = new Color(30, 30, 40);

    /** Text color for labels. */
    private static final Color TEXT_COLOR = new Color(220, 220, 200);

    /** Color for the nav button panel border. */
    private static final Color PANEL_BORDER = new Color(80, 80, 100);

    /** The main application window. */
    private final JFrame myFrame;

    /** Scrollable area showing game events and messages. */
    private final JTextArea myLogArea;

    /** Label showing hero name and class. */
    private final JLabel myHeroNameLabel;

    /** Label showing hero HP. */
    private final JLabel myHpLabel;

    /** Label showing healing potion count. */
    private final JLabel myPotionLabel;

    /** Label showing vision potion count. */
    private final JLabel myVisionLabel;

    /** Label showing bomb count. */
    private final JLabel myBombLabel;

    /** Label showing pillars collected. */
    private final JLabel myPillarLabel;

    /** Label showing current difficulty. */
    private final JLabel myDifficultyLabel;

    /** Navigation button — move North. */
    private final JButton myNorthBtn;

    /** Navigation button — move South. */
    private final JButton mySouthBtn;

    /** Navigation button — move East. */
    private final JButton myEastBtn;

    /** Navigation button — move West. */
    private final JButton myWestBtn;

    /** Button to use a healing potion outside combat. */
    private final JButton myUsePotionBtn;

    /** Button to use a vision potion outside combat. */
    private final JButton myUseVisionBtn;


    /**
     * Reference to the controller.
     * Set via setController() after both view and controller are created.
     */
    private GameController myController;

    /**
     * Constructs the SwingView by building all Swing components.
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

        // Create hero status labels
        myHeroNameLabel   = makeLabel("Hero: ---");
        myHpLabel         = makeLabel("HP: ---");
        myPotionLabel     = makeLabel("Potions: 0");
        myVisionLabel     = makeLabel("Vision: 0");
        myBombLabel       = makeLabel("Bombs: 0");
        myPillarLabel     = makeLabel("Pillars: [_][_][_][_]");
        myDifficultyLabel = makeLabel("Difficulty: ---");

        // Create navigation buttons
        myNorthBtn    = makeNavButton("North ▲");
        mySouthBtn    = makeNavButton("South ▼");
        myEastBtn     = makeNavButton("East ►");
        myWestBtn     = makeNavButton("◄ West");
        myUsePotionBtn = makeNavButton("Use Potion (H)");
        myUseVisionBtn = makeNavButton("Use Vision (V)");

        //layout Assembly
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
     * Wires the controller to this view and attaches all button listeners.
     * Call this after both the view and controller have been created.
     *
     * @param theController the game controller to wire up
     */
    public void setController(final GameController theController) {
        myController = theController;
        attachListeners();
    }

    /**
     * Makes the window visible.
     * Call after setController() to ensure listeners are attached first.
     */
    public void show() {
        myFrame.setVisible(true);
    }


    /**
     * Appends the room's text representation and its contents
     * to the message log.
     *
     * @param theRoom the room to display
     */
    @Override
    public void displayRoom(final Room theRoom) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(40)).append("\n");
        sb.append("  CURRENT ROOM:\n");
        sb.append(theRoom.toString()).append("\n");

        if (theRoom.hasEntrance()) sb.append("  [Entrance]\n");
        if (theRoom.hasExit())     sb.append("  [EXIT — escape is near!]\n");
        if (theRoom.hasPit())
            sb.append("  [Pit — ").append(theRoom.getPitDamage()).append(" damage!]\n");
        if (theRoom.getHealingPotion() != null)
            sb.append("  [Healing Potion]\n");
        if (theRoom.getVisionPotion() != null)
            sb.append("  [Vision Potion]\n");
        if (theRoom.getBomb() != null)
            sb.append("  [Bomb]\n");
        if (theRoom.getPillar() != null)
            sb.append("  [Pillar of ").append(theRoom.getPillar().name()).append("]\n");
        if (theRoom.hasMonster())
            sb.append("  [Monster: ").append(theRoom.getMonster().getName())
                    .append(" (").append(theRoom.getMonster().getHitPoints()).append(" HP)]\n");

        appendLog(sb.toString());

        // Update navigation button availability based on doors
        updateNavButtons(theRoom);
    }

    /**
     * Appends the full dungeon map text to the message log.
     *
     * @param theDungeon the dungeon to display
     */
    @Override
    public void displayDungeon(final Dungeon theDungeon) {
        appendLog("\n  DUNGEON MAP:\n" + theDungeon.toString());
    }

    /**
     * Appends surrounding room contents to the log.
     * Called when the hero uses a Vision Potion.
     *
     * @param theRooms the surrounding rooms to show
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
     * Appends a formatted combat status block to the log.
     * Called at the start of each combat round.
     *
     * @param theHero    the hero in combat
     * @param theMonster the monster in combat
     */
    @Override
    public void displayCombat(final Hero theHero, final Monster theMonster) {
        final String block = "\n"
                + "-".repeat(40) + "\n"
                + "  *** COMBAT ***\n"
                + "  " + theHero.getName() + "  HP: " + theHero.getHitPoints() + "\n"
                + "  " + theMonster.getName() + "  HP: " + theMonster.getHitPoints() + "\n"
                + "-".repeat(40);
        appendLog(block);
    }

    /**
     * Appends a single message line to the log.
     *
     * @param theMsg the message to display
     */
    @Override
    public void displayMessage(final String theMsg) {
        appendLog("  > " + theMsg);
    }

    /**
     * Updates all hero status labels in the left panel.
     * Called after any event that changes hero state.
     *
     * @param theHero the hero whose stats to display
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

            // Enable/disable item buttons based on inventory
            myUsePotionBtn.setEnabled(theHero.getHealingPotions() > 0);
            myUseVisionBtn.setEnabled(theHero.getVisionPotions() > 0);
        });
    }

    /**
     * Shows a modal JOptionPane dialog asking the player to choose
     * a combat action. Blocks until a valid selection is made.
     *
     * @return the HeroAction the player selected
     */
    @Override
    public HeroAction promptHeroAction() {
        final String[] options = {"Attack", "Special Skill",
                "Healing Potion", "Bomb"};
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

        // Map button index to HeroAction
        return switch (choice) {
            case 1 -> HeroAction.SPECIAL_SKILL;
            case 2 -> HeroAction.USE_HEALING_POTION;
            case 3 -> HeroAction.USE_BOMB;
            default -> HeroAction.ATTACK;
        };
    }

    /**
     * Converts a CombatEvent into a readable log message and appends it.
     * The Swing view uses the event's built-in toString() for simplicity.
     *
     * @param theEvent the event to display
     */
    @Override
    public void displayCombatEvent(final CombatEvent theEvent) {
        if (theEvent != null) {
            appendLog("  > " + theEvent.toString());
        }
    }

    /**
     * Called automatically by GameModel whenever state changes.
     * Routes the event to the correct display update.
     *
     * @param theEvt the property change event from GameModel
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
                // Dungeon was replaced — no auto-display needed
                break;

            case GameModel.PROP_COMBAT:
                final Object newCombat = theEvt.getNewValue();
                if (newCombat == null) {
                    appendLog("  Combat has ended.");
                }
                break;

            default:
                break;
        }
    }

    /**
     * Builds the menu bar with File and Help menus.
     *
     * File menu: New Game, Save Game, Load Game, separator, Exit.
     * Help menu: Instructions, About, separator, Toggle Audio.
     *
     * @return the assembled JMenuBar
     */
    private JMenuBar buildMenuBar() {
        final JMenuBar bar = new JMenuBar();
        bar.setBackground(new Color(45, 45, 55));

        // File menu
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

        // Help menu
        final JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(TEXT_COLOR);

        final JMenuItem instructItem = new JMenuItem("Instructions");
        instructItem.addActionListener(e -> showInstructions());

        final JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(myFrame,
                "Dungeon Adventure\nTCSS 360 — Spring 2026\n"
                        + "Team: Abdullah Temori, Daniella Birungi, Tarik Atasoy",
                "About", JOptionPane.INFORMATION_MESSAGE));

        final JCheckBoxMenuItem audioItem = new JCheckBoxMenuItem("Mute Audio");
        // Start unchecked (audio is on by default)
        audioItem.setSelected(false);
        audioItem.addActionListener(e -> {
            AudioManager.getInstance().toggleMute();
            // Checkbox state automatically matches the muted state
            audioItem.setSelected(AudioManager.getInstance().isMuted());
        });

        final JCheckBoxMenuItem cheatItem = new JCheckBoxMenuItem("Cheat Mode ON");
        // Start unchecked (cheat mode is off by default)
        cheatItem.setSelected(false);
        cheatItem.addActionListener(e -> {
            if (myController != null) {
                myController.toggleCheatMode();
                // Keep checkbox in sync with controller state
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
     * Builds the title bar panel at the top of the window.
     *
     * @return the title JPanel
     */
    private JPanel buildTitleBar() {
        final JPanel panel = new JPanel();
        panel.setBackground(new Color(20, 70, 20));
        final JLabel title = new JLabel("⚔  Dungeon Adventure  ⚔");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(255, 215, 0)); // gold
        panel.add(title);
        return panel;
    }

    /**
     * Builds the scrollable message log panel in the center.
     *
     * @return the log JScrollPane wrapped in a panel
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
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the hero status panel on the left side.
     * Contains all hero stat labels stacked vertically.
     *
     * @return the status JPanel
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
     * Builds the navigation button panel on the right side.
     * Arranged in a compass layout with item buttons below.
     *
     * @return the nav JPanel
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

        // Compass layout: N in top row, W/E in middle, S below
        final JPanel compass = new JPanel(new GridLayout(3, 3, 2, 2));
        compass.setBackground(new Color(35, 35, 50));
        compass.add(new JLabel()); // top-left
        compass.add(myNorthBtn);
        compass.add(new JLabel()); // top-right
        compass.add(myWestBtn);
        compass.add(new JLabel()); // center
        compass.add(myEastBtn);
        compass.add(new JLabel()); // bottom-left
        compass.add(mySouthBtn);
        compass.add(new JLabel()); // bottom-right

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
     * Attaches action listeners to all navigation and item buttons.
     * Called once after the controller is set via setController().
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
     * Updates nav buttons based on which doors exist in the given room.
     * Buttons for walls are disabled so the player cannot walk into them.
     *
     * @param theRoom the room currently occupied by the hero
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
     * Disables all navigation buttons.
     * Called when the game ends so the player cannot move after winning or losing.
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
     * Shows a new game setup dialog asking for hero name, class,
     * and difficulty, then starts a new game via the controller.
     */
    private void promptNewGame() {
        // Step 1: Hero name
        final String name = JOptionPane.showInputDialog(myFrame,
                "Enter your hero's name:", "New Game",
                JOptionPane.QUESTION_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;

        // Step 2: Hero class
        final String[] classes = {"Warrior", "Priestess", "Thief"};
        final String heroType = (String) JOptionPane.showInputDialog(myFrame,
                "Choose your hero class:\n"
                        + "  Warrior — Crushing Blow (high damage)\n"
                        + "  Priestess — Heal (restore HP in combat)\n"
                        + "  Thief — Surprise Attack (bonus turns)",
                "Hero Class", JOptionPane.QUESTION_MESSAGE,
                null, classes, classes[0]);
        if (heroType == null) return;

        // Step 3: Difficulty
        final String[] diffs = {"EASY", "MEDIUM", "HARD"};
        final String diffStr = (String) JOptionPane.showInputDialog(myFrame,
                "Choose difficulty:\n"
                        + "  EASY   — 5x5 dungeon, stronger hero\n"
                        + "  MEDIUM — 7x7 dungeon, balanced\n"
                        + "  HARD   — 10x10 dungeon, tougher monsters",
                "Difficulty", JOptionPane.QUESTION_MESSAGE,
                null, diffs, diffs[1]);
        if (diffStr == null) return;

        // Clear log and start the game
        myLogArea.setText("");
        appendLog("Starting new game...");
        myDifficultyLabel.setText("Difficulty: " + diffStr);

        final Difficulty difficulty = Difficulty.valueOf(diffStr);
        myController.startNewGame(name.trim(), heroType, difficulty);

        // Start dungeon background music
        AudioManager.getInstance().playMusic(AudioManager.MUSIC_DUNGEON);
    }

    /**
     * Shows a dialog explaining how to play the game.
     */
    private void showInstructions() {
        final String text =
                "HOW TO PLAY DUNGEON ADVENTURE\n\n"
                        + "GOAL: Collect all 4 Pillars of OO and reach the Exit.\n\n"
                        + "PILLARS: A=Abstraction E=Encapsulation\n"
                        + "         I=Inheritance  P=Polymorphism\n\n"
                        + "NAVIGATION:\n"
                        + "  Use the North/South/East/West buttons to move.\n"
                        + "  Buttons are disabled when a wall blocks that direction.\n\n"
                        + "ITEMS (picked up automatically):\n"
                        + "  H = Healing Potion (restores 5-15 HP)\n"
                        + "  V = Vision Potion (reveals 8 surrounding rooms)\n"
                        + "  B = Bomb (deals 75-150 damage in combat)\n\n"
                        + "COMBAT:\n"
                        + "  Entering a room with a monster starts combat.\n"
                        + "  Choose Attack, Special Skill, Potion, or Bomb each round.\n\n"
                        + "MENU:\n"
                        + "  File > New Game / Save / Load\n"
                        + "  Help > Toggle Audio / Toggle Cheat Mode\n\n"
                        + "CHEAT MODE: Reveals the entire dungeon map.";
        JOptionPane.showMessageDialog(myFrame, text,
                "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Creates a styled JLabel for the status panel.
     *
     * @param theText the initial label text
     * @return the configured JLabel
     */
    private JLabel makeLabel(final String theText) {
        final JLabel label = new JLabel("  " + theText);
        label.setFont(new Font("SansSerif", Font.PLAIN, STATUS_FONT_SIZE));
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Creates a styled navigation JButton.
     *
     * @param theText the button label
     * @return the configured JButton (disabled by default)
     */
    private JButton makeNavButton(final String theText) {
        final JButton btn = new JButton(theText);
        btn.setBackground(new Color(60, 60, 90));
        btn.setForeground(TEXT_COLOR);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setEnabled(false); // enabled after a game starts
        btn.setMaximumSize(new Dimension(150, 35));
        btn.setAlignmentX(JButton.CENTER_ALIGNMENT);
        return btn;
    }

    /**
     * Appends a line to the message log and scrolls to the bottom.
     * Always runs on the Event Dispatch Thread for thread safety.
     *
     * @param theMsg the text line to append
     */
    private void appendLog(final String theMsg) {
        SwingUtilities.invokeLater(() -> {
            myLogArea.append(theMsg + "\n");
            // Auto-scroll to bottom so latest message is always visible
            myLogArea.setCaretPosition(myLogArea.getDocument().getLength());
        });
    }


    /**
     * Formats the hero's collected pillars as compact brackets.
     * Collected: [A], not collected: [_].
     *
     * @param theHero the hero to read from
     * @return formatted pillar string e.g. "[A][_][I][_]"
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