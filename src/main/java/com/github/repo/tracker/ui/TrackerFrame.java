package com.github.repo.tracker.ui;

import com.github.repo.tracker.db.RepoDatabase;
import com.github.repo.tracker.model.Repo;
import com.github.repo.tracker.network.GithubService;
import com.github.repo.tracker.util.HashUtil;
import com.github.repo.tracker.util.ResourceManager;
import com.github.repo.tracker.util.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TrackerFrame extends JFrame {

    private final JTextField usernameField = new JTextField(20);
    private final JButton searchButton = new JButton(ResourceManager.get("search"));
    private final JButton langToggle = new JButton("EN");
    private final GenericTableModel<Repo> tableModel;
    private final JTable table;

    private final RepoDatabase database = new RepoDatabase();
    private final GithubService service = new GithubService();

    public TrackerFrame() {
        super("GitHub Repo Activity Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        String[] columns = {"Nama", "⭐", ResourceManager.get("forks"), ResourceManager.get("updated")};
        tableModel = new GenericTableModel<>(columns,
                Repo::getName,
                r -> r.getStargazersCount(),
                Repo::getForksCount,
                r -> DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        .withZone(ZoneId.systemDefault())
                        .format(r.getUpdatedAt())
        );
        table = new JTable(tableModel);

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel(ResourceManager.get("username")));
        topPanel.add(usernameField);
        topPanel.add(searchButton);
        topPanel.add(langToggle);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchButton.addActionListener(this::onSearch);
        langToggle.addActionListener(e -> toggleLanguage());
    }

    private void toggleLanguage() {
        Locale current = ResourceManager.getCurrentLocale();
        Locale newLoc = current.equals(new Locale("id")) ? Locale.ENGLISH : new Locale("id");
        ResourceManager.setLocale(newLoc);
        updateTexts();
    }

    private void updateTexts() {
        searchButton.setText(ResourceManager.get("search"));
        langToggle.setText(ResourceManager.getCurrentLocale().equals(Locale.ENGLISH) ? "ID" : "EN");
        String[] columns = {"Nama", "⭐", ResourceManager.get("forks"), ResourceManager.get("updated")};
        for (int i = 0; i < columns.length; i++) {
            tableModel.getColumnName(i);
        }
    }

    private void onSearch(ActionEvent evt) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, ResourceManager.get("empty_username"));
            return;
        }
        searchButton.setEnabled(false);
        new RepoWorker(username).execute();
    }

    private class RepoWorker extends SwingWorker<List<Repo>, Void> {
        private final String username;
        private Exception error;

        RepoWorker(String username) {
            this.username = username;
        }

        @Override
        protected List<Repo> doInBackground() {
            try {
                // Threading (syarat 2)
                List<Repo> repos = service.fetchRepos(username); // Network (syarat 7)
                // Cryptography (syarat 5)
                String hash = HashUtil.sha256(username);
                // Database (syarat 6)
                database.saveRepos(username, repos, hash);
                // Serialization (syarat 3) - simpan ke file
                java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                        new java.io.FileOutputStream("repos.ser"));
                oos.writeObject(repos);
                oos.close();
                return repos;
            } catch (Exception e) {
                error = e;
                return null;
            }
        }

        @Override
        protected void done() {
            searchButton.setEnabled(true);
            if (error != null) {
                JOptionPane.showMessageDialog(TrackerFrame.this, error.getMessage());
                return;
            }
            try {
                List<Repo> repos = get();
                tableModel.setData(repos);
                // Multimedia (syarat 8)
                SoundPlayer.play("/sounds/done.wav");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(TrackerFrame.this, e.getMessage());
            }
        }
    }
} 