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
    private String loggedInUser = null;

    // Komponen UI yang perlu update bahasa
    private final JLabel usernameLabel;
    private final JButton searchButton;
    private final JButton langToggle;
    private final JButton sortStarsButton;
    private final JButton sortForksButton;
    private final JButton sortNameButton;
    private final JButton logoutButton;

    private final RepoDatabase database = new RepoDatabase();
    private final GithubService service = new GithubService();

    public TrackerFrame() {
        super("GitHub Repo Activity Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Show login dialog
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);
        
        if (!loginDialog.isLoginSuccess()) {
            System.exit(0);
        }
        
        loggedInUser = loginDialog.getLoggedInUsername();

        // Inisialisasi komponen dengan teks dari ResourceManager
        usernameLabel = new JLabel(ResourceManager.get("username_label"));
        searchButton = createStyledButton(ResourceManager.get("search"), new Color(66, 133, 244));
        langToggle = createStyledButton(ResourceManager.getCurrentLocale().getLanguage().equals("id") ? ResourceManager.get("lang_en") : ResourceManager.get("lang_id"), new Color(108, 117, 125));
        sortStarsButton = createStyledButton(ResourceManager.get("sort_stars"), new Color(255, 193, 7));
        
        sortForksButton = createStyledButton(ResourceManager.get("sort_forks"), new Color(255, 193, 7));
        sortNameButton = createStyledButton(ResourceManager.get("sort_name"), new Color(255, 193, 7));
        logoutButton = createStyledButton("Logout", new Color(220, 53, 69));

        // Adjust width for sort buttons
        sortStarsButton.setPreferredSize(new Dimension(150, 35));
        sortForksButton.setPreferredSize(new Dimension(150, 35));
        sortNameButton.setPreferredSize(new Dimension(150, 35));

        // Style the username field
        usernameField.setPreferredSize(new Dimension(200, 35));
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        setContentPane(mainPanel);

        // 1. HEADER (Pencarian & Profil)
        userProfilePanel = new UserProfilePanel();
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(usernameLabel);
        searchPanel.add(usernameField);
        searchPanel.add(searchButton);
        
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightButtons.setBackground(Color.WHITE);
        rightButtons.add(langToggle);
        rightButtons.add(logoutButton);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(rightButtons, BorderLayout.EAST);
        
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(topPanel, BorderLayout.NORTH);
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
        
        // Style the table
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.getTableHeader().setForeground(new Color(51, 51, 51));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        sortPanel.setBackground(Color.WHITE);
        sortPanel.add(sortStarsButton);
        sortPanel.add(sortForksButton);
        sortPanel.add(sortNameButton);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(sortPanel, BorderLayout.NORTH);
        
        // Create a custom scroll pane with rounded borders
        JScrollPane scrollPane = new JScrollPane(table) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Color.WHITE);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // LISTENERS
        searchButton.addActionListener(this::onSearch);
        usernameField.addActionListener(this::onSearch);
        langToggle.addActionListener(e -> toggleLanguage());
        sortStarsButton.addActionListener(e -> sortRepos(Comparator.comparing(Repo::getStargazersCount).reversed()));
        sortForksButton.addActionListener(e -> sortRepos(Comparator.comparing(Repo::getForksCount).reversed()));
        sortNameButton.addActionListener(e -> sortRepos(Comparator.comparing(Repo::getName, String.CASE_INSENSITIVE_ORDER)));
        logoutButton.addActionListener(e -> {
            dispose();
            new TrackerFrame().setVisible(true);
        });

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
            userProfilePanel.setVisible(false);
            if (error != null) {
                String errorMessage;
                if (error instanceof RuntimeException && error.getCause() != null) {
                    errorMessage = error.getCause().getMessage();
                } else {
                    errorMessage = error.getMessage();
                }
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "Gagal mencari user. Periksa koneksi internet Anda.";
                }
                JOptionPane.showMessageDialog(
                    TrackerFrame.this,
                    errorMessage,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            try {
                FetchResult result = get();
                if (result != null) {
                    userProfilePanel.updateUser(result.user);
                    userProfilePanel.setVisible(true);
                    currentRepos = new java.util.ArrayList<>(result.repos);
                    tableModel.setData(currentRepos);
                    SoundPlayer.play("/done.mp3");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    TrackerFrame.this,
                    "Terjadi kesalahan: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
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

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(210, 210, 210));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(240, 240, 240));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                
                // Draw border
                g2.setColor(new Color(32, 32, 32));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        // Make sort buttons wider
        int width = text.startsWith("Sort by") ? 150 : 120;
        button.setPreferredSize(new Dimension(width, 35));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(new Color(32, 32, 32));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
}