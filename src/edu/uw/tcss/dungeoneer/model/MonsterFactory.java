package edu.uw.tcss.dungeoneer.model;

import java.sql.*;
import java.util.*;

public class MonsterFactory {
    private static final String DB_URL = "jdbc:sqlite:monsters.db";
    private final List<String> myMonsterNames = new ArrayList<>();

    /** Loads all monster names from DB at startup. */
    public MonsterFactory() {
        try (Connection conn =
                     DriverManager.getConnection(DB_URL)) {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT name FROM monsters");
            while (rs.next()) {
                myMonsterNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Cannot load monster database: " + e.getMessage()
            );
        }
    }

    /** Returns a monster whose stats match the database entry. */
    public Monster createByName(final String theName) {
        try (Connection conn =
                     DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT name FROM monsters WHERE name = ?"
            );
            ps.setString(1, theName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return switch (theName) {
                    case "Ogre"     -> new Ogre();
                    case "Gremlin"  -> new Gremlin();
                    case "Skeleton" -> new Skeleton();
                    default -> throw new IllegalArgumentException(
                            "No monster named: " + theName
                    );
                };
            } else {
                throw new IllegalArgumentException(
                        "No monster named: " + theName
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error: " + e.getMessage()
            );
        }
    }

    /** Returns a randomly selected monster. */
    public Monster createRandom() {
        String name = myMonsterNames.get(
                new Random().nextInt(myMonsterNames.size())
        );
        return createByName(name);
    }
}
