package edu.uw.tcss.dungeoneer.model;

/**
 * Factory class for creating Hero instances by type.
 *
 * @author atemori
 * @version Iteration 2
 */
public class HeroFactory {

    /**
     * Creates and returns a Hero of the given type with the given name.
     *
     * @param theType the hero class ("Warrior", "Priestess", or "Thief")
     *                case-insensitive
     * @param theName the name to give the hero
     * @return a new Hero instance with correct stats
     * @throws IllegalArgumentException if theType is not recognized
     */
    public Hero createHero(final String theType,
                           final String theName) {

        // Normalize so "warrior", "WARRIOR", "Warrior" all work
        final String normalized = theType.substring(0, 1).toUpperCase()
                + theType.substring(1).toLowerCase();

        return switch (normalized) {
            case "Warrior" -> new Warrior(theName);
            case "Priestess" -> new Priestess(theName);
            case "Thief" -> new Thief(theName);
            default -> throw new IllegalArgumentException(
                    "Unknown hero type: \"" + theType
                            + "\". Valid types: Warrior, Priestess, Thief"
            );
        };
    }
}
