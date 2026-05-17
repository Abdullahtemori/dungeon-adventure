package edu.uw.tcss.dungeoneer.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * SaveLoadManager handles saving and loading game state using
 * Java serialization. It writes the entire GameModel object to
 * a .sav file and reads it back when the player wants to resume.
 * This is a utility class. it has no fields or state of its own.
 * GameController calls it when the player selects File > Save Game
 * or File > Load Game from the menu.
 *
 * @author Daniella Birungi
 * @version Iteration 2
 */
public class SaveLoadManager {
    /**
     * The default file name used when no path is specified.
     * Saved in the project root directory.
     */
    private static final String DEFAULT_SAVE_PATH = "dungeoneer_save.sav";

    /**
     * Private constructor — this class should never be instantiated.
     * All methods are static utility methods.
     */
    private SaveLoadManager() {

    }
    /**
     * Saves the current GameModel to a file at the given path.
     * The entire model(dungeon, hero, inventory,flags) is
     * serialized into bytes and written to disk.
     * If saving fails the exception is caught and an error message is printed.
     * The game continues normally because a failed save does not crash.
     *
     * @param theModel the GameModel to save
     * @param thePath the file path to save to
     * @return true if save was successful, false if it failed
     */
    public static boolean saveGame(final GameModel theModel,
                                   final String thePath) {

        // Guard against null model
        if(theModel == null) {
            System.err.println("SaveLoadManager: cannot save null model");
        }
        // Use default path if none provided
        final String path = (thePath == null || thePath.isEmpty())
                ? DEFAULT_SAVE_PATH : thePath;

        // Try-with-resources automatically closes the streams
        try (final FileOutputStream fos = new FileOutputStream(path);
             final ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            // Write the entire GameModel object to the file
            oos.writeObject(theModel);
            oos.flush();
            System.out.println("Game saved successfully to: " + path);
            return true;

        } catch (final IOException e) {
            // Saving failed — inform the player but don't crash
            System.err.println("Failed to save game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves the game to the default save file path.
     * Convenience method that calls saveGame with the default path.
     *
     * @param theModel the game model to save
     * @return true if save was successful, false if it failed
     */
    public static boolean saveGame(final GameModel theModel) {
        return saveGame(theModel, DEFAULT_SAVE_PATH);
    }

    /**
     * Loads a GameModel from a save file at the given path.
     * The file is deserialized back into a GameModel object.
     * If loading fails (file not found, corrupted, wrong version),
     * the exception is caught and null is returned. The caller
     * (GameController) should check for null and show an error
     * message to the player instead of crashing.
     *
     * @param thePath the file path to load from (e.g. "save1.sav")
     * @return the loaded GameModel, or null if loading failed
     */
    public static GameModel loadGame(final String thePath) {
        // Use default path if none provided
        final String path = (thePath == null || thePath.isEmpty())
                ? DEFAULT_SAVE_PATH : thePath;

        // check before trying open
        if(!saveExists(path)) {
            System.err.println("SaveLoadManager: save file not found: " + path);
            return null;
        }

        // Try-with-resources automatically closes the streams
        try (final FileInputStream fis = new FileInputStream(path);
             final ObjectInputStream ois = new ObjectInputStream(fis)) {

            // Read and cast the object back to GameModel
            final GameModel model = (GameModel) ois.readObject();
            System.out.println("Game loaded successfully from: " + path);
            return model;

        } catch (final IOException e) {
            // File not found or corrupted
            System.err.println("Failed to load save file: "
                    + e.getMessage());
            e.printStackTrace();
            return null;

        } catch (final ClassNotFoundException e) {
            // Save file is from an incompatible version
            System.err.println("Save file is incompatible with "
                    + "this version of the game: " + e.getMessage());
            return null;
        }
    }

    /**
     * Loads a game from the default save file path.
     * Convenience method that calls loadGame with the default path.
     *
     * @return the loaded GameModel, or null if loading failed
     */
    public static GameModel loadGame() {
        return loadGame(DEFAULT_SAVE_PATH);
    }


    /**
     * Checks whether a save file exists at the given path.
     * GameController uses this to decide whether to enable or
     * disable the Load Game button in the menu.
     *
     * @param thePath the file path to check
     * @return true if the save file exists, false otherwise
     */
    public static boolean saveExists(final String thePath) {
        final String path = (thePath == null || thePath.isEmpty())
                ? DEFAULT_SAVE_PATH : thePath;
        return new java.io.File(path).exists();
    }

    /**
     * Checks whether a save file exists at the default path.
     *
     * @return true if a save file exists, false otherwise
     */
    public static boolean saveExists() {
        return saveExists(DEFAULT_SAVE_PATH);
    }

    /**
     * Deletes the save file at the given path.
     * Used by tests in tearDown() to clean up save files after each test
     * so they do not affect other tests.
     *
     * @param thePath the file path to delete
     * @return true if deleted successfully, false if file did not exist
     */
    public static boolean deleteSave(final String thePath) {
        final String path = (thePath == null || thePath.isEmpty())
                ? DEFAULT_SAVE_PATH : thePath;
        final java.io.File file = new java.io.File(path);
        if (file.exists()) {
            final boolean deleted = file.delete();
            if (!deleted) {
                System.err.println(
                        "SaveLoadManager: could not delete " + path);
            }
            return deleted;
        }
        return false;
    }

    /**
     * Deletes the save file at the default path.
     *
     * @return true if deleted, false if file did not exist
     */
    public static boolean deleteSave() {
        return deleteSave(DEFAULT_SAVE_PATH);
    }

}
