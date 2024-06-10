import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AttendanceOneEmp extends JFrame {
    private JTextField searchField;
    private JButton searchButton;
    private JLabel nameLabel;
    private JLabel jobTitleLabel;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    public AttendanceOneEmp() {
        setTitle("Attendance Report");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 246, 228));

        // Create search components
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Bodoni MT", Font.BOLD, 20));
        searchField.setFont(new Font("Bodoni MT", Font.PLAIN, 18));
        searchPanel.setBackground(new Color(250, 246, 228));

        // Add action listener to the search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchAttendance();
                attendanceTable.setVisible(true);
            }
        });

        // Add key listener to the search field to perform search on Enter key press
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchAttendance();
                    attendanceTable.setVisible(true);
                }
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Create labels for displaying employee name and job title
        JPanel labelPanel = new JPanel(new GridLayout(2, 1));
        nameLabel = new JLabel("", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Bodoni MT", Font.BOLD, 20));
        labelPanel.setBackground(new Color(250, 246, 228));
        labelPanel.add(nameLabel);

        jobTitleLabel = new JLabel("", SwingConstants.CENTER);
        jobTitleLabel.setFont(new Font("Bodoni MT", Font.PLAIN, 18));
        labelPanel.add(jobTitleLabel);

        panel.add(labelPanel, BorderLayout.CENTER);

        // Create table for displaying attendance details
        tableModel = new DefaultTableModel(new String[]{"Employee ID", "Date", "Sign In Time", "Sign Out Time", "Work Hours", "OT Hours"}, 0);
        attendanceTable = new JTable(tableModel);
        attendanceTable.setVisible(false); // Initially hide the table
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        panel.add(scrollPane, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void searchAttendance() {
        String searchText = searchField.getText().trim();

        // Clear previous search results
        tableModel.setRowCount(0);
        nameLabel.setText("");
        jobTitleLabel.setText("");

        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an employee ID or name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        String url = "jdbc:mysql://localhost:3306/bitApp";
        String user = "root";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Fetch employee details including name and job title
            String employeeQuery = "SELECT first_name, last_name, jobId FROM employeedetails WHERE employee_id = ?";
            PreparedStatement employeeStatement = connection.prepareStatement(employeeQuery);
            employeeStatement.setString(1, searchText);
            ResultSet employeeResult = employeeStatement.executeQuery();

            if (employeeResult.next()) {
                String firstName = employeeResult.getString("first_name");
                String lastName = employeeResult.getString("last_name");
                String jobId = employeeResult.getString("jobId");
                nameLabel.setText("Name: " + firstName + " " + lastName);

                // Fetch job title from jobroles table
                String jobQuery = "SELECT jobTitle FROM jobroles WHERE jobId = ?";
                PreparedStatement jobStatement = connection.prepareStatement(jobQuery);
                jobStatement.setString(1, jobId);
                ResultSet jobResult = jobStatement.executeQuery();
                if (jobResult.next()) {
                    String jobTitle = jobResult.getString("jobTitle");
                    jobTitleLabel.setText("Job Title: " + jobTitle);
                }
            }

            // Fetch attendance details
            String attendanceQuery = "SELECT * FROM Attendance WHERE employee_id = ? AND aDate BETWEEN ? AND ?";
            PreparedStatement statement = connection.prepareStatement(attendanceQuery);
            statement.setString(1, searchText);
            statement.setDate(2, Date.valueOf(firstDayOfMonth));
            statement.setDate(3, Date.valueOf(lastDayOfMonth));

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String employeeId = resultSet.getString("employee_id");
                String date = resultSet.getDate("aDate").toString();
                String signInTime = resultSet.getTimestamp("signInTime").toString();
                String signOutTime = resultSet.getTimestamp("signOutTime").toString();
                float workHours = resultSet.getFloat("workHours");
                float otHours = resultSet.getFloat("otHours");

                tableModel.addRow(new Object[]{employeeId, date, signInTime, signOutTime, workHours, otHours});
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
                new AttendanceOneEmp().setVisible(true);
            }
        });
    }
}
