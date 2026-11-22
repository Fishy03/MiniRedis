
package com.miniredis.client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MiniRedisGUI extends JFrame {

    private JTextField keyField;
    private JTextField valueField;
    private JTextField ttlField;
    private JTextArea responseArea;
    private JLabel statusLabel;

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public MiniRedisGUI() {
        setTitle("MiniRedis - Database Client");
        setSize(750, 700);
        setMinimumSize(new Dimension(700, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createUI();
    }

    private void createUI() {

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(245, 247, 250));

        // Header
        JLabel title = new JLabel("MiniRedis");
        title.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel subtitle = new JLabel("In-Memory Key-Value Database");
        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setBackground(main.getBackground());
        header.add(title);
        header.add(subtitle);

        // Connection section
        JPanel connection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connection.setBackground(Color.WHITE);

        statusLabel = new JLabel("Disconnected");

        JButton connectButton = new JButton("Connect");
        JButton disconnectButton = new JButton("Disconnect");
        JButton pingButton = new JButton("PING");

        connection.add(new JLabel("localhost:6379"));
        connection.add(statusLabel);
        connection.add(connectButton);
        connection.add(disconnectButton);
        connection.add(pingButton);

        // Input fields
        keyField = new JTextField(20);
        valueField = new JTextField(20);
        ttlField = new JTextField(20);

        keyField.setPreferredSize(new Dimension(200, 30));
        valueField.setPreferredSize(new Dimension(200, 30));
        ttlField.setPreferredSize(new Dimension(200, 30));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Command Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        form.add(new JLabel("Key:"));
        form.add(keyField);

        form.add(new JLabel("Value:"));
        form.add(valueField);

        form.add(new JLabel("TTL (seconds):"));
        form.add(ttlField);

        // Command buttons
        JPanel buttons = new JPanel(new GridLayout(2, 3, 10, 10));
        buttons.setBackground(main.getBackground());

        JButton setButton = new JButton("SET");
        JButton getButton = new JButton("GET");
        JButton delButton = new JButton("DELETE");
        JButton existsButton = new JButton("EXISTS");
        JButton expireButton = new JButton("EXPIRE");
        JButton ttlButton = new JButton("TTL");

        buttons.add(setButton);
        buttons.add(getButton);
        buttons.add(delButton);
        buttons.add(existsButton);
        buttons.add(expireButton);
        buttons.add(ttlButton);

        // Center layout
        JPanel center = new JPanel(new BorderLayout(10, 12));
        center.setBackground(main.getBackground());

        center.add(connection, BorderLayout.NORTH);
        center.add(form, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        // Response area
        responseArea = new JTextArea(8, 40);
        responseArea.setEditable(false);
        responseArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        responseArea.setText("Welcome to MiniRedis!\n");

        JScrollPane scroll = new JScrollPane(responseArea);

        JPanel responsePanel = new JPanel(new BorderLayout(5, 5));
        responsePanel.setBackground(Color.WHITE);
        responsePanel.setBorder(
            BorderFactory.createTitledBorder("Server Response")
        );

        responsePanel.add(scroll, BorderLayout.CENTER);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(responsePanel, BorderLayout.SOUTH);

        add(main);

        // Button actions
        connectButton.addActionListener(e -> connectToServer());
        disconnectButton.addActionListener(e -> disconnectFromServer());
        pingButton.addActionListener(e -> sendCommand("PING"));

        // SET
        setButton.addActionListener(e -> {

            String key = keyField.getText().trim();
            String value = valueField.getText().trim();
            String ttl = ttlField.getText().trim();

            if (key.isEmpty() || value.isEmpty()) {
                showMessage("Enter key and value.");
                return;
            }

            String command = "SET " + key + " " + value;

            if (!ttl.isEmpty()) {
                command += " EX " + ttl;
            }

            sendCommand(command);
        });

        // GET
        getButton.addActionListener(e -> sendKeyCommand("GET"));

        // DELETE
        delButton.addActionListener(e -> sendKeyCommand("DEL"));

        // EXISTS
        existsButton.addActionListener(e -> sendKeyCommand("EXISTS"));

        // TTL
        ttlButton.addActionListener(e -> sendKeyCommand("TTL"));

        // EXPIRE
        expireButton.addActionListener(e -> {

            String key = keyField.getText().trim();
            String ttl = ttlField.getText().trim();

            if (key.isEmpty() || ttl.isEmpty()) {
                showMessage("Enter key and TTL.");
                return;
            }

            sendCommand("EXPIRE " + key + " " + ttl);
        });
    }

    // Connect to server
    private void connectToServer() {

        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            showMessage("Already connected.");
            return;
        }

        new SwingWorker<String, Void>() {

            protected String doInBackground() throws Exception {

                socket = new Socket("localhost", 6379);

                input = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                );

                output = new PrintWriter(
                    socket.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8
                );

                return input.readLine();
            }

            protected void done() {
                try {
                    String response = get();
                    statusLabel.setText("Connected");
                    showMessage(response);

                } catch (Exception e) {
                    statusLabel.setText("Disconnected");
                    showMessage("Connection error: " + e.getMessage());
                    disconnectFromServer();
                }
            }

        }.execute();
    }

    // Disconnect from server
    private void disconnectFromServer() {

        try {
            if (socket != null) {
                socket.close();
            }

            socket = null;
            input = null;
            output = null;

            statusLabel.setText("Disconnected");
            showMessage("Disconnected from server.");

        } catch (IOException e) {
            showMessage("Error: " + e.getMessage());
        }
    }

    // Send key-based commands
    private void sendKeyCommand(String command) {

        String key = keyField.getText().trim();

        if (key.isEmpty()) {
            showMessage("Please enter a key.");
            return;
        }

        sendCommand(command + " " + key);
    }

    // Clear all input fields
    private void clearFields() {

        keyField.setText("");
        valueField.setText("");
        ttlField.setText("");

        keyField.requestFocusInWindow();
    }

    // Send command to server
    private void sendCommand(String command) {

        new SwingWorker<String, Void>() {

            protected String doInBackground() throws Exception {

                if (socket == null || socket.isClosed()
                        || output == null || input == null) {

                    throw new IOException("Not connected to server.");
                }

                output.println(command);

                String response = input.readLine();

                if (response == null) {
                    throw new IOException("Server disconnected.");
                }

                return response;
            }

            protected void done() {

                try {
                    String response = get();

                    showMessage(command + "\nResponse: " + response);

                    // Clear fields after successful response
                    clearFields();

                } catch (Exception e) {

                    showMessage("Error: " + e.getMessage());
                    statusLabel.setText("Disconnected");
                }
            }

        }.execute();
    }

    // Display server response
    private void showMessage(String message) {
        responseArea.append("\n" + message + "\n");
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            MiniRedisGUI gui = new MiniRedisGUI();
            gui.setVisible(true);

        });
    }
}
