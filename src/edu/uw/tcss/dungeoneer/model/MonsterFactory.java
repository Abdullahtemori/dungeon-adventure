package edu.uw.tcss.dungeoneer.model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * MonsterFactory loads monsters from SQLite.
 * If the database is missing, the game uses fallback monsters
 * instead of crashing.
 */
public class MonsterFactory {

    /**
     * Database file location.
     */
    private static final String DB_PATH = "database/monsters.db";

    /**
     * SQLite JDBC URL.
     */
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    /**
     * Backup monster list if DB fails.
     */
    private static final List<String> FALLBACK_NAMES =
            Arrays.asList("Ogre", "Gremlin", "Skeleton");

    /**
     * Loaded monster names.
     */
    private final List<String> myMonsterNames = new ArrayList<>();

    /**
     * True if database loaded successfully.
     */
    private final boolean myDbAvailable;

    /**
     * Random object.
     */
    private final Random myRandom = new Random();

    /**
     * Constructor.
     */
    public MonsterFactory() {

        boolean dbOk = false;

        try {

            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Check database file exists
            File dbFile = new File(DB_PATH);

            if (!dbFile.exists()) {

                System.out.println(
                        "WARNING: monsters.db not found at: "
                                + dbFile.getAbsolutePath());

            } else {

                try (Connection conn = DriverManager.getConnection(DB_URL);
                     Statement stmt = conn.createStatement();
                     ResultSet rs =
                             stmt.executeQuery("SELECT name FROM monsters")) {

                    while (rs.next()) {
                        myMonsterNames.add(rs.getString("name"));
                    }

                    dbOk = !myMonsterNames.isEmpty();
                }
            }

        } catch (Exception ex) {

            System.out.println(
                    "WARNING: SQLite database could not load.");

            System.out.println("Using fallback monster list.");

            System.out.println("Reason: " + ex.getMessage());

            myMonsterNames.clear();
        }

        if (!dbOk) {
            myMonsterNames.addAll(FALLBACK_NAMES);
        }

        myDbAvailable = dbOk;
    }

    /**
     * Creates a monster by name.
     *
     * @param theName monster name
     * @return Monster object
     */
    public Monster createByName(final String theName) {

        if (myDbAvailable) {

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement ps =
                         conn.prepareStatement(
                                 "SELECT name FROM monsters WHERE name = ?")) {

                ps.setString(1, theName);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return instantiate(theName);
                    }
                }

            } catch (Exception ex) {

                System.out.println(
                        "Database error. Using fallback monster creation.");
            }
        }

        return instantiate(theName);
    }

    /**
     * Creates random monster.
     *
     * @return random monster
     */
    public Monster createRandom() {

        String name = myMonsterNames.get(
                myRandom.nextInt(myMonsterNames.size()));

        return createByName(name);
    }

    /**
     * Returns DB status.
     *
     * @return true if DB works
     */
    public boolean isDatabaseAvailable() {
        return myDbAvailable;
    }

    /**
     * Creates monster objects.
     *
     * @param theName monster name
     * @return Monster object
     */
    private static Monster instantiate(final String theName) {

        return switch (theName) {

            case "Ogre" -> new Ogre();

            case "Gremlin" -> new Gremlin();

            case "Skeleton" -> new Skeleton();

            default ->
                    throw new IllegalArgumentException(
                            "Unknown monster: " + theName);
        };
    }
}
