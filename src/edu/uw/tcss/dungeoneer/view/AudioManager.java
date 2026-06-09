package edu.uw.tcss.dungeoneer.view;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Audio manger handles all the background music and sound effects for
 * the dungeon adventure game using the java sound API. It is implemented
 * as a singleton because there should only ever be one audio system
 * running at a time. Any class can call AudioManager.getInstance() to
 * play sounds without holding a reference passed through the constructor
 * chain.
 * Audio files will be place in:
 * src/edu/tcss/dungeoneer/view/sounds/
 * and named exactly as the constants below.
 *
 * @author Daniella Birungi
 * @version Iteration 4
 */
public final class AudioManager {

    /**
     * Background music for main menu.
     * File: sounds/menu_music.wav
     */
    public static final String MUSIC_MENU = "sounds/menu_music.wav";

    /**
     * Background music during dungeon navigation.
     * File: sounds/dungeon_music.wav
     */
    public static final String MUSIC_DUNGEON = "sounds/dungeon_music.wav";

    /**
     * Background music during combat.
     * File: sounds/combat_music.wav
     */
    @SuppressWarnings("unused")
    public static final String MUSIC_COMBAT = "sounds/combat_music.wav";

    /**
     * Sound effect for hero movement between rooms.
     */
    public static final String SFX_MOVE = "sounds/sfx_move.wav";

    /**
     * Sound effect for picking up an item.
     */
    public static final String SFX_PICKUP = "sounds/sfx_pickup.wav";

    /**
     * Sound effect for falling into a pit.
     */
    @SuppressWarnings("unused")
    public static final String SFX_PIT = "sounds/sfx_pit.wav";

    /**
     * Sound effect for a successful attack hit.
     */
    public static final String SFX_HIT = "sounds/sfx_hit.wav";

    /**
     * Sound effect for a missed attack.
     */
    public static final String SFX_MISS = "sounds/sfx_miss.wav";

    /**
     * Sound effect for using a bomb.
     */
    public static final String SFX_BOMB = "sounds/sfx_bomb.wav";

    /**
     * Sound effect for hero death.
     */
    public static final String SFX_DEATH = "sounds/sfx_death.wav";

    /**
     * Sound effect for defeating a monster.
     */
    public static final String SFX_VICTORY = "sounds/sfx_victory.wav";

    /**
     * Sound effect for winning the game.
     */
    @SuppressWarnings("unused")
    public static final String SFX_WIN = "sounds/sfx_win.wav";

    /**
     * The single instance of AudioManager.
     * Created lazily on first call to getInstance().
     */
    private static AudioManager myInstance;

    /**
     * The Clip currently playing background music.
     * Null when no music is playing.
     */
    private Clip myMusicClip;

    /**
     * Whether audio is currently muted.
     * When true, all play calls are no-ops.
     */
    private boolean myMuted;

    /**
     * Whether the audio system is available on this machine.
     * Set to false on first failure so we stop retrying.
     */
    private final boolean myAudioAvailable;

    /**
     * Private constructor.
     * Checks whether the audio system is available on this machine.
     * Sets myAudioAvailable = false if no sound card exists so all
     * subsequent calls fail silently without repeated error messages.
     */
    private AudioManager() {
        myMuted = false;
        myAudioAvailable = checkAudioAvailable();
        if (!myAudioAvailable) {
            System.out.println("AudioManager: No audio system found. "
                    + "Game will run without sound.");
        }
    }

    /**
     * Returns the single AudioManager instance.
     * Creates it on first call.
     *
     * @return the AudioManager singleton
     */
    public static synchronized AudioManager getInstance() {
        if (myInstance == null) {
            myInstance = new AudioManager();
        }
        return myInstance;
    }

    /**
     * Starts playing background music from the given file path,
     * looping continuously until stopMusic() or playMusic() is called.
     * If music is already playing it is stopped first.
     * If the file is not found or audio is unavailable, the call
     * is silently ignored — the game continues without music.
     *
     * @param theTrack the resource path to the .wav file
     */
    public void playMusic(final String theTrack) {
        if (!myAudioAvailable || myMuted || theTrack == null) return;

        // Stop any currently playing music first
        stopMusic();

        try {
            final Clip clip = loadClip(theTrack);
            if (clip == null) return;

            // Loop continuously until stopped
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            myMusicClip = clip;

        } catch (final Exception theException) {
            // Audio failed, log and continue without music
            System.err.println("AudioManager: Could not play music: "
                    + theTrack + " — " + theException.getMessage());
        }
    }

