package com.github.repo.tracker.ui;

import com.github.repo.tracker.db.UserDatabase;
import com.github.repo.tracker.util.ResourceManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginDialog extends JDialog {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final UserDatabase userDb = new UserDatabase();
    private boolean loginSuccess = false;
    private String loggedInUsername = null;
    private JButton loginButton;
    private JButton registerButton;

    public LoginDialog(Frame owner) {
        super(owner, ResourceManager.get("app_title"), true);
        setSize(700, 400);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Initialize text fields with larger size
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        
        // Style text fields with rounded corners and modern look
        Font inputFont = new Font("SansSerif", Font.PLAIN, 16);
        Dimension fieldSize = new Dimension(500, 40);
        usernameField.setPreferredSize(fieldSize);
        passwordField.setPreferredSize(fieldSize);
        usernameField.setFont(inputFont);
        passwordField.setFont(inputFont);
        
        // Custom border for text fields
        Border roundedBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(32, 32, 32), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );
        usernameField.setBorder(roundedBorder);
        passwordField.setBorder(roundedBorder);

        // Create main panel with white background
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Create content panel that will hold all components
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        contentPanel.setOpaque(false);
        
        // Create and style labels
        JLabel titleLabel = new JLabel(ResourceManager.get("app_title"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(32, 32, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create form panel for centered alignment
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add form fields with labels
        addFormField(formPanel, ResourceManager.get("username_label"), usernameField);
        addFormField(formPanel, ResourceManager.get("password"), passwordField);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Style buttons
        loginButton = createStyledButton(ResourceManager.get("login"), new Color(32, 32, 32));
        registerButton = createStyledButton(ResourceManager.get("register"), new Color(64, 64, 64));

        // Add components to button panel
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Add all components to content panel
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(40));
        contentPanel.add(formPanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(buttonPanel);

        // Add content panel to main panel
        mainPanel.add(contentPanel);

        // Set content pane
        setContentPane(mainPanel);

        // Add action listeners
        addActionListeners();
        
        // Set default button
        getRootPane().setDefaultButton(loginButton);
    }

    private void addActionListeners() {
        loginButton.addActionListener(this::onLogin);
        registerButton.addActionListener(this::onRegister);
    }

    private void addFormField(JPanel container, String labelText, JComponent field) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(new Color(32, 32, 32));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(8));
        fieldPanel.add(field);
        fieldPanel.add(Box.createVerticalStrut(20));
        
        container.add(fieldPanel);
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
        
        button.setPreferredSize(new Dimension(200, 40));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(new Color(32, 32, 32));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }
} 