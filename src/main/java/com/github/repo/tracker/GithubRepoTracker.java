/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.github.repo.tracker;

import com.github.repo.tracker.ui.TrackerFrame;
import javafx.application.Platform;

/**
 *
 * @author fadil
 */
public class GithubRepoTracker {

    public static void main(String[] args) {
        // Initialize JavaFX toolkit
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // JavaFX already initialized
        }

        // Start Swing application
        java.awt.EventQueue.invokeLater(() -> {
            new TrackerFrame().setVisible(true);
        });
    }
}
