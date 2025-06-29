package com.github.repo.tracker.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.application.Platform;

public class SoundPlayer {
    private static boolean initialized = false;
    
    private static void initializeJavaFX() {
        if (!initialized) {
            try {
                Platform.startup(() -> {});
                initialized = true;
            } catch (IllegalStateException e) {
                // JavaFX already initialized
                initialized = true;
            }
        }
    }

    public static void play(String resourcePath) {
        initializeJavaFX();
        
        try {
            String uri = SoundPlayer.class.getResource(resourcePath).toURI().toString();
            Media media = new Media(uri);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.dispose();
            });
            
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 