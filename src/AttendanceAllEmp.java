import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AttendanceAllEmp extends JPanel {
    private JTextField dateField;
    private JButton searchButton;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    public AttendanceAllEmp() {
        setLayout(new BorderLayout());

        // Create the panel for the "Attendance Report" label
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(250, 246, 228));
        JLabel titleLabel = new JLabel("Attendance Report", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Bodoni MT", Font.BOLD, 36));
        titlePanel.add(titleLabel);

        // Create the panel for the rest of the content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(250, 246, 228));

        // Create search components
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new Color(250, 246, 228));
        JLabel dateLabel = new JLabel("Select Date: ");
        dateLabel.setFont(new Font("Bodoni MT", Font.PLAIN, 20));
        dateField = new JTextField(10);
        dateField.setFont(new Font("Bodoni MT", Font.PLAIN, 20));
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Bodoni MT", Font.BOLD, 20));

        // Add action listener to the search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAttendance();
            }
        });

        searchPanel.add(dateLabel);
        searchPanel.add(dateField);
        searchPanel.add(searchButton);

        // Create table for displaying attendance details
        tableModel = new DefaultTableModel(new String[]{"Employee ID", "Employee Name", "Job Title", "Sign In Time", "Sign Out Time"}, 0);
        attendanceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(attendanceTable);

        // Add components to content panel
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Add panels to the main panel
        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void searchAttendance() {
        String dateString = dateField.getText().trim();

        // Clear previous search results
        tableModel.setRowCount(0);

        if (dateString.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String url = "jdbc:mysql://localhost:3306/bitApp";
        String user = "root";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT a.employee_id, e.first_name, e.last_name, j.jobTitle, a.signInTime, a.signOutTime " +
                    "FROM Attendance a " +
                    "INNER JOIN employeeDetails e ON a.employee_id = e.employee_id " +
                    "INNER JOIN jobroles j ON e.jobId = j.jobId " +
                    "WHERE a.aDate = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDate(1, Date.valueOf(date));

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String employeeId = resultSet.getString("employee_id");
                String employeeName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");
                String jobTitle = resultSet.getString("jobTitle");
                String signInTime = resultSet.getTimestamp("signInTime").toString();

                // Check if signOutTime is null
                String signOutTime = resultSet.getTimestamp("signOutTime") != null ?
                        resultSet.getTimestamp("signOutTime").toString() : "Not signed out";

                tableModel.addRow(new Object[]{employeeId, employeeName, jobTitle, signInTime, signOutTime});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching attendance data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
