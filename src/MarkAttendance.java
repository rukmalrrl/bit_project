import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class MarkAttendance extends JPanel {
    private JTextField employeeIdField;
    private JButton submitButton;
    JPanel markAttendancePanel;
    private JLabel titleLabel;
    private JLabel notificationLabel;

    public MarkAttendance() {
        markAttendancePanel = new JPanel(new BorderLayout());
        markAttendancePanel.setBackground(new Color(250, 246, 228)); // Set background color

        // Title label
        titleLabel = new JLabel("Employee Attendance", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Bodoni MT", Font.BOLD, 48));
        titleLabel.setBackground(new Color(250, 246, 228));
        markAttendancePanel.add(titleLabel, BorderLayout.NORTH);

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

        markAttendancePanel.add(inputPanel, BorderLayout.CENTER);
        markAttendancePanel.add(notificationLabel, BorderLayout.SOUTH);

        // Add markAttendancePanel to MarkAttendance
        setLayout(new BorderLayout());
        add(markAttendancePanel, BorderLayout.CENTER);
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

            // Get job title based on employee's job ID
            String jobQuery = "SELECT j.jobTitle FROM employeeDetails e INNER JOIN jobRoles j ON e.jobId = j.jobId WHERE e.employee_id = ?";
            PreparedStatement jobStmt = connection.prepareStatement(jobQuery);
            jobStmt.setString(1, employeeId);
            ResultSet jobResult = jobStmt.executeQuery();

            String jobTitle = "";
            if (jobResult.next()) {
                jobTitle = jobResult.getString("jobTitle");
            } else {
                showNotification("Job title not found for the employee.", true);
                return;
            }

            // Check if the employee has already signed in today
            String checkQuery = "SELECT * FROM Attendance WHERE employee_id = ? AND aDate = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, employeeId);
            checkStmt.setDate(2, Date.valueOf(currentDate));
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                // Employee has already signed in, update sign out time and calculate work hours and ot hours
                LocalDateTime signInTime = resultSet.getTimestamp("signInTime").toLocalDateTime();
                LocalDateTime signOutTime = currentDateTime;

                // Calculate work hours and OT hours
                LocalTime workStartTime = LocalTime.of(8, 0);
                LocalTime workEndTime = LocalTime.of(17, 0);

                float workHours = 0;
                float otHours = 0;

                if (!signInTime.toLocalDate().equals(signOutTime.toLocalDate())) {
                    // Sign out on a different day is not supported
                    showNotification("Invalid sign out time.", true);
                    return;
                }

                LocalTime signInLocalTime = signInTime.toLocalTime();
                LocalTime signOutLocalTime = signOutTime.toLocalTime();

                if (signInLocalTime.isBefore(workStartTime)) {
                    otHours += java.time.Duration.between(signInLocalTime, workStartTime).toMinutes() / 60.0;
                    signInLocalTime = workStartTime;
                }

                if (signOutLocalTime.isAfter(workEndTime)) {
                    otHours += java.time.Duration.between(workEndTime, signOutLocalTime).toMinutes() / 60.0;
                    signOutLocalTime = workEndTime;
                }

                workHours += java.time.Duration.between(signInLocalTime, signOutLocalTime).toMinutes() / 60.0;

                String updateQuery = "UPDATE Attendance SET signOutTime = ?, workHours = ?, otHours = ?, jobTitle = ? WHERE employee_id = ? AND aDate = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setTimestamp(1, Timestamp.valueOf(signOutTime));
                updateStmt.setFloat(2, workHours);
                updateStmt.setFloat(3, otHours);
                updateStmt.setString(4, jobTitle);
                updateStmt.setString(5, employeeId);
                updateStmt.setDate(6, Date.valueOf(currentDate));
                updateStmt.executeUpdate();
                showNotification("Sign out time recorded successfully.", false);
            } else {
                // Employee is signing in, insert new record
                String insertQuery = "INSERT INTO Attendance (employee_id, aDate, signInTime, jobTitle) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setString(1, employeeId);
                insertStmt.setDate(2, Date.valueOf(currentDate));
                insertStmt.setTimestamp(3, Timestamp.valueOf(currentDateTime));
                insertStmt.setString(4, jobTitle);
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
                JFrame frame = new JFrame("Employee Attendance Management");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(600, 400);
                frame.setLocationRelativeTo(null);
                frame.setContentPane(new MarkAttendance());
                frame.setVisible(true);
            }
        });
    }
}
