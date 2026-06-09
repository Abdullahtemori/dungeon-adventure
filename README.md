# Dungeon Adventure

A Java dungeon crawler game built with the MVC design pattern. Choose a hero,
explore a procedurally generated dungeon, battle monsters, collect all four
pillars of OO and escape through the exit to win.

## Team Members
- Abdullah Temori  —> Heroes & Monsters, Combat Panel, Hero/monster stats
                      and special skills and testing
- Tarik Atasoy     —> Dungeon generation, Room logic and dungeon Adventure entry
                      point and testing
- Daniella Birungi —> Items & Inventory system, game model, save load manager,
                      Swing View, audio manager and integration testing

## Prerequisites
* Java 17 or higher (This project was developed and tested on Java 25)
* IntelliJ IDEA (recommended) -> the .iml and .idea/ config files are included
* SQLite JDBC driver - required for loading monster data from the database

## Project Setup in IntelliJ
- Clone or unzip the project folder.
- Open in IntelliJ: File → Open → select the dungeon-adventure/ folder → click OK.
- Set the SDK: File → Project Structure → Project → set SDK to Java 17+.
- Add the JUnit JARs (already included in lib/): File → Project Structure → Libraries → + → Java
- Select all JARs inside the lib/ folder and click OK.
- Mark source and test roots:
  - Right-click src/ → Mark Directory as → Sources Root
  - Right-click test/ → Mark Directory as → Test Sources Root
- Add the SQLite JDBC driver

## SQLite Setup
Monster data is loaded from a local SQLite database. The game falls back to
hardcoded stats automatically if the database is missing, so this step is
optional but recommended for full functionality.

Here are the steps:

1. **Download the SQLite JDBC driver**
   Download `sqlite-jdbc-3.46.1.0.jar` (or newer) from:
   https://github.com/xerial/sqlite-jdbc/releases
   Place the JAR inside the `lib/` folder:

   ```text
   dungeon-adventure/
   └── lib/
       └── sqlite-jdbc-3.46.1.0.jar
   ```

2. **Add the JAR to IntelliJ**
   File → Project Structure → Libraries → + → Java
   Select `lib/sqlite-jdbc-3.46.1.0.jar` → click OK → Apply.

3. **Create the database**
   Run `DatabaseSetup.java` once to generate `database/monsters.db`:
   `src/edu/uw/tcss/dungeoneer/model/` → right-click `DatabaseSetup.java` → Run
   This creates the `database/` folder and populates it with Ogre, Gremlin,
   and Skeleton stats. You only need to do this once.

### Fallback mode
If the database file is missing or the JDBC driver is not on the classpath,
`MonsterFactory` automatically uses built-in monster stats and prints a
warning to the console. The game runs normally.

## How to run
1. Swing GUI (default):
In IntelliJ right click DungeonAdventure.java → Run
2. From the terminal (from the project root):

```bash
java -cp out/production/dungeon-adventure edu.uw.tcss.dungeoneer.DungeonAdventure
```

3. Using Console / text Mode:
Pass the `--console` flag to launch the text-based version:

```bash
java -cp out/production/dungeon-adventure edu.uw.tcss.dungeoneer.DungeonAdventure --console
```

A text menu will prompt you for hero name, class, and difficulty

## Gameplay

### Objective
Collect all four pillars of OO scattered throughout the dungeon and reach the
Exit room to win. Watch you HP (if it hits 0, the game ends)

### Difficulty Levels

| Difficulty | Dungeon Size |
|------------|--------------|
| Easy       | 5 x 5        |
| Medium     | 7 x 7        |
| Hard       | 10 x 10      |

### Hero Classes

| Hero      | HP  | Special Skill   | Description                                            |
|-----------|-----|-----------------|--------------------------------------------------------|
| Warrior   | 125 | Crushing Blow   | 40% chance to deal 75-175 damage in one hit            |
| Priestess | 75  | Heal            | Always restores 20-50 HP to themselves during combat   |
| Warrior   | 75  | Surprise Attack | 40% two attacks, 20% caught (no attack), 40% one attack |

### Monster Classes

| Monster  | HP  | Notes                                    |
|----------|-----|------------------------------------------|
| Orge     | 200 | Slow but hits hard (30–60 damage)        |
| Skeleton | 100 | Balanced attacker, 30% self-heal chance  |
| Gremlin  | 70  | Fast and evasive, 40% self-heal chance   |

### Items

| Item           | Effect                                                                |
|----------------|-----------------------------------------------------------------------|
| Healing Potion | Restores HP when used from inventory                                  |
| Vision Potion  | Reveals contents of the 8 surrounding rooms                           |
| Bomb           | Deals heavy damage to a monster in combat                             |
| Pillars of OO  | Collect all 4 (Abstraction, Encapsulation, Inheritance, Polymorphism) to win |

- Rooms may also contain pits that deal damage when entered.

## Controls

### GUI Controls
You can use the on-screen buttons, WASD and the arrow keys for all movement

