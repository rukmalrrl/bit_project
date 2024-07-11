import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class SalaryDetailsUpdater extends JPanel {
    private JComboBox<String> monthSelector;
    private JButton calculateButton;
    private JButton showButton;
    private JTable salaryTable;
    private DefaultTableModel tableModel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bitApp";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public SalaryDetailsUpdater() {
        setLayout(new BorderLayout());

        // Create the panel for the "Salary Report" label
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(250, 246, 228));
        JLabel titleLabel = new JLabel("Salary Report", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Bodoni MT", Font.BOLD, 36));
        titlePanel.add(titleLabel);

        // Create the panel for the rest of the content
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(250, 246, 228));
        contentPanel.setLayout(new FlowLayout());

        JLabel monthLabel = new JLabel("Select Month:");
        monthLabel.setFont(new Font("Bodoni MT", Font.PLAIN, 20));
        monthSelector = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
        monthSelector.setFont(new Font("Bodoni MT", Font.PLAIN, 20));
        calculateButton = new JButton("Calculate");
        calculateButton.setFont(new Font("Bodoni MT", Font.BOLD, 20));
        showButton = new JButton("Show");
        showButton.setFont(new Font("Bodoni MT", Font.BOLD, 20));

        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateAndUpdate();
            }
        });

        showButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDetails();
            }
        });

        contentPanel.add(monthLabel);
        contentPanel.add(monthSelector);
        contentPanel.add(calculateButton);
        contentPanel.add(showButton);

        // Create the table for displaying salary details
        tableModel = new DefaultTableModel(new String[]{"Employee ID", "Name", "Job Title", "Total Work Hours", "Total OT Hours", "Salary"}, 0);
        salaryTable = new JTable(tableModel);
        salaryTable.setFont(new Font("Bodoni MT", Font.PLAIN, 15));
        JScrollPane scrollPane = new JScrollPane(salaryTable);

        // Add panels to the main panel
        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void calculateAndUpdate() {
        String selectedMonth = (String) monthSelector.getSelectedItem();
        int selectedMonthIndex = monthSelector.getSelectedIndex() + 1; // January is 1, February is 2, etc.

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String getEmployeesQuery = "SELECT DISTINCT employee_id FROM attendance";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(getEmployeesQuery);

            while (rs.next()) {
                String employeeId = rs.getString("employee_id");

                String getTotalHoursQuery = "SELECT SUM(workHours) as totalWorkHours, SUM(otHours) as totalOTHours FROM attendance WHERE employee_id = ? AND MONTH(aDate) = ?";
                PreparedStatement pstmt = connection.prepareStatement(getTotalHoursQuery);
                pstmt.setString(1, employeeId);
                pstmt.setInt(2, selectedMonthIndex);
                ResultSet hoursResultSet = pstmt.executeQuery();

                if (hoursResultSet.next() && hoursResultSet.getFloat("totalWorkHours") > 0) {
                    float totalWorkHours = hoursResultSet.getFloat("totalWorkHours");
                    float totalOTHours = hoursResultSet.getFloat("totalOTHours");

                    String getJobDetailsQuery = "SELECT e.first_name, e.last_name, j.jobTitle, j.hourlyRate, j.otrate, j.allowances, j.deductions " +
                            "FROM employeeDetails e " +
                            "INNER JOIN jobRoles j ON e.jobId = j.jobId " +
                            "WHERE e.employee_id = ?";
                    PreparedStatement jobPstmt = connection.prepareStatement(getJobDetailsQuery);
                    jobPstmt.setString(1, employeeId);
                    ResultSet jobResultSet = jobPstmt.executeQuery();

                    if (jobResultSet.next()) {
                        String firstName = jobResultSet.getString("first_name");
                        String lastName = jobResultSet.getString("last_name");
                        String jobTitle = jobResultSet.getString("jobTitle");
                        float hourlyRate = jobResultSet.getFloat("hourlyRate");
                        float otRate = jobResultSet.getFloat("otrate");
                        float allowances = jobResultSet.getFloat("allowances");
                        float deductions = jobResultSet.getFloat("deductions");

                        float salary = (totalWorkHours * hourlyRate) + (totalOTHours * otRate) + allowances - deductions;

                        String updateSalaryDetailsQuery = "REPLACE INTO salaryDetails (employee_id, month, first_name, last_name, jobTitle, total_work_hours, total_ot_hours, salary) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement updatePstmt = connection.prepareStatement(updateSalaryDetailsQuery);
                        updatePstmt.setString(1, employeeId);
                        updatePstmt.setString(2, selectedMonth);
                        updatePstmt.setString(3, firstName);
                        updatePstmt.setString(4, lastName);
                        updatePstmt.setString(5, jobTitle);
                        updatePstmt.setFloat(6, totalWorkHours);
                        updatePstmt.setFloat(7, totalOTHours);
                        updatePstmt.setFloat(8, salary);
                        updatePstmt.executeUpdate();
                    }
                } else {
                    String getEmployeeDetailsQuery = "SELECT e.first_name, e.last_name, j.jobTitle " +
                            "FROM employeeDetails e " +
                            "INNER JOIN jobRoles j ON e.jobId = j.jobId " +
                            "WHERE e.employee_id = ?";
                    PreparedStatement empPstmt = connection.prepareStatement(getEmployeeDetailsQuery);
                    empPstmt.setString(1, employeeId);
                    ResultSet empResultSet = empPstmt.executeQuery();

                    if (empResultSet.next()) {
                        String firstName = empResultSet.getString("first_name");
                        String lastName = empResultSet.getString("last_name");
                        String jobTitle = empResultSet.getString("jobTitle");

                        String updateSalaryDetailsQuery = "REPLACE INTO salaryDetails (employee_id, month, first_name, last_name, jobTitle, total_work_hours, total_ot_hours, salary) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement updatePstmt = connection.prepareStatement(updateSalaryDetailsQuery);
                        updatePstmt.setString(1, employeeId);
                        updatePstmt.setString(2, selectedMonth);
                        updatePstmt.setString(3, firstName);
                        updatePstmt.setString(4, lastName);
                        updatePstmt.setString(5, jobTitle);
                        updatePstmt.setFloat(6, 0);
                        updatePstmt.setFloat(7, 0);
                        updatePstmt.setFloat(8, 0);
                        updatePstmt.executeUpdate();
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Salary details updated for month: " + selectedMonth, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating salary details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetails() {
        String selectedMonth = (String) monthSelector.getSelectedItem();

        // Clear previous search results
        tableModel.setRowCount(0);

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String query = "SELECT * FROM salaryDetails WHERE month = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, selectedMonth);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String employeeId = rs.getString("employee_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String jobTitle = rs.getString("jobTitle");
                float totalWorkHours = rs.getFloat("total_work_hours");
                float totalOTHours = rs.getFloat("total_ot_hours");
                float salary = rs.getFloat("salary");

                tableModel.addRow(new Object[]{employeeId, firstName + " " + lastName, jobTitle, totalWorkHours, totalOTHours, salary});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching salary details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Salary Details Updater");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.add(new SalaryDetailsUpdater());
        frame.setVisible(true);
    }
}
