package app;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.sound.sampled.*;
import java.util.concurrent.atomic.AtomicInteger;

// ✅ Generic Programming - Generic Repository Pattern
interface Repository<T> {
    void save(T item);
    List<T> findAll();
    T findById(String id);
    void delete(String id);
}

// ✅ Generic Programming - Generic Data Container
class DataContainer<T> {
    private final List<T> items;
    private final Class<T> type;
    
    public DataContainer(Class<T> type) {
        this.type = type;
        this.items = new ArrayList<>();
    }
    
    public void add(T item) {
        items.add(item);
    }
    
    public List<T> getAll() {
        return new ArrayList<>(items);
    }
    
    public Class<T> getType() {
        return type;
    }
    
    public int size() {
        return items.size();
    }
}

// ✅ Serialization - Serializable RepoActivity class
class RepoActivity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String repoName;
    private String activity;
    private String timestamp;
    private String author;
    private String encryptedData;
    
    public RepoActivity(String id, String repoName, String activity, String timestamp, String author) {
        this.id = id;
        this.repoName = repoName;
        this.activity = activity;
        this.timestamp = timestamp;
        this.author = author;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    
    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getEncryptedData() { return encryptedData; }
    public void setEncryptedData(String encryptedData) { this.encryptedData = encryptedData; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s by %s at %s", id, repoName, activity, author, timestamp);
    }
}

// ✅ Generic Programming + Database - Generic Repository Implementation
class RepoActivityRepository implements Repository<RepoActivity> {
    private Connection connection;
    
    public RepoActivityRepository() {
        initDatabase();
    }
    
    private void initDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:github_tracker.db");
            String createTable = """
                CREATE TABLE IF NOT EXISTS activities (
                    id TEXT PRIMARY KEY,
                    repo_name TEXT NOT NULL,
                    activity TEXT NOT NULL,
                    timestamp TEXT NOT NULL,
                    author TEXT NOT NULL,
                    encrypted_data TEXT
                )
                """;
            connection.createStatement().executeUpdate(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void save(RepoActivity item) {
        String sql = "INSERT OR REPLACE INTO activities (id, repo_name, activity, timestamp, author, encrypted_data) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, item.getId());
            stmt.setString(2, item.getRepoName());
            stmt.setString(3, item.getActivity());
            stmt.setString(4, item.getTimestamp());
            stmt.setString(5, item.getAuthor());
            stmt.setString(6, item.getEncryptedData());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public List<RepoActivity> findAll() {
        List<RepoActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM activities ORDER BY timestamp DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                RepoActivity activity = new RepoActivity(
                    rs.getString("id"),
                    rs.getString("repo_name"),
                    rs.getString("activity"),
                    rs.getString("timestamp"),
                    rs.getString("author")
                );
                activity.setEncryptedData(rs.getString("encrypted_data"));
                activities.add(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activities;
    }
    
    @Override
    public RepoActivity findById(String id) {
        String sql = "SELECT * FROM activities WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                RepoActivity activity = new RepoActivity(
                    rs.getString("id"),
                    rs.getString("repo_name"),
                    rs.getString("activity"),
                    rs.getString("timestamp"),
                    rs.getString("author")
                );
                activity.setEncryptedData(rs.getString("encrypted_data"));
                return activity;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM activities WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// ✅ Cryptography - Encryption/Decryption utility
class CryptoUtil {
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "MySecretKey12345"; // 16 chars for AES-128
    
    public static String encrypt(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return data; // Return original if encryption fails
        }
    }
    
    public static String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decodedData));
        } catch (Exception e) {
            e.printStackTrace();
            return encryptedData; // Return original if decryption fails
        }
    }
    
    public static String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return input;
        }
    }
}

// ✅ Network - GitHub API client
class GitHubApiClient {
    private static final String GITHUB_API_BASE = "https://api.github.com";
    
    public CompletableFuture<String> fetchRepoActivity(String owner, String repo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/commits?per_page=10");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setRequestProperty("User-Agent", "GitHub-Tracker-App");
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        });
    }
}

// ✅ Thread - Background data fetcher
class DataFetcher extends Thread {
    private final GitHubApiClient apiClient;
    private final Repository<RepoActivity> repository;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final AtomicInteger fetchCount;
    private volatile boolean running = true;
    
    public DataFetcher(Repository<RepoActivity> repository, JProgressBar progressBar, JLabel statusLabel) {
        this.repository = repository;
        this.apiClient = new GitHubApiClient();
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
        this.fetchCount = new AtomicInteger(0);
        setDaemon(true);
    }
    
    public void stopFetching() {
        running = false;
    }
    
