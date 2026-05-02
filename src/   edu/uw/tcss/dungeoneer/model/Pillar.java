package edu.uw.tcss.dungeoneer.model;

/**
 * Pillar represents one of the four pillars of object oriented programming
 * that the hero must collect to win the game. The four pillars are Abstraction,
 * Encapsulation, Inheritance and Polymorphism.
 *
 * @author Daniella Birungi
 * @version 1.0
 */
public enum Pillar {
    /**
     * Represents the abstraction pillar.
     */
    ABSTRACTION,
    /**
     * Represents the encapsulation pillar.
     */
    ENCAPSULATION,
    /**
     * Represents the inheritance pillar.
     */
    INHERITANCE,
    /**
     * Represents the polymorphism pillar.
     */
    POLYMORPHISM;

    /**
     * The display character for this pillar.
     * Usess the first letter of the pillar name
     *
     * @return the first character of the pillar name
     */
    public char getDisplayCharacter() {
        return this.name().charAt(0);
    }
}