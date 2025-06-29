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
    private final GenericTableModel<Repo> tableModel;
    private final JTable table;
    private final UserProfilePanel userProfilePanel;
    private List<Repo> currentRepos = List.of();

    // Komponen UI yang perlu update bahasa
    private final JLabel usernameLabel;
    private final JButton searchButton;
    private final JButton langToggle;
    private final JButton sortStarsButton;
    private final JButton sortForksButton;
    private final JButton sortNameButton;

    private final RepoDatabase database = new RepoDatabase();
    private final GithubService service = new GithubService();

    public TrackerFrame() {
        super("GitHub Repo Activity Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 700);
        setLocationRelativeTo(null);

        // Inisialisasi komponen dengan teks dari ResourceManager
        usernameLabel = new JLabel(ResourceManager.get("username_label"));
        searchButton = new JButton(ResourceManager.get("search"));
        langToggle = new JButton(ResourceManager.getCurrentLocale().getLanguage().equals("id") ? ResourceManager.get("lang_en") : ResourceManager.get("lang_id"));
        sortStarsButton = new JButton(ResourceManager.get("sort_stars"));
        sortForksButton = new JButton(ResourceManager.get("sort_forks"));
        sortNameButton = new JButton(ResourceManager.get("sort_name"));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // 1. HEADER (Pencarian & Profil)
        userProfilePanel = new UserProfilePanel();
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(usernameLabel);
        searchPanel.add(usernameField);
        searchPanel.add(searchButton);
        searchPanel.add(langToggle);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(searchPanel, BorderLayout.NORTH);
        headerPanel.add(userProfilePanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. KONTEN (Tombol Sort & Tabel)
        String[] columns = {
            ResourceManager.get("col_name"),
            ResourceManager.get("col_stars"),
            ResourceManager.get("col_forks"),
            ResourceManager.get("col_updated")
        };
        tableModel = new GenericTableModel<>(columns,
                Repo::getName,
                Repo::getStargazersCount,
                Repo::getForksCount,
                r -> r.getUpdatedAt() != null ? DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        .withZone(ZoneId.systemDefault())
                        .format(r.getUpdatedAt()) : "N/A"
        );
        table = new JTable(tableModel);
        
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortPanel.add(sortStarsButton);
        sortPanel.add(sortForksButton);
        sortPanel.add(sortNameButton);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(sortPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // LISTENERS
        searchButton.addActionListener(this::onSearch);
        usernameField.addActionListener(this::onSearch); // <-- Menambahkan listener Enter di sini
        langToggle.addActionListener(e -> toggleLanguage());
        sortStarsButton.addActionListener(e -> sortRepos(Comparator.comparing(Repo::getStargazersCount).reversed()));
        sortForksButton.addActionListener(e -> sortRepos(Comparator.comparing(Repo::getForksCount).reversed()));
        sortNameButton.addActionListener(e -> sortRepos(Comparator.comparing(Repo::getName, String.CASE_INSENSITIVE_ORDER)));

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
        Locale newLoc = current.getLanguage().equals("id") ? Locale.ENGLISH : new Locale("id", "ID");
        ResourceManager.setLocale(newLoc);
        updateTexts();
    }

    private void updateTexts() {
        usernameLabel.setText(ResourceManager.get("username_label"));
        searchButton.setText(ResourceManager.get("search"));
        langToggle.setText(ResourceManager.getCurrentLocale().getLanguage().equals("id") ? ResourceManager.get("lang_en") : ResourceManager.get("lang_id"));
        sortStarsButton.setText(ResourceManager.get("sort_stars"));
        sortForksButton.setText(ResourceManager.get("sort_forks"));
        sortNameButton.setText(ResourceManager.get("sort_name"));

        String[] columns = {
            ResourceManager.get("col_name"),
            ResourceManager.get("col_stars"),
            ResourceManager.get("col_forks"),
            ResourceManager.get("col_updated")
        };
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setHeaderValue(columns[i]);
        }
        table.getTableHeader().repaint();
    }

    private void onSearch(ActionEvent evt) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, ResourceManager.get("empty_username"), "Warning", JOptionPane.WARNING_MESSAGE);
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
                CompletableFuture<GitHubUser> userFuture = CompletableFuture.supplyAsync(() -> {
                    try { return service.fetchUser(username); } 
                    catch (Exception e) { throw new RuntimeException(e); }
                });

                CompletableFuture<List<Repo>> reposFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        List<Repo> repos = service.fetchRepos(username);
                        String hash = HashUtil.sha256(username);
                        database.saveRepos(username, repos, hash);
                        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream("repos.ser"))) {
                            oos.writeObject(new java.util.ArrayList<>(repos));
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                        return repos;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                CompletableFuture.allOf(userFuture, reposFuture).join();
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