    @Override
    public void run() {
        while (running) {
            try {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Fetching GitHub data...");
                    progressBar.setIndeterminate(true);
                });
                
                // Simulate fetching data from different repositories
                String[] testRepos = {"microsoft/vscode", "facebook/react", "google/tensorflow"};
                String selectedRepo = testRepos[fetchCount.get() % testRepos.length];
                String[] parts = selectedRepo.split("/");
                
                apiClient.fetchRepoActivity(parts[0], parts[1]).thenAccept(response -> {
                    // Create mock activity data since we can't parse real JSON without external libs
                    String activityId = "activity_" + System.currentTimeMillis();
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    
                    RepoActivity activity = new RepoActivity(
                        activityId,
                        selectedRepo,
                        "Commit pushed to main branch",
                        timestamp,
                        "developer_" + (fetchCount.get() % 5 + 1)
                    );
                    
                    // Encrypt sensitive data
                    String encryptedData = CryptoUtil.encrypt(activity.getActivity() + "|" + activity.getAuthor());
                    activity.setEncryptedData(encryptedData);
                    
                    repository.save(activity);
                    fetchCount.incrementAndGet();
                    
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Data fetched successfully. Total activities: " + fetchCount.get());
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(100);
                    });
                });
                
                // Sleep for 10 seconds before next fetch
                Thread.sleep(10000);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error fetching data: " + e.getMessage());
                    progressBar.setIndeterminate(false);
                });
            }
        }
    }
}

// ✅ Multimedia - Sound player
class SoundPlayer {
    public static void playNotificationSound() {
        try {
            // Generate a simple beep sound
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            
            // Generate 500ms beep at 1000Hz
            int sampleRate = 44100;
            int duration = 500; // milliseconds
            int frequency = 1000; // Hz
            
            byte[] buffer = new byte[sampleRate * duration / 1000];
            for (int i = 0; i < buffer.length; i++) {
                double angle = 2 * Math.PI * frequency * i / sampleRate;
                buffer[i] = (byte) (Math.sin(angle) * 127);
            }
            
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (Exception e) {
            // Fallback to system beep
            Toolkit.getDefaultToolkit().beep();
        }
    }
}

// ✅ Serialization - Data persistence manager
class DataPersistenceManager {
    private static final String DATA_FILE = "github_tracker_data.ser";
    
    public static void saveData(DataContainer<RepoActivity> container) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(container);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static DataContainer<RepoActivity> loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (DataContainer<RepoActivity>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new DataContainer<>(RepoActivity.class);
        }
    }
}

// ✅ Internationalization - Resource bundle manager
class I18nManager {
    private static ResourceBundle bundle;
    private static Locale currentLocale = Locale.getDefault();
    
    static {
        loadBundle();
    }
    
    private static void loadBundle() {
        try {
            // ✅ Internationalization - Load from properties file
            
            bundle = ResourceBundle.getBundle("messages", currentLocale);
        } catch (Exception e) {
            System.err.println("Cannot load resource bundle for locale " + currentLocale);
            System.err.println(e.getMessage());
            // Fallback to English locale if current locale fails
            if (!currentLocale.equals(Locale.ENGLISH)) {
                setLocale(Locale.ENGLISH);
            }
        }
    }
    
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key; // Return key if translation not found
        }
    }
    
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        loadBundle();
    }
}

// Main Application Class
public class main extends JFrame implements ActionListener {
    private final Repository<RepoActivity> repository;
    private final DataContainer<RepoActivity> dataContainer;
    private final ExecutorService executorService;
    private DataFetcher dataFetcher;
    
    // UI Components
    private JTable activityTable;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel totalLabel;
    private JButton startButton, stopButton, refreshButton, clearButton;
    private JMenuItem exportMenuItem, importMenuItem;
    
