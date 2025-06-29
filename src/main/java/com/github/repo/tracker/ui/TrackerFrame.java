package com.github.repo.tracker.ui;

import com.github.repo.tracker.db.RepoDatabase;
import com.github.repo.tracker.model.GitHubUser;
import com.github.repo.tracker.model.Repo;
import com.github.repo.tracker.network.GithubService;
import com.github.repo.tracker.util.HashUtil;
import com.github.repo.tracker.util.ResourceManager;
import com.github.repo.tracker.util.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class TrackerFrame extends JFrame {

    private final JTextField usernameField = new JTextField(20);
    private final JButton searchButton = new JButton(ResourceManager.get("search"));
    private final JButton langToggle = new JButton("EN");
    private final GenericTableModel<Repo> tableModel;
    private final JTable table;
    private final UserProfilePanel userProfilePanel;
    private List<Repo> currentRepos = List.of();

    private final RepoDatabase database = new RepoDatabase();
    private final GithubService service = new GithubService();

    public TrackerFrame() {
        super("GitHub Repo Activity Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 700);
        setLocationRelativeTo(null);

        // PANEL UTAMA
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // 1. HEADER (Pencarian & Profil)
        userProfilePanel = new UserProfilePanel();
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel(ResourceManager.get("username")));
        searchPanel.add(usernameField);
        searchPanel.add(searchButton);
        searchPanel.add(langToggle);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(searchPanel, BorderLayout.NORTH);
        headerPanel.add(userProfilePanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. KONTEN (Tombol Sort & Tabel)
        String[] columns = {"Nama", "⭐", ResourceManager.get("forks"), ResourceManager.get("updated")};
        tableModel = new GenericTableModel<>(columns,
                Repo::getName,
                Repo::getStargazersCount,
                Repo::getForksCount,
                r -> DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        .withZone(ZoneId.systemDefault())
                        .format(r.getUpdatedAt())
        );
        table = new JTable(tableModel);
        
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton sortStars = new JButton("Sort by Stars");
        sortStars.addActionListener(e -> sortRepos(Comparator.comparing(Repo::getStargazersCount).reversed()));
        JButton sortForks = new JButton("Sort by Forks");
        sortForks.addActionListener(e -> sortRepos(Comparator.comparing(Repo::getForksCount).reversed()));
        JButton sortName = new JButton("Sort by Name");
        sortName.addActionListener(e -> sortRepos(Comparator.comparing(Repo::getName, String.CASE_INSENSITIVE_ORDER)));
        sortPanel.add(sortStars);
        sortPanel.add(sortForks);
        sortPanel.add(sortName);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(sortPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // LISTENERS
        searchButton.addActionListener(this::onSearch);
        langToggle.addActionListener(e -> toggleLanguage());
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    Repo selectedRepo = tableModel.getRow(table.getSelectedRow());
                    RepoDetailDialog dialog = new RepoDetailDialog(TrackerFrame.this, selectedRepo, service, usernameField.getText().trim());
                    dialog.setVisible(true);
                }
            }
        });
    }

    private void sortRepos(Comparator<Repo> comparator) {
        if(currentRepos != null) {
            currentRepos.sort(comparator);
            tableModel.setData(currentRepos);
        }
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
            table.getColumnModel().getColumn(i).setHeaderValue(tableModel.getColumnName(i));
        }
        table.getTableHeader().repaint();
    }

    private void onSearch(ActionEvent evt) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, ResourceManager.get("empty_username"));
            return;
        }
        searchButton.setEnabled(false);
        userProfilePanel.setVisible(false);
        new RepoWorker(username).execute();
    }

    private class RepoWorker extends SwingWorker<FetchResult, Void> {
        private final String username;
        private Exception error;

        RepoWorker(String username) {
            this.username = username;
        }

        @Override
        protected FetchResult doInBackground() {
            try {
                // Jalankan pengambilan data user dan repo secara paralel
                CompletableFuture<GitHubUser> userFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return service.fetchUser(username);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                CompletableFuture<List<Repo>> reposFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        // 1. Ambil data repo dari network
                        List<Repo> repos = service.fetchRepos(username);
                        
                        // 2. Simpan ke database (logika yang hilang)
                        String hash = HashUtil.sha256(username);
                        database.saveRepos(username, repos, hash);

                        // 3. Simpan ke file serialisasi (logika yang hilang)
                        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                                new java.io.FileOutputStream("repos.ser"))) {
                            oos.writeObject(new java.util.ArrayList<>(repos));
                        } catch (java.io.IOException e) {
                            e.printStackTrace(); // Log error, tapi jangan hentikan proses
                        }
                        
                        return repos;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                // Tunggu kedua proses selesai
                CompletableFuture.allOf(userFuture, reposFuture).join();

                // Kembalikan hasilnya
                return new FetchResult(userFuture.get(), reposFuture.get());

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
                FetchResult result = get();
                userProfilePanel.updateUser(result.user);
                currentRepos = new java.util.ArrayList<>(result.repos);
                tableModel.setData(currentRepos);
                SoundPlayer.play("/sounds/done.wav");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(TrackerFrame.this, e.getMessage());
            }
        }
    }
    
    private static class FetchResult {
        final GitHubUser user;
        final List<Repo> repos;

        FetchResult(GitHubUser user, List<Repo> repos) {
            this.user = user;
            this.repos = repos;
        }
    }
} 