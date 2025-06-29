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
        super(owner, "Detail: " + repo.getName(), true);
        setSize(500, 400);
        setLocationRelativeTo(owner);

        JTextArea descriptionArea = new JTextArea(repo.getDescription());
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        JButton openInBrowserButton = new JButton("Open in Browser");
        JButton viewCommitsButton = new JButton("View Commits");

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
        
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openInBrowserButton);
        buttonPanel.add(viewCommitsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(panel);
    }
}

class CommitHistoryDialog extends JDialog {
    private final GenericTableModel<Commit> tableModel;

    CommitHistoryDialog(Dialog owner, Repo repo, GithubService service, String username) {
        super(owner, "Commit History: " + repo.getName(), true);
        setSize(700, 500);
        setLocationRelativeTo(owner);

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
        add(new JScrollPane(table), BorderLayout.CENTER);

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