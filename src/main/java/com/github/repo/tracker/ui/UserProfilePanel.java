package com.github.repo.tracker.ui;

import com.github.repo.tracker.model.GitHubUser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class UserProfilePanel extends JPanel {

    private final JLabel avatarLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel bioLabel = new JLabel();
    private final JLabel followersLabel = new JLabel();
    private final JLabel followingLabel = new JLabel();
    private final JLabel reposLabel = new JLabel();

    public UserProfilePanel() {
        setLayout(new BorderLayout(15, 0)); // Memberi jarak horizontal 15px
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Avatar di kiri, rata atas
        avatarLabel.setPreferredSize(new Dimension(80, 80));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.TOP);
        add(avatarLabel, BorderLayout.WEST);

        // Panel untuk semua elemen teks di kanan
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bioLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bioLabel.setForeground(Color.GRAY);
        bioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel untuk statistik agar tetap dalam satu baris
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.add(followersLabel);
        statsPanel.add(followingLabel);
        statsPanel.add(reposLabel);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Mencegah panel statistik membesar secara vertikal
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, statsPanel.getPreferredSize().height));

        // Tambahkan semua komponen teks ke panel utama
        textPanel.add(nameLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(bioLabel);
        textPanel.add(Box.createVerticalGlue()); // Mendorong statistik ke bawah
        textPanel.add(statsPanel);

        add(textPanel, BorderLayout.CENTER);
        setVisible(false); // Sembunyi sampai ada data
    }

    public void updateUser(GitHubUser user) {
        if (user == null) {
            setVisible(false);
            return;
        }

        nameLabel.setText(user.getName() != null ? user.getName() : user.getLogin());
        // Trik HTML untuk auto word-wrap pada bio
        bioLabel.setText("<html><body style='width: 300px'>" + (user.getBio() != null ? user.getBio() : "No bio available") + "</body></html>");
        followersLabel.setText("Followers: " + user.getFollowers());
        followingLabel.setText("Following: " + user.getFollowing());
        reposLabel.setText("Repositories: " + user.getPublicRepos());

        // Muat avatar di background
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                URL url = new URL(user.getAvatarUrl());
                BufferedImage image = ImageIO.read(url);
                Image scaledImage = image.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }

            @Override
            protected void done() {
                try {
                    avatarLabel.setIcon(get());
                } catch (Exception e) {
                    avatarLabel.setText("No Avatar");
                }
            }
        }.execute();

        setVisible(true);
    }
} 