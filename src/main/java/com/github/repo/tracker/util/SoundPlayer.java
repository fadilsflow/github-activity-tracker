
package com.github.repo.tracker.util;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

public class SoundPlayer {

    public static void play(String resourcePath) {
        URL url = SoundPlayer.class.getResource(resourcePath);
        if (url == null) {
            System.err.println("Audio resource not found: " + resourcePath);
            return;
        }
        try (var audioStream = AudioSystem.getAudioInputStream(url)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
} 