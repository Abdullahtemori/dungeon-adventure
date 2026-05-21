package edu.uw.tcss.dungeoneer.view;

import edu.uw.tcss.dungeoneer.controller.GameController;
import edu.uw.tcss.dungeoneer.model.CombatEvent;
import edu.uw.tcss.dungeoneer.model.Hero;
import edu.uw.tcss.dungeoneer.model.HeroAction;
import edu.uw.tcss.dungeoneer.model.Monster;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * CombatPanel is the Swing component that displays the active combat
 * screen between the hero and a monster.
 *
 * Layout (top to bottom):
 *   ┌─────────────────────────────────┐
 *   │  Hero pane  │  Monster pane     │  ← combatants row
 *   ├─────────────────────────────────┤
 *   │        Action Buttons           │  ← four action buttons
 *   ├─────────────────────────────────┤
 *   │        Combat Log (scroll)      │  ← scrollable event log
 *   └─────────────────────────────────┘
 *
 * Usage:
 *   GameController already holds references to the model. Pass it into
 *   the constructor so the four action buttons can delegate to it:
 *
 *     CombatPanel panel = new CombatPanel(controller);
 *     panel.startCombat(hero, monster);   // call when combat begins
 *     // after each round the controller calls panel.refresh(hero, monster)
 *     // and panel.logEvents(roundEvents)
 *
 * @author Abdullah Temori
 * @version Iteration 4
 */
public class CombatPanel extends JPanel {

    // ── Layout constants ─────────────────────────────────────────────────

    /** Width of the entire panel in pixels. */
    private static final int PANEL_W = 760;

    /** Height of the entire panel in pixels. */
    private static final int PANEL_H = 540;

    /** Preferred height of the combatant row at the top. */
    private static final int COMBATANT_ROW_H = 180;

    /** Preferred height of the action button row. */
    private static final int BUTTON_ROW_H = 60;

    /** HP bar width in pixels. */
    private static final int HP_BAR_W = 200;

    /** HP bar height in pixels. */
    private static final int HP_BAR_H = 18;

    /** Padding inside each card. */
    private static final int CARD_PAD = 12;

    /** Maximum HP value used for the hero bar (safe default if hero has no max). */
    private static final int DEFAULT_MAX_HP = 200;

    // ── Colours ──────────────────────────────────────────────────────────

    private static final Color BG_DARK       = new Color(28,  28,  38);
    private static final Color BG_CARD       = new Color(40,  40,  55);
    private static final Color BG_LOG        = new Color(20,  20,  30);
    private static final Color ACCENT_HERO   = new Color(80, 160, 255);
    private static final Color ACCENT_ENEMY  = new Color(220, 70,  70);
    private static final Color HP_GREEN      = new Color(60, 200, 100);
    private static final Color HP_YELLOW     = new Color(240, 195, 50);
    private static final Color HP_RED        = new Color(220, 60,  60);
    private static final Color BTN_NORMAL    = new Color(55,  55,  75);
    private static final Color BTN_HOVER     = new Color(75,  75, 100);
    private static final Color BTN_DISABLED  = new Color(38,  38,  50);
    private static final Color TEXT_PRIMARY  = new Color(230, 230, 240);
    private static final Color TEXT_DIM      = new Color(130, 130, 150);
    private static final Color LOG_HERO      = new Color(100, 180, 255);
    private static final Color LOG_ENEMY     = new Color(240, 100, 100);
    private static final Color LOG_HEAL      = new Color(80,  210, 120);
    private static final Color LOG_SYSTEM    = new Color(200, 180, 100);

    // ── Fonts ────────────────────────────────────────────────────────────

