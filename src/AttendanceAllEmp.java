import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;

import java.util.Vector;

public class AttendanceAllEmp extends JFrame {
    private JTextField dateField;
    private JButton searchButton;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    public AttendanceAllEmp() {
        setTitle("Attendance Report");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Create search components
        JPanel searchPanel = new JPanel();
        JLabel dateLabel = new JLabel("Select Date: ");
        dateField = new JTextField(10);
        searchButton = new JButton("Search");

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
        panel.add(searchPanel, BorderLayout.NORTH);

        // Create table for displaying attendance details
        tableModel = new DefaultTableModel(new String[]{"Employee ID", "Sign In Time", "Sign Out Time", "Work Hours", "OT Hours"}, 0);
        attendanceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(panel);
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
            String query = "SELECT * FROM Attendance WHERE aDate = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDate(1, Date.valueOf(date));

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String employeeId = resultSet.getString("employee_id");
                String signInTime = resultSet.getTimestamp("signInTime").toString();
                String signOutTime = resultSet.getTimestamp("signOutTime").toString();
                float workHours = resultSet.getFloat("workHours");
                float otHours = resultSet.getFloat("otHours");

                tableModel.addRow(new Object[]{employeeId, signInTime, signOutTime, workHours, otHours});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching attendance data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AttendanceAllEmp().setVisible(true);
            }
        });
    }
}