    /**
     * Stops the currently playing background music.
     * Safe to call even when no music is playing.
     */
    public void stopMusic() {
        if (myMusicClip != null) {
            try {
                myMusicClip.stop();
                myMusicClip.close();
            } catch (final Exception theException) {
                // Ignore stop errors
            } finally {
                myMusicClip = null;
            }
        }
    }

    /**
     * Plays a one-shot sound effect from the given file path.
     * The effect plays once and stops automatically.
     * Sound effects do not interrupt background music, they play
     * on a separate Clip instance at the same time.
     * If the file is not found or audio is unavailable, the call
     * is silently ignored.
     *
     * @param theSfx the resource path to the .wav file
     */
    // Clip closed asynchronously via LineListener on STOP
    @SuppressWarnings("resource")
    public void playSFX(final String theSfx) {
        if (!myAudioAvailable || myMuted || theSfx == null) return;

        try {
            final Clip clip = loadClip(theSfx);
            if (clip == null) return;

            // Play once, close the clip automatically when done
            clip.addLineListener(event -> {
                if (javax.sound.sampled.LineEvent.Type.STOP
                        .equals(event.getType())) {
                    clip.close();
                }
            });

            clip.start();

        } catch (final Exception theException) {
            // Audio failed, log and continue without SFX
            System.err.println("AudioManager: Could not play SFX: "
                    + theSfx + " — " + theException.getMessage());
        }
    }

    /**
     * Plays the correct sound effect for a given CombatEvent.
     * Convenience method so GameController/SwingView does not need
     * to write a switch statement every time.
     *
     * @param theEvent the combat event to play a sound for
     */
    @SuppressWarnings("unused")
    public void playCombatSFX(
            final edu.uw.tcss.dungeoneer.model.CombatEvent theEvent) {
        if (theEvent == null) return;

        switch (theEvent.getType()) {
            case ATTACK_HIT:
            case SPECIAL_SUCCESS:
                playSFX(SFX_HIT);
                break;
            case ATTACK_MISS:
            case SPECIAL_FAIL:
            case SPECIAL_CAUGHT:
                playSFX(SFX_MISS);
                break;
            case BOMB_USED:
                playSFX(SFX_BOMB);
                break;
            case POTION_USED:
            case SPECIAL_HEAL:
                playSFX(SFX_PICKUP);
                break;
            case COMBAT_END:
                // amount == 1 means hero won
                if (theEvent.getAmount() == 1) {
                    playSFX(SFX_VICTORY);
                } else {
                    playSFX(SFX_DEATH);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Toggles audio mute on or off.
     * When muted, all playMusic() and playSFX() calls are no-ops.
     * When unmuted, music resumes if it was stopped by muting.
     */
    public void toggleMute() {
        myMuted = !myMuted;

        if (myMuted) {
            // Mute: stop music without clearing the track reference
            if (myMusicClip != null && myMusicClip.isRunning()) {
                myMusicClip.stop();
            }
            System.out.println("AudioManager: Audio muted.");
        } else {
            // Unmute: resume music if it was playing
            if (myMusicClip != null) {
                myMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                myMusicClip.start();
            }
            System.out.println("AudioManager: Audio unmuted.");
        }
    }

    /**
     * Returns whether audio is currently muted.
     *
     * @return true if muted
     */
    public boolean isMuted() {
        return myMuted;
    }

    /**
     * Returns whether the audio system is available on this machine.
     *
     * @return true if audio is available, false if no sound card found
     */
    @SuppressWarnings("unused")
    public boolean isAudioAvailable() {
        return myAudioAvailable;
    }


    /**
     * Loads a .wav file from the classpath and returns an open Clip.
     * Returns null if the file cannot be found or loaded.
     *
     * @param theResourcePath the classpath path to the .wav file
     * @return an open ready-to-play Clip, or null on failure
     */
    private Clip loadClip(final String theResourcePath) {
        try {
            final InputStream rawStream =
                    getClass().getResourceAsStream(theResourcePath);

            if (rawStream == null) {
                // File not found in classpath (silent fail)
                System.err.println("AudioManager: Sound file not found: "
                        + theResourcePath);
                return null;
            }

            // BufferedInputStream required by AudioSystem for mark/reset
            final AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(rawStream));

            final Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;

        } catch (final Exception theException) {
            System.err.println("AudioManager: Failed to load clip: "
                    + theResourcePath + " — " + theException.getMessage());
            return null;
        }
    }

    /**
     * Tests whether the audio system is available on this machine.
     * Attempts to get a Clip from AudioSystem — if that throws,
     * audio is not available.
     *
     * @return true if audio is available
     */
    private boolean checkAudioAvailable() {
        try {
            AudioSystem.getClip();
            return true;
        } catch (final Exception theException) {
            return false;
        }
    }
}