package edu.uw.tcss.dungeoneer.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Creates Monster instances for the dungeon. The factory first tries
 * to read the monster catalogue from an SQLite database. When the
 * driver, the database file, or the expected schema is unavailable
 * the factory falls back to a hardcoded catalogue so the rest of the
 * game keeps running without crashing the launcher or the Swing UI.
 *
 * Two modes:
 *   DB mode       - jdbc:sqlite:monsters.db is reachable; names come
 *                   from the "monsters" table.
 *   Fallback mode - any DB problem is caught and the factory uses a
 *                   built-in name list (Ogre, Gremlin, Skeleton).
 *
 * Concrete Monster subclasses (Ogre, Gremlin, Skeleton) already carry
 * their SRS baseline stats in their no-arg constructors, so the
 * factory simply selects which subclass to instantiate by name.
 *
 * @author Tarik Atasoy
 * @version Iteration 4
 */
public class MonsterFactory {

    /** JDBC URL for the SQLite catalogue, resolved against the JVM CWD. */
    private static final String DB_URL = "jdbc:sqlite:monsters.db";

    /** Names used when the database cannot be reached. */
    private static final List<String> FALLBACK_NAMES =
            Arrays.asList("Ogre", "Gremlin", "Skeleton");

    /** Names available to {@link #createRandom()}. */
    private final List<String> myMonsterNames = new ArrayList<>();

    /** True when the constructor successfully loaded names from the DB. */
    private final boolean myDbAvailable;

    /** Shared random source for {@link #createRandom()}. */
    private final Random myRandom = new Random();

    /**
     * Builds the factory and loads the monster name list. If the
     * database, driver, or schema is missing or invalid, the factory
     * silently switches to the hardcoded fallback list. This keeps
     * the launcher and GUI alive when the optional SQLite driver is
     * not installed.
     */
    public MonsterFactory() {
        boolean dbOk = false;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM monsters")) {
            while (rs.next()) {
                myMonsterNames.add(rs.getString("name"));
            }
            dbOk = !myMonsterNames.isEmpty();
        } catch (final Exception ex) {
            // Driver missing, file missing, or schema mismatch.
            // Swallow and fall back; the launcher must not crash.
            myMonsterNames.clear();
        }

        if (!dbOk) {
            myMonsterNames.addAll(FALLBACK_NAMES);
        }
        myDbAvailable = dbOk;
    }

    /**
     * Creates a Monster instance for the given catalogue name. When
     * the database is reachable the name is first verified against
     * the "monsters" table; on any failure (including the row being
     * missing or the driver having vanished mid-session) the call
     * falls back to a direct instantiation by name.
     *
     * @param theName monster name from the catalogue
     * @return a fresh Monster instance with its SRS baseline stats
     * @throws IllegalArgumentException if the name does not match
     *                                  any known monster type
     */
    public Monster createByName(final String theName) {
        if (myDbAvailable) {
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT name FROM monsters WHERE name = ?")) {
                ps.setString(1, theName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return instantiate(theName);
                    }
                }
            } catch (final SQLException ignored) {
                // Fall through to the hardcoded path below.
            }
        }
        return instantiate(theName);
    }

    /**
     * Returns a randomly selected Monster from the loaded catalogue.
     * The selection pool is whichever list won at construction time
     * (database rows or fallback names).
     *
     * @return a random Monster instance
     */
    public Monster createRandom() {
        final String name = myMonsterNames.get(
                myRandom.nextInt(myMonsterNames.size()));
        return createByName(name);
    }

    /**
     * Indicates whether the factory is reading from the SQLite
     * catalogue or from its built-in fallback list. Useful for
     * diagnostics and for tests that want to verify which path is
     * active.
     *
     * @return true if the database backed the factory, false if
     *         the hardcoded fallback is in use
     */
    public boolean isDatabaseAvailable() {
        return myDbAvailable;
    }

    /**
     * Maps a catalogue name to the matching concrete Monster type.
     * Centralized so both the DB path and the fallback path produce
     * identical objects.
     *
     * @param theName monster name from the catalogue
     * @return a fresh Monster instance for that name
     * @throws IllegalArgumentException if the name is not recognized
     */
    private static Monster instantiate(final String theName) {
        return switch (theName) {
            case "Ogre"     -> new Ogre();
            case "Gremlin"  -> new Gremlin();
            case "Skeleton" -> new Skeleton();
            default -> throw new IllegalArgumentException(
                    "No monster named: " + theName);
        };
    }
}