    private static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  15);
    private static final Font FONT_STAT   = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font FONT_BTN    = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_LOG    = new Font("Monospaced", Font.PLAIN, 12);

    // ── Hero card widgets ─────────────────────────────────────────────────

    /** Label showing the hero's name. */
    private final JLabel myHeroNameLabel   = makeLabel("", FONT_TITLE, ACCENT_HERO);

    /** Label showing the hero's current / max HP as text. */
    private final JLabel myHeroHpLabel     = makeLabel("", FONT_STAT, TEXT_PRIMARY);

    /** Progress bar representing hero HP. */
    private final JProgressBar myHeroHpBar = makeHpBar();

    /** Label showing hero class stats (speed, block %). */
    private final JLabel myHeroStatsLabel  = makeLabel("", FONT_STAT, TEXT_DIM);

    /** Label showing hero inventory counts. */
    private final JLabel myHeroInvLabel    = makeLabel("", FONT_STAT, TEXT_DIM);

    // ── Monster card widgets ──────────────────────────────────────────────

    /** Label showing the monster's name. */
    private final JLabel myMonNameLabel    = makeLabel("", FONT_TITLE, ACCENT_ENEMY);

    /** Label showing the monster's current HP as text. */
    private final JLabel myMonHpLabel      = makeLabel("", FONT_STAT, TEXT_PRIMARY);

    /** Progress bar representing monster HP. */
    private final JProgressBar myMonHpBar  = makeHpBar();

    /** Label showing monster stats (speed, heal %). */
    private final JLabel myMonStatsLabel   = makeLabel("", FONT_STAT, TEXT_DIM);

    // ── Action buttons ────────────────────────────────────────────────────

    private final JButton myAttackBtn  = makeActionButton("⚔  Attack");
    private final JButton mySkillBtn   = makeActionButton("✨  Special Skill");
    private final JButton myPotionBtn  = makeActionButton("🧪  Use Potion");
    private final JButton myBombBtn    = makeActionButton("💣  Use Bomb");

    // ── Combat log ────────────────────────────────────────────────────────

    /** Non-editable text pane that accumulates combat events. */
    private final JTextPane myLogPane  = new JTextPane();

    /** Scroll wrapper around the log pane. */
    private final JScrollPane myLogScroll;

    // ── State ─────────────────────────────────────────────────────────────

    /** Max HP of the current hero — captured when combat starts. */
    private int myHeroMaxHp = DEFAULT_MAX_HP;

    /** Max HP of the current monster — captured when combat starts. */
    private int myMonMaxHp  = DEFAULT_MAX_HP;

    // ── Constructor ───────────────────────────────────────────────────────

    /**
     * Constructs a CombatPanel wired to the given GameController.
     * The four action buttons delegate directly to the controller's
     * combat-action methods so this panel never touches model state
     * directly.
     *
     * @param theController the GameController for this game session
     */
    public CombatPanel(final GameController theController) {
        super(new BorderLayout(0, 0));
        setBackground(BG_DARK);
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));

        // ── Build log pane (needed first so buttons can write to it)
        myLogPane.setEditable(false);
        myLogPane.setBackground(BG_LOG);
        myLogPane.setForeground(TEXT_PRIMARY);
        myLogPane.setFont(FONT_LOG);
        myLogPane.setMargin(new Insets(8, 8, 8, 8));
        myLogScroll = new JScrollPane(myLogPane);
        myLogScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80), 1),
                " Combat Log ",
                TitledBorder.LEFT, TitledBorder.TOP,
                FONT_STAT, TEXT_DIM));
        myLogScroll.setBackground(BG_DARK);
        myLogScroll.getViewport().setBackground(BG_LOG);
        myLogScroll.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // ── Wire action listeners ────────────────────────────────────────
        myAttackBtn.addActionListener((ActionEvent e) -> {
            setButtonsEnabled(false);
            theController.handleCombatAction(HeroAction.ATTACK);
        });
        mySkillBtn.addActionListener((ActionEvent e) -> {
            setButtonsEnabled(false);
            theController.handleCombatAction(HeroAction.SPECIAL_SKILL);
        });
        myPotionBtn.addActionListener((ActionEvent e) -> {
            setButtonsEnabled(false);
            theController.handleCombatAction(HeroAction.USE_HEALING_POTION);
        });
        myBombBtn.addActionListener((ActionEvent e) -> {
            setButtonsEnabled(false);
            theController.handleCombatAction(HeroAction.USE_BOMB);
        });

        // ── Assemble layout ──────────────────────────────────────────────
        add(buildCombatantRow(), BorderLayout.NORTH);
        add(buildButtonRow(),    BorderLayout.CENTER);
        add(myLogScroll,         BorderLayout.SOUTH);

        // Fix log height so combatants + buttons keep their space
        myLogScroll.setPreferredSize(
                new Dimension(PANEL_W, PANEL_H - COMBATANT_ROW_H - BUTTON_ROW_H));
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Initialises the panel for a new combat encounter.
     * Call this when GameModel fires PROP_COMBAT with a non-null value.
     *
     * @param theHero    the hero entering combat
     * @param theMonster the monster the hero is fighting
     */
    public void startCombat(final Hero theHero, final Monster theMonster) {
        myHeroMaxHp = theHero.getHitPoints();   // hero enters at current HP
        myMonMaxHp  = theMonster.getHitPoints(); // monster enters at current HP

        myLogPane.setText("");
        appendLog("⚔  Combat begins: "
                + theHero.getName() + " vs " + theMonster.getName(), LOG_SYSTEM);

        refresh(theHero, theMonster);
        setButtonsEnabled(true);
    }

    /**
     * Refreshes all stat displays after a combat round.
     * Call this from the controller (or SwingView's propertyChange)
     * after every call to Combat.executeHeroAction().
     *
     * @param theHero    the hero (updated HP / inventory)
     * @param theMonster the monster (updated HP)
     */
    public void refresh(final Hero theHero, final Monster theMonster) {
        // ── Hero card ────────────────────────────────────────────────────
        myHeroNameLabel.setText(theHero.getName());

        final int heroHp = Math.max(0, theHero.getHitPoints());
        myHeroHpLabel.setText("HP: " + heroHp + " / " + myHeroMaxHp);
        updateBar(myHeroHpBar, heroHp, myHeroMaxHp);

        myHeroStatsLabel.setText(
                "Speed: " + theHero.getAttackSpeed()
                + "   Block: " + pct(theHero.getChanceToBlock()));

        myHeroInvLabel.setText(
                "🧪 " + theHero.getHealingPotions()
                + "   💣 " + theHero.getBombs()
                + "   👁 " + theHero.getVisionPotions());

        // ── Monster card ─────────────────────────────────────────────────
        myMonNameLabel.setText(theMonster.getName());

        final int monHp = Math.max(0, theMonster.getHitPoints());
        myMonHpLabel.setText("HP: " + monHp + " / " + myMonMaxHp);
        updateBar(myMonHpBar, monHp, myMonMaxHp);

        myMonStatsLabel.setText(
                "Speed: " + theMonster.getAttackSpeed()
                + "   Heal: " + pct(theMonster.getChanceToHeal()));

        // ── Inventory-dependent button state ─────────────────────────────
        myPotionBtn.setEnabled(theHero.getHealingPotions() > 0);
        myBombBtn.setEnabled(theHero.getBombs() > 0);
        styleInventoryButton(myPotionBtn);
        styleInventoryButton(myBombBtn);

        repaint();
    }

    /**
     * Appends a single CombatEvent to the scrollable log.
     * Translates the event type into a human-readable coloured line,
     * matching the output of ConsoleView.displayCombatEvent().
     *
     * @param theEvent the event to log
     */
    public void logEvent(final CombatEvent theEvent) {
        if (theEvent == null) {
            return;
        }
        final String actor  = theEvent.getActor();
        final String target = theEvent.getTarget();
        final int    amount = theEvent.getAmount();

        switch (theEvent.getType()) {
            case ATTACK_HIT:
                appendLog(actor + " attacks " + target
                        + " for " + amount + " damage!", LOG_HERO);
                break;
            case ATTACK_BLOCKED:
                appendLog(target + " blocked " + actor
                        + "'s attack!", TEXT_DIM);
                break;
            case SPECIAL_SUCCESS:
                appendLog(actor + " uses special skill on "
                        + target + " for " + amount + " damage!", LOG_HERO);
                break;
            case MONSTER_HEAL:
                appendLog(actor + " heals for " + amount + " HP.", LOG_HEAL);
                break;
            case POTION_USED:
                appendLog(actor + " drinks a healing potion — +"
                        + amount + " HP.", LOG_HEAL);
                break;
            case BOMB_USED:
                appendLog("💥 BOOM!  " + actor + " bombs " + target
                        + " for " + amount + " damage!", LOG_ENEMY);
                break;
            case ITEM_UNAVAILABLE:
                appendLog(actor + " has no item to use!", LOG_SYSTEM);
                break;
            case COMBAT_END:
                if (amount == 1) {
                    appendLog("🏆  VICTORY — " + actor
                            + " stands triumphant!", LOG_SYSTEM);
                } else {
                    appendLog("💀  DEFEAT — " + actor
                            + " has fallen...", LOG_ENEMY);
                }
                setButtonsEnabled(false);
                break;
            default:
                appendLog(actor + " performs an action on " + target + ".",
                        TEXT_PRIMARY);
                break;
        }
    }

    /**
     * Convenience method: logs an entire round's event list in order.
     *
     * @param theEvents the list returned by Combat.executeHeroAction()
     */
    public void logEvents(final java.util.List<CombatEvent> theEvents) {
        if (theEvents == null) {
            return;
        }
        for (final CombatEvent e : theEvents) {
            logEvent(e);
        }
    }

    /**
     * Re-enables the four action buttons so the player can take
     * the next turn. Called by the controller after it has finished
     * processing the previous action and refreshing the UI.
     */
    public void enableActions() {
        setButtonsEnabled(true);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Builds the top row containing the hero card and monster card
     * side by side.
     *
     * @return the assembled combatant row panel
     */
    private JPanel buildCombatantRow() {
        final JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setBackground(BG_DARK);
        row.setBorder(new EmptyBorder(10, 10, 6, 10));
        row.setPreferredSize(new Dimension(PANEL_W, COMBATANT_ROW_H));

        row.add(buildHeroCard());
        row.add(buildMonsterCard());
        return row;
    }

    /**
     * Builds the hero stat card.
     *
     * @return the hero card panel
     */
    private JPanel buildHeroCard() {
        final JPanel card = makeCard(ACCENT_HERO);

        card.add(myHeroNameLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(myHeroHpLabel);
        card.add(Box.createVerticalStrut(3));

        final JPanel barWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barWrap.setOpaque(false);
        myHeroHpBar.setPreferredSize(new Dimension(HP_BAR_W, HP_BAR_H));
        barWrap.add(myHeroHpBar);
        card.add(barWrap);

        card.add(Box.createVerticalStrut(6));
        card.add(myHeroStatsLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(myHeroInvLabel);
        return card;
    }

    /**
     * Builds the monster stat card.
     *
     * @return the monster card panel
     */
    private JPanel buildMonsterCard() {
        final JPanel card = makeCard(ACCENT_ENEMY);

        card.add(myMonNameLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(myMonHpLabel);
        card.add(Box.createVerticalStrut(3));

        final JPanel barWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barWrap.setOpaque(false);
        myMonHpBar.setPreferredSize(new Dimension(HP_BAR_W, HP_BAR_H));
        barWrap.add(myMonHpBar);
        card.add(barWrap);

        card.add(Box.createVerticalStrut(6));
        card.add(myMonStatsLabel);
        return card;
    }

    /**
     * Builds the four-button action row.
     *
     * @return the assembled button panel
     */
    private JPanel buildButtonRow() {
        final JPanel row = new JPanel(new GridLayout(1, 4, 8, 0));
        row.setBackground(BG_DARK);
        row.setBorder(new EmptyBorder(4, 10, 6, 10));
        row.setPreferredSize(new Dimension(PANEL_W, BUTTON_ROW_H));

        row.add(myAttackBtn);
        row.add(mySkillBtn);
        row.add(myPotionBtn);
        row.add(myBombBtn);
        return row;
    }

    /**
     * Creates a dark card panel with a coloured top border accent and
     * a BoxLayout so stat labels stack vertically.
     *
     * @param theAccent colour used for the left/top accent border
     * @return the styled card panel
     */
    private static JPanel makeCard(final Color theAccent) {
        final JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, theAccent),
                new EmptyBorder(CARD_PAD, CARD_PAD, CARD_PAD, CARD_PAD)));
        return card;
    }

    /**
     * Creates a styled JLabel with the given text, font, and foreground.
     *
     * @param theText  initial text
     * @param theFont  label font
     * @param theColor foreground colour
     * @return the configured label
     */
    private static JLabel makeLabel(final String theText,
                                    final Font theFont,
                                    final Color theColor) {
        final JLabel lbl = new JLabel(theText);
        lbl.setFont(theFont);
        lbl.setForeground(theColor);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Creates a styled HP progress bar (no text, rounded).
     *
     * @return the configured progress bar
     */
    private static JProgressBar makeHpBar() {
        final JProgressBar bar = new JProgressBar(0, DEFAULT_MAX_HP);
        bar.setValue(DEFAULT_MAX_HP);
        bar.setStringPainted(false);
        bar.setBackground(new Color(50, 50, 65));
        bar.setForeground(HP_GREEN);
        bar.setBorderPainted(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        return bar;
    }

    /**
     * Creates a dark themed action button with hover effect.
     *
     * @param theLabel button text
     * @return the configured button
     */
    private static JButton makeActionButton(final String theLabel) {
        final JButton btn = new JButton(theLabel);
        btn.setFont(FONT_BTN);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BTN_NORMAL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(final java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(BTN_HOVER);
                }
            }
            @Override
            public void mouseExited(final java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(BTN_NORMAL);
                }
            }
        });
        return btn;
    }

    /**
     * Updates a HP bar's max, value, and colour based on the percentage
     * of HP remaining. Green above 50 %, yellow 25–50 %, red below 25 %.
     *
     * @param theBar    the bar to update
     * @param theCurrent current HP
     * @param theMax    maximum HP
     */
    private static void updateBar(final JProgressBar theBar,
                                  final int theCurrent,
                                  final int theMax) {
        theBar.setMaximum(Math.max(1, theMax));
        theBar.setValue(Math.max(0, theCurrent));

        final double pct = (double) theCurrent / Math.max(1, theMax);
        if (pct > 0.50) {
            theBar.setForeground(HP_GREEN);
        } else if (pct > 0.25) {
            theBar.setForeground(HP_YELLOW);
        } else {
            theBar.setForeground(HP_RED);
        }
    }

    /**
     * Enables or disables all four action buttons at once.
     * Inventory buttons are re-evaluated against current stock before
     * being enabled so an empty slot is never accidentally active.
     *
     * @param theEnabled true to enable, false to disable
     */
    private void setButtonsEnabled(final boolean theEnabled) {
        myAttackBtn.setEnabled(theEnabled);
        mySkillBtn.setEnabled(theEnabled);
        // Potion / bomb stay disabled if they were already greyed due
        // to empty inventory (their enabled state is managed by refresh).
        if (!theEnabled) {
            myPotionBtn.setEnabled(false);
            myBombBtn.setEnabled(false);
        }
        // When re-enabling, inventory state is handled by the next refresh()
        // call, so we only flip them on if theEnabled == true AND the buttons
        // are already flagged enabled from the last refresh.
        styleInventoryButton(myPotionBtn);
        styleInventoryButton(myBombBtn);
    }

    /**
     * Adjusts an inventory button's visual appearance to match whether it
     * is currently enabled (item available) or disabled (inventory empty).
     *
     * @param theBtn the potion or bomb button
     */
    private static void styleInventoryButton(final JButton theBtn) {
        if (theBtn.isEnabled()) {
            theBtn.setBackground(BTN_NORMAL);
            theBtn.setForeground(TEXT_PRIMARY);
        } else {
            theBtn.setBackground(BTN_DISABLED);
            theBtn.setForeground(TEXT_DIM);
        }
    }

    /**
     * Appends a coloured line to the combat log and auto-scrolls to
     * the bottom. Uses an AttributeSet so each event type gets its own
     * colour without the log needing HTML rendering.
     *
     * @param theText  message to append
     * @param theColor foreground colour for this line
     */
    private void appendLog(final String theText, final Color theColor) {
        final javax.swing.text.StyledDocument doc = myLogPane.getStyledDocument();
        final javax.swing.text.SimpleAttributeSet attrs =
                new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setForeground(attrs, theColor);

        try {
            if (doc.getLength() > 0) {
                doc.insertString(doc.getLength(), "\n", attrs);
            }
            doc.insertString(doc.getLength(), theText, attrs);
        } catch (final javax.swing.text.BadLocationException ex) {
            // Should never happen — location is always end-of-document.
            ex.printStackTrace();
        }

        // Auto-scroll to bottom after each append
        SwingUtilities.invokeLater(() ->
                myLogPane.setCaretPosition(doc.getLength()));
    }

    /**
     * Formats a double chance (0.0–1.0) as a percentage string.
     *
     * @param theChance the probability value
     * @return e.g. "40%"
     */
    private static String pct(final double theChance) {
        return (int) Math.round(theChance * 100) + "%";
    }
}