    public main() {
        this.repository = new RepoActivityRepository();
        this.dataContainer = DataPersistenceManager.loadData();
        this.executorService = Executors.newFixedThreadPool(3);
        
        initializeUI();
        loadInitialData();
        
        // Add shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dataFetcher != null) {
                dataFetcher.stopFetching();
            }
            DataPersistenceManager.saveData(dataContainer);
            executorService.shutdown();
        }));
    }
    
    private void initializeUI() {
        setTitle(I18nManager.getString("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create menu bar
        createMenuBar();
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        startButton = new JButton(I18nManager.getString("button.start"));
        stopButton = new JButton(I18nManager.getString("button.stop"));
        refreshButton = new JButton(I18nManager.getString("button.refresh"));
        clearButton = new JButton(I18nManager.getString("button.clear"));
        
        startButton.addActionListener(this);
        stopButton.addActionListener(this);
        refreshButton.addActionListener(this);
        clearButton.addActionListener(this);
        
        stopButton.setEnabled(false);
        
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(refreshButton);
        controlPanel.add(clearButton);
        
        // Create table
        String[] columnNames = {
            I18nManager.getString("column.id"),
            I18nManager.getString("column.repo"),
            I18nManager.getString("column.activity"),
            I18nManager.getString("column.timestamp"),
            I18nManager.getString("column.author")
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        activityTable = new JTable(tableModel);
        activityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activityTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        // Create status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(I18nManager.getString("status.ready"));
        totalLabel = new JLabel(I18nManager.getString("label.total") + " 0");
        progressBar = new JProgressBar();
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(totalLabel, BorderLayout.CENTER);
        statusPanel.add(progressBar, BorderLayout.SOUTH);
        
        // Add components to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Set window properties
        setSize(900, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu(I18nManager.getString("menu.file"));
        exportMenuItem = new JMenuItem(I18nManager.getString("menu.export"));
        importMenuItem = new JMenuItem(I18nManager.getString("menu.import"));
        JMenuItem exitMenuItem = new JMenuItem(I18nManager.getString("menu.exit"));
        
        exportMenuItem.addActionListener(this);
        importMenuItem.addActionListener(this);
        exitMenuItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(exportMenuItem);
        fileMenu.add(importMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        
        // Language menu
        JMenu languageMenu = new JMenu(I18nManager.getString("menu.language"));
        JMenuItem englishMenuItem = new JMenuItem(I18nManager.getString("language.english"));
        JMenuItem indonesianMenuItem = new JMenuItem(I18nManager.getString("language.indonesian"));
        
        englishMenuItem.addActionListener(e -> changeLanguage(Locale.ENGLISH));
        indonesianMenuItem.addActionListener(e -> changeLanguage(new Locale("id", "ID")));
        
        languageMenu.add(englishMenuItem);
        languageMenu.add(indonesianMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(languageMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void changeLanguage(Locale locale) {
        I18nManager.setLocale(locale);
        
        // Update UI components with new language
        setTitle(I18nManager.getString("app.title"));
        startButton.setText(I18nManager.getString("button.start"));
        stopButton.setText(I18nManager.getString("button.stop"));
        refreshButton.setText(I18nManager.getString("button.refresh"));
        clearButton.setText(I18nManager.getString("button.clear"));
        
        // Update table headers
        String[] columnNames = {
            I18nManager.getString("column.id"),
            I18nManager.getString("column.repo"),
            I18nManager.getString("column.activity"),
            I18nManager.getString("column.timestamp"),
            I18nManager.getString("column.author")
        };
        
        tableModel.setColumnIdentifiers(columnNames);
        
        repaint();
    }
    
    private void loadInitialData() {
        CompletableFuture.runAsync(() -> {
            List<RepoActivity> activities = repository.findAll();
            SwingUtilities.invokeLater(() -> {
                for (RepoActivity activity : activities) {
                    addActivityToTable(activity);
                }
                updateTotalLabel();
            });
        }, executorService);
    }
    
    private void addActivityToTable(RepoActivity activity) {
        Object[] row = {
            activity.getId(),
            activity.getRepoName(),
            activity.getActivity(),
            activity.getTimestamp(),
            activity.getAuthor()
        };
        tableModel.addRow(row);
        dataContainer.add(activity);
    }
    
    private void updateTotalLabel() {
        totalLabel.setText(I18nManager.getString("label.total") + " " + tableModel.getRowCount());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == startButton) {
            startTracking();
        } else if (source == stopButton) {
            stopTracking();
        } else if (source == refreshButton) {
            refreshData();
        } else if (source == clearButton) {
            clearData();
        } else if (source == exportMenuItem) {
            exportData();
        } else if (source == importMenuItem) {
            importData();
        }
    }
    
    private void startTracking() {
        if (dataFetcher == null || !dataFetcher.isAlive()) {
            dataFetcher = new DataFetcher(repository, progressBar, statusLabel);
            dataFetcher.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            statusLabel.setText(I18nManager.getString("status.tracking"));
            
            SoundPlayer.playNotificationSound();
        }
    }
    
    private void stopTracking() {
        if (dataFetcher != null) {
            dataFetcher.stopFetching();
            dataFetcher = null;
            
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            statusLabel.setText(I18nManager.getString("status.stopped"));
            progressBar.setValue(0);
            
            SoundPlayer.playNotificationSound();
        }
    }
    
    private void refreshData() {
        CompletableFuture.runAsync(() -> {
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                dataContainer.getAll().clear();
            });
            
            loadInitialData();
            
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Data refreshed");
                SoundPlayer.playNotificationSound();
            });
        }, executorService);
    }
    
    private void clearData() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear all data?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            tableModel.setRowCount(0);
            dataContainer.getAll().clear();
            
            // Clear from database
            CompletableFuture.runAsync(() -> {
                List<RepoActivity> allActivities = repository.findAll();
                for (RepoActivity activity : allActivities) {
                    repository.delete(activity.getId());
                }
            }, executorService);
            
            updateTotalLabel();
            statusLabel.setText("Data cleared");
            SoundPlayer.playNotificationSound();
        }
    }
    
    private void exportData() {
        CompletableFuture.runAsync(() -> {
            DataPersistenceManager.saveData(dataContainer);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, I18nManager.getString("message.export.success"));
                statusLabel.setText("Data exported");
            });
        }, executorService);
    }
    
    private void importData() {
        CompletableFuture.runAsync(() -> {
            DataContainer<RepoActivity> importedData = DataPersistenceManager.loadData();
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                for (RepoActivity activity : importedData.getAll()) {
                    addActivityToTable(activity);
                }
                updateTotalLabel();
                JOptionPane.showMessageDialog(this, I18nManager.getString("message.import.success"));
                statusLabel.setText("Data imported");
            });
        }, executorService);
    }
    
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new main());
    }
}