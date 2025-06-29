package com.github.repo.tracker.ui;

import com.github.repo.tracker.db.UserDatabase;
import com.github.repo.tracker.util.ResourceManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginDialog extends JDialog {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final UserDatabase userDb = new UserDatabase();
    private boolean loginSuccess = false;
    private String loggedInUsername = null;

    public LoginDialog(Frame owner) {
        super(owner, ResourceManager.get("app_title"), true);
        setSize(600, 400);  // Increased width
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Initialize text fields with larger size
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        
        // Style text fields
        Font inputFont = new Font("SansSerif", Font.PLAIN, 16);
        Dimension fieldSize = new Dimension(400, 40); // Increased field width
        usernameField.setPreferredSize(fieldSize);
        passwordField.setPreferredSize(fieldSize);
        usernameField.setFont(inputFont);
        passwordField.setFont(inputFont);
        
        // Main panel with generous padding
        JPanel mainPanel = new JPanel(new BorderLayout(20, 30));
        mainPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        // Header panel with app icon/title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("GitHub Repo Tracker");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32)); // Increased title size
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username field with icon
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel userLabel = new JLabel(ResourceManager.get("username_label") + ":");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameField, gbc);

        // Password field with icon
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passLabel = new JLabel(ResourceManager.get("password") + ":");
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel with larger spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton loginButton = createStyledButton(ResourceManager.get("login"));
        JButton registerButton = createStyledButton(ResourceManager.get("register"));

        loginButton.addActionListener(this::onLogin);
        registerButton.addActionListener(this::onRegister);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        // Add some vertical spacing before buttons
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(Box.createVerticalStrut(20), BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Enter key untuk login
        getRootPane().setDefaultButton(loginButton);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(180, 45));  // Wider buttons
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            }
        });
        
        return button;
    }

    private void onLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError(ResourceManager.get("empty_credentials"));
            return;
        }

        if (userDb.authenticate(username, password)) {
            loginSuccess = true;
            loggedInUsername = username;
            JOptionPane.showMessageDialog(
                this,
                String.format(ResourceManager.get("welcome_message"), username),
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } else {
            showError(ResourceManager.get("login_failed"));
            passwordField.setText("");
        }
    }

    private void onRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError(ResourceManager.get("empty_credentials"));
            return;
        }

        if (userDb.register(username, password)) {
            JOptionPane.showMessageDialog(
                this,
                ResourceManager.get("register_success"),
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            passwordField.setText("");
        } else {
            showError(ResourceManager.get("register_failed"));
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }
} 