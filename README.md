# Dungeon Adventure

A Java dungeon crawler game built with the MVC design pattern. This project is currently in progress.

## Team Members
- Abdullah Temori — Heroes & Monsters
- Tarik Atasoy — Dungeon & Rooms
- Daniella Birungi — Items & Inventory

## Project Goal
The goal of the game is to choose a hero, explore a dungeon, fight monsters, collect the four Pillars of OO, and reach the exit to win.

## Current Progress
For Iteration 1, we focused on the core model layer, including heroes, monsters, items, inventory, rooms, dungeon generation, and initial JUnit tests.

## How to Run
This project is still in progress. Open the repo in IntelliJ IDEA or another Java IDE and run the available tests/source files from the project structure.

## Tech
- Java 17+
- JUnit 5
- SQLite planned for monster data
- # SQLite Setup

This project uses SQLite for loading monster data.

## Setup Instructions

1. Download the SQLite JDBC driver.

2. Place the JAR file inside the `lib/` folder.

Example:

lib/sqlite-jdbc-3.46.1.0.jar

3. In IntelliJ:

File → Project Structure → Libraries

Add the SQLite JDBC JAR to the project.

4. Make sure the database file exists:

database/monsters.db

## Fallback Mode

If the database is missing or SQLite fails to load,
the game will automatically use fallback monsters
instead of crashing.
