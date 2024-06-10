import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;

public class MarkAttendance extends JFrame {
    private JTextField employeeIdField;
    private JButton submitButton;
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel notificationLabel;

    public MarkAttendance() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 246, 228)); // Set background color

        // Title label
        titleLabel = new JLabel("Employee Attendance", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Bodoni MT", Font.BOLD, 48));
        titleLabel.setBackground(new Color(250, 246, 228));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create the text field and button
        employeeIdField = new JTextField(20);
        employeeIdField.setFont(new Font("Bodoni MT", Font.PLAIN, 20));

        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Bodoni MT", Font.BOLD, 20));


        // Add action listener to the button
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markAttendance(employeeIdField.getText().trim());
            }
        });

        // Add key listener to the text field to submit on Enter key press
        employeeIdField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    markAttendance(employeeIdField.getText().trim());
                }
            }
        });

        // Notification label
        notificationLabel = new JLabel("", SwingConstants.CENTER);
        notificationLabel.setFont(new Font("Bodoni MT", Font.PLAIN, 30));
        notificationLabel.setBackground(new Color(250, 246, 228));

        // Add the text field and button to a panel
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(250, 246, 228));
        inputPanel.add(employeeIdField);
        inputPanel.add(submitButton);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Add space between components

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(notificationLabel, BorderLayout.SOUTH);

        // Set up the frame
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Employee Attendance Management");
        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    private void markAttendance(String employeeId) {
        if (employeeId.isEmpty()) {
            showNotification("Please enter a valid employee ID.", true);
            return;
        }

        LocalDate currentDate = LocalDate.now();
        LocalDateTime currentDateTime = LocalDateTime.now();

        String url = "jdbc:mysql://localhost:3306/bitApp";
        String user = "root";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Check if employee exists
            String employeeCheckQuery = "SELECT COUNT(*) FROM employeeDetails WHERE employee_id = ?";
            PreparedStatement employeeCheckStmt = connection.prepareStatement(employeeCheckQuery);
            employeeCheckStmt.setString(1, employeeId);
            ResultSet employeeCheckResult = employeeCheckStmt.executeQuery();
            if (employeeCheckResult.next() && employeeCheckResult.getInt(1) == 0) {
                showNotification("Employee not found.", true);
                return;
            }

            // Check if the employee has already signed in today
            String checkQuery = "SELECT * FROM Attendance WHERE employee_id = ? AND aDate = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, employeeId);
            checkStmt.setDate(2, Date.valueOf(currentDate));
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                // Employee has already signed in, update sign out time
                String updateQuery = "UPDATE Attendance SET signOutTime = ? WHERE employee_id = ? AND aDate = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setTimestamp(1, Timestamp.valueOf(currentDateTime));
                updateStmt.setString(2, employeeId);
                updateStmt.setDate(3, Date.valueOf(currentDate));
                updateStmt.executeUpdate();
                showNotification("Sign out time recorded successfully.", false);
            } else {
                // Employee is signing in, insert new record
                String insertQuery = "INSERT INTO Attendance (employee_id, aDate, signInTime) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setString(1, employeeId);
                insertStmt.setDate(2, Date.valueOf(currentDate));
                insertStmt.setTimestamp(3, Timestamp.valueOf(currentDateTime));
                insertStmt.executeUpdate();
                showNotification("Sign in time recorded successfully.", false);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showNotification("Error marking attendance: " + ex.getMessage(), true);
        }
    }

    private void showNotification(String message, boolean isError) {
        notificationLabel.setText(message);
        notificationLabel.setForeground(isError ? Color.RED : Color.BLUE);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        notificationLabel.setText("");
                        employeeIdField.setText("");
                    }
                });
            }
        }, 3000); // Clear the notification after 3 seconds
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MarkAttendance().setVisible(true);
            }
        });
    }
}
