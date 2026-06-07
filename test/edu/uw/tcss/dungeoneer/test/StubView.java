package edu.uw.tcss.dungeoneer.test;

import edu.uw.tcss.dungeoneer.model.CombatEvent;
import edu.uw.tcss.dungeoneer.model.Dungeon;
import edu.uw.tcss.dungeoneer.model.Hero;
import edu.uw.tcss.dungeoneer.model.HeroAction;
import edu.uw.tcss.dungeoneer.model.Monster;
import edu.uw.tcss.dungeoneer.model.Room;
import edu.uw.tcss.dungeoneer.view.GameView;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * StubView is a minimal no-op implementation of GameView used exclusively
 * in unit and integration tests. It captures all messages for assertion and
 * provides a configurable action sequence for combat prompts so tests never
 * block waiting for user input.
 *
 * <p>Usage:</p>
 * <pre>
 *   StubView view = new StubView();
 *   GameController ctrl = new GameController(view);
 *   ctrl.startNewGame("Hero", "Warrior", Difficulty.EASY);
 *   assertTrue(view.hasMessage("cheat Mode reset"));
 * </pre>
 *
 * @author Daniella Birungi (test infrastructure)
 * @version Iteration 5
 */
public class StubView implements GameView {

    // ── Message capture ───────────────────────────────────────────────────

    /** All messages sent via displayMessage() since last clearMessages(). */
    private final List<String> myMessages = new ArrayList<>();

    /** All PropertyChangeEvents received since construction. */
    private final List<PropertyChangeEvent> myEvents = new ArrayList<>();

    // ── Combat stub action ────────────────────────────────────────────────

    /**
     * Action returned by promptHeroAction(). Defaults to ATTACK so
     * combat always resolves without requiring explicit test setup.
     * Tests that need a specific action can call setNextAction().
     */
    private HeroAction myNextAction = HeroAction.ATTACK;

    // ── GameView implementation ───────────────────────────────────────────

    @Override
    public void displayMessage(final String theMsg) {
        myMessages.add(theMsg != null ? theMsg : "");
    }

    @Override
    public void displayRoom(final Room theRoom) {
        // intentional no-op — tests inspect model directly
    }

    @Override
    public void displayHeroStats(final Hero theHero) {
        // intentional no-op
    }

    @Override
    public void displayDungeon(final Dungeon theDungeon) {
        // intentional no-op
    }

    @Override
    public void displayCombat(final Hero theHero, final Monster theMonster) {
        // intentional no-op
    }

    /**
     * Returns the configured next action and cycles through ATTACK as
     * the safe default. If the stub action is ATTACK the fight will
     * resolve without extra configuration.
     *
     * @return the HeroAction to use for this combat round
     */
    @Override
    public HeroAction promptHeroAction() {
        return myNextAction;
    }

    @Override
    public void displayCombatEvent(final CombatEvent theEvent) {
        // intentional no-op
    }

    @Override
    public void displayVision(final List<Room> theRooms) {
        // intentional no-op
    }

    // ── PropertyChangeListener ────────────────────────────────────────────

    @Override
    public void propertyChange(final PropertyChangeEvent theEvt) {
        myEvents.add(theEvt);
    }

    // ── Test-helper API ───────────────────────────────────────────────────

    /**
     * Returns true if any captured message contains the given substring
     * (case-insensitive).
     *
     * @param theSubstring the text to search for
     * @return true if at least one message contains the substring
     */
    public boolean hasMessage(final String theSubstring) {
        if (theSubstring == null) {
            return false;
        }
        final String lower = theSubstring.toLowerCase();
        return myMessages.stream()
                .anyMatch(m -> m.toLowerCase().contains(lower));
    }

    /**
     * Returns an unmodifiable snapshot of all captured messages.
     *
     * @return list of messages in the order they were received
     */
    public List<String> getMessages() {
        return List.copyOf(myMessages);
    }

    /**
     * Returns all PropertyChangeEvents received since construction.
     *
     * @return list of events in order
     */
    public List<PropertyChangeEvent> getEvents() {
        return List.copyOf(myEvents);
    }

    /**
     * Clears all captured messages. Useful in tests that need to check
     * only the messages produced by a specific action.
     */
    public void clearMessages() {
        myMessages.clear();
    }

    /**
     * Configures the action that will be returned by promptHeroAction()
     * on the next (and all subsequent) calls, until changed again.
     *
     * @param theAction the action to return; must not be null
     */
    public void setNextAction(final HeroAction theAction) {
        myNextAction = theAction;
    }

    /**
     * Returns the total number of messages captured since last clear.
     *
     * @return message count
     */
    public int getMessageCount() {
        return myMessages.size();
    }
}