package com.github.repo.tracker.ui;

import com.github.repo.tracker.model.Commit;
import com.github.repo.tracker.model.Repo;
import com.github.repo.tracker.network.GithubService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RepoDetailDialog extends JDialog {

    public RepoDetailDialog(Frame owner, Repo repo, GithubService service, String username) {
        super(owner, "Repository Details: " + repo.getName(), true);
        setSize(600, 500);
        setLocationRelativeTo(owner);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(245, 245, 245);
                Color color2 = new Color(255, 255, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Repository name header
        JLabel nameLabel = new JLabel(repo.getName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        nameLabel.setForeground(new Color(51, 51, 51));
        
        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        statsPanel.setOpaque(false);
        
        JLabel starsLabel = new JLabel("⭐ " + repo.getStargazersCount());
        JLabel forksLabel = new JLabel("🔀 " + repo.getForksCount());
        
        starsLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        forksLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        
        statsPanel.add(starsLabel);
        statsPanel.add(forksLabel);

        // Header panel combining name and stats
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);
        headerPanel.add(nameLabel, BorderLayout.NORTH);
        headerPanel.add(statsPanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Description panel
        JTextArea descriptionArea = new JTextArea(repo.getDescription());
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descriptionArea.setBackground(new Color(248, 249, 250));
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        mainPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton openInBrowserButton = createStyledButton("Open in Browser", new Color(66, 133, 244));
        JButton viewCommitsButton = createStyledButton("View Commits", new Color(67, 160, 71));

        openInBrowserButton.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(repo.getHtmlUrl()));
                }
            } catch (IOException | URISyntaxException ex) {
                JOptionPane.showMessageDialog(this, "Could not open browser.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewCommitsButton.addActionListener(e -> {
            CommitHistoryDialog historyDialog = new CommitHistoryDialog(this, repo, service, username);
            historyDialog.setVisible(true);
        });
        
        buttonPanel.add(openInBrowserButton);
        buttonPanel.add(viewCommitsButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
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
                g2.setColor(new Color(210, 210, 210));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        button.setPreferredSize(new Dimension(150, 40));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(new Color(51, 51, 51));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
}

class CommitHistoryDialog extends JDialog {
    private final GenericTableModel<Commit> tableModel;

    CommitHistoryDialog(Dialog owner, Repo repo, GithubService service, String username) {
        super(owner, "Commit History: " + repo.getName(), true);
        setSize(800, 500);
        setLocationRelativeTo(owner);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(245, 245, 245);
                Color color2 = new Color(255, 255, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                g2d.dispose();
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = {"SHA", "Author", "Message", "Date"};
        tableModel = new GenericTableModel<>(columns,
                c -> c.getSha().substring(0, 7),
                Commit::getAuthorName,
                Commit::getMessage,
                c -> DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(c.getDate())
        );
        JTable table = new JTable(tableModel);
        
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

        // Custom scroll pane with rounded borders
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
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);

        new SwingWorker<List<Commit>, Void>() {
            @Override
            protected List<Commit> doInBackground() throws Exception {
                return service.fetchCommits(username, repo.getName());
            }

            @Override
            protected void done() {
                try {
                    tableModel.setData(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CommitHistoryDialog.this,
                            "Failed to load commits: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
} 