| Menu | Options                                  |
|------|------------------------------------------|
| File | New Game, Save Game, Load Game, Exit     |
| Help | Instructions, About, Mute Audio, Cheat mode |

- Combat buttons appear automatically when a monster is encountered

### Console Controls

| Key     | Action                                |
|---------|---------------------------------------|
| N/S/E/W | Move North / South / East / West      |
| H       | Use a Healing Potion                  |
| V       | Use a Vision Potion                   |
| T       | Display hero stats                    |
| M       | Display full dungeon map (cheat mode only) |
| SAVE    | Save game to default file             |
| LOAD    | Load game from default file           |
| Q       | Quit to main menu                     |
| XYZZY   | Toggle cheat mode (hidden)            |

## Save & Load
- GUI: File → Save Game / File → Load Game
- Console: type SAVE or LOAD at the prompt

The save file is written to `dungeoneer_save.sav` in the project root
directory. The entire game state (dungeon, hero, inventory, pillars) is
serialized and restored exactly.

## How to run tests

### In IntelliJ
- Right-click the test/ folder → Run All Tests
- Or right-click any individual test class → Run.

### From the terminal

```bash
java -cp "out/test/dungeon-adventure:out/production/dungeon-adventure:lib/*" \
  org.junit.platform.console.standalone.ConsoleLauncher \
  --scan-class-path
```

All tests are independent — each uses @BeforeEach / @AfterEach to
set up and tear down state. No test relies on another test's output.

## Test Coverage

| Package    | Test Files                                                            |
|------------|-----------------------------------------------------------------------|
| model      | CombatTest, DungeonBuilderTest, DungeonTest, GameModelTest, HeroTest, ItemTest, ItemIntegrationTest, MonsterTest, RoomTest, SaveLoadManagerTest, BombTest, HealingPotionTest, VisionPotionTest, PillarTest, WinLoseConditionTest, IntegrationTest |
| controller | GameControllerTest                                                    |
| (root)     | IntegrationTest                                                       |
| test       | StubView (test helper, not a test class)                             |

## Project Structure

```text
dungeon-adventure/
├── src/
│   └── edu/uw/tcss/dungeoneer/
│       ├── DungeonAdventure.java        (entry point)
│       ├── controller/
│       │   └── GameController.java
│       ├── model/
│       │   ├── GameModel.java
│       │   ├── Dungeon.java
│       │   ├── DungeonBuilder.java
│       │   ├── Room.java
│       │   ├── Hero.java / Warrior / Priestess / Thief
│       │   ├── Monster.java / Ogre / Gremlin / Skeleton
│       │   ├── Combat.java / CombatEvent.java
│       │   ├── Item.java / HealingPotion / VisionPotion / Bomb / Pillar
│       │   ├── HeroFactory.java / MonsterFactory.java
│       │   ├── SaveLoadManager.java
│       │   ├── Difficulty.java / Direction.java
│       │   └── DatabaseSetup.java
│       └── view/
│           ├── GameView.java            ← interface
│           ├── SwingView.java           ← GUI view
│           ├── ConsoleView.java         ← text view
│           ├── CombatPanel.java
│           ├── AudioManager.java
│           └── sounds/                  ← .wav audio files
├── test/
│   └── edu/uw/tcss/dungeoneer/
│       ├── model/                       ← model unit tests
│       ├── controller/                  ← controller tests
│       ├── test/StubView.java           ← test double for GameView
│       └── IntegrationTest.java         ← end-to-end tests
├── lib/                                 ← JUnit 5 JARs (SQLite JAR goes here)
├── database/                            ← monsters.db (generated by DatabaseSetup)
├── documents/
│   └── UML_final.pdf
└── README.md
```

## Sound Files

| File              | Plays When                  |
|-------------------|-----------------------------|
| menu_music.wav    | Main menu is open           |
| dungeon_music.wav | Exploring the dungeon       |
| combat_music.wav  | In combat                   |
| sfx_hit.wav       | Attack lands                |
| sfx_miss.wav      | Attack misses               |
| sfx_bomb.wav      | Bomb is used                |
| sfx_pickup.wav    | Item picked up/potion used  |
| sfx_pit.wav       | hero falls into pit         |
| sfx_death.wav     | hero is defeated            |
| sfx_victory.wav   | monster is defeated         |
| sfx_win.wav       | Player wins the game        |

- Audio mute toggle: Help → Mute Audio (GUI) — no console equivalent.

## Known Shortcomings
- The console view does not support in-combat item usage (bombs/potions during a fight
  must be used through the GUI).
- Sound playback may not be available on headless systems with no audio device;
  the game continues silently in that case.
- Save files from a previous version of the game may not load correctly if
  the class structure has changed since they were created.

## Extra Credit Items
- Full audio system (AudioManager) with background music, combat music,
  and per-event sound effects.
- Console mode (--console flag) as a fully playable alternative to the GUI,
  including a hidden cheat code (XYZZY).
- Serialization-based save/load that preserves complete game state
  including dungeon layout, hero inventory, and collected pillars.
- SQLite integration for monster data with graceful fallback.
