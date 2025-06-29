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
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        avatarLabel.setPreferredSize(new Dimension(80, 80));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        add(avatarLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bioLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bioLabel.setForeground(Color.GRAY);

        textPanel.add(nameLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(bioLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.add(followersLabel);
        statsPanel.add(followingLabel);
        statsPanel.add(reposLabel);

        textPanel.add(statsPanel);
        add(textPanel, BorderLayout.CENTER);
        setVisible(false); // Sembunyikan sampai ada data
    }

    public void updateUser(GitHubUser user) {
        if (user == null) {
            setVisible(false);
            return;
        }

        nameLabel.setText(user.getName() != null ? user.getName() : user.getLogin());
        bioLabel.setText("<html>" + (user.getBio() != null ? user.getBio() : "No bio available") + "</html>");
        followersLabel.setText("Followers: " + user.getFollowers());
        followingLabel.setText("Following: " + user.getFollowing());
        reposLabel.setText("Repositories: " + user.getPublicRepos());

        // Load avatar in background
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